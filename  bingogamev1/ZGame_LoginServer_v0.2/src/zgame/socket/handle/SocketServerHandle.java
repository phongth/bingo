package zgame.socket.handle;

import org.apache.log4j.Logger;

import zgame.bussiness.AuthenticateBussiness;
import zgame.bussiness.AvatarBussiness;
import zgame.bussiness.CheckForUpdateBussiness;
import zgame.bussiness.ImageDownloadBussiness;
import zgame.socket.DataPackage;
import zgame.socket.DataReceiveListener;
import zgame.socket.ProtocolConstants;
import zgame.socket.Server;

public class SocketServerHandle implements DataReceiveListener {
  private static final Logger log = Logger.getLogger(SocketServerHandle.class);
  private Server server;

  public SocketServerHandle(Server server) {
    this.server = server;
    server.setListener(this);
  }

  public void onConnectDone() {
  }

  public void onConnectFail() {
  }

  public void onDisconnect() {
    if (server.name != null) {
      AuthenticateBussiness.disconnect(server.name);
    }
  }

  public void onRecieveData(DataPackage dataPackage) {
    try {
      int header = dataPackage.getHeader();
      switch (header) {
      case ProtocolConstants.RequestHeader.HEART_BREATH_REQUEST:
        // Do nothing
        break;

      case ProtocolConstants.RequestHeader.CHECK_VERSION_REQUEST:
        CheckForUpdateBussiness.checkToUpdate(server, dataPackage);
        break;
      case ProtocolConstants.RequestHeader.DOWNLOADED_RESOURCE_LIST_REQUEST:
        ImageDownloadBussiness.downloadedResourceList(server, dataPackage);
        break;

      /** Authenticate */
      case ProtocolConstants.RequestHeader.AUTHENTICATE_REQUEST:
        AuthenticateBussiness.requestSalt(server, dataPackage);
        break;
      case ProtocolConstants.RequestHeader.AUTHENTICATE_WITH_PASSWORD_REQUEST:
      case ProtocolConstants.RequestHeader.AUTHENTICATE_WITH_SESSION_ID_REQUEST:
        AuthenticateBussiness.checkAuthenticate(server, dataPackage);
        break;
      case ProtocolConstants.RequestHeader.SIGN_OUT_REQUEST:
        AuthenticateBussiness.signOut(server, dataPackage);
        break;
      case ProtocolConstants.RequestHeader.REGISTER_REQUEST:
        AuthenticateBussiness.registerUser(server, dataPackage);
        break;
      case ProtocolConstants.RequestHeader.CHANGE_PASSWORD_REQUEST:
        AuthenticateBussiness.changePassword(server, dataPackage);

        /** Avatar */
      case ProtocolConstants.RequestHeader.AVARAR_CATEGORY_LIST_REQUEST:
        AvatarBussiness.getCategoryList(server, dataPackage);
        break;
      case ProtocolConstants.RequestHeader.GET_AVATAR_BY_CATEGORY_REQUEST:
        AvatarBussiness.getAvatarListByCategory(server, dataPackage);
        break;

      /** Cập nhật danh sách GameService */
      case ProtocolConstants.RequestHeader.GAME_SERVICE_LIST_REQUEST:
        AuthenticateBussiness.gameServiceListRequest(server, dataPackage);
        break;

      case ProtocolConstants.RequestHeader.CLOSE_CONNECTION_REQUEST:
        server.detroy();
        break;
      }
    } catch (Exception ex) {
      log.warn("Exception occur when process data package with header: " + dataPackage.getHeader() + " of user: " + server.name,
          ex);
    }
  }
}
