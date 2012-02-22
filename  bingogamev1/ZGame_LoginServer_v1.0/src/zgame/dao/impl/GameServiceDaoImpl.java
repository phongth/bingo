package zgame.dao.impl;

import zgame.bean.GameService;
import zgame.dao.GameServiceDao;

public class GameServiceDaoImpl extends GameServiceDao {

  public GameService getGameServiceById(String gameServiceId) {
    throw new UnsupportedOperationException("This function is not support");
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException("The method close is not supported");
  }

  @Override
  public void commit() {
    throw new UnsupportedOperationException("The method commit is not supported");
  }
}
