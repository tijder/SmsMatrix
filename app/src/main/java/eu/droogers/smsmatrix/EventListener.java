package eu.droogers.smsmatrix;

import android.util.Log;

import org.matrix.androidsdk.data.MyUser;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.listeners.IMXEventListener;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.core.model.MatrixError;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.rest.model.bingrules.BingRule;
import org.matrix.androidsdk.rest.model.sync.AccountDataElement;

import java.util.List;

/**
 * Created by gerben on 8-10-17.
 */

public class EventListener implements IMXEventListener {
    private static final String TAG = "EventListener";
    private boolean loaded = false;
    private Matrix mx;

    public EventListener (Matrix mx) {
        this.mx = mx;
    }

    @Override
    public void onStoreReady() {

    }

    @Override
    public void onPresenceUpdate(Event event, User user) {

    }

    @Override
    public void onAccountInfoUpdate(MyUser myUser) {

    }

    @Override
    public void onIgnoredUsersListUpdate() {

    }

    @Override
    public void onDirectMessageChatRoomsListUpdate() {

    }

    @Override
    public void onLiveEvent(Event event, RoomState roomState) {
        if (loaded) {
//            mx.getUnreadEvents();
            mx.sendEvent(event);
        }
        Log.e(TAG, "onLiveEvent: " + event);
    }

    @Override
    public void onLiveEventsChunkProcessed(String s, String s1) {

    }

    @Override
    public void onBingEvent(Event event, RoomState roomState, BingRule bingRule) {

    }

    @Override
    public void onEventSentStateUpdated(Event event) {

    }

    @Override
    public void onEventSent(Event event, String s) {

    }

    @Override
    public void onEventDecrypted(String s, String s1) {

    }

    @Override
    public void onBingRulesUpdate() {

    }

    @Override
    public void onInitialSyncComplete(String s) {
        loaded = true;
        mx.onEventStreamLoaded();
        mx.getUnreadEvents();
    }

    @Override
    public void onSyncError(MatrixError matrixError) {

    }

    @Override
    public void onCryptoSyncComplete() {

    }

    @Override
    public void onNewRoom(String s) {

    }

    @Override
    public void onJoinRoom(String s) {

    }

    @Override
    public void onRoomFlush(String s) {

    }

    @Override
    public void onRoomInternalUpdate(String s) {

    }

    @Override
    public void onNotificationCountUpdate(String s) {

    }

    @Override
    public void onLeaveRoom(String s) {

    }

    @Override
    public void onRoomKick(String s) {

    }

    @Override
    public void onReceiptEvent(String s, List<String> list) {

    }

    @Override
    public void onRoomTagEvent(String s) {

    }

    @Override
    public void onReadMarkerEvent(String s) {

    }

    @Override
    public void onToDeviceEvent(Event event) {

    }

    @Override
    public void onNewGroupInvitation(String s) {

    }

    @Override
    public void onJoinGroup(String s) {

    }

    @Override
    public void onLeaveGroup(String s) {

    }

    @Override
    public void onGroupProfileUpdate(String s) {

    }

    @Override
    public void onGroupRoomsListUpdate(String s) {

    }

    @Override
    public void onGroupUsersListUpdate(String s) {

    }

    @Override
    public void onGroupInvitedUsersListUpdate(String s) {

    }

    @Override
    public void onAccountDataUpdated(AccountDataElement accountDataElement) {

    }
}
