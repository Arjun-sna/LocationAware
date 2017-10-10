package in.arjsna.mapsalarm.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {CheckPoint.class}, version = 1, exportSchema = false)
public abstract class AppDataBase extends RoomDatabase {
  private CheckPointDataSource recordItemDataSource;

  public abstract CheckPointDao checkPointDao();
  private static final String DATABASE_NAME = "location_aware.db";

  private static AppDataBase appDataBaseInstance;

  public static AppDataBase getInstance(Context context) {
    if (appDataBaseInstance == null) {
      appDataBaseInstance = Room.databaseBuilder(context, AppDataBase.class, DATABASE_NAME).build();
      appDataBaseInstance.recordItemDataSource =
          new CheckPointDataSource(appDataBaseInstance.checkPointDao());
    }
    return appDataBaseInstance;
  }

  public CheckPointDataSource getCheckPointDataSource() {
    return recordItemDataSource;
  }
}
