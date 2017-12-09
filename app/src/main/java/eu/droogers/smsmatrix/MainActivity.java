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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();

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
                checkPermissions();
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
                startService();
            }
        });
        startService();
    }

    private void checkPermissions() {
        askPermission(Manifest.permission.SEND_SMS);
        askPermission(Manifest.permission.READ_PHONE_STATE);
        askPermission(Manifest.permission.READ_CONTACTS);
        askPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

    }

    private void askPermission(String permission) {
        if (getApplicationContext().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{permission},1);
        }
    }

    private void startService() {
        Intent intent = new Intent(this, MatrixService.class);
        startService(intent);
    }
}
