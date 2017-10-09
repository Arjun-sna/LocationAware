package in.arjsna.mapsalarm.locationalarm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import in.arjsna.mapsalarm.R;
import in.arjsna.mapsalarm.global.PermissionUtils;
import in.arjsna.mapsalarm.mvpbase.BaseActivity;
import javax.inject.Inject;

public class LocationAlarmActivity extends BaseActivity
    implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, LocationAlarmMVPContract.ILocationAlarmView {
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
  private static final long MIN_TIME = 400;
  private static final float MIN_DISTANCE = 1000;

  private GoogleMap mMap;
  private boolean mPermissionDenied = false;
  private FrameLayout mMapHolderLayout;
  private ImageView locationPin;

  @Inject
  public LocationAlarmMVPContract.ILocationPresenter<LocationAlarmMVPContract.ILocationAlarmView>
      locationPresenter;
  private Location currentLocation;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_location_alarm);
    if (getActivityComponent() != null) {
      getActivityComponent().inject(this);
      locationPresenter.onAttach(this);
    }
    initView();
  }

  private void initView() {
    //Toolbar toolbar = findViewById(R.id.toolbar);
    //setSupportActionBar(toolbar);
    locationPin = findViewById(R.id.location_pin);
    SupportMapFragment supportMapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    supportMapFragment.getMapAsync(this);
    bindEvents();
  }

  private void bindEvents() {
    locationPin.setOnClickListener(v -> {
      locationPresenter.onLocationPinClicked();
    });
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
}
