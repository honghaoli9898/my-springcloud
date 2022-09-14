package com.seaboxdata.sdps.common.captcha.model.common;

import com.seaboxdata.sdps.common.captcha.util.StringUtils;

import java.io.Serializable;

public class ResponseModel implements Serializable {

    private static final long serialVersionUID = 8445617032523881407L;

    private String            code;

    private String            msg;

    private Object            data;

    public ResponseModel() {
        this.code = RepCodeEnum.SUCCESS.getCode();
    }

    public ResponseModel(RepCodeEnum repCodeEnum) {
       this.setCodeEnum(repCodeEnum);
    }

    //成功
    public static ResponseModel success(){
        return ResponseModel.successMsg("成功");
    }
    public static ResponseModel successMsg(String message){
        ResponseModel responseModel = new ResponseModel();
        responseModel.setMsg(message);
        return responseModel;
    }
    public static ResponseModel successData(Object data){
        ResponseModel responseModel = new ResponseModel();
        responseModel.setCode(RepCodeEnum.SUCCESS.getCode());
        responseModel.setData(data);
        return responseModel;
    }

    //失败
    public static ResponseModel errorMsg(RepCodeEnum message){
        ResponseModel responseModel = new ResponseModel();
        responseModel.setCodeEnum(message);
        return responseModel;
    }
    public static ResponseModel errorMsg(String message){
        ResponseModel responseModel = new ResponseModel();
        responseModel.setCode(RepCodeEnum.ERROR.getCode());
        responseModel.setMsg(message);
        return responseModel;
    }
    public static ResponseModel errorMsg(RepCodeEnum repCodeEnum, String message){
        ResponseModel responseModel = new ResponseModel();
        responseModel.setCode(repCodeEnum.getCode());
        responseModel.setMsg(message);
        return responseModel;
    }
    public static ResponseModel exceptionMsg(String message){
        ResponseModel responseModel = new ResponseModel();
        responseModel.setCode(RepCodeEnum.EXCEPTION.getCode());
        responseModel.setMsg(RepCodeEnum.EXCEPTION.getDesc() + ": " + message);
        return responseModel;
    }

	@Override
	public String toString() {
		return "ResponseModel{" + "code='" + code + '\'' + ", msg='"
				+ msg + '\'' + ", data=" + data + '}';
	}

	public boolean isSuccess(){
        return StringUtils.equals(code, RepCodeEnum.SUCCESS.getCode());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public void setCodeEnum(RepCodeEnum repCodeEnum) {
        this.code=repCodeEnum.getCode();
        this.msg=repCodeEnum.getDesc();
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }


}
