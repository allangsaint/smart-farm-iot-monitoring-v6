package domain

import java.sql.Timestamp

object IoTDomain {

  // Clase para representar los datos de un sensor de humedad del suelo.
  // Las Case Classes son ideales para modelar datos inmutables en Scala/Spark.
  final case class SoilMoistureData(sensorId: String, soilMoisture: Double, timestamp: Timestamp)

  // Clase para representar los datos de un sensor de temperatura y humedad.
  // zoneId es opcional (Option) ya que inicialmente no sabemos a qué zona pertenece el sensor.
  final case class TemperatureHumidityData(sensorId: String, temperature: Double, humidity: Double, timestamp: Timestamp, zoneId: Option[String] = None)

  // Clase para representar los datos de un sensor de nivel de CO2.
  final case class CO2Data(sensorId: String, co2Level: Double, timestamp: Timestamp, zoneId: Option[String] = None)

}
