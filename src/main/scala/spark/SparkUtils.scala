package spark

import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

trait SparkUtils {

  def createEmptyDataFrame(schema: StructType)(implicit spark: SparkSession): DataFrame = spark.createDataFrame(spark.sparkContext.emptyRDD[Row], schema)


}
