package com.lqr.wechat.netty.handler;

import com.lqr.wechat.netty.service.Session;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by longhuasshen on 17/11/2.
 */

public class FileHandler extends SimpleChannelInboundHandler {

    private Session session;

    public FileHandler(Session session) {
        this.session = session;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

    }
}
