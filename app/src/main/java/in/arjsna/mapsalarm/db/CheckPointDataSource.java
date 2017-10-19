package in.arjsna.mapsalarm.db;

import io.reactivex.Single;
import java.util.List;

public class CheckPointDataSource {
  private final CheckPointDao checkPointDao;

  public CheckPointDataSource(CheckPointDao checkPointDao) {
    this.checkPointDao = checkPointDao;
  }

  public Single<List<CheckPoint>> getAllCheckPoints() {
    return Single.fromCallable(checkPointDao::getAllCheckPoints);
  }

  public Single<Boolean> insertNewCheckPoint(CheckPoint checkPoint) {
    return Single.fromCallable(() -> checkPointDao.insertNewCheckPoint(checkPoint) > 1);
  }

  public Single<Boolean> deleteCheckPoint(CheckPoint checkPoint) {
    return Single.fromCallable(() -> checkPointDao.deleteCheckPoint(checkPoint) > 1);
  }
}
