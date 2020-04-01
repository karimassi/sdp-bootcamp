package ch.epfl.balelecbud.location;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationRequest;

public final class LocationUtil {
    private final static String TAG = LocationUtil.class.getSimpleName();
    private static final String LOCATION_ENABLE_FILE = TAG + ".LOCATION_ENABLE_FILE";
    private static final String LOCATION_ENABLE_KEY = TAG + ".LOCATION_ENABLE_KEY";
    private final static long FASTEST_UPDATE_INTERVAL = 30_000;
    public final static int LOCATION_PERMISSIONS_REQUEST_CODE = 34;
    private final static long UPDATE_INTERVAL = 60_000;
    private final static long MAX_WAIT_TIME = UPDATE_INTERVAL;
    private static LocationRequest locationRequest;
    private static LocationClient client;

    private static void createLocationRequest() {
        locationRequest = new LocationRequest();

        locationRequest.setInterval(LocationUtil.UPDATE_INTERVAL);
        locationRequest.setFastestInterval(LocationUtil.FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setMaxWaitTime(LocationUtil.MAX_WAIT_TIME);
    }

    @VisibleForTesting
    public static void setLocationClient(LocationClient client) {
        LocationUtil.client = client;
    }

    private static LocationClient getClient(Context context) {
        if (LocationUtil.client == null)
            LocationUtil.client = new FusedLocationClientAdapter(context);
        return LocationUtil.client;
    }

    private static LocationRequest getLocationRequestInstance() {
        if (locationRequest == null)
            createLocationRequest();

        return locationRequest;
    }

    /**
     * Request the permissions required for the location service
     *
     * @param activity the activity requesting the permissions
     */
    public static void requestLocationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(activity,
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                    LocationUtil.LOCATION_PERMISSIONS_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION },
                    LocationUtil.LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    public interface Action {
        void perform();
    }

    /**
     * Handle the result of the request for permissions needed for the location service
     *
     * @param grantResults          the results of the request
     * @param onPermissionCanceled  an action to perform if the permission request is canceled
     * @param onPermissionGranted   an action to perform if the permission request is granted
     * @param onPermissionDenied    an action to perform if the permission request is denied
     */
    public static void onLocationRequestPermissionsResult(@NonNull int[] grantResults,
                                                          @NonNull Action onPermissionCanceled,
                                                          @NonNull Action onPermissionGranted,
                                                          @NonNull Action onPermissionDenied) {
        if (grantResults.length <= 0) {
            onPermissionCanceled.perform();
        } else if (isLocationPermissionGranted(grantResults)) {
            onPermissionGranted.perform();
        } else {
            onPermissionDenied.perform();
        }
    }

    private static boolean isLocationPermissionGranted(@NonNull int[] grantResults) {
        return (grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                        grantResults[1] == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Check if the location service is activated or not
     *
     * @param context the context from which to check the location service status
     * @return        the location service status
     */
    public static boolean isLocationActive(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(LOCATION_ENABLE_FILE, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(LocationUtil.LOCATION_ENABLE_KEY, false);
    }

    /**
     * Enable the location service
     *
     * @param context the context from which the order comes from
     */
    public static void enableLocation(Context context) {
        changedLocationState(context, true);
    }

    /**
     * Disable the location service
     *
     * @param context the context from which the order comes from
     */
    public static void disableLocation(Context context) {
        changedLocationState(context, false);
    }

    private static void changedLocationState(Context context, boolean status) {
        if (status) {
            requestLocationUpdates(context);
        } else {
            removeLocationUpdates(context);
        }
        SharedPreferences sharedPref = context.getSharedPreferences(LOCATION_ENABLE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(LocationUtil.LOCATION_ENABLE_KEY, status);
        editor.apply();
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, LocationService.class);
        intent.setAction(LocationService.ACTION_PROCESS_UPDATES);
        return PendingIntent.getService(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void requestLocationUpdates(Context context) {
        try {
            Log.i(TAG, "Starting location updates");
            getClient(context).requestLocationUpdates(getLocationRequestInstance(), getPendingIntent(context));
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private static void removeLocationUpdates(Context context) {
        Log.i(TAG, "Removing location updates");
        getClient(context).removeLocationUpdates(getPendingIntent(context));
    }

}