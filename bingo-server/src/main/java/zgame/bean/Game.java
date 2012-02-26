package zgame.bean;

import zgame.bussiness.GameBroadChessBussiness;
import zgame.bussiness.GameCaroBussiness;
import zgame.bussiness.GameChessBussiness;
import zgame.bussiness.GameComponent;

public class Game extends Entity {

  public Game(String id) {
    super(id);
  }

  public GameComponent createGameComponent(Table table) {
    if ("caro".equals(id)) {
      return new GameCaroBussiness(table);
    } else if ("cotuong".equals(id)) {
      return new GameChessBussiness(table);
    } else if ("covua".equals(id)) {
      return new GameBroadChessBussiness(table);
    }
    return null;
  }
}
