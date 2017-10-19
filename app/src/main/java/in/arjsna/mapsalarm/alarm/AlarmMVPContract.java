package in.arjsna.mapsalarm.alarm;

import in.arjsna.mapsalarm.db.CheckPoint;
import in.arjsna.mapsalarm.mvpbase.IMVPPresenter;
import in.arjsna.mapsalarm.mvpbase.IMVPView;

public interface AlarmMVPContract {
  interface IAlarmView extends IMVPView {

    void stopRinging();

    void loadMap();

    void setMarkerOnMap(CheckPoint checkPoint);
  }

  interface IAlarmPresenter<V extends IAlarmView> extends IMVPPresenter<V> {
    void onViewInitialised();

    void onDismissButtonClicked();

    void onIntentDataAvailable(CheckPoint currentCheckPoint);

    void onMapReady();
  }
}
