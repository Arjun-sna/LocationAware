package in.arjsna.mapsalarm.global;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import in.arjsna.mapsalarm.di.qualifiers.ActivityContext;
import javax.inject.Inject;

public class LocationProvider {
  private static final int REQUEST_CHECK_SETTINGS = 1;
  private final Context context;
  private FusedLocationProviderClient locationProviderClient;

  @Inject public LocationProvider(@ActivityContext Context context) {
    this.context = context;
    locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
  }

  @SuppressLint("MissingPermission")
  public void getLastLocation(AppLocationListener appLocationListener) {
    locationProviderClient.getLastLocation()
        .addOnSuccessListener((Activity) context, appLocationListener::onLocationAvailable);
  }

  public void setUpLocationRequest(OnSuccessListener<LocationSettingsResponse> successListener,
      OnFailureListener onFailureListener) {
    LocationRequest locationRequest = new LocationRequest();
    locationRequest.setInterval(2 * 60 * 1000);
    locationRequest.setFastestInterval(60000);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    LocationSettingsRequest.Builder builder =
        new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
    SettingsClient settingsClient = LocationServices.getSettingsClient(context);
    Task<LocationSettingsResponse> locationSettingsResponseTask =
        settingsClient.checkLocationSettings(builder.build());

    locationSettingsResponseTask.addOnSuccessListener(successListener);

    locationSettingsResponseTask.addOnFailureListener(onFailureListener);
  }
}
