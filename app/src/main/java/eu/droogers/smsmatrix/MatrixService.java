package eu.droogers.smsmatrix;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
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
        syncDelay = sp.getString("syncDelay", "");
        syncTimeout = sp.getString("syncTimeout", "");

        if (mx == null && !botUsername.isEmpty() && !botPassword.isEmpty() && !username.isEmpty() && !device.isEmpty() && !hsUrl.isEmpty() && !syncDelay.isEmpty() && !syncTimeout.isEmpty()) {
            mx = new Matrix(getApplication(), hsUrl, botUsername, botPassword, username, device, syncDelay, syncTimeout);
            Log.e(TAG, "onStartCommand222: " + hsUrl );
            Toast.makeText(this, "service starting:", Toast.LENGTH_SHORT).show();
        }

        Log.e(TAG, "onStartCommand: Service");

        String phone = intent.getStringExtra("SendSms_phone");
        String body = intent.getStringExtra("SendSms_body");
        String type = intent.getStringExtra("SendSms_type");
        if (phone != null) {
            mx.sendMessage(phone, body, type);
        }
        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        mx.destroy();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
