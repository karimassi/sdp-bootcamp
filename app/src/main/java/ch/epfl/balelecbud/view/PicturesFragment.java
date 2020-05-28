package ch.epfl.balelecbud.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import ch.epfl.balelecbud.R;
import ch.epfl.balelecbud.model.Picture;
import ch.epfl.balelecbud.utility.DateFormatter;
import ch.epfl.balelecbud.utility.storage.Storage;

import static ch.epfl.balelecbud.BalelecbudApplication.getAppStorage;
import static ch.epfl.balelecbud.utility.storage.Storage.USER_PICTURES;

public final class PicturesFragment extends Fragment {

    public static PicturesFragment newInstance() {
        return (new PicturesFragment());
    }

    private String currentPhotoPath;

    private static String TAG = PicturesFragment.class.getSimpleName();

    private static final int CAMERA_PERM_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;


    @Override
    public void onStart() {
        super.onStart();
        FragmentActivity activity = getActivity();
        Button cameraButton = activity.findViewById(R.id.takePicBtn);
        cameraButton.setOnClickListener(v -> askCameraPermissions());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.fragment_pictures, container, false);
    }

    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else{
            dispatchTakePictureIntent();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else {
                Toast.makeText(getActivity(), R.string.camera_permission_refused_msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                Log.d(TAG, "Absolute Url of Image is " + Uri.fromFile(f));
                String filename = f.getName();
                try {
                    getAppStorage().putFile(USER_PICTURES, filename, f);
                } catch (IOException e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
        }

    }

    private File createImageFile() throws IOException {
        String timeStamp = DateFormatter.FILE_TIMESTAMP.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getFilesDir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "ch.epfl.balelecbud.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }
}
