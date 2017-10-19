package in.arjsna.mapsalarm.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "checkpoints")
public class CheckPoint implements Parcelable {
  @PrimaryKey(autoGenerate = true)
  private long id;
  private String name;
  private double latitude;
  private double longitude;
  private boolean active;

  public CheckPoint() {

  }

  protected CheckPoint(Parcel in) {
    id = in.readLong();
    name = in.readString();
    latitude = in.readDouble();
    longitude = in.readDouble();
    active = in.readByte() != 0;
  }

  public static final Creator<CheckPoint> CREATOR = new Creator<CheckPoint>() {
    @Override
    public CheckPoint createFromParcel(Parcel in) {
      return new CheckPoint(in);
    }

    @Override
    public CheckPoint[] newArray(int size) {
      return new CheckPoint[size];
    }
  };

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public boolean isActive() {
    return active;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(id);
    dest.writeString(name);
    dest.writeDouble(latitude);
    dest.writeDouble(longitude);
    dest.writeByte((byte) (active ? 1 : 0));
  }
}
