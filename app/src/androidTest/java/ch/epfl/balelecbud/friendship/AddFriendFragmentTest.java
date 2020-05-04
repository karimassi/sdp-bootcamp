package ch.epfl.balelecbud.friendship;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.balelecbud.BalelecbudApplication;
import ch.epfl.balelecbud.R;
import ch.epfl.balelecbud.authentication.Authenticator;
import ch.epfl.balelecbud.authentication.MockAuthenticator;
import ch.epfl.balelecbud.models.User;
import ch.epfl.balelecbud.testUtils.TestAsyncUtils;
import ch.epfl.balelecbud.util.database.Database;
import ch.epfl.balelecbud.util.database.MockDatabase;
import ch.epfl.balelecbud.util.database.MyQuery;
import ch.epfl.balelecbud.util.database.MyWhereClause;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.balelecbud.util.database.Database.DOCUMENT_ID_OPERAND;
import static ch.epfl.balelecbud.util.database.MyWhereClause.Operator.EQUAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@RunWith(AndroidJUnit4.class)
public class AddFriendFragmentTest {

    private final User currentUser = MockDatabase.karim;
    private final User otherUser = MockDatabase.celine;

    private final Authenticator mockAuth = MockAuthenticator.getInstance();
    private final MockDatabase mockDb = MockDatabase.getInstance();

    private Bundle bundle;
    FragmentScenario<AddFriendFragment> scenario;

    @Before
    public void setup() {
        mockDb.resetDatabase();
        BalelecbudApplication.setAppAuthenticator(mockAuth);
        BalelecbudApplication.setAppDatabase(mockDb);
        mockAuth.setCurrentUser(currentUser);
        bundle = new Bundle();
        bundle.putParcelable("user", currentUser);
        scenario = FragmentScenario.launch(AddFriendFragment.class, bundle, R.style.Theme_AppCompat, null);
    }

    @Test
    public void testFragmentIsShown() {
        scenario.onFragment(fragment -> {
            assertThat(fragment.getDialog(), is(notNullValue()));
            assertThat(fragment.requireDialog().isShowing(), is(true));
        });
    }

    @Test
    public void addFriendDialogDismissedOnCancel() {
        scenario.onFragment(fragment -> {
            assertThat(fragment.getDialog(), is(notNullValue()));
            assertThat(fragment.requireDialog().isShowing(), is(true));
            fragment.dismiss();
            fragment.getParentFragmentManager().executePendingTransactions();
            assertThat(fragment.getDialog(), is(nullValue()));
        });

    }

    @Test
    public void addFriendDialogInvalidEmail() {
        onView(withId(R.id.edit_text_email_add_friend)).perform(typeText("fakemail"))
                .perform(closeSoftKeyboard());
        onView(withText(R.string.add_friend_request)).perform(click());
        onView(withId(R.id.text_view_add_friend)).check(doesNotExist());
    }

    @Test
    public void addFriendDialogEmptyEmail() {
        onView(withId(R.id.edit_text_email_add_friend)).perform(typeText(""))
                .perform(closeSoftKeyboard());
        onView(withText(R.string.add_friend_request)).perform(click());
        onView(withId(R.id.text_view_add_friend)).check(doesNotExist());
    }

    @Test
    public void addFriendDialogOwnEmail() {
        onView(withId(R.id.edit_text_email_add_friend)).perform(typeText(currentUser.getEmail()))
                .perform(closeSoftKeyboard());
        onView(withText(R.string.add_friend_request)).perform(click());
        onView(withId(R.id.text_view_add_friend)).check(doesNotExist());
    }

    @Test
    public void addFriendDialogValidEmail() throws InterruptedException {
        onView(withId(R.id.edit_text_email_add_friend))
                .perform(typeText(otherUser.getEmail())).perform(closeSoftKeyboard());
        onView(withText(R.string.add_friend_request)).perform(click());
        onView(withId(R.id.text_view_add_friend)).check(doesNotExist());
        TestAsyncUtils sync = new TestAsyncUtils();
        MyQuery query = new MyQuery(Database.FRIEND_REQUESTS_PATH, new MyWhereClause(DOCUMENT_ID_OPERAND, EQUAL, otherUser.getUid()));
        mockDb.query(query).thenApply(maps -> maps.get(0))
                .whenComplete((stringObjectMap, throwable) -> {
                    if (stringObjectMap != null) {
                        sync.assertTrue(stringObjectMap.containsKey(currentUser.getUid()));
                    } else {
                        sync.fail();
                    }
                    sync.call();
                });
        sync.waitCall(1);
        sync.assertCalled(1);
    }
}