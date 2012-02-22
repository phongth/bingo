package zgame.dao.stub;

import java.util.HashMap;
import java.util.Map;

import zgame.bean.GameService;
import zgame.dao.GameServiceDao;

public class GameServiceDaoStub extends GameServiceDao {
  private static Map<String, GameService> gameServiceMap = new HashMap<String, GameService>();
  
  static {
    initMockDBLocal();
  }
  
  protected static void initMockDBLocal() {
    GameService gameService1 = new GameService("1", "Server Mien Bac", "localhost", 1200, 1000);
    gameServiceMap.put(gameService1.getId(), gameService1);
    
    GameService gameService2 = new GameService("2", "Server Mien Nam", "localhost", 1201, 1000);
    gameServiceMap.put(gameService2.getId(), gameService2);
  }
  
  protected static void initMockDBExternal() {
    GameService gameService1 = new GameService("1", "Server Mien Bac", "122.201.15.5", 1200, 1000);
    gameServiceMap.put(gameService1.getId(), gameService1);
    
    GameService gameService2 = new GameService("2", "Server Mien Nam", "122.201.15.5", 1201, 1000);
    gameServiceMap.put(gameService2.getId(), gameService2);
  }
  
	public GameService getGameServiceById(String gameServiceId) {
	  return gameServiceMap.get(gameServiceId);
	}

  @Override
  public void close() {
  }

  @Override
  public void commit() {
  }
}
