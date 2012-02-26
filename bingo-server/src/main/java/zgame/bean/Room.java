package zgame.bean;

import zgame.main.Global;

public class Room extends Entity {
  public static final int ROOM_BASIC = 0;
  public static final int ROOM_ADVANCE = 1;
  public static final int ROOM_VIP = 2;

  public static int[] MIN_BID_BY_ROOM_TYPE = { 0, 20000, 50000 };
  public static int[] MAX_BID_BY_ROOM_TYPE = { 20000, 50000, 500000 };

  private Game game;
  private int type = ROOM_BASIC;

  public Room(String id) {
    super(id);
    Global.roomMap.put(id, this);
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public Game getGame() {
    return game;
  }

  public void setGame(Game game) {
    this.game = game;
    this.parent = game;
  }

  public int getMinBid() {
    return MIN_BID_BY_ROOM_TYPE[type];
  }

  public int getMaxBid() {
    return MAX_BID_BY_ROOM_TYPE[type];
  }
}
