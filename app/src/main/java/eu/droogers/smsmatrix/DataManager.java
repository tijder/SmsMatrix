package eu.droogers.smsmatrix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static eu.droogers.smsmatrix.DatabaseContract.SmsEntry;
import static eu.droogers.smsmatrix.DatabaseContract.MmsEntry;

public class DataManager {

    private static DataManager ourInsance = null;
    private static final String LOG_TAG = "UntidyLamp.DataManager";
    public static final String[] ALLColumns = new String[] { "*" };
    public static final String unread = "locked=0 AND read=0";
    public static final String mmsTrIdError = "locked=1 AND read=0 AND error_code=1 AND mms_id=0";
    //final String[] whatColumns = {SmsEntry.COLUMN_ADDRESS};
    public static final int TYPE_RECIEVED = 2; //2 is recieved (1 is sent)
    public static final int NOT_READ = 0; //messages has not been read yet
    public static final int IS_READ = 1; //message has been read
    public static final int NO_STATUS = -1; //No status yet
    public static final int NOT_LOCKED = 0; //its not locked
    public static final int LOCKED = 1; //its locked
    public static final int ERROR_NO = 0;  //no error yet
    public static final int ERROR_onUnexpectedError = 1;
    public static final int ERROR_onMatrixError = 2;
    public static final int ERROR_onNetworkError = 3;

    public static final int MMS_TR_ID_ERROR = 1; // cant find tr_id from onRecieve in mmssms.db

    public static DataManager getInstance() {
        if(ourInsance == null) {
            ourInsance = new DataManager();
        }
        return ourInsance;
    }

    public static int dumpSmses(OpenHelper dbHelper) {
        Log.i(LOG_TAG, "dumpSmses");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor smsQueryCursor = db.query(DatabaseContract.SmsEntry.TABLE_NAME, ALLColumns, null, null, null, null, null);
        Log.i(LOG_TAG, "Heres what's in the cursor " + DatabaseUtils.dumpCursorToString(smsQueryCursor)  );
        int smsesCount = smsQueryCursor.getCount();
        smsQueryCursor.close();
        Log.i(LOG_TAG, "dumpSmses is done");
        return smsesCount;
    }


    public static boolean markSmsRead(OpenHelper dbHelper, int smsId) {
        Log.i(LOG_TAG, "markSmsRead called");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SmsEntry.COLUMN_READ, IS_READ);
        try {
            db.update(DatabaseContract.SmsEntry.TABLE_NAME, values, "_id=" + smsId, null );
            db.close();
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "markSmsRead failed  " + e  );
            db.close();
            return false;
        }
    }

    public static boolean lockSms(OpenHelper dbHelper, int smsId) {
        Log.i(LOG_TAG, "lockSms called");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SmsEntry.COLUMN_LOCKED, LOCKED);
        try {
            db.update(DatabaseContract.SmsEntry.TABLE_NAME, values, "_id=" + smsId, null );
            db.close();
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "lockSms failed  " + e  );
            db.close();
            return false;
        }
    }

    public static boolean unlockSms(OpenHelper dbHelper, int smsId) {
        Log.i(LOG_TAG, "unlockSms called");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SmsEntry.COLUMN_LOCKED, NOT_LOCKED);
        try {
            db.update(DatabaseContract.SmsEntry.TABLE_NAME, values, "_id=" + smsId, null );
            db.close();
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "unlockSms failed  " + e  );
            db.close();
            return false;
        }
    }

    public static boolean errorSms(OpenHelper dbHelper, int smsId, int error) {
        Log.i(LOG_TAG, "errorSms called");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SmsEntry.COLUMN_ERROR_CODE, error);
        try {
            db.update(DatabaseContract.SmsEntry.TABLE_NAME, values, "_id=" + smsId, null );
            db.close();
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "errorSms failed  " + e  );
            db.close();
            return false;
        }
    }


    public static int dumpMmses(OpenHelper dbHelper) {
        Log.i(LOG_TAG, "dumpMmses");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor mmsQueryCursor = db.query(DatabaseContract.MmsEntry.TABLE_NAME, ALLColumns, null, null, null, null, null);
        Log.i(LOG_TAG, "Heres what's in the cursor " + DatabaseUtils.dumpCursorToString(mmsQueryCursor)  );
        int mmsesCount = mmsQueryCursor.getCount();
        mmsQueryCursor.close();
        db.close();
        Log.i(LOG_TAG, "dumpMmses is done");
        return mmsesCount;
    }


    public static boolean markMmsRead(OpenHelper dbHelper, int mmsId) {
        Log.i(LOG_TAG, "markMmsRead called");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MmsEntry.COLUMN_READ, IS_READ);
        try {
            db.update(DatabaseContract.MmsEntry.TABLE_NAME, values, "_id=" + mmsId, null );
            db.close();
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "markMmsRead failed  " + e  );
            db.close();
            return false;
        }

    }

    public static boolean lockMms(OpenHelper dbHelper, int mmsId) {
        Log.i(LOG_TAG, "lockMms called");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MmsEntry.COLUMN_LOCKED, LOCKED);
        try {
            db.update(DatabaseContract.MmsEntry.TABLE_NAME, values, "_id=" + mmsId, null );
            db.close();
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "lockMms failed  " + e  );
            db.close();
            return false;
        }
    }

    public static boolean unlockMms(OpenHelper dbHelper, int mmsId) {
        Log.i(LOG_TAG, "unlockMms called");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MmsEntry.COLUMN_LOCKED, NOT_LOCKED);
        try {
            db.update(DatabaseContract.MmsEntry.TABLE_NAME, values, "_id=" + mmsId, null );
            db.close();
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "unlockMms failed  " + e  );
            db.close();
            return false;
        }
    }

    public static boolean errorMms(OpenHelper dbHelper, int mmsId, int error) {
        Log.i(LOG_TAG, "errorMms called");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MmsEntry.COLUMN_ERROR_CODE, error);
        try {
            db.update(DatabaseContract.MmsEntry.TABLE_NAME, values, "_id=" + mmsId, null );
            db.close();
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "errorMms failed  " + e  );
            db.close();
            return false;
        }
    }

    public static int countSmses(OpenHelper dbHelper) {
        Log.i(LOG_TAG, "countSmses");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor countSmsesCursor = db.query(DatabaseContract.SmsEntry.TABLE_NAME, ALLColumns, null, null, null, null, null);
        int smsesCount = countSmsesCursor.getCount();
        countSmsesCursor.close();
        db.close();
        Log.i(LOG_TAG, "countSmses is done");
        return smsesCount;
    }

    public static int countMmses(OpenHelper dbHelper) {
        Log.i(LOG_TAG, "countMmses");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor countMmsesCursor = db.query(DatabaseContract.MmsEntry.TABLE_NAME, ALLColumns, null, null, null, null, null);
        int mmsesCount = countMmsesCursor.getCount();
        countMmsesCursor.close();
        db.close();
        Log.i(LOG_TAG, "countMmses is done");
        return mmsesCount;
    }

    public static long importSms(OpenHelper dbHelper, String address, long date, long dateSent, int read, int type, int status, String body, int locked, int errorCode) {
        Log.i(LOG_TAG, "importSms");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SmsEntry.COLUMN_ADDRESS, address);
        values.put(DatabaseContract.SmsEntry.COLUMN_DATE, date);
        values.put(DatabaseContract.SmsEntry.COLUMN_DATE_SENT, dateSent);
        values.put(DatabaseContract.SmsEntry.COLUMN_READ, read);
        values.put(DatabaseContract.SmsEntry.COLUMN_STATUS, status);
        values.put(DatabaseContract.SmsEntry.COLUMN_TYPE, type);
        values.put(DatabaseContract.SmsEntry.COLUMN_BODY, body);
        values.put(DatabaseContract.SmsEntry.COLUMN_LOCKED, locked);
        values.put(DatabaseContract.SmsEntry.COLUMN_ERROR_CODE, errorCode);
        try {
            long smsImportRowId = db.insert(DatabaseContract.SmsEntry.TABLE_NAME, null, values);
            Log.i(LOG_TAG, "smsImportRowId  " + smsImportRowId  );
            db.close();
            return smsImportRowId;
        } catch (Exception e) {
            Log.e(LOG_TAG, "importSms failed  " + e  );
            db.close();
            return -1;
        }
    }

    public static void updateLongSmsBody (OpenHelper dbHelper, long id, String body ){

        Log.i(LOG_TAG, "updateLongSmsBody");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SmsEntry.COLUMN_BODY, body);

        try {
            Log.i(LOG_TAG, " try updateLongSmsBody _id=" + id  );
            db.update(DatabaseContract.SmsEntry.TABLE_NAME, values, "_id=" + id, null );
        } catch (Exception e) {
            Log.e(LOG_TAG, "updateLongSmsBody failed  " + e  );
        }
        db.close();
    }


    public static long importMms(OpenHelper dbHelper, String tr_id, String address, long date, long dateSent, int read, int type, int mmsId, int status, String body, int locked, int errorCode) {
        Log.i(LOG_TAG, "importMms");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MmsEntry.COLUMN_TR_ID, tr_id);
        values.put(DatabaseContract.MmsEntry.COLUMN_ADDRESS, address);
        values.put(DatabaseContract.MmsEntry.COLUMN_DATE, date);
        values.put(DatabaseContract.MmsEntry.COLUMN_DATE_SENT, dateSent);
        values.put(DatabaseContract.MmsEntry.COLUMN_READ, read);
        values.put(DatabaseContract.MmsEntry.COLUMN_STATUS, status);
        values.put(DatabaseContract.MmsEntry.COLUMN_TYPE, type);
        values.put(DatabaseContract.MmsEntry.COLUMN_MMS_ID, mmsId);
        values.put(DatabaseContract.MmsEntry.COLUMN_BODY, body);
        values.put(DatabaseContract.MmsEntry.COLUMN_LOCKED, locked);
        values.put(DatabaseContract.MmsEntry.COLUMN_ERROR_CODE, errorCode);
        try {
            long mmsImportRowId = db.insert(DatabaseContract.MmsEntry.TABLE_NAME, null, values);
            Log.i(LOG_TAG, "smsImportRowId  " + mmsImportRowId  );
            db.close();
            return mmsImportRowId;
        } catch (Exception e) {
            db.close();
            return -1;
        }
    }

    public static void updateFailedMms (OpenHelper dbHelper, int id, int mmsId, String body, int locked, int errorCode ){
        Log.i(LOG_TAG, "updateFailedMms");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MmsEntry.COLUMN_MMS_ID, mmsId);
        values.put(DatabaseContract.MmsEntry.COLUMN_BODY, body);
        values.put(DatabaseContract.MmsEntry.COLUMN_LOCKED, locked);
        values.put(DatabaseContract.MmsEntry.COLUMN_ERROR_CODE, errorCode);

        try {
            Log.i(LOG_TAG, " try updateFailedMms _id=" + id  );
            db.update(DatabaseContract.MmsEntry.TABLE_NAME, values, "_id=" + id, null );
        } catch (Exception e) {
            Log.e(LOG_TAG, "updateFailedMms _id="+ id + "  failed:  " + e  );
        }
        db.close();
    }

    public static boolean deleteAll (OpenHelper dbHelper) {
        return false;
    }

}
