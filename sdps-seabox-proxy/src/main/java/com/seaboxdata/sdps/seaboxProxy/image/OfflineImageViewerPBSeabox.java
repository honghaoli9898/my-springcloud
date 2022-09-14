package com.seaboxdata.sdps.seaboxProxy.image;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

@Slf4j
public class OfflineImageViewerPBSeabox {
    public static int run(String[] args) {
        Options options = buildOptions();
        if (args.length == 0) {
            return 0;
        }
        CommandLineParser parser = new PosixParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            log.error("错误解析命令行选项:", e);
            return -1;
        }

        //输入文件路径
        String inputFile = cmd.getOptionValue("i");
//        //处理器
//        String processor = cmd.getOptionValue("p", "Delimited");
        //输出目录
        String outputFile = cmd.getOptionValue("o", "-");
        //分隔符
        String delimiter = cmd.getOptionValue("delimiter", "\t");
        //临时目录
        String tempPath = cmd.getOptionValue("t", "");
        //是否扩充
        String inflate = cmd.getOptionValue("inflate", "false");

        PrintStream out = null;
        RandomAccessFile accessFile = null;
        SeaboxImageTextWriter writer = null;

        try {
            out = "-".equals(outputFile) ? System.out : new PrintStream(outputFile, "UTF-8");
            writer = new SeaboxImageTextWriter(out, delimiter, tempPath, Boolean.parseBoolean(inflate));
            accessFile = new RandomAccessFile(inputFile, "r");
            writer.visit(accessFile);

            return 0;
        } catch (IOException e) {
            log.error("提取HDFS原数据异常:", e);
            return -1;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (accessFile != null) {
                    accessFile.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                log.error("提取HDFS原数据异常:", e);
                return -1;
            }
        }
    }

    /**
     * 构建运行选项
     *
     * @return
     */
    private static Options buildOptions() {
        Options options = new Options();
        OptionBuilder.isRequired();
        OptionBuilder.hasArgs();
        OptionBuilder.withLongOpt("inputFile");
        options.addOption(OptionBuilder.create("i"));
        options.addOption("o", "outputFile", true, "");
        options.addOption("p", "processor", true, "");
        options.addOption("h", "help", false, "");
        options.addOption("maxSize", true, "");
        options.addOption("step", true, "");
        options.addOption("format", false, "");
        options.addOption("addr", true, "");
        options.addOption("delimiter", true, "");
        options.addOption("t", "temp", true, "");
        return options;
    }
}
