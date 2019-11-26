package eu.droogers.smsmatrix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gerben on 6-10-17.
 */

public class ReceiverListener extends BroadcastReceiver {
    private static final String TAG = "ReceiverListener";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            handleIncomingSMS(context, intent);
        } else if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            handleIncomingCall(context, intent);
        }
        else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent intentServ = new Intent(context, MatrixService.class);
            ContextCompat.startForegroundService(context, intentServ);
        }
    }

    private void handleIncomingSMS(Context context, Intent intent) {
        Map<String, String> msg = null;
        SmsMessage[] msgs = null;
        Bundle bundle = intent.getExtras();

        if (bundle != null && bundle.containsKey("pdus")) {
            Object[] pdus = (Object[]) bundle.get("pdus");

            if (pdus != null) {
                int nbrOfpdus = pdus.length;
                msg = new HashMap<String, String>(nbrOfpdus);
                msgs = new SmsMessage[nbrOfpdus];

                // Send long SMS of same sender in one message
                for (int i = 0; i < nbrOfpdus; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);

                    String originatinAddress = msgs[i].getOriginatingAddress();

                    // Check if index with number exists
                    if (!msg.containsKey(originatinAddress)) {
                        // Index with number doesn't exist
                        msg.put(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody());

                    } else {
                        // Number is there.
                        String previousparts = msg.get(originatinAddress);
                        String msgString = previousparts + msgs[i].getMessageBody();
                        msg.put(originatinAddress, msgString);
                    }
                }
            }
        }
        for (String originatinAddress : msg.keySet()) {
            Utilities.sendMatrix(context, msg.get(originatinAddress), originatinAddress, Matrix.MESSAGE_TYPE_TEXT);
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
        Utilities.sendMatrix(context, body, cal_from, Matrix.MESSAGE_TYPE_NOTICE);
    }
}
