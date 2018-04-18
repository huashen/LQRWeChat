package com.lqr.wechat.netty.handler;


import android.util.Log;

import com.lqr.wechat.app.AppConst;
import com.lqr.wechat.manager.BroadcastManager;
import com.lqr.wechat.netty.bean.ChatMessage;
import com.lqr.wechat.netty.bean.Msg;
import com.lqr.wechat.netty.bean.MsgHelper;
import com.lqr.wechat.netty.service.Session;
import com.lqr.wechat.util.LogUtils;
import com.lqr.wechat.util.UIUtils;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;

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
            case CHAT_MESSAGE:
                Log.v("org.weishe.weichat", "-------------聊天信息---------------");
                Log.v("org.weishe.weichat", "|from:"
                        + message.getChatMessage().getFromId());
                Log.v("org.weishe.weichat", "|to:"
                        + message.getChatMessage().getToId());
                Log.v("org.weishe.weichat", "|token:"
                        + message.getChatMessage().getToken());
                Log.v("org.weishe.weichat", "|content:"
                        + message.getChatMessage().getContent());
                Log.v("org.weishe.weichat", "|from:"
                        + message.getChatMessage().getFromId());
                Log.v("org.weishe.weichat", "---------------------------------");
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setContent(message.getChatMessage().getContent());

                chatMessage.setDate(new Date());
                chatMessage.setFromId(message.getChatMessage().getFromId());
                chatMessage.setToId(message.getChatMessage().getToId());
                chatMessage.setType(ChatMessage.TYPE_RECEIVE);
                chatMessage.setWhoId(message.getChatMessage().getToId());
                chatMessage.setChecked(false);
                chatMessage.setMsgType(message.getChatMessage().getMsgType());
                chatMessage.setChatGroupId(message.getChatMessage()
                        .getChatGroupId());
                chatMessage.setDiscussionGroupId(message.getChatMessage()
                        .getDiscussionGroupId());
                chatMessage.setContentType(message.getChatMessage()
                        .getContentType());
                chatMessage.setFileGroupName(message.getChatMessage()
                        .getFileGroupName());
                chatMessage.setFilePath(message.getChatMessage().getFilePath());
                chatMessage.setChatMessageId(message.getChatMessage()
                        .getChatMessageId());
                chatMessage.setUuid(message.getChatMessage().getUuid());
                chatMessage.setStatus(message.getChatMessage().getStatus());
                //Todo 如果是带附件的消息

                //Todo 保存聊天信息到本地
                LogUtils.v(">>>>>>>>>>>>>>>>>>>CHAT_MESSAGE");
                //发送广播
                Message localMessage = new Message();
                localMessage.setContent(TextMessage.obtain(message.getChatMessage().getContent()));
                localMessage.setConversationType(Conversation.ConversationType.PRIVATE);
                localMessage.setMessageDirection(Message.MessageDirection.RECEIVE);
                localMessage.setSentStatus(Message.SentStatus.SENT);
                BroadcastManager.getInstance(UIUtils.getContext()).sendBroadcast(AppConst.UPDATE_CURRENT_SESSION, localMessage);
                break;
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
                LogUtils.v("=======>receiptMessage", receiptMessage.toString());
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
