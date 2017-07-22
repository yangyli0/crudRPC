package com.alibaba.middleware.race.rpc.demo.builder;

import com.alibaba.middleware.race.rpc.api.RpcProvider;
import com.alibaba.middleware.race.rpc.demo.service.RaceTestService;
import com.alibaba.middleware.race.rpc.demo.service.RaceTestServiceImpl;

/**
 * Created by huangsheng.hs on 2015/3/26.
 */
public class ProviderBuilder {
    public static void buildProvider(){
        publish();
    }

    private static void publish() {
        RpcProvider rpcProvider = null;
        try {
            rpcProvider = (RpcProvider) getProviderImplClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if(rpcProvider == null){
            System.out.println("Start Rpc Provider failed.");
            System.exit(1);
        }

        rpcProvider.serviceInterface(RaceTestService.class) // 将RaceTestService接口作为服务
                .impl(new RaceTestServiceImpl())    // 委托对象为 new RaceTestServiceImpl()
                .version("1.0.0.api")
                .timeout(3000)
                .serializeType("java").publish();

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private static Class<?> getProviderImplClass(){ // 返回提供服务的类名
        try {
            return Class.forName("com.alibaba.middleware.race.rpc.api.impl.RpcProviderImpl");
        } catch (ClassNotFoundException e) {
            System.out.println("Cannot found the class which must exist and override all RpcProvider's methods");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
