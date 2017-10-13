package in.arjsna.mapsalarm.di.components;

import dagger.Component;
import in.arjsna.mapsalarm.bgservice.LocationAwareService;
import in.arjsna.mapsalarm.di.modules.ServiceModule;
import in.arjsna.mapsalarm.di.scopes.ServiceScope;

@Component(dependencies = ApplicationComponent.class, modules = ServiceModule.class)
@ServiceScope
public interface ServiceComponent {
  void inject(LocationAwareService locationAwareService);
}
