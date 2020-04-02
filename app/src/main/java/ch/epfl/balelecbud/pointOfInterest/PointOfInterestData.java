package ch.epfl.balelecbud.pointOfInterest;

import androidx.annotation.VisibleForTesting;

import java.util.LinkedList;

import ch.epfl.balelecbud.models.Location;
import ch.epfl.balelecbud.util.CompletableFutureUtils;
import ch.epfl.balelecbud.util.database.DatabaseWrapper;
import ch.epfl.balelecbud.util.database.FirestoreDatabaseWrapper;
import ch.epfl.balelecbud.util.database.MyQuery;
import ch.epfl.balelecbud.util.views.RecyclerViewData;

public class PointOfInterestData extends RecyclerViewData<PointOfInterest, PointOfInterestHolder> {

    private static DatabaseWrapper databaseImplementation = FirestoreDatabaseWrapper.getInstance();

    @VisibleForTesting
    public static void setDatabaseImplementation(DatabaseWrapper db){
        databaseImplementation = db;
    }

    @Override
    public void reload() {
        MyQuery query = new MyQuery(DatabaseWrapper.POINT_OF_INTEREST_PATH, new LinkedList<>());
        databaseImplementation.query(query, PointOfInterest.class)
                .whenComplete(new CompletableFutureUtils.MergeBiConsumer<>(this));
    }

    @Override
    public void bind(int index, PointOfInterestHolder viewHolder) {
        viewHolder.nameTextView.setText(data.get(index).getName());
        viewHolder.typeTextView.setText(data.get(index).getType());
        viewHolder.locationTextView.setText(new Location(data.get(index).getLocation()).toString());
    }
}
