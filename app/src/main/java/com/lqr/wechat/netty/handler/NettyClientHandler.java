package com.lqr.wechat.netty.handler;


import android.util.Log;

import com.lqr.wechat.app.AppConst;
import com.lqr.wechat.manager.BroadcastManager;
import com.lqr.wechat.netty.bean.Msg;
import com.lqr.wechat.netty.bean.MsgHelper;
import com.lqr.wechat.netty.service.Session;
import com.lqr.wechat.util.LogUtils;

import java.net.InetSocketAddress;
import java.util.List;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by longhuasshen on 17/10/29.
 */

@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<Msg.Message> {

    private int pingTimes = 0;

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

        switch (message.getMessageType()) {
            case FRIENDS_LIST:
                List<Msg.Friends> fl = message.getFriendsListMessageList();
                // 转发给FriendsListHandler处理
                if (fl == null || fl.size() < 1) {
//                    CacheManager.saveObject(session, null,
//                            org.weishe.weichat.bean.Friends.getCacheKey(session
//                                    .getUser().getId()));
//                    // 广播事件
//                    Intent intent1 = new Intent();
//                    intent1.setAction(Constants.INTENT_ACTION_RECEIVE_FRIEND_GROUP_LIST);
//                    session.sendBroadcast(intent1);
                } else {
                    channelHandlerContext.fireChannelRead(fl);
                }
                break;
            case RECEIPT:
                // 消息回执
                Msg.ReceiptMessage receiptMessage = message.getReceiptMessage();
//                // 更新本地消息状态
//                DBHelper.getgetInstance(session).updateChatMessageStatus(
//                        rm.getUuid(), rm.getStatus());
//                // 通知UI组件更新界面
//                BroadcastHelper.onReceiveReceiptMessage(session);
                break;
            default:
                break;
        }

        ReferenceCountUtil.release(message);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtils.v(">>>>>>>>发生异常了");
        super.exceptionCaught(ctx, cause);
    }


    /**
     * 当出现WRITER_IDLE、ALL_IDLE时则需要向客户端发出ping消息，试探远端是否还在
     */
    private void sendPing(ChannelHandlerContext ctx) {
        // Session session = SessionManager.get(ctx.channel());

        Msg.Message ping = MsgHelper.newPingMessage(Msg.MessageType.CLIENT_PING,
                1 + "123456");
        ctx.channel().writeAndFlush(ping);
        Log.v("org.weiche.weichat", "pingTimes：" + pingTimes);
        Log.v("org.weiche.weichat", "session" + session);
        Log.v("org.weiche.weichat", "this" + this);

        if (pingTimes > 5 && !session.isReStarting()) {
            boolean r = session.reConnect();
            if (r) {
                pingTimes = 0;
            }
            Log.v("org.weiche.weichat", "session" + session);
            Log.v("org.weiche.weichat", "this" + this);
            Log.v("org.weiche.weichat", "重连结果：" + r);
        }

        pingTimes++;

    }

    // 利用写空闲发送心跳检测消息
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:
                    Log.v("org.weishe.weichat", "服务器客户端：WRITER_IDLE");
                    sendPing(ctx);
                    Log.v("org.weishe.weichat", "服务器客户端 send ping to server。");
                    break;
                default:
                    break;
            }
        }
    }
}
