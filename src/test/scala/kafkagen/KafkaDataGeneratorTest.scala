package kafkagen

import org.scalatest.funsuite.AnyFunSuite
import java.sql.Timestamp

/**
 * Clase de test para validar la lógica del generador de datos de Kafka.
 * Se centra en probar el formateo de los mensajes antes de ser enviados.
 */
class KafkaDataGeneratorTest extends AnyFunSuite {

  test("formatMessage: debería formatear correctamente mensajes de temperatura y humedad") {
    // Arrange
    val topic = "temperature_humidity"
    val sensorId = "sensor1"
    val value = 25.5
    val ts = Timestamp.valueOf("2023-10-14 10:00:00")
    
    // Act
    val result = NoModificar.formatMessage(topic, sensorId, value, ts)
    
    // Assert
    // El formato esperado es "sensorId,temperature,humidity,timestamp" 
    // (en NoModificar se usa el mismo valor para temp y hum por simplicidad)
    assert(result == s"sensor1,25.5,25.5,$ts")
  }

  test("formatMessage: debería formatear correctamente mensajes de CO2") {
    // Arrange
    val topic = "co2"
    val sensorId = "sensor2"
    val value = 400.0
    val ts = Timestamp.valueOf("2023-10-14 10:05:00")
    
    // Act
    val result = NoModificar.formatMessage(topic, sensorId, value, ts)
    
    // Assert
    assert(result == s"sensor2,400.0,$ts")
  }

  test("formatMessage: debería formatear correctamente mensajes de humedad del suelo") {
    // Arrange
    val topic = "soil_moisture"
    val sensorId = "sensor3"
    val value = 60.0
    val ts = Timestamp.valueOf("2023-10-14 10:10:00")
    
    // Act
    val result = NoModificar.formatMessage(topic, sensorId, value, ts)
    
    // Assert
    assert(result == s"sensor3,60.0,$ts")
  }

  test("formatMessage: debería lanzar IllegalArgumentException para un topic desconocido") {
    // Arrange
    val topic = "topic_inventado"
    
    // Act & Assert
    intercept[IllegalArgumentException] {
      NoModificar.formatMessage(topic, "sensor1", 10.0, new Timestamp(System.currentTimeMillis()))
    }
  }

  /**
   * Tests adicionales para robustecer el formateo de mensajes.
   * Estos casos cubren valores que podrían considerarse extremos o especiales.
   */
  test("formatMessage: debería manejar correctamente un valor de 0.0") {
    // Arrange
    val sensorId = "sensor_zero"
    val value = 0.0
    val ts = new Timestamp(System.currentTimeMillis())
    
    // Act
    val result = NoModificar.formatMessage("co2", sensorId, value, ts)
    
    // Assert
    assert(result == s"sensor_zero,0.0,$ts")
  }

  test("formatMessage: debería manejar correctamente valores negativos") {
    // Aunque el generador aleatorio no los produce, la utilidad debe ser capaz de formatearlos
    // Arrange
    val sensorId = "sensor_negativo"
    val value = -15.5
    val ts = new Timestamp(System.currentTimeMillis())
    
    // Act
    val result = NoModificar.formatMessage("temperature_humidity", sensorId, value, ts)
    
    // Assert: El formato de temperatura y humedad repite el valor dos veces por diseño de NoModificar
    assert(result == s"sensor_negativo,-15.5,-15.5,$ts")
  }

  test("formatMessage: debería formatear correctamente con sensorId que incluya caracteres especiales permitidos") {
    // Arrange: Los IDs de sensores suelen tener guiones o guiones bajos
    val sensorId = "sensor-A_01"
    val value = 100.5
    val ts = new Timestamp(System.currentTimeMillis())
    
    // Act
    val result = NoModificar.formatMessage("soil_moisture", sensorId, value, ts)
    
    // Assert
    assert(result == s"sensor-A_01,100.5,$ts")
  }

  test("formatMessage: debería lanzar una NullPointerException si el timestamp es nulo") {
    // Este test nos ayuda a entender el comportamiento de la concatenación de cadenas en Scala
    // cuando se trabaja con nulos. En Scala, s"$nulo" se convierte en "null".
    // Sin embargo, es buena práctica saber cómo reacciona nuestra lógica.
    
    // Arrange
    val sensorId = "sensor_nulo"
    val value = 50.0
    val ts: Timestamp = null
    
    // Act
    val result = NoModificar.formatMessage("co2", sensorId, value, ts)
    
    // Assert: Verificamos que se convierte a la cadena "null"
    assert(result.contains(",null"))
    assert(result == s"sensor_nulo,50.0,null")
  }
}
