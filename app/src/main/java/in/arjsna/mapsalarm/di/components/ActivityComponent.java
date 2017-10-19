package in.arjsna.mapsalarm.di.components;

import dagger.Component;
import in.arjsna.mapsalarm.alarm.AlarmActivity;
import in.arjsna.mapsalarm.di.modules.ActivityModule;
import in.arjsna.mapsalarm.di.scopes.ActivityScope;
import in.arjsna.mapsalarm.locationalarm.LocationAlarmActivity;

@ActivityScope
@Component (modules = ActivityModule.class, dependencies = ApplicationComponent.class)
public interface ActivityComponent {
  void inject(LocationAlarmActivity locationAlarmActivity);

  void inject(AlarmActivity alarmActivity);
}
