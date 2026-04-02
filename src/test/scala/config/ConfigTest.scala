package config

import org.scalatest.funsuite.AnyFunSuite

/**
 * Clase de prueba para verificar que la configuración de la aplicación se carga correctamente
 * desde los archivos de configuración (application.conf).
 * Estos tests aseguran que los valores clave para conectar con Kafka y las rutas de archivos son correctos.
 */
class ConfigTest extends AnyFunSuite {
  import config.AppConfig._

  test("Validar configuración de Kafka Bootstrap Servers") {
    // Arrange & Act
    // El valor esperado debe ser el definido en application.conf
    val expected = "localhost:9092"
    
    // Assert: Verificamos que la dirección del servidor Kafka coincide con la configurada
    assert(kafkaBootstrapServers == expected)
  }

  test("Validar nombre del Topic de Temperatura y Humedad") {
    // Arrange & Act
    val expected = "temperature_humidity"
    
    // Assert: El topic debe coincidir exactamente con el definido en application.conf
    assert(temperatureHumidityTopic == expected)
  }

  test("Validar nombre del Topic de CO2") {
    // Arrange & Act
    val expected = "co2"
    
    // Assert
    assert(co2Topic == expected)
  }

  test("Validar nombre del Topic de Humedad del Suelo (Soil Moisture)") {
    // Arrange & Act
    val expected = "soil_moisture"
    
    // Assert
    assert(soilMoistureTopic == expected)
  }

  test("Validar la Ruta Base de almacenamiento") {
    // Arrange & Act
    val expected = "./tmp/"
    
    // Assert: La ruta base es donde se guardarán temporalmente los datos y las tablas Delta
    assert(rutaBase == expected)
  }

  test("Validar el Sufijo de Checkpoint") {
    // Los checkpoints de Spark Streaming se guardan con este sufijo para organización
    assert(sufijoCheckpoint == "_chk")
  }

  test("Validar nombres de Tablas Delta") {
    assert(Tablas.RawTemperatureHumidityZone == "raw_temperature_humidity_zone")
    assert(Tablas.TemperatureHumidityZoneMerge == "temperature_humidity_zone_merge")
  }

  test("Probar generación de Ruta para una Tabla") {
    // Arrange: Preparamos un nombre de tabla de prueba
    val nombreTabla = "test_table"
    
    // Act: Llamamos a la función de utilidad
    val resultado = getRutaParaTabla(nombreTabla)
    
    // Assert: Comprobamos que concatena correctamente la ruta base
    assert(resultado == "./tmp/test_table")
  }

  test("Probar generación de Ruta para Checkpoint de una Tabla") {
    // Arrange
    val nombreTabla = "test_table"
    
    // Act
    val resultado = getRutaParaTablaChk(nombreTabla)
    
    // Assert: Comprobamos que concatena la ruta base y el sufijo de checkpoint
    assert(resultado == "./tmp/test_table_chk")
  }

  test("Probar generación de Ruta con nombre de tabla vacío") {
    // Arrange
    val nombreTabla = ""
    
    // Act
    val resultado = getRutaParaTabla(nombreTabla)
    
    // Assert: Debería devolver solo la ruta base
    assert(resultado == "./tmp/")
  }

  /**
   * Tests para la función de asignación de sensores a zonas.
   * Verifica que el mapeo dinámico desde el archivo JSON funcione correctamente.
   */
  test("assignSensorToZone: debería asignar correctamente un sensor a la Zona 1") {
    // Arrange: ID de sensor que pertenece a la Zona 1 en el JSON
    val sensorId = "sensor1"
    
    // Act: Llamada a la función de asignación
    val result = assignSensorToZone(sensorId)
    
    // Assert: Verificación de que la zona asignada sea la esperada
    assert(result == "zona1")
  }

  test("assignSensorToZone: debería asignar correctamente un sensor a la Zona 2") {
    // Arrange: ID de sensor que pertenece a la Zona 2 en el JSON
    val sensorId = "sensor4"
    
    // Act
    val result = assignSensorToZone(sensorId)
    
    // Assert
    assert(result == "zona2")
  }

  test("assignSensorToZone: debería asignar correctamente un sensor a la Zona 3") {
    // Arrange: ID de sensor que pertenece a la Zona 3 en el JSON
    val sensorId = "sensor9"
    
    // Act
    val result = assignSensorToZone(sensorId)
    
    // Assert
    assert(result == "zona3")
  }

  test("assignSensorToZone: debería devolver 'unknown' para un sensor no existente en el JSON") {
    // Arrange: ID de sensor que no está definido en el mapeo
    val sensorId = "sensor_no_existente"
    
    // Act
    val result = assignSensorToZone(sensorId)
    
    // Assert: El comportamiento esperado es que devuelva la cadena por defecto "unknown"
    assert(result == "unknown")
  }

  test("assignSensorToZone: debería manejar correctamente un ID de sensor vacío") {
    // Arrange: ID de sensor vacío
    val sensorId = ""
    
    // Act
    val result = assignSensorToZone(sensorId)
    
    // Assert: Debe devolver "unknown"
    assert(result == "unknown")
  }

  test("assignSensorToZone: debería manejar correctamente un ID de sensor con espacios") {
    // Arrange: ID de sensor con espacios (no definido en el mapeo)
    val sensorId = " sensor1 "
    
    // Act
    val result = assignSensorToZone(sensorId)
    
    // Assert: El mapeo es exacto, por lo que debería ser "unknown"
    assert(result == "unknown")
  }

  test("assignSensorToZone: debería manejar correctamente un ID de sensor nulo") {
    // Arrange: ID de sensor nulo
    val sensorId: String = null
    
    // Act
    val result = assignSensorToZone(sensorId)
    
    // Assert: Al usar .getOrElse con un mapa de Scala, si la clave es nula suele dar "unknown" 
    // o lanzar excepción si no se controla, pero en este caso el mapa no contiene nulos.
    assert(result == "unknown")
  }

  test("assignSensorToZone: debería ser sensible a mayúsculas si el ID no coincide exactamente") {
    // Arrange: El JSON define "sensor1", probamos con "SENSOR1"
    val sensorId = "SENSOR1"
    
    // Act
    val result = assignSensorToZone(sensorId)
    
    // Assert: Actualmente la comparación es exacta, por lo que debería ser unknown
    assert(result == "unknown")
  }

  test("AppConfig.config: debería cargar una configuración válida") {
    // Verificamos que el objeto config no es nulo y tiene alguna de las claves esperadas
    assert(config != null)
    assert(config.hasPath("kafka.bootstrap.servers"))
  }
}
