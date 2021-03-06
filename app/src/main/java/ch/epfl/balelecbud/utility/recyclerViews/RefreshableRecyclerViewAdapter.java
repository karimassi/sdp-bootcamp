package ch.epfl.balelecbud.utility.recyclerViews;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.CompletableFuture;

import ch.epfl.balelecbud.BalelecbudApplication;
import ch.epfl.balelecbud.R;
import ch.epfl.balelecbud.utility.InformationSource;

import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT;

/**
 * Generic adapter for refreshable recycler view
 * @param <A> the type of the data displayed in the recycler view
 * @param <B> the type of the view holder for the displayed data
 */
public final class RefreshableRecyclerViewAdapter<A, B extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<B> {

    private static final String TAG = "RefRecViewAdpt";
    private final ViewHolderFactory<B> factory;
    private final View freshnessView;
    private final RecyclerViewData<A, B> data;
    private final int itemId;

    public RefreshableRecyclerViewAdapter(ViewHolderFactory<B> factory, View freshnessView, RecyclerViewData<A, B> data, int itemId) {
        this.factory = factory;
        this.freshnessView = freshnessView;
        this.data = data;
        this.itemId = itemId;
        data.setAdapter(this);
        checkConnectivityAndReload(InformationSource.CACHE_FIRST).thenAccept(this::handleFreshness);
    }

    public CompletableFuture<Long> reloadData() {
        return checkConnectivityAndReload(InformationSource.REMOTE_ONLY);
    }

    public void setOnRefreshListener(SwipeRefreshLayout refreshLayout) {
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            reloadData()
                    .thenAccept(this::handleFreshness)
                    .thenRun(() -> refreshLayout.setRefreshing(false));
        });
    }

    private void handleFreshness(Long freshness) {
        Log.v(TAG, "handling freshness : " + freshness);
        if (freshness != null) {
            String result = BalelecbudApplication.getAppContext().getString(R.string.cache_info_start)
                    + ((System.currentTimeMillis() - freshness) / 60_000L) +
                    BalelecbudApplication.getAppContext().getString(R.string.cache_info_end);
            try {
                Snackbar.make(freshnessView, result, LENGTH_SHORT).show();
            }catch(Exception e){
                Log.d(TAG, e.getMessage());
            }
            Log.d(TAG, result);
        }
    }

    @NonNull
    @Override
    public B onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(itemId, parent, false);
        return factory.createInstance(view);
    }

    @Override
    public void onBindViewHolder(@NonNull B holder, int position) {
        data.bind(position, holder);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private CompletableFuture<Long> checkConnectivityAndReload(InformationSource preferredSource) {
        return BalelecbudApplication.getConnectivityChecker().isConnectionAvailable() ?
                data.reload(preferredSource) :
                data.reload(InformationSource.CACHE_ONLY);
    }
}
