package com.seaboxdata.sdps.seaboxProxy.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.framework.bean.request.DirRequest;
import com.seaboxdata.sdps.common.framework.bean.request.StorgeRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

/**
 * 时间工具类
 */
@Slf4j
public class SeaboxDateUtil {

    private static final String HOUR_PATTERN = "hour";
    private static final String DAY_PATTERN = "day";
    private static final String MONTH_PATTERN = "month";
    private static final List<String> HOUR_FORMATS = Arrays.asList("yyyy-MM-dd HH", "MM/dd/yyyy HH", "yyyy:MM:dd HH", "yyyyMMddHH");
    private static final List<String> DAY_FORMATS = Arrays.asList("yyyy-MM-dd", "MM/dd/yyyy", "yyyy:MM:dd", "yyyyMMdd");
    private static final List<String> MONTH_FORMATS = Arrays.asList("yyyy-MM", "yyyy:MM", "yyyyMM");
    private static final String[] FORMATS = new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH", "yyyy-MM-dd", "yyyy-MM", "MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy HH:mm", "MM/dd/yyyy HH", "MM/dd/yyyy", "MM/dd/yyyy'T'HH:mm:ss.SSS'Z'", "MM/dd/yyyy'T'HH:mm:ss.SSSZ", "MM/dd/yyyy'T'HH:mm:ss.SSS", "MM/dd/yyyy'T'HH:mm:ssZ", "MM/dd/yyyy'T'HH:mm:ss", "yyyy:MM:dd HH:mm:ss", "yyyy:MM:dd HH:mm", "yyyy:MM:dd HH", "yyyy:MM:dd", "yyyy:MM", "yyyyMMddHHmmss", "yyyyMMddHHmm", "yyyyMMddHH", "yyyyMMdd", "yyyyMM"};

    public static void setDayInfo(StorgeRequest storgeRequest) {
        if (StrUtil.isNotBlank(storgeRequest.getStartDay())
                && StrUtil.isNotBlank(storgeRequest.getEndDay())) {
            return;
        }
        String startDay = "";
        DateTime endDate = DateUtil.offsetDay(DateUtil.date(), -1);
        String endDay = DateUtil.format(endDate,
                CommonConstant.SIMPLE_DATE_FORMAT);
        storgeRequest.setEndDay(endDay);
        if (StrUtil.isBlank(storgeRequest.getPeriod())) {
            return;
        }
        switch (storgeRequest.getPeriod()) {
            case "WEEK":
                startDay = DateUtil.format(DateUtil.offsetWeek(endDate, -1),
                        CommonConstant.SIMPLE_DATE_FORMAT);
                break;
            case "MONTH":
                startDay = DateUtil.format(DateUtil.offsetMonth(endDate, -1),
                        CommonConstant.SIMPLE_DATE_FORMAT);
                break;
            case "YEAR":
                startDay = DateUtil.format(DateUtil.offsetMonth(endDate, -12),
                        CommonConstant.SIMPLE_DATE_FORMAT);
                break;
            default:
                throw new BusinessException("时间范围输入的格式为WEEK,MONTH,YEAR");
        }
        storgeRequest.setStartDay(startDay);
    }

    public static void setDayInfo(DirRequest dirRequest) {
        StorgeRequest storgeRequest = new StorgeRequest();
        BeanUtils.copyProperties(dirRequest, storgeRequest);
        setDayInfo(storgeRequest);
        dirRequest.setEndDay(storgeRequest.getEndDay());
        dirRequest.setStartDay(storgeRequest.getStartDay());
    }

    public static SimpleDateFormat parseFormat(String date) {
        if (StringUtils.isBlank(date)) {
            return null;
        } else {
            SimpleDateFormat sdf = null;
            int length = 0;

            while(length < FORMATS.length) {
                String format = FORMATS[length];
                try {
                    sdf = new SimpleDateFormat(format);
                    sdf.parse(date);
                    break;
                } catch (Exception var7) {
                    sdf = null;
                    log.error("parse date error of " + date + "，format=" + format);
                    ++length;
                }
            }

            return sdf;
        }
    }

    public static String parsePattern(SimpleDateFormat format) {
        if (format == null) {
            return null;
        } else {
            String formatPattern = format.toPattern();
            if (HOUR_FORMATS.contains(formatPattern)) {
                return "hour";
            } else if (DAY_FORMATS.contains(formatPattern)) {
                return "day";
            } else {
                return MONTH_FORMATS.contains(formatPattern) ? "month" : null;
            }
        }
    }

    public static SimpleDateFormat getFormatByPattern(String pattern) {
        if ("hour".equals(pattern)) {
            return new SimpleDateFormat("yyyyMMddHH");
        } else if ("day".equals(pattern)) {
            return new SimpleDateFormat("yyyyMMdd");
        } else {
            return "month".equals(pattern) ? new SimpleDateFormat("yyyyMM") : new SimpleDateFormat("yyyyMMdd");
        }
    }

    public static long getTodayZeroTime() {
        long currentTime = Clock.systemUTC().millis();
        return currentTime / 86400000L * 86400000L - (long) TimeZone.getDefault().getRawOffset();
    }
}
