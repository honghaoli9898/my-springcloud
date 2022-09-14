package com.seaboxdata.sdps.common.core.utils;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RsaUtil {

	/**
	 * 类型
	 */
	public static final String ENCRYPT_TYPE = "RSA";

	/**
	 * 获取公钥的key
	 */
	private static final String PUBLIC_KEY = "RSAPublicKey";

	/**
	 * 获取私钥的key
	 */
	private static final String PRIVATE_KEY = "RSAPrivateKey";

	/**
	 * 公钥加密
	 *
	 * @param content
	 *            要加密的内容
	 * @param publicKey
	 *            公钥
	 */
	public static String encrypt(String content, PublicKey publicKey) {
		try {
			RSA rsa = new RSA(null, publicKey);
			return rsa.encryptBase64(content, KeyType.PublicKey);
		} catch (Exception e) {
			log.error("加密报错", e);
		}
		return null;
	}

	/**
	 * 公钥加密
	 *
	 * @param content
	 *            要加密的内容
	 * @param publicKey
	 *            公钥
	 */
	public static String encrypt(String content, String publicKey) {
		try {
			RSA rsa = new RSA(null, publicKey);
			return rsa.encryptBase64(content, KeyType.PublicKey);
		} catch (Exception e) {
			log.error("加密报错", e);
		}
		return null;
	}

	/**
	 * 私钥解密
	 *
	 * @param content
	 *            要解密的内容
	 * @param privateKey
	 *            私钥
	 */
	public static String decrypt(String content, PrivateKey privateKey) {
		try {
			RSA rsa = new RSA(privateKey, null);
			return rsa.decryptStr(content, KeyType.PrivateKey);
		} catch (Exception e) {
			log.error("解密报错", e);
		}
		return null;
	}

	/**
	 * 私钥解密
	 *
	 * @param content
	 *            要解密的内容
	 * @param privateKey
	 *            私钥
	 */
	public static String decrypt(String content, String privateKey) {
		try {
			RSA rsa = new RSA(privateKey, null);
			return rsa.decryptStr(content, KeyType.PrivateKey);
		} catch (Exception e) {
			log.error("解密报错", e);
		}
		return null;
	}

	public static void main(String[] args) {
		String pass = "Admin@951753$";
		String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC/mRNOzpnSlJRNd28+OCIrY/mTmhTnzFFG4KffcFhLQDs9gZuyXT5WVEGcqQCluDYpiigVKKytDA5o+nt167WkQ+Dezr+u33+U5xcvS+BA0uIWKO3p3v+eO08MAvLwA3iTge5gSMkL+AShMcztrW4XOZU+U0J0kGmvk1isPlzsiQIDAQAB";
		String privateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAL+ZE07OmdKUlE13bz44Iitj+ZOaFOfMUUbgp99wWEtAOz2Bm7JdPlZUQZypAKW4NimKKBUorK0MDmj6e3XrtaRD4N7Ov67ff5TnFy9L4EDS4hYo7ene/547TwwC8vADeJOB7mBIyQv4BKExzO2tbhc5lT5TQnSQaa+TWKw+XOyJAgMBAAECgYAE0ax0BhuFmcXYpHVEdPw8B+fUO/MEaQXO0PH7+1sHKoKK7557SB/cI0Kitnf9ExLurMpJDPS55gSbRJiUHNV81PEqWmKS1XSVtDGOvApwYZxmR9VL1qnbYy87V9dJFIP5Iy15CFMbWK1KeIFRqL0TZTT40KcYqKCfDf84DvbgYQJBAOGeMLBihI/ja+HRS0EPCBmcrQz/uY/UjvpUOLRpZhj1rIMZUr8dXdyDUaxMQoLc4CytHZeTxsrR2RdO6q6gRqECQQDZZhobCUlKKuYtUgQFC0daeO4mRAGvci5d1BeSOuOnmww+vgkB1dGW2pF/0+zU609F2LDp+hHPuNm/ORiPziTpAkAFt9mqDsUnVG8+iOxsWLIu2/6yzqRoVc3N4GaTg/xXc5qMtA6Z02e2mAlw4XnqESkJWNXgKpmmOE1VPuXJyTwhAkBj6RegS0FCc7q7I1EznD5bpu4T6fc4UWpbtshqbOQJOCmFgEKVdFXRVXu+2n+iTs5s1CxiK4oaA+MWjb/q/xtZAkA6UOGJNL2AIpLyxMEfhJZAZVQKaw2ta8RlMIywX0jG+3k5ztgqpz5er1KoePLhknmr44Uohjy611mqlyB7b4cW";
		String test = "Rz14u5b7Id6lrv4+hT+Od9d1AQP1dBsoOel9PWtzVHtTOQCqjHUDtzNQ5cR+leparmiDt9WRKuR96Uu8pH3T/BeAdCON5pL4/33ibHuao1PAoxnqF7v0FCQU4szr46E67U2ENe5QBHLQC0vOpGWj9ozUt3+eBUtGz720A6Xn9yQ=";
		String test1 = "hfsbTCSFBBxy05er/X8MIXmxqhgKg3mkq6GCUQ2GDa1KcsThp4rNBGY3QMBbmQb3Tc4WwP/735INBqGq8jf3/x8GjH1+WHF9sjFX8iu/SiyOy22+C8XYvAKeGPqy2CdL+57YRbsLX838X4oFZUPa2aIrG18nLUhuNYmAKtXyCKg=";
		String enc = encrypt(pass, publicKey);
		System.out.println("加密后的值:");
		System.out.println(enc);
		System.out.println("解密后的值:");
		System.out.println(decrypt(enc, privateKey));
		System.out.println("=================");
		System.out.println(decrypt(test, privateKey));
		System.out.println(decrypt(test1, privateKey));
		// Map<String, String> keyPair = generateKeyPair(1024);
		// System.out.println(keyPair.toString());
	}

	/**
	 * 获取公私钥-请获取一次后保存公私钥使用
	 *
	 * @return
	 */
	public static Map<String, String> generateKeyPair(int size) {
		try {
			KeyPair pair = SecureUtil.generateKeyPair(ENCRYPT_TYPE, size);
			PrivateKey privateKey = pair.getPrivate();
			PublicKey publicKey = pair.getPublic();
			// 获取 公钥和私钥 的 编码格式（通过该 编码格式 可以反过来 生成公钥和私钥对象）
			byte[] pubEncBytes = publicKey.getEncoded();
			byte[] priEncBytes = privateKey.getEncoded();

			new Base64Encoder();
			// 把 公钥和私钥 的 编码格式 转换为 Base64文本 方便保存
			String pubEncBase64 = Base64Encoder.encode(pubEncBytes);
			String priEncBase64 = Base64Encoder.encode(priEncBytes);

			Map<String, String> map = new HashMap<String, String>(2);
			map.put(PUBLIC_KEY, pubEncBase64);
			map.put(PRIVATE_KEY, priEncBase64);

			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}



}
