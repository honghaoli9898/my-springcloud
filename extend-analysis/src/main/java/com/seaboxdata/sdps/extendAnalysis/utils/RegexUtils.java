package com.seaboxdata.sdps.extendAnalysis.utils;

import com.seaboxdata.sdps.common.framework.bean.analysis.DBTableInfo;
import com.seaboxdata.sdps.common.framework.enums.DirFileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RegexUtils {
    private static final Pattern HIVE_DB_PATTERN = Pattern.compile("^\\/warehouse\\/tablespace\\/managed\\/hive\\/[a-zA-Z_]{1,}[0-9a-zA-Z_]{0,}\\.db$");
    private static final Pattern HIVE_EXTERNAL_DB_PATTERN = Pattern.compile("^\\/warehouse\\/tablespace\\/external\\/hive\\/[a-zA-Z_]{1,}[0-9a-zA-Z_]{0,}\\.db$");
    private static final Pattern HBASE_DB_PATTERN = Pattern.compile("^\\/apps\\/hbase\\/data\\/data\\/[0-9a-zA-Z_]{1,}$");
    private static final Pattern HIVE_DB_MATCHER = Pattern.compile("^\\/warehouse\\/tablespace\\/managed\\/hive\\/(.*?)\\.db$");
    private static final Pattern HIVE_EXTERNAL_DB_MATCHER = Pattern.compile("^\\/warehouse\\/tablespace\\/external\\/hive\\/(.*?)\\.db$");
    private static final Pattern HBASE_DB_MATCHER = Pattern.compile("^\\/apps\\/hbase\\/data\\/data\\/(.*?)$");
    private static final Pattern HIVE_TABLE_PATTERN = Pattern.compile("^\\/warehouse\\/tablespace\\/managed\\/hive\\/[a-zA-Z_]{1,}[0-9a-zA-Z_]{0,}\\.db\\/[a-zA-Z_]{1,}[0-9a-zA-Z_]{0,}$");
    private static final Pattern HBASE_TABLE_PATTERN = Pattern.compile("^\\/apps\\/hbase\\/data\\/data\\/[0-9a-zA-Z_]{1,}\\/[0-9a-zA-Z_]{1,}$");
    private static final Pattern HIVE_TABLE_MATCHER = Pattern.compile("^\\/warehouse\\/tablespace\\/managed\\/hive\\/(.*?)\\.db\\/(.*?)$");
    private static final Pattern HBASE_TABLE_MATCHER = Pattern.compile("^\\/apps\\/hbase\\/data\\/data\\/(.*?)\\/(.*?)$");
    private static final Pattern PROJECT_PATTERN = Pattern.compile("^\\/project\\/[0-9a-zA-Z_]{1,}$");
    private static final Pattern PROJECT_MATCHER = Pattern.compile("^\\/project\\/(.*?)");
    private static List<RegPattern> ALL_REG_PATTERNS;
    private static List<RegexUtils.RegPattern> DB_REG_PATTERNS = new ArrayList();

    static {
        DB_REG_PATTERNS.add(new RegexUtils.RegPattern(HIVE_DB_PATTERN, HIVE_DB_MATCHER, DirFileType.DATABASE_HIVE, RegexUtils.DBCategory.hive, RegexUtils.DBType.db));
        DB_REG_PATTERNS.add(new RegexUtils.RegPattern(HIVE_EXTERNAL_DB_PATTERN, HIVE_EXTERNAL_DB_MATCHER, DirFileType.DATABASE_EXTERNAL_HIVE, RegexUtils.DBCategory.hive_external, RegexUtils.DBType.db));
        DB_REG_PATTERNS.add(new RegexUtils.RegPattern(HBASE_DB_PATTERN, HBASE_DB_MATCHER, DirFileType.DATABASE_HBASE, RegexUtils.DBCategory.hbase, RegexUtils.DBType.db));
        DB_REG_PATTERNS.add(new RegexUtils.RegPattern(HIVE_TABLE_PATTERN, HIVE_TABLE_MATCHER, DirFileType.TABLE_HIVE, RegexUtils.DBCategory.hive, RegexUtils.DBType.table));
        DB_REG_PATTERNS.add(new RegexUtils.RegPattern(HBASE_TABLE_PATTERN, HBASE_TABLE_MATCHER, DirFileType.TABLE_HBASE, RegexUtils.DBCategory.hbase, RegexUtils.DBType.table));
        ALL_REG_PATTERNS = new ArrayList();
        ALL_REG_PATTERNS.addAll(DB_REG_PATTERNS);
        ALL_REG_PATTERNS.add(new RegexUtils.RegPattern(PROJECT_PATTERN, PROJECT_MATCHER, DirFileType.PROJECT, RegexUtils.DBCategory.none, RegexUtils.DBType.none));
    }

    /**
     * 根据路径匹配
     * @param path
     * @return
     */
    public static String matchTenantKey(String path) {
        String key = null;
        try {
            RegexUtils.RegPattern pattern = getMatchPattern(path, ALL_REG_PATTERNS);
            if (pattern == null || !pattern.getMatcher().matcher(path).matches()) {
                return null;
            }
            if (DB_REG_PATTERNS.contains(pattern)) {
                DBTableInfo info = getDBTableByPattern(path, pattern);
                String typeValue = info.getType().equalsIgnoreCase(RegexUtils.DBType.table.name()) ?
                        info.getDbName() + "." + info.getTableName() :
                        info.getDbName();
                key = pattern.getType().name() + "#" + typeValue;
            } else {
                Matcher matcher = pattern.getMatcher().matcher(path);
                matcher.matches();
                key = pattern.getType().name() + "#" + matcher.group(1);
            }
        } catch (Exception e) {
            log.error("根据路径匹配项目异常:", path, e);
            System.exit(1);
        }
        return key;
    }

    /**
     * 对路径根据规则匹配
     *
     * @param path
     * @param patternList
     * @return
     */
    private static RegexUtils.RegPattern getMatchPattern(String path, List<RegexUtils.RegPattern> patternList) {
        RegexUtils.RegPattern regPattern = null;
        Iterator iterator = patternList.iterator();

        while (iterator.hasNext()) {
            RegexUtils.RegPattern entity = (RegexUtils.RegPattern) iterator.next();
            boolean matches = entity.getPattern().matcher(path).matches();
            if (matches) {
                regPattern = entity;
                break;
            }
        }

        return regPattern;
    }

    /**
     * 根据[规则]获取[数据库表信息对象]
     *
     * @param path
     * @param pattern
     * @return
     */
    private static DBTableInfo getDBTableByPattern(String path, RegexUtils.RegPattern pattern) {
        if (pattern == null) {
            return null;
        } else {
            DBTableInfo info = new DBTableInfo();
            info.setType(pattern.getDbType().name());
            info.setCategory(pattern.getDbCategory().name());
            info.setPath(path);
            Matcher matcher = pattern.getMatcher().matcher(path);
            boolean matches = matcher.matches();
            if (!matches) {
                return null;
            } else {
                info.setDbName(matcher.group(1));
                if (pattern.getDbType().name().equalsIgnoreCase(RegexUtils.DBType.table.name())) {
                    info.setTableName(matcher.group(2));
                }
                return info;
            }
        }
    }

    /**
     * 路径匹配数据库或者表
     *
     * @param path
     * @param cluster
     * @return
     */
    public static DBTableInfo matchDBTable(String path, String cluster) {
        DBTableInfo dbTableInfo = null;

        try {
            //根据路径获取
            RegexUtils.RegPattern pattern = getMatchPattern(path, DB_REG_PATTERNS);
            //
            dbTableInfo = getDBTableByPattern(path, pattern);
            if (dbTableInfo != null) {
                dbTableInfo.setCluster(cluster);
            }
        } catch (Exception e) {
            log.error("match db table error of path=" + path, e);
            System.exit(1);
        }

        return dbTableInfo;
    }

    /**
     * 是否匹配数据库或表
     *
     * @param path
     * @return
     */
    public static boolean isMatchDBTable(String path) {
        RegexUtils.RegPattern pattern = null;

        try {
            pattern = getMatchPattern(path, DB_REG_PATTERNS);
        } catch (Exception e) {
            log.error("error of isMatch db Table of path=" + path, e);
            System.exit(1);
        }

        return pattern != null;
    }

    /**
     * 数据库类别
     */
    public static enum DBCategory {
        hive,
        hive_external,
        hbase,
        none;

        private DBCategory() {
        }
    }

    /**
     * 数据库或表或其他
     */
    public static enum DBType {
        db,
        table,
        none;

        private DBType() {
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class RegPattern {
        /**
         * 模式
         */
        private Pattern pattern;
        /**
         * 匹配
         */
        private Pattern matcher;
        /**
         * 目录文件类型
         */
        private DirFileType type;
        /**
         * 数据库类别
         */
        private RegexUtils.DBCategory dbCategory;
        /**
         * 类型
         */
        private RegexUtils.DBType dbType;
    }
}
