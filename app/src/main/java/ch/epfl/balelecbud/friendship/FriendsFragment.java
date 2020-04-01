package ch.epfl.balelecbud.friendship;

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
import ch.epfl.balelecbud.models.User;

public class FriendsFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_friends, container, true);

        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_friends);
        final FriendsRecyclerViewAdapter adapter = new FriendsRecyclerViewAdapter((User) getArguments().get("user"));

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.swipe_refresh_layout_friends);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.reloadData();
                refreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    public static FriendsFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putParcelable("user", user);
        FriendsFragment fragment = new FriendsFragment();
        fragment.setArguments(args);
        return fragment;
    }
}