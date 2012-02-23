package zgame.bussiness;

import java.util.Collection;

import org.apache.log4j.Logger;

import zgame.bean.Entity;
import zgame.bean.Room;
import zgame.bean.Table;
import zgame.bean.User;
import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.server.Server;

public class RoomBussiness {
  private static final Logger log = Logger.getLogger(RoomBussiness.class);

  public static void joinRoom(Server server, DataPackage inputDataPackage) {
    String roomId = inputDataPackage.nextString();
    Room room = Global.roomMap.get(roomId);
    User user = server.user;
    int errorCode = 0;

    if (user == null) {
      log.warn("ERROR : RoomBussiness : joinRoom : User try to join room without authenticate");
      errorCode = ProtocolConstants.ErrorCode.NO_AUTHEN_ERROR;
    } else if (room == null) {
      log.warn("ERROR : RoomBussiness : joinRoom : User try to join room that not exist: " + roomId + " username: "
          + user.getName());
      errorCode = -1;
    } else if (user.getMoney() < room.getMinBid()) {
      errorCode = ProtocolConstants.ErrorCode.NOT_ENOUGN_MONEY_TO_JOIN_ROOM_ERROR;
    } else if (room.getConcurrentUser() >= room.getMaxUser()) {
      errorCode = ProtocolConstants.ErrorCode.ROOM_FULL_ERROR;
    }

    if (errorCode < 0) {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.JOIN_ROOM_FAIL_RESPONSE);
      dataPackage.putInt(errorCode);
      server.write(dataPackage);

      // Cập nhật lại cho user danh sách phòng
      if ((user != null) && (room != null)) {
        ChooseGameBussiness.getRoomList(server, room.getParent().getId(), ProtocolConstants.ResponseHeader.ROOM_LIST_RESPONSE);
      }
    } else {
      if (user.entity instanceof Room) {
        user.entity.removeUser(user);
      }

      room.addUser(user);
      getTableList(server, roomId, ProtocolConstants.ResponseHeader.JOIN_ROOM_SUCCESS_RESPONSE);
    }
  }

  public static void leaveRoom(Server server, DataPackage inputDataPackage) {
    User user = server.user;
    if (user.entity instanceof Room) {
      user.entity.removeUser(user);
    }
  }

  public static void getTableList(Server server, DataPackage inputDataPackage) {
    String roomId = inputDataPackage.nextString();
    getTableList(server, roomId, ProtocolConstants.ResponseHeader.TABLE_LIST_RESPONSE);
  }

  public static void getTableList(Server server, String roomId, int responseHeader) {
    Entity room = Global.roomMap.get(roomId);

    if (room == null) {
      return;
    }

    Collection<Entity> tables = room.getChilds();

    DataPackage dataPackage = new DataPackage(responseHeader);
    dataPackage.putString(roomId);
    dataPackage.putInt(tables.size());
    for (Entity entity : tables) {
      Table table = (Table) entity;
      dataPackage.putString(table.getId());
      dataPackage.putString(table.getName());
      dataPackage.putInt(table.getBid());
      dataPackage.putInt(table.getPassword().equals("") ? 0 : 1);
      dataPackage.putInt(table.getConcurrentUser());
      dataPackage.putInt(table.getMaxUser());
      dataPackage.putInt(table.isPlaying() ? 1 : 0);
    }
    server.write(dataPackage);
  }
}
