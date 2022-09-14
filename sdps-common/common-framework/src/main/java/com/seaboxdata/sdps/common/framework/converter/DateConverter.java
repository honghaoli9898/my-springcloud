package com.seaboxdata.sdps.common.framework.converter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.core.convert.converter.Converter;

import cn.hutool.core.date.DateUtil;

public class DateConverter implements Converter<String, Date> {

	private static final List<String> formarts = new ArrayList<>(5);
	static {
		formarts.add("yyyy-MM");
		formarts.add("yyyy-MM-dd");
		formarts.add("yyyy-MM-dd HH:mm");
		formarts.add("yyyy-MM-dd HH:mm:ss");
		formarts.add("yyyy-MM-dd'T'HH:mm:ss");
	}

	@Override
	public Date convert(String source) {
		String value = source.trim();
		if ("".equals(value)) {
			return null;
		}
		if (source.matches("^\\d{4}-\\d{1,2}$")) {
			return parseDate(source, formarts.get(0));
		} else if (source.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$")) {
			return parseDate(source, formarts.get(1));
		} else if (source
				.matches("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}$")) {
			return parseDate(source, formarts.get(2));
		} else if (source
				.matches("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}$")) {
			return parseDate(source, formarts.get(3));
		} else if (source.length() >= 19
				&& source
						.substring(0, 19)
						.matches(
								"^\\d{4}-\\d{1,2}-\\d{1,2}T{1}\\d{1,2}:\\d{1,2}:\\d{1,2}$")) {
			return parseDate(source.substring(0, 19), formarts.get(4));
		} else if (source.matches("\\d{13}") || source.matches("\\d{15}")) {
			return DateUtil.date(Long.valueOf(source));
		} else {
			throw new IllegalArgumentException("Invalid boolean value '"
					+ source + "'");
		}
	}

	/**
	 * 格式化日期
	 * 
	 * @param dateStr
	 *            String 字符型日期
	 * @param format
	 *            String 格式
	 * @return Date 日期
	 */
	public Date parseDate(String dateStr, String format) {
		Date date = null;
		try {
			DateFormat dateFormat = new SimpleDateFormat(format);
			date = dateFormat.parse(dateStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}

}