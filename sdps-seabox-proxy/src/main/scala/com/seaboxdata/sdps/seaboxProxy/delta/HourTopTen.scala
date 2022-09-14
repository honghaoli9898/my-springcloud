package com.seaboxdata.sdps.seaboxProxy.delta

import java.text.SimpleDateFormat
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * @author: Denny
  * @date: 2022/5/20 18:33
  * @desc:
  */
object HourTopTen {
  def main(args: Array[String]): Unit = {
    val inputPath = args(0)
    val outputPath = args(1)
    val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH")
    val spark: SparkSession = SparkSession.builder().appName("HourTopTen").getOrCreate()
    //import spark.implicits._
    val inputDF: DataFrame = spark.read.format("delta").load(inputPath).toDF()
    inputDF.createOrReplaceTempView("t_basic")
    val queryDF: DataFrame = spark.sql("select ts,topic from t_basic")

    spark.close()
  }

  case class HourTopDefine(date:String, hour:Int,topic:String,rank:Int,number:Int)
}
