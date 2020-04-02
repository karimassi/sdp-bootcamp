package ch.epfl.balelecbud.friendship;

import android.view.View;

import ch.epfl.balelecbud.models.User;
import ch.epfl.balelecbud.util.CompletableFutureUtils;
import ch.epfl.balelecbud.util.views.RecyclerViewData;

public class FriendRequestData extends RecyclerViewData<User, RequestViewHolder> {

    private User currentUser;

    public FriendRequestData(User user) {
        super();
        currentUser = user;
    }

    @Override
    public void reload() {
        FriendshipUtils.getRequestsUids(currentUser)
                .thenCompose( strings -> CompletableFutureUtils.unify(FriendshipUtils.getFriends(strings)))
                .whenComplete(new CompletableFutureUtils.MergeBiConsumer<>(this));
    }

    @Override
    public void bind(final int index, RequestViewHolder viewHolder) {
        viewHolder.friendName.setText(data.get(index).getDisplayName());
        viewHolder.acceptButton.setOnClickListener(v -> {
            FriendshipUtils.acceptRequest(data.get(index));
            remove(index);
        });
        viewHolder.deleteButton.setOnClickListener(v -> {
            FriendshipUtils.deleteRequest(data.get(index));
            remove(index);
        });
    }


}