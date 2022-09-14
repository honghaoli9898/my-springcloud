package com.seaboxdata.sdps.provider.gateway.config;

import cn.hutool.core.util.StrUtil;
import com.seaboxdata.sdps.provider.gateway.constant.LoginServerConstants;
import com.seaboxdata.sdps.provider.gateway.feign.MenuService;
import com.seaboxdata.sdps.provider.gateway.utils.ServerLoginUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyPipeline;

import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;

@Component
@Slf4j
public class EncodeQueryNettyWebServerCustomizer implements
		WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
	@Autowired
	private MenuService menuService;

	@Override
	public void customize(NettyReactiveWebServerFactory factory) {
		factory.addServerCustomizers(httpServer -> httpServer.observe((conn,
				state) -> {
			if (state == ConnectionObserver.State.CONNECTED) {
				conn.channel()
						.pipeline()
						.addAfter(NettyPipeline.HttpCodec, "",
								new QueryHandler());
			}
		}));
	}

	class QueryHandler extends ChannelInboundHandlerAdapter {

		public QueryHandler() {
		}

		private String handleUrl(String clusterId, String url, String serverType) {
			switch (serverType) {
			case "A":
				url = StrUtil.replace(url,
						LoginServerConstants.ambari_url_path,
						LoginServerConstants.ambari_url_path + "-" + clusterId);
				break;
			case "S":
				url = StrUtil.replace(url, LoginServerConstants.sdo_url_path,
						LoginServerConstants.sdo_url_path + "-" + clusterId);
				break;
			case "G":
				url = StrUtil
						.replace(url, LoginServerConstants.grafana_url_path,
								LoginServerConstants.grafana_url_path + "-"
										+ clusterId);
				break;
			case "L":
				url = StrUtil.replace(url,
						LoginServerConstants.logsearch_url_path,
						LoginServerConstants.logsearch_url_path + "-"
								+ clusterId);
				break;
			case "SHBASE":
				url = StrUtil
						.replace(url, LoginServerConstants.s_hbase_url_path,
								LoginServerConstants.s_hbase_url_path + "-"
										+ clusterId);
				break;
			case "SFL2":
				url = StrUtil.replace(url, LoginServerConstants.sfl2_url_path,
						LoginServerConstants.sfl2_url_path + "-" + clusterId);
				break;
			case "R":
				url = StrUtil.replace(url, LoginServerConstants.ssm_url_path,
						LoginServerConstants.ssm_url_path + "-" + clusterId);
				break;
			case "SLOG2":
				url = StrUtil.replace(url, LoginServerConstants.slog2_url_path,
						LoginServerConstants.slog2_url_path + "-" + clusterId);
				break;
			case "D":
				url = StrUtil.replace(url,
						LoginServerConstants.sredis_url_path,
						LoginServerConstants.sredis_url_path + "-" + clusterId);
				break;
			case "SEA_OSS":
				url = StrUtil
						.replace(url, LoginServerConstants.SEA_OSS_URL_PATH,
								LoginServerConstants.SEA_OSS_URL_PATH + "-"
										+ clusterId);
				break;
			case "SDT2":
				url = StrUtil.replace(url, LoginServerConstants.SDT2_URL_PATH,
						LoginServerConstants.SDT2_URL_PATH + "-" + clusterId);
				break;
			case "SKAFKA":
				url = StrUtil.replace(url,
						LoginServerConstants.SKAFKA_URL_PATH,
						LoginServerConstants.SKAFKA_URL_PATH + "-" + clusterId);
				break;
			case "SCS":
				url = StrUtil.replace(url, LoginServerConstants.SCS_URL_PATH,
						LoginServerConstants.SCS_URL_PATH + "-" + clusterId);
				break;
			case "SMS":
				url = StrUtil.replace(url, LoginServerConstants.SMS_URL_PATH,
						LoginServerConstants.SMS_URL_PATH + "-" + clusterId);
				break;
			default:
				break;
			}

			return url;
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws UnsupportedEncodingException {
			if (msg instanceof HttpRequest) {
				HttpRequest request = (HttpRequest) msg;
				String clusterId = request.headers().get(
						LoginServerConstants.cluster_id);
				String serverType = request.headers().get(
						LoginServerConstants.server_type);
				String url = request.uri();
				if (StrUtil.isNotBlank(clusterId)
						&& StrUtil.isNotBlank(serverType)) {
					url = handleUrl(clusterId, url, serverType);
				}
				log.info("url: {}", url);
				String[] split = url.split("\\?");
				StringBuilder fixUrl = new StringBuilder(split[0]);
				if (split.length > 1) {
					fixUrl.append("?");
					String chars = split[1].toString();
					for (Entry<String, String> entry : ServerLoginUtil.charMap
							.entrySet()) {
						chars = StrUtil.replace(chars, entry.getKey(),
								entry.getValue());
					}
					fixUrl.append(chars);
				}
				log.info("fixUrl: {}", fixUrl.toString());
				request.setUri(fixUrl.toString());
			}
			ctx.fireChannelRead(msg);
		}

	}
}