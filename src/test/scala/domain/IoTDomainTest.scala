package domain

import org.scalatest.funsuite.AnyFunSuite
import domain.IoTDomain._
import java.sql.Timestamp

/**
 * Clase de prueba para los modelos de datos de IoT.
 * Verifica que las Case Classes se instancien correctamente y mantengan su inmutabilidad.
 */
class IoTDomainTest extends AnyFunSuite {

  test("SoilMoistureData debería instanciarse correctamente") {
    // Arrange
    val now = new Timestamp(System.currentTimeMillis())
    
    // Act
    val data = SoilMoistureData("sensor1", 45.5, now)
    
    // Assert
    assert(data.sensorId == "sensor1")
    assert(data.soilMoisture == 45.5)
    assert(data.timestamp == now)
  }

  test("TemperatureHumidityData debería tener zoneId opcional por defecto") {
    // Arrange
    val now = new Timestamp(System.currentTimeMillis())
    
    // Act
    val data = TemperatureHumidityData("sensor2", 24.0, 55.0, now)
    
    // Assert
    assert(data.zoneId.isEmpty) // Por defecto es None
    assert(data.temperature == 24.0)
    assert(data.humidity == 55.0)
  }

  test("TemperatureHumidityData debería permitir establecer zoneId") {
    // Arrange
    val now = new Timestamp(System.currentTimeMillis())
    
    // Act
    val data = TemperatureHumidityData("sensor2", 24.0, 55.0, now, Some("zonaA"))
    
    // Assert
    assert(data.zoneId.contains("zonaA"))
  }

  test("CO2Data debería instanciarse con todos los campos") {
    // Arrange
    val now = new Timestamp(System.currentTimeMillis())
    
    // Act
    val data = CO2Data("sensor3", 850.0, now, Some("zonaB"))
    
    // Assert
    assert(data.sensorId == "sensor3")
    assert(data.co2Level == 850.0)
    assert(data.zoneId.contains("zonaB"))
  }

  test("Comparación de igualdad entre objetos de dominio (Inmutabilidad)") {
    val now = new Timestamp(1000L)
    val data1 = SoilMoistureData("s1", 10.0, now)
    val data2 = SoilMoistureData("s1", 10.0, now)
    val data3 = SoilMoistureData("s2", 10.0, now)
    
    // Assert: Las case classes en Scala implementan equals basándose en valores
    assert(data1 == data2)
    assert(data1 != data3)
  }
}
