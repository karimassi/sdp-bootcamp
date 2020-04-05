package ch.epfl.balelecbud;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import ch.epfl.balelecbud.pointOfInterest.PointOfInterest;
import ch.epfl.balelecbud.pointOfInterest.PointOfInterestData;
import ch.epfl.balelecbud.pointOfInterest.PointOfInterestHolder;
import ch.epfl.balelecbud.util.views.RecyclerViewData;
import ch.epfl.balelecbud.util.views.RefreshableRecyclerViewAdapter;

public class PointOfInterestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_of_interest);

        RecyclerView recyclerView = findViewById(R.id.pointOfInterestRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        RecyclerViewData<PointOfInterest, PointOfInterestHolder> data = new PointOfInterestData();
        RefreshableRecyclerViewAdapter<PointOfInterest, PointOfInterestHolder> adapter = new RefreshableRecyclerViewAdapter<>(
                PointOfInterestHolder::new, data, R.layout.item_point_of_interest);
        recyclerView.setAdapter(adapter);
        SwipeRefreshLayout refreshLayout = findViewById(R.id.swipe_refresh_layout_point_of_interest);
        adapter.setOnRefreshListener(refreshLayout);
    }
}
