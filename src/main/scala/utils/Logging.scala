package utils

import org.apache.log4j.{Level, Logger}

/**
 * Trait para facilitar el registro de logs (logging) desde Spark usando Log4j.
 * Se puede mezclar (mixin) en cualquier objeto o clase para obtener acceso a un logger configurado.
 * 
 * Es `Serializable` y usa `@transient lazy val` para asegurar que el logger no se serialice
 * y se reconstruya en los nodos ejecutores de Spark.
 */
trait Logging extends Serializable {
  @transient private lazy val logger: Logger = Logger.getLogger(getClass.getName)

  /** Registra un mensaje de información. */
  def logInfo(msg: => String): Unit = {
    if (logger.isInfoEnabled) logger.info(msg)
  }

  /** Registra un mensaje de depuración (debug). */
  def logDebug(msg: => String): Unit = {
    if (logger.isDebugEnabled) logger.debug(msg)
  }

  /** Registra un mensaje de advertencia (warning). */
  def logWarn(msg: => String): Unit = {
    logger.warn(msg)
  }

  /** Registra un mensaje de error con una excepción asociada. */
  def logError(msg: => String, throwable: Throwable): Unit = {
    logger.error(msg, throwable)
  }

  /** Registra un mensaje de error. */
  def logError(msg: => String): Unit = {
    logger.error(msg)
  }

  /** Permite cambiar dinámicamente el nivel de log. */
  def setLogLevel(level: Level): Unit = {
    logger.setLevel(level)
  }
}
