package com.lqr.wechat.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.lqr.wechat.R;
import com.lqr.wechat.SessionService;
import com.lqr.wechat.app.AppConst;
import com.lqr.wechat.constant.Constants;
import com.lqr.wechat.manager.BroadcastManager;
import com.lqr.wechat.mpush.base.BuildConfig;
import com.lqr.wechat.mpush.base.MPush;
import com.lqr.wechat.mpush.base.MyLog;
import com.lqr.wechat.mpush.base.Notifications;
import com.lqr.wechat.netty.service.Session;
import com.lqr.wechat.ui.adapter.CommonFragmentPagerAdapter;
import com.lqr.wechat.ui.base.BaseActivity;
import com.lqr.wechat.ui.base.BaseFragment;
import com.lqr.wechat.ui.fragment.FragmentFactory;
import com.lqr.wechat.ui.presenter.MainAtPresenter;
import com.lqr.wechat.ui.view.IMainAtView;
import com.lqr.wechat.util.PopupWindowUtils;
import com.lqr.wechat.util.UIUtils;
import com.mpush.client.ClientConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class MainActivity extends BaseActivity<IMainAtView, MainAtPresenter> implements ViewPager.OnPageChangeListener, IMainAtView, ActivityCompat.OnRequestPermissionsResultCallback {

    private List<BaseFragment> mFragmentList = new ArrayList<>(4);

    @Bind(R.id.ibAddMenu)
    ImageButton mIbAddMenu;
    @Bind(R.id.vpContent)
    ViewPager mVpContent;

    //底部
    @Bind(R.id.llMessage)
    LinearLayout mLlMessage;
    @Bind(R.id.tvMessageNormal)
    TextView mTvMessageNormal;
    @Bind(R.id.tvMessagePress)
    TextView mTvMessagePress;
    @Bind(R.id.tvMessageTextNormal)
    TextView mTvMessageTextNormal;
    @Bind(R.id.tvMessageTextPress)
    TextView mTvMessageTextPress;
    @Bind(R.id.tvMessageCount)
    public TextView mTvMessageCount;

    @Bind(R.id.llContacts)
    LinearLayout mLlContacts;
    @Bind(R.id.tvContactsNormal)
    TextView mTvContactsNormal;
    @Bind(R.id.tvContactsPress)
    TextView mTvContactsPress;
    @Bind(R.id.tvContactsTextNormal)
    TextView mTvContactsTextNormal;
    @Bind(R.id.tvContactsTextPress)
    TextView mTvContactsTextPress;
    @Bind(R.id.tvContactCount)
    public TextView mTvContactCount;
    @Bind(R.id.tvContactRedDot)
    public TextView mTvContactRedDot;

    @Bind(R.id.llDiscovery)
    LinearLayout mLlDiscovery;
    @Bind(R.id.tvDiscoveryNormal)
    TextView mTvDiscoveryNormal;
    @Bind(R.id.tvDiscoveryPress)
    TextView mTvDiscoveryPress;
    @Bind(R.id.tvDiscoveryTextNormal)
    TextView mTvDiscoveryTextNormal;
    @Bind(R.id.tvDiscoveryTextPress)
    TextView mTvDiscoveryTextPress;
    @Bind(R.id.tvDiscoveryCount)
    public TextView mTvDiscoveryCount;

    @Bind(R.id.llMe)
    LinearLayout mLlMe;
    @Bind(R.id.tvMeNormal)
    TextView mTvMeNormal;
    @Bind(R.id.tvMePress)
    TextView mTvMePress;
    @Bind(R.id.tvMeTextNormal)
    TextView mTvMeTextNormal;
    @Bind(R.id.tvMeTextPress)
    TextView mTvMeTextPress;
    @Bind(R.id.tvMeCount)
    public TextView mTvMeCount;

    @Override
    public void init() {
        registerBR();

        Intent intentOne = new Intent(this, Session.class);
        startService(intentOne);
    }

    @Override
    public void initView() {
        setToolbarTitle(UIUtils.getString(R.string.app_name));
        mIbAddMenu.setVisibility(View.VISIBLE);

        //等待全局数据获取完毕
        showWaitingDialog(UIUtils.getString(R.string.please_wait));

        //默认选中第一个
        setTransparency();
        mTvMessagePress.getBackground().setAlpha(255);
        mTvMessageTextPress.setTextColor(Color.argb(255, 69, 192, 26));

        //设置ViewPager的最大缓存页面
        mVpContent.setOffscreenPageLimit(3);

        mFragmentList.add(FragmentFactory.getInstance().getRecentMessageFragment());
        mFragmentList.add(FragmentFactory.getInstance().getContactsFragment());
        mFragmentList.add(FragmentFactory.getInstance().getDiscoveryFragment());
        mFragmentList.add(FragmentFactory.getInstance().getMeFragment());
        mVpContent.setAdapter(new CommonFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList));
    }

    @Override
    public void initListener() {
        mIbAddMenu.setOnClickListener(v -> {
            //显示或隐藏popupwindow
            View menuView = View.inflate(MainActivity.this, R.layout.menu_main, null);
            PopupWindow popupWindow = PopupWindowUtils.getPopupWindowAtLocation(menuView, getWindow().getDecorView(), Gravity.TOP | Gravity.RIGHT, UIUtils.dip2Px(5), mAppBar.getHeight() + 30);
            menuView.findViewById(R.id.tvCreateGroup).setOnClickListener(v1 -> {
                jumpToActivity(CreateGroupActivity.class);
                popupWindow.dismiss();
            });
            menuView.findViewById(R.id.tvHelpFeedback).setOnClickListener(v1 -> {
                jumpToWebViewActivity(AppConst.WeChatUrl.HELP_FEED_BACK);
                popupWindow.dismiss();
            });
            menuView.findViewById(R.id.tvAddFriend).setOnClickListener(v1 -> {
                jumpToActivity(AddFriendActivity.class);
                popupWindow.dismiss();
            });
            menuView.findViewById(R.id.tvScan).setOnClickListener(v1 -> {
                jumpToActivity(ScanActivity.class);
                popupWindow.dismiss();
            });
        });

        mLlMessage.setOnClickListener(v -> bottomBtnClick(v));
        mLlContacts.setOnClickListener(v -> bottomBtnClick(v));
        mLlDiscovery.setOnClickListener(v -> bottomBtnClick(v));
        mLlMe.setOnClickListener(v -> bottomBtnClick(v));
        mVpContent.setOnPageChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBR();
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    public void bottomBtnClick(View view) {
        setTransparency();
        switch (view.getId()) {
            case R.id.llMessage:
                mVpContent.setCurrentItem(0, false);
                mTvMessagePress.getBackground().setAlpha(255);
                mTvMessageTextPress.setTextColor(Color.argb(255, 69, 192, 26));
                break;
            case R.id.llContacts:
                mVpContent.setCurrentItem(1, false);
                mTvContactsPress.getBackground().setAlpha(255);
                mTvContactsTextPress.setTextColor(Color.argb(255, 69, 192, 26));
                break;
            case R.id.llDiscovery:
                mVpContent.setCurrentItem(2, false);
                mTvDiscoveryPress.getBackground().setAlpha(255);
                mTvDiscoveryTextPress.setTextColor(Color.argb(255, 69, 192, 26));
                break;
            case R.id.llMe:
                mVpContent.setCurrentItem(3, false);
                mTvMePress.getBackground().setAlpha(255);
                mTvMeTextPress.setTextColor(Color.argb(255, 69, 192, 26));
                break;
        }
    }

    /**
     * 把press图片、文字全部隐藏(设置透明度)
     */
    private void setTransparency() {
        mTvMessageNormal.getBackground().setAlpha(255);
        mTvContactsNormal.getBackground().setAlpha(255);
        mTvDiscoveryNormal.getBackground().setAlpha(255);
        mTvMeNormal.getBackground().setAlpha(255);
        mTvMessagePress.getBackground().setAlpha(1);
        mTvContactsPress.getBackground().setAlpha(1);
        mTvDiscoveryPress.getBackground().setAlpha(1);
        mTvMePress.getBackground().setAlpha(1);
        mTvMessageTextNormal.setTextColor(Color.argb(255, 153, 153, 153));
        mTvContactsTextNormal.setTextColor(Color.argb(255, 153, 153, 153));
        mTvDiscoveryTextNormal.setTextColor(Color.argb(255, 153, 153, 153));
        mTvMeTextNormal.setTextColor(Color.argb(255, 153, 153, 153));
        mTvMessageTextPress.setTextColor(Color.argb(0, 69, 192, 26));
        mTvContactsTextPress.setTextColor(Color.argb(0, 69, 192, 26));
        mTvDiscoveryTextPress.setTextColor(Color.argb(0, 69, 192, 26));
        mTvMeTextPress.setTextColor(Color.argb(0, 69, 192, 26));
    }

    @Override
    protected MainAtPresenter createPresenter() {
        return new MainAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean isToolbarCanBack() {
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //根据ViewPager滑动位置更改透明度
        int diaphaneity_one = (int) (255 * positionOffset);
        int diaphaneity_two = (int) (255 * (1 - positionOffset));
        switch (position) {
            case 0:
                mTvMessageNormal.getBackground().setAlpha(diaphaneity_one);
                mTvMessagePress.getBackground().setAlpha(diaphaneity_two);
                mTvContactsNormal.getBackground().setAlpha(diaphaneity_two);
                mTvContactsPress.getBackground().setAlpha(diaphaneity_one);
                mTvMessageTextNormal.setTextColor(Color.argb(diaphaneity_one, 153, 153, 153));
                mTvMessageTextPress.setTextColor(Color.argb(diaphaneity_two, 69, 192, 26));
                mTvContactsTextNormal.setTextColor(Color.argb(diaphaneity_two, 153, 153, 153));
                mTvContactsTextPress.setTextColor(Color.argb(diaphaneity_one, 69, 192, 26));
                break;
            case 1:
                mTvContactsNormal.getBackground().setAlpha(diaphaneity_one);
                mTvContactsPress.getBackground().setAlpha(diaphaneity_two);
                mTvDiscoveryNormal.getBackground().setAlpha(diaphaneity_two);
                mTvDiscoveryPress.getBackground().setAlpha(diaphaneity_one);
                mTvContactsTextNormal.setTextColor(Color.argb(diaphaneity_one, 153, 153, 153));
                mTvContactsTextPress.setTextColor(Color.argb(diaphaneity_two, 69, 192, 26));
                mTvDiscoveryTextNormal.setTextColor(Color.argb(diaphaneity_two, 153, 153, 153));
                mTvDiscoveryTextPress.setTextColor(Color.argb(diaphaneity_one, 69, 192, 26));
                break;
            case 2:
                mTvDiscoveryNormal.getBackground().setAlpha(diaphaneity_one);
                mTvDiscoveryPress.getBackground().setAlpha(diaphaneity_two);
                mTvMeNormal.getBackground().setAlpha(diaphaneity_two);
                mTvMePress.getBackground().setAlpha(diaphaneity_one);
                mTvDiscoveryTextNormal.setTextColor(Color.argb(diaphaneity_one, 153, 153, 153));
                mTvDiscoveryTextPress.setTextColor(Color.argb(diaphaneity_two, 69, 192, 26));
                mTvMeTextNormal.setTextColor(Color.argb(diaphaneity_two, 153, 153, 153));
                mTvMeTextPress.setTextColor(Color.argb(diaphaneity_one, 69, 192, 26));
                break;
        }
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 1) {
            //如果是“通讯录”页被选中，则显示快速导航条
            FragmentFactory.getInstance().getContactsFragment().showQuickIndexBar(true);
        } else {
            FragmentFactory.getInstance().getContactsFragment().showQuickIndexBar(false);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state != ViewPager.SCROLL_STATE_IDLE) {
            //滚动过程中隐藏快速导航条
            FragmentFactory.getInstance().getContactsFragment().showQuickIndexBar(false);
        } else {
            FragmentFactory.getInstance().getContactsFragment().showQuickIndexBar(true);
        }
    }

    private void registerBR() {
        BroadcastManager.getInstance(this).register(AppConst.FETCH_COMPLETE, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                hideWaitingDialog();
            }
        });
    }

    private void unRegisterBR() {
        BroadcastManager.getInstance(this).unregister(AppConst.FETCH_COMPLETE);
    }

    @Override
    public TextView getTvMessageCount() {
        return mTvMessageCount;
    }


    private SessionService mSessionService;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSessionService = SessionService.Stub.asInterface(service);
            Log.v("org.weishe.weichat", "获取  SessionService！");

            try {
                mSessionService.getFriendList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSessionService = null;
        }

    };

    public SessionService getSessionService() {
        return mSessionService;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(Constants.INTENT_SERVICE_SESSION);
        intent.setAction(Constants.INTENT_SERVICE_SESSION);
        intent.setPackage("com.lqr.wechat");
        this.bindService(intent, connection, Context.BIND_AUTO_CREATE);

//        Intent intentOne = new Intent(this, Session.class);
//        startService(intentOne);

        Notifications.I.init(this.getApplicationContext());
        Notifications.I.setSmallIcon(R.mipmap.ic_launcher);
        Notifications.I.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        SharedPreferences sp = this.getSharedPreferences("mpush.cfg", Context.MODE_PRIVATE);
        String alloc = sp.getString("allotServer", null);
        requestReadPhonePermission();

        bindUser();
        startPush();
    }

    /**
     * 执行权限
     */
    private void requestReadPhonePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            //在这里面处理需要权限的代码
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }
    }

    /**
     * 绑定用户
     *
     */
    public void bindUser() {
        String userId = "userId-0";
        if (!TextUtils.isEmpty(userId)) {
            MPush.I.bindAccount(userId, "mpush:" + (int) (Math.random() * 10));
        }
    }

    /**
     * 连接mpush
     *
     */
    public void startPush() {
        String allocServer = "http://192.168.1.100:9999";

        if (TextUtils.isEmpty(allocServer)) {
            Toast.makeText(this, "请填写正确的alloc地址", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!allocServer.startsWith("http://")) {
            allocServer = "http://" + allocServer;
        }

        String userId = "userId-0";

        initPush(allocServer, userId);

        MPush.I.checkInit(this.getApplication()).startPush();
        Toast.makeText(this, "start push", Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化服务
     *
     * @param allocServer
     * @param userId
     */
    private void initPush(String allocServer, String userId) {
        //公钥有服务端提供和私钥对应
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";

        ClientConfig cc = ClientConfig.build()
                .setPublicKey(publicKey)
                .setAllotServer(allocServer)
                .setDeviceId(getDeviceId())
                .setClientVersion(BuildConfig.VERSION_NAME)
                .setLogger(new MyLog(this, (EditText) findViewById(R.id.log)))
                .setLogEnabled(BuildConfig.DEBUG)
                .setEnableHttpProxy(true)
                .setUserId(userId);
        MPush.I.checkInit(getApplicationContext()).setClientConfig(cc);
    }

    /**
     * 设备id
     *
     * @return
     */
    private String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Activity.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            String time = Long.toString((System.currentTimeMillis() / (1000 * 60 * 60)));
            deviceId = time + time;
        }
        return deviceId;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }
}
