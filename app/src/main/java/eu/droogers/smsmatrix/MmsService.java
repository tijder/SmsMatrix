package eu.droogers.smsmatrix;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MmsService extends Service {
    private static final String TAG = "MmsService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: MmsService");
        Boolean firstStart = intent.getBooleanExtra("startButton", false);
        Boolean newMms = intent.getBooleanExtra("onRecieveMms", false);

        if (firstStart) {
            Log.i(TAG, "App starting");
            firstBoot();
        } else if (newMms) {
            Log.i(TAG, "New MMS from BroadcastListener.");
            checkForNewMms();
            checkUnreadMms();
        } else {
            Log.e(TAG, "Something started MmsService?");
            //we shouldn't get here
        }
        return START_NOT_STICKY;
    }

    private void firstBoot() {
        SqlOpenHelper mSqlOpenHelper = new SqlOpenHelper(getApplicationContext());
        long mmsCount = dbManager.countMmses(mSqlOpenHelper);
        if (mmsCount == 0) {
            Log.i(TAG, "Importing latest MMS to our DB as starting point");
            importLatestMms(mSqlOpenHelper);
        }
        mSqlOpenHelper.close();
    }

    private void checkForNewMms() {
        //can we lock here?
        SqlOpenHelper mSqlOpenHelper = new SqlOpenHelper(this);
        long lastBridgedMms = dbManager.lastMmsId(mSqlOpenHelper);
        long latestMms = getLatestMms(mSqlOpenHelper);
        Log.i(TAG, "Last MMS ID in our database: " + lastBridgedMms + "  Latest MMS ID: " + latestMms);
        if (lastBridgedMms > 0 && latestMms > lastBridgedMms) {
            Log.i(TAG, "there is a new MMS to send to Matrix!");
            findNewMms(lastBridgedMms);
        } else {
            Log.i(TAG, "Looks like our databases are in sync.");
        }
        mSqlOpenHelper.close();
    }

    private void findNewMms(long lastBridgedMms) {
        Uri mmsInboxDatabase = Uri.parse("content://mms/inbox");
        String selectionPart = "_id > " + lastBridgedMms;
        Cursor mmsInboxDatabaseCursor = getContentResolver().query(
                mmsInboxDatabase,
                null,
                selectionPart,
                null,
                "_id ASC");
        while (mmsInboxDatabaseCursor.moveToNext()) {
            long mid = mmsInboxDatabaseCursor.getInt(mmsInboxDatabaseCursor.getColumnIndex("_id"));
            Log.i(TAG, "mid to look in part for: " + mid );
            writeToDatabase(mid);
        }
    }

    private void writeToDatabase(long mid) {
        SqlOpenHelper mSqlOpenHelper = new SqlOpenHelper(this);
        //Get Phone number
        String sourceNumber = getMmsSourceNumber(mid);
        Log.i(TAG, "Picture from: " + sourceNumber);
        dbManager.importNewMms(mSqlOpenHelper,dbManager.TYPE_INCOMING,dbManager.EPOCH_NO,dbManager.EPOCH_NO, sourceNumber, mid,dbManager.BRIDGED_NO,dbManager.LOCK_NO,dbManager.ERROR_NO);
    }

    private String getMmsSourceNumber(long mid) {
        String sourceNumber = null;

        Uri mmsAddrDatabase = Uri.parse("content://mms/" + mid + "/addr/");
        String selectionPart = "type=137";
        //137 = source
        //107 = dest
        String[] selectAddress = new String[] { "address" };
        Cursor mmsAddrDatabaseCursor = getContentResolver().query(
                mmsAddrDatabase,
                selectAddress,
                selectionPart,
                null,
                null);
        try {
            mmsAddrDatabaseCursor.moveToFirst();
            sourceNumber = mmsAddrDatabaseCursor.getString(mmsAddrDatabaseCursor.getColumnIndex("address"));
            //remove the +1 because I dont need them
            //sourceNumber = sourceNumber.replace("+1", "");
        } catch (Exception e) {
            Log.e(TAG, "Error finding phone number of MID");
            sourceNumber = "0";
        } finally {
            mmsAddrDatabaseCursor.close();
        }

        return sourceNumber;
    }

    private long getLatestMms(SqlOpenHelper mSqlOpenHelper) {
        int mmsId = 0;
        Uri mmsInboxDatabase = Uri.parse("content://mms/inbox");
        Cursor mmsInboxDatabaseCursor = getContentResolver().query(
                mmsInboxDatabase,
                null,
                null,
                null,
                "_id DESC");

        try {
            mmsInboxDatabaseCursor.moveToFirst();
            mmsId = mmsInboxDatabaseCursor.getInt(mmsInboxDatabaseCursor.getColumnIndex("_id"));
        } catch (Exception e) {
            Log.e(TAG, "ERROR gettings Latest MMS: " + e );
            mmsId = 0;
        } finally {
            mmsInboxDatabaseCursor.close();
        }

        return mmsId;
    }


    private long importLatestMms(SqlOpenHelper mSqlOpenHelper) {
        long mmsId = getLatestMms(mSqlOpenHelper);
        Log.i(TAG, "Latest MMS message: " + mmsId);
        long now = System.currentTimeMillis();
        dbManager.importNewMms(mSqlOpenHelper, dbManager.TYPE_RESYNC, now, now, "0000000000", mmsId, dbManager.BRIDGED_INIT, dbManager.LOCK_NO, dbManager.ERROR_NO );
        return mmsId;
    }



    private void checkUnreadMms() {
        SqlOpenHelper mSqlOpenHelper = new SqlOpenHelper(getApplicationContext());
        SQLiteDatabase appDb = mSqlOpenHelper.getReadableDatabase();
        String unreadMmsSelection = "bridged=0 AND lock=0";
        Cursor unreadMmsQueryCursor = appDb.query(DatabaseContract.MmsEntry.TABLE_NAME, null, unreadMmsSelection, null, null, null, null);
        Log.i(TAG, "mmsQueryCursor cursor " + DatabaseUtils.dumpCursorToString(unreadMmsQueryCursor)  );

        while (unreadMmsQueryCursor.moveToNext()) {
            int id = unreadMmsQueryCursor.getInt(unreadMmsQueryCursor.getColumnIndex("_id"));
            int mid = unreadMmsQueryCursor.getInt(unreadMmsQueryCursor.getColumnIndex("mms_id"));
            String number = unreadMmsQueryCursor.getString(unreadMmsQueryCursor.getColumnIndex("number"));

            //
            String selectionPart = "mid=" + String.valueOf(mid) + " AND NOT ct=\'application/smil\'";
            Uri uri = Uri.parse("content://mms/part");
            Cursor mmsPartCursor = getContentResolver().query(uri, null, selectionPart, null, null );

            //The part table will have 2+ entries with the same mid.
            //The first is information about the rest - application/smil - I ignore this
            //The second++ is each picture set to us
            //The Last (if provided) is the text message along with all the pictures - text/plain
            //So an MMS with 2 pictures with text will have 4 parts. information, picture, picture, text.
            while (mmsPartCursor.moveToNext()) {
                String type = mmsPartCursor.getString(mmsPartCursor.getColumnIndex("ct"));

                if (type.equals("text/plain")){
                    String text = mmsPartCursor.getString(mmsPartCursor.getColumnIndex("text"));
                    Log.i(TAG, "MMS message with text:  " + text );
                    Utilities.sendMatrix(getApplicationContext(), text, number, Matrix.MESSAGE_TYPE_TEXT);
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
                        Utilities.sendMatrix(
                                getApplicationContext(),
                                mediaData,
                                number,
                                Matrix.MESSAGE_TYPE_IMAGE,
                                fileName,
                                contentType
                        );
                    } else if (isVideoType(contentType)) {
                        Utilities.sendMatrix(
                                getApplicationContext(),
                                mediaData,
                                number,
                                Matrix.MESSAGE_TYPE_VIDEO,
                                fileName,
                                contentType
                        );
                    }
                    dbManager.markMmsRead(mSqlOpenHelper, id);

                }
            }
            mmsPartCursor.close();
        }
        unreadMmsQueryCursor.close();
        appDb.close();
        mSqlOpenHelper.close();

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
        Log.i(TAG, "onDestroy: MmsService");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
