package ch.epfl.balelecbud.utility.notifications.concertSoon;

import android.app.PendingIntent;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.google.android.gms.location.LocationRequest;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import ch.epfl.balelecbud.BalelecbudApplication;
import ch.epfl.balelecbud.R;
import ch.epfl.balelecbud.RootActivity;
import ch.epfl.balelecbud.model.Slot;
import ch.epfl.balelecbud.utility.authentication.MockAuthenticator;
import ch.epfl.balelecbud.utility.database.MockDatabase;
import ch.epfl.balelecbud.utility.location.LocationClient;
import ch.epfl.balelecbud.utility.location.LocationUtils;
import ch.epfl.balelecbud.utility.notifications.NotificationMessageTest;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

@RunWith(AndroidJUnit4.class)
public class ConcertSoonNotificationTest {

    private final MockAuthenticator mockAuth = MockAuthenticator.getInstance();

    @Rule
    public final ActivityTestRule<RootActivity> mActivityRule =
            new ActivityTestRule<RootActivity>(RootActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();
            MockDatabase.getInstance().resetDatabase();
            BalelecbudApplication.setAppAuthenticator(mockAuth);
            mockAuth.signOut();
            mockAuth.setCurrentUser(MockDatabase.celine);
        }
    };

    @Before
    public void setup() {
        LocationUtils.setLocationClient(new LocationClient() {
            @Override
            public void requestLocationUpdates(LocationRequest lr, PendingIntent intent) {

            }

            @Override
            public void removeLocationUpdates(PendingIntent intent) {

            }
        });
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        if (device.hasObject(By.text("ALLOW"))) {
            device.findObject(By.text("ALLOW")).click();
            device.waitForWindowUpdate(null, 1000);
        }
        NotificationMessageTest.clearNotifications(device);
    }

    @Test
    public void scheduleNotificationWorksCorrectly(){
        String appName = mActivityRule.getActivity().getString(R.string.app_name);
        String expectedTitle = mActivityRule.getActivity().getString(R.string.concert_soon_notification_title);
        String expectedText = "Le nom de mon artiste starts in 15 minutes on Scene 3";
        Context ctx = mActivityRule.getActivity().getApplicationContext();
        Slot s = new Slot(0, "Le nom de mon artiste", "Scene 3", "path1", Timestamp.now(), Timestamp.now());

        NotificationScheduler ns = NotificationScheduler.getInstance();
        ns.scheduleNotification(ctx, s);

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.openNotification();
        device.wait(Until.hasObject(By.textStartsWith(expectedTitle)), 10_000);

        UiObject2 title = device.findObject(By.text(expectedTitle));
        assertEquals(title.getText(), expectedTitle);
        UiObject2 text = device.findObject(By.text(expectedText));
        assertEquals(text.getText(), expectedText);
        title.click();
    }

    @Test
    public void cancelNotificationWorksCorrectly(){
        String appName = mActivityRule.getActivity().getString(R.string.app_name);
        String expectedTitle = mActivityRule.getActivity().getString(R.string.concert_soon_notification_title);

        Context ctx = mActivityRule.getActivity().getApplicationContext();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        // schedule the notification to go off in 5 seconds,
        // which leaves plenty of time to cancel it
        cal.add(Calendar.SECOND, 5);
        cal.add(Calendar.MINUTE, 15);
        Slot s = new Slot(0, "Le nom de mon artiste", "path2", "Scene 3",
                new Timestamp(cal.getTime()), new Timestamp(cal.getTime()));

        NotificationScheduler ns = NotificationScheduler.getInstance();
        ns.scheduleNotification(ctx, s);
        ns.cancelNotification(ctx, s);

        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.openNotification();
        device.wait(Until.hasObject(By.text(expectedTitle)), 10_000);

        UiObject2 title = device.findObject(By.text(expectedTitle));
        assertNull(title);
        //hide notifications
        device.pressBack();

        device.waitForIdle();
    }
}
