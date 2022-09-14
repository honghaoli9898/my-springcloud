package com.seaboxdata.sdps.licenseclient.common;

public class ServerResponse<T>{

    //每一个微服务模块最好定义一个自己的错误代码静态类，并且从200000开始编码，0表示正确
    public static final ServerResponse<Object> HYSTRIX_ERROR = new ServerResponse<>(100001, "服务熔断");
    public static final ServerResponse<Object> INTERNAL_ERROR = new ServerResponse<>(500, "服务内部错误");
    public static final ServerResponse<Object> SUCCESS_RESPONSE = new ServerResponse<>();
    public static final ServerResponse<Object> SIGNATURE_ERROR = new ServerResponse<>(100002, "签名错误");

    private int result;
    private int errcode;
    private String errmsg;
    private String detailErrMsg; //详细错误信息
    private T data;

    //可以用在zipkin界面检索服务链路
    private String hint; //实际上是traceid

    public ServerResponse() {

    }

    public ServerResponse(int errCode, String msg, String detailErrMsg) {
        this.errcode = errCode;
        this.errmsg = msg;
        this.detailErrMsg = detailErrMsg;
    }

    public ServerResponse(int errCode, String msg) {
        this(errCode, msg, "");
    }

    public ServerResponse(T data) {
        result = 0;
        errcode = 0;
        errmsg = "";
        setData(data);
    }

    public boolean success() {
        return errcode == 0;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getDetailErrMsg() {
        return detailErrMsg;
    }

    public void setDetailErrMsg(String detailErrMsg) {
        this.detailErrMsg = detailErrMsg;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

}
