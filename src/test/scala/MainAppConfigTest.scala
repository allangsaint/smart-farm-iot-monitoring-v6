import org.scalatest.funsuite.AnyFunSuite

/**
 * Clase de prueba para el objeto AppConfig definido en Main.scala.
 * Se prueba por separado para asegurar que el mapeo estático de zonas funcione según lo previsto.
 */
class MainAppConfigTest extends AnyFunSuite {

  test("AppConfig (Main) debería tener el mapeo de sensores correcto para la Zona 1") {
    // Assert
    assert(AppConfig.sensorToZoneMap("sensor1") == AppConfig.Zone1)
    assert(AppConfig.sensorToZoneMap("sensor2") == AppConfig.Zone1)
    assert(AppConfig.sensorToZoneMap("sensor3") == AppConfig.Zone1)
  }

  test("AppConfig (Main) debería tener el mapeo de sensores correcto para la Zona 2") {
    // Assert
    assert(AppConfig.sensorToZoneMap("sensor4") == AppConfig.Zone2)
    assert(AppConfig.sensorToZoneMap("sensor5") == AppConfig.Zone2)
    assert(AppConfig.sensorToZoneMap("sensor6") == AppConfig.Zone2)
  }

  test("AppConfig (Main) debería tener el mapeo de sensores correcto para la Zona 3") {
    // Assert
    assert(AppConfig.sensorToZoneMap("sensor7") == AppConfig.Zone3)
    assert(AppConfig.sensorToZoneMap("sensor8") == AppConfig.Zone3)
    assert(AppConfig.sensorToZoneMap("sensor9") == AppConfig.Zone3)
  }

  test("AppConfig (Main) debería definir correctamente las constantes de tiempo") {
    assert(AppConfig.OneMinute == "1 minute")
    assert(AppConfig.OneHour == "1 hour")
    assert(AppConfig.OneDay == "1 day")
  }

  test("AppConfig (Main) debería tener configuraciones de Watermark y Window coherentes") {
    assert(AppConfig.WatermarkDuration == AppConfig.OneMinute)
    assert(AppConfig.WindowDuration == AppConfig.OneMinute)
  }
}
