package com.seaboxdata.sdps.common.core.config;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestWrapper extends HttpServletRequestWrapper {
	private final byte[] body;

	public RequestWrapper(HttpServletRequest request) {
		super(request);
		String bodyStr = getBodyString(request);
		body = bodyStr.getBytes(Charset.defaultCharset());
	}

	private String getBodyString(final ServletRequest request) {
		try {
			return inputStream2String(request.getInputStream());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getBodyString() {
		final InputStream inputStream = new ByteArrayInputStream(body);
		return inputStream2String(inputStream);
	}

	private String inputStream2String(InputStream inputStream) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream,
					Charset.defaultCharset()));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			log.error("转换body报错", e);
			throw new RuntimeException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e2) {
					log.error("关闭流失败", e2);
				}
			}
		}
		return sb.toString();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
		return new ServletInputStream() {

			@Override
			public int read() throws IOException {
				return inputStream.read();
			}

			@Override
			public void setReadListener(ReadListener arg0) {

			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public boolean isFinished() {
				return false;
			}
		};
	}
}
