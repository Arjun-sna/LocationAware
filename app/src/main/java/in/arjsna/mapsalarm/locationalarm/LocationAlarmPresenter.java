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
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import javax.inject.Inject;

public class LocationAlarmPresenter<V extends LocationAlarmMVPContract.ILocationAlarmView>
    extends BasePresenter<V> implements LocationAlarmMVPContract.ILocationPresenter<V> {
  private ArrayList<CheckPoint> allCheckPoints;
  @Inject LocationProvider locationProvider;

  @Inject public LocationAlarmPresenter(@ActivityContext Context context,
      CheckPointDataSource checkPointDataSource) {
    super(context, checkPointDataSource);
  }

  @Override public void onLocationPermissionGranted() {
    updateCurrentLocation();
    addCheckPointMarkers();
  }

  private void addCheckPointMarkers() {
    getCheckPointDataSource().getAllCheckPoints()
        .toObservable()
        .doOnNext(checkPoints -> allCheckPoints = (ArrayList<CheckPoint>) checkPoints)
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
            getView().notifyListAdapter();
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

  @Override public void onMyLocationBtnClicked() {
    updateCurrentLocation();
  }

  @Override public void onCheckPointListBtnClicked() {
    getView().showBottomSheet();
  }

  @Override public int getCheckPointsCount() {
    return allCheckPoints != null ? allCheckPoints.size() : 0;
  }

  @Override public CheckPoint getCheckPointAt(int position) {
    return allCheckPoints != null ? allCheckPoints.get(position) : null;
  }

  @Override public void onCheckPointItemClicked(int adapterPosition) {
    CheckPoint checkPoint = allCheckPoints.get(adapterPosition);
    getView().updateCurrentLocation(checkPoint.getLatitude(), checkPoint.getLongitude());
  }

  @Override public void onDeleteCheckPoint(int adapterPosition) {
    getCheckPointDataSource().deleteCheckPoint(allCheckPoints.get(adapterPosition))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new CompletableObserver() {
          @Override public void onSubscribe(Disposable d) {

          }

          @Override public void onComplete() {
            allCheckPoints.remove(adapterPosition);
            getView().removeMarker(adapterPosition);
            getView().notifyListAdapter();
          }

          @Override public void onError(Throwable e) {
            getView().showError("Delete Failed");
          }
        });
  }

  @Override public void onEditCheckPoint(int adapterPosition) {
    // TODO: 20/10/17 edit
  }

  @Override public void onLocationSelected(double latitude, double longitude) {
    getView().updateCurrentLocation(latitude, longitude);
  }

  private void updateCurrentLocation() {
    locationProvider.getLastLocation(
        location -> getView().updateCurrentLocation(location.getLatitude(),
            location.getLongitude()));
  }

  private void insertCheckPoint(CheckPoint checkPoint) {
    getCheckPointDataSource().insertNewCheckPoint(checkPoint)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableSingleObserver<Boolean>() {
          @Override public void onSuccess(Boolean aBoolean) {
            allCheckPoints.add(checkPoint);
            getView().addMarkerOnMap(checkPoint);
            getView().notifyListAdapter();
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
