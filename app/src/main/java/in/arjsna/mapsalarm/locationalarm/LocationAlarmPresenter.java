package in.arjsna.mapsalarm.locationalarm;

import android.content.Context;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import in.arjsna.mapsalarm.db.CheckPoint;
import in.arjsna.mapsalarm.db.CheckPointDataSource;
import in.arjsna.mapsalarm.di.qualifiers.ActivityContext;
import in.arjsna.mapsalarm.global.LocationProvider;
import in.arjsna.mapsalarm.mvpbase.BasePresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import javax.inject.Inject;

public class LocationAlarmPresenter<V extends LocationAlarmMVPContract.ILocationAlarmView>
    extends BasePresenter<V> implements LocationAlarmMVPContract.ILocationPresenter<V> {

  @Inject LocationProvider locationProvider;

  @Inject public LocationAlarmPresenter(@ActivityContext Context context,
      CheckPointDataSource checkPointDataSource) {
    super(context, checkPointDataSource);
  }

  @Override public void onLocationPermissionGranted() {
    locationProvider.getLastLocation(location -> getView().updateCurrentLocation(location));
  }

  @Override public void onLocationPinClicked() {
    getView().showAddCheckPointDialog();
  }

  @Override public void onSetCheckPoint(String checkpointName, double latitude, double longitude) {
    CheckPoint checkPoint = new CheckPoint();
    checkPoint.setActive(true);
    checkPoint.setName(checkpointName);
    checkPoint.setLatitude(latitude);
    checkPoint.setLongitude(longitude);
    getCheckPointDataSource().insertNewCheckPoint(checkPoint)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableSingleObserver<Boolean>() {
          @Override public void onSuccess(Boolean aBoolean) {
            locationProvider.setUpLocationRequest(
                settingsResponse -> getView().startLocationAwareService(), e -> {
                  int statusCode = ((ApiException) e).getStatusCode();
                  switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                      getView().startResolutionForLocation((ResolvableApiException) e);
                      break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                      getView().showError("Location setting error");
                      break;
                  }
                });
          }

          @Override public void onError(Throwable e) {
            getView().showError("Failed to add alarm");
          }
        });
  }
}
