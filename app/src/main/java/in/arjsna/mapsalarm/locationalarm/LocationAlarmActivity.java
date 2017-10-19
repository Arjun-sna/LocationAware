package in.arjsna.mapsalarm.locationalarm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.ResolvableApiException;
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
import in.arjsna.mapsalarm.di.qualifiers.ActivityContext;
import in.arjsna.mapsalarm.global.PermissionUtils;
import in.arjsna.mapsalarm.mvpbase.BaseActivity;
import javax.inject.Inject;

public class LocationAlarmActivity extends BaseActivity
    implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, LocationAlarmMVPContract.ILocationAlarmView {
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
  private static final int REQUEST_CHECK_SETTINGS = 2;

  private GoogleMap mMap;
  private boolean mPermissionDenied = false;
  private ImageView locationPin;

  @Inject
  public LocationAlarmMVPContract.ILocationPresenter<LocationAlarmMVPContract.ILocationAlarmView>
      locationPresenter;
  @Inject @ActivityContext public Context context;
  private Location currentLocation;
  private CardView locationAlarmLayout;
  private TextView dismissAlarmBtn;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_location_alarm);
    if (getActivityComponent() != null) {
      getActivityComponent().inject(this);
      locationPresenter.onAttach(this);
    }
    initView();
  }

  private void processIntent() {
    String action = getIntent().getAction();
    System.out.println("Action " + action);
    if (action != null && action.equals(LocationAwareService.LOCATION_REACHED)) {
      locationPresenter.onLocationReached();
    }
  }

  @Override protected void onResume() {
    super.onResume();
    processIntent();
  }

  private void initView() {
    //Toolbar toolbar = findViewById(R.id.toolbar);
    //setSupportActionBar(toolbar);
    locationPin = findViewById(R.id.location_pin);
    locationAlarmLayout = findViewById(R.id.alarm_view_layout);
    dismissAlarmBtn = findViewById(R.id.dismiss_btn);
    SupportMapFragment supportMapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    supportMapFragment.getMapAsync(this);
    bindEvents();
  }

  private void bindEvents() {
    locationPin.setOnClickListener(v -> locationPresenter.onLocationPinClicked());
    dismissAlarmBtn.setOnClickListener(v -> locationPresenter.dismissBtnClicked());
  }

  @Override public void showAddCheckPointDialog() {
    LatLng target = mMap.getCameraPosition().target;
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    View dialogView =
        LayoutInflater.from(context).inflate(R.layout.set_checkpoint_dialog_view, null, false);
    ((TextView) dialogView.findViewById(R.id.check_point_lat_tv)).setText(
        String.valueOf(target.latitude));
    ((TextView) dialogView.findViewById(R.id.check_point_long_tv)).setText(
        String.valueOf(target.longitude));
    EditText nameEditText = dialogView.findViewById(R.id.check_point_name_et);
    AlertDialog alertDialog = builder.setView(dialogView).show();
    RxView.clicks(dialogView.findViewById(R.id.set_checkpoint_done_btn)).subscribe(__ -> {
      String enteredText = nameEditText.getText().toString();
      if (enteredText.length() >= 4) {
        locationPresenter.onSetCheckPoint(enteredText, target.latitude, target.longitude);
        alertDialog.dismiss();
      } else {
        nameEditText.setError("Name should have minimum of 4 characters.");
      }
    });
    RxView.clicks(dialogView.findViewById(R.id.set_checkpoint_cancel_btn))
        .subscribe(__ -> alertDialog.dismiss());
  }

  @Override public void startLocationAwareService() {
    Intent serviceIntent = new Intent(context, LocationAwareService.class);
    startService(serviceIntent);
  }

  @Override public void startResolutionForLocation(ResolvableApiException resolvable) {
    try {
      resolvable.startResolutionForResult((Activity) context, REQUEST_CHECK_SETTINGS);
    } catch (IntentSender.SendIntentException e) {
      e.printStackTrace();
    }
  }

  @Override public void showError(String message) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }

  @Override public void getLocationDropMarker() {
    LatLng target = mMap.getCameraPosition().target;
    mMap.addMarker(
        new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin))
            .position(target)
            .draggable(false));
    mMap.addCircle(new CircleOptions().center(target).clickable(false).radius(200).strokeWidth(5));
    float[] results = new float[2];

    Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
        target.latitude, target.longitude, results);
    Toast.makeText(LocationAlarmActivity.this, " " + results[0] / 1000, Toast.LENGTH_SHORT).show();
  }

  private void getPermissionAndEnableLocation() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
          Manifest.permission.ACCESS_FINE_LOCATION, true);
    } else if (mMap != null) {
      mMap.setMyLocationEnabled(true);
      locationPresenter.onLocationPermissionGranted();
    }
  }

  @Override public boolean onMyLocationButtonClick() {
    return false;
  }

  @Override public void onMyLocationClick(@NonNull Location location) {
    Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
  }

  @Override public void onMapReady(GoogleMap googleMap) {
    initialiseMap(googleMap);
    getPermissionAndEnableLocation();
  }

  private void initialiseMap(GoogleMap googleMap) {
    mMap = googleMap;
    mMap.setOnMyLocationButtonClickListener(this);
    mMap.setOnMyLocationClickListener(this);
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
      return;
    }
    if (PermissionUtils.isPermissionGranted(permissions, grantResults,
        Manifest.permission.ACCESS_FINE_LOCATION)) {
      getPermissionAndEnableLocation();
    } else {
      mPermissionDenied = true;
    }
  }

  @Override protected void onResumeFragments() {
    super.onResumeFragments();
    if (mPermissionDenied) {
      showMissingPermissionError();
      mPermissionDenied = false;
    }
  }

  private void showMissingPermissionError() {
    PermissionUtils.PermissionDeniedDialog.newInstance(true)
        .show(getSupportFragmentManager(), "dialog");
  }

  @Override public void updateCurrentLocation(Location location) {
    this.currentLocation = location;
    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
        new LatLng(location.getLatitude(), location.getLongitude()), 15), 10, null);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    locationPresenter.onDetach();
  }

  @Override
  public void addMarkerOnMap(CheckPoint checkPoint) {
    mMap.addMarker(new MarkerOptions()
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag))
        .draggable(false)
        .position(new LatLng(checkPoint.getLatitude(), checkPoint.getLongitude()))
        .title(checkPoint.getName()));
  }

  @Override public void showAlarmLayout() {
    locationAlarmLayout.setVisibility(View.VISIBLE);
  }

  @Override public void hideAlarmLayout() {
    locationAlarmLayout.setVisibility(View.GONE);
  }

  @Override public void stopRinging() {
    Intent stopIntent = new Intent(LocationAlarmActivity.this, LocationAwareService.class);
    stopService(stopIntent);
  }
}
