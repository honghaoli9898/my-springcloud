package com.seaboxdata.sdps.common.core.constant;

import cn.hutool.core.util.IdUtil;

/**
 * Security 权限常量
 *
 * @author zlt
 *         <p>
 *         Blog: https://zlt2000.gitee.io Github: https://github.com/zlt2000
 */
public interface SecurityConstants {
	String SSO_PASSWORD = "sso".concat(IdUtil.fastSimpleUUID());
	
	String KDC_KEYTAB_PATH = "/kdc";
	
	String USER_SYNC_KEYTAB_PATH = "/usersync";
	
	String ITEM_KEYTAB_PATH = "/item";
	
	String SEABOX_KEYTAB_PATH = "/seabox";
	
	String SDPS_USER_PRINCIPAL_TYPE = "SDPS";
	
	String ACCESS_BAK = "access_bak:";

	String USER_NAME = "username";

	String LOGIN_KEY = "login:user:";

	String ALREADY_LOGIN = "alreadyLogin";

	String LIMIT_KEY = "login:limit:";

	Long LIMIT_TIME = 300L;

	Long LIMIT_COUNT = 5L;
	/**
	 * 用户信息分隔符
	 */
	String USER_SPLIT = ":";

	/**
	 * 用户信息头
	 */
	String USER_HEADER = "x-user-header";

	/**
	 * 用户id信息头
	 */
	String USER_ID_HEADER = "x-userid-header";

	/**
	 * 角色信息头
	 */
	String ROLE_HEADER = "x-role-header";

	/**
	 * 租户信息头(应用)
	 */
	String TENANT_HEADER = "x-tenant-header";

	/**
	 * 账号类型信息头
	 */
	String ACCOUNT_TYPE_HEADER = "x-account-type-header";

	/**
	 * 基础角色
	 */
	String BASE_ROLE = "ROLE_USER";

	/**
	 * 授权码模式
	 */
	String AUTHORIZATION_CODE = "authorization_code";

	/**
	 * 密码模式
	 */
	String PASSWORD = "password";

	/**
	 * 刷新token
	 */
	String REFRESH_TOKEN = "refresh_token";

	/**
	 * oauth token
	 */
	String OAUTH_TOKEN_URL = "/oauth/token";

	/**
	 * 默认的处理验证码的url前缀
	 */
	String DEFAULT_VALIDATE_CODE_URL_PREFIX = "/validata/code";

	/**
	 * 重置密码处理验证码url前缀
	 */
	String RESET_PASSWORD_VALIDATE_CODE_URL_PREFIX = "/validata/resetPassCode";

	/**
	 * 手机号的处理验证码的url前缀
	 */
	String MOBILE_VALIDATE_CODE_URL_PREFIX = "/validata/smsCode";

	/**
	 * 默认生成图形验证码宽度
	 */
	String DEFAULT_IMAGE_WIDTH = "100";

	/**
	 * 默认生成图像验证码高度
	 */
	String DEFAULT_IMAGE_HEIGHT = "35";

	/**
	 * 默认生成图形验证码长度
	 */
	String DEFAULT_IMAGE_LENGTH = "4";

	/**
	 * 默认生成图形验证码过期时间
	 */
	int DEFAULT_IMAGE_EXPIRE = 60;

	/**
	 * 边框颜色，合法值： r,g,b (and optional alpha) 或者 white,black,blue.
	 */
	String DEFAULT_COLOR_FONT = "blue";

	/**
	 * 图片边框
	 */
	String DEFAULT_IMAGE_BORDER = "no";

	/**
	 * 默认图片间隔
	 */
	String DEFAULT_CHAR_SPACE = "5";

	/**
	 * 默认保存code的前缀
	 */
	String DEFAULT_CODE_KEY = "DEFAULT_CODE_KEY";

	/**
	 * 验证码文字大小
	 */
	String DEFAULT_IMAGE_FONT_SIZE = "30";
	/**
	 * zlt公共前缀
	 */
	String ZLT_PREFIX = "zlt:";
	/**
	 * 缓存client的redis key，这里是hash结构存储
	 */
	String CACHE_CLIENT_KEY = "oauth_client_details";
	/**
	 * OAUTH模式登录处理地址
	 */
	String OAUTH_LOGIN_PRO_URL = "/user/login";
	/**
	 * 获取授权码地址
	 */
	String AUTH_CODE_URL = "/oauth/authorize";
	/**
	 * 登录页面
	 */
	String LOGIN_PAGE = "/login.html";
	/**
	 * 登录失败页面
	 */
	String LOGIN_FAILURE_PAGE = LOGIN_PAGE + "?error";
	/**
	 * 登出URL
	 */
	String LOGOUT_URL = "/oauth/remove/token";
	/**
	 * 默认token过期时间(1小时)
	 */
	Integer ACCESS_TOKEN_VALIDITY_SECONDS = 60 * 60;
	/**
	 * redis中授权token对应的key
	 */
	String REDIS_TOKEN_AUTH = "auth:";
	/**
	 * redis中应用对应的token集合的key
	 */
	String REDIS_CLIENT_ID_TO_ACCESS = "client_id_to_access:";
	/**
	 * redis中用户名对应的token集合的key
	 */
	String REDIS_UNAME_TO_ACCESS = "uname_to_access:";
	/**
	 * rsa公钥
	 */
	String RSA_PUBLIC_KEY = "pubkey.txt";
	/**
	 * 获取id_token的response_type
	 */
	String ID_TOKEN = "id_token";

	/**
	 * 令牌颁发者
	 */
	String ISS = "http://seaboxdata.sdps.cn";

	/**
	 * 默认账号类型
	 */
	String DEF_ACCOUNT_TYPE = "admin";

	/**
	 * 账号类型参数名
	 */
	String ACCOUNT_TYPE_PARAM_NAME = "account_type";

	/**
	 * 密码正则
	 */
	String PASS_REG = "^(?![0-9]+$)(?![^0-9]+$)(?![a-zA-Z]+$)(?![^a-zA-Z]+$)(?![a-zA-Z0-9]+$)[a-zA-Z0-9\\S]+$";
}
