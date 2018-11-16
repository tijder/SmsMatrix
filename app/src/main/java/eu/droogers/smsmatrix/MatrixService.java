package eu.droogers.smsmatrix;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

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
    private NotificationManager notificationManager = null;
    private final static int PERSISTENT_NOTIFICATION_ID = 001;
    private final static String NOTIFICATION_CHANNEL_ID = "smsmatrix";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
            persistentNotification();
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

    public void persistentNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Persistent";
            String description = "Sms Matrix Service is running";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.matrix_user)
                .setContentTitle("SmsMatrix")
                .setContentText("Matrix service running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);
        notificationManager.notify(PERSISTENT_NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        mx.destroy();
        notificationManager.cancel(PERSISTENT_NOTIFICATION_ID);
        this.mms.stopMMSMonitoring();
        this.mms = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
