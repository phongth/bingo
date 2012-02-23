package zgame.socket.handle;

import zgame.bean.Entity;
import zgame.bean.Table;
import zgame.bussiness.AuthenticateBussiness;
import zgame.bussiness.ChooseGameBussiness;
import zgame.bussiness.FriendBussiness;
import zgame.bussiness.RoomBussiness;
import zgame.bussiness.TableBussiness;
import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.DataReceiveListener;
import zgame.socket.ProtocolConstants;
import zgame.socket.server.Server;

public class SocketServerHandle implements DataReceiveListener {
  private Server server;

  public SocketServerHandle(Server server) {
    this.server = server;
    server.setListener(this);
  }

  @Override
  public void onConnectDone() {
  }

  @Override
  public void onConnectFail() {
  }

  @Override
  public void onDisconnect() {
    if (server.user != null) {
      // Báo cho DefaultService biết là user đã out khỏi GameService
      DataPackage dataPackage = new DataPackage(ProtocolConstants.RequestHeader.USER_OUT_GAME_SERVER_INFORM_REQUEST);
      dataPackage.putString(server.user.getName());
      Global.client.write(dataPackage);

      Entity entity = server.user.entity;
      // Nếu user đang ở trong bàn thì thực hiện sự kiện user thoát ra khỏi bàn
      if (entity instanceof Table) {
        TableBussiness.leaveTable(server.user);
      }

      // Clear thông tin của user trên server và ngắt kết nối với user đó
      while (entity != null) {
        entity.removeUser(server.user);
        entity = entity.getParent();
      }
      Global.serverMap.remove(server.user.getName());
      server.detroy();
    }
  }

  @Override
  public void onRecieveData(DataPackage dataPackage) {
    int header = dataPackage.getHeader();

    if (header == ProtocolConstants.RequestHeader.HEART_BREATH_REQUEST) {
      // Do nothing
      return;
    }

    // If user did not authenticate yet
    if (server.user == null) {
      switch (header) {
      /** Authenticate */
      case ProtocolConstants.RequestHeader.AUTHENTICATE_REQUEST:
        AuthenticateBussiness.requestSalt(server, dataPackage);
        break;
      case ProtocolConstants.RequestHeader.AUTHENTICATE_WITH_SESSION_ID_REQUEST:
        AuthenticateBussiness.authenticateBySessionId(server, dataPackage);
        break;
      }
      return;
    }

    // If user already authenticate
    switch (header) {

    /** Game, room and table */
    case ProtocolConstants.RequestHeader.GET_ROOM_LIST_REQUEST:
      ChooseGameBussiness.getRoomList(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.GET_TABLE_LIST_REQUEST:
      RoomBussiness.getTableList(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.JOIN_GAME_REQUEST:
      ChooseGameBussiness.joinGame(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.JOIN_ROOM_REQUEST:
      RoomBussiness.joinRoom(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.JOIN_TABLE_REQUEST:
      TableBussiness.joinTable(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.LEAVE_ROOM_REQUEST:
      RoomBussiness.leaveRoom(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.LEAVE_TABLE_REQUEST:
      TableBussiness.leaveTable(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.SEND_READY_REQUEST:
      TableBussiness.sendReady(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.CONFIG_TABLE_REQUEST:
      TableBussiness.configTable(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.START_GAME_REQUEST:
      TableBussiness.startGame(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.GAME_ACTION_REQUEST:
      TableBussiness.onGameAction(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.LIST_USER_WANT_TO_PLAY_REQUEST:
      TableBussiness.listWantToPlayUserList(server, dataPackage);
      break;

    /** Friend function */
    case ProtocolConstants.RequestHeader.ADD_FRIEND_REQUEST:
      FriendBussiness.onAddFriendRequest(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.ADD_FRIEND_AGREE_REQUEST:
      FriendBussiness.onAddFriendAgreeRequest(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.ADD_FRIEND_DENY_REQUEST:
      FriendBussiness.onAddFriendDenyRequest(server, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.GET_FRIEND_LIST_REQUEST:
      FriendBussiness.onFriendListRequest(server, dataPackage);
      break;
    }
  }
}
