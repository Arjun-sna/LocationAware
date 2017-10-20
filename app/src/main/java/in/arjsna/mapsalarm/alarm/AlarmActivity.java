package in.arjsna.mapsalarm.alarm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jakewharton.rxbinding2.view.RxView;
import in.arjsna.mapsalarm.R;
import in.arjsna.mapsalarm.bgservice.LocationAwareService;
import in.arjsna.mapsalarm.db.CheckPoint;
import in.arjsna.mapsalarm.locationalarm.LocationAlarmActivity;
import in.arjsna.mapsalarm.mvpbase.BaseActivity;
import javax.inject.Inject;

public class AlarmActivity extends BaseActivity implements AlarmMVPContract.IAlarmView,
    OnMapReadyCallback {
  private PowerManager.WakeLock mWakeLock;

  @Inject
  AlarmMVPContract.IAlarmPresenter<AlarmMVPContract.IAlarmView> alarmPresenter;
  private View dismissButton;

  private GoogleMap googleMap;
  private TextView checkPointName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
        PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "AlarmActivity");
    mWakeLock.acquire(2 * 60 * 1000L);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Window window = getWindow();
      window.getDecorView().setSystemUiVisibility(
          View.SYSTEM_UI_FLAG_LAYOUT_STABLE
              | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
      window.setStatusBarColor(Color.TRANSPARENT);
    }

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
    checkPointName = findViewById(R.id.check_point_name_tv);
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
    Intent locationAlarmIntent = new Intent(AlarmActivity.this, LocationAlarmActivity.class);
    locationAlarmIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(locationAlarmIntent);
    finish();
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
      LatLng latLng = new LatLng(checkPoint.getLatitude(), checkPoint.getLongitude());
      googleMap.addMarker(new MarkerOptions().position(
          latLng)
          .draggable(false)
          .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag)));
      googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15), 10, null);
      googleMap.addCircle(new CircleOptions().center(latLng)
          .strokeWidth(5)
          .strokeColor(getResources().getColor(R.color.cardview_dark_background))
          .radius(500));
    }
  }

  @Override
  public void setCheckPointName(String name) {
    checkPointName.setText(name);
  }
}
