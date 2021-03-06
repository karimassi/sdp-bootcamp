package ch.epfl.balelecbud.utility.database;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import ch.epfl.balelecbud.utility.InformationSource;
import ch.epfl.balelecbud.utility.cache.Cache;
import ch.epfl.balelecbud.utility.database.query.MyQuery;

import static ch.epfl.balelecbud.BalelecbudApplication.getAppCache;
import static ch.epfl.balelecbud.BalelecbudApplication.getRemoteDatabase;

/**
 * Decorator class for a cached database
 */
public final class CachedDatabase implements Database {

    private static final String TAG = CachedDatabase.class.getSimpleName();

    private Cache cache;

    private static CachedDatabase instance = new CachedDatabase(getAppCache());

    private CachedDatabase(Cache cache) {
        this.cache = cache;
    }

    public static CachedDatabase getInstance() {
        return instance;
    }

    @VisibleForTesting
    public void setCache(Cache cache) {
        this.cache = cache;
    }

    @Override
    public void unregisterDocumentListener(String collectionName, String documentID) {
        getRemoteDatabase().unregisterDocumentListener(collectionName, documentID);
    }

    @Override
    public <T> void listenDocument(String collectionName, String documentID, Consumer<T> consumer, Class<T> type) {
        getRemoteDatabase().listenDocument(collectionName, documentID, consumer, type);
    }

    @Override
    public <T> CompletableFuture<FetchedData<T>> query(MyQuery query, Class<T> tClass) {
        CompletableFuture<FetchedData<T>> result = new CompletableFuture<>();
        if (query.getSource().equals(InformationSource.CACHE_ONLY) || (query.getSource().equals(InformationSource.CACHE_FIRST) && cache.contains(query))) {
            try {
                result = cache.get(query, tClass);
            } catch (IOException e) {
                Log.d(TAG, "query: " + e.getLocalizedMessage());
                result.completeExceptionally(e);
            }
        } else {
            result = getRemoteDatabase().query(query, tClass);
            result.whenComplete((ts, throwable) -> {
                if (throwable == null) {
                    cache.flush(query.getCollectionName());
                    for (T t : ts.getList()) {
                        String id = query.hasDocumentIdOperand() ? query.getIdOperand() : String.valueOf(t.hashCode());
                        try {
                            cache.put(query.getCollectionName(), id, t);
                        } catch (IOException e) {
                            Log.d(TAG, "query: " + e.getLocalizedMessage());
                        }
                    }
                }
            });
        }
        return result;
    }

    @Override
    public CompletableFuture<FetchedData<Map<String, Object>>> query(MyQuery query) {
        CompletableFuture<FetchedData<Map<String, Object>>> result = new CompletableFuture<>();
        if (query.getSource().equals(InformationSource.CACHE_FIRST) && cache.contains(query)) {
            try {
                result = cache.get(query);
            } catch (IOException e) {
                Log.d(TAG, "query: " + e.getLocalizedMessage());
                result.completeExceptionally(e);
            }
        } else {
            result = getRemoteDatabase().query(query);
            result.whenComplete((fetchedData, throwable) -> {
               if (throwable == null) {
                   cache.flush(query.getCollectionName());
                   for (Map m : fetchedData.getList()) {
                       String id = query.hasDocumentIdOperand() ? query.getIdOperand() : String.valueOf(m.hashCode());
                       try {
                           cache.put(query.getCollectionName(), id, m);
                       } catch (IOException e) {
                           Log.d(TAG, "query: " + e.getLocalizedMessage());
                       }
                   }
               }
            });
        }
        return result;
    }

    @Override
    public void updateDocument(String collectionName, String documentID, Map<String, Object> updates) {
        getRemoteDatabase().updateDocument(collectionName, documentID, updates);
    }

    @Override
    public <T> void storeDocument(String collectionName, T document) {
        getRemoteDatabase().storeDocument(collectionName, document);
    }

    @Override
    public <T> CompletableFuture<Void> storeDocumentWithID(String collectionName, String documentID, T document) {
        return getRemoteDatabase().storeDocumentWithID(collectionName, documentID, document);
    }

    @Override
    public void deleteDocumentWithID(String collectionName, String documentID) {
        getRemoteDatabase().deleteDocumentWithID(collectionName, documentID);
    }
}
