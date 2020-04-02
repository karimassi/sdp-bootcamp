package ch.epfl.balelecbud;

import ch.epfl.balelecbud.authentication.MockAuthenticator;
import ch.epfl.balelecbud.testUtils.TestAsyncUtils;

import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;

public class BasicAuthenticationTest {

    protected void logout() throws Throwable {
        TestAsyncUtils sync = new TestAsyncUtils();
        Runnable myRunnable = () -> {
            MockAuthenticator.getInstance().signOut();
            sync.call();
        };
        runOnUiThread(myRunnable);
        sync.waitCall(1);
    }
}