package eu.droogers.smsmatrix;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class dbManager {
    private static dbManager ourInsance = null;
    private static final String TAG = "dbManager";
    public static final int TYPE_INCOMING = 1;
    public static final int TYPE_OUTGOING = 2;
    public static final int TYPE_RESYNC = 3;
    public static final int BRIDGED_NO = 0;
    public static final int BRIDGED_YES = 1;
    public static final int BRIDGED_INIT = 2;
    public static final int LOCK_NO = 0;
    public static final int LOCK_YES = 1;
    public static final int ERROR_NO = 0;
    public static final int EPOCH_NO = 0;


    private static final String[] ALLColumns = new String[] { "*" };


    public static dbManager getInstance() {
        if(ourInsance == null) {
            ourInsance = new dbManager();
        }
        return ourInsance;
    }

    public static long dumpSmses(SqlOpenHelper dbHelper) {
        Log.i(TAG, "dumpSmses");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor smsQueryCursor = db.query(DatabaseContract.SmsEntry.TABLE_NAME, ALLColumns, null, null, null, null, null);
        long smsCount=smsQueryCursor.getCount();
        Log.i(TAG, "Heres what's in the cursor " + DatabaseUtils.dumpCursorToString(smsQueryCursor)  );
        smsQueryCursor.close();
        db.close();

        Log.i(TAG, "dumpSmses is done");
        return smsCount;
    }

    public static long dumpMmses(SqlOpenHelper dbHelper) {
        Log.i(TAG, "dumpMmses");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor mmsQueryCursor = db.query(DatabaseContract.MmsEntry.TABLE_NAME, ALLColumns, null, null, null, null, null);
        Log.i(TAG, "Heres what's in the cursor " + DatabaseUtils.dumpCursorToString(mmsQueryCursor)  );
        long mmsCount = mmsQueryCursor.getCount();
        mmsQueryCursor.close();
        db.close();

        Log.i(TAG, "dumpMmses is done");
        return mmsCount;
    }

    public static long countMmses(SqlOpenHelper dbHelper) {
        Log.i(TAG, "countMmses");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor mmsQueryCursor = db.query(DatabaseContract.MmsEntry.TABLE_NAME, ALLColumns, null, null, null, null, null);
        long mmsCount = mmsQueryCursor.getCount();
        mmsQueryCursor.close();
        db.close();

        Log.i(TAG, "countMmses is done");
        return mmsCount;
    }

    public static long importNewSms(SqlOpenHelper dbHelper, int type, long sourceEpoch, long recievedEpoch, String number, String message, int bridged, int lock, int error) {
        Log.i(TAG, "importNewSms");
        long smsImportRowId = -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SmsEntry.COLUMN_TYPE, type);
        values.put(DatabaseContract.SmsEntry.COLUMN_SOURCE_EPOCH, sourceEpoch);
        values.put(DatabaseContract.SmsEntry.COLUMN_RECIEVED_EPOCH, recievedEpoch);
        values.put(DatabaseContract.SmsEntry.COLUMN_NUMBER, number);
        values.put(DatabaseContract.SmsEntry.COLUMN_MESSAGE, message);
        values.put(DatabaseContract.SmsEntry.COLUMN_BRIDGED, bridged);
        values.put(DatabaseContract.SmsEntry.COLUMN_LOCK, lock);
        values.put(DatabaseContract.SmsEntry.COLUMN_ERROR, error);
        try {
            smsImportRowId = db.insert(DatabaseContract.SmsEntry.TABLE_NAME, null, values);
            Log.i(TAG, "importNewSms  " + smsImportRowId  );
        } catch (Exception e) {
            Log.e(TAG, "importNewSms failed  " + e  );
            smsImportRowId = -1;
        } finally {
            db.close();
            return smsImportRowId;
        }
    }

    public static void updateLongSmsBody (SqlOpenHelper dbHelper, long id, String body ){
        Log.i(TAG, "updateLongSmsBody");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.SmsEntry.COLUMN_MESSAGE, body);
        try {
            Log.i(TAG, " try updateLongSmsBody _id=" + id  );
            db.update(DatabaseContract.SmsEntry.TABLE_NAME, values, "_id=" + id, null );
        } catch (Exception e) {
            Log.e(TAG, "updateLongSmsBody failed  " + e  );
        } finally {
            db.close();
        }
    }

    public static long importNewMms(SqlOpenHelper dbHelper, int type, long sourceEpoch, long recievedEpoch, String number, long mmsId, int bridged, int lock, int error) {
        Log.i(TAG, "importNewMms");
        long mmsImportRowId = -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MmsEntry.COLUMN_TYPE, type);
        values.put(DatabaseContract.MmsEntry.COLUMN_SOURCE_EPOCH, sourceEpoch);
        values.put(DatabaseContract.MmsEntry.COLUMN_RECIEVED_EPOCH, recievedEpoch);
        values.put(DatabaseContract.MmsEntry.COLUMN_NUMBER, number);
        values.put(DatabaseContract.MmsEntry.COLUMN_MMS_ID, mmsId);
        values.put(DatabaseContract.MmsEntry.COLUMN_BRIDGED, bridged);
        values.put(DatabaseContract.MmsEntry.COLUMN_LOCK, lock);
        values.put(DatabaseContract.MmsEntry.COLUMN_ERROR, error);
        try {
            mmsImportRowId = db.insert(DatabaseContract.MmsEntry.TABLE_NAME, null, values);
            Log.i(TAG, "importNewMms  " + mmsImportRowId  );
        } catch (Exception e) {
            Log.e(TAG, "importNewMms failed  " + e  );
            mmsImportRowId = -1;
        } finally {
            db.close();
            return mmsImportRowId;
        }
    }

    public static long lastMmsId(SqlOpenHelper dbHelper) {
        Log.i(TAG, "lastMmsId");
        int mmsId = 0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor mmsQueryCursor = db.query(DatabaseContract.MmsEntry.TABLE_NAME, ALLColumns, null, null, null, null, "mms_id DESC");
        mmsQueryCursor.moveToFirst();
        mmsId = mmsQueryCursor.getInt(mmsQueryCursor.getColumnIndex("mms_id"));
        mmsQueryCursor.close();
        db.close();
        Log.i(TAG, "lastMmsId is done");
        return mmsId;
    }


    public static void markMmsRead (SqlOpenHelper dbHelper, long id ){
        Log.i(TAG, "markMmsRead");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MmsEntry.COLUMN_BRIDGED, 1);
        try {
            Log.i(TAG, " try updateLongSmsBody _id=" + id  );
            db.update(DatabaseContract.MmsEntry.TABLE_NAME, values, "_id=" + id, null );
        } catch (Exception e) {
            Log.e(TAG, "updateLongSmsBody failed  " + e  );
        } finally {
            db.close();
        }
    }

}
