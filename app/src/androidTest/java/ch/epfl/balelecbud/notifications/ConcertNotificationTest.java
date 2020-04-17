package ch.epfl.balelecbud.notifications;

import android.app.PendingIntent;
import android.util.Log;
import android.view.Gravity;

import androidx.room.Room;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.google.android.gms.location.LocationRequest;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import ch.epfl.balelecbud.BalelecbudApplication;
import ch.epfl.balelecbud.R;
import ch.epfl.balelecbud.WelcomeActivity;
import ch.epfl.balelecbud.location.LocationClient;
import ch.epfl.balelecbud.location.LocationUtil;
import ch.epfl.balelecbud.notifications.concertFlow.ConcertFlow;
import ch.epfl.balelecbud.notifications.concertFlow.objects.ConcertOfInterestDatabase;
import ch.epfl.balelecbud.schedule.models.Slot;
import ch.epfl.balelecbud.util.database.MockDatabaseWrapper;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static ch.epfl.balelecbud.testUtils.CustomMatcher.getItemInSchedule;
import static ch.epfl.balelecbud.testUtils.CustomViewAssertion.switchChecked;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ConcertNotificationTest {

    private final MockDatabaseWrapper mock = MockDatabaseWrapper.getInstance();
    @Rule
    public final ActivityTestRule<WelcomeActivity> mActivityRule =
            new ActivityTestRule<WelcomeActivity>(WelcomeActivity.class) {
                @Override
                protected void beforeActivityLaunched() {
                    super.beforeActivityLaunched();
                    LocationUtil.setLocationClient(new LocationClient() {
                        @Override
                        public void requestLocationUpdates(LocationRequest lr, PendingIntent intent) {
                        }

                        @Override
                        public void removeLocationUpdates(PendingIntent intent) {
                        }
                    });
                    BalelecbudApplication.setAppDatabaseWrapper(mock);
                }
            };
    private final Slot s = new Slot(0, "Le nom de mon artiste", "Scene 3",
            Timestamp.now(), Timestamp.now());
    private ConcertOfInterestDatabase db;
    private UiDevice device;

    @Before
    public void setup() {
        device = UiDevice.getInstance(getInstrumentation());
        if (device.hasObject(By.text("ALLOW"))) {
            device.findObject(By.text("ALLOW")).click();
            device.waitForWindowUpdate(null, 1_000);
        }
        // quit the notifications center if it happens to be open
        clearNotifications();
        this.db = Room.inMemoryDatabaseBuilder(
                getApplicationContext(),
                ConcertOfInterestDatabase.class
        ).build();
        ConcertFlow.setMockDb(db);
    }

    @After
    public void tearDown() {
        assertTrue(this.db.isOpen());
        this.db.close();
        clearNotifications();
    }

    private void clearNotifications() {
        device.openNotification();
        UiObject2 button = device.findObject(By.text("CLEAR ALL"));
        if (button != null) button.click();
        device.pressBack();

    }

    @Test
    public void subscribeToAConcertScheduleANotification() throws Throwable {
        checkSwitchAfter(() -> {
            checkNotification();
            device.waitForWindowUpdate(null, 10000);
            openScheduleActivityFrom(R.id.root_activity_drawer_layout, R.id.root_activity_nav_view);
            Log.v("mySuperTag", "executed subscribeToAConcertScheduleANotification");
        }, s, false);
    }

    @Test
    public void subscribeToAConcertKeepItSubscribed() throws Throwable {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        // schedule the notification to go off in 5 minutes,
        // which leaves plenty of time for the test to finish and cancel it
        cal.add(Calendar.MINUTE, 20);
        Slot s1 = new Slot(0, "Le nom de mon artiste", "Scene 3",
                new Timestamp(cal.getTime()), new Timestamp(cal.getTime()));
        checkSwitchAfter(() -> {
            openInfoActivityFrom(R.id.schedule_activity_drawer_layout, R.id.schedule_activity_nav_view);
            openScheduleActivityFrom(R.id.festival_info_activity_drawer_layout, R.id.festival_info_activity_nav_view);
            Log.v("mySuperTag", "executed subscribeToAConcertKeepItSubscribed");
        }, s1, true);
        onView(getItemInSchedule(0, 3)).perform(click());
    }

    @Test
    public void unsubscribeToAConcertCancelNotification() throws Throwable {
        String expectedTitle = getApplicationContext().getString(R.string.concert_soon_notification_title);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        // schedule the notification to go off in 5 seconds,
        // which leaves plenty of time to cancel it
        cal.add(Calendar.SECOND, 5);
        cal.add(Calendar.MINUTE, 15);
        Slot s1 = new Slot(0, "Le nom de mon artiste", "Scene 3",
                new Timestamp(cal.getTime()), new Timestamp(cal.getTime()));


        openScheduleActivityFrom(R.id.root_activity_drawer_layout, R.id.root_activity_nav_view);

        mock.addItem(s1);
        assertNotNull(device.wait(Until.hasObject(By.text(s1.getArtistName())), 1000));
        onView(getItemInSchedule(0, 3)).perform(click());
        onView(getItemInSchedule(0, 3)).perform(click());

        device.openNotification();
        device.wait(Until.hasObject(By.text(expectedTitle)), 10_000);

        assertFalse(device.hasObject(By.text(expectedTitle)));
        //hide notifications
        device.pressBack();

        device.waitForIdle();
    }

    private void checkSwitchAfter(Runnable runnable, Slot s, boolean switchStateAfter) throws Throwable {
        openScheduleActivityFrom(R.id.root_activity_drawer_layout, R.id.root_activity_nav_view);

        mock.addItem(s);
        onView(getItemInSchedule(0, 3)).perform(click());
        runnable.run();

        mock.addItem(s);
        onView(getItemInSchedule(0, 3)).check(switchChecked(switchStateAfter));
    }

    private void checkNotification() {
        String expectedTitle = mActivityRule.getActivity().getString(R.string.concert_soon_notification_title);
        String expectedText = "Le nom de mon artiste starts in 15 minutes on Scene 3";

        device.openNotification();
        assertNotNull(device.wait(Until.hasObject(By.textStartsWith(expectedTitle)), 30_000));
        UiObject2 title = device.findObject(By.text(expectedTitle));
        assertNotNull(title);
        assertNotNull(device.findObject(By.text(expectedText)));
        title.click();
    }

    private void openDrawerFrom(int layout_id, int nav_id) {
        onView(withId(layout_id)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(nav_id)).check(matches(isDisplayed()));
    }

    private void clickItemFrom(int itemId, int nav_id) {
        onView(withId(nav_id)).perform(NavigationViewActions.navigateTo(itemId));
    }

    private void openScheduleActivityFrom(int layout_id, int nav_id) {
        openDrawerFrom(layout_id, nav_id);
        clickItemFrom(R.id.activity_main_drawer_schedule, nav_id);
        device.waitForIdle(10000);
    }

    private void openInfoActivityFrom(int layout_id, int nav_id) {
        openDrawerFrom(layout_id, nav_id);
        clickItemFrom(R.id.activity_main_drawer_info, nav_id);
        device.waitForIdle(10000);
    }

}