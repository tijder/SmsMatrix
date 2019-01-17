package eu.droogers.smsmatrix;

import android.provider.BaseColumns;

public final class DatabaseContract {
    private DatabaseContract () {}

    public static final class SmsEntry implements BaseColumns {
        public static final String TABLE_NAME = "sms";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DATE_SENT = "date_sent";
        public static final String COLUMN_READ = "read";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_BODY = "body";
        public static final String COLUMN_LOCKED = "locked";
        public static final String COLUMN_ERROR_CODE = "error_code";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_ADDRESS + " TEXT NOT NULL, " +
                        COLUMN_DATE + " INTEGER NOT NULL, " +
                        COLUMN_DATE_SENT + " INTEGER NOT NULL, " +
                        COLUMN_READ + " INTEGER NOT NULL, " +
                        COLUMN_STATUS + " INTEGER NOT NULL, " +
                        COLUMN_TYPE + " INTEGER NOT NULL, " +
                        COLUMN_BODY + " TEXT NOT NULL, " +
                        COLUMN_LOCKED + " INTEGER, " +
                        COLUMN_ERROR_CODE + " INTEGER )";


    }

    public static final class MmsEntry implements BaseColumns {
        public static final String TABLE_NAME = "mms";
        public static final String COLUMN_TR_ID = "tr_id";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DATE_SENT = "date_sent";
        public static final String COLUMN_READ = "read";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_MMS_ID = "mms_id";
        public static final String COLUMN_BODY = "body";
        public static final String COLUMN_LOCKED = "locked";
        public static final String COLUMN_ERROR_CODE = "error_code";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_TR_ID + " TEXT NOT NULL, " +
                        COLUMN_ADDRESS + " TEXT NOT NULL, " +
                        COLUMN_DATE + " INTEGER NOT NULL, " +
                        COLUMN_DATE_SENT + " INTEGER NOT NULL, " +
                        COLUMN_READ + " INTEGER NOT NULL, " +
                        COLUMN_STATUS + " INTEGER NOT NULL, " +
                        COLUMN_TYPE + " INTEGER NOT NULL, " +
                        COLUMN_MMS_ID + " INTEGER NOT NULL, " +
                        COLUMN_BODY + " TEXT, " +
                        COLUMN_LOCKED + " INTEGER, " +
                        COLUMN_ERROR_CODE + " INTEGER )";
    }



}
