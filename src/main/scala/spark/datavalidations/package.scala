package spark

import domain.IoTDomain.TemperatureHumidityData

import scala.util.matching.Regex

package object datavalidations {

  // Expresión regular para validar el formato de sensorId: alfanumérico, guiones y guiones bajos.
  val sensorIdRegex: Regex = """^[a-zA-Z0-9-_]+$""".r

  // Número de campos esperados para el sensor de Temperatura y Humedad.
  val ExpectedFieldCount = 4

  // Definición de alias de tipos (Type Aliases) para mejorar la legibilidad del código.
  private type RawSensorData = String
  type ListOfRawSensorData = Seq[RawSensorData]
  
  // Representa el resultado de una validación: o bien un error (String) o los datos validados.
  type ValidatedSensorData = Either[String, TemperatureHumidityData]
}
