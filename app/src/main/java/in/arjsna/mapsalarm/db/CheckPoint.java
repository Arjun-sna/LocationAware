package in.arjsna.mapsalarm.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "checkpoints")
public class CheckPoint {
  @PrimaryKey(autoGenerate = true)
  private long id;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }
}
