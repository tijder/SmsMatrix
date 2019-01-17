package eu.droogers.smsmatrix;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private boolean notificationCheckBoxStatus;
    private boolean mmsCheckBoxStatus;

    private OpenHelper mDbOpenHelper;
    private String onReceiveType = "nothing";
    private int app_id = 0;

    public static NotificationManager notificationManager = null;
    public final static int PERSISTENT_NOTIFICATION_ID = 001;
    public final static String NOTIFICATION_CHANNEL_ID = "smsmatrix";
    public static NotificationCompat.Builder mBuilder = null;

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
        notificationCheckBoxStatus = sp.getBoolean("notificationCheckBoxStatus", false);
        mmsCheckBoxStatus = sp.getBoolean("mmsCheckBoxStatus", false);

        if (mx == null && !botUsername.isEmpty() && !botPassword.isEmpty() && !username.isEmpty() && !device.isEmpty() && !hsUrl.isEmpty() && !syncDelay.isEmpty() && !syncTimeout.isEmpty()) {
            mx = new Matrix(getApplication(), hsUrl, botUsername, botPassword, username, device, syncDelay, syncTimeout);
            Log.e(TAG, "onStartCommand: " + hsUrl );
            if (notificationCheckBoxStatus){
                persistentNotification();
            }
            Toast.makeText(this, "service starting:", Toast.LENGTH_SHORT).show();
        } else if (mx == null) {
            Toast.makeText(this, "Missing Information", Toast.LENGTH_SHORT).show();
        }

        Log.e(TAG, "onStartCommand: Service");

        //From ReceiverListener
        onReceiveType = intent.getStringExtra("onReceiveType");

        //Recieved a new SMS
        if (onReceiveType != null && onReceiveType.equals("sms")) {
            Log.i(TAG, "SMS ");
            try {
                mDbOpenHelper = new OpenHelper(this);
            } catch (Exception e) {
                Log.e(TAG, "error " + e);
            }

            try {
                Map<String, String> msg = null;
                SmsMessage[] msgs = null;
                Bundle bundle = intent.getExtras();
                long sms_id = 0;

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
                                // Index with number doesn't exist (First message from that person)
                                //date get NOW date when oncrecieved is called
                                //long date = Instant.now().toEpochMilli();
                                long date = 0;
                                //date_sent
                                long date_sent = msgs[i].getTimestampMillis();
                                msg.put(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody());
                                String fromAddress = msgs[i].getOriginatingAddress();
                                String fromBody = msgs[i].getMessageBody();
                                sms_id = DataManager.importSms(mDbOpenHelper,
                                        fromAddress,
                                        date,
                                        date_sent,
                                        DataManager.NOT_READ,
                                        DataManager.TYPE_RECIEVED,
                                        DataManager.NO_STATUS,
                                        fromBody,
                                        DataManager.NOT_LOCKED,
                                        DataManager.ERROR_NO);
                            } else {
                                //Make sure we know where to put the rest of the long sms data
                                if (sms_id > 0) {
                                    // Number is there and we have the ID for the first imported long sms.
                                    String previousparts = msg.get(originatinAddress);
                                    String msgString = previousparts + msgs[i].getMessageBody();
                                    msg.put(originatinAddress, msgString);

                                    DataManager.updateLongSmsBody(mDbOpenHelper,
                                            sms_id,
                                            msgString);

                                } else {
                                    Log.e(TAG, "Problem adding Log SMS to database");
                                }
                            }
                        }
                        //start sms stuff
                        Log.i(TAG, "SMSes done ");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "error " + e);
            }

        }
        //Recieved a new SMS END

        //Recieved a new MMS
        if (onReceiveType != null && mmsCheckBoxStatus && onReceiveType.equals("mms")) {
            Log.i(TAG, "MMS - MMS");
            try {
                mDbOpenHelper = new OpenHelper(this);
            } catch (Exception e) {
                Log.e(TAG, "MMS - OpenHelper error " + e);
            }

            //I dont know how to download MMSC from cell provider. so we will do soemthing with mmssms.sb
            Bundle bundle = intent.getExtras();
            if (bundle != null && mDbOpenHelper != null) {
                byte[] buffer = bundle.getByteArray("data");
                String bufferString = new String(buffer);
                Log.i(TAG, "MMS - bufferString: " + bufferString);

                //Get Phone Number (between + and /TYPE
                int mmsIndex = bufferString.indexOf("/TYPE");
                int mmsIndexPlus = bufferString.indexOf("+");
                String address = bufferString.substring(mmsIndexPlus, mmsIndex);
                //Got phone number, Remove +1
                address = address.replace("+1", "");
                Log.i(TAG, "MMS - buffer address: " + address);

                //Get MMSCURL TR ID (first char string in buffer)
                //try to match this with tr_id in mmssms.db
                String tr_id = "";
                for (char ch: bufferString.toCharArray()) {
                    if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '-') {
                        tr_id += ch;
                    } else if ( !tr_id.isEmpty() ){
                        Log.i(TAG, "MMS - tr_id " + tr_id);
                        break;
                    }
                }

                //I have no idea how to download the MMSC - we'll query the mmssms.db
                //If you can, please fix this or make it better.
                Log.i(TAG, "MMS - address " + address);
                Log.i(TAG, "MMS - tr_id " + tr_id);

                DataManager.importMms(
                        mDbOpenHelper,
                        tr_id,
                        address,
                        0,
                        0,
                        DataManager.NOT_READ,
                        DataManager.TYPE_RECIEVED,
                        0,
                        DataManager.NO_STATUS,
                        "",
                        DataManager.NOT_LOCKED,
                        DataManager.ERROR_NO);

                //there is a new MMS - Lets sleep to let the default messenger download the MMS
                Log.e(TAG, "Sleep to let android download mms    ");
                try { TimeUnit.SECONDS.sleep(9); } catch (Exception e) {}

            } else {
                Log.e(TAG, "MMS - Error with MMS bundle or Database");
            }

        } else if (!mmsCheckBoxStatus) {
            Log.i(TAG, "MMS - MMS not enabled");
        }
        //Recieved a new MMS END


        //Ran each time service is started.
        //Checks for unread messages in database and send them
        if (mx != null ) {
            //Check Database everytime something calls this and try to send all unread messages

            // SMS and MMS
            OpenHelper dbHelper = new OpenHelper(this);


            //SMS Start
            Log.i(TAG, "checking for SMSes");
            Log.i(TAG, "unreadSmses called");
            SQLiteDatabase unreadSmsesDb = dbHelper.getReadableDatabase();
            String[] notLocked = new String[] { "locked" };


            Cursor unreadSms =  unreadSmsesDb.query(
                    DatabaseContract.SmsEntry.TABLE_NAME,
                    DataManager.ALLColumns,
                    DataManager.unread,
                    null,
                    null,
                    null,
                    null);


            if (unreadSms.moveToFirst()) {
                //Found unread SMS
                do {
                    Log.i(TAG, "sending unread messages");
                    String body = unreadSms.getString(unreadSms.getColumnIndex("body"));
                    String address = unreadSms.getString(unreadSms.getColumnIndex("address"));
                    int smsId = unreadSms.getInt(unreadSms.getColumnIndex("_id"));
                    //locking SMS
                    Log.i(TAG, "Locking unread messages");
                    DataManager.lockSms(dbHelper, smsId);
                    //trying to send SMS
                    Log.i(TAG, "sending unread messages");
                    mx.sendMessage(address, body, Matrix.MESSAGE_TYPE_TEXT, smsId);
                } while (unreadSms.moveToNext());
            } else {
                Log.i(TAG, "No unread SMSes in Database");
            }
            unreadSms.close();
            unreadSmsesDb.close();
            // SMS done


            // MMS Start
            if (mmsCheckBoxStatus) {
                Log.i(TAG, "Checking for MMSes");
                SQLiteDatabase appDatabase = dbHelper.getReadableDatabase();
                Cursor appDatabaseCursor = appDatabase.query(
                        DatabaseContract.MmsEntry.TABLE_NAME,
                        DataManager.ALLColumns,
                        DataManager.unread,
                        null,
                        null,
                        null,
                        null);
                //Log.i(TAG, "appDatabaseCursor cursor " + DatabaseUtils.dumpCursorToString(appDatabaseCursor)  );

                while (appDatabaseCursor.moveToNext()) {
                    String appTr_id = appDatabaseCursor.getString(appDatabaseCursor.getColumnIndex("tr_id"));
                    int app_id = appDatabaseCursor.getInt(appDatabaseCursor.getColumnIndex("_id"));
                    DataManager.lockMms(dbHelper, app_id);
                    String address = appDatabaseCursor.getString(appDatabaseCursor.getColumnIndex("address"));



                    Log.i(TAG, "Found tr_id="+ appTr_id + " in app database marked as unread (not sent).");
                    Uri mmsInboxDatabase = Uri.parse("content://mms/inbox");

                    //Match tr_id in app database(that I got from MMS onRecieve)
                    //to tr_id in the mmssms.db inbox(the pdu table) to get the _id
                    Cursor mmsInboxDatabaseCursor = getContentResolver().query(
                            mmsInboxDatabase,
                            null,
                            "tr_id='" + appTr_id + "'",
                            null,
                            null);

                    if (mmsInboxDatabaseCursor.moveToFirst()) {
                        //there should only be one tr_id match here.
                        //Log.i(TAG, "Second (only one, want mid cursor " + DatabaseUtils.dumpCursorToString(mmsInboxDatabaseCursor)  );
                        int mid = mmsInboxDatabaseCursor.getInt(mmsInboxDatabaseCursor.getColumnIndex("_id"));
                        mmsInboxDatabaseCursor.close();


                        //the _id from mmssms.db content://mms/inbox(pdu table) will be matched to mid in mmssms.db part


                        String selectionPart = "mid=" + String.valueOf(mid) + " AND NOT ct=\'application/smil\'";
                        Uri uri = Uri.parse("content://mms/part");
                        Cursor mmsPartCursor = getContentResolver().query(uri, null, selectionPart, null, null );
                        Log.i(TAG, "THIRDDDDD cursor " + DatabaseUtils.dumpCursorToString(mmsPartCursor)  );

                        //The part table will have 2+ entries with the same mid.
                        //The first is information about the rest - application/smil - I ignore this
                        //The second++ is each picture set to us
                        //The Last (if provided) is the text message along with all the pictures - text/plain
                        //So an MMS with 2 pictures with text will have 4 parts. information, picture, picture, text.
                        while (mmsPartCursor.moveToNext()) {
                            String type = mmsPartCursor.getString(mmsPartCursor.getColumnIndex("ct"));
                            Log.i(TAG, "typetype:  " + type );

                            if (type.equals("text/plain")){
                                String text = mmsPartCursor.getString(mmsPartCursor.getColumnIndex("text"));
                                Log.i(TAG, "MMS message with text:  " + text );
                                mx.sendMessage(address, text, Matrix.MESSAGE_TYPE_TEXT, 0);
                            } else {
                                String name = mmsPartCursor.getString(mmsPartCursor.getColumnIndex("name"));
                                Log.i(TAG, "MMS message picture:  " + name );
                                byte[] mediaData = null;
                                String fileName = "";

                                fileName = mmsPartCursor.getString(mmsPartCursor.getColumnIndex("name"));
                                Log.e(TAG, "mmsPartCursor - fileName " + fileName);

                                mediaData = readMMSPart(mmsPartCursor.getString(mmsPartCursor.getColumnIndex("_id")));
                                Log.e(TAG, "mmsPartCursor - mediaData " + mediaData);

                                String contentType = mmsPartCursor.getString(mmsPartCursor.getColumnIndex("ct"));
                                Log.e(TAG, "mmsPartCursor - contentType " + contentType);

                                if (isImageType(contentType)) {
                                    mx.sendFile(address, mediaData, Matrix.MESSAGE_TYPE_IMAGE, fileName, contentType, app_id);
                                } else if (isVideoType(contentType)) {
                                    mx.sendFile(address, mediaData, Matrix.MESSAGE_TYPE_VIDEO, fileName, contentType, app_id);
                                }

                            }
                        }
                    } else {
                        DataManager.unlockMms(dbHelper, app_id);

                        Log.e(TAG, "I cant find tr_id="+appTr_id+"  in the MMS Inbox. perhaps the MMS has not been downloaded yet");
                        mmsInboxDatabaseCursor.close();
                    }

                }

                Log.i(TAG, "Unread MMSes have been processes.");
            } else {
                Log.i(TAG, "MMS not enabled");
            }
            // MMS Done




        }
        //Ran each time service is started END

        return START_NOT_STICKY;

    }


    public void persistentNotification() {
        notificationManager = getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            CharSequence name = "Persistent";
            String description = "Sms Matrix Service is running";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("SmsMatrix")
                .setContentText("Matrix service running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);
        notificationManager.notify(PERSISTENT_NOTIFICATION_ID, mBuilder.build());
    }

    private byte[] readMMSPart(String partId) {
        byte[] partData = null;
        Uri partURI = Uri.parse("content://mms/part/" + partId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;

        try {

            Log.i(TAG,"Entered into readMMSPart try.");
            ContentResolver mContentResolver = getContentResolver();
            is = mContentResolver.openInputStream(partURI);

            byte[] buffer = new byte[256];
            int len = is.read(buffer);
            while (len >= 0) {
                baos.write(buffer, 0, len);
                len = is.read(buffer);
            }
            partData = baos.toByteArray();

        } catch (IOException e) {
            Log.e(TAG, "Exception == Failed to load part data");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception :: Failed to close stream");
                }
            }
        }
        return partData;
    }

    private boolean isImageType(String mime) {
        boolean result = false;
        if (mime.equalsIgnoreCase("image/jpg")
                || mime.equalsIgnoreCase("image/jpeg")
                || mime.equalsIgnoreCase("image/png")
                || mime.equalsIgnoreCase("image/gif")
                || mime.equalsIgnoreCase("image/bmp")) {
            result = true;
        }
        return result;
    }

    private boolean isVideoType(String mime) {
        boolean result = false;
        if (mime.equalsIgnoreCase("video/3gpp")
                || mime.equalsIgnoreCase("video/3gpp2")
                || mime.equalsIgnoreCase("video/avi")
                || mime.equalsIgnoreCase("video/mp4")
                || mime.equalsIgnoreCase("video/mpeg")
                || mime.equalsIgnoreCase("video/webm")) {
            result = true;
        }
        return result;
    }

    @Override
    public void onDestroy() {
        if (notificationCheckBoxStatus) {
            try {
                notificationManager.cancel(PERSISTENT_NOTIFICATION_ID);
            } catch (Exception e) {}
        }
        mx.destroy();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
