package com.seaboxdata.sdps.licenseutil.util;



import com.seaboxdata.sdps.licenseutil.exception.license.BusinessException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * 提供日期时间处理的工具类.
 *
 * @author
 */
@Slf4j
public class DateUtil{

    /**
     * 日期格式(yyyy-MM-dd)
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_PATTERN1 = "yyyy_MM_dd";

    public static final String DATE_TIME_PATTERN2 = "yyyyMMddHHmmss";

    public static final String DATE_TIME_PATTERN3 = "yyyyMMddHHmmssSSS";


    /**
     * 日期时间格式(yyyy-MM-dd HH:mm:ss)
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期时间格式(yyyy-MM-dd HH:mm:ss)
     */
    public static final String DATE_TIME_PATTERNA = "yyyy-MM-dd HH:mm:ss";
    /**
     * 日期时间格式(yyyy-MM-dd)
     */
    public static final String DATE_TIME_PATTERNB = "yyyy-MM-dd";

    /**
     * 日期时间格式(yyyy/MM/dd HH:mm)
     */
    public static final String DATE_TIME_PATTERNC = "yyyy/MM/dd HH:mm";

    /**
     * 日期时间格式(yyyy/MM/dd)   mm:ss
     */
    public static final String DATE_TIME_PATTERND = "yyyy/MM/dd";

    /**
     * 日期时间格式(mm:ss)
     */
    public static final String DATE_TIME_PATTERNE = "mm:ss";

    /**
     * 日期时间格式(MM/dd/yyyy)
     */
    public static final String DATE_TIME_PATTERNF = "MM/dd/yyyy";

    /**
     * 时间格式(HH:mm:ss)
     */
    public static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * 按指定的格式({@link SimpleDateFormat})格式化时间
     *
     * @param millis 时间毫秒数.
     * @param pattern 格式
     * @return 格式化后的时间
     */
    public static String format(long millis, String pattern) {
        return format(new Date(millis), pattern);
    }

    /**
     * 时间相减得出分钟
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static long getDateMinus(String startDate, String endDate, String pattern) {
        SimpleDateFormat myFormatter = new SimpleDateFormat(pattern);
        try {
            Date date = myFormatter.parse(startDate);
            Date mydate = myFormatter.parse(endDate);
            long day = (mydate.getTime() - date.getTime()) / (60 * 1000);
            return day;
        } catch (ParseException e) {
            log.error("解析异常", e);
        }
        return 0;
    }

    /**
     * 实现日期加一天的方法
     *
     * @param s
     * @param n
     * @return
     */
    public static String addDay(String s, int n) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Calendar cd = Calendar.getInstance();
            cd.setTime(sdf.parse(s));
            cd.add(Calendar.DATE, n);//增加一天
            //cd.add(Calendar.MONTH, n);//增加一个月

            return sdf.format(cd.getTime());

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据年 月 获取对应的月份 天数
     */
    public static int getDaysByYearMonth(String dateString) {
        int year = Integer.parseInt(dateString.substring(0, 4));
        int month = Integer.parseInt(dateString.substring(5, 7));
        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month - 1);
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 获取某月的最后一天
     *
     * @throws
     * @Title:getLastDayOfMonth
     * @Description:
     * @param:@param year
     * @param:@param month
     * @param:@return
     * @return:String
     */
    public static String getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        //设置年份
        cal.set(Calendar.YEAR, year);
        //设置月份
        cal.set(Calendar.MONTH, month - 1);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        //设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfMonth = sdf.format(cal.getTime());

        return lastDayOfMonth;
    }

    /**
     * 把字符串转为日期
     *
     * @param strDate
     * @return
     * @throws Exception
     */
    public static Date ConverToDate(String strDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            log.error("解析异常", e);
        }
        return null;
    }

    /**
     * @param strDate
     * @param format
     * @return
     */
    public static Date ConverToDate(String strDate, String format) {
        DateFormat df = new SimpleDateFormat(format);
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            log.error("解析异常", e);
        }
        return null;
    }


    /**
     * 获取某段时间的所有的日期
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<Date> findDates(String startTime, String endTime) {
        Date startDate = DateUtil.ConverToDate(startTime);
        Date endDate = DateUtil.ConverToDate(endTime);
        List<Date> lDate = new ArrayList<Date>();
        if (null == startDate || null == endDate) {
            return lDate;
        }
        lDate.add(startDate);
        Calendar calBegin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间    
        calBegin.setTime(startDate);
        Calendar calEnd = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间    
        calEnd.setTime(endDate);
        // 测试此日期是否在指定日期之后
        while (endDate.after(calBegin.getTime())) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量    
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            lDate.add(calBegin.getTime());
        }
        return lDate;
    }

    /**
     * 根据日期取得星期几(返回int型)
     *
     * @param date
     * @return
     */
    public static int getWeek(Date date) {
        int[] weeks = {0, 1, 2, 3, 4, 5, 6};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week_index < 0) {
            week_index = 0;
        }
        return weeks[week_index];
    }

    /**
     * 计算两个日期的时间差
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static String getTimeDifference(String startTime, String endTime) {
        SimpleDateFormat dfs = new SimpleDateFormat("HH:mm:ss");//"yyyy-MM-dd HH:mm:ss.SSS"
        long between = 0;
        try {
            Date begin = dfs.parse(startTime);//"2009-07-21 11:23:21"
            Date end = dfs.parse(endTime);//"2009-07-21 11:24:49"
            between = (end.getTime() - begin.getTime());// 得到两者的毫秒数
        } catch (Exception ex) {
            log.error("解析异常", ex);
        }
        long day = between / (24 * 60 * 60 * 1000);
        long hour = (between / (60 * 60 * 1000) - day * 24);
        long min = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        //long ms = (between - day * 24 * 60 * 60 * 1000 - hour * 60 * 60 * 1000
        //       - min * 60 * 1000 - s * 1000);//毫秒
        String date = day + "天" + hour + "小时" + min + "分" + s + "秒";
        if (day == 0 && hour != 0) {
            date = hour + "小时" + min + "分" + s + "秒";
        } else if (day == 0 && hour == 0 && min != 0) {
            date = min + "分" + s + "秒";
        } else if (day == 0 && hour == 0 && min == 0) {
            date = +s + "秒";
        }
        return date;
    }

    /**
     * 两个时间之差
     *
     * @param startDate
     * @param endDate
     * @return 分钟
     */
    public static Integer getBetweenMinutes(Date startDate, Date endDate) {
        Integer minutes = 0;
        try {
            if (startDate != null && endDate != null) {
                long ss = 0;
                if (startDate.before(endDate)) {
                    ss = endDate.getTime() - startDate.getTime();
                } else {
                    ss = startDate.getTime() - endDate.getTime();
                }
                minutes = Integer.valueOf((int) (ss / (60 * 1000)));
            }
        } catch (Exception e) {
            log.error("解析异常", e);
        }
        return minutes;
    }

    /**
     * 获取当前时间到第二天早上九点的时间的秒数
     *
     * @return
     */
    public static int getNextDaySec() {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, 1);//加一天
        cal.set(Calendar.HOUR_OF_DAY, 9);//设置小时
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Long tm = cal.getTimeInMillis() - date.getTime();
        return (int) (tm / 1000);
    }

    /**
     * 根据日期取得星期几
     *
     * @param date
     * @return
     */
    public static String getWeekOfDate(Date date) {
        String[] weekOfDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week_index < 0) {
            week_index = 0;
        }
        return weekOfDays[week_index];
    }

    /**
     * 按指定的格式({@link SimpleDateFormat})格式化时间
     *
     * @param date 时间
     * @param pattern 格式
     * @return 格式化后的时间
     */
    public static String format(Date date, String pattern) {
        DateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    /**
     * 按默认的格式(yyyy-MM-dd)格式化日期
     *
     * @param date 时间
     * @return 格式化后的日期
     */
    public static String formatDate(Date date) {
        return format(date, DATE_PATTERN);
    }

    /**
     * 按默认的格式(HH:mm:ss)格式化时间
     *
     * @param date 时间
     * @return 格式化后的时间
     */
    public static String formatTime(Date date) {
        return format(date, TIME_PATTERN);
    }

    /**
     * 按默认的格式(yyyy-MM-dd HH:mm:ss)格式化日期时间
     *
     * @param date 时间
     * @return 格式化后的日期时间.
     */
    public static String formatDateTime(Date date) {
        return format(date, DATE_TIME_PATTERN);
    }

    /**
     * 按指定的格式({@link SimpleDateFormat})格式化当前时间
     *
     * @param pattern 格式
     * @return 格式化后的当前时间
     */
    public static String formatCurrent(String pattern) {
        return format(new Date(), pattern);
    }

    /**
     * 按默认的格式(yyyy-MM-dd)格式化当前日期
     *
     * @return 格式化后的当前日期
     */
    public static String formatCurrentDate() {
        return format(new Date(), DATE_PATTERN);
    }

    /**
     * 按默认的格式(HH:mm:ss)格式化当前时间
     *
     * @return 格式化后的当前时间.
     */
    public static String formatCurrentTime() {
        return format(new Date(), TIME_PATTERN);
    }

    /**
     * 按默认的格式(yyyy-MM-dd HH:mm:ss)格式化当前日期时间
     *
     * @return 格式化后的当前日期时间.
     */
    public static String formatCurrentDateTime() {
        return format(new Date(), DATE_TIME_PATTERN);
    }

    /**
     * 获得今天凌晨0点0分对应的时间
     *
     * @return 今天凌晨0点0分对应的时间
     */
    public static Date getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获得指定时间当天凌晨0点0分对应的时间
     *
     * @param date 指定的时间
     * @return 指定时间当天凌晨0点0分对应的时间
     */
    public static Date getTheDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 比较开始日期和结束日期
     *
     * @param start 开始日期
     * @param end 结束日期
     * @return 如果start和end均为<code>null</code>或他们代表的日期相同,返回0;
     *         如果end为<code>null</code>,返回大于0的数字;
     *         如果start为<code>null</code>,则start假设为当前日期和end比较,
     *         start代表日期在end代表的日期前,则返回小于0的数字,
     *         start代表日期在end代表的日期后,则返回大于0的数字.
     */
    public static int compareDate(Date start, Date end) {
        if (start == null && end == null) {
            return 0;
        } else if (end == null) {
            return 1;
        } else if (start == null) {
            start = new Date();
        }
        start = getTheDate(start);
        end = getTheDate(end);
        return start.compareTo(end);
    }

    /**
     * 从指定的日期字符串中解释出时间, 解释的规则({@link SimpleDateFormat})由pattern指定
     *
     * @param dateString 日期字符串
     * @param pattern 解释规则
     * @return 时间
     */
    public static Date stringToDate(String dateString, String pattern) {
        DateFormat formatter = new SimpleDateFormat(pattern);
        try {
            return formatter.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 返回指定时间点 <code>amount</code> 年后(前)的时间. 返回新对象,源日期对象(<code>date</code>)不变
     *
     * @param date 源时间,不能为<code>null</code>.
     * @param amount 年数,正数为加,负数为减.
     * @return 增加(减少)指定年数后的日期对象.
     * @throws IllegalArgumentException 如果源日期对象(<code>date</code>)为<code>null</code>.
     */
    public static Date addYears(Date date, int amount) {
        return add(date, Calendar.YEAR, amount);
    }

    /**
     * 返回指定时间点 <code>amount</code> 月后(前)的时间. 返回新对象,源日期对象(<code>date</code>)不变.
     *
     * @param date 源时间,不能为<code>null</code>.
     * @param amount 月数,正数为加,负数为减.
     * @return 增加(减少)指定月数后的日期对象.
     * @throws IllegalArgumentException 如果源日期对象(<code>date</code>)为<code>null</code>.
     */
    public static Date addMonths(Date date, int amount) {
        return add(date, Calendar.MONTH, amount);
    }

    /**
     * 返回指定时间点 <code>amount</code> 星期后(前)的时间. 返回新对象,源日期对象(<code>date</code>)不变.
     *
     * @param date 源时间,不能为<code>null</code>.
     * @param amount 星期数,正数为加,负数为减.
     * @return 增加(减少)指定星期数后的日期对象.
     * @throws IllegalArgumentException 如果源日期对象(<code>date</code>)为<code>null</code>.
     */
    public static Date addWeeks(Date date, int amount) {
        return add(date, Calendar.WEEK_OF_YEAR, amount);
    }

    /**
     * 返回指定时间点 <code>amount</code> 天后(前)的时间. 返回新对象,源日期对象(<code>date</code>)不变.
     *
     * @param date 源时间,不能为<code>null</code>.
     * @param amount 天数,正数为加,负数为减.
     * @return 增加(减少)指定天数后的日期对象.
     * @throws IllegalArgumentException 如果源日期对象(<code>date</code>)为<code>null</code>.
     */
    public static Date addDays(Date date, int amount) {
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }

    /**
     * 返回指定时间点 <code>amount</code> 小时后(前)的时间. 返回新对象,源日期对象(<code>date</code>)不变.
     *
     * @param date 源时间,不能为<code>null</code>.
     * @param amount 小时数,正数为加,负数为减.
     * @return 增加(减少)指定小时数后的日期对象.
     * @throws IllegalArgumentException 如果源日期对象(<code>date</code>)为<code>null</code>.
     */
    public static Date addHours(Date date, int amount) {
        return add(date, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * 返回指定时间点 <code>amount</code> 分钟后(前)的时间. 返回新对象,源日期对象(<code>date</code>)不变.
     *
     * @param date 源时间,不能为<code>null</code>.
     * @param amount 分钟数,正数为加,负数为减.
     * @return 增加(减少)指定分钟数后的日期对象.
     * @throws IllegalArgumentException 如果源日期对象(<code>date</code>)为<code>null</code>.
     */
    public static Date addMinutes(Date date, int amount) {
        return add(date, Calendar.MINUTE, amount);
    }

    /**
     * 返回指定时间点 <code>amount</code> 秒后(前)的时间. 返回新对象,源日期对象(<code>date</code>)不变.
     *
     * @param date 源时间,不能为<code>null</code>.
     * @param amount 秒数,正数为加,负数为减.
     * @return 增加(减少)指定秒数后的日期对象.
     * @throws IllegalArgumentException 如果源日期对象(<code>date</code>)为<code>null</code>.
     */
    public static Date addSeconds(Date date, int amount) {
        return add(date, Calendar.SECOND, amount);
    }

    /**
     * 返回指定时间点 <code>amount</code> 豪秒后(前)的时间. 返回新对象,源日期对象(<code>date</code>)不变.
     *
     * @param date 源时间,不能为<code>null</code>.
     * @param amount 豪秒数,正数为加,负数为减.
     * @return 增加(减少)指定豪秒数后的日期对象.
     * @throws IllegalArgumentException 如果源日期对象(<code>date</code>)为<code>null</code>.
     */
    public static Date addMilliseconds(Date date, int amount) {
        return add(date, Calendar.MILLISECOND, amount);
    }

    /**
     * 返回指定时间点增加(减少)一段时间后的时间. 返回新对象,源日期对象(<code>date</code>)不变
     *
     * @param date 源时间,不能为<code>null</code>.
     * @param calendarField 增加(减少)的日历项.
     * @param amount 数量,正数为加,负数为减.
     * @return 日历项增加(减少)指定数目后的日期对象.
     * @throws IllegalArgumentException 如果源日期对象(<code>date</code>)为<code>null</code>.
     */
    private static Date add(Date date, int calendarField, int amount) {
        if (date == null) {
            throw new IllegalArgumentException("日期对象不允许为null!");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    /**
     * 返回指定时间增加（减少）自然日及小时候的日期对象
     *
     * @param date 源时间
     * @param days 增加（减少）的天数
     * @param hours 增加（减少）的小时数
     * @return 增加（减少）天、小时后的日期对象
     */
    public static Date getDateAfterNatureDays(Date date, int days, int hours) {
        if (date == null) {
            date = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        c.add(Calendar.HOUR, hours);

        return c.getTime();
    }

    /**
     * 返回指定时间增加（减少）工作日及小时候的日期对象
     *
     * @param date 源时间
     * @param days 增加（减少）的天数
     * @param hours 增加（减少）的小时数
     * @return 增加（减少）天、小时后的日期对象
     */
    public static Date getDateAfterWorkDays(Date date, int days, int hours) {
        if (date == null) {
            date = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        c.add(Calendar.HOUR, hours);

        return c.getTime();
    }

    /*
     * 将时间转换为时间戳
     */
    public static String dateToStamp(String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s) {
        String res = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long lt = Long.parseLong(s);
            Date date = new Date(lt);
            res = simpleDateFormat.format(date);
        } catch (Exception e) {
            //throw new Exception("请检查时间是否是时间搓类型");
        }

        return res;
    }

    /**
     * 计算日期,传入一个整数的天数计算出当前日期到天数后的日期Date对象
     *
     * @param days 天数 整数
     * @return Date对象
     */
    public static Date dateCalculation(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + days);
        return calendar.getTime();
    }

    /**
     * 格式化运行时间 单位 秒
     */
    public static String runTimeFormat(Long utime) {

        Long sec = utime;
        String res = "";
        int days = 0;
        int hours = 0;
        int mins = 0;
        if (sec >= 86400) {
            days = (int) Math.floor((double) sec / 86400);
            sec = sec % 86400;
            if (days > 0) {
                res = days + "天";
            }
        }

        if (sec >= 3600) {
            hours = (int) Math.floor((double) sec / 3600);
            sec = sec % 3600;
            if (hours > 0) {
                res = res + hours + "小时";
            }
        }

        if (sec >= 60) {
            mins = (int) Math.floor((double) sec / 60);
            sec = sec % 60;
            res = res + mins + "分钟";
        } else {
            res = res + "0分钟";
        }

        return res + sec + "秒";
    }

    public static void main(String[] args) {
        String s = stampToDate("1502964348398");
        System.out.println(s);

        System.out.println(runTimeFormat(864000L));
    }

    public static int getTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }


    /**
     * 获取从最小日期开始 n个 interval年的开始
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToYearBeginning(List<Long> data,
                                           Long interval,
                                           Date minDate){
        Calendar minCal = Calendar.getInstance();
        minCal.setTime(minDate);
        int minYear = minCal.get(Calendar.YEAR);

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            int year = calendar.get(Calendar.YEAR);

                            long actualYear = ((year - minYear) / interval) * interval  + minYear;

                            Calendar newCal = Calendar.getInstance();
                            newCal.set(Calendar.YEAR, (int) actualYear);
                            newCal.set(Calendar.MONTH, 0);
                            newCal.set(Calendar.DAY_OF_MONTH,1);
                            newCal.set(Calendar.HOUR_OF_DAY,0);
                            newCal.set(Calendar.MINUTE,0);
                            newCal.set(Calendar.SECOND,0);
                            newCal.set(Calendar.MILLISECOND,0);
                            return newCal.getTime().getTime();
                        }
                ).collect(Collectors.toList());
    }

    /**
     * 获取从最小日期开始 n个 interval年的结尾（-1秒）
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToYearEnd(List<Long> data,
            Long interval,
            Date minDate){
        Calendar minCal = Calendar.getInstance();
        minCal.setTime(minDate);
        int minYear = minCal.get(Calendar.YEAR);

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            int year = calendar.get(Calendar.YEAR);

                            //+1是为了获取粒度的结尾
                            long actualYear = ((year - minYear) / interval + 1) * interval + minYear;

                            Calendar newCal = Calendar.getInstance();
                            newCal.set(Calendar.YEAR, (int) actualYear);
                            newCal.set(Calendar.MONTH, 0);
                            newCal.set(Calendar.DAY_OF_MONTH,1);
                            newCal.set(Calendar.HOUR_OF_DAY,0);
                            newCal.set(Calendar.MINUTE,0);
                            newCal.set(Calendar.SECOND,0);
                            newCal.set(Calendar.MILLISECOND,0);
                            return newCal.getTime().getTime() - 1000;
                        }
                ).collect(Collectors.toList());
    }

    /**
     * 获取从最小日期开始 n个 interval季度的开始
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToSeasonBeginning(List<Long> data,
                                                 Long interval,
                                                 Date minDate){
        Calendar minCal = Calendar.getInstance();
        minCal.setTime(minDate);
        int minSeason = minCal.get(Calendar.YEAR) * 4 + (minCal.get(Calendar.MONTH)/3);

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            int season = calendar.get(Calendar.YEAR) * 4 + (calendar.get(Calendar.MONTH) / 3);

                            long actualSeason = ((season - minSeason) / interval) * interval + minSeason;

                            Calendar newCal = Calendar.getInstance();
                            newCal.set(Calendar.YEAR, (int) actualSeason / 4);
                            newCal.set(Calendar.MONTH, (int) (actualSeason % 4) * 3);
                            newCal.set(Calendar.DAY_OF_MONTH,1);
                            newCal.set(Calendar.HOUR_OF_DAY,0);
                            newCal.set(Calendar.MINUTE,0);
                            newCal.set(Calendar.SECOND,0);
                            newCal.set(Calendar.MILLISECOND,0);
                            return newCal.getTime().getTime();
                        }
                ).collect(Collectors.toList());
    }

    /**
     * 获取从最小日期开始 n个 interval季度的结尾（-1秒）
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToSeasonEnd(List<Long> data,
            Long interval,
            Date minDate){
        Calendar minCal = Calendar.getInstance();
        minCal.setTime(minDate);
        int minSeason = minCal.get(Calendar.YEAR) * 4 + (minCal.get(Calendar.MONTH)/3);

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            int season = calendar.get(Calendar.YEAR) * 4 + (calendar.get(Calendar.MONTH) / 3);

                            //+1是为了获取粒度的结尾
                            long actualSeason = ((season - minSeason) / interval + 1) * interval + minSeason;

                            Calendar newCal = Calendar.getInstance();
                            newCal.set(Calendar.YEAR, (int) actualSeason / 4);
                            newCal.set(Calendar.MONTH, (int) (actualSeason % 4) * 3);
                            newCal.set(Calendar.DAY_OF_MONTH,1);
                            newCal.set(Calendar.HOUR_OF_DAY,0);
                            newCal.set(Calendar.MINUTE,0);
                            newCal.set(Calendar.SECOND,0);
                            newCal.set(Calendar.MILLISECOND,0);
                            return newCal.getTime().getTime() - 1000;
                        }
                ).collect(Collectors.toList());
    }





    /**
     * 获取从最小日期开始 n个 interval月的开始
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToMonthBeginning(List<Long> data,
                                                   Long interval,
                                                   Date minDate){
        Calendar minMon = Calendar.getInstance();
        minMon.setTime(minDate);
        int minMonth = minMon.get(Calendar.YEAR) * 12 + minMon.get(Calendar.MONTH);

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            int month = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

                            long actualMonth = ((month - minMonth) / interval) * interval + minMonth;

                            Calendar newCal = Calendar.getInstance();
                            newCal.set(Calendar.YEAR, (int) actualMonth / 12);
                            newCal.set(Calendar.MONTH, (int) actualMonth % 12);
                            newCal.set(Calendar.DAY_OF_MONTH,1);
                            newCal.set(Calendar.HOUR_OF_DAY,0);
                            newCal.set(Calendar.MINUTE,0);
                            newCal.set(Calendar.SECOND,0);
                            newCal.set(Calendar.MILLISECOND,0);
                            return newCal.getTime().getTime();
                        }
                ).collect(Collectors.toList());
    }

    /**
     * 获取从最小日期开始 n个 interval月的结尾（-1秒）
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToMonthEnd(List<Long> data,
            Long interval,
            Date minDate){
        Calendar minMon = Calendar.getInstance();
        minMon.setTime(minDate);
        int minMonth = minMon.get(Calendar.YEAR) * 12 + minMon.get(Calendar.MONTH);

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            int month = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

                            //+1是为了获取粒度的结尾
                            long actualMonth = ((month - minMonth) / interval + 1) * interval + minMonth;

                            Calendar newCal = Calendar.getInstance();
                            newCal.set(Calendar.YEAR, (int) actualMonth / 12);
                            newCal.set(Calendar.MONTH, (int) actualMonth % 12);
                            newCal.set(Calendar.DAY_OF_MONTH,1);
                            newCal.set(Calendar.HOUR_OF_DAY,0);
                            newCal.set(Calendar.MINUTE,0);
                            newCal.set(Calendar.SECOND,0);
                            newCal.set(Calendar.MILLISECOND,0);
                            return newCal.getTime().getTime() - 1000;
                        }
                ).collect(Collectors.toList());
    }

    /**
     * 获取从最小日期开始 n个 interval周的开始
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToWeekBeginning(List<Long> data,
                                                  Long interval,
                                                  Date minDate){
        Calendar minw = Calendar.getInstance();
        minw.setTime(minDate);
        minw.setFirstDayOfWeek(Calendar.MONDAY);

        // 获得当前日期是一个星期的第几天
        int dayWeek = minw.get(Calendar.DAY_OF_WEEK);
        if(dayWeek==1){
            dayWeek = 8;
        }
        minw.add(Calendar.DATE, minw.getFirstDayOfWeek() - dayWeek);// 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值

        minw.set(Calendar.HOUR_OF_DAY,0);
        minw.set(Calendar.MINUTE,0);
        minw.set(Calendar.SECOND,0);
        minw.set(Calendar.MILLISECOND,0);
        long minWeek = minw.getTime().getTime();

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            long week = calendar.getTime().getTime();
                            //还没计算隔几周呢

                            long size = 1000 * 60 * 60 * 24 * 7 * interval;
                            return ((week - minWeek)/size) * size + minWeek;


                        }
                ).collect(Collectors.toList());
    }


    /**
     * 获取从最小日期开始 n个 interval周的结尾（-1秒）
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToWeekEnd(List<Long> data,
            Long interval,
            Date minDate){
        Calendar minw = Calendar.getInstance();
        minw.setTime(minDate);
        minw.setFirstDayOfWeek(Calendar.MONDAY);

        // 获得当前日期是一个星期的第几天
        int dayWeek = minw.get(Calendar.DAY_OF_WEEK);
        if(dayWeek==1){
            dayWeek = 8;
        }
        minw.add(Calendar.DATE, minw.getFirstDayOfWeek() - dayWeek);// 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值

        minw.set(Calendar.HOUR_OF_DAY,0);
        minw.set(Calendar.MINUTE,0);
        minw.set(Calendar.SECOND,0);
        minw.set(Calendar.MILLISECOND,0);
        long minWeek = minw.getTime().getTime();

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            long week = calendar.getTime().getTime();
                            //还没计算隔几周呢

                            long size = 1000 * 60 * 60 * 24 * 7 * interval;
                            //+1是为了获取粒度的结尾
                            return ((week - minWeek)/size + 1) * size + minWeek - 1000;
                        }
                ).collect(Collectors.toList());
    }


    /**
     * 获取从最小日期开始 n个 interval日的开始
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToDayBeginning(List<Long> data,
                                                 Long interval,
                                                 Date minDate){
        Calendar minD = Calendar.getInstance();
        minD.setTime(minDate);
        minD.set(Calendar.HOUR_OF_DAY,0);
        minD.set(Calendar.MINUTE,0);
        minD.set(Calendar.SECOND,0);
        minD.set(Calendar.MILLISECOND,0);

        long minDay = minD.getTime().getTime();

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            long day = calendar.getTime().getTime();

                            long size = 1000 * 60 * 60 * 24 * interval;

                            return ((day - minDay) / size) * size + minDay;
                        }
                ).collect(Collectors.toList());
    }


    /**
     * 获取从最小日期开始 n个 interval日的结尾（-1秒）
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToDayEnd(List<Long> data,
            Long interval,
            Date minDate){
        Calendar minD = Calendar.getInstance();
        minD.setTime(minDate);
        minD.set(Calendar.HOUR_OF_DAY,0);
        minD.set(Calendar.MINUTE,0);
        minD.set(Calendar.SECOND,0);
        minD.set(Calendar.MILLISECOND,0);

        long minDay = minD.getTime().getTime();

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            long day = calendar.getTime().getTime();

                            long size = 1000 * 60 * 60 * 24 * interval;

                            //+1是为了获取粒度的结尾
                            return ((day - minDay) / size + 1) * size + minDay - 1000;
                        }
                ).collect(Collectors.toList());
    }

    /**
     * 获取从最小日期开始 n个 interval小时的开始
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToHourBeginning(List<Long> data,
                                                Long interval,
                                                Date minDate){
        Calendar minH = Calendar.getInstance();
        minH.setTime(minDate);
        minH.set(Calendar.MINUTE,0);
        minH.set(Calendar.SECOND,0);
        minH.set(Calendar.MILLISECOND,0);

        long minHour = minH.getTime().getTime();

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            long hour = calendar.getTime().getTime();

                            long size = 1000 * 60 * 60 * interval;

                            return ((hour - minHour) / size) * size + minHour;

                        }
                ).collect(Collectors.toList());
    }

    /**
     * 获取从最小日期开始 n个 interval小时的结尾（-1秒）
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToHourEnd(List<Long> data,
            Long interval,
            Date minDate){
        Calendar minH = Calendar.getInstance();
        minH.setTime(minDate);
        minH.set(Calendar.MINUTE,0);
        minH.set(Calendar.SECOND,0);
        minH.set(Calendar.MILLISECOND,0);

        long minHour = minH.getTime().getTime();

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            long hour = calendar.getTime().getTime();

                            long size = 1000 * 60 * 60 * interval;
                            //+1是为了获取粒度的结尾
                            return ((hour - minHour) / size + 1) * size + minHour - 1000;

                        }
                ).collect(Collectors.toList());
    }


    /**
     * 获取从最小日期开始 n个 interval分钟的开始
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToMinuteBeginning(List<Long> data,
                                                 Long interval,
                                                 Date minDate){
        Calendar minM = Calendar.getInstance();
        minM.setTime(minDate);
        minM.set(Calendar.SECOND,0);
        minM.set(Calendar.MILLISECOND,0);

        long minMinute = minM.getTime().getTime();

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            long minute = calendar.getTime().getTime();

                            long size = 1000 * 60 * interval;

                            return ((minute - minMinute) / size) * size + minMinute;

                        }
                ).collect(Collectors.toList());
    }


    /**
     * 获取从最小日期开始 n个 interval分钟的结尾（-1秒）
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToMinuteEnd(List<Long> data,
            Long interval,
            Date minDate){
        Calendar minM = Calendar.getInstance();
        minM.setTime(minDate);
        minM.set(Calendar.SECOND,0);
        minM.set(Calendar.MILLISECOND,0);

        long minMinute = minM.getTime().getTime();

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            long minute = calendar.getTime().getTime();

                            long size = 1000 * 60 * interval;
                            //+1是为了获取粒度的结尾
                            return ((minute - minMinute) / size + 1) * size + minMinute - 1000;
                        }
                ).collect(Collectors.toList());
    }


    /**
     * 获取从最小日期开始 n个 interval秒的开始
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToSecondBeginning(List<Long> data,
                                                   Long interval,
                                                   Date minDate){
        Calendar minS = Calendar.getInstance();
        minS.setTime(minDate);
        minS.set(Calendar.MILLISECOND,0);

        long minSecond = minS.getTime().getTime();

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            long second = calendar.getTime().getTime();

                            long size = 1000 * interval;

                            return ((second - minSecond) / size) * size + minSecond;

                        }
                ).collect(Collectors.toList());
    }


    /**
     * 获取从最小日期开始 n个 interval秒的结尾（-1秒）
     * @param data 日期数据
     * @param interval 间隔
     * @param minDate 最小日期
     * @return
     */
    public static List<Long> backToSecondEnd(List<Long> data,
            Long interval,
            Date minDate){
        Calendar minS = Calendar.getInstance();
        minS.setTime(minDate);
        minS.set(Calendar.MILLISECOND,0);

        long minSecond = minS.getTime().getTime();

        return data
                .stream()
                .map(
                        timeLine -> {
                            if(timeLine==null){return null;}
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date(timeLine));
                            long second = calendar.getTime().getTime();

                            long size = 1000 * interval;
                            //+1是为了获取粒度的结尾
                            return ((second - minSecond) / size + 1) * size + minSecond - 1000;

                        }
                ).collect(Collectors.toList());
    }


    /**
     * 解析时间
     * @param str 字符串
     * @param format 格式
     * @param timeZone 市区
     * @return
     */
    public static Date parseDateFormat(String str, String format,String timeZone){
        Date newDate = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        if(!StringUtils.isEmpty(timeZone)){
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        }
        try {
            newDate = simpleDateFormat.parse(str);
        } catch (ParseException e) {
        }
        return newDate;
    }

    /**
     * 用多种格式解析日期字符串
     * @param o1 日期字符串
     * @return
     */
    public static Date parseDateWithMulFormat(String o1){
        Date parse = null;
        parse = parseDateFormat(o1,"yyyy-MM-dd'T'hh:mm:ss.SSSZ","GMT");
        if(parse!=null){
            return parse;
        }
        parse = parseDateFormat(o1,"yyyy-MM-dd HH:mm:ss","");
        if(parse!=null){
            return parse;
        }
        //秒
        parse = parseDateFormat(o1,"yyyy年MM月dd日 HH时mm分ss秒","");
        if(parse!=null){
            return parse;
        }
        //分
        parse = parseDateFormat(o1,"yyyy年MM月dd日 HH时mm分","");
        if(parse!=null){
            return parse;
        }
        //小时
        parse = parseDateFormat(o1,"yyyy年MM月dd日 HH时","");
        if(parse!=null){
            return parse;
        }
        //日
        parse = parseDateFormat(o1,"yyyy年MM月dd日","");
        if(parse!=null){
            return parse;
        }
        //周
        parse = parseDateFormat(o1,"yyyy-MM-dd","");
        if(parse!=null){
            return parse;
        }
        //月
        parse = parseDateFormat(o1,"yyyy年MM月","");
        if(parse!=null){
            return parse;
        }
        //季度
        if(o1.contains("季度")){
            String[] s1 = o1.split("年");
            if(s1.length!=2){
                log.error("parseDateWithMulFormat  exception ,o1:{}", o1);
            }
            String[] s2 = s1[1].split("季度");
            if(s2.length!=1){
                log.error("parseDateWithMulFormat exception ,o1:{}", o1);
            }
            int year = Integer.parseInt(s1[0]);
            int quarter = Integer.parseInt(s2[0]);

            Calendar newCal = Calendar.getInstance();
            newCal.set(Calendar.YEAR, year);
            newCal.set(Calendar.MONTH, (quarter-1)*3);
            newCal.set(Calendar.DAY_OF_MONTH,1);
            newCal.set(Calendar.HOUR_OF_DAY,0);
            newCal.set(Calendar.MINUTE,0);
            newCal.set(Calendar.SECOND,0);
            newCal.set(Calendar.MILLISECOND,0);
            parse = newCal.getTime();
            if(parse!=null){
                return parse;
            }
        }
        //年
        parse = parseDateFormat(o1,"yyyy年","");
        if(parse!=null){
            return parse;
        }
        return null;
    }
}
