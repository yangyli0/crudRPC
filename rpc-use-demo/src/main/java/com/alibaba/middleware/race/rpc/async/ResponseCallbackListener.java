package com.alibaba.middleware.race.rpc.async;

/**
 * Created by lee on 7/21/17.
 */
public interface ResponseCallbackListener {
    void onResponse(Object response);
    void onTimeout();
    void onException(Exception e);

}
