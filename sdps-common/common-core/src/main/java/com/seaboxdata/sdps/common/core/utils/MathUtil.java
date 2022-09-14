package com.seaboxdata.sdps.common.core.utils;

import org.springframework.util.CollectionUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

public class MathUtil {
//    private static final int IntegerDigits = 3;
    private static final int FractionDigits = 2;

    public static double division(Double numerator, Long denominator) {
        if (numerator != null && denominator != null) {
            DecimalFormat df = new DecimalFormat("0.0000");
            return denominator != 0L ? Double.valueOf(df.format(numerator / (double)denominator)) : 0.0D;
        } else {
            return 0.0D;
        }
    }

    public static double division(Long numerator, Long denominator) {
        if (numerator != null && denominator != null) {
            DecimalFormat df = new DecimalFormat("0.0000");
            return denominator != 0L ? Double.valueOf(df.format((double)numerator / (double)denominator)) : 0.0D;
        } else {
            return 0.0D;
        }
    }

    public static String divisionToPercent(Long numerator, Long denominator){
        NumberFormat nf = java.text.NumberFormat.getPercentInstance();
        //小数点前保留几位
//        nf.setMaximumIntegerDigits(IntegerDigits);
        // 小数点后保留几位
        nf.setMinimumFractionDigits(FractionDigits);
        String str = "0%";
        if (denominator != null && denominator != 0L){
            str = nf.format((double) numerator / denominator);
        }
        return str;
    }

    public static double division(int numerator, int denominator) {
        DecimalFormat df = new DecimalFormat("0.0000");
        return denominator != 0 ? Double.valueOf(df.format((double)numerator / (double)denominator)) : 0.0D;
    }

    public static Double average(List<Long> numbers) {
        if (CollectionUtils.isEmpty(numbers)) {
            return 0.0D;
        } else {
            OptionalDouble average = numbers.stream().mapToDouble(Long::doubleValue).average();
            return average.isPresent() ? average.getAsDouble() : 0.0D;
        }
    }

    public static Double median(List<Long> numbers) {
        if (CollectionUtils.isEmpty(numbers)) {
            return 0.0D;
        } else {
            Long[] numArray = (Long[])numbers.toArray(new Long[numbers.size()]);
            Arrays.sort(numArray);
            int middle = numArray.length / 2;
            Long medianValue;
            if (numArray.length % 2 == 1) {
                medianValue = numArray[middle];
            } else {
                medianValue = (numArray[middle - 1] + numArray[middle]) / 2L;
            }

            return medianValue.doubleValue();
        }
    }
}
