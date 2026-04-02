package spark

import org.apache.spark.sql.types._
import org.scalatest.funsuite.AnyFunSuite

/**
 * Clase de test para validar las utilidades de Spark.
 * Aquí probamos la creación de DataFrames vacíos basados en un esquema definido.
 *
 * Extiende AnyFunSuite para una descripción clara de los casos de prueba
 * y SparkSessionWrapper para tener acceso a una sesión de Spark real durante el test.
 */
class SparkUtilsTest extends AnyFunSuite with SparkSessionWrapper {

  // Objeto de ayuda para acceder a los métodos del Trait SparkUtils
  object TestSparkUtils extends SparkUtils

  test("createEmptyDataFrame: debería crear un DataFrame vacío con el esquema proporcionado") {
    // Escenario (Arrange): Definimos un esquema para nuestro DataFrame
    val schema = StructType(Seq(
      StructField("id", StringType, nullable = false),
      StructField("valor", DoubleType, nullable = true),
      StructField("timestamp", TimestampType, nullable = true)
    ))

    // Acción (Act): Llamamos al método para crear el DataFrame vacío
    val df = TestSparkUtils.createEmptyDataFrame(schema)

    // Verificación (Assert):
    // 1. Comprobamos que el DataFrame está vacío (count == 0)
    assert(df.count() == 0, "El DataFrame debería estar vacío")
    
    // 2. Comprobamos que el esquema coincide exactamente con el proporcionado
    assert(df.schema == schema, "El esquema del DataFrame no coincide con el esperado")
    
    // 3. Comprobamos que los nombres de las columnas son correctos
    assert(df.columns.contains("id"))
    assert(df.columns.contains("valor"))
    assert(df.columns.contains("timestamp"))
  }
}
