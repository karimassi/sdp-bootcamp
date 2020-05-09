package ch.epfl.balelecbud.authentication;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.CompletableFuture;

import ch.epfl.balelecbud.models.User;
import ch.epfl.balelecbud.util.TaskToCompletableFutureAdapter;
import ch.epfl.balelecbud.util.database.Database;
import ch.epfl.balelecbud.util.database.FirestoreDatabase;
import ch.epfl.balelecbud.util.database.MyQuery;
import ch.epfl.balelecbud.util.database.MyWhereClause;

import static ch.epfl.balelecbud.util.database.Database.DOCUMENT_ID_OPERAND;
import static ch.epfl.balelecbud.util.database.MyWhereClause.Operator.EQUAL;

public class FirebaseAuthenticator implements Authenticator {
    private static final Authenticator instance = new FirebaseAuthenticator();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private FirebaseAuthenticator() { }

    @Override
    public CompletableFuture<String> signInAnonymously() {
        return new TaskToCompletableFutureAdapter<>(mAuth.signInAnonymously()).thenApply(authResult -> getCurrentUid());
    }

    @Override
    public CompletableFuture<User> signIn(String email, String password) {
        return new TaskToCompletableFutureAdapter<>(mAuth.signInWithEmailAndPassword(email, password))
                .thenCompose(authResult -> FirestoreDatabase.getInstance()
                        .query(new MyQuery(Database.USERS_PATH,
                                new MyWhereClause(DOCUMENT_ID_OPERAND, EQUAL, getCurrentUid())),
                                User.class)
                        .thenApply(users -> users.get(0)));
    }

    @Override
    public CompletableFuture<Void> createAccount(final String name, final String email, String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        return new TaskToCompletableFutureAdapter<>(mAuth.getCurrentUser().linkWithCredential(credential))
                .thenCompose(authResult -> FirestoreDatabase.getInstance()
                        .storeDocumentWithID(Database.USERS_PATH, getCurrentUid(),
                                new User(email, name, getCurrentUid())));
    }

    @Override
    public String getCurrentUid() {
        return mAuth.getCurrentUser().getUid();
    }

    @Override
    public void signOut() {
        Authenticator.super.signOut();
        this.mAuth.signOut();
    }

    public static Authenticator getInstance() {
        return instance;
    }
}
