package eu.droogers.smsmatrix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

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
    }

    private void handleIncomingSMS(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String msg_from;
        if (bundle != null){
            try{
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                for(int i=0; i<msgs.length; i++){
                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    msg_from = msgs[i].getOriginatingAddress();
                    String msgBody = msgs[i].getMessageBody();
                    Utilities.sendMatrix(context, msgBody, msg_from, Matrix.MESSAGE_TYPE_TEXT);
                }
            }catch(Exception e){
                Log.d("Exception caught",e.getMessage());
            }
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
