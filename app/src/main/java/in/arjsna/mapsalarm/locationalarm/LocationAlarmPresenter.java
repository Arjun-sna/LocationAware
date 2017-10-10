package in.arjsna.mapsalarm.locationalarm;

import android.content.Context;
import in.arjsna.mapsalarm.db.CheckPoint;
import in.arjsna.mapsalarm.db.CheckPointDataSource;
import in.arjsna.mapsalarm.di.qualifiers.ActivityContext;
import in.arjsna.mapsalarm.global.LocationProvider;
import in.arjsna.mapsalarm.mvpbase.BasePresenter;
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
    locationProvider.getLocation(location -> getView().updateCurrentLocation(location));
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
        .subscribeWith(new DisposableSingleObserver<Boolean>() {
          @Override public void onSuccess(Boolean aBoolean) {

          }

          @Override public void onError(Throwable e) {

          }
        });
  }
}
