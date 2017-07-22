package com.alibaba.middleware.race.rpc.model;

import java.io.Serializable;

/**
 * Created by lee on 7/19/17.
 */
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = -225640454370459555L;
    private String errorMsg;    // 用于返回异常
    private Object  appResponse;    // 返回正常调用结果
    private long requestId;

    public synchronized  static
        RpcResponse responseFactory(long requestId, Object appResponse, String errorMsg) {
        return new RpcResponse(requestId, appResponse, errorMsg);
    }
    private RpcResponse(long requestId, Object appResponse, String errorMsg) {
        this.requestId = requestId;
        this.appResponse = appResponse;
        this.errorMsg = errorMsg;
    }

    public Object getAppResponse() { return appResponse; }
    public String getErrorMsg() { return errorMsg; }
    public boolean isError() { return errorMsg != null ? true : false; }

    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public void setAppResponse(Object appResponse) { this.appResponse = appResponse; }



}
