package ch.epfl.balelecbud.util;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;

import java.util.concurrent.CompletableFuture;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TaskToCompletableFutureAdapter<T> extends CompletableFuture<T> {

    public TaskToCompletableFutureAdapter(Task<T> result) {
        result.addOnSuccessListener(this::complete);
        result.addOnFailureListener(this::completeExceptionally);
    }
}
