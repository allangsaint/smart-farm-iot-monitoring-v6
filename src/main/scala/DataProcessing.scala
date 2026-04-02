package spark

import config.AppConfig.kafkaBootstrapServers
import org.apache.spark.sql.{Dataset, SparkSession}

import java.sql.Timestamp

/**
 * Objeto que contiene las funciones para el procesamiento inicial de datos desde fuentes externas (Kafka).
 */
object DataProcessing {
  /**
   * Lee un flujo (stream) de datos desde un topic de Kafka.
   *
   * @param topic El nombre del topic de Kafka a suscribirse.
   * @param spark La sesión de Spark actual.
   * @return Un Dataset que contiene tuplas de (valor en cadena, timestamp del mensaje).
   */
  def getKafkaStream(topic: String, spark: SparkSession): Dataset[(String, Timestamp)] = {
    import spark.implicits._
    
    // Leemos de Kafka usando el formato "kafka"
    spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServers) // Direcciones de los servidores Kafka
      .option("subscribe", topic)                                // Topic al que nos suscribimos
      .option("startingOffsets", "latest")                      // Empezar a leer desde el último mensaje
      .option("failOnDataLoss", "false")                        // Evitar fallos si se pierden datos antiguos en Kafka
      .load()
      // Kafka entrega los datos como binarios, los convertimos a tipos legibles (String y Timestamp)
      .selectExpr("CAST(value AS STRING)", "CAST(timestamp AS TIMESTAMP)")
      .as[(String, Timestamp)]
  }
}
