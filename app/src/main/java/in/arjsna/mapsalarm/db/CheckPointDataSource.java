package in.arjsna.mapsalarm.db;

import io.reactivex.Completable;
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

  public Completable deleteCheckPoint(CheckPoint checkPoint) {
    return Completable.fromAction(() -> checkPointDao.deleteCheckPoint(checkPoint));
  }

  public Single<Integer> getCheckPointCount() {
    return Single.fromCallable(checkPointDao::getCount);
  }
}
