package in.arjsna.mapsalarm.locationalarm;

import android.content.Context;
import in.arjsna.mapsalarm.global.LocationProvider;
import in.arjsna.mapsalarm.di.qualifiers.ActivityContext;
import in.arjsna.mapsalarm.mvpbase.BasePresenter;
import javax.inject.Inject;

public class LocationAlarmPresenter<V extends LocationAlarmMVPContract.ILocationAlarmView>
    extends BasePresenter<V> implements LocationAlarmMVPContract.ILocationPresenter<V> {

  @Inject LocationProvider locationProvider;

  @Inject public LocationAlarmPresenter(@ActivityContext Context context) {
    super(context);
  }

  @Override public void onLocationPermissionGranted() {
    locationProvider.getLocation(location -> getView().updateCurrentLocation(location));
  }
}
