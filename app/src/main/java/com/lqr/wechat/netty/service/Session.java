package com.lqr.wechat.netty.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.lqr.wechat.constant.Constants;
import com.lqr.wechat.model.Friends;
import com.lqr.wechat.model.User;
import com.lqr.wechat.model.cache.CacheManager;
import com.lqr.wechat.netty.bean.ChatMessage;
import com.lqr.wechat.netty.bean.ClientRequestMessage;
import com.lqr.wechat.netty.bean.Msg;
import com.lqr.wechat.netty.bean.MsgHelper;
import com.lqr.wechat.netty.handler.FileHandler;
import com.lqr.wechat.netty.handler.JsonDataHandler;
import com.lqr.wechat.netty.handler.ListMessageHandler;
import com.lqr.wechat.netty.handler.NettyClientHandler;
import com.lqr.wechat.util.BeepManager;
import com.lqr.wechat.util.BroadcastHelper;
import com.lqr.wechat.util.LogUtils;
import com.lqr.wechat.util.NetWorkUtil;

import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import com.lqr.wechat.SessionService;
import com.lqr.wechat.util.StringUtils;

/**
 * Created by longhuasshen on 17/11/2.
 */

public class Session extends Service {

    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    private String serverIp;

    private int serverPort;

    private User user;

    private String token;

    private SocketChannel socketChannel;

    private List<Friends> friends;

    private boolean onInternet;

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    private Bootstrap bootstrap;

    private NettyClientHandler nettyClientHandler;

    private JsonDataHandler jsonDataHandler;

    private ListMessageHandler listMessageHandler;

    private FileHandler fileHandler;

    private boolean firStart = true;

    public boolean reStarting = false;


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getAction();
            switch (key) {
                case CONNECTIVITY_CHANGE_ACTION:
                    if (NetWorkUtil.isNetworkAvailable(Session.this)) {
                        onInternet = true;
                    } else {
                        onInternet = false;
                    }
                    break;

                case Constants.INTENT_ACTION_REFRESH_FRIENDS_DATA:
                    Msg.Message msg = MsgHelper.newClientRequestMessage(
                            ClientRequestMessage.FRIEND_GROUP_LIST, user.getId(),
                            token, "");
                    socketChannel.writeAndFlush(msg);

                    Msg.Message msg2 = MsgHelper.newClientRequestMessage(
                            ClientRequestMessage.FRIEND_LIST, user.getId(), token,
                            "");
                    socketChannel.writeAndFlush(msg2);
                    break;
                default:
                    break;
            }

        }
    };

    // 消息提示音
    private BeepManager beepManager;

    @Override
    public void onCreate() {
        super.onCreate();
        // 监听网络状态
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_CHANGE_ACTION);
        intentFilter.addAction(Constants.INTENT_ACTION_REFRESH_FRIENDS_DATA);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销监听
        unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Kathy", "onStartCommand - startId = " + startId + ", Thread ID = " + Thread.currentThread().getId());
        this.serverIp = "10.0.2.2";
        this.serverPort = 8888;

        nettyClientHandler = new NettyClientHandler(Session.this);
        jsonDataHandler = new JsonDataHandler(Session.this);
        listMessageHandler = new ListMessageHandler(Session.this);
        fileHandler = new FileHandler(Session.this);

//        this.user = (User) CacheManager.readObject(this,
//                Constants.CACHE_CURRENT_USER);
//        this.token = (String) CacheManager.readObject(this,
//                Constants.CACHE_CURRENT_USER_TOKEN);
//        if (user != null && token != null) {
//            beepManager = new BeepManager(this);
//            // 启动通讯服务
//            connect();
//        }
        connect();
        return Service.START_STICKY;
    }

    /**
     * 发送消息
     *
     * @param msg
     */
    public void sendMessage(Object msg) {
        if (this.socketChannel != null) {
            this.socketChannel.writeAndFlush(msg);
        }
    }

    public class LocalBinder extends SessionService.Stub {

        public Session getService() {
            return Session.this;
        }

        @Override
        public void sendMessage(String uuid, int contentType, String message,
                                int toId, int msgType, String fileGroupName, String filePath)
                throws RemoteException {
            Msg.Message msg = null;

            switch (msgType) {
                case ChatMessage.MSG_TYPE_UU:
                    msg = MsgHelper.newUUChatMessage(uuid, 1, toId,
                            message, "123456", true,
                            StringUtils.getCurrentStringDate(), 0, contentType,
                            fileGroupName, filePath, ChatMessage.STATUS_SEND);
                    break;
                case ChatMessage.MSG_TYPE_UCG:
                    msg = MsgHelper.newUCGChatMessage(uuid, user.getId(), toId,
                            message, token, true,
                            StringUtils.getCurrentStringDate(), 0, contentType,
                            fileGroupName, filePath, ChatMessage.STATUS_SEND);
                    break;
                case ChatMessage.MSG_TYPE_UDG:
                    msg = MsgHelper.newUUChatMessage(uuid, user.getId(), toId,
                            message, token, true,
                            StringUtils.getCurrentStringDate(), 0, contentType,
                            fileGroupName, filePath, ChatMessage.STATUS_SEND);
                    break;
            }
            socketChannel.writeAndFlush(msg);

            BroadcastHelper.onSendChatMessage(Session.this);
        }

        @Override
        public void getFriendList() throws RemoteException {
            Msg.Message msg = MsgHelper.newClientRequestMessage(
                    ClientRequestMessage.FRIEND_LIST, 1, "123456", "");
            socketChannel.writeAndFlush(msg);
        }

        @Override
        public void getMessageList(int fromMessageId) throws RemoteException {
            Msg.Message message = MsgHelper.newClientRequestMessage(
                    ClientRequestMessage.CHAT_MESSAGE_LIST, user.getId(),
                    token, fromMessageId + "");

            socketChannel.writeAndFlush(message);
        }

        @Override
        public int getUserId() throws RemoteException {

            return user.getId();
        }

        @Override
        public String getUserName() throws RemoteException {
            return user.getName();
        }

        @Override
        public void getFriendGroupsList() throws RemoteException {
            Msg.Message msg = MsgHelper.newClientRequestMessage(
                    ClientRequestMessage.FRIEND_GROUP_LIST, user.getId(),
                    token, "");
            socketChannel.writeAndFlush(msg);
        }

        @Override
        public String getToken() throws RemoteException {
            return token;
        }

        @Override
        public void sendAttachment(long id) throws RemoteException {
//            Attachment a = DBHelper.getgetInstance(Session.this).getAttachment(
//                    id);
//            if (a == null) {
//                return;
//            }
//            Message msg = MsgHelper.newFileUpload(a, user.getId(), token);
//            socketChannel.writeAndFlush(msg);
        }

        @Override
        public void getTodoList(int fromMessageId) throws RemoteException {
            Msg.Message msg1 = MsgHelper.newClientRequestMessage(
                    ClientRequestMessage.TODO_LIST, user.getId(), token,
                    fromMessageId + "");
            socketChannel.writeAndFlush(msg1);
        }

        @Override
        public void getChatGroupList() throws RemoteException {
            Msg.Message msg = MsgHelper.newClientRequestMessage(
                    ClientRequestMessage.CHAT_GROUP_LIST, user.getId(), token,
                    "");
            socketChannel.writeAndFlush(msg);
        }

        @Override
        public void getDiscussionGroupList() throws RemoteException {
            Msg.Message msg = MsgHelper.newClientRequestMessage(
                    ClientRequestMessage.DISCUSSION_GROUP_LIST, user.getId(),
                    token, "");
            socketChannel.writeAndFlush(msg);
        }

        @Override
        public void getChatGroupMemberList(int groupId) throws RemoteException {
            Msg.Message msg = MsgHelper.newClientRequestMessage(
                    ClientRequestMessage.CHAT_GROUP_MEMBER_LIST, user.getId(),
                    token, "" + groupId);
            socketChannel.writeAndFlush(msg);

        }

        @Override
        public void getDiscussionGroupMemberList(int dGroupId)
                throws RemoteException {
            Msg.Message msg = MsgHelper.newClientRequestMessage(
                    ClientRequestMessage.DISCUSSION_GROUP_MEMBER_LIST,
                    user.getId(), token, "" + dGroupId);
            socketChannel.writeAndFlush(msg);

        }

        @Override
        public void getRelateUser() throws RemoteException {
            Msg.Message msg = MsgHelper.newClientRequestMessage(
                    ClientRequestMessage.RELATE_USER_LIST, user.getId(), token,
                    "");
            socketChannel.writeAndFlush(msg);
        }

    }

    /**
     * 利用netty连接远程服务端保持通讯
     */
    private boolean connect() {
        LogUtils.v(">>>>>>>>>>>>>>>>>>>>connect", "ip:" + serverIp + ", port:" + serverPort);
        if (firStart) {
            firStart = false;
        }
        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.group(eventLoopGroup);
        bootstrap.remoteAddress(serverIp, serverPort);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel)
                    throws Exception {

                // socketChannel.pipeline().remove("IdleStateHandler");

                Log.v("org.weishe.weichat", "netty initChannel 调用了！");
                socketChannel.pipeline().addLast("IdleStateHandler",
                        new IdleStateHandler(200, 100, 0));

                // decoded
                socketChannel.pipeline().addLast(
                        "LengthFieldBasedFrameDecoder",
                        new LengthFieldBasedFrameDecoder(10240, 0, 4, 0, 4));
                socketChannel.pipeline().addLast("ProtobufDecoder",
                        new ProtobufDecoder(Msg.Message.getDefaultInstance()));
                // encoded
                socketChannel.pipeline().addLast("LengthFieldPrepender",
                        new LengthFieldPrepender(4));
                socketChannel.pipeline().addLast("ProtobufEncoder",
                        new ProtobufEncoder());

                socketChannel.pipeline().addLast("ClassResolvers",
                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                socketChannel.pipeline().addLast(nettyClientHandler);
                socketChannel.pipeline().addLast(jsonDataHandler);
                socketChannel.pipeline().addLast(listMessageHandler);
                socketChannel.pipeline().addLast(fileHandler);

            }
        });
        LogUtils.v(">>>>>>>>>>>>>>>>>>>>bootstrap" + bootstrap);

        try {
            ChannelFuture future = bootstrap.connect(serverIp, serverPort)
                    .sync();
            if (future.isSuccess()) {
                socketChannel = (SocketChannel) future.channel();
                // 登录认证
                auth();
                Log.v("org.weishe.weichat", "netty 通讯服务启动成功!");
                return true;
            } else {
                Log.v("org.weishe.weichat", "netty 通讯服务启动失败!");
                return false;
            }
            // future.channel().closeFuture().sync();
        } catch (Exception e) {
            Log.v("org.weishe.weichat", "netty 通讯服务启动失败!");
//            Log.v("org.weishe.weichat", e.getMessage());
            e.printStackTrace();
            return false;
        }

    }

    public boolean isReStarting() {
        return reStarting;
    }

    public boolean reConnect() {
        reStarting = true;
        socketChannel.pipeline().remove(IdleStateHandler.class);
        socketChannel.pipeline().remove(NettyClientHandler.class);
        ChannelFuture future = socketChannel.close();
        // future.channel().closeFuture().sync();
        try {
            future.channel().closeFuture().sync();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        if (future.isSuccess()) {
            Log.v("org.weishe.weichat", "netty 成功关闭，准备重启!");
        } else {
            Log.v("org.weishe.weichat", "netty 关闭失败！ 准备重启!");
        }
        while (true) {
            // boolean r = reStart();
            boolean r = connect();
            if (r) {
                reStarting = false;
                return true;
            }
            try {
                Thread.sleep(1000);
                Log.v("org.weishe.weichat", "睡眠一会儿再重启！");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean reStart() {

        ChannelFuture future2 = null;

        try {
            future2 = bootstrap.connect(serverIp, serverPort).sync();
        } catch (Exception e) {
            Log.v("org.weishe.weichat", "netty 通讯服务启动失败!");
            Log.v("org.weishe.weichat", e.getMessage());
            e.printStackTrace();
            return false;
        }

        if (future2 != null && future2.isSuccess()) {
            socketChannel = (SocketChannel) future2.channel();
            // 登录认证
            auth();
            Log.v("org.weishe.weichat", "netty 通讯服务启动成功!");
            return true;
        } else {
            Log.v("org.weishe.weichat", "netty 通讯服务启动失败!");
            return false;
        }

    }

    /**
     * 发送认证消息
     */
    public void auth() {
        Msg.Message loginMsg = Msg.Message
                .newBuilder()
                .setClientLoginMessage(
                        Msg.ClientLoginMessage.newBuilder().setToken("123456")
                                .setUserId(1).build())
                .setMessageType(Msg.MessageType.CLIENT_LOGIN).build();
        socketChannel.writeAndFlush(loginMsg);
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public List<Friends> getFriends() {
        return friends;
    }

    protected void setFriends(List<Friends> friends) {
        this.friends = friends;
    }

    /**
     * 提示响铃
     */
    public void ring() {
        beepManager.playBeepSoundAndVibrate();
    }

    public boolean isOnInternet() {
        return onInternet;
    }
}
