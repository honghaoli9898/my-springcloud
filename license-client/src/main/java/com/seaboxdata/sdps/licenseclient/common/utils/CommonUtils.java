package com.seaboxdata.sdps.licenseclient.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CommonUtils{

    public static List<String> getHourList(int cycle) {
        List<String> hourList = new ArrayList<>();
        for (int i = cycle - 1; i >= 0; i--) {
            LocalDateTime now = LocalDateTime.now();
            now = now.minusHours(i);
            hourList.add(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH")));
        }
        return hourList;
    }

    public static List<String> getDayList(int cycle) {
        List<String> dayList = new ArrayList<>();
        for (int i = cycle - 1; i >= 0; i--) {
            LocalDateTime now = LocalDateTime.now();
            now = now.minusDays(i);
            dayList.add(now.toLocalDate().toString());
        }
        return dayList;
    }


    public static List<String> getWeekList(int cycle) {
        List<String> weekList = new ArrayList<>();
        for (int i = cycle - 1; i >= 0; i--) {
            LocalDateTime now = LocalDateTime.now();
            now = now.minusWeeks(i);
            weekList.add(now.minusDays(now.getDayOfWeek().getValue() - 1L).toLocalDate().toString());
        }
        return weekList;
    }


    public static List<String> getMonthList(int cycle) {
        List<String> monthList = new ArrayList<>();
        for (int i = cycle - 1; i >= 0; i--) {
            LocalDateTime now = LocalDateTime.now();
            now = now.minusMonths(i);
            monthList.add(now.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        return monthList;
    }

    public static void loadDataList(Map<String, Integer> resMap, List<String> keyList, List<Integer> dataList,
            String keyPrefix) {
        for (String key : keyList) {
            if (resMap.containsKey(keyPrefix + key)) {
                dataList.add(resMap.get(keyPrefix + key));
            } else {
                dataList.add(0);
            }
        }
    }


    public static Map<String, Object> buildApplicationMap(Map<String, Integer> resMap, List<String> keyList,
            String cycleType) {
        Map<String, Object> map = new HashMap();
        List<Integer> addDataList = new ArrayList<>();
        List<Integer> useDataList = new ArrayList<>();
        List<Map> indexDataList = new ArrayList<>();
        CommonUtils.loadDataList(resMap, keyList, addDataList, cycleType + "_add_");
        CommonUtils.loadDataList(resMap, keyList, useDataList, cycleType + "_use_");
        Map<String, Object> addMap = new HashMap();
        addMap.put("name", "新增数");
        addMap.put("data", addDataList);

        Map<String, Object> useMap = new HashMap();
        useMap.put("name", "使用数");
        useMap.put("data", useDataList);

        indexDataList.add(addMap);
        indexDataList.add(useMap);

        map.put("indexKey", keyList);
        map.put("indexData", indexDataList);
        return map;
    }


}
