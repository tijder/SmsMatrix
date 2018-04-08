package eu.droogers.smsmatrix;

import android.content.Context;
import android.content.Intent;

public class Utilities {
    public static void sendMatrix(Context context, String body, String phone, String type) {
        Intent intent = new Intent(context, MatrixService.class);
        intent.putExtra("SendSms_phone", phone);
        intent.putExtra("SendSms_body", body);
        intent.putExtra("SendSms_type", type);
        context.startService(intent);
    }

    public static void sendMatrix(Context context, byte[] body, String phone, String type, String fileName, String contentType) {
        Intent intent = new Intent(context, MatrixService.class);
        intent.putExtra("SendSms_phone", phone);
        intent.putExtra("SendSms_body", body);
        intent.putExtra("SendSms_type", type);
        intent.putExtra("SendSms_fileName", fileName);
        intent.putExtra("SendSms_contentType", contentType);
        context.startService(intent);
    }
}
