package in.arjsna.mapsalarm.alarm;

import in.arjsna.mapsalarm.mvpbase.IMVPPresenter;
import in.arjsna.mapsalarm.mvpbase.IMVPView;

public interface AlarmMVPContract {
  interface IAlarmView extends IMVPView {

    void stopService();
  }

  interface IAlarmPresenter<V extends IAlarmView> extends IMVPPresenter<V> {
    void onViewInitialised();

    void onDismissButtonClicked();
  }
}
