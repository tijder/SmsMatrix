package eu.droogers.smsmatrix;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_MMS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.SEND_SMS;
import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {
    static Matrix mx;
    private SharedPreferences sp;
    private EditText botUsername;
    private EditText botPassword;
    private EditText username;
    private EditText device;
    private EditText hsUrl;
    private EditText syncDelay;
    private EditText syncTimeout;
    private static final String[] PERMISSIONS_REQUIRED = new String[]{
        READ_SMS, SEND_SMS, RECEIVE_SMS, READ_PHONE_STATE, READ_CONTACTS, READ_EXTERNAL_STORAGE, RECEIVE_MMS
    };
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        botUsername = (EditText) findViewById(R.id.editText_botUsername);
        botPassword = (EditText) findViewById(R.id.editText_botpassword);
        username = (EditText) findViewById(R.id.editText_username);
        device = (EditText) findViewById(R.id.editText_device);
        hsUrl = (EditText) findViewById(R.id.editText_hsUrl);
        syncDelay = (EditText) findViewById(R.id.editText_syncDelay);
        syncTimeout = (EditText) findViewById(R.id.editText_syncTimeout);

        botUsername.setText(sp.getString("botUsername", ""));
        botPassword.setText(sp.getString("botPassword", ""));
        username.setText(sp.getString("username", ""));
        device.setText(sp.getString("device", ""));
        hsUrl.setText(sp.getString("hsUrl", ""));
        syncDelay.setText(sp.getString("syncDelay", "12"));
        syncTimeout.setText(sp.getString("syncTimeout", "30"));


        Button saveButton = (Button) findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermissions()) {
                    askPermissions();
                } else {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("botUsername", botUsername.getText().toString());
                    editor.putString("botPassword", botPassword.getText().toString());
                    editor.putString("username", username.getText().toString());
                    editor.putString("device", device.getText().toString());
                    editor.putString("hsUrl", hsUrl.getText().toString());
                    editor.putString("syncDelay", syncDelay.getText().toString());
                    editor.putString("syncTimeout", syncTimeout.getText().toString());
                    editor.apply();

                    Log.e(TAG, "onClick: " + botUsername.getText().toString() );
                    Intent matrixService = new Intent(getApplicationContext(), eu.droogers.smsmatrix.MatrixService.class);
                    startService(matrixService);

                    Intent mmsIntent = new Intent(getApplicationContext(), eu.droogers.smsmatrix.MmsService.class);
                    mmsIntent.putExtra("startButton", true);
                    startService(mmsIntent);
                }

            }
        });
        if (!checkPermissions()) {
            askPermissions();
        } else {
            startService();
        }
    }

    private boolean checkPermissions() {
        for (String permission: PERMISSIONS_REQUIRED) {
            int result = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            if (result  != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            Log.i(TAG, "setOnClickListener - result result result" + result);
        }
        return true;
    }

    private void askPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, PERMISSION_REQUEST_CODE);
    }


    private void startService() {
        Intent intent = new Intent(this, MatrixService.class);
        startService(intent);
    }
}
