package com.alibaba.middleware.race.rpc.network;

/**
 * Created by lee on 7/20/17.
 */
public interface RpcServer {
    void start() throws Throwable;
    void stop() throws Throwable;
}
