package littleblue.com.autopacket;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;

public class ImplService extends Service {
    private static String TAG = "RedPacket.ImplService";

    private Context mContext;
    private long mReceiverTime = -1;
    private static final int MSG_RECEIVED_IS_SERVICE_RUN = 1;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.logI(TAG, "onStartCommand");
        startForeground(FakeService.NOTIFY_ID, new Notification());
        startService(new Intent(this, FakeService.class));
        mContext = this;

//        IntentFilter filter = new IntentFilter(OpenRedPacketService.ACTION_IS_SERVICE_RUN);
//        filter.addAction("OPEN_IN_KEYGUARD");
//        registerReceiver(mReceiver, filter);
        return super.onStartCommand(intent, flags, startId);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Utils.logI(TAG, "mReceiver action: " + action);
            if (OpenRedPacketService.ACTION_IS_SERVICE_RUN.equals(action)) {
                if (mReceiverTime < 0) {
                    mHandler.sendEmptyMessageDelayed(MSG_RECEIVED_IS_SERVICE_RUN, 5*DateUtils.SECOND_IN_MILLIS);
                }
                mReceiverTime = System.currentTimeMillis();
            } else if ("OPEN_IN_KEYGUARD".equals(action)) {
                openInKeyguard();
            }
        }
    };

    private KeyguardManager mKeyguardManager;
    private PowerManager mPowerManager;
    private void openInKeyguard() {
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mKeyguardManager= (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        //屏幕解锁
        KeyguardManager.KeyguardLock kl = mKeyguardManager.newKeyguardLock("unLock");
        kl.disableKeyguard();

        //屏幕唤醒
        PowerManager.WakeLock wl = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        wl.acquire();
        wl.release();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECEIVED_IS_SERVICE_RUN:
                    if (System.currentTimeMillis() - mReceiverTime > 10* DateUtils.SECOND_IN_MILLIS) {
                        Utils.logI(TAG, "OpenRedPacketService is not run");
                        sendNotification();
                    }
                    mHandler.sendEmptyMessageDelayed(MSG_RECEIVED_IS_SERVICE_RUN, 5*DateUtils.SECOND_IN_MILLIS);
                    break;
            }
        }
    };

    private void sendNotification() {
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.notification_restart_srvice_title))
                .setContentText(getString(R.string.notification_restart_srvice_content));
        //.setWhen(System.currentTimeMillis());
        notifyManager.notify(1, builder.build());
    }
}
