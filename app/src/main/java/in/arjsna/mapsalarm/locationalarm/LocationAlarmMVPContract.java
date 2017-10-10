package in.arjsna.mapsalarm.locationalarm;

import android.location.Location;
import in.arjsna.mapsalarm.mvpbase.IMVPPresenter;
import in.arjsna.mapsalarm.mvpbase.IMVPView;

public interface LocationAlarmMVPContract {
  interface ILocationAlarmView extends IMVPView {

    void updateCurrentLocation(Location location);

    void getLocationDropMarker();

    void showAddCheckPointDialog();
  }

  interface ILocationPresenter<V extends ILocationAlarmView> extends IMVPPresenter<V> {

    void onLocationPermissionGranted();

    void onLocationPinClicked();
  }
}
