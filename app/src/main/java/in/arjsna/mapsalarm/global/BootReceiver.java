package in.arjsna.mapsalarm.global;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import in.arjsna.mapsalarm.bgservice.LocationAwareService;
import in.arjsna.mapsalarm.db.CheckPointDataSource;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;

public class BootReceiver extends BroadcastReceiver {

  @Inject
  CheckPointDataSource checkPointDataSource;

  @Override public void onReceive(Context context, Intent intent) {
    ((LocationAware) context.getApplicationContext()).getApplicationComponent().inject(this);
    checkPointDataSource.getCheckPointCount()
        .doOnSuccess(count -> {
          if (count > 0) {
            context.startService(new Intent(context, LocationAwareService.class));
          }
        })
        .subscribeOn(Schedulers.io())
        .subscribe();
  }
}
