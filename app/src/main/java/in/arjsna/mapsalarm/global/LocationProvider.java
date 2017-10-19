package in.arjsna.mapsalarm.global;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationProvider {
  private static final int REQUEST_CHECK_SETTINGS = 1;
  private static final int LOCATION_UPDATE_INTERVAL = 15 * 1000;
  private static final int LOCATION_UPDATE_FASTEST_INTERVAL = 10 * 1000;
  private final Context context;
  private FusedLocationProviderClient locationProviderClient;

  public LocationProvider(Context context) {
    this.context = context;
    locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
  }

  @SuppressLint("MissingPermission")
  public void getLastLocation(AppLocationListener appLocationListener) {
    locationProviderClient.getLastLocation()
        .addOnSuccessListener(appLocationListener::onLocationAvailable);
  }

  public void setUpLocationRequest(OnSuccessListener<LocationSettingsResponse> successListener,
      OnFailureListener onFailureListener) {
    LocationRequest locationRequest = getLocationRequest();
    LocationSettingsRequest.Builder builder =
        new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
    SettingsClient settingsClient = LocationServices.getSettingsClient(context);
    Task<LocationSettingsResponse> locationSettingsResponseTask =
        settingsClient.checkLocationSettings(builder.build());

    locationSettingsResponseTask.addOnSuccessListener(successListener);

    locationSettingsResponseTask.addOnFailureListener(onFailureListener);
  }

  @NonNull private LocationRequest getLocationRequest() {
    LocationRequest locationRequest = new LocationRequest();
    locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
    locationRequest.setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    return locationRequest;
  }

  @SuppressLint("MissingPermission")
  public void startLocationUpdates(LocationCallback locationCallback) {
    locationProviderClient.requestLocationUpdates(getLocationRequest(), locationCallback, null);
  }

  public void stopLocationUpdates(LocationCallback locationCallback) {
    locationProviderClient.removeLocationUpdates(locationCallback);
  }
}
