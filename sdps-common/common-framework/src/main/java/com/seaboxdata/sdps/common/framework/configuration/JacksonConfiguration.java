package com.seaboxdata.sdps.common.framework.configuration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import cn.hutool.core.date.DateUtil;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.seaboxdata.sdps.common.framework.converter.DateConverter;

/**
 * WebMvc Jackson配置
 *
 * @author Joey
 * @date 2018-04-05 TODO 增加FastJson配置(https://www.jianshu.com/p/cff237cd5a70)
 */
@Slf4j
@Configuration
public class JacksonConfiguration {

	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
			DateConverter dateConverter) {
		log.info(">>>>>>>>>>初始化时间转换器<<<<<<<<<<<");
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		objectMapper.setTimeZone(TimeZone.getTimeZone("GTM+8:00"));
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		SimpleModule module = new SimpleModule();
		module.addDeserializer(Date.class, new JsonDeserializer<Date>() {

			@Override
			public Date deserialize(JsonParser arg0, DeserializationContext arg1)
					throws IOException, JsonProcessingException {
				return dateConverter.convert(arg0.getText());
			}

		});
		module.addSerializer(Date.class, new JsonSerializer<Date>() {

			@Override
			public void serialize(Date arg0, JsonGenerator arg1,
					SerializerProvider arg2) throws IOException {
				arg1.writeString(DateUtil.format(arg0, "yyyy-MM-dd HH:mm:ss"));
			}

		});
		objectMapper.registerModule(module);
		converter.setObjectMapper(objectMapper);
		return converter;
	}
}
