package in.arjsna.mapsalarm.locationalarm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    implements OnMapReadyCallback,
    LocationAlarmMVPContract.ILocationAlarmView {
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
  private static final int REQUEST_CHECK_SETTINGS = 2;
  private static double LATITUDE_PAN = 0;

  private GoogleMap mMap;
  private boolean mPermissionDenied = false;
  private ImageView locationPin;

  @Inject
  public CheckPointsAdapter checkPointsAdapter;

  @Inject
  public LocationAlarmMVPContract.ILocationPresenter<LocationAlarmMVPContract.ILocationAlarmView>
      locationPresenter;
  @Inject
  @ActivityContext
  public Context context;
  private FloatingActionButton currentLocationBtn;
  private FloatingActionButton checkPointsListBtn;
  private BottomSheetBehavior mBottomSheetBehavior;

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
    locationPin = findViewById(R.id.location_pin);
    currentLocationBtn = findViewById(R.id.my_location_btn);
    checkPointsListBtn = findViewById(R.id.check_points_list_btn);
    View bottomSheet = findViewById(R.id.bottom_sheet);
    mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
    mBottomSheetBehavior.setHideable(true);
    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    RecyclerView checkPointsList = findViewById(R.id.check_point_list_view);
    checkPointsList.setLayoutManager(new LinearLayoutManager(context));
    checkPointsList.setAdapter(checkPointsAdapter);

    SupportMapFragment supportMapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    supportMapFragment.getMapAsync(this);
    bindEvents();
  }

  private void bindEvents() {
    locationPin.setOnClickListener(v -> locationPresenter.onLocationPinClicked());
    currentLocationBtn.setOnClickListener(v -> locationPresenter.onMyLocationBtnClicked());
    checkPointsListBtn.setOnClickListener(v -> locationPresenter.onCheckPointListBtnClicked());
    mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
          locationPin.setVisibility(View.GONE);
          LATITUDE_PAN = 0.005;
        } else {
          locationPin.setVisibility(View.VISIBLE);
          LATITUDE_PAN = 0;
        }
      }

      @Override public void onSlide(@NonNull View bottomSheet, float slideOffset) {

      }
    });
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

  private void getPermissionAndEnableLocation() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
          Manifest.permission.ACCESS_FINE_LOCATION, true);
    } else if (mMap != null) {
      locationPresenter.onLocationPermissionGranted();
    }
  }

  @Override public void onMapReady(GoogleMap googleMap) {
    initialiseMap(googleMap);
    getPermissionAndEnableLocation();
  }

  private void initialiseMap(GoogleMap googleMap) {
    mMap = googleMap;
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

  @Override public void updateCurrentLocation(double latitude, double longitude) {
    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
        new LatLng(latitude - LATITUDE_PAN, longitude), 15), 1000, null);
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

  @Override
  public void showBottomSheet() {
    if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
      mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    } else {
      mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
  }

  @Override public void notifyListAdapter() {
    checkPointsAdapter.notifyDataSetChanged();
  }
}
