package spark

import org.scalatest.funsuite.AnyFunSuite
import spark.Implicits._

/**
 * Clase de prueba para los Implicits de Spark.
 * Estas utilidades permiten escribir configuraciones de memoria de forma más legible.
 * Ejemplo: 1.Gb en lugar de Memory(1, "g").
 */
class ImplicitsTest extends AnyFunSuite {

  test("Debería crear un objeto Memory con unidad Gb correctamente") {
    // Arrange: Definimos un valor entero
    val size = 4
    
    // Act: Utilizamos la extensión implícita .Gb
    val memory = size.Gb
    
    // Assert: Verificamos que se ha creado el objeto con los valores correctos
    assert(memory.value == 4)
    assert(memory.unit == "g")
    assert(memory.toString == "4g")
  }

  test("Debería crear un objeto Memory con unidad Mb correctamente") {
    // Arrange: Definimos un valor entero
    val size = 512
    
    // Act: Utilizamos la extensión implícita .Mb
    val memory = size.Mb
    
    // Assert: Verificamos que se ha creado el objeto con los valores correctos
    assert(memory.value == 512)
    assert(memory.unit == "m")
    assert(memory.toString == "512m")
  }

  test("Debería funcionar con diferentes valores de memoria") {
    // Probar valores límite o comunes
    assert(0.Gb.toString == "0g")
    assert(1024.Mb.toString == "1024m")
    assert((-1).Gb.value == -1) // Aunque no tenga sentido físico, la lógica lo permite
  }
}
