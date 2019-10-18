package eu.droogers.smsmatrix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gerben on 6-10-17.
 */

public class ReceiverListener extends BroadcastReceiver {
    private static final String TAG = "ReceiverListener";
    private static final String ACTION_MMS_RECEIVED = "android.provider.Telephony.WAP_PUSH_RECEIVED";
    private static final String MMS_DATA_TYPE = "application/vnd.wap.mms-message";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            handleIncomingSMS(context, intent);
        } else if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            handleIncomingCall(context, intent);
        } else if (intent.getAction().equals(ACTION_MMS_RECEIVED) && intent.getType().equals(MMS_DATA_TYPE)) {
            handleIncomingMMS(context, intent);
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

    private void handleIncomingMMS(final Context context, Intent intent) {
        //I don't know how to download MMSC from cell provider. so we will do something with mmssms.sb
        try {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                byte[] buffer = bundle.getByteArray("data");
                String bufferString = new String(buffer);
                Log.i(TAG, "bufferString: " + bufferString);

                //Get Phone Number (between + and /TYPE
                int mmsIndex = bufferString.indexOf("/TYPE");
                int mmsIndexPlus = bufferString.indexOf("+");
                String number = bufferString.substring(mmsIndexPlus, mmsIndex);
                //Got phone number, Remove +1
                //number = number.replace("+1", "");
                Log.i(TAG, "Phone Number: " + number);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.i(TAG, "Checking if MMS is downloaded.");
                        // this code will be executed after 15 seconds
                        Intent intnet = new Intent(context, eu.droogers.smsmatrix.MmsService.class);
                        intnet.putExtra("onRecieveMms", true);
                        context.startService(intnet);
                    }
                }, 15000);

            }

        } catch (Exception e ) {
            Log.e(TAG, "received mms failed " + e);
        }

    }

}
