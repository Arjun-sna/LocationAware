package in.arjsna.mapsalarm.di.modules;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import dagger.Module;
import dagger.Provides;
import in.arjsna.mapsalarm.global.LocationProvider;
import in.arjsna.mapsalarm.di.qualifiers.ActivityContext;
import in.arjsna.mapsalarm.di.scopes.ActivityScope;
import in.arjsna.mapsalarm.locationalarm.LocationAlarmMVPContract;
import in.arjsna.mapsalarm.locationalarm.LocationAlarmPresenter;

@Module public class ActivityModule {
  private final AppCompatActivity appCompatActivity;

  public ActivityModule(AppCompatActivity appCompatActivity) {
    this.appCompatActivity = appCompatActivity;
  }

  @ActivityScope @ActivityContext @Provides Context providesContext() {
    return appCompatActivity;
  }

  @ActivityScope @Provides LocationProvider provideLocationProvider(
      @ActivityContext Context context) {
    return new LocationProvider(context);
  }

  @ActivityScope @Provides
  LocationAlarmMVPContract.ILocationPresenter<LocationAlarmMVPContract.ILocationAlarmView> provideLocationPresenter(
      LocationAlarmPresenter<LocationAlarmMVPContract.ILocationAlarmView> locationAlarmPresenter) {
    return locationAlarmPresenter;
  }
}
