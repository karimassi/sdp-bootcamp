package ch.epfl.balelecbud.view.transport;


import android.util.Log;

import java.util.concurrent.CompletableFuture;

import ch.epfl.balelecbud.model.Location;
import ch.epfl.balelecbud.model.TransportStation;
import ch.epfl.balelecbud.utility.CompletableFutureUtils;
import ch.epfl.balelecbud.utility.InformationSource;
import ch.epfl.balelecbud.utility.TransportUtils;
import ch.epfl.balelecbud.utility.database.FetchedData;
import ch.epfl.balelecbud.utility.recyclerViews.OnRecyclerViewInteractionListener;
import ch.epfl.balelecbud.utility.recyclerViews.RecyclerViewData;

public final class TransportStationData extends RecyclerViewData<TransportStation, TransportStationHolder> {

    private static String TAG = TransportStationData.class.getSimpleName();

    private Location userLocation;
    private OnRecyclerViewInteractionListener<TransportStation> interactionListener;

    TransportStationData(Location userLocation, OnRecyclerViewInteractionListener<TransportStation> interactionListener) {
        this.userLocation = userLocation;
        this.interactionListener = interactionListener;
    }

    @Override
    public CompletableFuture<Long> reload(InformationSource preferredSource) {
        return TransportUtils.getNearbyStations(userLocation)
                //wrap in FetchedData with freshness set to null
                .thenApply(FetchedData::new)
                .thenApply(new CompletableFutureUtils.MergeFunction<>(this));
    }

    @Override
    public void bind(int index, TransportStationHolder viewHolder) {
        viewHolder.nameView.setText(data.get(index).getStationName());
        viewHolder.distanceView.setText(data.get(index).getFormattedDistanceToUser());
        viewHolder.itemView.setOnClickListener(v -> {
            interactionListener.onItemSelected(data.get(index));
            Log.d(TAG, "Selected station: " + data.get(index).getStationName());
        });
    }
}
