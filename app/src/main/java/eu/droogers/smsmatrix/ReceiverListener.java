package eu.droogers.smsmatrix;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gerben on 6-10-17.
 */

public class ReceiverListener extends BroadcastReceiver {
    private static final String TAG = "ReceiverListener";
    private static final String ACTION_MMS_RECEIVED = "android.provider.Telephony.WAP_PUSH_RECEIVED";
    public static final String MMS_DATA_TYPE = "application/vnd.wap.mms-message";
    private OpenHelper mDbOpenHelper;





    @Override
    public void onReceive(Context context, Intent intent) {
        mDbOpenHelper = new OpenHelper(context);
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Log.d(TAG, "got SMS");
            Intent newIntent = new Intent(intent);
            newIntent.setComponent(new ComponentName(context, eu.droogers.smsmatrix.MatrixService.class));
            newIntent.putExtra("onReceiveType", "sms");
            context.startService(newIntent);
        } else if (intent.getAction().equals(ACTION_MMS_RECEIVED) && intent.getType().equals(MMS_DATA_TYPE)) {
            Log.d(TAG, "got MMS");
            Intent newIntent = new Intent(intent);
            newIntent.setComponent(new ComponentName(context, eu.droogers.smsmatrix.MatrixService.class));
            newIntent.putExtra("onReceiveType", "mms");
            context.startService(newIntent);
        } else if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            handleIncomingCall(context, intent);
        }
    }

    private void handleIncomingCall(Context context, Intent intent) {
        String cal_state = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
        String cal_from = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
        String body = cal_from;
        switch(cal_state){
            case "IDLE":
                body += " end call";
                break;
            case "OFFHOOK":
                body += " answered call";
                break;
            case "RINGING":
                body += " is calling";
                break;
        }
        //sendMatrix(context, body, cal_from, Matrix.MESSAGE_TYPE_NOTICE);
        //public static void sendMatrix(Context context, String body, String phone, String type) {
        //    Intent intent = new Intent(context, MatrixService.class);
        //    intent.putExtra("SendSms_phone", phone);
        //    intent.putExtra("SendSms_body", body);
        //    intent.putExtra("SendSms_type", type);
        //    context.startService(intent);
        //}
    }

}
