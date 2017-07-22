package com.alibaba.middleware.race.rpc.network;

import com.alibaba.middleware.race.rpc.api.impl.RpcProviderImpl;
import com.alibaba.middleware.race.rpc.network.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.CancellationException;


/**
 * Created by lee on 7/20/17.
 */
public class NettyServer implements RpcServer, Runnable {
    private RpcProviderImpl provider;
    private int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;
    private ServerBootstrap serverBootstrap;
    private ChannelFuture serverFuture;


    public NettyServer(RpcProviderImpl provider, int port) {
        this.provider = provider;
        this.port = port;
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        /*
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception{
                        //ch.pipeline().addLast(serverHandlerTmp);
                        ch.pipeline().addLast(serverHandler);
                    }
                });
                */
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ServerInitializer(this.provider));
    }
    public void start() throws Throwable{
        System.out.println("start listening: " + this.port);
        serverFuture = serverBootstrap.bind(port).sync();
        serverFuture.channel().closeFuture().sync();    // 等待关闭
        System.out.println("stop listening");
    }

    public void stop() throws Throwable {
        if (this.serverFuture != null
                && (this.serverFuture.channel().isOpen() || !this.serverFuture
                .isCancelled())) {
            try {
                this.serverFuture.channel().disconnect();
                this.serverFuture.channel().close();
                this.serverFuture.cancel(true);
            } catch (CancellationException e) {
                throw new Throwable("Stop server occur a exception!");
            }
        }
    }
    public void run(){
        try {
            start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }
}

class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private RpcProviderImpl provider;
    public ServerInitializer(RpcProviderImpl provider) {
        this.provider = provider;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception{
        ch.pipeline().addLast(new ServerHandler(this.provider));

    }
}
