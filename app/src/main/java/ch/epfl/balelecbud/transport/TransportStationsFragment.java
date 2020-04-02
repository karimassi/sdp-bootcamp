package ch.epfl.balelecbud.transport;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import ch.epfl.balelecbud.R;
import ch.epfl.balelecbud.models.Location;
import ch.epfl.balelecbud.transport.objects.TransportStation;
import ch.epfl.balelecbud.util.views.OnRecyclerViewInteractionListener;
import ch.epfl.balelecbud.util.views.RefreshableRecyclerViewAdapter;

public class TransportStationsFragment extends Fragment {

    private OnRecyclerViewInteractionListener<TransportStation> stationSelectedListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_transport_stations, container, false);

        Context context = view.getContext();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_transport_stations);

        TransportStationData data = new TransportStationData(getArguments().getParcelable("location"), stationSelectedListener);
        final RefreshableRecyclerViewAdapter<TransportStation, TransportStationHolder> adapter =
                new RefreshableRecyclerViewAdapter<>(TransportStationHolder::new, data, R.layout.item_transport_station);
        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.swipe_refresh_layout_transport_stations);
        adapter.setOnRefreshListener(refreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void setInteractionListener(OnRecyclerViewInteractionListener<TransportStation> stationSelectedListener) {
        this.stationSelectedListener = stationSelectedListener;
    }

    public static TransportStationsFragment newInstance(Location userLocation) {
        Bundle args = new Bundle();
        args.putParcelable("location", userLocation);
        TransportStationsFragment fragment = new TransportStationsFragment();
        fragment.setArguments(args);
        return fragment;
    }





}
