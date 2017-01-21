package littleblue.com.firstredpacket;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class OpenRedPacketService extends AccessibilityService {
    private static String TAG = "RedPacket.OpenRedPacketService";

    private Context mContext;
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

}
