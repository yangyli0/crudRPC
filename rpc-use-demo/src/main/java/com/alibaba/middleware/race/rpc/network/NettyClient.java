package com.alibaba.middleware.race.rpc.network;

import com.alibaba.middleware.race.rpc.context.RpcContext;
import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.alibaba.middleware.race.rpc.model.RpcResponse;
import com.alibaba.middleware.race.rpc.network.handler.ClientHandler;
import com.alibaba.middleware.race.rpc.serialization.JavaSerialization;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by lee on 7/20/17.
 */

public class NettyClient extends ChannelInboundHandlerAdapter implements RpcClient{
    private EventLoopGroup workGroup;
    private Bootstrap bootstrap;
    private int timeout;
    private volatile Channel clientChannel = null;
    private volatile  RpcResponse response; // 需要对所有线程立即可见(隐含workGroup里的线程)
    private volatile boolean isInit;

    public NettyClient(int timeout) {
        this.timeout = timeout;
        workGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        response = null;
        isInit = false;
    }

    /*
    public void init() {
        if (isInit) return;
        if (workGroup == null || bootstrap == null) return;
        bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.timeout)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(this);
                    }
                });
        this.isInit = true;
    }
    */

    public void init() {
        if (isInit) return;
        if (workGroup == null || bootstrap == null) return;
        bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.timeout)
                .handler(new ClientInitializer(this));
        this.isInit = true;
    }


    public boolean connect(String sip, int port) throws Throwable{
        if (!isInit)  init();
        bootstrap.connect(sip, port).sync();
        return true;
    }
    public boolean sendRequest(RpcRequest request) throws Throwable{
        boolean isSend = false;
        long start = System.currentTimeMillis();
        while (this.clientChannel == null) {
            if (System.currentTimeMillis() - start > timeout) {
                System.out.println("Timeout Exception");
                throw new RuntimeException("Timeout");
            }
        }
        if (this.clientChannel.isActive()) {
            // 放入context;
            request.setContext(RpcContext.getContext());
            byte[] requestBytes = JavaSerialization.encode(request);
            ByteBuf buf = Unpooled.copiedBuffer(requestBytes.clone());
            this.clientChannel.writeAndFlush(buf);
            isSend = true;
        }
        return isSend;
    }


    public RpcResponse getResponse() throws Throwable {
        synchronized (this.clientChannel) {    //
            long startTime = System.currentTimeMillis();
            this.clientChannel.wait(this.timeout);    // 与notifyAll()搭配
            if (System.currentTimeMillis() - startTime > this.timeout) {
                throw new RuntimeException("Time out exception");
            }
            return this.response;
        }
    }

    public boolean close() throws Throwable {
        if (clientChannel != null && clientChannel.isActive()) {
            clientChannel.disconnect();
            clientChannel.close();
            synchronized (clientChannel) {
                clientChannel.notifyAll();
            }
        }
        workGroup.shutdownGracefully();
        return true;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clientChannel = ctx.channel();
    }
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("get response from server");
        ByteBuf recvBuf = (ByteBuf)msg;
        byte[] recvBytes = new byte[recvBuf.readableBytes()];
        recvBuf.readBytes(recvBytes);
        this.response = (RpcResponse)JavaSerialization.decode(recvBytes);
        synchronized (this.clientChannel) {
            clientChannel.notifyAll();
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        this.response.setErrorMsg(cause.getMessage());
        ctx.close();
    }

}

class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private ChannelHandler handler;
    public ClientInitializer(ChannelHandler handler) {
        this.handler = handler;
    }
    @Override
    public void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(this.handler);
    }
}







