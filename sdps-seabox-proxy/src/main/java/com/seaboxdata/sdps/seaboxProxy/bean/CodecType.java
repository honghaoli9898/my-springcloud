package com.seaboxdata.sdps.seaboxProxy.bean;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.orc.CompressionKind;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.avro.file.CodecFactory;

import java.util.Arrays;
import java.util.List;

public enum CodecType {
    UNCOMPRESSED(0, "UNCOMPRESSED", "", "", new byte[0]),
    DEFLATE(1, "DEFLATE", "org.apache.hadoop.io.compress.DefaultCodec", ".deflate", new byte[0]),
    GZIP(2, "GZIP", "org.apache.hadoop.io.compress.GzipCodec", ".gz", new byte[]{31, -117, 8}),
    BZIP2(3, "BZIP2", "org.apache.hadoop.io.compress.BZip2Codec", ".bz2", new byte[]{66, 90, 104}),
    LZO(4, "LZO", "com.hadoop.compression.lzo.LzoCodec", ".lzo_deflate", new byte[0]),
    LZ4(5, "LZ4", "org.apache.hadoop.io.compress.Lz4Codec", ".lz4", new byte[0]),
    SNAPPY(6, "SNAPPY", "org.apache.hadoop.io.compress.SnappyCodec", ".snappy", new byte[0]),
    LZOP(7, "LZOP", "com.hadoop.compression.lzo.LzopCodec", ".lzo", new byte[]{-119, 76, 90, 79, 0, 13, 10, 26, 10}),
    ZLIB(8, "ZLIB", "org.apache.orc.impl.ZlibCodec", ".bz2", new byte[]{31, -117, 8});

    private Integer index;
    private String name;
    private String codec;
    private String extension;
    private byte[] headerMagic;
    private static List<CodecType> SPARK_TEXT_CODEC = Arrays.asList(UNCOMPRESSED, BZIP2, GZIP, LZ4, SNAPPY, DEFLATE);
    private static List<CodecType> SPARK_ORC_CODEC = Arrays.asList(UNCOMPRESSED, SNAPPY, BZIP2, LZO, ZLIB);
    private static List<CodecType> SPARK_AVRO_CODEC = Arrays.asList(UNCOMPRESSED, SNAPPY, DEFLATE);
    private static List<CodecType> SPARK_PARQUET_CODEC = Arrays.asList(UNCOMPRESSED, SNAPPY, GZIP, LZO);

    CodecType(Integer index, String name, String codec, String extension, byte[] headerMagic) {
        this.index = index;
        this.name = name;
        this.codec = codec;
        this.extension = extension;
        this.headerMagic = headerMagic;
    }

    public Integer getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    public String getCodec() {
        return this.codec;
    }

    public String getExtension() {
        return this.extension;
    }

    public byte[] getHeaderMagic() {
        return this.headerMagic;
    }

    public static CodecType getTypeByName(String name) {
        CodecType retValue = null;
        CodecType[] values = values();
        CodecType[] var3 = values;
        int var4 = values.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            CodecType value = var3[var5];
            if (value.getName().equalsIgnoreCase(name)) {
                retValue = value;
                break;
            }
        }

        return retValue;
    }

    public static CodecType getTypeByName(String name, CodecType defaultValue) {
        CodecType type = getTypeByName(name);
        return type == null ? defaultValue : type;
    }

    public static CodecType getTypeByCodec(String codec) {
        CodecType retValue = null;
        CodecType[] values = values();
        CodecType[] var3 = values;
        int var4 = values.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            CodecType value = var3[var5];
            if (value.getCodec().equalsIgnoreCase(codec)) {
                retValue = value;
                break;
            }
        }

        return retValue;
    }

    public static CodecType getTypeByCodec(String codec, CodecType defaultValue) {
        CodecType type = getTypeByCodec(codec);
        return type == null ? defaultValue : type;
    }

    public static CodecType getTypeByExtension(String extension) {
        CodecType retValue = UNCOMPRESSED;
        CodecType[] values = values();

        for(int i = 0; i < values.length; i++) {
            CodecType codecType = values[i];
            if (codecType.getExtension().equalsIgnoreCase(extension) || codecType.getExtension().equalsIgnoreCase("." + extension)) {
                retValue = codecType;
                break;
            }
        }

        return retValue;
    }

    public String getAvroCodecName() {
        return this == UNCOMPRESSED ? "null" : this.getName().toLowerCase();
    }

    public CompressionCodecName getCodecName() throws Exception {
        CompressionCodecName codecName;
        switch(this) {
            case SNAPPY:
                codecName = CompressionCodecName.SNAPPY;
                break;
            case GZIP:
                codecName = CompressionCodecName.GZIP;
                break;
            case LZO:
                codecName = CompressionCodecName.LZO;
                break;
            case UNCOMPRESSED:
                codecName = CompressionCodecName.UNCOMPRESSED;
                break;
            default:
                throw new Exception("not support codec");
        }

        return codecName;
    }

    public CodecFactory getCodecFactory() throws Exception {
        CodecFactory factory;
        switch(this) {
            case SNAPPY:
                factory = CodecFactory.snappyCodec();
                break;
            case GZIP:
            case LZO:
            default:
                throw new Exception("not support codec");
            case UNCOMPRESSED:
                factory = CodecFactory.nullCodec();
                break;
            case DEFLATE:
                factory = CodecFactory.deflateCodec(1);
                break;
            case BZIP2:
                factory = CodecFactory.bzip2Codec();
        }

        return factory;
    }

    public CompressionKind getCodecKind() throws Exception {
        CompressionKind kind;
        switch(this) {
            case SNAPPY:
                kind = CompressionKind.SNAPPY;
                break;
            case GZIP:
            case DEFLATE:
            default:
                throw new Exception("not support codec");
            case LZO:
                kind = CompressionKind.LZO;
                break;
            case UNCOMPRESSED:
                kind = CompressionKind.NONE;
                break;
            case BZIP2:
            case ZLIB:
                kind = CompressionKind.ZLIB;
                break;
            case LZ4:
                kind = CompressionKind.LZ4;
        }

        return kind;
    }

    public CompressionCodec getCompressionCodec() throws Exception {
        String codeClassName = this.getCodec();
        return StringUtils.isBlank(codeClassName) ? null : (CompressionCodec)Class.forName(codeClassName).newInstance();
    }

    public String getSparkCodec(FormatType format) {
        String sparkCodec = null;
        switch(format) {
            case TEXT:
                sparkCodec = this.name().toLowerCase();
                break;
            case ORC:
                sparkCodec = this == BZIP2 ? "zlib" : this.name().toLowerCase();
                break;
            case AVRO:
                sparkCodec = this.getName().toLowerCase();
                break;
            case PARQUET:
                sparkCodec = this.name().toLowerCase();
        }

        return sparkCodec;
    }
}
