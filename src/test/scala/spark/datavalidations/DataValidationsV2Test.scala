package spark.datavalidations

import org.scalatest.funsuite.AnyFunSuite
import java.sql.Timestamp
import spark.datavalidations.DataValidationsV2._

class DataValidationsV2Test extends AnyFunSuite {

  test("validarDatosSensorCO2 (V2): debería acumular multiples errores (Cats)") {
    // Arrange: ID invalido y valor que no es numero
    val input = "ID_CON_@_MAL,no_soy_numero,2023-10-14 10:00:00"
    
    // Act
    val result = validarDatosSensorCO2(input)
    
    // Assert: Verificamos que es invalido y contiene ambos errores
    assert(result.isInvalid)
    val errors = result.toEither.left.get.toList
    assert(errors.exists(_.contains("ID de sensor inválido")))
    assert(errors.exists(_.contains("no es un número válido")))
  }

  test("validarDatosSensorCO2 (V2): deberia procesar correctamente un dato valido") {
    val input = "sensor1,450.0,2023-10-14 10:00:00"
    val result = validarDatosSensorCO2(input)
    
    assert(result.isValid)
    assert(result.toOption.get.sensorId == "sensor1")
    assert(result.toOption.get.co2Level == 450.0)
  }

  test("validarDatosSensorSoil (V2): deberia detectar falta de campos") {
    val input = "sensor1,45.0" // Falta el timestamp
    val result = validarDatosSensorSoil(input)
    
    assert(result.isInvalid)
    assert(result.toEither.left.get.head.contains("no tiene los 3 campos"))
  }
}
