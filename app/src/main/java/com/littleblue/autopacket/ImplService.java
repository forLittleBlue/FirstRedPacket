package com.littleblue.autopacket;

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
        return super.onStartCommand(intent, flags, startId);
    }
}
