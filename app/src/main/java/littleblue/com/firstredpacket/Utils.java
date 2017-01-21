package littleblue.com.firstredpacket;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by 10964492 on 2017/1/21.
 */

public class Utils {

    public static void showToastView(Context context, String msg, int time) {
        final View toastView = LayoutInflater.from(context).inflate(R.layout.toast_view, null);
        TextView textView = (TextView) toastView.findViewById(R.id.toast_textView);
        textView.setText(msg);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM, 0, 150);
        toast.setView(toastView);
        toast.setDuration(time);
        toast.show();
    }
}
