package eu.droogers.smsmatrix;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

/**
 * Created by gerben on 7-10-17.
 */

public class MatrixService extends Service {
    private Matrix mx;
    private final String TAG = "MatrixService";
    private String botUsername;
    private String botPassword;
    private String username;
    private String device;
    private String hsUrl;
    private String syncDelay;
    private String syncTimeout;
    private MMSMonitor mms;
    private String mChannelId = "";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (mChannelId.isEmpty()) {
                mChannelId = createNotificationChannel("sync", "Sync Service");
            }
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, mChannelId);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(getApplicationInfo().loadLabel(getPackageManager()))
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(1, notification);
        }

        SharedPreferences sp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        botUsername = sp.getString("botUsername", "");
        botPassword = sp.getString("botPassword", "");
        username = sp.getString("username", "");
        device = sp.getString("device", "");
        hsUrl = sp.getString("hsUrl", "");
        syncDelay = sp.getString("syncDelay", "12");
        syncTimeout = sp.getString("syncTimeout", "60");

        if (mx == null && !botUsername.isEmpty() && !botPassword.isEmpty() && !username.isEmpty() && !device.isEmpty() && !hsUrl.isEmpty() && !syncDelay.isEmpty() && !syncTimeout.isEmpty()) {
            mx = new Matrix(getApplication(), hsUrl, botUsername, botPassword, username, device, syncDelay, syncTimeout);
            Log.e(TAG, "onStartCommand: " + hsUrl );
            Toast.makeText(this, "service starting:", Toast.LENGTH_SHORT).show();
        } else if (mx == null) {
            Toast.makeText(this, "Missing Information", Toast.LENGTH_SHORT).show();
        }

        Log.e(TAG, "onStartCommand: Service");

        String phone = intent.getStringExtra("SendSms_phone");
        String type = intent.getStringExtra("SendSms_type");
        if (phone != null) {
            System.out.println(phone);
            if (type.equals(Matrix.MESSAGE_TYPE_TEXT) || type.equals(Matrix.MESSAGE_TYPE_NOTICE))
            {
                String body = intent.getStringExtra("SendSms_body");
                mx.sendMessage(phone, body, type);
            } else if (type.equals(Matrix.MESSAGE_TYPE_IMAGE) || type.equals(Matrix.MESSAGE_TYPE_VIDEO)) {
                byte[] body = intent.getByteArrayExtra("SendSms_body");
                String fileName = intent.getStringExtra("SendSms_fileName");
                String contentType = intent.getStringExtra("SendSms_contentType");
                mx.sendFile(phone, body, type, fileName, contentType);
            }
        }

        if (this.mms == null) {
            this.mms = new MMSMonitor(this , getApplicationContext());
            this.mms.startMMSMonitoring();
        }

        return START_NOT_STICKY;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    public void onDestroy() {
        if (mx != null) {
            mx.destroy();
        }
        this.mms.stopMMSMonitoring();
        this.mms = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
