package com.seaboxdata.sdps.seaboxProxy.delta

import java.text.SimpleDateFormat
import java.util.Date
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * @author: Denny
  * @date: 2022/5/13 11:34
  * @desc:
  */
object OriginTransform {
  def main(args: Array[String]): Unit = {
    val originFilePath = args(0)
    val outputPath = args(1)

    val standardSDF = new SimpleDateFormat("yyyy-MM-dd HH:ss:mm")
    val dateSDF = new SimpleDateFormat("yyyy-MM-dd")
    val hourSDF = new SimpleDateFormat("HH")
    val minuteSDF = new SimpleDateFormat("mm")
    val today = dateSDF.format(new Date())


    val spark: SparkSession = SparkSession.builder().appName("OriginDataTransform").getOrCreate()
    val sc: SparkContext = spark.sparkContext
    import spark.implicits._

    val dataDF: DataFrame = sc.textFile(originFilePath).map(_.split("\t")).map(line => {
      val timeStamp: Long = standardSDF.parse(today + " " + line(0)).getTime
      val hour: Int = hourSDF.format(new Date(timeStamp)).toInt
      val minute: Int = minuteSDF.format(new Date(timeStamp)).toInt
      val userID: Int = line(1).toInt
      val topic: String = line(2)
      val resultRank: Int = line(3).toInt
      val clickRank: Int = line(4).toInt
      val url: String = line(5)

      OutputDefine(today, hour, minute, userID, topic, resultRank, clickRank, url)
    }).toDF()

    dataDF.write.format("delta").save(outputPath)

    spark.close()
  }
}

case class OutputDefine(date:String, hour:Int, minute:Int, userId:Int, topic:String, resultRank:Int, clickRank:Int, url:String)