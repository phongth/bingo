package zgame.bussiness;

import java.util.Collection;

import org.apache.log4j.Logger;

import zgame.bean.Entity;
import zgame.bean.Room;
import zgame.bean.User;
import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.server.ServerConnection;

public class ChooseGameBussiness {
  private static final Logger log = Logger.getLogger(ChooseGameBussiness.class);

  public static void joinGame(ServerConnection server, DataPackage inputDataPackage) {
    String gameId = inputDataPackage.nextString();
    User user = server.user;
    int errorCode = 0;

    if (user == null) {
      log.warn("ERROR : GameBussiness : joinGame : User try to join game without authenticate");
      errorCode = ProtocolConstants.ErrorCode.NO_AUTHEN_ERROR;
    } else {
      Entity game = Global.lobby.getChild(gameId);
      if (game == null) {
        log.warn("ERROR : GameBussiness : joinGame : User try to join game which does'nt exist: " + gameId + " username: "
            + user.getName());
        errorCode = -1;
      }
    }

    if (errorCode < 0) {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.JOIN_GAME_FAIL_RESPONSE);
      dataPackage.putString(gameId);
      dataPackage.putInt(errorCode);
      server.write(dataPackage);
    } else {
      getRoomList(server, gameId, ProtocolConstants.ResponseHeader.JOIN_GAME_SUCCESS_RESPONSE);
    }
  }

  public static void getRoomList(ServerConnection server, DataPackage inputDataPackage) {
    String gameId = inputDataPackage.nextString();
    getRoomList(server, gameId, ProtocolConstants.ResponseHeader.ROOM_LIST_RESPONSE);
  }

  public static void getRoomList(ServerConnection server, String gameId, int responseHeader) {
    Entity game = Global.lobby.getChild(gameId);
    Collection<Entity> rooms = game.getChilds();

    DataPackage dataPackage = new DataPackage(responseHeader);
    dataPackage.putString(gameId);
    dataPackage.putInt(rooms.size());
    for (Entity entity : rooms) {
      Room room = (Room) entity;
      dataPackage.putString(room.getId());
      dataPackage.putString(room.getName());
      dataPackage.putInt(room.getType());
      dataPackage.putInt(room.getConcurrentUser());
      dataPackage.putInt(room.getMaxUser());
      dataPackage.putInt(room.getMinBid());
      dataPackage.putInt(room.getMaxBid());
    }
    server.write(dataPackage);
  }
}
