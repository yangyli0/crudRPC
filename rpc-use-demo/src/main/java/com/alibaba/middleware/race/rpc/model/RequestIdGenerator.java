package com.alibaba.middleware.race.rpc.model;

/**
 * Created by lee on 7/20/17.
 */
public class RequestIdGenerator {
    private static volatile long requestId = 0L;

    public static synchronized long getRequestId() {
        requestId = (requestId + 1) % Long.MAX_VALUE;
        return requestId;
    }
}
