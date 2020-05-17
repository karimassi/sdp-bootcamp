package ch.epfl.balelecbud.view.pointOfInterest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import ch.epfl.balelecbud.R;
import ch.epfl.balelecbud.model.PointOfInterest;
import ch.epfl.balelecbud.utility.recyclerViews.RecyclerViewData;
import ch.epfl.balelecbud.utility.recyclerViews.RefreshableRecyclerViewAdapter;

public final class PointOfInterestFragment extends Fragment {

    public static PointOfInterestFragment newInstance() {
        return (new PointOfInterestFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.fragment_point_of_interest, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        RecyclerView recyclerView = getView().findViewById(R.id.pointOfInterestRecyclerView);
        View freshnessView = getView().findViewById(R.id.freshness_info_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        RecyclerViewData<PointOfInterest, PointOfInterestHolder> data = new PointOfInterestData(getActivity());
        RefreshableRecyclerViewAdapter<PointOfInterest, PointOfInterestHolder> adapter = new RefreshableRecyclerViewAdapter<>(
                PointOfInterestHolder::new, freshnessView, data, R.layout.item_point_of_interest);
        recyclerView.setAdapter(adapter);
        SwipeRefreshLayout refreshLayout = getView().findViewById(R.id.swipe_refresh_layout_point_of_interest);
        adapter.setOnRefreshListener(refreshLayout);
    }
}
