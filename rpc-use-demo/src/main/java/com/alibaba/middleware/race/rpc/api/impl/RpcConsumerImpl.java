package com.alibaba.middleware.race.rpc.api.impl;

import com.alibaba.middleware.race.rpc.aop.ConsumerHook;
import com.alibaba.middleware.race.rpc.api.RpcConsumer;
import com.alibaba.middleware.race.rpc.async.ResponseCallbackListener;
import com.alibaba.middleware.race.rpc.async.ResponseFuture;
import com.alibaba.middleware.race.rpc.conf.Configure;
import com.alibaba.middleware.race.rpc.model.RequestIdGenerator;
import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.alibaba.middleware.race.rpc.model.RpcResponse;
import com.alibaba.middleware.race.rpc.network.NettyClient;
import com.alibaba.middleware.race.rpc.network.RpcClient;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * Created by lee on 7/21/17.
 */
public class RpcConsumerImpl extends RpcConsumer {
    private String version;
    private int timeout;
    private ConcurrentMap<String, Method>  serviceList = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Future<Object>> taskList = new ConcurrentHashMap<>();
    private ConsumerHook hook;

    @Override
    public RpcConsumer interfaceClass(Class<?> interfaceClazz) {
        if (interfaceClazz == null)
            throw new IllegalArgumentException("interface clazz is null");
        if (!interfaceClazz.isInterface())
            throw new IllegalArgumentException(interfaceClazz.getName()+" is not an interface");
        Method[] methods = interfaceClazz.getMethods();
        for (Method method: methods) {
            serviceList.put(method.getName(), method);
            System.out.println(method.getName());
        }
        return super.interfaceClass(interfaceClazz);
    }

    @Override
    public RpcConsumer version(String version) {
        this.version = version;
        return super.version(version);
    }
    @Override
    public RpcConsumer clientTimeout(int timeout) {
        this.timeout = timeout;
        return super.clientTimeout(timeout);
    }

    @Override
    public RpcConsumer hook(ConsumerHook hook) {
        this.hook = hook;
        return super.hook(hook);
    }

    @Override
    public Object instance() {
        return super.instance();
    }   // 生成代理对象

    @Override
    public void asynCall(String methodName) {
        asynCall(methodName, null);
    }
    @Override
    public <T extends ResponseCallbackListener> void asynCall(String methodName, T callbackListener) {
        Method method = serviceList.get(methodName);
        // 被异步调用的方法没有参数
        FutureTask<Object> task = new FutureTask<Object>(new MethodInvocation(this, method, null));
        new Thread(task).start();
        new ResponseFuture().setFuture(task);   // 实际改变的的静态变量futureThreadLocal
        this.taskList.put(methodName, task);
        if (callbackListener != null) {
            try {
                callbackListener.onResponse(ResponseFuture.getResponse(this.timeout));  // 这里等待任务完成
            } catch(InterruptedException e) {
                if (e.getMessage().contains("Time Out"))
                    callbackListener.onTimeout();
                else
                    callbackListener.onException(e);
            }
        }

    }

    @Override
    public void cancelAsyn(String methodName) {
        if (taskList.containsKey(methodName)) {
            Future<Object> future = taskList.get(methodName);
            if (!future.isCancelled()) {
                future.cancel(true);

            }
            taskList.remove(methodName);
        }

    }

    // 这里没有委托对象，invoke()方法内部直接发起远程调用

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // TOdO: requestId 设置的有些随意
        RpcResponse response = RpcResponse.responseFactory(-1, null, null);
        String methodName = method.getName();
        if (!serviceList.containsKey(methodName)) {
            response.setErrorMsg(methodName+"() does not exist in serviceList");
            return response;
        }
        if (taskList.containsKey(methodName))   // 调用任务正在执行
            return null;

        String className = method.getDeclaringClass().getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        RpcRequest request = RpcRequest.requestFactory(RequestIdGenerator.getRequestId(),
                className, methodName, parameterTypes, args);
        NettyClient client = new NettyClient(this.timeout);

        hook.before(request);   // 调用前hook
        Configure conf = Configure.getConfInstance();
        try {
            client.connect(conf.getSIP(), conf.getPORT());
            client.sendRequest(request);
            response = client.getResponse();
            // for debug use;
            System.out.println("hell");
        } catch (Throwable e) {
            response.setErrorMsg(e.getMessage());
        }
        finally {
            client.close();
        }
        if (response.isError()) {
            System.out.println(response.getErrorMsg());
            throw new RuntimeException(response.getErrorMsg());
        }
        hook.after(request);    // 调用后的钩子
        // return response; //bug 所在地
        return response.getAppResponse();

    }

    public ConsumerHook getHook() { return this.hook; }
    public int getTimeout() { return this.timeout; }


}


class MethodInvocation implements Callable<Object> {
    private RpcConsumerImpl consumer;
    private Method method;
    private Object[] args;
    public MethodInvocation (RpcConsumerImpl consumer, Method method, Object[] args) {
        this.consumer = consumer;
        this.method = method;
        this.args = args;
    }
    public Object call() {

        RpcResponse response = RpcResponse.responseFactory(-2, null, null);
        RpcClient client = null;
        try {
            RpcRequest request = RpcRequest.requestFactory(RequestIdGenerator.getRequestId(),
                    this.consumer.getClass().getName(), this.method.getName(), null, this.args);

            this.consumer.getHook().before(request);    // 调用前的钩子
            client = new NettyClient(this.consumer.getTimeout());
            Configure conf = Configure.getConfInstance();

            client.connect(conf.getSIP(), conf.getPORT());
            response  = client.getResponse();

            this.consumer.getHook().after(request); // 调用后钩子
            if (response.isError()) {
                System.out.println(response.getErrorMsg());
                throw new RuntimeException(response.getErrorMsg());
            }


        } catch (Throwable e) {
            response.setErrorMsg(e.getMessage());
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Throwable e) {}
            }
        }
        return response;
    }
}



















