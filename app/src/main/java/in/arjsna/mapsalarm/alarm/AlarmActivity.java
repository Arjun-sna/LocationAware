package in.arjsna.mapsalarm.alarm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jakewharton.rxbinding2.view.RxView;
import in.arjsna.mapsalarm.R;
import in.arjsna.mapsalarm.bgservice.LocationAwareService;
import in.arjsna.mapsalarm.db.CheckPoint;
import in.arjsna.mapsalarm.mvpbase.BaseActivity;
import javax.inject.Inject;

public class AlarmActivity extends BaseActivity implements AlarmMVPContract.IAlarmView,
    OnMapReadyCallback {
  private PowerManager.WakeLock mWakeLock;

  @Inject
  AlarmMVPContract.IAlarmPresenter<AlarmMVPContract.IAlarmView> alarmPresenter;
  private View dismissButton;

  private GoogleMap googleMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
        PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "AlarmActivity");
    mWakeLock.acquire(2 * 60 * 1000L);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

    setContentView(R.layout.activity_alarm);
    if (getActivityComponent() != null) {
      getActivityComponent().inject(this);
      alarmPresenter.onAttach(this);
    }
    initViews();
    bindEvents();
    processIntent();
  }

  private void processIntent() {
    String action = getIntent().getAction();
    if (action != null && action.equals(LocationAwareService.LOCATION_REACHED)) {
      CheckPoint currentCheckPoint =
          getIntent().getParcelableExtra(LocationAwareService.CURRENT_CHECKPOINT);
      alarmPresenter.onIntentDataAvailable(currentCheckPoint);
    }
  }

  private void bindEvents() {
    RxView.clicks(dismissButton)
        .subscribe(__ -> alarmPresenter.onDismissButtonClicked());
  }

  private void initViews() {
    dismissButton = findViewById(R.id.dismiss_btn);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    alarmPresenter.onViewInitialised();
  }

  @Override
  public void loadMap() {
    SupportMapFragment supportMapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    supportMapFragment.getMapAsync(this);
  }

  @Override public void stopRinging() {
    Intent stopIntent = new Intent(AlarmActivity.this, LocationAwareService.class);
    stopService(stopIntent);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mWakeLock.release();
  }

  @Override public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;
    alarmPresenter.onMapReady();
  }

  @Override
  public void setMarkerOnMap(CheckPoint checkPoint) {
    if (googleMap != null) {
      googleMap.addMarker(new MarkerOptions().position(
          new LatLng(checkPoint.getLatitude(), checkPoint.getLongitude()))
          .draggable(false)
          .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag)));
    }
  }
}
