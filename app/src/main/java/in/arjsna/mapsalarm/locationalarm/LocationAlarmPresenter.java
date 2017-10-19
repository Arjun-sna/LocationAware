package in.arjsna.mapsalarm.locationalarm;

import android.content.Context;
import android.location.Location;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import in.arjsna.mapsalarm.bgservice.LocationAwareService;
import in.arjsna.mapsalarm.db.CheckPoint;
import in.arjsna.mapsalarm.db.CheckPointDataSource;
import in.arjsna.mapsalarm.di.qualifiers.ActivityContext;
import in.arjsna.mapsalarm.global.LocationProvider;
import in.arjsna.mapsalarm.mvpbase.BasePresenter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
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
    addCheckPointMarkers();
  }

  private void addCheckPointMarkers() {
    getCheckPointDataSource().getAllCheckPoints()
        .toObservable()
        .flatMap(Observable::fromIterable)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableObserver<CheckPoint>() {
          @Override public void onNext(CheckPoint checkPoint) {
            getView().addMarkerOnMap(checkPoint);
          }

          @Override public void onError(Throwable e) {
            getView().showError(e.getLocalizedMessage());
          }

          @Override public void onComplete() {

          }
        });
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
    locationProvider.getLastLocation(location -> {
      float[] results = new float[3];
      Location.distanceBetween(location.getLatitude(), location.getLongitude(),
          checkPoint.getLatitude(), checkPoint.getLongitude(), results);
      if (results[0] < LocationAwareService.MAX_DISTANCE_RANGE) {
        getView().showError("You are already near to the location specified");
      } else {
        insertCheckPoint(checkPoint);
      }
    });
  }

  private void insertCheckPoint(CheckPoint checkPoint) {
    getCheckPointDataSource().insertNewCheckPoint(checkPoint)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableSingleObserver<Boolean>() {
          @Override public void onSuccess(Boolean aBoolean) {
            getView().addMarkerOnMap(checkPoint);
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
