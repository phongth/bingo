package zgame.bussiness;

import org.apache.log4j.Logger;

import zgame.bean.Entity;
import zgame.bean.ServerSessionStore;
import zgame.bean.Session;
import zgame.bean.Table;
import zgame.bean.User;
import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.client.ClientConnection;
import zgame.socket.server.ServerConnection;
import zgame.utils.SaltUtil;

public class AuthenticateBussiness {
  private static final Logger log = Logger.getLogger(AuthenticateBussiness.class);

  public static void disconnect(String username) {
  }

  public static void requestSalt(ServerConnection server, DataPackage inputDataPackage) {
    // Lấy username do client gửi lên
    String username = inputDataPackage.nextString();

    // Tạo salt và lưu lại theo user name
    String salt1 = SaltUtil.createAndStoreSaltByUsername(username);

    // Gửi salt cho client
    DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.SALT_REPONSE);
    dataPackage.putString(salt1);
    server.write(dataPackage);

    // Gửi salt1 cho default service để kiểm chứng
    DataPackage dataPackage2 = new DataPackage(ProtocolConstants.RequestHeader.AUTHENTICATE_REQUEST);
    dataPackage2.putString(username);
    dataPackage2.putString(salt1);
    Global.client.write(dataPackage2);

    log.info(">>>>> Request sessionId from Default Service for user: " + username);
  }

  public static void sessionNotFound(ClientConnection client, DataPackage inputDataPackage) {
    String username = inputDataPackage.nextString();
    Session session = new Session(null);
    Global.sessionMapTmp.put(username, session);
    checkUserSession(username);
  }

  public static void sessionIdResponse(ClientConnection client, DataPackage inputDataPackage) {
    String username = inputDataPackage.nextString();
    String encodedData = inputDataPackage.nextString();

    log.info(">>>>> Default service response sessionId for user: " + username);

    Session session = new Session(encodedData);
    Global.sessionMapTmp.put(username, session);
    checkUserSession(username);
  }

  public static void authenticateBySessionId(ServerConnection server, DataPackage inputDataPackage) {
    String username = inputDataPackage.nextString();
    String encodedData = inputDataPackage.nextString();

    log.info(">>>>> Receive session Id from user: " + username);

    String salt = SaltUtil.getSaltByUsername(username);
    if (salt == null) {
      log.warn("AuthenticateBussiness : authenticateBySessionId : Client authenticate without salt");
      server.write(new DataPackage(ProtocolConstants.ResponseHeader.AUTHENTICATE_FAIL_RESPONSE));
      return;
    }

    ServerSessionStore store = new ServerSessionStore();
    store.server = server;
    store.encodedSessionId = encodedData;
    Global.encodedSessionFromClient.put(username, store);
    checkUserSession(username);
  }

  public static void onUserInfoReceive(ClientConnection client, DataPackage inputDataPackage) {
    String userName = inputDataPackage.nextString();
    ServerConnection server = Global.connectionMap.get(userName);
    if (server == null) {
      return;
    }

    User user = server.user;
    if (user == null) {
      return;
    }
    user.setMoney(inputDataPackage.nextInt());
    user.setAvatarId(inputDataPackage.nextInt());

    // Chuyển tiếp thông tin user này cho client
    DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.USER_INFO_REPONSE);
    dataPackage.putInt(user.getMoney());
    dataPackage.putInt(user.getAvatarId());
    server.write(dataPackage);
  }

  private static void checkUserSession(String username) {
    Session session = Global.sessionMapTmp.get(username);
    ServerSessionStore store = Global.encodedSessionFromClient.get(username);

    // Nếu chưa đủ dữ liệu để xác nhận thì thoát
    if ((session == null) || (store == null)) {
      return;
    }

    Global.sessionMapTmp.remove(username);
    Global.encodedSessionFromClient.remove(username);

    ServerConnection server = store.server;

    // User này chưa có session bên default service
    if (session.getId() == null) {
      log.warn("ERROR: AuthenticateBussiness : checkUserSession: user " + username
          + " authen with no session id in default service");
      server.write(new DataPackage(ProtocolConstants.ResponseHeader.AUTHENTICATE_FAIL_RESPONSE));
      return;
    }

    log.info(">>>>> Start check session for user: " + username);

    String encodedDataFromDefaultService = session.getId();
    String encodedDataFromClient = store.encodedSessionId;

    if (encodedDataFromDefaultService.equals(encodedDataFromClient)) {

      // Sau khi đăng nhập thành công thì tạo đối tượng user mới và đưa vào
      // lobby
      server.user = new User(username, server);
      server.user.entity = Global.lobby;
      Global.lobby.addUser(server.user);

      // Lưu trữ connection theo username để phục vụ tìm kiếm
      Global.connectionMap.put(username, server);
      Global.notAuthenConnectionList.remove(server);

      // Báo về client là authen thành công
      server.write(new DataPackage(ProtocolConstants.ResponseHeader.AUTHENTICATE_SUCCESS_RESPONSE));

      log.info(">>>>> Authenticate SUCCESS for user: " + username);

      // Báo cho DefaulService biết là user đã join in GameService
      DataPackage dataPackage = new DataPackage(ProtocolConstants.RequestHeader.USER_JOIN_IN_GAME_SERVER_INFORM_REQUEST);
      dataPackage.putString(username);
      Global.client.write(dataPackage);
    } else {
      // Báo về client là authen thất bại
      server.write(new DataPackage(ProtocolConstants.ResponseHeader.AUTHENTICATE_FAIL_RESPONSE));

      log.info(">>>>> Authenticate FAIL for user: " + username);
    }
  }
  
  public static void onDisconnect(ServerConnection server) {
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
      Global.connectionMap.remove(server.user.getName());
      server.detroy();
    }
  }
}
