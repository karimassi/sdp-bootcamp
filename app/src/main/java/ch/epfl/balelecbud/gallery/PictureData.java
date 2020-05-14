package ch.epfl.balelecbud.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import ch.epfl.balelecbud.utility.database.Database;
import ch.epfl.balelecbud.utility.recyclerViews.RecyclerViewData;

import static ch.epfl.balelecbud.BalelecbudApplication.getAppStorage;

public class PictureData extends RecyclerViewData<Picture, PictureHolder> {
    private static final String TAG = PictureData.class.getSimpleName();

    public PictureData() {
        super();
    }

    @Override
    public void reload(Database.Source preferredSource) {
        StorageReference listRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReference().child("users_pictures");
        listRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        Picture p = new Picture(item.getPath().substring(1));
                        if(super.size() != listResult.getItems().size())
                            super.add(super.size(), p);
                    }
                });
    }

    @Override
    public void bind(int index, PictureHolder viewHolder) {
        final Picture picture = data.get(index);
        CompletableFuture<File> imageDownload = getAppStorage().getFile(picture.getImageFileName());
        imageDownload.whenComplete((file, t) -> {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            viewHolder.imageView.setImageBitmap(bitmap);
            viewHolder.imageView.setVisibility(View.VISIBLE);
        });
    }
}
