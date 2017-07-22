package com.alibaba.middleware.race.rpc.api.impl;

import com.alibaba.middleware.race.rpc.api.RpcProvider;
import com.alibaba.middleware.race.rpc.conf.Configure;
import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.alibaba.middleware.race.rpc.model.RpcResponse;
import com.alibaba.middleware.race.rpc.network.NettyServer;
import com.alibaba.middleware.race.rpc.network.handler.ServerHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by lee on 7/21/17.
 */
public class RpcProviderImpl extends RpcProvider {
    private String version;
    private int timeout;
    private ConcurrentMap<String, Method> serviceList = new ConcurrentHashMap<>();
    private Object serviceInstance = null;
    private Class<?> serviceType = null;
    private NettyServer server = null;



    @Override
    public RpcProvider serviceInterface(Class<?> serviceInstance) {
        if (serviceInstance == null)
            throw new IllegalArgumentException("serviceInstance is null");
        if (!serviceInstance.isInterface())
            throw new IllegalArgumentException("serviceInstance is not an interface");

        Method[] methods = serviceInstance.getMethods();
        System.out.println("all provided methods: ");
        for (Method method: methods) {
            serviceList.put(method.getName(), method);
            System.out.println(method.getName());
        }
        serviceType = serviceInstance;
        return super.serviceInterface(serviceInstance);
    }

    @Override
    public RpcProvider version(String version) {
        this.version = version;
        return super.version(version);
    }
    @Override
    public RpcProvider timeout(int timeout) {
        this.timeout = timeout;
        return super.timeout(timeout);
    }
    @Override
    public RpcProvider serializeType(String serializeType) {
        return super.serializeType(serializeType);
    }

    @Override
    public RpcProvider impl(Object serviceInstance) {
        if (serviceInstance == null)
            throw new IllegalArgumentException("service instance is null");
        if (!serviceType.isInstance(serviceInstance))
            throw new IllegalArgumentException("service is not an instance of service type");

        this.serviceInstance = serviceInstance;
        return super.impl(serviceInstance);
    }

    @Override
    public void publish() { // 启动服务器
        try {
            ServerHandler handler = new ServerHandler(this);
            this.server = new NettyServer(this, Configure.getConfInstance().getPORT());
            new Thread(server).start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public synchronized RpcResponse requestHandler(RpcRequest request) {
        String methodName = request.getMethodName();
        System.out.println(methodName+ "() is requested");
        RpcResponse  response = RpcResponse.responseFactory(request.getRequestId(), null, null);
        if (serviceList.containsKey(methodName)) {
            Method method = serviceList.get(methodName);
            try {
                Object result = method.invoke(this.serviceInstance, request.getArgs()); // 获取调用结果
                response.setAppResponse(result);
            } catch (IllegalAccessException e) {
                System.out.println("IllegalAccessException");
                response.setErrorMsg(e.getMessage());
            } catch (InvocationTargetException e) {
                System.out.println("InvocationTargetException");
                response.setErrorMsg(e.getMessage());
            }
        }
        else {
            response.setAppResponse(null);
            response.setErrorMsg("requested method is not exist!");
        }
        return response;
    }


}















