package com.seaboxdata.sdps.common.core.utils;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

public class SmUtils {
	private static final String PUBLIC_KEY = "publicKey";
	private static final String PRIVATE_KEY = "privateKey";
	public static final String ENCRYPT_TYPE = "SM2";

	public static Map<String, String> generateKeyPair() {
		try {
			KeyPair pair = SecureUtil.generateKeyPair(ENCRYPT_TYPE);
			PrivateKey privateKey = pair.getPrivate();
			PublicKey publicKey = pair.getPublic();
			byte[] pubEncBytes = publicKey.getEncoded();
			byte[] priEncBytes = privateKey.getEncoded();
			String pubEncBase64 = new Base64Encoder().encode(pubEncBytes);
			String priEncBase64 = new Base64Encoder().encode(priEncBytes);
			Map<String, String> map = MapUtil.newHashMap();
			map.put(PUBLIC_KEY, pubEncBase64);
			map.put(PRIVATE_KEY, priEncBase64);
			return map;
		} catch (Exception e) {

		}
		return null;
	}

	public static String encrypt(String body, PublicKey publicKey) {
		SM2 sm2 = SmUtil.sm2();
		sm2.setPublicKey(publicKey);
		return sm2.encryptBcd(body, KeyType.PublicKey);
	}

	public static String decrypt(String body, PrivateKey privateKey) {
		SM2 sm2 = SmUtil.sm2();
		sm2.setPrivateKey(privateKey);
		return StrUtil.utf8Str(sm2.decryptFromBcd(body, KeyType.PrivateKey));
	}

	public static String encryptStr(String body, String publicKey) {
		SM2 sm2 = SmUtil.sm2(null, publicKey);
		return sm2.encryptBcd(body, KeyType.PublicKey);
	}

	public static String decryptStr(String body, String privateKey) {
		SM2 sm2 = SmUtil.sm2(privateKey, null);
		return StrUtil.utf8Str(sm2.decryptFromBcd(body, KeyType.PrivateKey));
	}

	public static String encryptForSM4(String body) {
		SymmetricCrypto sm4 = SmUtil.sm4();
		return sm4.encryptBase64(body, CharsetUtil.UTF_8);
	}

	public static String decryptForSM4(String body) {
		SymmetricCrypto sm4 = SmUtil.sm4();
		return sm4.decryptStr(body, CharsetUtil.CHARSET_UTF_8);
	}
}
