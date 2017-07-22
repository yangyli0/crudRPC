package com.alibaba.middleware.race.rpc.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lee on 7/20/17.
 */
public class RpcContext {
    private static Map<String, Object> props = new HashMap<>();

    public static void addProp(String key, Object val) {
        props.put(key, val);
    }
    public static Object getProp(String key) {
        return props.get(key);
    }
    public static Map<String, Object> getContext() {
        return Collections.unmodifiableMap(props);
    }

    public static Map<String, Object> getProps() { return Collections.unmodifiableMap(props); }

}
