package zgame.socket.handle;

import org.apache.log4j.Logger;

import zgame.bussiness.AuthenticateBussiness;
import zgame.bussiness.FriendBussiness;
import zgame.bussiness.RegistGameServiceBussiness;
import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.DataReceiveListener;
import zgame.socket.ProtocolConstants;

public class SocketClientHandle implements DataReceiveListener {
  public static final Logger log = Logger.getLogger(SocketClientHandle.class);

  @Override
  public void onRecieveData(DataPackage dataPackage) {
    int header = dataPackage.getHeader();
    switch (header) {

    /** Phần đăng ký GameService */
    case ProtocolConstants.RequestHeader.GAME_SERVICE_INFO_REQUEST:
      RegistGameServiceBussiness.gameServiceInfoRequest(Global.client, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.GAME_SERVICE_REGISTER_SUCCESS_REQUEST:
      RegistGameServiceBussiness.gameServiceRegistSuccess(Global.client, dataPackage);
      break;
    case ProtocolConstants.RequestHeader.GAME_SERVICE_REGISTER_FAIL_REQUEST:
      RegistGameServiceBussiness.gameServiceRegistFail(Global.client, dataPackage);
      break;

    /** Phần GameService lấy sessionId từ DefaultService theo username */
    case ProtocolConstants.ResponseHeader.SESSION_NOT_EXIST:
      AuthenticateBussiness.sessionNotFound(Global.client, dataPackage);
      break;
    case ProtocolConstants.ResponseHeader.SESSION_RESPONSE:
      AuthenticateBussiness.sessionIdResponse(Global.client, dataPackage);
      break;

    /** Phần update info từ DefautService */
    case ProtocolConstants.ResponseHeader.USER_INFO_REPONSE:
      AuthenticateBussiness.onUserInfoReceive(Global.client, dataPackage);
      break;

    /** Friend function */
    case ProtocolConstants.RequestHeader.ADD_FRIEND_REQUEST:
      FriendBussiness.onAddFriendRequestFromDefaultService(dataPackage);
      break;
    case ProtocolConstants.ResponseHeader.ADD_FRIEND_SUCCESS_RESPONSE:
      FriendBussiness.onAddFriendSuccessFromDefaultService(dataPackage);
      break;
    case ProtocolConstants.ResponseHeader.ADD_FRIEND_FAIL_USER_DENY_RESPONSE:
      FriendBussiness.onAddFriendFailUserDenyFromDefaultService(dataPackage);
      break;
    case ProtocolConstants.ResponseHeader.ADD_FRIEND_FAIL_USER_NOT_EXIST_RESPONSE:
      FriendBussiness.onAddFriendFailUserNotExistFromDefaultService(dataPackage);
      break;
    case ProtocolConstants.ResponseHeader.ALREADY_FRIEND_RESPONSE:
      FriendBussiness.onAlreadyFriendResponseFromDefaultService(dataPackage);
      break;
    case ProtocolConstants.ResponseHeader.FRIEND_LIST_RESPONSE:
      FriendBussiness.onFriendListResponseFromDefaultService(dataPackage);
      break;
    }
  }

  @Override
  public void onConnectDone() {
  }

  @Override
  public void onConnectFail() {
  }

  @Override
  public void onDisconnect() {
    // // Tự động kết nối lại ngay với Default service khi mất kết nối
    // log.info("Connect to DEFAULT SERVICE at " + Global.DEFAULT_SERVICE_URL +
    // ":" + Global.DEFAULT_SERVICE_PORT);
    // Global.client = new Client(Global.DEFAULT_SERVICE_URL,
    // Global.DEFAULT_SERVICE_PORT, this);
  }
}
