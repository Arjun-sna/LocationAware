package in.arjsna.mapsalarm.bgservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import in.arjsna.mapsalarm.R;
import in.arjsna.mapsalarm.locationalarm.LocationAlarmActivity;
import java.util.Locale;

public class LocationAwareService extends Service{
  private static final int NOTIFY_ID = 100;

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    startForeground(NOTIFY_ID, createNotification());
    return START_STICKY;
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
