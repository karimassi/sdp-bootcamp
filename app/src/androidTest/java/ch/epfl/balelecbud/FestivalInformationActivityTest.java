package ch.epfl.balelecbud;

import android.view.Gravity;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.balelecbud.festivalInformation.models.FestivalInformation;
import ch.epfl.balelecbud.festivalInformation.FestivalInformationAdapter;
import ch.epfl.balelecbud.matchers.RecyclerViewMatcher;
import ch.epfl.balelecbud.util.database.MockDatabaseWrapper;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class FestivalInformationActivityTest {

    MockDatabaseWrapper mock;

    @Rule
    public final ActivityTestRule<FestivalInformationActivity> mActivityRule = new ActivityTestRule<FestivalInformationActivity>(FestivalInformationActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            mock = new MockDatabaseWrapper();
            FestivalInformationAdapter.setDatabaseImplementation(mock);
        }
    };

    @Test
    public void testFestivalInfoRecyclerViewIsDisplayed() {
        onView(withId(R.id.festivalInfoRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void testCanAddInfoToDatabase() throws Throwable {
        final FestivalInformation info = new FestivalInformation("New", "Hello it's a me, new");
        mock.addItem(info);
        testInfoInView(onView(new RecyclerViewMatcher(R.id.festivalInfoRecyclerView).atPosition(0)), info);
    }

    @Test
    public void testCanModifyInfoFromDatabase() throws Throwable {
        final FestivalInformation info = new FestivalInformation("New", "Hello it's a me, new");
        final FestivalInformation infoModified = new FestivalInformation("Modified", "Hello it's a me, new");

        mock.addItem(info);
        mock.modifyItem(infoModified, 0);

        testInfoInView(onView(new RecyclerViewMatcher(R.id.festivalInfoRecyclerView).atPosition(0)), infoModified);
    }

    @Test
    public void testCantModifyInfoFromDatabaseThatIsNotThere() throws Throwable {
        final FestivalInformation info = new FestivalInformation("New", "Hello it's a me, new");
        final FestivalInformation infoModified = new FestivalInformation();

        mock.addItem(info);
        mock.modifyItem(infoModified, 2);

        testInfoInView(onView(new RecyclerViewMatcher(R.id.festivalInfoRecyclerView).atPosition(0)), info);
    }

    @Test
    public void testCanDeleteInfoFromDatabase() throws Throwable {
        final FestivalInformation info = new FestivalInformation("Bad", "Hello it's a me, bad");

        mock.addItem(info);
        mock.addItem(info);
        mock.removeItem(info, 0);

        testInfoInView(onView(new RecyclerViewMatcher(R.id.festivalInfoRecyclerView).atPosition(0)), info);
    }

    @Test
    public void testCantDeleteInfoFromEmptyDatabase() throws Throwable {
        final FestivalInformation info = new FestivalInformation();
        mock.removeItem(info, 0);
    }

    private void testInfoInView(ViewInteraction viewInteraction, FestivalInformation information) {
        viewInteraction.check(matches(hasDescendant(withText(information.getTitle()))));
        viewInteraction.check(matches(hasDescendant(withText(information.getInformation()))));
    }

    @Test
    public void testDrawer() {
        onView(withId(R.id.festival_info_activity_drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.headerImageView)).check(matches(isDisplayed()));
        onView(withId(R.id.festival_info_activity_nav_view)).check(matches(isDisplayed()));
    }

    @Test
    public void openInfoActivityFromDrawer() {
        onView(withId(R.id.festival_info_activity_drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.festival_info_activity_nav_view)).check(matches(isDisplayed()));
        onView(withId(R.id.festival_info_activity_nav_view)).perform(NavigationViewActions.navigateTo(R.id.activity_main_drawer_info));
        onView(withId(R.id.festivalInfoRecyclerView)).check(matches(isDisplayed()));

    }

    @Test
    public void openScheduleActivityFromDrawer() {
        onView(withId(R.id.festival_info_activity_drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.festival_info_activity_nav_view)).check(matches(isDisplayed()));
        onView(withId(R.id.festival_info_activity_nav_view)).perform(NavigationViewActions.navigateTo(R.id.activity_main_drawer_schedule));
        onView(withId(R.id.scheduleRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void openPOIActivityFromDrawer() {
        onView(withId(R.id.festival_info_activity_drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.festival_info_activity_nav_view)).check(matches(isDisplayed()));
        onView(withId(R.id.festival_info_activity_nav_view)).perform(NavigationViewActions.navigateTo(R.id.activity_main_drawer_poi));
        onView(withId(R.id.pointOfInterestRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void openMapActivityFromDrawer() {
        onView(withId(R.id.festival_info_activity_drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.festival_info_activity_nav_view)).check(matches(isDisplayed()));
        onView(withId(R.id.festival_info_activity_nav_view)).perform(NavigationViewActions.navigateTo(R.id.activity_main_drawer_map));
        onView(withId(R.id.map)).check(matches(isDisplayed()));
    }

    @Test
    public void openTransportActivityFromDrawer() {
        onView(withId(R.id.festival_info_activity_drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.festival_info_activity_nav_view)).check(matches(isDisplayed()));
        onView(withId(R.id.festival_info_activity_nav_view)).perform(NavigationViewActions.navigateTo(R.id.activity_main_drawer_transport));
        onView(withId(R.id.fragmentTransportList)).check(matches(isDisplayed()));
    }

    @Test
    public void signOutFromDrawer() {
        onView(withId(R.id.festival_info_activity_drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.festival_info_activity_nav_view)).check(matches(isDisplayed()));
        onView(withId(R.id.festival_info_activity_nav_view)).perform(NavigationViewActions.navigateTo(R.id.sign_out_button));
    }

    @Test
    public void testBackPress(){
        onView(withId(R.id.festival_info_activity_drawer_layout)).check(matches(isClosed(Gravity.LEFT))).perform(DrawerActions.open());
        onView(withId(R.id.festival_info_activity_nav_view)).check(matches(isDisplayed()));
        Espresso.pressBack();
        onView(withId(R.id.festival_info_activity_drawer_layout)).check(matches(isClosed(Gravity.LEFT)));
        Espresso.pressBack();
    }
}