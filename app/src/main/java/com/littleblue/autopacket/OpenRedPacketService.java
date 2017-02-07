package com.littleblue.autopacket;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OpenRedPacketService extends AccessibilityService {
    private static String TAG = "RedPacket.OpenService";

    private Context mContext;
    private boolean isOnStateChange = false;
    private boolean isOnOpenPacket = false;
    private boolean isOpenInKeyguard = false;
    private HashMap<String, Integer> mNodeHashMap = new HashMap<>();//不能用list
    private static AccessibilityNodeInfo  mNode;
    private int mPacketInScreenY = 0;
    private int mOpendPacketInScreenY = 0;
    private int mResultCount = 0;
    private int mRanking = -1;
    private final int GLOBAL_ACTION_BACK_MSG = 1;
    private final int SHOW_IM_RUN = 2;
    private final int LOCK_KEYGUARD = 3;
    private final ArrayList<String> mWannotGroupList = new ArrayList<>();
    private final ArrayList<String> mWannotManList = new ArrayList<>();
    private int mWannotManIndex = 0;
    private int mWannotOpenIndex = 0;
    private int mAllIndex = 0;
    private String mLastPacketStr = "";
    private boolean isWannotGroupOrMan = false;
    private String mLastResultText = "";
    private PacketResultInfo mPacketResultInfo;
    private static String mNodeId = "0";
    public static String mWeiXinName;
    public static final String ACTION_IS_SERVICE_RUN = "littleBlue.action_is_service_run";
    public static final String ACTION_UPDATE_WANNOT_LIST = "littleBlue.action_update_wannot_list";
    private KeyguardManager mKeyguardManager;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        /*有关AccessibilityEvent事件的回调函数.系统通过sendAccessibiliyEvent()不断的发送AccessibilityEvent到此处
            当用户发生发生变化时,系统会发送一系列的AccessibilityEvent事件,比如按钮被电击时会发送TYPE_VIEW_CLICKED类型的事件.
            方法	说明:
            getEventType()	事件类型
            getSource()	获取事件源对应的结点信息
            getClassName()	获取事件源对应类的类型,比如点击事件是有某个Button产生的,那么此时获取的就是Button的完整类名
            getText()	获取事件源的文本信息,比如事件是有TextView发出的,此时获取的就是TextView的text属性.如果该事件源是树结构,那么此时获取的是这个树上所有具有text属性的值的集合
            isEnabled()	事件源(对应的界面控件)是否处在可用状态
            getItemCount()	如果事件源是树结构,将返回该树根节点下子节点的数量

            系统不断的产生各种事件,有些是界面控件产生的,有些是系统产生的.对于由界面控件的产生的事件,通常我们将该控件称之为事件源.并不是所有的事件都能通过getSource()方法获取到事件源,
            比如像通知消息类型的事件(TYPE_NOTIFICATION_STATE_CHANGED).
            */

        int eventType = event.getEventType();
        logEventType(eventType);//这个屏蔽时，TYPE_WINDOW_CONTENT_CHANGED并不能更新AccessibilityNodeInfo
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> textList = event.getText();
                if (textList.isEmpty()) break;
                for (CharSequence charSequence : textList) {
                    String textString = charSequence.toString();
                    Utils.logI(TAG, "Notification charSequence: " + textString);
                    //如果微信红包的提示信息,则模拟点击进入相应的聊天窗口
                    if (textString.contains("[微信红包]")) {
                        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                            Notification notification = (Notification) event.getParcelableData();
                            PendingIntent pendingIntent = notification.contentIntent;
                            mNodeHashMap.clear();
                            boolean isInKeyguard = mKeyguardManager.isKeyguardLocked();//判断是否为锁屏
                            if (isInKeyguard) {
                                Utils.logI(TAG, "Notification in keyguard");
                                isOpenInKeyguard = true;

                                Intent intent = new Intent(RemoteService.ACTION_DISABLE_KEYGUARD);
                                sendBroadcast(intent);
                                sendNotifiactionIntent(pendingIntent);
                                mHandler.sendEmptyMessageDelayed(LOCK_KEYGUARD, 3000);
                            } else {
                                sendNotifiactionIntent(pendingIntent);
                            }
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                //Utils.logI(TAG, "CONTENT getClassName: " + event.getClassName());
                if (isOnStateChange) break;
                //break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                isOnStateChange = true;
                String className = event.getClassName().toString();
                Utils.logI(TAG, "STATE getClassName: " + className);
                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                    mHandler.removeMessages(GLOBAL_ACTION_BACK_MSG);
                    handleEvents();
                } else if (className.endsWith("android.widget.TextView")) {
                    handleEvents();
                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                    if (isOnOpenPacket()) {
                        mNodeHashMap.put(mNodeId, 0);
                        isOnOpenPacket = true;
                        if (isOpenInKeyguard) {
                            mHandler.sendEmptyMessage(LOCK_KEYGUARD);
                        }
                    } else {
                        if (isOpenInKeyguard) {
                            mHandler.sendEmptyMessage(GLOBAL_ACTION_BACK_MSG);
                            mHandler.sendEmptyMessage(LOCK_KEYGUARD);
                        } else {
                            mHandler.sendEmptyMessageDelayed(GLOBAL_ACTION_BACK_MSG, 2000);
                        }
                    }
                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")) {
                    if (!isOnOpenPacket) break;
                    isOnOpenPacket = false;
                    mRanking = -1;
                    mResultCount = 0;
                    mPacketResultInfo = new PacketResultInfo();
                    mPacketResultInfo.time = System.currentTimeMillis();
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    getPacketResult(nodeInfo);
                    Utils.logI(TAG, "Pakcket result: " + mPacketResultInfo.toString());
                    dealForResult(mPacketResultInfo);
                    mHandler.sendEmptyMessageDelayed(GLOBAL_ACTION_BACK_MSG, 500);
                }
                isOnStateChange = false;
                break;
        }

    }

    private void sendNotifiactionIntent(PendingIntent pendingIntent) {

        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case GLOBAL_ACTION_BACK_MSG:
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    break;
                case SHOW_IM_RUN:
                    Intent intent = new Intent(ACTION_IS_SERVICE_RUN);
                    sendBroadcast(intent);
                    mHandler.sendEmptyMessageDelayed(SHOW_IM_RUN, 5*DateUtils.SECOND_IN_MILLIS);
                    break;
                case LOCK_KEYGUARD:
                    isOpenInKeyguard = false;
                    Intent intent2 = new Intent(RemoteService.ACTION_REENABLE_KEYGUARD);
                    sendBroadcast(intent2);
                    break;
            }
        }
    };

    private void handleEvents() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
//                        List<AccessibilityNodeInfo> nodeInfos = rootNode.findAccessibilityNodeInfosByText("领取红包");
//                        AccessibilityNodeInfo lastNode = nodeInfos.get(nodeInfos.size() - 1);
//                        Utils.logI(TAG, "findAccessibilityNodeInfosByText, lastNode: " + lastNode);
//                        AccessibilityNodeInfo actionNode = lastNode.getParent();
//                        Utils.logI(TAG, "node.getParent()actionNode: " + actionNode);
            //actionNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            //以上方法判断领取红包可能会造成失效

            mNode = null;
            mPacketInScreenY = 0;
            mOpendPacketInScreenY = 0;
            mWannotManIndex = 0;
            mAllIndex = 0;
            mWannotOpenIndex = 0;
            mLastPacketStr = "";
            isWannotGroupOrMan = false;
            findRedPacket(rootNode);

            if (isWannotGroupOrMan) {
                Utils.logI(TAG, " handleEvents isWannotGroupOrMan");
                if (isOpenInKeyguard) {
                    mHandler.sendEmptyMessage(LOCK_KEYGUARD);
                }
                if (mNode != null && mAllIndex - mWannotOpenIndex == 1) {
                    //mWannotNodeInfo.add(mNode);
                    Utils.showToastView(mContext, getString(R.string.is_not_my_wanner_packet), Toast.LENGTH_SHORT);
                }
                return;
            } else if (mNode == null) {
                Utils.logI(TAG, " handleEvents mNode is null");
                return;
            }
            Utils.logI(TAG, " handleEvents mNode isNot null");

            String nodeStr = mNode.toString();
            mNodeId = nodeStr.substring(49, 57);
            if (mNodeHashMap.containsKey(mNodeId) && mNodeHashMap.get(mNodeId) == mPacketInScreenY) {
                Utils.logI(TAG, "handleEvents this node has been added");
                return;
            }
            mNodeHashMap.put(mNodeId, mPacketInScreenY);

            mNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            AccessibilityNodeInfo parent = mNode.getParent();
            //Utils.logI(TAG, "node.getParent(): " + parent);
            while (parent != null) {
                if (parent.isClickable()) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
                parent = parent.getParent();
            }
        }
    }

    /**
     * 递归查找当前聊天窗口中的红包信息
     *
     * 聊天窗口中的红包都存在"领取红包"一词,因此可根据该词查找红包
     *
     * @param node
     */
    private void findRedPacket(AccessibilityNodeInfo node) {

        if (node.getChildCount() == 0) {
            if (node.getText() != null) {
                String nodeText = node.getText().toString();
                Utils.logI(TAG, "findRedPacket: " + nodeText);
                mAllIndex++;
                int index1 = nodeText.indexOf("(");
                if (index1 > 0 && mLastPacketStr.contains("微信") && mWannotGroupList.contains(nodeText.substring(0, index1))) {
                    //不想抢的群
                    Utils.logI(TAG, "findRedPacket WannotGroup: " + nodeText);
                    isWannotGroupOrMan = true;
                } else if (mWannotManList.contains(nodeText)) {
                    if (mLastPacketStr.contains("微信")) {
                        //不想抢的人非群界面
                        Utils.logI(TAG, "findRedPacket not in group WannotMan: " + nodeText);
                        isWannotGroupOrMan = true;
                    } else {
                        //不想抢的人在群界面
                        Utils.logI(TAG, "findRedPacket in group WannotMan: " + nodeText);
                        mWannotManIndex = mAllIndex;
                    }
                } else if ("领取红包".equals(nodeText)) {
                    mWannotOpenIndex = mAllIndex;
                    if (mWannotManIndex > 0 && mAllIndex - mWannotManIndex == 2) {
                        isWannotGroupOrMan = true;
                    } else {
                        Rect outRect = new Rect();
                        node.getBoundsInScreen(outRect);
                        if (outRect.top > mPacketInScreenY) {
                            mPacketInScreenY = outRect.top;
                        }
                        Utils.logI(TAG, "findRedPacket packet outRect: " + outRect.toString());
                        mNode = node;
                    }
                } else if(nodeText.contains("你领取了")) {
                    mNode = null;
                    Rect outRect = new Rect();
                    node.getBoundsInScreen(outRect);
                    if (outRect.top > mOpendPacketInScreenY) {
                        mOpendPacketInScreenY = outRect.top;
                    }
                    Utils.logI(TAG, "findRedPacket opend outRect: " + outRect.toString());
                }
                mLastPacketStr = nodeText;
            }
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (node.getChild(i) != null) {
                    findRedPacket(node.getChild(i));
                }

            }
        }
    }

    private boolean isOnOpenPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        boolean isOnOpen = false;
        if (nodeInfo != null) {
            for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
                AccessibilityNodeInfo info = nodeInfo.getChild(i);
                if ("android.widget.Button".equals(info.getClassName())) {
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);//打开红包
                    isOnOpen = true;
                    break;
                }
            }
        }
        return isOnOpen;
    }

    private void getPacketResult(AccessibilityNodeInfo node) {
        if (node != null) {
            if (node.getChildCount() == 0) {
                if (node.getText() != null) {
                    String nodeText = node.getText().toString();
                    Utils.logI(TAG, "getPacketResult: " + nodeText);
                    if (mResultCount == 0) {
                        mPacketResultInfo.packetName = nodeText;
                        mResultCount = 1;//微信6.5.4版本
                    } else if (nodeText.equals("元") && mLastResultText.contains(".")) {
                        mPacketResultInfo.isSuccessed = true;
                        mPacketResultInfo.myMoney = mLastResultText;//金额
                    } else if(nodeText.equals(mWeiXinName) && mResultCount == 1) {
                        Utils.logI(TAG, "getPacketResult: mWeiXinName" );
                        mResultCount = 2;
                        mRanking = 1;
                    } else if (nodeText.equals("手气最佳") && mResultCount == 2) {
                        Utils.logI(TAG, "getPacketResult: luckiest" );
                        mPacketResultInfo.luckiest = nodeText;
                        mResultCount = -1;
                    } else if (mResultCount == -1 && mRanking == 1) {
                        if (nodeText.contains("元") && nodeText.length() > 4) {
                            mRanking++;//排名
                        }
                    }
                    if (nodeText.equals("查看我的红包记录")) {
                        Utils.logI(TAG, "getPacketResult: nodeText.equals(\"查看我的红包记录\")" );
                        mPacketResultInfo.ranking = mRanking;
                    }
                    mLastResultText = nodeText;
                }
            } else {
                for (int i = 0; i < node.getChildCount(); i++) {
                    if (node.getChild(i) != null) {
                        getPacketResult(node.getChild(i));
                    }

                }
            }
        }
    }

    private void dealForResult(PacketResultInfo resultInfo) {
        if (resultInfo.isSuccessed) {
            String str = resultInfo.packetName.contains("的红包") ? "" : "的红包";
            if (resultInfo.ranking > 0) {
                String toastText = "恭喜你第" + resultInfo.ranking + "个抢到" + resultInfo.packetName + str + " ￥" + resultInfo.myMoney;
                if (resultInfo.luckiest.length() > 0) toastText += "[手气最佳]";
                Utils.showToastView(mContext, toastText, Toast.LENGTH_LONG);
            } else {
                String toastText = "恭喜你抢到" + resultInfo.packetName + str + " ￥" + resultInfo.myMoney;
                Utils.showToastView(mContext, toastText, Toast.LENGTH_LONG);
            }
        }
    }

    public static final class PacketResultInfo {
        public  boolean isSuccessed = false;
        public  String packetName = "";
        public  String myMoney = "";
        public  String luckiest = "";
        public  int ranking = 0;
        public  long time;

        public PacketResultInfo() {
            this.isSuccessed = false;
            this.packetName = "";
            this.myMoney = "";
            this.luckiest = "";
            this.ranking = 0;
        }

        public String toString() {
            return "isSuccessed: " + isSuccessed + " " + this.packetName + " " + this.myMoney + " " + this.luckiest + " " + this.ranking;
        }

        public String getSaveString() {
            return this.packetName + "|" + this.myMoney + "|" + this.luckiest + "|" + this.ranking + "|" + this.time;
        }
    }

    private boolean isOurPhone = true;
    private void simulateTouch() {
        Utils.logI(TAG, "simulateTouch");
        if (!isOurPhone) return;
        try {
            Process proc =Runtime.getRuntime().exec("input tap 545 2155");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setSimulateClick(View view, float x, float y) {
        long downTime = SystemClock.uptimeMillis();
        final MotionEvent downEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        downTime += 1000;
        final MotionEvent upEvent = MotionEvent.obtain(downTime, downTime,
                MotionEvent.ACTION_UP, x, y, 0);
        view.onTouchEvent(downEvent);
        view.onTouchEvent(upEvent);
        downEvent.recycle();
        upEvent.recycle();
    }

    private void updateWannotList() {
        String mGroupNamesStr = Utils.getWannotGroupNames(mContext);
        String mManNamesStr = Utils.getWannotManNames(mContext);
        mWannotGroupList.clear();
        mWannotManList.clear();
        if (mGroupNamesStr.length() > 0) {
            String[] strArray = mGroupNamesStr.split("`");
            for (int i = 0; i < strArray.length; i++) {
                mWannotGroupList.add(strArray[i]);
            }
        }
        if (mManNamesStr.length() > 0) {
            String[] strArray = mManNamesStr.split("`");
            for (int i = 0; i < strArray.length; i++) {
                mWannotManList.add(strArray[i]);
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        //系统成功绑定该服务时被触发,也就是当你在设置中开启相应的服务,
        // 通常我们可以在这里做一些初始化操作

        mContext = this;
        Utils.showToastView(mContext, getString(R.string.app_name) + mContext.getString(R.string.toast_after_start), Toast.LENGTH_SHORT);

        updateWannotList();

        mKeyguardManager= (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        //mHandler.sendEmptyMessage(SHOW_IM_RUN);

        IntentFilter filter = new IntentFilter(ACTION_UPDATE_WANNOT_LIST);
        registerReceiver(mReceiver, filter);
        super.onServiceConnected();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Utils.logI(TAG, "mReceiver action: " + action);
            if (ACTION_UPDATE_WANNOT_LIST.equals(action)) {
                updateWannotList();
            }
        }
    };

    private void logEventType(int eventType) {
        String eventTypeName = "";
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventTypeName = "TYPE_VIEW_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                eventTypeName = "TYPE_VIEW_FOCUSED";
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                eventTypeName = "TYPE_VIEW_LONG_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                eventTypeName = "TYPE_VIEW_SELECTED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                eventTypeName = "TYPE_VIEW_TEXT_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                eventTypeName = "TYPE_WINDOW_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                eventTypeName = "TYPE_NOTIFICATION_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                eventTypeName = "TYPE_TOUCH_EXPLORATION_GESTURE_END";
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                eventTypeName = "TYPE_ANNOUNCEMENT";
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                eventTypeName = "TYPE_TOUCH_EXPLORATION_GESTURE_START";
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                eventTypeName = "TYPE_VIEW_HOVER_ENTER";
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                eventTypeName = "TYPE_VIEW_HOVER_EXIT";
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                eventTypeName = "TYPE_VIEW_SCROLLED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                eventTypeName = "TYPE_VIEW_TEXT_SELECTION_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                eventTypeName = "TYPE_WINDOW_CONTENT_CHANGED";
                break;
        }
        Utils.logI(TAG, "onAccessibilityEvent eventType=" + eventTypeName);
    }

}
