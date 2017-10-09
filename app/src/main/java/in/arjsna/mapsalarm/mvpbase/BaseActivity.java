package in.arjsna.mapsalarm.mvpbase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import in.arjsna.mapsalarm.global.LocationAware;
import in.arjsna.mapsalarm.di.components.DaggerActivityComponent;
import in.arjsna.mapsalarm.di.modules.ActivityModule;

public class BaseActivity extends AppCompatActivity {
  private DaggerActivityComponent daggerActivityComponent;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    daggerActivityComponent = (DaggerActivityComponent) DaggerActivityComponent.builder()
        .applicationComponent(((LocationAware) getApplication()).getDaggerApplicationComponent())
        .activityModule(new ActivityModule(this))
        .build();
  }

  public DaggerActivityComponent getActivityComponent() {
    return daggerActivityComponent;
  }
}
