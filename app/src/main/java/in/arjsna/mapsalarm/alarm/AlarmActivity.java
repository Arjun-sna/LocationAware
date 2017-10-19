package in.arjsna.mapsalarm.alarm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import com.jakewharton.rxbinding2.view.RxView;
import in.arjsna.mapsalarm.R;
import in.arjsna.mapsalarm.bgservice.LocationAwareService;
import in.arjsna.mapsalarm.mvpbase.BaseActivity;
import javax.inject.Inject;

public class AlarmActivity extends BaseActivity implements AlarmMVPContract.IAlarmView {
  private PowerManager.WakeLock mWakeLock;

  @Inject
  AlarmMVPContract.IAlarmPresenter<AlarmMVPContract.IAlarmView> alarmPresenter;
  private View dismissButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
        PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "AlarmActivity");
    mWakeLock.acquire();
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

    setContentView(R.layout.activity_alarm);
    if (getActivityComponent() != null) {
      getActivityComponent().inject(this);
      alarmPresenter.onAttach(this);
    }
    initViews();
    bindEvents();
  }

  private void bindEvents() {
    RxView.clicks(dismissButton)
        .subscribe(__ -> alarmPresenter.onDismissButtonClicked());
  }

  private void initViews() {
    dismissButton = findViewById(R.id.alarm_dismiss_view);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    alarmPresenter.onViewInitialised();
  }

  @Override public void stopService() {
    Intent stopIntent = new Intent(AlarmActivity.this, LocationAwareService.class);
    stopService(stopIntent);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mWakeLock.release();
  }
}
