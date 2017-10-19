package in.arjsna.mapsalarm.alarm;

import android.content.Context;
import in.arjsna.mapsalarm.db.CheckPointDataSource;
import in.arjsna.mapsalarm.di.qualifiers.ActivityContext;
import in.arjsna.mapsalarm.mvpbase.BasePresenter;
import javax.inject.Inject;

public class AlarmPresenter<V extends AlarmMVPContract.IAlarmView> extends BasePresenter<V>
    implements AlarmMVPContract.IAlarmPresenter<V> {

  @Inject
  public AlarmPresenter(@ActivityContext Context context,
      CheckPointDataSource checkPointDataSource) {
    super(context, checkPointDataSource);
  }

  @Override public void onViewInitialised() {

  }

  @Override public void onDismissButtonClicked() {
    getView().stopService();
  }
}
