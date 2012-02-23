package zgame.socket.handle;

import org.apache.log4j.Logger;

import zgame.bussiness.FriendBussiness;
import zgame.bussiness.GameServiceBussiness;
import zgame.main.GameServiceController;
import zgame.socket.DataPackage;
import zgame.socket.DataReceiveListener;
import zgame.socket.ProtocolConstants;
import zgame.socket.ServerConnection;

public class GameServiceHandle implements DataReceiveListener {
  private static final Logger log = Logger.getLogger(GameServiceHandle.class);
  private GameServiceController controller;
  private ServerConnection server;

  public GameServiceHandle(ServerConnection server, GameServiceController controller) {
    this.server = server;
    this.controller = controller;

    server.setListener(this);

    // Tạo datapackage yêu cầu GameService cung cấp thông tin để kiểm tra
    server.write(new DataPackage(ProtocolConstants.RequestHeader.GAME_SERVICE_INFO_REQUEST));
  }

  public void onRecieveData(DataPackage dataPackage) {
    int header = dataPackage.getHeader();

    // Nếu GameService chưa valid thì chỉ tiến hành kiểm tra
    if (server.name == null) {
      switch (header) {
      /** Nhận các thông tin dùng để xác nhận gameService */
      case ProtocolConstants.ResponseHeader.GAME_SERVICE_INFO_RESPONSE:
        GameServiceBussiness.validGameServiceInfo(server, dataPackage);
        break;
      }
      return;
    }

    try {
      switch (header) {
      /** Các thông tin của game service */
      case ProtocolConstants.RequestHeader.UPDATE_GAME_SERVER_INFO_REQUEST:
        GameServiceBussiness.updateGameServiceInfo(server, dataPackage);
        break;

      /** Khi GameService nhờ DefaulService gửi về sessionId theo username */
      case ProtocolConstants.RequestHeader.AUTHENTICATE_REQUEST:
        GameServiceBussiness.authenRequestHandle(server, dataPackage);
        break;

      /** GameService thông báo cho DefaultService khi có user in or out */
      case ProtocolConstants.RequestHeader.USER_JOIN_IN_GAME_SERVER_INFORM_REQUEST:
        GameServiceBussiness.userJoinInInform(server, dataPackage);
        break;
      case ProtocolConstants.RequestHeader.USER_OUT_GAME_SERVER_INFORM_REQUEST:
        GameServiceBussiness.userOutInform(server, dataPackage);
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
        FriendBussiness.onGetFriendListRequest(server, dataPackage);
        break;
      }
    } catch (Exception ex) {
      log.warn("Exception occur", ex);
    }
  }

  public void onConnectDone() {
  }

  public void onConnectFail() {
  }

  public void onDisconnect() {
    GameServiceBussiness.releaseAllConnectionToUser(server);
    controller.removeGameService(server.name);
  }

  public ServerConnection getServer() {
    return server;
  }
}
