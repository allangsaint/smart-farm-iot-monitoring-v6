package spark.datavalidations

import org.scalatest.funsuite.AnyFunSuite

/**
 * Clase de prueba para los valores definidos en el package object de datavalidations.
 */
class PackageTest extends AnyFunSuite {

  test("sensorIdRegex debería validar correctamente los IDs de sensores") {
    // Casos válidos
    assert(sensorIdRegex.matches("sensor1"))
    assert(sensorIdRegex.matches("sensor_A"))
    assert(sensorIdRegex.matches("sensor-123"))
    assert(sensorIdRegex.matches("12345"))
    
    // Casos inválidos
    assert(!sensorIdRegex.matches("sensor 1")) // Espacios
    assert(!sensorIdRegex.matches("sensor#1")) // Caracteres especiales
    assert(!sensorIdRegex.matches("sensor.1")) // Puntos
    assert(!sensorIdRegex.matches(""))         // Vacío (según el regex ^[...]+$)
  }

  test("ExpectedFieldCount debería ser el valor esperado") {
    assert(ExpectedFieldCount == 4)
  }
}
