package in.arjsna.mapsalarm.locationalarm;

import in.arjsna.mapsalarm.mvpbase.BasePresenter;

public class LocationAlarmPresenter<V extends LocationAlarmMVPContract.ILocationAlarmView>
    extends BasePresenter<V> implements LocationAlarmMVPContract.ILocationPresenter<V> {
}
