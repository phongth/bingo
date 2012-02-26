package zgame.bussiness;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import zgame.bean.Friend;
import zgame.bean.Session;
import zgame.bean.User;
import zgame.dao.AuthenticateDao;
import zgame.dao.FriendDao;
import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.ServerConnection;

public class FriendBussiness {
  private static final Logger log = Logger.getLogger(FriendBussiness.class);

  public static void onAddFriendRequest(ServerConnection server, DataPackage dataPackage) throws SQLException {
    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();
    FriendDao friendDao = FriendDao.createInstance();

    String fromUser = dataPackage.nextString();
    String toUser = dataPackage.nextString();

    if (log.isDebugEnabled()) {
      log.debug("onAddFriendRequest : fromUser " + fromUser + " : toUser " + toUser);
    }

    // If fromUser does not exist then log warning
    if (!AuthenticateBussiness.isExist(fromUser, authenticateDao)) {
      log.warn("FriendBussiness : onAddFriendRequest : fromUser does NOT exist: " + fromUser);
      return;
    }

    // If toUser does not exist then notify add friend fail
    if (!AuthenticateBussiness.isExist(toUser, authenticateDao)) {
      DataPackage userNotExistDataPackage = new DataPackage(
          ProtocolConstants.ResponseHeader.ADD_FRIEND_FAIL_USER_NOT_EXIST_RESPONSE);
      userNotExistDataPackage.putString(fromUser);
      userNotExistDataPackage.putString(toUser);
      server.write(userNotExistDataPackage);
      return;
    }

    // Check if fromUser and toUser are already friend
    if (isFriend(fromUser, toUser, friendDao)) {
      DataPackage alreadyFriendDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.ALREADY_FRIEND_RESPONSE);
      alreadyFriendDataPackage.putString(fromUser);
      alreadyFriendDataPackage.putString(toUser);
      server.write(alreadyFriendDataPackage);
      return;
    }

    // Add requestAddFriend to cache
    Map<String, String> requestAddFriend = Global.requestAddFriendCache.get(fromUser);
    if (requestAddFriend == null) {
      requestAddFriend = new HashMap<String, String>();
      Global.requestAddFriendCache.put(fromUser, requestAddFriend);
    }
    requestAddFriend.put(toUser, toUser);

    // Forward addFriendRequest to the GameService of toUser
    Session toUserSession = Global.sessionMap.get(toUser);
    if (toUserSession != null) {
      if (toUserSession.getCurrentGameService() != null) {
        ServerConnection toUserServer = toUserSession.getCurrentGameService().getServer();
        DataPackage addFriendDataPackage = new DataPackage(ProtocolConstants.RequestHeader.ADD_FRIEND_REQUEST);
        addFriendDataPackage.putString(fromUser);
        addFriendDataPackage.putString(toUser);
        toUserServer.write(addFriendDataPackage);
      }
    }

    authenticateDao.close();
    friendDao.close();
  }

  public static void onAddFriendAgreeRequest(ServerConnection server, DataPackage dataPackage) throws SQLException {
    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();
    FriendDao friendDao = FriendDao.createInstance();

    String fromUser = dataPackage.nextString();
    String toUser = dataPackage.nextString();

    if (log.isDebugEnabled()) {
      log.debug("onAddFriendAgreeRequest : fromUser " + fromUser + " : toUser " + toUser);
    }

    // If fromUser does not exist then log warning
    if (!AuthenticateBussiness.isExist(fromUser, authenticateDao)) {
      log.warn("FriendBussiness : onAddFriendAgreeRequest : fromUser does not exist: " + fromUser);
      return;
    }

    // If toUser does not exist then log warning
    if (!AuthenticateBussiness.isExist(toUser, authenticateDao)) {
      log.warn("FriendBussiness : onAddFriendAgreeRequest : toUser does not exist: " + toUser);
      return;
    }

    // Check and remove requestAddFriendFromCache
    boolean isAlreadyAddFriend = false;
    Map<String, String> requestAddFriend = Global.requestAddFriendCache.get(fromUser);
    if (requestAddFriend != null) {
      isAlreadyAddFriend = (requestAddFriend.get(toUser) != null);
      requestAddFriend.remove(toUser);
    }

    // If aleady friend then
    if (!isAlreadyAddFriend) {
      log.warn("FriendBussiness: onAddFriendAgreeRequest: User did NOT request friend (fromUser: " + fromUser + ", toUser: "
          + toUser + ")");
      return;
    }

    // Get user 1 id
    User user1Info = AuthenticateBussiness.getUserInfo(fromUser, authenticateDao);
    if (user1Info == null) {
      log.warn("FriendBussiness: onAddFriendAgreeRequest: Can NOT get the info of user: " + fromUser);
      return;
    }
    int user1Id = user1Info.getUserId();

    // Get user 2 id
    User user2Info = AuthenticateBussiness.getUserInfo(toUser, authenticateDao);
    if (user2Info == null) {
      log.warn("FriendBussiness: onAddFriendAgreeRequest: Can NOT get the info of user: " + toUser);
      return;
    }
    int user2Id = user2Info.getUserId();

    // Make friend
    friendDao.makeFriend(user1Id, user2Id);
    updateCache(fromUser, friendDao);
    updateCache(toUser, friendDao);

    // Forward addFriendAgreeRequest to the GameService of fromUser
    Session fromUserSession = Global.sessionMap.get(fromUser);
    if (fromUserSession != null) {
      if (fromUserSession.getCurrentGameService() != null) {
        ServerConnection fromUserServer = fromUserSession.getCurrentGameService().getServer();
        DataPackage addFriendAgreeDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.ADD_FRIEND_SUCCESS_RESPONSE);
        addFriendAgreeDataPackage.putString(fromUser);
        addFriendAgreeDataPackage.putString(toUser);
        fromUserServer.write(addFriendAgreeDataPackage);

        // Update friend list for fromUser
        fromUserServer.write(createFriendListDataPackageOfUser(fromUser));
      }
    }

    // Send friend list to toUser
    Session toUserSession = Global.sessionMap.get(toUser);
    if (toUserSession != null) {
      if (toUserSession.getCurrentGameService() != null) {
        ServerConnection toUserServer = toUserSession.getCurrentGameService().getServer();
        toUserServer.write(createFriendListDataPackageOfUser(toUser));
      }
    }

    authenticateDao.close();
    friendDao.close();
  }

  public static void onAddFriendDenyRequest(ServerConnection server, DataPackage dataPackage) throws SQLException {
    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();

    String fromUser = dataPackage.nextString();
    String toUser = dataPackage.nextString();

    if (log.isDebugEnabled()) {
      log.debug("onAddFriendDenyRequest : fromUser " + fromUser + " : toUser " + toUser);
    }

    // If fromUser does not exist then log warning
    if (!AuthenticateBussiness.isExist(fromUser, authenticateDao)) {
      log.warn("FriendBussiness : onAddFriendAgreeRequest : fromUser does not exist: " + fromUser);
      return;
    }

    // If toUser does not exist then log warning
    if (!AuthenticateBussiness.isExist(toUser, authenticateDao)) {
      log.warn("FriendBussiness : onAddFriendAgreeRequest : toUser does not exist: " + toUser);
      return;
    }

    // Remove requestAddFriendFromCache
    Map<String, String> requestAddFriend = Global.requestAddFriendCache.get(fromUser);
    if (requestAddFriend != null) {
      requestAddFriend.remove(toUser);
    }

    // Forward addFriendDenyRequest to the GameService of fromUser
    Session fromUserSession = Global.sessionMap.get(fromUser);
    if (fromUserSession != null) {
      if (fromUserSession.getCurrentGameService() != null) {
        ServerConnection fromUserServer = fromUserSession.getCurrentGameService().getServer();
        DataPackage addFriendAgreeDataPackage = new DataPackage(ProtocolConstants.RequestHeader.ADD_FRIEND_DENY_REQUEST);
        addFriendAgreeDataPackage.putString(fromUser);
        addFriendAgreeDataPackage.putString(toUser);
        fromUserServer.write(addFriendAgreeDataPackage);
      }
    }

    authenticateDao.close();
  }

  public static void onGetFriendListRequest(ServerConnection server, DataPackage dataPackage) throws SQLException {
    String username = dataPackage.nextString();

    if (log.isDebugEnabled()) {
      log.debug("onGetFriendListRequest : fromUser " + username);
    }

    server.write(createFriendListDataPackageOfUser(username));

  }

  public static void onUserJoinToGameService(String username, ServerConnection server) throws SQLException {
    // Update user friend list to gameService
    server.write(createFriendListDataPackageOfUser(username));

    // Check and send requestAddFriend offline to GameServer to forward to user
    for (String fromUser : Global.requestAddFriendCache.keySet()) {
      for (String toUser : Global.requestAddFriendCache.get(fromUser).keySet()) {
        if (toUser.equals(username)) {
          DataPackage dataPackage = new DataPackage(ProtocolConstants.RequestHeader.ADD_FRIEND_REQUEST);
          dataPackage.putString(fromUser);
          dataPackage.putString(username); // toUser;
          server.write(dataPackage);
        }
      }
    }
  }

  private static DataPackage createFriendListDataPackageOfUser(String username) throws SQLException {
    FriendDao friendDao = FriendDao.createInstance();

    // Build friend list info
    List<Friend> friendList = new ArrayList<Friend>();
    List<String> friendNameList = getFriendList(username, friendDao);
    for (String friendName : friendNameList) {
      Friend friend = new Friend().setUsername(friendName);
      Session friendSession = Global.sessionMap.get(friendName);
      if (friendSession != null && friendSession.isOnline()) {
        friend.setOnline(true);
        friend.setLocationInfo(friendSession.getCurrentGameService().getName());
      }
      friendList.add(friend);
    }

    // Send friend to the GameService that user's online
    DataPackage friendListDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.FRIEND_LIST_RESPONSE);
    friendListDataPackage.putString(username); // owner
    friendListDataPackage.putInt(friendList.size());
    for (Friend friend : friendList) {
      friendListDataPackage.putString(friend.getUsername());
      friendListDataPackage.putInt(friend.isOnline() ? 1 : 0);
      friendListDataPackage.putString(friend.getLocationInfo());
    }
    friendDao.close();

    return friendListDataPackage;
  }

  protected static List<String> getFriendList(String username, FriendDao friendDao) throws SQLException {
    List<String> friendList = new ArrayList<String>();
    Map<String, String> friendMap = Global.friendListCache.get(username);
    if (friendMap == null) {
      updateCache(username, friendDao);
      friendMap = Global.friendListCache.get(username);
    }

    for (String friendName : friendMap.keySet()) {
      friendList.add(friendName);
    }
    return friendList;
  }

  protected static boolean isFriend(String username1, String username2, FriendDao friendDao) throws SQLException {
    // Get friendList of username1
    Map<String, String> friendMap = Global.friendListCache.get(username1);
    if (friendMap == null) {
      updateCache(username1, friendDao);
      friendMap = Global.friendListCache.get(username1);
    }
    return (friendMap.get(username2) != null);
  }

  protected static void updateCache(String username, FriendDao friendDao) throws SQLException {
    if (username == null) {
      return;
    }

    AuthenticateDao authenticateDao = AuthenticateDao.createInstance();

    // Check friend list on cache
    Map<String, String> friendMap = Global.friendListCache.get(username);
    if (friendMap == null) {
      friendMap = new HashMap<String, String>();
      Global.friendListCache.put(username, friendMap);
    }

    // Get user id
    User userInfo = AuthenticateBussiness.getUserInfo(username, authenticateDao);
    if (userInfo == null) {
      log.warn("FriendBussiness: updateCache: Can NOT get the info of user: " + username);
      return;
    }
    int userId = userInfo.getUserId();

    // Get friend list
    List<String> friends = friendDao.getFriendList(userId);
    for (String friendName : friends) {
      friendMap.put(friendName, friendName);
    }

    authenticateDao.close();
  }
}
