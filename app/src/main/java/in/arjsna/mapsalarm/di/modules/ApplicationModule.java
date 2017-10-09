package in.arjsna.mapsalarm.di.modules;

import android.app.Application;
import android.content.Context;
import dagger.Module;
import dagger.Provides;
import in.arjsna.mapsalarm.di.qualifiers.ApplicationContext;
import javax.inject.Singleton;

@Module public class ApplicationModule {
  private final Application application;

  public ApplicationModule(Application application) {
    this.application = application;
  }

  @Provides @Singleton @ApplicationContext Context provideApplicationContext() {
    return application.getApplicationContext();
  }
}
