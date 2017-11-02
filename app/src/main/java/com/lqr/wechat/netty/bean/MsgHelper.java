package com.lqr.wechat.netty.bean;

import com.lqr.wechat.netty.bean.Msg.Message;

/**
 * Created by longhuasshen on 17/11/2.
 */

public class MsgHelper {

    /**
     * 生成一个带返回消息的消息
     *
     * @param type
     * @param message
     * @return
     */
    public static Message newResultMessage(Msg.MessageType type, String message) {
        Msg.ResultMessage.Builder builder = Msg.ResultMessage.newBuilder();
        Msg.ResultMessage rtMessage = builder.setMessage(message)
                .setMessageType(type).build();
        Msg.Message.Builder b = Msg.Message.newBuilder();
        Msg.Message m = b.setResultMessage(rtMessage).setMessageType(type)
                .build();
        return m;
    }

    /**
     * 生成一个带ping/pong消息的消息
     *
     * @param type
     * @param clientId
     * @return
     */
    public static Message newPingMessage(Msg.MessageType type, String clientId) {
        Msg.PingMessage.Builder bu = Msg.PingMessage.newBuilder();
        Msg.PingMessage rtMessage = bu.setClientId(clientId)
                .setMessageType(type).build();
        Msg.Message.Builder b = Msg.Message.newBuilder();
        Msg.Message m = b.setPingMessage(rtMessage).setMessageType(type)
                .build();
        return m;
    }


    /**
     * 消息回执
     *
     * @param uuid
     * @param status
     * @param userId
     * @param token
     * @return
     */
    public static Msg.Message newReceiptMessage(String uuid, int status,
                                                int userId, String token) {
        Msg.Message.Builder b = Msg.Message.newBuilder();

        Msg.ReceiptMessage.Builder rb = Msg.ReceiptMessage.newBuilder();
        rb.setStatus(status).setUuid(uuid).setUserId(userId).setToken(token);

        Msg.ReceiptMessage rm = rb.build();

        b.setMessageType(Msg.MessageType.RECEIPT);
        b.setReceiptMessage(rm);
        Msg.Message m = b.build();
        return m;
    }

    /**
     *
     * @param type
     * @param userId
     * @param para
     *            可以为空
     * @return
     */
    public static Message newClientRequestMessage(int type, int userId,
                                                  String token, String para) {
        Msg.ClientRequestMessage.Builder bu = Msg.ClientRequestMessage
                .newBuilder();
        Msg.ClientRequestMessage rtMessage = bu.setUserId(userId)
                .setToken(token).setRequestType(type).setParameter(para)
                .build();
        Msg.Message.Builder b = Msg.Message.newBuilder();
        Msg.Message m = b.setClientRequestMessage(rtMessage)
                .setMessageType(Msg.MessageType.CLIENT_REQUEST).build();
        return m;
    }
}
