package in.arjsna.mapsalarm.di.modules;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import dagger.Module;
import dagger.Provides;
import in.arjsna.mapsalarm.di.qualifiers.ActivityContext;
import in.arjsna.mapsalarm.di.scopes.ActivityScope;

@Module public class ActivityModule {
  private final AppCompatActivity appCompatActivity;

  public ActivityModule(AppCompatActivity appCompatActivity) {
    this.appCompatActivity = appCompatActivity;
  }

  @ActivityScope @ActivityContext @Provides Context providesContext() {
    return appCompatActivity;
  }
}
