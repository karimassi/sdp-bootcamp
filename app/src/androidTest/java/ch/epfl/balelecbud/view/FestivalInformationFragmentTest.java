package ch.epfl.balelecbud.view;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.balelecbud.BalelecbudApplication;
import ch.epfl.balelecbud.R;
import ch.epfl.balelecbud.model.FestivalInformation;
import ch.epfl.balelecbud.testUtils.RecyclerViewMatcher;
import ch.epfl.balelecbud.utility.database.Database;
import ch.epfl.balelecbud.utility.database.MockDatabase;
import ch.epfl.balelecbud.view.festivalInformation.FestivalInformationFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class FestivalInformationFragmentTest {

    private final MockDatabase mock = MockDatabase.getInstance();

    @Before
    public void setup() {
        mock.resetDatabase();
        BalelecbudApplication.setAppDatabase(mock);
        FragmentScenario.launchInContainer(FestivalInformationFragment.class);
    }

    @After
    public void cleanUp() {
        mock.resetDocument(Database.FESTIVAL_INFORMATION_PATH);
    }

    @Test
    public void testFestivalInfoRecyclerViewIsDisplayed() {
        onView(ViewMatchers.withId(R.id.festivalInfoRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void testCanAddInfoToDatabase() {
        final FestivalInformation info = new FestivalInformation("New", "Hello it's a me, new");
        mock.storeDocument(Database.FESTIVAL_INFORMATION_PATH, info);
        onView(withId(R.id.swipe_refresh_layout_festival_info)).perform(swipeDown());
        testInfoInView(onView(new RecyclerViewMatcher(R.id.festivalInfoRecyclerView).atPosition(0)), info);
    }

    @Ignore("Should not be able to delete from Database for now but it's eventually a feature we'd like to implement so ignore the test for now")
    @Test
    public void testDeletedInfoIsNotDisplayed() {
        final FestivalInformation info1 = new FestivalInformation("Bad", "Hello it's a me, bad");
        final FestivalInformation info2 = new FestivalInformation("Good", "Hello it's a me, good");

        mock.storeDocument(Database.FESTIVAL_INFORMATION_PATH, info1);
        mock.storeDocument(Database.FESTIVAL_INFORMATION_PATH, info2);

        onView(withId(R.id.swipe_refresh_layout_festival_info)).perform(swipeDown());

        testInfoInView(onView(new RecyclerViewMatcher(R.id.festivalInfoRecyclerView).atPosition(0)), info1);
        testInfoInView(onView(new RecyclerViewMatcher(R.id.festivalInfoRecyclerView).atPosition(1)), info2);
        onView(withId(R.id.festivalInfoRecyclerView)).check(matches(hasChildCount(2)));

        mock.deleteDocumentWithID(Database.FESTIVAL_INFORMATION_PATH, "Good");

        onView(withId(R.id.swipe_refresh_layout_festival_info)).perform(swipeDown());

        testInfoInView(onView(new RecyclerViewMatcher(R.id.festivalInfoRecyclerView).atPosition(0)), info1);
        onView(withId(R.id.festivalInfoRecyclerView)).check(matches(hasChildCount(1)));
    }

    private void testInfoInView(ViewInteraction viewInteraction, FestivalInformation information) {
        viewInteraction.check(matches(hasDescendant(withText(information.getTitle()))));
        viewInteraction.check(matches(hasDescendant(withText(information.getInformation()))));
    }
}