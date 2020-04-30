package ch.epfl.balelecbud;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.balelecbud.schedule.SlotData;
import ch.epfl.balelecbud.schedule.models.Slot;
import ch.epfl.balelecbud.testUtils.TestAsyncUtils;
import ch.epfl.balelecbud.util.database.DatabaseWrapper;
import ch.epfl.balelecbud.util.database.MockDatabaseWrapper;
import ch.epfl.balelecbud.util.intents.FlowUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.balelecbud.testUtils.CustomMatcher.getItemInSchedule;
import static ch.epfl.balelecbud.testUtils.CustomViewAssertion.switchChecked;
import static ch.epfl.balelecbud.util.database.MockDatabaseWrapper.slot1;
import static ch.epfl.balelecbud.util.database.MockDatabaseWrapper.slot2;

@RunWith(AndroidJUnit4.class)
public class ScheduleActivityWithSubscribedConcertTest {
    private MockDatabaseWrapper mock = MockDatabaseWrapper.getInstance();

    @Rule
    public final ActivityTestRule<ScheduleActivity> mActivityRule = new ActivityTestRule<ScheduleActivity>(ScheduleActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            BalelecbudApplication.setAppDatabaseWrapper(mock);
            SlotData.setIntentLauncher(intent -> { });
            mock.resetDocument(DatabaseWrapper.CONCERT_SLOTS_PATH);
        }

        @Override
        protected Intent getActivityIntent() {
            Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ScheduleActivity.class);
            FlowUtil.packCallback(new Slot[]{slot1}, intent);
            return intent;
        }

        @Override
        protected void afterActivityFinished() {
            super.afterActivityFinished();
            mock.resetMockDatabase();
        }
    };

    @Before
    public void setUpMockIntentLauncher() {
        mock.resetDocument(DatabaseWrapper.CONCERT_SLOTS_PATH);
        refreshRecyclerView();
    }

    @Test
    public void testUnSubscribeToAConcert() throws Throwable {
        TestAsyncUtils sync = new TestAsyncUtils();
        SlotData.setIntentLauncher(intent -> {
            if (intent.getAction() == null)
                sync.fail();

            String action = intent.getAction();
            switch (action) {
                case FlowUtil.ACK_CONCERT:
                case FlowUtil.GET_ALL_CONCERT:
                    sync.fail();
                    break;
                case FlowUtil.CANCEL_CONCERT:
                    sync.call();
                case FlowUtil.SUBSCRIBE_CONCERT:
                    break;
            }
        });
        mock.storeDocument(DatabaseWrapper.CONCERT_SLOTS_PATH, slot1);
        mock.storeDocument(DatabaseWrapper.CONCERT_SLOTS_PATH, slot2);

        refreshRecyclerView();

        onView(getItemInSchedule(0, 3)).perform(click());
        sync.waitCall(1);
        sync.assertCalled(1);
        sync.assertNoFailedTests();
    }

    @Test
    public void testSubscribedConcertIsChecked() {
        mock.storeDocument(DatabaseWrapper.CONCERT_SLOTS_PATH, slot1);
        mock.storeDocument(DatabaseWrapper.CONCERT_SLOTS_PATH, slot2);

        refreshRecyclerView();

        onView(getItemInSchedule(0, 3))
                .check(switchChecked(true));
        onView(getItemInSchedule(1, 3))
                .check(switchChecked(false));
    }

    private void refreshRecyclerView() {
        onView(withId(R.id.swipe_refresh_layout_schedule)).perform(swipeDown());
    }
}
