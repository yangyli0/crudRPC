package com.alibaba.middleware.race.rpc.conf;

/**
 * Created by lee on 7/19/17.
 */
public class Configure {
    private final int PORT;
    private final String SIP;
    private Configure() {
        this.SIP = System.getProperty("SIP", "127.0.0.1");
        this.PORT = Integer.parseInt(System.getProperty("PORT", "9999"));
    }


    //　配置对象单例

    private static class SingletonHolder {
        private static final Configure CONF_INSTANCE = new Configure();
    }
    public static Configure getConfInstance() {
        return SingletonHolder.CONF_INSTANCE;
    }

    public int getPORT() { return PORT; }
    public String getSIP() { return SIP; }

}
