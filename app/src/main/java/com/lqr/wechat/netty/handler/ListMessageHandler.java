package com.lqr.wechat.netty.handler;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.lqr.wechat.app.AppConst;
import com.lqr.wechat.db.model.Friend;
import com.lqr.wechat.manager.BroadcastManager;
import com.lqr.wechat.model.cache.UserCache;
import com.lqr.wechat.model.response.UserRelationshipResponse;
import com.lqr.wechat.netty.bean.Friends;
import com.lqr.wechat.netty.bean.Msg;
import com.lqr.wechat.netty.service.Session;
import com.lqr.wechat.util.LogUtils;
import com.lqr.wechat.util.PinyinUtils;
import com.lqr.wechat.util.UIUtils;

import org.litepal.crud.DataSupport;
import org.litepal.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by longhuasshen on 17/11/2.
 */
@ChannelHandler.Sharable
public class ListMessageHandler extends SimpleChannelInboundHandler<List> {

    private Session session;

    public ListMessageHandler(Session session) {
        this.session = session;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, List messages) throws Exception {
        if (messages != null && messages.size() > 0) {
            Object o = messages.get(0);
            //聊天记录
            if (o instanceof Msg.ChatMessage) {
                onReceiveChatMessageList(messages);
            }

            // 好友列表
            if (o instanceof Msg.Friends) {
                onReceiveFriendsList(messages);
            }
        }

        ReferenceCountUtil.release(messages);
    }

    private void onReceiveChatMessageList(List messages) {
        //Todo 保存本地消息

        LogUtils.v("========================>保存本地消息");

        //发送广播
    }


    private void onReceiveFriendsList(List<Msg.Friends> friends) {
        List<Friends> friendsList = new ArrayList<>();
        if (friends != null && friends.size() > 0) {
            for (Msg.Friends f : friends) {
                Friends friend = new Friends();
                friend.setAge(f.getAge());
                friend.setAvatarPath(f.getAvatarPath());
                friend.setFriendsGroupId(f.getFriendsGroupId());
                friend.setGender(f.getGender());
                friend.setId(f.getId());
                friend.setName(f.getName());
                friend.setOnline(f.getOnline());
                friend.setOnlineType(f.getOnlineType());
                friend.setRemarkName(f.getRemarkName());
                friend.setSignature(f.getSignature());
                friend.setUserId(f.getUserId());
                friendsList.add(friend);
            }
        }
        Log.v("ListMessageHandler", "friendsList");
        LogUtils.printList(friendsList);

        if (friendsList != null && friendsList.size() > 0) {
            saveFriends(friendsList);
        }

//        // 将对象写入本地
//        CacheManager.saveObject(session, _fl,
//                Friends.getCacheKey(session.getUser().getId()));
//        // 发出通知
//
//        // 广播事件
        BroadcastManager.getInstance(UIUtils.getContext()).sendBroadcast(AppConst.FETCH_COMPLETE);
        BroadcastManager.getInstance(UIUtils.getContext()).sendBroadcast(AppConst.UPDATE_FRIEND);
        BroadcastManager.getInstance(UIUtils.getContext()).sendBroadcast(AppConst.UPDATE_GROUP);
        BroadcastManager.getInstance(UIUtils.getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);

    }

    public synchronized void saveFriends(List<Friends> list) {
        deleteFriends();
        List<Friend> friends = new ArrayList<>();
        for (Friends friends1 : list) {
                Friend friend = new Friend(
                        String.valueOf(friends1.getId()),
                        friends1.getName(),
                        friends1.getAvatarPath(),
                        TextUtils.isEmpty(friends1.getRemarkName()) ? friends1.getRemarkName() : "龙猫",
                        null, null, null, null,
                        PinyinUtils.getPinyin(friends1.getName()),
                        "longmao"
                );
                friends.add(friend);
        }
        if (friends != null && friends.size() > 0)
            DataSupport.saveAll(friends);
    }

    public synchronized List<Friend> getFriends() {
        return DataSupport.where("userid != ?", UserCache.getId()).find(Friend.class);
    }

    public synchronized void deleteFriends() {
        List<Friend> friends = getFriends();
        for (Friend friend : friends) {
            friend.delete();
        }
    }
}
