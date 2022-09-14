package com.seaboxdata.sdps.eureka.secret;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;

public class RsaUtils {
	public static final String ENCRYPT_TYPE = "RSA";
	private static final String PUBLIC_KEY = "RSAPublicKey";
	private static final String PRIVATE_KEY = "RSAPrivateKey";

	public static String encrypt(String content, PublicKey pubicKey) {
		try {
			RSA rsa = new RSA(null, pubicKey);
			return rsa.encryptBase64(content, KeyType.PublicKey);
		} catch (Exception e) {
		}
		return null;
	}

	public static String encrypt(String content, String pubicKey) {
		try {
			RSA rsa = new RSA(null, pubicKey);
			return rsa.encryptBase64(content, KeyType.PublicKey);
		} catch (Exception e) {
		}
		return null;
	}

	public static String encryptPrivate(String content, String privateKey) {
		try {
			RSA rsa = new RSA(privateKey, null);
			return rsa.encryptBase64(content, KeyType.PrivateKey);
		} catch (Exception e) {
		}
		return null;
	}

	public static String encryptPrivate(String content, PrivateKey privateKey) {
		try {
			RSA rsa = new RSA(privateKey, null);
			return rsa.encryptBase64(content, KeyType.PrivateKey);
		} catch (Exception e) {
		}
		return null;
	}

	public static String decrypt(String content, PrivateKey privateKey) {
		try {
			RSA rsa = new RSA(privateKey, null);
			return rsa.decryptStr(content, KeyType.PrivateKey);
		} catch (Exception e) {
		}
		return null;
	}

	public static String decryptPublic(String content, PublicKey publicKey) {
		try {
			RSA rsa = new RSA(null, publicKey);
			return rsa.decryptStr(content, KeyType.PublicKey);
		} catch (Exception e) {
		}
		return null;
	}

	public static String decryptPublic(String content, String publicKey) {
		try {
			RSA rsa = new RSA(null, publicKey);
			return rsa.decryptStr(content, KeyType.PublicKey);
		} catch (Exception e) {
		}
		return null;
	}

	public static String decryptPrivate(String content, String privateKey) {
		try {
			RSA rsa = new RSA(privateKey, null);
			return rsa.decryptStr(content, KeyType.PrivateKey);
		} catch (Exception e) {
		}
		return null;
	}

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
			map.put(PRIVATE_KEY, priEncBase64);
			map.put(PUBLIC_KEY, pubEncBase64);
			return map;
		} catch (Exception e) {
		}
		return null;
	}
}
