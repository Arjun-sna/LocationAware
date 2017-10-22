package in.arjsna.mapsalarm.locationalarm;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import in.arjsna.mapsalarm.R;
import in.arjsna.mapsalarm.db.CheckPoint;
import javax.inject.Inject;

public class CheckPointsAdapter
    extends RecyclerView.Adapter<CheckPointsAdapter.CheckPointViewHolder> {

  private final LocationAlarmMVPContract.ILocationPresenter<LocationAlarmMVPContract.ILocationAlarmView>
      locationAlarmPresenter;
  private final LayoutInflater layoutInflater;

  @Inject
  public CheckPointsAdapter(
      LocationAlarmMVPContract.ILocationPresenter<LocationAlarmMVPContract.ILocationAlarmView> locationAlarmPresenter,
      LayoutInflater layoutInflater) {
    this.locationAlarmPresenter = locationAlarmPresenter;
    this.layoutInflater = layoutInflater;
  }

  @Override public CheckPointViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new CheckPointViewHolder(
        layoutInflater.inflate(R.layout.check_point_list_item, parent, false),
        locationAlarmPresenter);
  }

  @Override public void onBindViewHolder(CheckPointViewHolder holder, int position) {
    CheckPoint checkPoint = locationAlarmPresenter.getCheckPointAt(position);
    holder.checkPointName.setText(checkPoint.getName());
  }

  @Override public int getItemCount() {
    return locationAlarmPresenter.getCheckPointsCount();
  }

  static class CheckPointViewHolder extends RecyclerView.ViewHolder {
    private final LocationAlarmMVPContract.ILocationPresenter<LocationAlarmMVPContract.ILocationAlarmView>
        locationAlarmPresenter;
    TextView checkPointName;
    //ImageView editBtn;
    ImageView deleteBtn;

    CheckPointViewHolder(View itemView,
        LocationAlarmMVPContract.ILocationPresenter<LocationAlarmMVPContract.ILocationAlarmView> locationAlarmPresenter) {
      super(itemView);
      this.locationAlarmPresenter = locationAlarmPresenter;
      checkPointName = itemView.findViewById(R.id.check_point_name_tv);
      //editBtn = itemView.findViewById(R.id.edit_check_point_name);
      deleteBtn = itemView.findViewById(R.id.delete_check_point);
      bindEvents();
    }

    private void bindEvents() {
      itemView.setOnClickListener(
          v -> locationAlarmPresenter.onCheckPointItemClicked(getAdapterPosition()));
      deleteBtn.setOnClickListener(
          v -> locationAlarmPresenter.onDeleteCheckPoint(getAdapterPosition()));
      //editBtn.setOnClickListener(
      //    v -> locationAlarmPresenter.onEditCheckPoint(getAdapterPosition()));
    }
  }
}
