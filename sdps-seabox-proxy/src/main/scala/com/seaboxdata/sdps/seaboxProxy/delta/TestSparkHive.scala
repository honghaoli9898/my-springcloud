package com.seaboxdata.sdps.seaboxProxy.delta

import org.apache.spark.sql.{DataFrame, SparkSession}


/**
  * @author: Denny
  * @date: 2022/5/24 20:02
  * @desc: 测试Spark2.4.4是否能读取Hive数据
  */
object TestSparkHive {
  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession.builder()
      //.master("local")
      .enableHiveSupport()
      .config("hive.metastore.uris","thrift://10.1.3.113:9083")
      .config("spark.sql.warehouse.dir","hdfs://10.1.3.113:8020/warehouse/tablaspace/managed/hive/")
      .appName("SparkHive")
      .getOrCreate()

    val sqlDF: DataFrame = spark.sql("show databases")
    sqlDF.show(10,false)

    //sqlDF.collect().foreach(println)
  }

}
