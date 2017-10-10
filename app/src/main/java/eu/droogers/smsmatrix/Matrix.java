package eu.droogers.smsmatrix;

import android.content.Context;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.gson.JsonElement;

import org.matrix.androidsdk.HomeServerConnectionConfig;
import org.matrix.androidsdk.MXDataHandler;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.store.IMXStore;
import org.matrix.androidsdk.data.store.IMXStoreListener;
import org.matrix.androidsdk.data.store.MXFileStore;
import org.matrix.androidsdk.data.store.MXMemoryStore;
import org.matrix.androidsdk.listeners.IMXEventListener;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;
import org.matrix.androidsdk.rest.client.LoginRestClient;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.Message;
import org.matrix.androidsdk.rest.model.login.Credentials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by gerben on 6-10-17.
 */

public class Matrix {
    HomeServerConnectionConfig hsConfig;
    Context context;
    MXSession session;
    int transaction;
    private String tag = "Matrix";
    private List<NotSendMesage> notSendMesages = new ArrayList<>();
    MXDataHandler dh;
    private IMXEventListener evLis;
    IMXStore store;
    String deviceName;

    private String realUserid;

    public Matrix (final Context context, String url, String botUsername, String botPassword, String username, String device) {
        this.context = context;
        hsConfig = new HomeServerConnectionConfig(Uri.parse(url));

        realUserid = username;
        deviceName = device;

        login(botUsername, botPassword);
    }

    private void login(String username, String password) {
        new LoginRestClient(hsConfig).loginWithUser(username, password, deviceName, new SimpleApiCallback<Credentials>() {

            @Override
            public void onSuccess(Credentials credentials) {
                super.onSuccess(credentials);
                onLogin(credentials);
            }
        });
    }

    private void onLogin(Credentials credentials) {
        hsConfig.setCredentials(credentials);
        startEventStream();
    }

    public void startEventStream() {
        evLis = new EventListener(this);


        if (false) {
            store = new MXFileStore(hsConfig, context);
        } else {
            store = new MXMemoryStore(hsConfig.getCredentials(), context);
        }

        dh = new MXDataHandler(store, hsConfig.getCredentials());

//        NetworkConnectivityReceiver nwMan = new NetworkConnectivityReceiver();

        session = new MXSession(hsConfig, dh, context);
        session.setSyncDelay(12000);
        session.setSyncTimeout(30*60*1000);
        Log.e(TAG, "onLogin:" + session.getSyncTimeout());



        if (store.isReady()) {
            session.getDataHandler().checkPermanentStorageData();
            session.startEventStream(store.getEventStreamToken());
            session.getDataHandler().addListener(evLis);
        } else {
            store.addMXStoreListener(new IMXStoreListener() {
                @Override
                public void postProcess(String s) {

                }

                @Override
                public void onStoreReady(String s) {
                    session.getDataHandler().checkPermanentStorageData();
                    session.startEventStream(store.getEventStreamToken());
                    session.getDataHandler().addListener(evLis);
                }

                @Override
                public void onStoreCorrupted(String s, String s1) {
                    Log.e(TAG, "onStoreCorrupted: " + s  );
                }

                @Override
                public void onStoreOOM(String s, String s1) {

                }

                @Override
                public void onReadReceiptsLoaded(String s) {

                }
            });
        }
    }

    public void sendMessage(final String phoneNumber, final String body) {
        if (session != null && session.isAlive()) {
            Room room = getRoomByPhonenumber(phoneNumber);
            if (room == null) {
                Log.e(TAG, "sendMessage: not found" );
                session.createRoomDirectMessage(realUserid, new SimpleApiCallback<String>() {
                    @Override
                    public void onSuccess(String info) {
                        super.onSuccess(info);
                        session.getRoomsApiClient().updateTopic(info, phoneNumber, new SimpleApiCallback<Void>());
                        SendMesageToRoom(store.getRoom(info), body);
                    }
                });
            } else {
                SendMesageToRoom(room, body);
            }
        } else {
            Log.e(tag, "Error with sending message");
            notSendMesages.add(new NotSendMesage(phoneNumber, body));
        }
    }

    public void SendMesageToRoom(Room room, String body) {
        Message msg = new Message();
        msg.body = body;
        msg.msgtype = "m.mesage";
        session.getRoomsApiClient().sendMessage(String.valueOf(transaction), room.getRoomId(), msg, new SimpleApiCallback<Event>());
        transaction++;
    }

    public void getUnreadEvents() {
        List<String> types = new ArrayList<>();
        types.add("m.room.message");

        Collection<Room> rooms = store.getRooms();
        for (Room room : rooms) {
            List<Event> roomsDirect = store.unreadEvents(room.getRoomId(), types);
            for (Event event : roomsDirect) {
                sendEvent(event);
            }
        }
    }

    public void sendEvent(Event event) {
        if (event.sender.equals(realUserid)) {
            Room room = store.getRoom(event.roomId);
            JsonElement json = event.getContent();
            String body = json.getAsJsonObject().get("body").getAsString();
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(room.getTopic(), null, body, null, null);
            room.markAllAsRead(new SimpleApiCallback<Void>());
        }
    }


    public void onEventStreamLoaded() {
        sendMessageList(notSendMesages);
        notSendMesages.clear();
    }

    public void sendMessageList(List<NotSendMesage> messages) {
        for (NotSendMesage ms : messages) {
            sendMessage(ms.getPhone(), ms.getBody());
        }
    }

    public Room getRoomByPhonenumber (String number) {
        Collection<Room> rooms = store.getRooms();
        Log.e(TAG, "getRoomByPhonenumber: " + number );
        Log.e(TAG, "getRoomByPhonenumber: " + rooms.size() );
        for (Room room : rooms) {
            Log.e(TAG, "getRoomByPhonenumber: " + room.getTopic() );
            if (room.getTopic() != null && room.getTopic().equals(number)) {
                return room;
            }
        }
        return null;
    }

    public void destroy() {
        session.stopEventStream();
        dh.removeListener(evLis);
        store.close();
    }
}
