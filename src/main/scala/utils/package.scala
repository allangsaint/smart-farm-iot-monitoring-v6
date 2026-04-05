import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.sql.functions._

package object utils {

  object DataTransformations {
    import domain.IoTDomain._
    
    val sensorIdToZoneId = udf((sensorId: String) => {
      val mapping = Map(
        "sensor1" -> "zona1", "sensor2" -> "zona1", "sensor3" -> "zona1",
        "sensor4" -> "zona2", "sensor5" -> "zona2", "sensor6" -> "zona2",
        "sensor7" -> "zona3", "sensor8" -> "zona3", "sensor9" -> "zona3"
      )
      mapping.getOrElse(sensorId, "unknown")
    })

    /**
     * Añade una columna "zoneId" a un Dataset de temperatura y humedad.
     * Utiliza la UDF definida arriba para realizar el mapeo.
     */
    def addZoneColumn(df: Dataset[TemperatureHumidityData]): DataFrame = {
      df.withColumn("zoneId", sensorIdToZoneId(col("sensorId")))
    }

  }
}
