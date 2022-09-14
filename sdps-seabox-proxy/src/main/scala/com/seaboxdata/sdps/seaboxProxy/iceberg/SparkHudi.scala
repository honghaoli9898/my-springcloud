package com.seaboxdata.sdps.seaboxProxy.iceberg

import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author: Denny
  * @date: 2022/6/10 19:31
  * @desc:
  */
object SparkHudi {

  def main(args: Array[String]): Unit = {
    val conf = new  SparkConf()
    val sc = new SparkContext(conf)
    val spark: SparkSession = SparkSession.builder().master("local")
      .appName("SparkHudi")
      .getOrCreate()
    sc.setLogLevel("WARN")

    val dataDF: DataFrame = spark.read.format("hudi").load("hdfs://master:8020/warehouse/tablespace/hive/managed/test")

    dataDF.show()

    dataDF.write.format("hudi").mode(SaveMode.Overwrite).option("path","").save()

    spark.close()
  }

}
