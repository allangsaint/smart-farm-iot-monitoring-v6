import config.AppConfig.sensorToZoneMap
import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{col, udf}

package object utils {

  object DataTransformations {
    import domain.IoTDomain._
    
    /**
     * UDF (User Defined Function) para mapear un sensorId a un zoneId.
     * Si el sensor no existe en el mapa de configuración, devuelve "unknown".
     */
    val sensorIdToZoneId: UserDefinedFunction = udf((sensorId: String) => sensorToZoneMap.getOrElse(sensorId, "unknown"))

    /**
     * Añade una columna "zoneId" a un Dataset de temperatura y humedad.
     * Utiliza la UDF definida arriba para realizar el mapeo.
     */
    def addZoneColumn(df: Dataset[TemperatureHumidityData]): DataFrame = {
      df.withColumn("zoneId", sensorIdToZoneId(col("sensorId")))
    }

  }
}
