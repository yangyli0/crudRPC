package com.alibaba.middleware.race.rpc.network.handler;

import com.alibaba.middleware.race.rpc.model.RpcResponse;
import com.alibaba.middleware.race.rpc.serialization.JavaSerialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by lee on 7/20/17.
 */

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private RpcResponse response = null;
    private volatile Channel clientChannel = null;
    private int timeout;
    private BlockingQueue<RpcResponse> resQueue;

    public ClientHandler(int timeout, BlockingQueue<RpcResponse> resQueue) {
        this.timeout = timeout;
        this.resQueue = resQueue;
    }


    /*
    public RpcResponse getReponse() throws Throwable{
        while (this.reponse == null) {

        }
        synchronized (clientChannel) {
            long start = System.currentTimeMillis();
            clientChannel.wait(this.timeout);
            if (System.currentTimeMillis() - start > timeout) {
                System.out.println("Timeout Exception");
                throw new RuntimeException("Time out");
            }
        }
        return this.reponse;
    }
    */

    public RpcResponse getReponse() throws Throwable {
        this.response =  resQueue.poll(3*this.timeout, TimeUnit.MICROSECONDS); // TODO: debug
        return response;
    }


    public Channel getChannel() {
        return this.clientChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        clientChannel = ctx.channel();
        System.out.println("channel connected.");
    }

    /*
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("get response from server");
        ByteBuf buf = (ByteBuf)msg;
        byte[] recvBytes = new byte[buf.readableBytes()];    //  TODO:直接用bytebuf反序列化
        buf.readBytes(recvBytes);
        RpcResponse response = null;
        this.reponse = (RpcResponse) JavaSerialization.decode(recvBytes);   // 获取响应
        synchronized (this.clientChannel) { //TODO: 做什么用
            clientChannel.notifyAll();
        }

    }
    */

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("get response from server");
        ByteBuf recvBuf = (ByteBuf)msg;
        byte[] recvBytes = new byte[recvBuf.readableBytes()];
        recvBuf.readBytes(recvBytes);
        //this.response = (RpcResponse) JavaSerialization.decode(recvBytes);
        RpcResponse res = (RpcResponse) JavaSerialization.decode(recvBytes);
        resQueue.offer(res);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        super.exceptionCaught(ctx, cause);
        this.response.setErrorMsg(cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
