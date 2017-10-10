package in.arjsna.mapsalarm.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "checkpoints")
public class CheckPoint {
  @PrimaryKey(autoGenerate = true)
  private long id;
  private String name;
  private double latitude;
  private double longitude;
  private boolean active;

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
}
