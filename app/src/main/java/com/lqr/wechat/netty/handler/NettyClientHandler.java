package com.lqr.wechat.netty.handler;


import com.lqr.wechat.netty.bean.Msg;
import com.lqr.wechat.netty.service.Session;
import com.lqr.wechat.util.LogUtils;

import java.net.InetSocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by longhuasshen on 17/10/29.
 */

public class NettyClientHandler extends SimpleChannelInboundHandler<Msg.Message> {

    private Session session;

    public NettyClientHandler(Session session) {
        this.session = session;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Msg.Message message) throws Exception {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) channelHandlerContext
                    .channel().remoteAddress();

        String ip = inetSocketAddress.getAddress().getHostAddress();
        int port = inetSocketAddress.getPort();
        LogUtils.v("receive msg from server ip:" + ip + ", port:" + port);
        LogUtils.v("msgType:" + message.getMessageType());


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtils.v(">>>>>>>>发生异常了");
        super.exceptionCaught(ctx, cause);
    }
}
