package zgame.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import zgame.bussiness.GameComponent;
import zgame.bussiness.TableBussiness;
import zgame.main.Global;

public class Table extends Entity {
  public static final int MAX_PASSWORD_LEN = 10;

  private Game game;
  private Room room;

  private int bid = 0;
  private String password = "";
  private boolean isPlaying = false;
  private User tableMaster;
  private GameComponent gameComponent;
  private String lastWinUser;

  private Map<String, User> viewUsers = new HashMap<String, User>();

  public Table(String id) {
    super(id);
    Global.tableMap.put(id, this);
  }

  @Override
  public void addUser(User user) {
    super.addUser(user);

    // Nếu chưa có chủ phòng thì gán chủ phòng cho người chơi mới vào
    if (tableMaster == null) {
      tableMaster = user;
    }
    user.setReady(false); // Người chơi vừa vào bàn thì thiết lập là chưa sẵn
                          // sàng
  }

  @Override
  public void removeUser(User user) {
    super.removeUser(user);
    if (users.size() == 0) {
      reset();
    } else {
      // Nếu là chủ phòng rời bàn thì nhường quyền chủ phòng cho người chơi khác
      if (tableMaster != null && tableMaster.equals(user)) {
        Collection<User> tmpUsers = users.values();
        tableMaster = (User) tmpUsers.toArray()[0];
        password = ""; // Khi chuyển chủ phòng thì reset password
      }

      if ((user != null) && user.getName().equals(lastWinUser)) {
        lastWinUser = null;
      }
    }
  }

  public void addViewUser(User user) {
    viewUsers.put(user.getName(), user);
  }

  public void getViewUser(String username) {
    viewUsers.get(username);
  }

  public void removeViewUser(String username) {
    viewUsers.remove(username);
  }

  public Game getGame() {
    return game;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public Room getRoom() {
    return room;
  }

  public String getLastWinUser() {
    return lastWinUser;
  }

  public void setLastWinUser(String lastWinUser) {
    this.lastWinUser = lastWinUser;
  }

  public void setRoom(Room room) {
    this.room = room;
    this.parent = room;
    if (room.getGame() != null) {
      this.game = room.getGame();
    }
  }

  public int getBid() {
    return bid;
  }

  public void setBid(int bid) {
    this.bid = bid;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isPlaying() {
    return isPlaying;
  }

  public void startGame() {
    isPlaying = true;
    if (gameComponent != null) {
      gameComponent.init();
    }
  }

  public void endGame() {
    isPlaying = false;
    if (gameComponent != null) {
      gameComponent.detroy();
    }

    // Cho tất cả các user về trạng thái chưa sẵn sàng
    Collection<User> users = getUsers();
    for (User user : users) {
      user.setReady(false);
    }

    // Cập nhật lại danh sách user cho các user trong bàn
    TableBussiness.notifyNewMemberListToAllUserInTable(id);
  }

  public User getTableMaster() {
    return tableMaster;
  }

  public void setTableMaster(User tableMaster) {
    this.tableMaster = tableMaster;
  }

  public GameComponent getGameComponent() {
    return gameComponent;
  }

  public void setGameComponent(GameComponent gameComponent) {
    this.gameComponent = gameComponent;
  }

  public void reset() {
    Room room = (Room) parent;

    bid = room.getMinBid();
    password = "";
    isPlaying = false;
    tableMaster = null;
    lastWinUser = null;
  }
}
