package littleblue.com.autopacket;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by 10964492 on 2017/1/21.
 */

public class Utils {
    private static final String TAG = "RedPacket.Utils";

    private static final String PRF = "LittleBlue";
    private static final String KEY_WEIXIN_NAME = "weixin_name";
    public static final String NO_INPUT = "NO_INPUT(@-021)";


    public static void showToastView(Context context, String msg, int time) {
        final View toastView = LayoutInflater.from(context).inflate(R.layout.toast_view, null);
        TextView textView = (TextView) toastView.findViewById(R.id.toast_textView);
        int bytes = msg.getBytes().length;
        if (bytes > 48) {
            textView.setTextSize(16.5f);
        }
        textView.setText(msg);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM, 0, (int)context.getResources().getDimension(R.dimen.toast_y_offset));
        toast.setView(toastView);
        toast.setDuration(time);
        toast.show();
    }

    public static void saveWeiXinName(Context context, String name) {
        Log.i(TAG, "saveWeiXinName: " + name);
        SharedPreferences pref = context.getSharedPreferences(PRF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_WEIXIN_NAME, name);
        editor.commit();
    }

    public static String getWeiXinName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PRF, Context.MODE_PRIVATE);
        String name = pref.getString(KEY_WEIXIN_NAME, NO_INPUT);
        Log.i(TAG, "getWeiXinName: " + name);
        return name;
    }
}
