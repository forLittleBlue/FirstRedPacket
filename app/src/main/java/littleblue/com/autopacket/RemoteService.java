package littleblue.com.autopacket;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.format.DateUtils;

public class RemoteService extends Service {
    private final static String TAG = "RedPacket.RemoteService";

    private Context mContext;
    public static String ACTION_DISABLE_KEYGUARD = "littleblue.action_disable_keyguard";
    public static String ACTION_REENABLE_KEYGUARD = "littleblue.action_reensable_keyguard";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.logI(TAG, "onStartCommand");
        mContext = this;

        IntentFilter filter = new IntentFilter(OpenRedPacketService.ACTION_IS_SERVICE_RUN);
        filter.addAction(ACTION_DISABLE_KEYGUARD);
        filter.addAction(ACTION_REENABLE_KEYGUARD);
        registerReceiver(mReceiver, filter);
        return super.onStartCommand(intent, flags, startId);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Utils.logI(TAG, "mReceiver action: " + action);
            if (ACTION_DISABLE_KEYGUARD.equals(action)) {
                openInKeyguard();
            } else if (ACTION_REENABLE_KEYGUARD.equals(action)) {
                mKeyguardLock.reenableKeyguard();
            }
        }
    };

    private KeyguardManager mKeyguardManager;
    private PowerManager mPowerManager;
    KeyguardManager.KeyguardLock mKeyguardLock;
    private void openInKeyguard() {
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mKeyguardManager= (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        //屏幕解锁
        mKeyguardLock = mKeyguardManager.newKeyguardLock("unLock");
        mKeyguardLock.disableKeyguard();

        //屏幕唤醒
        PowerManager.WakeLock wl = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        wl.acquire();
        wl.release();
    }
}
