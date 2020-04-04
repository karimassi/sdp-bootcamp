package ch.epfl.balelecbud;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import ch.epfl.balelecbud.schedule.ScheduleAdapter;
import ch.epfl.balelecbud.schedule.models.Slot;
import ch.epfl.balelecbud.util.intents.FlowUtil;
import ch.epfl.balelecbud.util.intents.IntentLauncher;

public class ScheduleActivity extends BasicActivity {

    private ScheduleAdapter mAdapter;

    private static final String TAG = ScheduleAdapter.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Log.d(TAG, "onCreate: Creation of the activity");
        RecyclerView rvSchedule = findViewById(R.id.scheduleRecyclerView);
        List<Slot> slots = FlowUtil.unpackCallback(getIntent());
        mAdapter = new ScheduleAdapter(this, slots == null ?
                Collections.<Slot>emptyList() : slots);
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        rvSchedule.setAdapter(mAdapter);

        this.configureToolBar(R.id.schedule_activity_toolbar);
        this.configureDrawerLayout(R.id.schedule_activity_drawer_layout);
        this.configureNavigationView(R.id.schedule_activity_nav_view);
    }

    @VisibleForTesting
    public void setIntentLauncher(IntentLauncher intentLauncher) {
        this.mAdapter.setIntentLauncher(intentLauncher);
    }

}
