package eu.droogers.smsmatrix;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MMSMonitor {
    private MatrixService mainActivity;
    private ContentResolver contentResolver = null;
    private Context mainContext;
    private Handler mmshandler = null;
    private ContentObserver mmsObserver = null;
    public boolean monitorStatus = false;
    private int mmsCount = 0;
    private static final String TAG = "MMSMonitor";

    public MMSMonitor(final MatrixService mainActivity, final Context mainContext) {
        this.mainActivity = mainActivity;
        contentResolver = mainActivity.getContentResolver();
        this.mainContext = mainContext;
        mmshandler = new MMSHandler();
        mmsObserver = new MMSObserver(mmshandler);
        Log.i(TAG, "***** Start MMS Monitor *****");
    }


    public void startMMSMonitoring() {
        try {
            monitorStatus = false;
            if (!monitorStatus) {
                contentResolver.registerContentObserver(
                    Uri.parse("content://mms"),
                    true,
                    mmsObserver
                );

                // Save the count of MMS messages on start-up.
                Uri uriMMSURI = Uri.parse("content://mms-sms");
                Cursor mmsCur = mainActivity.getContentResolver().query(
                    uriMMSURI,
                    null,
                    Telephony.Mms.MESSAGE_BOX + " = " + Telephony.Mms.MESSAGE_BOX_INBOX,
                    null,
                    Telephony.Mms._ID
                );
                if (mmsCur != null && mmsCur.getCount() > 0) {
                    mmsCount = mmsCur.getCount();
                    Log.d(TAG, "Init MMSCount = " + mmsCount);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    public void stopMMSMonitoring() {
        try {
            monitorStatus = false;
            if (!monitorStatus){
                contentResolver.unregisterContentObserver(mmsObserver);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    class MMSHandler extends Handler {
        public void handleMessage(final Message msg) {
            //Log.i(TAG, "Handler");
        }
    }


    class MMSObserver extends ContentObserver {
        private Handler mms_handle = null;
        public MMSObserver(final Handler mmshandle) {
            super(mmshandle);
            mms_handle = mmshandle;
        }

        public void onChange(final boolean bSelfChange) {
            super.onChange(bSelfChange);
            Log.i(TAG, "Onchange");

            try {
                monitorStatus = true;

                // Send message to Activity.
                Message msg = new Message();
                mms_handle.sendMessage(msg);

                // Get the MMS count.
                Uri uriMMSURI = Uri.parse("content://mms/");
                Cursor mmsCur = mainActivity.getContentResolver().query(
                    uriMMSURI,
                    null,
                    Telephony.Mms.MESSAGE_BOX + " = " + Telephony.Mms.MESSAGE_BOX_INBOX,
                    null,
                    Telephony.Mms._ID
                );

                int currMMSCount = 0;
                if (mmsCur != null && mmsCur.getCount() > 0) {
                    currMMSCount = mmsCur.getCount();
                }

                // Proceed if there is a new message.
                if (currMMSCount > mmsCount) {
                    mmsCount = currMMSCount;
                    mmsCur.moveToLast();

                    // Get the message id and subject.
                    String subject = mmsCur.getString(mmsCur.getColumnIndex(Telephony.Mms.SUBJECT));
                    int id = Integer.parseInt(mmsCur.getString(mmsCur.getColumnIndex(Telephony.Mms._ID)));
                    Log.d(TAG, "_id = " + id);
                    Log.d(TAG, "Subject = " + subject);

                    byte[] mediaData = null;
                    String message = "";
                    String address = "";
                    String fileName = "";
                    String fileType = "";
                    String messageType = "";

                    // Get parts.
                    Uri uriMMSPart = Uri.parse("content://mms/part");
                    Cursor curPart = mainActivity.getContentResolver().query(
                        uriMMSPart,
                        null,
                        Telephony.Mms.Part.MSG_ID + " = " + id,
                        null,
                        Telephony.Mms.Part._ID
                    );
                    Log.d(TAG, "Parts records length = " + curPart.getCount());
                    curPart.moveToLast();
                    do {
                        String contentType = curPart.getString(curPart.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));
                        String partId = curPart.getString(curPart.getColumnIndex(Telephony.Mms.Part._ID));
                        fileName = curPart.getString(curPart.getColumnIndex(Telephony.Mms.Part.NAME));
                        Log.d(TAG, "partId = " + partId);
                        Log.d(TAG, "Part mime type = " + contentType);

                        if (contentType.equalsIgnoreCase("text/plain"))
                        {
                            // Get the message.

                            Log.i(TAG,"==== Get the message start ====");
                            messageType = Matrix.MESSAGE_TYPE_TEXT;
                            byte[] messageData = readMMSPart(partId);
                            if (messageData != null && messageData.length > 0) {
                                message = new String(messageData);
                            }

                            if (message.isEmpty()) {
                                Cursor curPart1 = mainActivity.getContentResolver().query(
                                    uriMMSPart,
                                    null,
                                    Telephony.Mms.Part.MSG_ID + " = " + id + " and "+ Telephony.Mms.Part._ID + " = " + partId,
                                    null,
                                    Telephony.Mms.Part._ID
                                );
                                for (int i = 0; i < curPart1.getColumnCount(); i++)
                                {
                                    Log.d(TAG,"Column Name : " + curPart1.getColumnName(i));
                                }
                                curPart1.moveToLast();
                                message = curPart1.getString(13);
                            }
                            Log.d(TAG,"Txt Message = " + message);
                        } else if (isImageType(contentType) || isVideoType(contentType)) {
                            // Get the media.

                            if (isImageType(contentType)) {
                                messageType = Matrix.MESSAGE_TYPE_IMAGE;
                            } else if (isVideoType(contentType)) {
                                messageType = Matrix.MESSAGE_TYPE_VIDEO;
                            }
                            Log.i(TAG, "==== Get the media start ====");
                            fileType = contentType;
                            mediaData = readMMSPart(partId);
                            Log.i(TAG, "Media data length == " + mediaData.length);
                        }
                    } while (curPart.moveToPrevious());



                    // Get the sender's address.
                    Uri uriMMSAddr = Uri.parse("content://mms/" + id + "/addr");
                    Cursor addrCur = mainActivity.getContentResolver().query(
                        uriMMSAddr,
                        null,
                        Telephony.Mms.Addr.TYPE + " = 137",     // PduHeaders.FROM
                        null,
                        Telephony.Mms.Addr._ID
                    );
                    if (addrCur != null) {
                        addrCur.moveToLast();
                        do{
                            Log.d(TAG, "addrCur records length = " + addrCur.getCount());
                            if (addrCur.getCount() > 0) {
                                address = addrCur.getString(addrCur.getColumnIndex(Telephony.Mms.Addr.ADDRESS));
                            }
                            Log.d(TAG, "address = " + address);

                            if (!message.isEmpty()) {
                                Utilities.sendMatrix(mainActivity, message, address, messageType);
                            }
                            if (mediaData != null) {
                                Utilities.sendMatrix(
                                    mainActivity,
                                    mediaData,
                                    address,
                                    messageType,
                                    fileName,
                                    fileType
                                );
                            }
                        } while (addrCur.moveToPrevious());
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }


    private byte[] readMMSPart(String partId) {
        byte[] partData = null;
        Uri partURI = Uri.parse("content://mms/part/" + partId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;

        try {

            Log.i(TAG,"Entered into readMMSPart try.");
            ContentResolver mContentResolver = mainActivity.getContentResolver();
            is = mContentResolver.openInputStream(partURI);

            byte[] buffer = new byte[256];
            int len = is.read(buffer);
            while (len >= 0) {
                baos.write(buffer, 0, len);
                len = is.read(buffer);
            }
            partData = baos.toByteArray();
            //Log.i(TAG, "Text Msg  :: " + new String(partData));

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
}