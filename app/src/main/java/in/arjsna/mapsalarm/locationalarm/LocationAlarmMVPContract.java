package in.arjsna.mapsalarm.locationalarm;

import android.location.Location;
import com.google.android.gms.common.api.ResolvableApiException;
import in.arjsna.mapsalarm.mvpbase.IMVPPresenter;
import in.arjsna.mapsalarm.mvpbase.IMVPView;

public interface LocationAlarmMVPContract {
  interface ILocationAlarmView extends IMVPView {

    void updateCurrentLocation(Location location);

    void getLocationDropMarker();

    void showAddCheckPointDialog();

    void startLocationAwareService();

    void startResolutionForLocation(ResolvableApiException resolvable);

    void showError(String message);
  }

  interface ILocationPresenter<V extends ILocationAlarmView> extends IMVPPresenter<V> {

    void onLocationPermissionGranted();

    void onLocationPinClicked();

    void onSetCheckPoint(String checkpointName, double latitude, double longitude);
  }
}
