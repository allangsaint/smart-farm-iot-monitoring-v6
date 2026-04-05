package utils

import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.{Files, Paths}
import java.io.File
import utils.DirectoryCleaner

/**
 * Clase de prueba para la utilidad DirectoryCleaner.
 * Esta prueba es importante para asegurar que la limpieza de directorios temporales funciona
 * sin borrar accidentalmente archivos importantes fuera del ámbito del proyecto.
 */
class DirectoryCleanerTest extends AnyFunSuite {

  test("cleanTempDirectory debería eliminar un directorio y su contenido") {
    // Arrange: Crear un directorio temporal de prueba con archivos dentro
    val tempDir = Files.createTempDirectory("cleaner_test_")
    val subDir = Files.createDirectory(tempDir.resolve("subdir"))
    val tempFile = Files.createFile(tempDir.resolve("test_file.txt"))
    val subFile = Files.createFile(subDir.resolve("sub_test_file.txt"))

    // Verificamos que se han creado correctamente
    assert(Files.exists(tempDir))
    assert(Files.exists(tempFile))
    assert(Files.exists(subFile))

    // Act: Ejecutamos la limpieza
    DirectoryCleaner.cleanTempDirectory(tempDir.toString)

    // Assert: Verificamos que ya no existe el directorio ni su contenido
    assert(!Files.exists(tempDir), s"El directorio $tempDir aún existe")
    assert(!Files.exists(tempFile), "El archivo temporal aún existe")
    assert(!Files.exists(subFile), "El archivo en el subdirectorio aún existe")
  }

  test("cleanTempDirectory no debería fallar si el directorio no existe") {
    // Arrange: Una ruta que sabemos que no existe
    val nonExistentPath = "ruta/que/no/existe/absolutamente/nunca"

    // Act & Assert: No debería lanzar ninguna excepción
    try {
      DirectoryCleaner.cleanTempDirectory(nonExistentPath)
    } catch {
      case e: Exception => fail(s"Se lanzó una excepción inesperada: ${e.getMessage}")
    }
  }
}
