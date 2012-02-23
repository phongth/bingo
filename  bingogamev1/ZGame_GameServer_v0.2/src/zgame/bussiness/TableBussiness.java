package zgame.bussiness;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import zgame.bean.Entity;
import zgame.bean.Room;
import zgame.bean.Table;
import zgame.bean.User;
import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.server.Server;

public class TableBussiness {
  private static final Logger log = Logger.getLogger(TableBussiness.class);

  public static void onGameAction(Server server, DataPackage inputDataPackage) {
    User user = server.user;

    if (user == null) {
      log.warn("ERROR : TableBussiness : onGameAction : User try to do game action at table without authenticate");
      return;
    }

    if (!(user.entity instanceof Table)) {
      log.warn("ERROR : TableBussiness : onGameAction : User who is not in table try to do game action: " + user.getName());
      return;
    }

    Table table = (Table) user.entity;
    if (!table.isPlaying()) {
      log.warn("ERROR : TableBussiness : onGameAction : User try to send game action when game not play: " + user.getName());
      return;
    }

    if (table.getGameComponent() == null) {
      log.warn("ERROR : TableBussiness : onGameAction : table have no GameComponent: " + user.getName());
      return;
    }

    table.getGameComponent().onActionPerform(server, inputDataPackage);
  }

  public static void joinTable(Server server, DataPackage inputDataPackage) {
    String tableId = inputDataPackage.nextString();
    Table table = Global.tableMap.get(tableId);
    User user = server.user;
    int errorCode = 0;

    if (user == null) {
      log.warn("ERROR : TableBussiness : joinTable : User try to join table without authenticate");
      errorCode = ProtocolConstants.ErrorCode.NO_AUTHEN_ERROR;
    } else if (table == null) {
      log.warn("ERROR : TableBussiness : joinTable : user try to join table that is not exist: " + tableId + " username: "
          + user.getName());
      errorCode = -1;
    } else if (user.getMoney() < table.getBid()) {
      errorCode = ProtocolConstants.ErrorCode.NOT_ENOUGH_MONEY_TO_JOIN_TABLE_ERROR;
    } else if (table.getConcurrentUser() >= table.getMaxUser()) {
      errorCode = ProtocolConstants.ErrorCode.TABLE_FULL_ERROR;
    } else if (table.isPlaying()) {
      errorCode = ProtocolConstants.ErrorCode.TABLE_ALREADY_PLAY_ERROR;
    } else {
      if (!table.getPassword().equals("")) {
        // Thử lấy password từ client và so sánh
        String password = "";
        try {
          password = inputDataPackage.nextString();
        } catch (ArrayIndexOutOfBoundsException ex) {
        }

        if (!table.getPassword().equals(password)) {
          errorCode = ProtocolConstants.ErrorCode.TABLE_PASSWORD_WRONG_ERROR;
        }
      }
    }

    if (errorCode < 0) {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.JOIN_TABLE_FAIL_RESPONSE);
      dataPackage.putInt(errorCode);
      server.write(dataPackage);

      // Cập nhật lại danh sách bàn cho user
      if (user != null && table != null) {
        RoomBussiness.getTableList(server, table.getParent().getId(), ProtocolConstants.ResponseHeader.TABLE_LIST_RESPONSE);
      }
    } else {
      checkCacheUserError(table); // Kiểm tra và fix lỗi cache user ở bàn

      // Trường hợp quick join thì thoát khỏi phòng cũ
      if ((user.entity instanceof Room) && (user.entity != table.getParent())) {
        user.entity.removeUser(user);
      }

      table.getParent().addUser(user);
      table.addUser(user);
      notifyNewMemberListToAllUserInTable(tableId);
      user.server.write(new DataPackage(ProtocolConstants.ResponseHeader.JOIN_TABLE_SUCCESS_RESPONSE));
    }
  }

  private static void checkCacheUserError(Table table) {
    Collection<User> users = table.getUsers();
    for (User user : users) {
      // Nếu user không ở bàn hiện tại
      if (!user.entity.getId().equals(table.getId())) {
        Entity entity = user.entity;
        table.removeUser(user); // Remove user khỏi bàn hiện tại
        entity.addUser(user); // Đưa user trở về entity cũ
      }
    }
  }

  public static void leaveTable(Server server, DataPackage inputDataPackage) {
    leaveTable(server.user);
  }

  public static void leaveTable(User user) {
    if (user == null) {
      log.warn("ERROR : TableBussiness : leaveTable : User try to leave table without authenticate");
      return;
    }

    if (user.entity instanceof Table) {
      Table table = (Table) user.entity;

      if (table.isPlaying() && table.getGameComponent() != null) {
        table.getGameComponent().onUserLeaveTable(user);
      }

      table.removeUser(user);
      notifyNewMemberListToAllUserInTable(table.getId());
    }
  }

  public static void sendReady(Server server, DataPackage inputDataPackage) {
    User user = server.user;

    if (user == null) {
      log.warn("ERROR : TableBussiness : sendReady : User try to send ready at table without authenticate");
      return;
    }

    if (!(user.entity instanceof Table)) {
      log.warn("ERROR : TableBussiness : sendReady : User who is not in table try to send ready table: " + user.getName());
      return;
    }

    boolean ready = inputDataPackage.nextInt() == 0 ? false : true;
    if (ready == user.isReady()) {
      return;
    }

    user.setReady(ready);
    notifyNewMemberListToAllUserInTable(user.entity.getId());
  }

  public static void startGame(Server server, DataPackage inputDataPackage) {
    User user = server.user;
    int errorCode = 0;

    if (user == null) {
      log.warn("ERROR : TableBussiness : startGame : User try to start game without authenticate");
      errorCode = ProtocolConstants.ErrorCode.NO_AUTHEN_ERROR;
    } else if (!(user.entity instanceof Table)) {
      log.warn("ERROR : TableBussiness : startGame : User who is not in table try to config table: " + user.getName());
      errorCode = -1;
    } else {

      // Kiểm tra số lượng user trong bàn xem có đủ để start game hay không
      Table table = (Table) user.entity;
      Collection<User> users = table.getUsers();
      if (users.size() < 2) {
        errorCode = ProtocolConstants.ErrorCode.NOT_ENOUGH_USER_TO_START_GAME_ERROR;
      }

      for (User tableUser : users) {

        // Chủ bàn thì không cần phải kiểm tra ready
        if (user.equals(table.getTableMaster())) {
          continue;
        }

        if (!tableUser.isReady()) {
          errorCode = ProtocolConstants.ErrorCode.USER_NOT_READY_ERROR;
          break;
        }
      }
    }

    Table table = (Table) user.entity;
    if (errorCode < 0) {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.GAME_START_FAIL_RESPONSE);
      dataPackage.putInt(errorCode);
      server.write(dataPackage);

      // Cập nhật cho user về danh sách user
      notifyNewMemberListToUser(table.getId(), user);
    } else {

      // Báo cho tất cả các user trong bàn là Game Start
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.GAME_START_RESPONSE);
      table.startGame();
      Collection<User> users = table.getUsers();
      for (User tableUser : users) {
        tableUser.server.write(dataPackage);
      }
    }
  }

  public static void configTable(Server server, DataPackage inputDataPackage) {
    int errorCode = 0;
    User user = server.user;
    int bid = inputDataPackage.nextInt();
    String password = inputDataPackage.nextString();

    if (user == null) {
      log.warn("ERROR : TableBussiness : configTable : User try to config table without authenticate");
      errorCode = ProtocolConstants.ErrorCode.NO_AUTHEN_ERROR;
    } else if (!(user.entity instanceof Table)) {
      log.warn("ERROR : TableBussiness : configTable : User who is not in table try to config table: " + user.getName());
      errorCode = -1;
    } else if (password.length() > Table.MAX_PASSWORD_LEN) {
      errorCode = -ProtocolConstants.ErrorCode.TABLE_PASSWORD_TOO_LONG_ERROR;
    } else if (bid > user.getMoney()) {
      errorCode = ProtocolConstants.ErrorCode.NOT_ENOUGH_MONEY_TO_SET_TABLE_CONFIG_ERROR;
    } else {
      Table table = (Table) user.entity;
      if ((table.getTableMaster() == null) || !table.getTableMaster().equals(user)) {
        log.warn("ERROR : TableBussiness : configTable : User who is not table master try to config table: " + user.getName());
        errorCode = -1;
      }
    }

    if (errorCode < 0) {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.CONFIG_TABLE_FAIL_RESPONSE);
      dataPackage.putInt(errorCode);
      server.write(dataPackage);
    } else {
      Table table = (Table) user.entity;
      table.setPassword(password);
      table.setBid(bid);

      // Thông báo cho chủ bàn là config room thành công
      server.write(new DataPackage(ProtocolConstants.ResponseHeader.CONFIG_TABLE_SUCCESS_RESPONSE));

      // Tạo package để gửi thông báo cho user
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.TABLE_CONFIG_CHANGE_RESPONSE);
      dataPackage.putInt(table.getBid());

      Collection<User> users = table.getUsers();
      for (User user1 : users) {

        // Thành viên nào không đủ tiền so với config mới thì tự động cho ra
        // khỏi bàn
        if (user1.getMoney() < bid) {
          leaveTable(user1);
        }

        // Gửi thông tin bàn cho tất cả các thành viên trong bàn
        user1.server.write(dataPackage);
      }
    }
  }

  public static void listWantToPlayUserList(Server server, DataPackage inputDataPackage) {
    int numberOfUserInPage = 10; // TODO: have to load this setting from config
                                 // file
    int pageNumber = inputDataPackage.nextInt();

    System.out.println(">>>>>>Receive listWantToPlayUserList request");

    List<User> freeUser = new ArrayList<User>();
    for (Server server1 : Global.serverMap.values()) {
      if (!(server1.user.entity instanceof Table)) {
        freeUser.add(server1.user);
      }
    }

    if (numberOfUserInPage * pageNumber > freeUser.size()) {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ErrorCode.NO_MORE_FREE_USER);
      server.write(dataPackage);
    }

    int endPoint = (pageNumber + 1) * numberOfUserInPage;
    if (endPoint > freeUser.size()) {
      endPoint = freeUser.size();
    }

    List<User> userInPageList = freeUser.subList(pageNumber * numberOfUserInPage, endPoint);
    DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.LIST_USER_WANT_TO_PLAY_RESPONSE);
    dataPackage.putInt(pageNumber);
    dataPackage.putInt(userInPageList.size());

    for (User user : userInPageList) {
      dataPackage.putString(user.getName());
      dataPackage.putString(FriendBussiness.getUserLocation(user.entity));
    }
    server.write(dataPackage);
  }

  public static void notifyNewMemberListToAllUserInTable(String tableId) {
    Table table = Global.tableMap.get(tableId);
    Collection<User> users = table.getUsers();

    DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.USER_LIST_OF_TABLE_RESPONSE);
    dataPackage.putString(table.getTableMaster() == null ? "" : table.getTableMaster().getName());
    dataPackage.putInt(users.size());
    for (User user : users) {
      dataPackage.putString(user.getName());
      dataPackage.putInt(user.getMoney());
      dataPackage.putInt(user.isReady() ? 1 : 0);
      dataPackage.putInt(user.getAvatarId());
    }

    for (User user : users) {
      user.server.write(dataPackage);
    }
  }

  public static void notifyNewMemberListToUser(String tableId, User toUser) {
    Table table = Global.tableMap.get(tableId);
    Collection<User> users = table.getUsers();

    DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.USER_LIST_OF_TABLE_RESPONSE);
    dataPackage.putString(table.getTableMaster() == null ? "" : table.getTableMaster().getName());
    dataPackage.putInt(users.size());
    for (User user : users) {
      dataPackage.putString(user.getName());
      dataPackage.putInt(user.getMoney());
      dataPackage.putInt(user.isReady() ? 1 : 0);
      dataPackage.putInt(user.getAvatarId());
    }
    toUser.server.write(dataPackage);
  }
}
