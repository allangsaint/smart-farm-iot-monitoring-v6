package spark

import org.apache.spark.sql.SparkSession
import Implicits.Memory
import Implicits.IntWithMemorySize


sealed trait LogLevel {
  def level: String
}

case object ErrorLevel extends LogLevel {
  override def level: String = "ERROR"
}

case object WarnLevel extends LogLevel {
  override def level: String = "WARN"
}

case object InfoLevel extends LogLevel {
  override def level: String = "INFO"
}

case object DebugLevel extends LogLevel {
  override def level: String = "DEBUG"
}

object Log {
  def level(logLevel: LogLevel): Unit = {
    // You can call your 'setLogLevel' function here
    // Aquí puedes llamar a tu función 'setLogLevel'
    println(s"Estableciendo nivel de log a ${logLevel.level}")
  }
}

/**
 * Trait que proporciona una sesión de Spark configurada.
 * Utiliza un patrón Builder para permitir una configuración flexible antes de construir la sesión.
 */
trait SparkSessionWrapper {
  implicit val spark: SparkSession = createSparkSession.build

  /**
   * Clase interna para construir la sesión de Spark de forma fluida.
   */
  class SessionBuilder {
    private var driverCores = 1
    private var executorCores = 1
    private var logLevel = "ERROR"
    private var driverMemory: Memory = 1.Gb
    private var executorMemory: Memory = 1.Gb
    private var appName = "spark session"
    private var offHeapEnabled = false
    private var offHeapGbSize: Memory = 0.Gb
    private var withCustomAppName = false
    private var hiveSupportEnabled = false
    private var deltaLakeSupportEnabled = false
    private var shufflePartitionsTuned = false
    private var shufflePartitions: Int = 10
    private var checkPointLocationEnabled = false
    private var checkPointLocation = "./tmp/checkpoint"

    /**
     * Configura el nivel de logs (ERROR, WARN, INFO, DEBUG).
     */
    def withLogLevel(level: LogLevel): SessionBuilder = {
      logLevel = level.level
      this
    }

    /**
     * Configura la memoria del Driver.
     */
    def withDriverMemory(memory: Memory): SessionBuilder = {
      driverMemory = memory
      this
    }

    /**
     * Configura la memoria de los Ejecutores.
     */
    def withExecutorMemory(memory: Memory): SessionBuilder = {
      executorMemory = memory
      this
    }

    /**
     * Configura el número de núcleos para el Driver.
     */
    def withDriverCores(cores: Int): SessionBuilder = {
      driverCores = cores
      this
    }

    /**
     * Configura el número de núcleos para los Ejecutores.
     */
    def withExecutorCores(cores: Int): SessionBuilder = {
      executorCores = cores
      this
    }

    /**
     * Define el nombre de la aplicación Spark.
     */
    def withName(name: String): SessionBuilder = {
      withCustomAppName = true
      appName = name
      this
    }

    /**
     * Habilita el uso de memoria Off-Heap.
     */
    def withOffHeapEnabled: SessionBuilder = {
      offHeapEnabled = true
      this
    }

    /**
     * Define el tamaño de la memoria Off-Heap.
     */
    def withOffHeapGbSize(size: Memory): SessionBuilder = {
      offHeapGbSize = size
      this
    }

    /**
     * Habilita el soporte para Hive.
     */
    def withEnableHiveSupport: SessionBuilder = {
      hiveSupportEnabled = true
      this
    }

    /**
     * Habilita el soporte para Delta Lake.
     * Configura las extensiones y el catálogo necesarios para Delta.
     */
    def withDeltaLakeSupport: SessionBuilder = {
      deltaLakeSupportEnabled = true
      this
    }

    /**
     * Ajusta el número de particiones para shuffles (uniones, agregaciones).
     * Por defecto en Spark es 200, aquí lo solemos bajar para desarrollo local.
     */
    def withTunedShufflePartitions(numParts: Int): SessionBuilder = {
      shufflePartitionsTuned = true
      shufflePartitions = numParts
      this
    }

    /**
     * Define la ubicación para los checkpoints de streaming.
     */
    def withCheckpointLocation(location: String): SessionBuilder = {
      checkPointLocationEnabled = true
      checkPointLocation = location
      this
    }

     /**
     * Método privado que construye la SparkSession con todas las configuraciones aplicadas.
     */
    private def buildSparkSession(appName: String): SparkSession = {

      var builder: SparkSession.Builder = SparkSession
        .builder()
        .master("local[*]")
        .appName(appName)
        .config("spark.driver.memory", driverMemory.toString)
        .config("spark.executor.memory", executorMemory.toString)
        .config("spark.driver.cores", driverCores.toString)
        .config("spark.executor.cores", executorCores.toString)
        // Desactivamos la web UI de Spark para evitar problemas con los tests
        .config("spark.ui.enabled", "false")
        // Desactivamos todas las compresiones para facilitar las pruebas y depuración
        .config("spark.rdd.compress", "false")                    // Desactivar compresión de RDDs
        .config("spark.shuffle.compress", "false")                // Desactivar compresión de shuffle
        .config("spark.shuffle.spill.compress", "false")          // Desactivar compresión de spill de shuffle
        .config("spark.broadcast.compress", "false")              // Desactivar compresión de broadcast
        .config("spark.sql.parquet.compression.codec", "none")    // Desactivar compresión Parquet
        .config("spark.sql.orc.compression.codec", "none")        // Desactivar compresión ORC
        .config("spark.sql.json.compression.codec", "none")       // Desactivar compresión JSON
        .config("spark.sql.avro.compression.codec", "uncompressed")       // Desactivar compresión Avro
        .config("spark.sql.csv.compression.codec", "none")        // Desactivar compresión CSV


      if (offHeapEnabled) {
        builder = builder.config("spark.memory.offHeap.enabled", "true")
        builder = builder.config("spark.memory.offHeap.size", offHeapGbSize.toString)
      }

      if (hiveSupportEnabled) {
        builder = builder.enableHiveSupport()
      }
      if (deltaLakeSupportEnabled) {
        builder = builder.config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
        builder = builder.config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
      }

      if (shufflePartitionsTuned) {
        builder = builder.config("spark.shuffle.partitions", shufflePartitions)
      }

      if (checkPointLocationEnabled) {
        builder = builder.config("spark.sql.streaming.checkpointLocation", checkPointLocation)
      }

      builder.getOrCreate()
    }

    def build: SparkSession = {
      val session = buildSparkSession("spark session")
      setLogLevel(session, logLevel)
      session
    }

    def build(name: String): SparkSession = {
      val session = buildSparkSession(name)
      setLogLevel(session, logLevel)
      session
    }
  }

  def createSparkSession: SessionBuilder = new SessionBuilder

  private def setLogLevel(spark: SparkSession, logLevel: String): Unit = spark.sparkContext.setLogLevel(logLevel)
}

// Ejemplo de uso con soporte para Delta Lake habilitado
object SparkSessionWrapperApp extends SparkSessionWrapper with App {

  override implicit val spark: SparkSession = createSparkSession
    .withCheckpointLocation("./tmp")
    .withDeltaLakeSupport
    .build

}

