package in.arjsna.mapsalarm.bgservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import in.arjsna.mapsalarm.R;
import in.arjsna.mapsalarm.alarm.AlarmActivity;
import in.arjsna.mapsalarm.db.CheckPoint;
import in.arjsna.mapsalarm.db.CheckPointDataSource;
import in.arjsna.mapsalarm.di.components.DaggerServiceComponent;
import in.arjsna.mapsalarm.di.components.ServiceComponent;
import in.arjsna.mapsalarm.di.modules.ServiceModule;
import in.arjsna.mapsalarm.global.LocationAware;
import in.arjsna.mapsalarm.global.LocationProvider;
import in.arjsna.mapsalarm.locationalarm.LocationAlarmActivity;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

public class LocationAwareService extends Service {
  private static final int NOTIFY_ID = 100;
  public static final String LOCATION_REACHED = "LOCATION_REACHED";
  public final static int MAX_DISTANCE_RANGE = 500;
  // Time period between two vibration events
  private final static int VIBRATE_DELAY_TIME = 2000;
  // Vibrate for 1000 milliseconds
  private final static int DURATION_OF_VIBRATION = 1000;
  // Increase alarm volume gradually every 600ms
  private final static int VOLUME_INCREASE_DELAY = 600;
  // Volume level increasing step
  private final static float VOLUME_INCREASE_STEP = 0.01f;
  // Max player volume level
  private final static float MAX_VOLUME = 1.0f;

  @Inject
  public LocationProvider locationProvider;

  @Inject
  CheckPointDataSource checkPointDataSource;

  @Inject
  CompositeDisposable compositeDisposable;
  private MediaPlayer mPlayer;
  private Vibrator mVibrator;
  private float mVolumeLevel = 0;
  private Handler mHandler = new Handler();

  @Override public void onCreate() {
    super.onCreate();
    ServiceComponent serviceComponent = DaggerServiceComponent.builder()
        .serviceModule(new ServiceModule(this))
        .applicationComponent(((LocationAware) getApplication()).getApplicationComponent())
        .build();
    serviceComponent.inject(this);
  }

  private Location currentLocation;
  LocationCallback locationCallback = new LocationCallback() {
    @Override public void onLocationResult(LocationResult locationResult) {
      for (Location location : locationResult.getLocations()) {
        Log.i("Debug ", "On Location Available " + location.toString());
        currentLocation = location;
        matchForCheckPoints();
      }
    }

    @Override public void onLocationAvailability(LocationAvailability locationAvailability) {
      Log.i("Debug ", "On Location availability");
    }
  };

  private void matchForCheckPoints() {
    compositeDisposable.add(checkPointDataSource.getAllCheckPoints()
        .toObservable()
        .flatMap(Observable::fromIterable)
        .filter(checkPoint -> {
          float[] results = new float[3];
          Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
              checkPoint.getLatitude(), checkPoint.getLongitude(), results);
          return results[0] < MAX_DISTANCE_RANGE;
        })
        .doOnNext(checkPoint -> checkPointDataSource.deleteCheckPoint(checkPoint).subscribe())
        .subscribeOn(Schedulers.io())
        .subscribeWith(new DisposableObserver<CheckPoint>() {
          @Override public void onNext(CheckPoint checkPoint) {
            Log.i("Debug ", "Location reached");
            openActivity();
            startPlayer();
          }

          @Override public void onError(Throwable e) {

          }

          @Override public void onComplete() {
            //// TODO: 15/10/17 schedule next job
          }
        }));
  }

  private MediaPlayer.OnErrorListener mErrorListener = (mp, what, extra) -> {
    stopPlayer();
    LocationAwareService.this.stopSelf();
    return true;
  };

  private void stopPlayer() {
    if (mPlayer != null) {
      mPlayer.stop();
      mPlayer.release();
      mHandler.removeCallbacksAndMessages(null);
    }
  }

  private Runnable mVibrationRunnable = new Runnable() {
    @Override
    public void run() {
      mVibrator.vibrate(DURATION_OF_VIBRATION);
      // Provide loop for vibration
      mHandler.postDelayed(mVibrationRunnable,
          DURATION_OF_VIBRATION + VIBRATE_DELAY_TIME);
    }
  };

  private Runnable mVolumeRunnable = new Runnable() {
    @Override
    public void run() {
      // increase volume level until reach max value
      if (mPlayer != null && mVolumeLevel < MAX_VOLUME) {
        mVolumeLevel += VOLUME_INCREASE_STEP;
        mPlayer.setVolume(mVolumeLevel, mVolumeLevel);
        // set next increase in 600ms
        mHandler.postDelayed(mVolumeRunnable, VOLUME_INCREASE_DELAY);
      }
    }
  };

  private void startPlayer() {
    mPlayer = new MediaPlayer();
    mPlayer.setOnErrorListener(mErrorListener);

    try {
      // add vibration to alarm alert if it is set
      //if (App.getState().settings().vibrate()) {
      mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
      mHandler.post(mVibrationRunnable);
      //}
      // Player setup is here
      String ringtone;// = App.getState().settings().ringtone();
      //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
      //    && ringtone.startsWith("content://media/external/")
      //    && checkSelfPermission(
      //    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
      ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString();
      //}
      mPlayer.setDataSource(this, Uri.parse(ringtone));
      mPlayer.setLooping(true);
      mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
      mPlayer.setVolume(mVolumeLevel, mVolumeLevel);
      mPlayer.prepare();
      mPlayer.start();

      //if (App.getState().settings().ramping()) {
      //  mHandler.postDelayed(mVolumeRunnable, VOLUME_INCREASE_DELAY);
      //} else {
      mPlayer.setVolume(MAX_VOLUME, MAX_VOLUME);
      //}
    } catch (Exception e) {
      if (mPlayer.isPlaying()) {
        mPlayer.stop();
      }
      stopSelf();
    }
  }

  private void openActivity() {
    Intent myIntent = new Intent(getApplicationContext(), AlarmActivity.class);
    myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    myIntent.setAction(LOCATION_REACHED);
    System.out.println("Action Set");
    startActivity(myIntent);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    startForeground(NOTIFY_ID, createNotification());
    startListeningForLocationUpdates();
    return START_STICKY;
  }

  private void startListeningForLocationUpdates() {
    locationProvider.startLocationUpdates(locationCallback);
  }

  private Notification createNotification() {
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(
            R.drawable.map_pin)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Listening for location updated")
            .setOngoing(true);
    mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
        new Intent[] {new Intent(getApplicationContext(), LocationAlarmActivity.class)}, 0));
    return mBuilder.build();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    stopPlayer();
    compositeDisposable.dispose();
    locationProvider.stopLocationUpdates(locationCallback);
  }
}
