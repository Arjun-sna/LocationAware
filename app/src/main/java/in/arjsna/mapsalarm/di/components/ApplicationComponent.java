package in.arjsna.mapsalarm.di.components;

import android.content.Context;
import dagger.Component;
import in.arjsna.mapsalarm.LocationAware;
import in.arjsna.mapsalarm.di.modules.ApplicationModule;
import in.arjsna.mapsalarm.di.qualifiers.ApplicationContext;
import javax.inject.Singleton;

@Component(modules = ApplicationModule.class)
@Singleton
public interface ApplicationComponent {
  void inject(LocationAware locationAware);

  @ApplicationContext Context getContext();

}
