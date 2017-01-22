package littleblue.com.firstredpacket;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String TAG = "RedPacket.MainActivity";

    private Context mContext;
    private Button mStartButton;
    private static String mService = "littleblue.com.firstredpacket/.OpenRedPacketService";
    private boolean isAccSeviceEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mStartButton = (Button) findViewById(R.id.button_start);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAccSeviceEnabled = getServiceIsEnabled();
                if (!isAccSeviceEnabled) {
                    Intent intent =  new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    Utils.showToastView(mContext, "在本界面开启“自动抢红包”的服务", Toast.LENGTH_LONG);
                } else {
                    Utils.showToastView(mContext, "服务已启动，请尽情享受", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAccSeviceEnabled = getServiceIsEnabled();
    }

    private boolean getServiceIsEnabled() {
        boolean isEnabled = false;
        AccessibilityManager abm = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfoList = abm.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo serviceInfo : serviceInfoList) {
            Log.i(TAG, "AccessibilityServiceInfo : " + serviceInfo.getId());
            if (mService.equals(serviceInfo.getId())) {
                isEnabled = true;
                break;
            }
        }
        return isEnabled;
    }


}
