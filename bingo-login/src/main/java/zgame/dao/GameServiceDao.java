package zgame.dao;

import zgame.bean.GameService;
import zgame.dao.stub.GameServiceDaoStub;

public abstract class GameServiceDao extends AbstractDao {
  public static GameServiceDao createInstance() {
    return new GameServiceDaoStub();
  }

  public abstract GameService getGameServiceById(String gameServiceId);

  public abstract void commit();

  public abstract void close();
}
