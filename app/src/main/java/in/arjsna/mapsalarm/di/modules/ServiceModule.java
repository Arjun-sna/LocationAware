package in.arjsna.mapsalarm.di.modules;

import android.app.Service;
import android.content.Context;
import dagger.Module;
import dagger.Provides;
import in.arjsna.mapsalarm.di.qualifiers.ServiceContext;
import in.arjsna.mapsalarm.di.scopes.ServiceScope;
import in.arjsna.mapsalarm.global.LocationProvider;

@Module
public class ServiceModule {
  private final Service service;

  public ServiceModule(Service service) {
    this.service = service;
  }

  @ServiceScope
  @ServiceContext
  @Provides
  Context provideContext() {
    return service;
  }

  @ServiceScope
  @Provides
  LocationProvider provideLocationProvider(@ServiceContext Context context) {
    return new LocationProvider(context);
  }
}
