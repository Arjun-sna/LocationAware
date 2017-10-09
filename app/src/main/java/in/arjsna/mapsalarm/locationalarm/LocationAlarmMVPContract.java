package in.arjsna.mapsalarm.locationalarm;

import in.arjsna.mapsalarm.mvpbase.IMVPPresenter;
import in.arjsna.mapsalarm.mvpbase.IMVPView;

public interface LocationAlarmMVPContract {
  interface ILocationAlarmView extends IMVPView {

  }

  interface ILocationPresenter<V extends ILocationAlarmView> extends IMVPPresenter<V> {

  }
}
