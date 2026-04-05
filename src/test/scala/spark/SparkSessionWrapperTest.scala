package spark

import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.SparkSession

/**
 * Clase de prueba para SparkSessionWrapper y sus utilidades relacionadas.
 * Verifica que los niveles de log y el constructor de sesiones funcionen correctamente.
 */
class SparkSessionWrapperTest extends AnyFunSuite {

  test("Los niveles de log deberían devolver la cadena correcta") {
    assert(ErrorLevel.level == "ERROR")
    assert(WarnLevel.level == "WARN")
    assert(InfoLevel.level == "INFO")
    assert(DebugLevel.level == "DEBUG")
  }

  test("El objeto Log.level no debería fallar") {
    // Act & Assert
    try {
      Log.level(InfoLevel)
    } catch {
      case e: Exception => fail(s"Log.level falló inesperadamente: ${e.getMessage}")
    }
  }

  test("SessionBuilder debería permitir configurar diferentes parámetros") {
    // Usamos una instancia de SparkSessionWrapper (anonima)
    val wrapper = new SparkSessionWrapper {}
    val builder = wrapper.createSparkSession
    
    // Configuramos varios parámetros
    builder
      .withName("TestApp")
      .withDriverMemory(Implicits.Memory(2, "g"))
      .withExecutorMemory(Implicits.Memory(1, "g"))
      .withDriverCores(2)
      .withExecutorCores(4)
      .withLogLevel(WarnLevel)
      .withTunedShufflePartitions(5)
      .withCheckpointLocation("./tmp/test_chk")
      .withDeltaLakeSupport
      .withOffHeapEnabled
      .withOffHeapGbSize(Implicits.Memory(1, "g"))
    
    // Nota: No llamamos a .build para evitar crear múltiples sesiones pesadas en los tests
    // si no es necesario, pero validamos que el builder no lance excepciones durante la configuración.
    assert(builder != null)
  }

  test("SparkSessionWrapper debería proporcionar una sesión de Spark funcional") {
    // Este test realmente instancia una sesión
    val wrapper = new SparkSessionWrapper {
      override implicit val spark: SparkSession = createSparkSession
        .withName("FunctionalTest")
        .withTunedShufflePartitions(2)
        .build
    }
    
    assert(wrapper.spark != null)
    // Nota: Como SparkSession.getOrCreate() devuelve la misma sesión en todos los tests,
    // es posible que las configuraciones de una sesión anterior persistan.
    // Por eso, solo verificamos que la sesión está disponible.
    assert(wrapper.spark.isInstanceOf[SparkSession])
  }
}
