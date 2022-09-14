package com.seaboxdata.sdps.extendAnalysis.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public enum FileStatsType {
    TYPE_ERROR(-1L, -1L),
    //临时一个月
    TEMP_ONE_MONTH(0L, 30L),
    //临时三个月
    TEMP_THREE_MONTH(31L, 90L),
    //临时半年
    TEMP_HALF_YEAR(91L, 180L),
    //临时一年
    TEMP_ONE_YEAR(181L, 365L),
    //临时两年
    TEMP_TWO_YEAR(366L, 730L),
    //临时三年
    TEMP_THREE_YEAR(731L, 1095L),
    //其他临时
    TEMP_OTHERS(1096L, 9223372036854775807L),

    SIZE_EMPTY(0L, 0L),

    SIZE_64M(1L, 67108864L),

    SIZE_128M(67108865L, 134217728L),

    SIZE_256M(134217729L, 268435456L),

    SIZE_512M(268435457L, 536870912L),

    SIZE_1G(536870913L, 1073741824L),

    SIZE_OTHERS(1073741825L, 9223372036854775807L);

    private static List<FileStatsType> TEMP_LIST = Arrays.asList(TEMP_ONE_MONTH, TEMP_THREE_MONTH, TEMP_HALF_YEAR, TEMP_ONE_YEAR, TEMP_TWO_YEAR, TEMP_THREE_YEAR, TEMP_OTHERS);
    private static List<FileStatsType> SIZE_LIST = Arrays.asList(SIZE_EMPTY, SIZE_64M, SIZE_128M, SIZE_256M, SIZE_512M, SIZE_1G, SIZE_OTHERS);
    private Long lowerLimit;
    private Long upperLimit;

    private FileStatsType(Long lowerLimit, Long upperLimit) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public static FileStatsType getTempType(int days) {
        return getByDiff(TEMP_LIST, (long) days);
    }

    public static FileStatsType getSizeType(long size) {
        return getByDiff(SIZE_LIST, size);
    }

    public static FileStatsType getByDiff(List<FileStatsType> ranges, Long days) {
        FileStatsType result = TYPE_ERROR;
        Iterator<FileStatsType> iterator = ranges.iterator();

        while (iterator.hasNext()) {
            FileStatsType type = (FileStatsType) iterator.next();
            if (days >= type.getLowerLimit() && days <= type.getUpperLimit()) {
                result = type;
                break;
            }
        }

        return result;
    }

    public Long getUpperLimit() {
        return this.upperLimit;
    }

    public Long getLowerLimit() {
        return this.lowerLimit;
    }
}
