package config

import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import scala.jdk.CollectionConverters._

object AppConfig {

  // TODO: ¿Qué tal si usamos el fichero application.conf y TypeSafe Config?
  val config: Config = ConfigFactory.load()
  // Configuración de Kafka
  //val kafkaBootstrapServers = "localhost:9092"
  val kafkaBootstrapServers: String = config.getString("kafka.bootstrap.servers")
  //val temperatureHumidityTopic = "temperature_humidity"
  val temperatureHumidityTopic: String = config.getString("topics.temperature-humidity")

  val co2Topic: String = config.getString("topics.co2")
  val soilMoistureTopic: String = config.getString("topics.soil-moisture")

  // Rutas de directorios
  val rutaBase: String = config.getString("ruta-base") // "./tmp/"
  val sufijoCheckpoint ="_chk"


  object Tablas {
    val RawTemperatureHumidityZone = "raw_temperature_humidity_zone"
    val TemperatureHumidityZoneMerge = "temperature_humidity_zone_merge"
  }

  def getRutaParaTabla(nombreTabla: String): String = {
    rutaBase + nombreTabla
  }

  def getRutaParaTablaChk(nombreTabla: String): String = {

    getRutaParaTabla(nombreTabla) + sufijoCheckpoint
  }

  /**
   * Carga el mapeo de sensores a zonas desde el archivo JSON de recursos.
   * Construye un mapa de sensorId (ej. "sensor1") a zoneId (ej. "zona1").
   */
  private lazy val dynamicSensorToZoneMap: Map[String, String] = {
    try {
      val mappingConfig = ConfigFactory.parseResources("mapping_zonas.json")
      val zonas = mappingConfig.getConfigList("zonas").asScala

      zonas.flatMap { zonaConfig =>
        val zonaId = s"zona${zonaConfig.getInt("id")}"
        val sensores = zonaConfig.getConfigList("sensores").asScala
        sensores.map { sensorConfig =>
          val sensorId = s"sensor${sensorConfig.getInt("id")}"
          sensorId -> zonaId
        }
      }.toMap
    } catch {
      case e: Exception =>
        println(s"Error al cargar mapping_zonas.json: ${e.getMessage}")
        Map.empty[String, String]
    }
  }

  /**
   * Asigna un sensor a su zona correspondiente.
   * Ahora utiliza la información cargada desde el fichero JSON.
   *
   * @param sensorId Identificador del sensor.
   * @return Identificador de la zona asignada o "unknown" si no se encuentra.
   */
  def assignSensorToZone(sensorId: String): String = {
    dynamicSensorToZoneMap.getOrElse(sensorId, "unknown")
  }

}

object ConfigApp extends App {
  import AppConfig.Tablas._
  import AppConfig._

  // Para ver más detalles de las pruebas, consulte src/test/scala/config/ConfigTest.scala
  println("Ejecutando comprobaciones rápidas de AppConfig...")
  
  println(s"Ruta Tabla: ${getRutaParaTabla(RawTemperatureHumidityZone)}")
  println(s"Prueba de asignación dinámica: sensor1 -> ${assignSensorToZone("sensor1")}")
  
  println("Finalizado. Utilice 'sbt test' para una validación completa.")
}
