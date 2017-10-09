package in.arjsna.mapsalarm.mvpbase;

public class BasePresenter<V extends IMVPView> implements IMVPPresenter<V> {
  V view;
  @Override public void onAttach(V view) {
     this.view = view;
  }

  @Override public void onDetach() {
    view = null;
  }
}
