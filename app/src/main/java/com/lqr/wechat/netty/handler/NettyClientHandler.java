package com.lqr.wechat.netty.handler;


import com.lqr.wechat.netty.bean.Msg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by longhuasshen on 17/10/29.
 */

public class NettyClientHandler extends SimpleChannelInboundHandler<Msg.Message> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Msg.Message message) throws Exception {

    }
}
