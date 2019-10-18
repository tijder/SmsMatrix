package eu.droogers.smsmatrix;

import android.provider.BaseColumns;

public class DatabaseContract {

    private DatabaseContract () {}

    public static final class SmsEntry implements BaseColumns {
        public static final String TABLE_NAME = "sms";
        public static final String COLUMN_TYPE = "type";
        //matrix -> cell=1 ; cell -> matrix = 2
        public static final String COLUMN_SOURCE_EPOCH = "source_epoch";
        //epoch in ms from sending party
        public static final String COLUMN_RECIEVED_EPOCH = "received_epoch";
        //epoch in ms when the device on onRecieve
        public static final String COLUMN_NUMBER = "number";
        //Who the message is from
        public static final String COLUMN_MESSAGE = "message";
        //Body of the message
        public static final String COLUMN_BRIDGED = "bridged";
        //Was it sent successfully?
        public static final String COLUMN_LOCK = "lock";
        //Are we doing something with this message?
        public static final String COLUMN_ERROR = "error";
        //Was there an error doing something?

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_TYPE + " INTEGER NOT NULL, " +
                        COLUMN_SOURCE_EPOCH + " INTEGER, " +
                        COLUMN_RECIEVED_EPOCH + " INTEGER NOT NULL, " +
                        COLUMN_NUMBER + " TEXT NOT NULL, " +
                        COLUMN_MESSAGE + " TEXT NOT NULL, " +
                        COLUMN_BRIDGED + " INTEGER NOT NULL, " +
                        COLUMN_LOCK + " INTEGER NOT NULL, " +
                        COLUMN_ERROR + " INTEGER NOT NULL )";

    }

    public static final class MmsEntry implements BaseColumns {
        public static final String TABLE_NAME = "mms";
        public static final String COLUMN_TYPE = "type";
        //matrix -> cell=1 ; cell -> matrix = 2
        public static final String COLUMN_SOURCE_EPOCH = "source_epoch";
        //epoch in ms from sending party
        public static final String COLUMN_RECIEVED_EPOCH = "received_epoch";
        //epoch in ms when the device on onRecieve
        public static final String COLUMN_NUMBER = "number";
        //Who the message is from
        public static final String COLUMN_MMS_ID = "mms_id";
        //ID of mmssms.db - addr
        public static final String COLUMN_BRIDGED = "bridged";
        //Was it sent successfully?
        public static final String COLUMN_LOCK = "lock";
        //Are we doing something with this message?
        public static final String COLUMN_ERROR = "error";
        //Was there an error doing something?


        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_TYPE + " INTEGER NOT NULL, " +
                        COLUMN_SOURCE_EPOCH + " INTEGER, " +
                        COLUMN_RECIEVED_EPOCH + " INTEGER NOT NULL, " +
                        COLUMN_NUMBER + " TEXT NOT NULL, " +
                        COLUMN_MMS_ID + " INTEGER UNIQUE NOT NULL, " +
                        COLUMN_BRIDGED + " INTEGER NOT NULL, " +
                        COLUMN_LOCK + " INTEGER, " +
                        COLUMN_ERROR + " INTEGER )";
    }

}
