package spark.datavalidations

import cats.data.ValidatedNel
import cats.implicits._
import domain.IoTDomain._
import java.sql.Timestamp
import scala.util.Try

object DataValidationsV2 {

  // Definimos un tipo para manejar miltiples errores a la vez (Nel = NonEmptyList)
  type ValidationResult[A] = ValidatedNel[String, A]

  // 1. Validador de ID: Revisa que el ID sea alfanumerico
  def validateSensorId(id: String): ValidationResult[String] = {
    if (id.matches("^[a-zA-Z0-9-]+$")) id.validNel 
    else s"ID de sensor invalido: $id".invalidNel
  }

  // 2. Validador de Numeros: Intenta convertir el texto a numero decimal
  def validateDouble(v: String, campo: String): ValidationResult[Double] = {
    Try(v.toDouble).toOption
      .map(_.validNel)
      .getOrElse(s"El valor de $campo no es un numero valido: $v".invalidNel)
  }

  // 3. Validador de Fecha: Revisa que el formato sea yyyy-mm-dd hh:mm:ss
  def validateTimestamp(ts: String): ValidationResult[Timestamp] = {
    Try(Timestamp.valueOf(ts)).toOption
      .map(_.validNel)
      .getOrElse(s"Formato de fecha incorrecto: $ts".invalidNel)
  }

  // T2: Validacion para CO2
  def validarDatosSensorCO2(line: String): ValidationResult[CO2Data] = {
    val p = line.split(",")
    if (p.length != 3) {
      "La linea de CO2 no tiene los 3 campos requeridos".invalidNel
    } else {
      // Revision de los 3 campos y si fallan, dice todos los errores
      (validateSensorId(p(0)), validateDouble(p(1), "CO2"), validateTimestamp(p(2)))
        .mapN((id, lvl, time) => CO2Data(id, lvl, time, None))
    }
  }

  // T2: Validacion para Humedad del Suelo
  def validarDatosSensorSoil(line: String): ValidationResult[SoilMoistureData] = {
    val p = line.split(",")
    if (p.length != 3) {
      "La linea de Soil no tiene los 3 campos requeridos".invalidNel
    } else {
      (validateSensorId(p(0)), validateDouble(p(1), "Soil Moisture"), validateTimestamp(p(2)))
        .mapN((id, mst, time) => SoilMoistureData(id, mst, time))
    }
  }

  // T1: Validacion para Temperatura
  def validarDatosSensorTemperature(line: String): ValidationResult[TemperatureHumidityData] = {
    val p = line.split(",")
    if (p.length != 4) {
      "La linea de Temperatura no tiene los 4 campos requeridos".invalidNel
    } else {
      (
        validateSensorId(p(0)), 
        validateDouble(p(1), "Temperatura"), 
        validateDouble(p(2), "Humedad"), 
        validateTimestamp(p(3))
      ).mapN((id, temp, hum, time) => TemperatureHumidityData(id, temp, hum, time, None))
    }
  }
}
