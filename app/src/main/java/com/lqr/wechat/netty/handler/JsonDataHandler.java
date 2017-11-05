package com.lqr.wechat.netty.handler;

import com.lqr.wechat.netty.service.Session;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by longhuasshen on 17/11/2.
 */
@ChannelHandler.Sharable
public class JsonDataHandler extends SimpleChannelInboundHandler {

    private Session session;

    public JsonDataHandler(Session session) {
        this.session = session;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

    }
}
