package in.arjsna.mapsalarm.global;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import in.arjsna.mapsalarm.di.qualifiers.ActivityContext;
import in.arjsna.mapsalarm.global.AppLocationListener;
import javax.inject.Inject;

public class LocationProvider {
  private final Context context;
  private FusedLocationProviderClient locationProviderClient;

  @Inject public LocationProvider(@ActivityContext Context context) {
    this.context = context;
    locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
  }

  @SuppressLint("MissingPermission")
  public void getLocation(AppLocationListener appLocationListener) {
    locationProviderClient.getLastLocation().addOnSuccessListener((Activity) context,
        appLocationListener::onLocationAvailable);
  }
}
