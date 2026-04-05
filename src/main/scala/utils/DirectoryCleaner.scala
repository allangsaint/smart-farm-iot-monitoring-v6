package utils

object DirectoryCleaner extends Logging {

  def cleanTempDirectory(path: String): Unit = {
    import java.nio.file.{Files, Paths}
    import java.util.Comparator

    // Obtener la ruta como Path
    val directoryPath = Paths.get(path)

    // Verificar si el directorio existe antes de proceder
    if (Files.exists(directoryPath)) {
      // Recorremos todos los archivos del directorio (orden inverso para eliminar archivos antes que directorios)
      // Files.walk devuelve un "java stream" con los directorios y subdirectorios
      logInfo(s"Eliminando archivos del directorio $path")
      Files.walk(directoryPath)
        .sorted(Comparator.reverseOrder())
        // Utilizamos `.map(_.toFile)` en este caso porque `Files.walk` genera un `Stream[Path]`
        // (es decir, un flujo de objetos `Path` que representan rutas en el sistema de archivos).
        // Sin embargo, para poder eliminar esos archivos o directorios, necesitamos convertir cada `Path` a un objeto `File`.
        // .map(_.toFile) equivale a .map(file => file.toFile)
        .map(_.toFile)
        .forEach(file => {
          // Intentar eliminar cada archivo y mostrar una advertencia si falla
          if (!file.delete()) {
            logWarn(s"Advertencia: No se pudo eliminar ${file.getAbsolutePath}")
          }
        })
    } else {
      // Si el directorio no existe, indicarlo con un mensaje
      logInfo(s"El directorio $path no existe, se omite la limpieza.")
    }
  }
}
