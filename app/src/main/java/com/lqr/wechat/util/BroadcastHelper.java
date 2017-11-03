package com.lqr.wechat.util;

import android.content.Context;
import android.content.Intent;

import com.lqr.wechat.constant.Constants;

/**
 * Created by longhuasshen on 17/11/3.
 */

public class BroadcastHelper {

    public static void onSendChatMessage(Context context) {
        Intent intent0 = new Intent();
        intent0.setAction(Constants.INTENT_ACTION_SEND_CHAT_MESSAGE);
        context.sendBroadcast(intent0);
    }


    /**
     * 收到消息
     *
     * @param context
     */
    public static void onReceiveChatMessageList(Context context) {
        Intent intent0 = new Intent();
        intent0.setAction(Constants.INTENT_ACTION_RECEIVE_CHAT_MESSAGE_LIST);
        context.sendBroadcast(intent0);
    }

    /**
     * 收到代办消息
     *
     * @param context
     */
    public static void onReceiveTodoList(Context context) {
        Intent intent0 = new Intent();
        intent0.setAction(Constants.INTENT_ACTION_RECEIVE_CHAT_MESSAGE_LIST);
        context.sendBroadcast(intent0);
    }

    public static void refreshFriendsData(Context context) {
        Intent intent0 = new Intent();
        intent0.setAction(Constants.INTENT_ACTION_REFRESH_FRIENDS_DATA);
        context.sendBroadcast(intent0);

    }

    public static void onReceiveReceiptMessage(Context context) {
        Intent intent0 = new Intent();
        intent0.setAction(Constants.INTENT_ACTION_RECEIVE_RECEIPT_MESSAGE);
        context.sendBroadcast(intent0);
    }

    public static void onVoiceMsgDownload(Context context) {
        Intent intent0 = new Intent();
        intent0.setAction(Constants.INTENT_ACTION_VOICE_MSG_DOWLOAD);
        context.sendBroadcast(intent0);

    }
}
