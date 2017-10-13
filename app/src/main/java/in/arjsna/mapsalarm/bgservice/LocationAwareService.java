package in.arjsna.mapsalarm.bgservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import in.arjsna.mapsalarm.R;
import in.arjsna.mapsalarm.di.components.DaggerServiceComponent;
import in.arjsna.mapsalarm.di.components.ServiceComponent;
import in.arjsna.mapsalarm.global.LocationAware;
import in.arjsna.mapsalarm.global.LocationProvider;
import in.arjsna.mapsalarm.locationalarm.LocationAlarmActivity;
import javax.inject.Inject;

public class LocationAwareService extends Service{
  private static final int NOTIFY_ID = 100;

  @Inject
  public LocationProvider locationProvider;

  @Override public void onCreate() {
    super.onCreate();
    ServiceComponent serviceComponent = DaggerServiceComponent.builder()
        .applicationComponent(((LocationAware) getApplication()).getApplicationComponent())
        .build();
    serviceComponent.inject(this);
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
}
