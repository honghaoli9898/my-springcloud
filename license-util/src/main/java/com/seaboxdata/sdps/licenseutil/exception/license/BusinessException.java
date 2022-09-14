package com.seaboxdata.sdps.licenseutil.exception.license;



import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * License异常处理类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends BaseBusinessException {


    public BusinessException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
    public BusinessException(String msg, int code) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public static final BusinessException ERR_LICENSE_PRIVATE_CREATE_FAIL = new BusinessException(9001, "license私钥申请失败");
    public static final BusinessException ERR_LICENSE_CERT_CREATE_FAIL = new BusinessException(9002, "cert证书导出失败");
    public static final BusinessException ERR_LICENSE_PUB_CREATE_FAIL = new BusinessException(9003, "license公钥导出失败");
    public static final BusinessException ERR_LICENSE_LIC_CREATE_FAIL = new BusinessException(9004, "license授权文件生成失败");
    public static final BusinessException ERR_LICENSE_ESN_NULL = new BusinessException(9005, "ESN码异常");
    public static final BusinessException ERR_LICENSE_ESN_NO_RECORD = new BusinessException(9006, "ESN码不存在");
    public static final BusinessException ERR_LICENSE_CONFIG_READ = new BusinessException(9007, "License配置文件读取异常");
    public static final BusinessException ERR_LICENSE_CONFIG_ANALYS = new BusinessException(9008, "License配置文件解析异常");
    public static final BusinessException ERR_LICENSE_INSTALL_FAIL = new BusinessException(9009, "license证书安装失败");
    public static final BusinessException ERR_LICENSE_UPLOAD_FAIL = new BusinessException(9010, "license证书更新失败");
    public static final BusinessException ERR_LICENSE_VERIFY_INVALID = new BusinessException(9011, "License无效或已过期");
    public static final BusinessException ERR_LICENSE_VERIFY_FAIL = new BusinessException(9012, "License校验失败");
    public static final BusinessException ERR_LICENSE_CREATE_PARAMS_DEFACT = new BusinessException(9013, "参数异常");
    public static final BusinessException ERR_LICENSE_VERIFY_EXPRIE = new BusinessException(9014, "License已过期");
    public static final BusinessException ERR_LICENSE_NOT_INSTALL = new BusinessException(9015, "License证书未安装");
    public static final BaseBusinessException REST_POST_SERVICE_EXCEPTION = new BusinessException("内部服务POST调用异常！", 10005);
    //未安装license的错误码
    public static final Integer ERR_LICENSE_NOT_INSTALL_CODE = 9015;
    public static final BaseBusinessException REST_GET_SERVICE_EXCEPTION = new BusinessException("内部服务get调用异常！", 10004);

    public static final BusinessException REST_GET_FAIL = new BusinessException("内部服务异常(get)！", 8019);
    public static final BusinessException USER_NOT_LOGIN_ERROR = new BusinessException("用户未登录！", 8001);
    public static final BusinessException REST_GET_USER_ACCESS_FAIL_ERROR = new BusinessException(
            "服务内部获取用户权限信息失败！", 8002);

    public static final BusinessException IS_NOT_ADMIN_ERROR = new BusinessException("管理员接口,普通用户无权限使用！", 8004);
}
