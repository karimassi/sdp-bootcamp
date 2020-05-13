package ch.epfl.balelecbud.view.friendship;

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
import ch.epfl.balelecbud.model.User;
import ch.epfl.balelecbud.utility.recyclerViews.RefreshableRecyclerViewAdapter;

public class ReceivedFriendRequestsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);

        Context context = view.getContext();
        final RecyclerView recyclerView = view.findViewById(R.id.recycler_view_friend_requests);
        View freshnessView = getView().findViewById(R.id.freshness_info_layout);

        ReceivedFriendRequestData data = new ReceivedFriendRequestData((User) getArguments().get("user"));
        final RefreshableRecyclerViewAdapter<User, ReceivedRequestViewHolder> adapter =
                new RefreshableRecyclerViewAdapter<>(ReceivedRequestViewHolder::new, freshnessView, data, R.layout.item_friend_request);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.swipe_refresh_layout_friend_requests);
        adapter.setOnRefreshListener(refreshLayout);

        return view;
    }

    public static ReceivedFriendRequestsFragment newInstance(User user) {

        Bundle args = new Bundle();
        args.putParcelable("user", user);
        ReceivedFriendRequestsFragment fragment = new ReceivedFriendRequestsFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
