package com.seaboxdata.sdps.provider.gateway.dynamic;

import java.util.Map;
import java.util.concurrent.Future;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.redis.template.RedisRepository;
import com.seaboxdata.sdps.provider.gateway.constant.LoginServerConstants;
import com.seaboxdata.sdps.provider.gateway.feign.MenuService;
import com.seaboxdata.sdps.provider.gateway.thread.ServerLoginCallable;

@Slf4j
@Component
public class DynamicLoginScheduling {
	@Autowired
	private MenuService menuService;
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;
	@Autowired
	private RedisRepository redisRepository;

	@Scheduled(cron = "0 */25 * * * ?")
	public void getLoginInfo() {
		try {
			Map<String, Future<Boolean>> futureMap = MapUtil.newHashMap();
			if (!LoginServerConstants.loginServerMap.isEmpty()) {
				LoginServerConstants.loginServerMap
						.forEach((k, v) -> {
							String[] arr = StrUtil.splitToArray(k,
									LoginServerConstants.redis_join);
							String clusterId = arr[0];
							String serverType = arr[1];

							ServerLoginCallable serverLoginCallable = new ServerLoginCallable(
									menuService, redisRepository, clusterId,
									serverType, v);
							futureMap.put(k,
									taskExecutor.submit(serverLoginCallable));
						});
				Long startTime = System.currentTimeMillis();
				while (true) {
					boolean isNotDone = futureMap.entrySet().stream()
							.anyMatch(entry -> !entry.getValue().isDone());
					if (!isNotDone) {
						break;
					}
					Long currTime = System.currentTimeMillis();
					if ((currTime - startTime) > LoginServerConstants.timeout) {
						futureMap.forEach((k, v) -> {
							if (!v.isDone()) {
								v.cancel(true);
							}
						});
						break;
					}
					try {
						Thread.sleep(LoginServerConstants.sleep);
					} catch (InterruptedException e) {
						log.error("休息{}ms报错", LoginServerConstants.sleep);
					}
				}
			}
			Thread.sleep(LoginServerConstants.timeout);
		} catch (Exception e) {
			log.error("更新组件登录失败", e);
		}
	}

	public static void main(String[] args) {
//		String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC/mRNOzpnSlJRNd28+OCIrY/mTmhTnzFFG4KffcFhLQDs9gZuyXT5WVEGcqQCluDYpiigVKKytDA5o+nt167WkQ+Dezr+u33+U5xcvS+BA0uIWKO3p3v+eO08MAvLwA3iTge5gSMkL+AShMcztrW4XOZU+U0J0kGmvk1isPlzsiQIDAQAB";
//		String encry = RsaUtil.encrypt("sdo", publicKey);
//		System.out.println(encry);
//		String privateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAL+ZE07OmdKUlE13bz44Iitj+ZOaFOfMUUbgp99wWEtAOz2Bm7JdPlZUQZypAKW4NimKKBUorK0MDmj6e3XrtaRD4N7Ov67ff5TnFy9L4EDS4hYo7ene/547TwwC8vADeJOB7mBIyQv4BKExzO2tbhc5lT5TQnSQaa+TWKw+XOyJAgMBAAECgYAE0ax0BhuFmcXYpHVEdPw8B+fUO/MEaQXO0PH7+1sHKoKK7557SB/cI0Kitnf9ExLurMpJDPS55gSbRJiUHNV81PEqWmKS1XSVtDGOvApwYZxmR9VL1qnbYy87V9dJFIP5Iy15CFMbWK1KeIFRqL0TZTT40KcYqKCfDf84DvbgYQJBAOGeMLBihI/ja+HRS0EPCBmcrQz/uY/UjvpUOLRpZhj1rIMZUr8dXdyDUaxMQoLc4CytHZeTxsrR2RdO6q6gRqECQQDZZhobCUlKKuYtUgQFC0daeO4mRAGvci5d1BeSOuOnmww+vgkB1dGW2pF/0+zU609F2LDp+hHPuNm/ORiPziTpAkAFt9mqDsUnVG8+iOxsWLIu2/6yzqRoVc3N4GaTg/xXc5qMtA6Z02e2mAlw4XnqESkJWNXgKpmmOE1VPuXJyTwhAkBj6RegS0FCc7q7I1EznD5bpu4T6fc4UWpbtshqbOQJOCmFgEKVdFXRVXu+2n+iTs5s1CxiK4oaA+MWjb/q/xtZAkA6UOGJNL2AIpLyxMEfhJZAZVQKaw2ta8RlMIywX0jG+3k5ztgqpz5er1KoePLhknmr44Uohjy611mqlyB7b4cW";
//		System.out.println(RsaUtil.decrypt(encry, privateKey));

		//spark密码
		String pass = "EmYswvRqThkgHmD6hZpWHusk+Jr7NGm/dfK55y8jXYewvqmIbB7+rwMDVj+ak0bYDlASohI90+7Tmsiyx4AkMHGtW5EGiKzsyuBpxBf9QdC1BJXUHLmCGUMfQx72JYVLzurzaZaLRYCw+JRBxg9sTOZfYGKEJkyGJo1LsLp9Ytc=";
		String sparkPrivateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAL0NBXGS8wdmS/DzDOJsybCXXdn+PLoe06dqNKiZrBVx9zT9LkUq7sWjWbguiQiRLOg1gPvfWrjdKK96eO7ZfqWiP274w+i2Jp3MwOF29cc4ZC7d/Tz75IqkEr+jZ60swIz6hgPgo+ksxpXdoHWzk5zS2xbp6Ro7DxZ1t2Js5PHPAgMBAAECgYA/ryw/Ry5hNr+sSmPzvovu1f7CmZdhZ1MLWYalqSbEDqElE4jLuhu3LkDh/6Mf5K/JBYUO0/3eunmjK1GjvFah5vvK1LFVoAieSuMWQz9M+5CzJQBCRNFctjkBOKW/THPojZaNpOjuV/2U0fxoSJ2zfQTwdPVRQjSvQxSTGH9NOQJBAOW7RSuMrRcxzN8sK5SLZKiCbqwpPYN4PmhjC6ujOFbH/U+XB/owCHle8oE2252nFggK0o0fjmtk3TiVNWFnY3sCQQDSqvEUv4mfF7cz56dCHt7hxmBh13BR0zFNDGruGM4BSlWl9SxmZM/XaRzsrz42Rr2P9cMj6aEPaFexKaoviYC9AkBcVcG+eENV0EFc7d7yTHh/tjUPY2ADCIBk6nJVYTwSvT0WEUk2iLnPNpdX0hnBKtgTEW6BJE7U4Wx9ApO6+5gjAkAhfeGz7buAWnHaw6JYpbXDaRlblLebmprE6At77N+bXuj86Fx1ruNIGMLrcfTPBjIO2AB9DiB35ZvDY59VzWN1AkEAqoPSmyYYpB3rN+d/nvNJeyF1BcDrGalRyiFjW+f0TNewCd5MpIxw4daw4VgXakXLP9YuCVnU+ksJpy2M3ukj4Q==";
		System.out.println(RsaUtil.decrypt(pass, sparkPrivateKey));

	}
}
