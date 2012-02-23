package zgame.bussiness;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import zgame.bean.GameService;
import zgame.bean.Session;
import zgame.bean.User;
import zgame.dao.AuthenticateDao;
import zgame.exception.DupplicateException;
import zgame.exception.NotFoundException;
import zgame.main.Global;
import zgame.main.Main;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.Server;
import zgame.utils.MD5;
import zgame.utils.SaltUtil;

public class AuthenticateBussiness {
  private static Logger log = Logger.getLogger(AuthenticateBussiness.class);

  public static void disconnect(String username) {
    Session session = Global.sessionMap.get(username);
    if ((session != null) && (session.getCurrentGameService() == null)) {
      session.setOnline(false);
    }
  }

  public static void registerUser(Server server, DataPackage inputDataPackage) throws SQLException {
    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();
    String username = inputDataPackage.nextString();
    String md5Pass = inputDataPackage.nextString();
    int providerId = inputDataPackage.nextInt();

    int errorCode = 0;

    if (username.length() < Global.USER_NAME_MIN_LEN) {
      errorCode = ProtocolConstants.ErrorCode.USER_NAME_TOO_SHORT_ERROR;
    } else if (username.length() > Global.USER_NAME_MAX_LEN) {
      errorCode = ProtocolConstants.ErrorCode.USER_NAME_TOO_LONG_ERROR;
    } else {
      if (isExist(username, authenticateDao)) {
        errorCode = ProtocolConstants.ErrorCode.USER_NAME_EXIST_ERROR;
      }
    }

    if (errorCode < 0) {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.REGISTER_FAIL_RESPONSE);
      dataPackage.putString(username);
      dataPackage.putInt(errorCode);
      server.write(dataPackage);
      return;
    }

    try {
      int defaultMoney = 5000; // TODO: đang cho mỗi user khi tạo tài
      // khoản là 5000
      authenticateDao.createUser(username, md5Pass, defaultMoney, providerId);
      authenticateDao.commit();

      updateCache(username, authenticateDao); // Update cache
    } catch (DupplicateException e) {
      errorCode = ProtocolConstants.ErrorCode.USER_NAME_EXIST_ERROR;
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.REGISTER_FAIL_RESPONSE);
      dataPackage.putString(username);
      dataPackage.putInt(errorCode);
      server.write(dataPackage);
      return;
    }

    DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.REGISTER_SUCCESS_RESPONSE);
    dataPackage.putString(username);
    server.write(dataPackage);

    authenticateDao.close();
  }

  public static void changePassword(Server server, DataPackage inputDataPackage) throws SQLException {
    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();

    String username = inputDataPackage.nextString();
    String oldPassMd5 = inputDataPackage.nextString();
    String newPassMd5 = inputDataPackage.nextString();

    User user = getUserInfo(username, authenticateDao);
    if (user == null) {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.CHANGR_PASSWORD_FAIL_RESPONSE);
      dataPackage.putString(username);
      dataPackage.putInt(ProtocolConstants.ErrorCode.USER_NOT_EXIST_ERROR);
      server.write(dataPackage);
      return;
    }

    if (!user.getMd5Pass().equals(oldPassMd5)) {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.CHANGR_PASSWORD_FAIL_RESPONSE);
      dataPackage.putString(username);
      dataPackage.putInt(ProtocolConstants.ErrorCode.PASSWORD_NOT_CORRECT_ERROR);
      server.write(dataPackage);
      return;
    }

    try {
      authenticateDao.changePassword(username, newPassMd5);
      authenticateDao.commit();

      updateCache(username, authenticateDao); // Update cache
    } catch (NotFoundException e) {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.CHANGR_PASSWORD_FAIL_RESPONSE);
      dataPackage.putString(username);
      dataPackage.putInt(ProtocolConstants.ErrorCode.USER_NOT_EXIST_ERROR);
      server.write(dataPackage);
      return;
    }

    authenticateDao.close();
  }

  public static void signOut(Server server, DataPackage inputDataPackage) {
    String username = inputDataPackage.nextString();
    String encodedData = inputDataPackage.nextString();

    Session session = Global.sessionMap.get(username);
    if ((session != null) && encodedData.equals(MD5.toBase64(session.getId().getBytes()))) {
      signOut(username);
    }
  }

  protected static void signOut(String username) {
    Global.sessionMap.remove(username);
  }

  public static void checkAuthenticate(Server server, DataPackage inputDataPackage) throws SQLException {
    int userId = -1;
    String username = inputDataPackage.nextString();
    String encodedData = inputDataPackage.nextString();
    Session session = Global.sessionMap.get(username);

    if ((session != null) && session.isOnline()) {
      userId = ProtocolConstants.ErrorCode.ALREADY_ONLINE_ERROR;
      if (log.isDebugEnabled()) {
        log.debug("User already online error: " + username);
      }
    } else if (inputDataPackage.getHeader() == ProtocolConstants.RequestHeader.AUTHENTICATE_WITH_PASSWORD_REQUEST) {
      userId = checkLoginByPassword(username, encodedData);
    }

    // Nếu authen thành công thì userId > -1
    if (userId > -1) {
      // Nếu user đã đăng nhập với account khác thì sign out
      if (server.name != null && !server.name.equals("")) {
        Session oldSession = Global.sessionMap.get(server.name);
        if (oldSession != null) {
          Global.sessionMap.remove(server.name);
        }
      }

      Session session1 = new Session(userId, username);
      Global.sessionMap.put(session1.getUsername(), session1);
      server.name = session1.getUsername();
      Global.serverMap.put(server.name, server); // Đưa kết nối với client
      // đã authen thành công
      // vào map để phục vụ
      // cho việc tìm kiếm

      // Cấu trúc data: sessionId + sessionTimeout +
      // HEART_BREATH_SEQUENCE_TIME
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.AUTHENTICATE_SUCCESS_RESPONSE);
      dataPackage.putString(username);
      dataPackage.putString(session1.getId());
      dataPackage.putInt(Global.HEART_BREATH_SEQUENCE_TIME);

      // Đưa thêm danh sách GameService vào data trả về
      Map<String, GameService> gameServices = Main.gameServiceController.getGameServices();
      dataPackage.putInt(gameServices.size());
      Collection<GameService> c = gameServices.values();
      for (GameService gameService : c) {
        dataPackage.putString(gameService.getId());
        dataPackage.putString(gameService.getName());
        dataPackage.putString(gameService.getUrl());
        dataPackage.putInt(gameService.getPort());
        dataPackage.putInt(gameService.getCocurrentUser());
        dataPackage.putInt(gameService.getMaxUser());
      }
      server.write(dataPackage);
    } else {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.AUTHENTICATE_FAIL_RESPONSE);
      dataPackage.putString(username);
      dataPackage.putInt(userId); // Đưa mã lỗi vào thông báo trả về
      // (userId chính là mã lỗi)
      server.write(dataPackage);
    }
  }

  private static int checkLoginByPassword(String username, String encodedData) throws SQLException {
    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();
    if (!isExist(username, authenticateDao)) {
      return -1;
    }

    User user = getUserInfo(username, authenticateDao);
    String salt = SaltUtil.getSaltByUsername(username); // Nếu user authen
    // mà chưa có salt
    if (salt == null) {
      log.warn("User " + username + " authen with no salt cached in server.");
      return -1;
    }

    // Nếu password sai thì trả ra là đăng nhập không thành công
    String reencodedData = MD5.toBase64((username + user.getMd5Pass() + salt).getBytes());
    if (!reencodedData.equals(encodedData)) {
      return -1;
    }

    if (user.getIsBlock() == '1') { // Nếu user đã bị block
      return ProtocolConstants.ErrorCode.USER_BLOCK_ERROR;
    }
    authenticateDao.close();
    return user.getUserId();
  }

  public static void gameServiceListRequest(Server server, DataPackage inputDataPackage) {
    DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.GAME_SERVICE_LIST_RESPONSE);

    // Đưa thêm danh sách GameService vào data trả về
    Map<String, GameService> gameServices = Main.gameServiceController.getGameServices();
    dataPackage.putInt(gameServices.size());
    Collection<GameService> c = gameServices.values();
    for (GameService gameService : c) {
      dataPackage.putString(gameService.getId());
      dataPackage.putInt(gameService.getCocurrentUser());
    }
    server.write(dataPackage);
  }

  public static void requestSalt(Server server, DataPackage inputDataPackage) {
    // Lấy username do client gửi lên
    String username = inputDataPackage.nextString();

    // Tạo salt và lưu lại theo user name
    String salt = SaltUtil.createAndStoreSaltByUsername(username);

    // Gửi salt cho client
    DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.SALT_REPONSE);
    dataPackage.putString(salt);
    server.write(dataPackage);
  }

  public static String getUsernameByUserId(int userId) throws SQLException {
    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();

    String username = Global.userNameByUserIdMap.get(String.valueOf(userId));
    if (username == null) {
      updateCache(userId, authenticateDao);
      username = Global.userNameByUserIdMap.get(String.valueOf(userId));
    }

    if (username == null) {
      log.warn("Can NOT get user info of user id: " + userId);
    }

    authenticateDao.close();
    return username;
  }

  protected static User getUserInfo(String username, AuthenticateDao authenticateDao) throws SQLException {
    User user = Global.userInfoCache.get(username);
    if (user != null) {
      return user;
    }

    updateCache(username, authenticateDao);
    user = Global.userInfoCache.get(username);

    return user;
  }

  protected static boolean isExist(String username, AuthenticateDao authenticateDao) throws SQLException {
    User user = Global.userInfoCache.get(username);
    if (user != null) {
      return true;
    }

    updateCache(username, authenticateDao);
    user = Global.userInfoCache.get(username);

    return (user != null);
  }

  protected static void updateCache(String username, AuthenticateDao authenticateDao) throws SQLException {
    User user = authenticateDao.getUserInfo(username);
    if (user != null) {
      Global.userInfoCache.put(username, user);
      Global.userNameByUserIdMap.put(String.valueOf(user.getUserId()), username);
    }
  }

  protected static void updateCache(int userId, AuthenticateDao authenticateDao) throws SQLException {
    User user = authenticateDao.getUserInfo(userId);
    if (user != null) {
      Global.userInfoCache.put(user.getUsername(), user);
      Global.userNameByUserIdMap.put(String.valueOf(user.getUserId()), user.getUsername());
    }
  }
}
