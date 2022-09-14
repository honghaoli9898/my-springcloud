package com.seaboxdata.sdps.seaboxProxy.minio


import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.core.fs.FileSystem

/**
  * @author: Denny
  * @date: 2022/6/16 11:29
  * @desc:
  */
object FlinkTest {
  /*def main(args: Array[String]): Unit = {
    //val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    import org.apache.flink.api.scala._
    env.setParallelism(1)

    //val data: DataStreamSource[String] = env.fromElements("hello hadoop hive spark hive spark")
    val data: DataSet[String] = env.fromElements("hello hadoop hive spark hive spark")
    val result: AggregateDataSet[(String, Int)] = data.flatMap(_.split(" ")).map((_,1)).groupBy(0).sum(1)

    println("print the result to the consule:")
    result.print()

    print("save the result to the minio oss:")
    result.writeAsText("s3://flink-output/output3.txt",FileSystem.WriteMode.OVERWRITE)


    //env.execute("word count")
    env.execute("s3 output5 task")
  }*/

}
