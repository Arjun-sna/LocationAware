package in.arjsna.mapsalarm.mvpbase;

public interface IMVPPresenter<V extends IMVPView> {
  void onAttach(V view);
  void onDetach();
}
