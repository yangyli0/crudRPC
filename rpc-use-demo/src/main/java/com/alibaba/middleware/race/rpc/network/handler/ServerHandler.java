package com.alibaba.middleware.race.rpc.network.handler;


import com.alibaba.middleware.race.rpc.api.impl.RpcProviderImpl;
import com.alibaba.middleware.race.rpc.context.RpcContext;
import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.alibaba.middleware.race.rpc.model.RpcResponse;
import com.alibaba.middleware.race.rpc.serialization.JavaSerialization;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;

/**
 * Created by lee on 7/21/17.
 */

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private RpcProviderImpl provider;
    public ServerHandler(RpcProviderImpl provider) {
        super();
        this.provider = provider;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("receive a request.");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        //从字节流中获取request对象
        ByteBuf recvBuf = (ByteBuf)msg;
        byte[] recvBytes = new byte[recvBuf.readableBytes()];
        recvBuf.readBytes(recvBytes);
        RpcRequest request = (RpcRequest) JavaSerialization.decode(recvBytes);

        // 获取client中的context
        Map<String, Object> props = request.getContext();
        if (props != null) {
            for (Map.Entry<String, Object> entry: props.entrySet())
                RpcContext.addProp(entry.getKey(), entry.getValue());
        }

        // 获取response
        RpcResponse response = this.provider.requestHandler(request);   // 交给handler处理，获取response
        byte[] responsBytes = JavaSerialization.encode(response);
        ByteBuf sendBuf = Unpooled.copiedBuffer(responsBytes);
        ctx.write(sendBuf);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws  Exception {
        ctx.flush();
    }
}


















