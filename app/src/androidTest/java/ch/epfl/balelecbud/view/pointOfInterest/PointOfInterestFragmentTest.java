package ch.epfl.balelecbud.view.pointOfInterest;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.balelecbud.BalelecbudApplication;
import ch.epfl.balelecbud.R;
import ch.epfl.balelecbud.model.PointOfInterest;
import ch.epfl.balelecbud.testUtils.RecyclerViewMatcher;
import ch.epfl.balelecbud.utility.database.Database;
import ch.epfl.balelecbud.utility.database.MockDatabase;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.balelecbud.utility.database.MockDatabase.pointOfInterest1;

@RunWith(AndroidJUnit4.class)
public class PointOfInterestFragmentTest  {

    private final MockDatabase mock = MockDatabase.getInstance();

    @Before
    public void setup() {
        mock.resetDatabase();
        BalelecbudApplication.setAppDatabase(mock);
        mock.resetDocument(Database.POINT_OF_INTEREST_PATH);
        FragmentScenario.launchInContainer(PointOfInterestFragment.class);
        refreshRecyclerView();
    }

    @After
    public void cleanUp() {
        mock.resetDocument(Database.POINT_OF_INTEREST_PATH);
    }

    @Test
    public void testPointOfInterestRecyclerViewIsDisplayed() {
        onView(ViewMatchers.withId(R.id.pointOfInterestRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void testCanAddPOIToDatabase() {
        mock.storeDocument(Database.POINT_OF_INTEREST_PATH, pointOfInterest1);

        refreshRecyclerView();

        testInfoInView(onView(new RecyclerViewMatcher(R.id.pointOfInterestRecyclerView).
                atPosition(0)), pointOfInterest1);
    }

    private void refreshRecyclerView() {
        onView(withId(R.id.swipe_refresh_layout_point_of_interest)).perform(swipeDown());
    }

    private void testInfoInView(ViewInteraction viewInteraction, PointOfInterest poi) {
        viewInteraction.check(matches(hasDescendant(withText(poi.getName()))));
    }

}