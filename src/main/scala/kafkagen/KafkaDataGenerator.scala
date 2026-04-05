package kafkagen

import config.AppConfig._
import org.apache.kafka.clients.producer._
import java.sql.Timestamp
import java.util.Properties
import scala.util.Random

object NoModificar {
    // Esta parte desgraciadamente no la podemos modificar porque simula lo que nos envía los dispositivos en su formato
    // Sí que podemos en tiempo de desarrollo probar a enviar datos mal formateados para ver cómo los podríamos gestionar
  
    private val props = new Properties()
    props.put("bootstrap.servers", kafkaBootstrapServers)
    props.put("key.serializer"  , "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    private val producer = new KafkaProducer[String, String](props)

    def formatMessage(topic: String, sensorId: String, value: Double, timestamp: Timestamp): String = {
        topic match {
            case "temperature_humidity" => s"$sensorId,$value,$value,$timestamp"
            case "co2" => s"$sensorId,$value,$timestamp"
            case "soil_moisture" => s"$sensorId,$value,$timestamp"
            case _ => throw new IllegalArgumentException(s"Topic no válido: $topic")
        }
    }

  def sendData(topic: String, sensorId: String, value: Double, timestamp: Timestamp): Unit = {
    val message = formatMessage(topic, sensorId, value, timestamp)
    val record = new ProducerRecord[String, String](topic, sensorId, message)
    println(s"Enviando datos: $message al topic $topic")
    producer.send(record)

    if (Random.nextInt(100) < 10) {
      println(s"--- SIMULANDO DUPLICADO ---")
      producer.send(record)
    }
  }

  def close(): Unit = {
    producer.close()
  }
}

object KafkaDataGenerator {

  def main(args: Array[String]): Unit = {
    val maxRecords = 1000000
    val maxDevices = 9
    val sleepMs = 200 // Milisegundos de espera entre cada mensaje enviado

    for (j <- 1 to maxRecords) {
        for (i <- 1 to maxDevices) {
          val timestamp = if (j % 20 == 0) {
            new Timestamp(System.currentTimeMillis() - 10000) // 10 segundos tarde
          } else {
            new Timestamp(System.currentTimeMillis())
          }

          if (j % 50 == 0 && i == 3) {
            NoModificar.sendData("temperature_humidity", s"sensor-chungo-$i", Math.random() * 100, timestamp)
            NoModificar.sendData("co2", s"sensor-chungo-$i", Math.random() * 100, timestamp)
            NoModificar.sendData("soil_moisture", s"sensor-chungo-$i", Math.random() * 100, timestamp)
          } else {
            NoModificar.sendData("temperature_humidity", s"sensor$i", Math.random() * 100, timestamp)
            NoModificar.sendData("co2", s"sensor$i", Math.random() * 100, timestamp)
            NoModificar.sendData("soil_moisture", s"sensor$i", Math.random() * 100, timestamp)
            Thread.sleep(sleepMs)
          }
        }
    }
    NoModificar.close()
  }
}