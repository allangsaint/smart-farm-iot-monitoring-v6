package spark.datavalidations

import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.col

import java.sql.Timestamp
import scala.util.{Success, Try}

object DataValidations {

  import domain.IoTDomain._

  private val logger = org.apache.log4j.Logger.getLogger(this.getClass)

  val unknownZoneFilter: Column = col("zoneId") === "unknown"

  // Función para validar los datos de los sensores de Temperatura y Humedad
  def validarDatosSensorTemperatureHumidity(value: String): TemperatureHumidityData = {
    val parts = value.split(",")

    // Loguear y lanzar un error si la estructura de entrada no es la esperada
    if (parts.length != 4) {
      logger.error(s"Formato de entrada inválido: '$value'. Se esperaban 4 partes, se encontraron ${parts.length}.")
      throw new IllegalArgumentException(s"Formato de entrada inválido: $value")
    }

    val sensorId = parts(0)
    val maybeTemperature = Try(parts(1).toDouble)
    val maybeHumidity = Try(parts(2).toDouble)
    val maybeTimestamp = Try(Timestamp.valueOf(parts(3)))

    // Validar el sensorId usando una expresión regular (solo alfanuméricos, guiones y guiones bajos)
    val sensorIdRegex = """^[a-zA-Z0-9-_]+$""".r
    if (sensorId.isEmpty || sensorIdRegex.findFirstIn(sensorId).isEmpty) {
      logger.error(s"sensorId inválido: '$sensorId'. Debe ser alfanumérico y puede incluir '-' o '_'.")
      throw new IllegalArgumentException(s"sensorId inválido: $sensorId")
    }

    (maybeTemperature, maybeHumidity, maybeTimestamp) match {
      case (Success(temperature), Success(humidity), Success(timestamp)) =>
        // Validar que el timestamp esté en un rango correcto (no puede ser futuro ni negativo)
        if (timestamp.getTime <= 0 || timestamp.getTime > System.currentTimeMillis()) {
          logger.error(s"Timestamp inválido: '$timestamp'.")
          throw new IllegalArgumentException(s"Timestamp inválido: $timestamp")
        }
        // Retornar el objeto de dominio con los datos validados
        TemperatureHumidityData(sensorId, temperature, humidity, timestamp)

      // Manejar otros casos de entrada inválida (errores de parseo numérico o de fecha)
      case _ =>
        logger.error(s"Valores inválidos en el registro: '$value'.")
        throw new IllegalArgumentException(s"Valores inválidos: $value")
    }
  }


  def validarDatosSensorTemperatureHumiditySoilMoisture(value: String, timestamp: Timestamp): SoilMoistureData = {
    // TODO: Implementar validaciones
    // TODO: Revisar si el valor de retorno es correcto (¿qué pasa si el valor no es correcto?)
    val parts = value.split(",")
    if (parts.length < 2) throw new IllegalArgumentException("Formato insuficiente")
    val sensorId = parts(0)
    if (!sensorId.matches("^[a-zA-Z0-9-_]+$")) {
        throw new IllegalArgumentException(s"sensorId inválido: $sensorId")
    }
    val moisture = scala.util.Try(parts(1).toDouble).getOrElse(
        throw new IllegalArgumentException("La humedad no es numérica")
    )
    
    SoilMoistureData(sensorId, moisture, timestamp)
  }

  def validarDatosSensorCO2(value: String, timestamp: Timestamp): CO2Data = {
    // TODO: Implementar validaciones
    // TODO: Revisar si el valor de retorno es correcto (¿qué pasa si el valor no es correcto?) -> Ver Option, Try, Either
    val parts = value.split(",")
    if (parts.length < 3) throw new IllegalArgumentException("Formato insuficiente")
    val co2Level = parts(1).toDouble
    if (co2Level < 0) throw new IllegalArgumentException("CO2 negativo")
    CO2Data(parts(0), co2Level, timestamp)
  }
}
