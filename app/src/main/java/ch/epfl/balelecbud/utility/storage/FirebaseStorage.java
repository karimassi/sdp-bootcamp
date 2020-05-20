package ch.epfl.balelecbud.utility.storage;

import android.util.Log;

import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import ch.epfl.balelecbud.utility.TaskToCompletableFutureAdapter;

public class FirebaseStorage implements Storage {

    private static final String TAG = FirebaseStorage.class.getSimpleName();

    private static final FirebaseStorage instance = new FirebaseStorage();
    private final com.google.firebase.storage.FirebaseStorage firebaseStorage = com.google.firebase.storage.FirebaseStorage.getInstance();

    private FirebaseStorage() {}

    public static FirebaseStorage getInstance() { return instance; }

    @Override
    public CompletableFuture<File> getFile(String path) {
        CompletableFuture<File> future = new CompletableFuture<>();

        StorageReference storageRef = firebaseStorage.getReference();
        try {
            final File localFile = File.createTempFile("images", "jpg");
            future = new TaskToCompletableFutureAdapter<>(storageRef.child(path).getFile(localFile)).thenApply(x -> localFile);
        } catch (IOException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<List<String>> getAllFileNameIn(String collectionName) {
        StorageReference ref = firebaseStorage.getReference().child(collectionName);
        /**ArrayList<String> names = new ArrayList<String>();
        ref.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                names.add(item.getPath().substring(1));
            }
        });
        return names;**/
        Log.d(TAG, "getAllFileNameIn " + collectionName);
        return new TaskToCompletableFutureAdapter<>(ref.listAll())
                .thenApply(listResult ->
                        listResult.getItems().stream().map(StorageReference::getPath).collect(Collectors.toList()));
    }
}

