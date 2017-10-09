package in.arjsna.mapsalarm.mvpbase;

import android.content.Context;

public class BasePresenter<V extends IMVPView> implements IMVPPresenter<V> {
  private final Context mContext;
  V view;

  public BasePresenter(Context context) {
    mContext = context;
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

  public Context getmContext() {
    return mContext;
  }
}
