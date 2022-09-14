package com.seaboxdata.sdps.seaboxProxy.iceberg

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * @author: Denny
  * @date: 2022/6/2 10:21
  * @desc:
  */
object Iceberg2Flink {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder().appName("Iceberg on Flink").master("local").getOrCreate()
    val sc: SparkContext = spark.sparkContext
    import spark.implicits._
    val dataDF: DataFrame = spark.read.format("iceberg").load("")
  }

}
