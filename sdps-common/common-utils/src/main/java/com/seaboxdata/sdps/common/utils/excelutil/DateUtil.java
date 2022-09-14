package com.seaboxdata.sdps.common.utils.excelutil;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * 日期时间处理的工具类
 */
@Slf4j
public class DateUtil {

    /**
     * 日期格式，年份，例如：2004，2008
     */
    public static final String DATE_FORMAT_YYYY = "yyyy";

    /**
     * 日期格式，年份和月份，例如：200707，200808
     */
    public static final String DATE_FORMAT_YYYYMM = "yyyyMM";

    /**
     * 日期格式，年份和月份，例如：2007-07，2008-08
     */
    public static final String DATE_FORMAT_YYYY_MM = "yyyy-MM";

    /**
     * 日期格式，年月日，例如：050630，080808
     */
    public static final String DATE_FORMAT_YYMMDD = "yyMMdd";

    /**
     * 日期格式，年月日，用横杠分开，例如：06-12-25，08-08-08
     */
    public static final String DATE_FORMAT_YY_MM_DD = "yy-MM-dd";

    /**
     * 日期格式，年月日，例如：20050630，20080808
     */
    public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";

    /**
     * 日期格式，年月日，用横杠分开，例如：2006-12-25，2008-08-08
     */
    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * 日期格式，年月日，例如：2016.10.05
     */
    public static final String DATE_FORMAT_POINTYYYYMMDD = "yyyy.MM.dd";

    /**
     * 日期格式，年月日，例如：2016年10月05日
     */
    public static final String DATE_TIME_FORMAT_YYYY年MM月DD日 = "yyyy年MM月dd日";

    /**
     * 日期格式，年月日时分，例如：200506301210，200808081210
     */
    public static final String DATE_FORMAT_YYYYMMDDHHmm = "yyyyMMddHHmm";

    /**
     * 日期格式，年月日时分，例如：20001230 12:00，20080808 20:08
     */
    public static final String DATE_TIME_FORMAT_YYYYMMDD_HH_MI = "yyyyMMdd HH:mm";

    /**
     * 日期格式，年月日时分，例如：2000-12-30 12:00，2008-08-08 20:08
     */
    public static final String DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI = "yyyy-MM-dd HH:mm";

    /**
     * 日期格式，年月日时分秒，例如：20001230120000，20080808200808
     */
    public static final String DATE_TIME_FORMAT_YYYYMMDDHHMISS = "yyyyMMddHHmmss";

    /**
     * 日期格式，年月日时分秒，年月日用横杠分开，时分秒用冒号分开
     * 例如：2005-05-10 23：20：00，2008-08-08 20:08:08
     */
    public static final String DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式，年月日时分秒毫秒，例如：20001230120000123，20080808200808456
     */
    public static final String DATE_TIME_FORMAT_YYYYMMDDHHMISSSSS = "yyyyMMddHHmmssSSS";

    /**
     * 时间戳转某种时间格式
     *
     * @param timestamp
     * @return
     */
    public static String timestampToDate(long timestamp, String timeFormat) {
        String result = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeFormat);
            Date date = new Date(timestamp);
            result = simpleDateFormat.format(date);
        } catch (Exception e) {
            log.error("时间戳转某种时间格式异常:", e);
        }
        return result;
    }

    /**
     * 对日期进行加减后根据格式返回日期字符串
     * @param date
     * @param numDay
     * @param pattern
     * @return
     */
    public static String subDay(Date date, int numDay, String pattern) {
        SimpleDateFormat df=new SimpleDateFormat(pattern);
        return df.format(new Date(date.getTime() - (long)numDay * 24 * 60 * 60 * 1000));
    }

    /**
     * 对日期进行加减后根据格式返回日期字符串
     * @param date
     * @param numDay
     * @param pattern
     * @return
     */
    public static String subDay(String date, int numDay, String pattern) {
        try {
            SimpleDateFormat df=new SimpleDateFormat(pattern);
            Date parse = df.parse(date);
            return df.format(new Date(parse.getTime() - (long)numDay * 24 * 60 * 60 * 1000));
        } catch (Exception e) {
            log.error("日期转换错误", e);
        }
        return null;
    }

    /**
     * 根据时间戳获取0分或30分
     * @param timestamp 时间戳
     * @return
     */
    public static Date getHalfMinutes(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        // 秒
        calendar.set(Calendar.SECOND, 0);
        // 毫秒
        calendar.set(Calendar.MILLISECOND, 0);
        int minutes = calendar.get(Calendar.MINUTE);
        if (minutes < 30) {
            calendar.set(Calendar.MINUTE, 0);
        } else {
            calendar.set(Calendar.MINUTE, 30);
        }
        return calendar.getTime();
    }

    /**
     * 时间转时间戳
     * @param date    时间
     * @param pattern 时间戳
     * @return
     */
    public static Long strToTimestamp(String date, String pattern) {
        SimpleDateFormat df=new SimpleDateFormat(pattern);
        Long timestamp = 0L;
        try {
            Date parse = df.parse(date);
            timestamp = parse.getTime();
        } catch (ParseException e) {
            log.error("strToTimestamp error", e);
        }
        return timestamp;
    }

    /**
     * 求两个日期的相差天数
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param partten   日期格式
     * @return
     */
    public static Integer subDate(String startDate, String endDate, String partten) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(partten);
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            Long subDay = (end.getTime() - start.getTime()) / 1000 / 24 / 60 / 60;
            return subDay.intValue();
        } catch (Exception e) {
            log.error("时间转换异常", e);
        }
        return null;
    }
}
