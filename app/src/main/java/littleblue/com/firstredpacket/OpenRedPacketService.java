package littleblue.com.firstredpacket;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class OpenRedPacketService extends AccessibilityService {
    private static String TAG = "RedPacket.OpenService";

    private Context mContext;
    private HashMap<String, Float> mNodeHashMap = new HashMap<>();//不能用list
    private static AccessibilityNodeInfo  mNode;
    private int mPacketInScreenY = 0;
    private int mOpendPacketInScreenY = 0;

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
        logEventType(eventType);
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> textList = event.getText();
                if (textList.isEmpty()) break;
                for (CharSequence charSequence : textList) {
                    String textString = charSequence.toString();
                    Log.i(TAG, "Notification charSequence: " + textString);
                    //如果微信红包的提示信息,则模拟点击进入相应的聊天窗口
                    if (textString.contains("[微信红包]")) {
                        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                            Notification notification = (Notification) event.getParcelableData();
                            PendingIntent pendingIntent = notification.contentIntent;
                            try {
                                pendingIntent.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                            mNodeHashMap.clear();
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                //Log.i(TAG, "CONTENT getClassName: " + event.getClassName());
                //break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                Log.i(TAG, "STATE getClassName: " + className);
                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                    handleEvents();
                } else if (className.endsWith("android.widget.TextView")) {
                    //mHandler.sendEmptyMessageDelayed(1, 1000);
                    //handleEvents();
                    simulateTouch();
                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    if (nodeInfo != null) {
                        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
                            AccessibilityNodeInfo info = nodeInfo.getChild(i);
                            if ("android.widget.Button".equals(info.getClassName())) {
                                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);//打开红包
                                break;
                            }
                        }
                    }
                }

                break;

        }

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handleEvents();
        }
    };

    private void handleEvents() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        //Log.i(TAG, "STATE rootNode: " + rootNode);
        if (rootNode != null) {
//                        List<AccessibilityNodeInfo> nodeInfos = rootNode.findAccessibilityNodeInfosByText("领取红包");
//                        AccessibilityNodeInfo lastNode = nodeInfos.get(nodeInfos.size() - 1);
//                        Log.i(TAG, "findAccessibilityNodeInfosByText, lastNode: " + lastNode);
//                        AccessibilityNodeInfo actionNode = lastNode.getParent();
//                        Log.i(TAG, "node.getParent()actionNode: " + actionNode);
            //actionNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            //以上方法判断领取红包可能会造成失效

            if (mNodeHashMap.size() > 0) {
                for (String nodeId : mNodeHashMap.keySet()) {
                    Log.i(TAG, " handleEvents mNodeHashMap.key : " + nodeId);
                }
            }

            mNode = null;
            mPacketInScreenY = 0;
            mOpendPacketInScreenY = 0;
            findRedPacket(rootNode);

            if (mNode == null) {
                Log.i(TAG, " handleEvents mNode is null");
                return;
            }
            Log.i(TAG, " handleEvents mNode isNot null");
            String nodeStr = mNode.toString();
            String nodeId = nodeStr.substring(49, 57);
            if (mNodeHashMap.containsKey(nodeId)) {
                Log.i(TAG, "findRedPacket this node has been added");
                return;
            }
            mNodeHashMap.put(nodeId, -1.0f);

            mNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            AccessibilityNodeInfo parent = mNode.getParent();
            //Log.i(TAG, "node.getParent(): " + parent);
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
                //Log.i(TAG, "findRedPacket: " + node.getText().toString());
                if ("领取红包".equals(node.getText().toString())) {
                    //Log.i(TAG, "findRedPacket node: " + node);
                    String nodeStr = node.toString();
                    //int idStart = nodeStr.indexOf("@");//48
                    String nodeId = nodeStr.substring(49, 57);
                    Log.i(TAG, "*******findRedPacket nodeId: " + nodeId);

                    Rect outRect = new Rect();
                    node.getBoundsInScreen(outRect);
                    if (outRect.top > mPacketInScreenY) {
                        mPacketInScreenY = outRect.top;
                    }
                    Log.i(TAG, "findRedPacket packet outRect: " + outRect.toString());
                    mNode = node;
                } else if((node.getText().toString()).contains("你领取了")) {
                    mNode = null;
                    Rect outRect = new Rect();
                    node.getBoundsInScreen(outRect);
                    if (outRect.top > mOpendPacketInScreenY) {
                        mOpendPacketInScreenY = outRect.top;
                    }
                    Log.i(TAG, "findRedPacket opend outRect: " + outRect.toString());
                }
            }
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (node.getChild(i) != null) {
                    findRedPacket(node.getChild(i));
                }

            }
        }
    }

    private boolean isOurPhone = true;
    private void simulateTouch() {
        Log.i(TAG, "simulateTouch");
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

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        //系统成功绑定该服务时被触发,也就是当你在设置中开启相应的服务,
        // 通常我们可以在这里做一些初始化操作
        super.onServiceConnected();
        mContext = this;
        Utils.showToastView(mContext, getString(R.string.app_name) + "服务已启动，请尽情享受", Toast.LENGTH_SHORT);
    }

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
        Log.i(TAG, "onAccessibilityEvent eventType=" + eventTypeName);
    }

}
