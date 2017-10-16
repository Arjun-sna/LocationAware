package in.arjsna.mapsalarm.di.modules;

import android.app.Service;
import android.content.Context;
import dagger.Module;
import dagger.Provides;
import in.arjsna.mapsalarm.di.qualifiers.ServiceContext;
import in.arjsna.mapsalarm.di.scopes.ServiceScope;
import in.arjsna.mapsalarm.global.LocationProvider;
import io.reactivex.disposables.CompositeDisposable;

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
  CompositeDisposable provideCompositeDisposable() {
    return new CompositeDisposable();
  }
}
