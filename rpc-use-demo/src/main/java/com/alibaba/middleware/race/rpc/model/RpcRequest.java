package com.alibaba.middleware.race.rpc.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by lee on 7/19/17.
 */
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 3050395738507191119L;  // 序列化需要声明
    private long requestId;
    private String  className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] args;
    private Map<String, Object> context;    // rpc context

    // TODO: 需要用工厂方法吗？
    public synchronized static RpcRequest requestFactory(long requestId, String className,
                   String methodName, Class<?>[] parameterTypes, Object[] args) {
        return new RpcRequest(requestId, className, methodName, parameterTypes, args);
    }


    private RpcRequest(long requestId, String className, String methodName,
                      Class<?>[] parameterTypes, Object[] args) {
        this.requestId  = requestId;
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.args = args;
    }

    public long getRequestId() { return requestId; }
    public String getMethodName() { return methodName; }
    public Object[] getArgs() { return args; }
    public Map<String, Object> getContext() { return context; }

    public void setContext(Map<String, Object> context) { this.context = context;}







}
