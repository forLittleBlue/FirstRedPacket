package com.littleblue.autopacket;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class FakeService extends Service {
    private static String TAG = "RedPacket.FakeService";

    public static int NOTIFY_ID = 100101;

    public FakeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.logI(TAG, "onStartCommand");
        startForeground(NOTIFY_ID, new Notification());
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}
