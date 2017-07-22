package com.alibaba.middleware.race.rpc.aop;

import com.alibaba.middleware.race.rpc.model.RpcRequest;

/**
 * Created by lee on 7/21/17.
 */
public interface ProviderHook {
    void before(RpcRequest request);
    void after(RpcRequest request);
}
