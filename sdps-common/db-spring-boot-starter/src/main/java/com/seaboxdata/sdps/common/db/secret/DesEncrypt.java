package com.seaboxdata.sdps.common.db.secret;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.DES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;

public class DesEncrypt {
	public static final String DEFAULT_KEY = "rePOqDi8vJs=";

	public static String encrypt(String content, String key) {
		try {
			DES des = new DES(new Base64Decoder().decode(key));
			return des.encryptBase64(content);
		} catch (Exception e) {
		}
		return null;
	}

	public static String decrypt(String content, String key) {
		try {
			DES des = new DES(new Base64Decoder().decode(key));
			return des.decryptStr(content);
		} catch (Exception e) {
		}
		return null;
	}

	public static String generateKey() {
		try {
			byte[] key = SecureUtil.generateKey(
					SymmetricAlgorithm.DES.getValue()).getEncoded();
			return new Base64Encoder().encode(key);
		} catch (Exception e) {
		}
		return null;
	}
}
