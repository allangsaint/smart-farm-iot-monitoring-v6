import config.AppConfig
import config.AppConfig._
import domain.IoTDomain._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession 
import org.apache.spark.sql.RelationalGroupedDataset
import spark.datavalidations.DataValidationsV2
import spark.{ErrorLevel, SparkSessionWrapper, SparkUtils}
import utils.DataTransformations._
import utils.DirectoryCleaner

object Main extends SparkUtils with SparkSessionWrapper {
  private val isDevEnvironment = true

  // Configuración de Spark Session
  override implicit val spark: SparkSession = createSparkSession
    .withName("IoT Farm Monitoring Final")
    .withCheckpointLocation("./tmp/checkpoint")
    .withTunedShufflePartitions(10)
    .withDeltaLakeSupport
    .withLogLevel(ErrorLevel)
    .build

  def main(args: Array[String]): Unit = {
    import spark.implicits._

    // Limpieza y Rutas dinamicas
    if (isDevEnvironment) {
      DirectoryCleaner.cleanTempDirectory(rutaBase)
    }

    // Accumulator para conteo de errores
    val errorAccumulator = spark.sparkContext.longAccumulator("ErroresValidacion")

    // Broadcast join (Carga desde json)
    val zonasStaticDF = spark.read
      .option("multiLine", true)
      .json("src/main/resources/mapping_zonas.json") 
      .select(explode($"zonas").as("zona"))
      .select(
        $"zona.id".as("zoneId"),
        $"zona.nombre".as("nombreZona"),
        explode($"zona.sensores").as("sensor")
      )
      .select($"zoneId", $"nombreZona", $"sensor.id".as("sensorId"), $"sensor.tipo")

    val zonasBroadcast = broadcast(zonasStaticDF)

    // PROCESAMIENTO DE TEMPERATURA Y HUMEDAD
    val temperatureHumidityDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServers)
      .option("subscribe", temperatureHumidityTopic)
      .load()
      .selectExpr("CAST(value AS STRING)").as[String]
      .map { line =>
        val result = DataValidationsV2.validarDatosSensorTemperature(line)
        if (result.isInvalid) errorAccumulator.add(1)
        result.toOption
      }.flatMap(x => x).toDF()
      .withWatermark("timestamp", "5 minutes")
      .dropDuplicates("sensorId", "timestamp") // Deduplicacion

    // PROCESAMIENTO DE CO2
    val co2DF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServers)
      .option("subscribe", co2Topic)
      .load()
      .selectExpr("CAST(value AS STRING)").as[String]
      .map { line =>
        val result = DataValidationsV2.validarDatosSensorCO2(line)
        if (result.isInvalid) errorAccumulator.add(1)
        result.toOption
      }.flatMap(x => x).toDF()
      .withWatermark("timestamp", "5 minutes")

    // PROCESAMIENTO DE HUMEDAD DEL SUELO
    val soilMoistureDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServers)
      .option("subscribe", soilMoistureTopic)
      .load()
      .selectExpr("CAST(value AS STRING)").as[String]
      .map { line =>
        val result = DataValidationsV2.validarDatosSensorSoil(line)
        if (result.isInvalid) errorAccumulator.add(1)
        result.toOption
      }.flatMap(x => x).toDF()
      .withWatermark("timestamp", "5 minutes")

    // ENRIQUECIMIENTO (Broadcast Join para todos los sensores)
    val enrichedTempDF = temperatureHumidityDF.join(zonasBroadcast, "sensorId")
    val enrichedCO2DF = co2DF.join(zonasBroadcast, "sensorId")

    // Persistencia en delta (usando tablas de AppConfig)
    enrichedTempDF.writeStream
      .format("delta")
      .option("checkpointLocation", getRutaParaTablaChk(Tablas.RawTemperatureHumidityZone))
      .trigger(Trigger.ProcessingTime("10 second"))
      .start(getRutaParaTabla(Tablas.RawTemperatureHumidityZone))

    // Analitica con cube
    // Temperatura promedio por sensor y zona con ventana de 1 hora
    val tempAnalyticsCube = enrichedTempDF
      .cube(
        window($"timestamp", "1 hour"),
        $"zoneId",
        $"sensorId"
      )
      .agg(
        avg("temperature").as("avg_temp"),
        max("temperature").as("max_temp"), // T6
        count("sensorId").as("total_lecturas") //T6
      )

    // Esta seria la salida
    val queryCube = tempAnalyticsCube.writeStream
      .outputMode("complete")
      .format("console")
      .option("truncate", "false")
      .start()

    // Mostrando el CO2 por sensor
    val queryCO2 = enrichedCO2DF.groupBy(window($"timestamp", "1 minute"), $"sensorId", $"nombreZona")
      .agg(avg("co2Level").as("avg_co2"))
      .writeStream
      .outputMode("complete")
      .format("console")
      .start()

    spark.streams.awaitAnyTermination()
  }
}