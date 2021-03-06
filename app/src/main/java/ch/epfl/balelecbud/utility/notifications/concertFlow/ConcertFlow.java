package ch.epfl.balelecbud.utility.notifications.concertFlow;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.room.Room;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Consumer;

import ch.epfl.balelecbud.model.Slot;
import ch.epfl.balelecbud.utility.FlowUtils;
import ch.epfl.balelecbud.utility.notifications.NotificationInterface;
import ch.epfl.balelecbud.utility.notifications.concertFlow.objects.ConcertOfInterestDAO;
import ch.epfl.balelecbud.utility.notifications.concertFlow.objects.ConcertOfInterestDatabase;
import ch.epfl.balelecbud.utility.notifications.concertSoon.NotificationScheduler;

/**
 * Service handling the flow of the subscribed concerts
 */
public final class ConcertFlow extends IntentService {
    private static final String TAG = ConcertFlow.class.getSimpleName();
    private static NotificationInterface scheduler;
    private static ConcertOfInterestDatabase mockDb = null;
    private static Consumer<Intent> launcher = null;
    private ConcertOfInterestDAO concertOfInterestDAO;
    private ConcertOfInterestDatabase db;

    public ConcertFlow() {
        super(TAG);
    }

    @VisibleForTesting
    public static void setMockDb(ConcertOfInterestDatabase concertDb) {
        ConcertFlow.mockDb = concertDb;
    }

    @VisibleForTesting
    public static void setNotificationScheduler(NotificationInterface scheduler) {
        ConcertFlow.scheduler = scheduler;
    }

    @VisibleForTesting
    public static void setLauncher(Consumer<Intent> launcher) {
        ConcertFlow.launcher = launcher;
    }

    @Override
    public void onHandleIntent(@Nullable Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();
        Log.d("ConcertFlow", "onHandleIntent: action = " + action);
        switch (action) {
            case FlowUtils.ACK_CONCERT:
                handleAckIntent(intent);
                break;
            case FlowUtils.SUBSCRIBE_CONCERT:
                handleSubscribeIntent(intent);
                break;
            case FlowUtils.CANCEL_CONCERT:
                handleCancelIntent(intent);
                break;
            case FlowUtils.GET_ALL_CONCERT:
                handleGetAllIntent(intent);
                break;
        }
    }

    private void handleGetAllIntent(@NonNull Intent intent) {
        Intent callbackIntent = FlowUtils.unpackIntentFromIntent(intent);
        if (callbackIntent != null)
            this.getAllScheduledConcert(callbackIntent);
    }

    private void handleCancelIntent(@NonNull Intent intent) {
        Slot s = FlowUtils.unpackSlotFromIntent(intent);
        if (s != null)
            this.removeConcert(s);
    }

    private void handleSubscribeIntent(@NonNull Intent intent) {
        Slot s = FlowUtils.unpackSlotFromIntent(intent);
        if (s != null)
            this.scheduleNewConcert(s);
    }

    private void handleAckIntent(@NonNull Intent intent) {
        this.removeConcertById(FlowUtils.unpackIdFromAckIntent(intent));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        if (mockDb == null)
            db = Room.databaseBuilder(this,
                    ConcertOfInterestDatabase.class, "ConcertsOfInterest").build();
        else
            db = mockDb;
        concertOfInterestDAO = db.getConcertOfInterestDAO();
        if (scheduler == null)
            scheduler = NotificationScheduler.getInstance();
        if (launcher == null)
            launcher = this::startActivity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mockDb == null)
            db.close();
    }

    private void getAllScheduledConcert(Intent callbackIntent) {
        List<Slot> res = this.concertOfInterestDAO.getAllConcertOfInterestList();
        Log.d(TAG, "getAllScheduledConcert: " + res.toString());
        launcher.accept(FlowUtils.packCallback(Lists.newArrayList(res), callbackIntent));
    }

    private void scheduleNewConcert(final Slot newSlot) {
        if (!concertOfInterestDAO.getAllConcertOfInterestList().contains(newSlot)) {
            concertOfInterestDAO.insertConcert(newSlot);
        }
        scheduler.scheduleNotification(ConcertFlow.this, newSlot);
    }

    private void removeConcert(final Slot slot) {
        concertOfInterestDAO.removeConcert(slot);
        scheduler.cancelNotification(this, slot);
    }

    private void removeConcertById(int id) {
        this.concertOfInterestDAO.removeConcertById(id);
    }
}
