package spark.datavalidations

import org.scalatest.funsuite.AnyFunSuite
import java.sql.Timestamp
import domain.IoTDomain._
import spark.datavalidations.DataValidations._

/**
 * Clase de test para validar las funciones de DataValidations.
 * Esta clase sirve como ejemplo para alumnos aprendiendo Scala y Spark.
 * 
 * En ScalaTest, AnyFunSuite nos permite escribir tests de una manera descriptiva
 * usando bloques "test(...) { ... }".
 */
class DataValidationsTest extends AnyFunSuite {

  // --- Tests para validarDatosSensorTemperatureHumidity ---

  test("validarDatosSensorTemperatureHumidity: debería procesar correctamente una cadena válida") {
    // Escenario (Arrange): Preparamos los datos de entrada
    // Formato esperado: "sensorId,temperature,humidity,timestamp"
    val sensorId = "sensor_01"
    val temp = 25.5
    val hum = 60.0
    val tsStr = "2023-10-14 10:00:00"
    val input = s"$sensorId,$temp,$hum,$tsStr"

    // Acción (Act): Llamamos a la función que queremos probar
    val result = validarDatosSensorTemperatureHumidity(input)

    // Verificación (Assert): Comprobamos que el resultado es el esperado
    assert(result.sensorId == sensorId)
    assert(result.temperature == temp)
    assert(result.humidity == hum)
    assert(result.timestamp == Timestamp.valueOf(tsStr))
  }

  test("validarDatosSensorTemperatureHumidity: debería lanzar IllegalArgumentException si el formato es incorrecto") {
    // Una cadena con solo 3 partes en lugar de 4
    val input = "sensor1,25.5,60.0"
    
    // Verificamos que se lanza la excepción esperada
    intercept[IllegalArgumentException] {
      val result = validarDatosSensorTemperatureHumidity(input)
      result
    }
  }

  test("validarDatosSensorTemperatureHumidity: debería lanzar IllegalArgumentException si el sensorId es inválido") {
    // sensorId con caracteres no permitidos (ej: $)
    val input = "sensor$,25.5,60.0,2023-10-14 10:00:00"
    
    intercept[IllegalArgumentException] {
      validarDatosSensorTemperatureHumidity(input)
    }
  }

  test("validarDatosSensorTemperatureHumidity: debería lanzar IllegalArgumentException si el timestamp es futuro") {
    // Un timestamp muy lejano en el futuro
    val futuro = "2099-01-01 00:00:00"
    val input = s"sensor1,25.5,60.0,$futuro"
    
    intercept[IllegalArgumentException] {
      validarDatosSensorTemperatureHumidity(input)
    }
  }

  test("validarDatosSensorTemperatureHumidity: debería lanzar IllegalArgumentException si los valores numéricos no son válidos") {
    // "abc" no es un Double válido para temperatura
    val input = "sensor1,abc,60.0,2023-10-14 10:00:00"
    
    intercept[IllegalArgumentException] {
      validarDatosSensorTemperatureHumidity(input)
    }
  }

  // --- Tests para validarDatosSensorTemperatureHumiditySoilMoisture ---

  test("validarDatosSensorTemperatureHumiditySoilMoisture: debería procesar correctamente datos válidos") {
    /**
     * Arrange: Preparación de datos válidos.
     * El formato esperado es "sensorId,soilMoisture,timestamp_str" (aunque el ts se pase aparte).
     */
    val sensorId = "soil_01"
    val moisture = 45.5
    val input = s"$sensorId,$moisture,2023-10-14 10:00:00"
    val ts = new Timestamp(System.currentTimeMillis())
    
    // Act
    val result = validarDatosSensorTemperatureHumiditySoilMoisture(input, ts)
    
    // Assert
    assert(result.sensorId == sensorId)
    assert(result.soilMoisture == moisture)
    assert(result.timestamp == ts)
  }

  test("validarDatosSensorTemperatureHumiditySoilMoisture: debería lanzar IllegalArgumentException si la humedad no es numérica") {
    val input = "soil_01,no_es_numero,extra"
    val ts = new Timestamp(System.currentTimeMillis())
    
    intercept[IllegalArgumentException] {
      validarDatosSensorTemperatureHumiditySoilMoisture(input, ts)
    }
  }

  test("validarDatosSensorTemperatureHumiditySoilMoisture: debería lanzar IllegalArgumentException si el sensorId es inválido") {
    val input = "sensor_inv@lido,45.5,extra"
    val ts = new Timestamp(System.currentTimeMillis())
    
    intercept[IllegalArgumentException] {
      validarDatosSensorTemperatureHumiditySoilMoisture(input, ts)
    }
  }

  // --- Tests para validarDatosSensorCO2 ---

  test("validarDatosSensorCO2: debería procesar correctamente datos válidos") {
    val sensorId = "co2_01"
    val co2Level = 400.0
    val input = s"$sensorId,$co2Level,extra"
    val ts = new Timestamp(System.currentTimeMillis())
    
    val result = validarDatosSensorCO2(input, ts)
    
    assert(result.sensorId == sensorId)
    assert(result.co2Level == co2Level)
    assert(result.timestamp == ts)
  }

  test("validarDatosSensorCO2: debería lanzar IllegalArgumentException si el nivel de CO2 es negativo") {
    val input = "co2_01,-10.5,extra"
    val ts = new Timestamp(System.currentTimeMillis())
    
    intercept[IllegalArgumentException] {
      validarDatosSensorCO2(input, ts)
    }
  }

  test("validarDatosSensorCO2: debería lanzar IllegalArgumentException si el formato es insuficiente") {
    val input = "solo_sensor_id"
    val ts = new Timestamp(System.currentTimeMillis())
    
    intercept[IllegalArgumentException] {
      validarDatosSensorCO2(input, ts)
    }
  }
}
