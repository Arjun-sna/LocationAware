package in.arjsna.mapsalarm.global;

import android.app.Application;
import in.arjsna.mapsalarm.di.components.DaggerApplicationComponent;
import in.arjsna.mapsalarm.di.modules.ApplicationModule;

public class LocationAware extends Application {
  private DaggerApplicationComponent daggerApplicationComponent;

  @Override public void onCreate() {
    super.onCreate();
    daggerApplicationComponent =
        (DaggerApplicationComponent) DaggerApplicationComponent.builder()
            .applicationModule(new ApplicationModule(this))
            .build();
  }

  public DaggerApplicationComponent getApplicationComponent() {
    return daggerApplicationComponent;
  }
}
