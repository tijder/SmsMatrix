package eu.droogers.smsmatrix;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlOpenHelper extends SQLiteOpenHelper {
    public  static final String DATABASE_NAME = "SmsMatrix.db";
    public static final int DATABASE_VERSION = 1;


    public SqlOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.SmsEntry.SQL_CREATE_TABLE);
        db.execSQL(DatabaseContract.MmsEntry.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
