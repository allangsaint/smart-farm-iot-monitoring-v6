package utils

import org.scalatest.funsuite.AnyFunSuite

/**
 * Clase de prueba para el trait Logging.
 * Verifica que el logger se inicializa correctamente y que los métodos de logging no lanzan excepciones.
 */
class LoggingTest extends AnyFunSuite {

  // Creamos una clase concreta que extiende el trait para poder probarlo
  class TestLogger extends Logging

  test("Los métodos de log no deberían lanzar excepciones al ser llamados") {
    // Arrange
    val loggerObj = new TestLogger
    
    // Act & Assert (No debería haber excepciones)
    loggerObj.logInfo("Mensaje de prueba de información")
    loggerObj.logWarn("Mensaje de prueba de advertencia")
    loggerObj.logDebug("Mensaje de prueba de depuración")
    
    try {
      loggerObj.logError("Mensaje de prueba de error", new Exception("Error simulado"))
    } catch {
      case e: Exception => fail(s"logError lanzó una excepción inesperada: ${e.getMessage}")
    }
  }

  test("setLogLevel debería permitir cambiar el nivel de log") {
    // Arrange
    val loggerObj = new TestLogger
    import org.apache.log4j.Level
    
    // Act & Assert
    loggerObj.setLogLevel(Level.DEBUG)
    loggerObj.logDebug("Este mensaje debería verse si el nivel es DEBUG")
    
    loggerObj.setLogLevel(Level.ERROR)
    // No podemos verificar fácilmente la salida de consola, pero al menos aseguramos que no falla
  }
}
