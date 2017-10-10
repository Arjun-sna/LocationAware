package in.arjsna.mapsalarm.mvpbase;

import android.content.Context;
import in.arjsna.mapsalarm.db.CheckPointDataSource;

public class BasePresenter<V extends IMVPView> implements IMVPPresenter<V> {
  private final Context mContext;
  private final CheckPointDataSource mCheckPointDataSource;
  V view;

  public BasePresenter(Context context, CheckPointDataSource checkPointDataSource) {
    mContext = context;
    mCheckPointDataSource = checkPointDataSource;
  }

  @Override public void onAttach(V view) {
     this.view = view;
  }

  @Override public void onDetach() {
    view = null;
  }

  public V getView() {
    return view;
  }

  public Context getContext() {
    return mContext;
  }

  public CheckPointDataSource getCheckPointDataSource() {
    return mCheckPointDataSource;
  }
}
