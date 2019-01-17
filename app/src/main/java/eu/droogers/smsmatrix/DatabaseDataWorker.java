package eu.droogers.smsmatrix;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;


public class DatabaseDataWorker {
    private SQLiteDatabase mDb;
    public DatabaseDataWorker(SQLiteDatabase db) {
        mDb = db;
    }



    private void insertSms(int address, int date, int dateSent, int read, int type, int status, String body, int locked, int errorCode) {
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

        long newRowId = mDb.insert(DatabaseContract.SmsEntry.TABLE_NAME, null, values);
    }

    private void insertMms(String tr_id, int address, int date, int dateSent, int read, int type, int mmsId, int status, String body, int locked, int errorCode) {
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

        long newRowId = mDb.insert(DatabaseContract.MmsEntry.TABLE_NAME, null, values);
    }



}
