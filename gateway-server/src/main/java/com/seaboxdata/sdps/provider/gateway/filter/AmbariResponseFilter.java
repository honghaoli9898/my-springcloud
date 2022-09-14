package com.seaboxdata.sdps.provider.gateway.filter;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.synchronoss.cloud.nio.multipart.util.IOUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
//@Component
public class AmbariResponseFilter implements GlobalFilter, Ordered {
	@Override
	public int getOrder() {
		return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange,
			GatewayFilterChain chain) {
		
		BodyHandlerFunction bodyHandler = (resp, body) -> Flux
				.from(body)
				.map(dataBuffer -> {
					// 响应信息转换为字符串
					String reqBody = null;
					try {
						// dataBuffer 转换为String
						reqBody = IOUtils.inputStreamAsString(
								dataBuffer.asInputStream(), "UTF-8");
					} catch (IOException e) {
						log.error("读取返回信息报错",e);
					}
					return reqBody;
				})
				.flatMap(orgBody -> {
						String rbody = orgBody;
						HttpHeaders headers = resp.getHeaders();
						if (headers.get("X-Frame-Options") != null) {
							headers.remove("X-Frame-Options");
						}
						return resp
								.writeWith(Flux.just(rbody).map(
										bx -> resp.bufferFactory().wrap(
												bx.getBytes())));
					}).then();

		// 构建响应包装类
		BodyHandlerServerHttpResponseDecorator responseDecorator = new BodyHandlerServerHttpResponseDecorator(
				bodyHandler, exchange.getResponse());
		return chain
                .filter(exchange.mutate().response(responseDecorator).build());
	}
}
