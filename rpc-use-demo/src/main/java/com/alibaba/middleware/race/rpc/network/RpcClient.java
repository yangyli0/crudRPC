package com.alibaba.middleware.race.rpc.network;

import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.alibaba.middleware.race.rpc.model.RpcResponse;

/**
 * Created by lee on 7/20/17.
 */
public interface RpcClient {
    boolean connect(String sip, int port) throws  Throwable; // 发起连接
    boolean sendRequest(RpcRequest request)throws Throwable;
    boolean close() throws Throwable;
    RpcResponse getResponse() throws Throwable;
    //boolean isConnected();
}
