package utils

import org.scalatest.funsuite.AnyFunSuite
import utils.DataTransformations._
import domain.IoTDomain._
import spark.SparkSessionWrapper

/**
 * Clase de prueba para las transformaciones de datos.
 * Utiliza SparkSessionWrapper para tener una sesión de Spark disponible durante los tests.
 */
class DataTransformationsTest extends AnyFunSuite with SparkSessionWrapper {

  import spark.implicits._

  test("addZoneColumn debería asignar correctamente zoneId basado en sensorId") {
    /**
     * Arrange: Preparación de los datos de entrada.
     * Creamos una secuencia de objetos de dominio TemperatureHumidityData con diferentes sensores.
     */
    val inputData = Seq(
      TemperatureHumidityData("sensor1", 25.5, 60.5, java.sql.Timestamp.valueOf("2023-10-14 10:15:00")),
      TemperatureHumidityData("sensor4", 22.3, 58.9, java.sql.Timestamp.valueOf("2023-10-14 10:20:00")),
      TemperatureHumidityData("sensor7", 20.8, 50.2, java.sql.Timestamp.valueOf("2023-10-14 10:25:00")),
      TemperatureHumidityData("unknown_sensor", 19.5, 45.0, java.sql.Timestamp.valueOf("2023-10-14 10:30:00"))
    )

    // Convertimos la secuencia a un DataFrame de Spark
    val inputDf = inputData.toDF()

    /**
     * Act: Ejecución de la transformación.
     * Llamamos a la función addZoneColumn que añade el campo zoneId.
     */
    val resultDf = addZoneColumn(inputDf.as[TemperatureHumidityData])

    /**
     * Assert: Verificación de los resultados esperados.
     * Definimos cómo deberían ser los datos después de la transformación.
     */
    val expectedData = Seq(
      TemperatureHumidityData("sensor1", 25.5, 60.5, java.sql.Timestamp.valueOf("2023-10-14 10:15:00"), Some("zona1")),
      TemperatureHumidityData("sensor4", 22.3, 58.9, java.sql.Timestamp.valueOf("2023-10-14 10:20:00"), Some("zona2")),
      TemperatureHumidityData("sensor7", 20.8, 50.2, java.sql.Timestamp.valueOf("2023-10-14 10:25:00"), Some("zona3")),
      TemperatureHumidityData("unknown_sensor", 19.5, 45.0, java.sql.Timestamp.valueOf("2023-10-14 10:30:00"), Some("unknown"))
    )

    val expectedDf = expectedData.toDF()

    // Validamos que el esquema sea el mismo (incluyendo la nueva columna zoneId)
    assert(resultDf.schema == expectedDf.schema)
    
    // Recolectamos los datos para comparar el contenido
    val resultData = resultDf.as[TemperatureHumidityData].collect()
    val expectedResultData = expectedDf.as[TemperatureHumidityData].collect()
    
    // Verificamos que los elementos resultantes coincidan con los esperados
    assert(resultData.sameElements(expectedResultData))
  }

  test("addZoneColumn debería manejar correctamente IDs de sensores nulos o inexistentes") {
    // Arrange
    val inputData = Seq(
      TemperatureHumidityData(null, 0.0, 0.0, new java.sql.Timestamp(0L)),
      TemperatureHumidityData("", 0.0, 0.0, new java.sql.Timestamp(0L)),
      TemperatureHumidityData("no_existe", 0.0, 0.0, new java.sql.Timestamp(0L))
    )
    val inputDf = inputData.toDF()

    // Act
    val resultDf = addZoneColumn(inputDf.as[TemperatureHumidityData])
    val resultZones = resultDf.select("zoneId").as[String].collect()

    // Assert: Todos deberían ser "unknown" según la lógica de sensorToZoneMap.getOrElse
    assert(resultZones.forall(_ == "unknown"))
  }
}
