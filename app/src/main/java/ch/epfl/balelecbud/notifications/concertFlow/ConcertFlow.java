package ch.epfl.balelecbud.notifications.concertFlow;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.room.Room;

import java.util.Arrays;
import java.util.function.Consumer;

import ch.epfl.balelecbud.notifications.concertFlow.objects.ConcertOfInterestDAO;
import ch.epfl.balelecbud.notifications.concertFlow.objects.ConcertOfInterestDatabase;
import ch.epfl.balelecbud.notifications.concertSoon.NotificationScheduler;
import ch.epfl.balelecbud.notifications.concertSoon.NotificationSchedulerInterface;
import ch.epfl.balelecbud.schedule.models.Slot;
import ch.epfl.balelecbud.util.intents.FlowUtil;

public class ConcertFlow extends IntentService {
    private static final String TAG = ConcertFlow.class.getSimpleName();
    private NotificationSchedulerInterface scheduler;
    private ConcertOfInterestDAO concertOfInterestDAO;
    private ConcertOfInterestDatabase db;
    private static ConcertOfInterestDatabase mockDb = null;

    public ConcertFlow() {
        super(TAG);
    }

    private Consumer<Intent> launcher = ConcertFlow.this::startActivity;

    @VisibleForTesting
    public void setLauncher(Consumer<Intent> launcher) {
        this.launcher = launcher;
    }

    @Override
    public void onHandleIntent(@Nullable Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();
        Log.d("ConcertFlow", "onHandleIntent: action = " + action);
        switch (action) {
            case FlowUtil.ACK_CONCERT:
                handleAckIntent(intent);
                break;
            case FlowUtil.SUBSCRIBE_CONCERT:
                handleSubscribeIntent(intent);
                break;
            case FlowUtil.CANCEL_CONCERT:
                handleCancelIntent(intent);
                break;
            case FlowUtil.GET_ALL_CONCERT:
                handleGetAllIntent(intent);
                break;
        }
    }

    private void handleGetAllIntent(@NonNull Intent intent) {
        Intent callbackIntent = FlowUtil.unpackIntentFromIntent(intent);
        if (callbackIntent != null)
            this.getAllScheduledConcert(callbackIntent);
    }

    private void handleCancelIntent(@NonNull Intent intent) {
        Slot s = FlowUtil.unpackSlotFromIntent(intent);
        if (s != null)
            this.removeConcert(s);
    }

    private void handleSubscribeIntent(@NonNull Intent intent) {
        Slot s = FlowUtil.unpackSlotFromIntent(intent);
        if (s != null)
            this.scheduleNewConcert(s);
    }

    private void handleAckIntent(@NonNull Intent intent) {
        this.removeConcertById(FlowUtil.unpackIdFromAckIntent(intent));
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mockDb == null)
            db.close();
    }

    @VisibleForTesting
    public static void setMockDb(ConcertOfInterestDatabase concertDb) {
        mockDb = concertDb;
    }

    @VisibleForTesting
    public void setNotificationScheduler(NotificationSchedulerInterface scheduler) {
        this.scheduler = scheduler;
    }

    private void getAllScheduledConcert(Intent callbackIntent) {
        Slot[] res = this.concertOfInterestDAO.getAllConcertOfInterest();
        Log.d(TAG, "getAllScheduledConcert: " + Arrays.toString(res));
        this.launcher.accept(FlowUtil.packCallback(res, callbackIntent));
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
