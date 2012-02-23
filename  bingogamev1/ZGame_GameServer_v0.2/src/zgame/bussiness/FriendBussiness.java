package zgame.bussiness;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import zgame.bean.Entity;
import zgame.bean.Friend;
import zgame.bean.Game;
import zgame.bean.Room;
import zgame.bean.Table;
import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.server.Server;

public class FriendBussiness {
  private static final Logger log = Logger.getLogger(FriendBussiness.class);

  public static void onAddFriendRequest(Server server, DataPackage dataPackage) {
    String fromUser = server.user.getName();
    String toUser = dataPackage.nextString();

    if (log.isDebugEnabled()) {
      log.debug("onAddFriendRequest : fromUser " + fromUser + " : toUser " + toUser);
    }

    if (fromUser.equals(toUser)) {
      return;
    }

    // Get friend list from cache and check for already friend
    Map<String, Friend> friends = Global.friendListCache.get(fromUser);
    if (friends != null) {
      // If they are already friends then notify to client
      if (friends.get(toUser) != null) {
        DataPackage alreadyFriendDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.ALREADY_FRIEND_RESPONSE);
        alreadyFriendDataPackage.putString(toUser);
        server.write(alreadyFriendDataPackage);
        return;
      }
    }

    // Forward the request to DefaultService
    DataPackage addFriendRequestDataPackage = new DataPackage(ProtocolConstants.RequestHeader.ADD_FRIEND_REQUEST);
    addFriendRequestDataPackage.putString(fromUser); // fromUser
    addFriendRequestDataPackage.putString(toUser); // toUser
    Global.client.write(addFriendRequestDataPackage);
  }

  public static void onAddFriendAgreeRequest(Server server, DataPackage dataPackage) {
    String fromUser = dataPackage.nextString();
    String toUser = server.user.getName();

    if (log.isDebugEnabled()) {
      log.debug("onAddFriendAgreeRequest : fromUser " + fromUser + " : toUser " + toUser);
    }

    // Forward addFriendAgree to DefaultService
    DataPackage addFriendAgreeDataPackage = new DataPackage(ProtocolConstants.RequestHeader.ADD_FRIEND_AGREE_REQUEST);
    addFriendAgreeDataPackage.putString(fromUser);
    addFriendAgreeDataPackage.putString(toUser);
    Global.client.write(addFriendAgreeDataPackage);
  }

  public static void onAddFriendDenyRequest(Server server, DataPackage dataPackage) {
    String fromUser = dataPackage.nextString();
    String toUser = server.user.getName();

    if (log.isDebugEnabled()) {
      log.debug("onAddFriendDenyRequest : fromUser " + fromUser + " : toUser " + toUser);
    }

    // Forward addFriendDeny to DefaultService
    DataPackage addFriendAgreeDataPackage = new DataPackage(ProtocolConstants.RequestHeader.ADD_FRIEND_DENY_REQUEST);
    addFriendAgreeDataPackage.putString(fromUser);
    addFriendAgreeDataPackage.putString(toUser);
    Global.client.write(addFriendAgreeDataPackage);
  }

  public static void onFriendListRequest(Server server, DataPackage dataPackage) {
    if (log.isDebugEnabled()) {
      log.debug("onFriendListRequest : fromUser " + server.user.getName());
    }

    // Request to update friend list from default service
    DataPackage requestFriendListDataPackage = new DataPackage(ProtocolConstants.RequestHeader.GET_FRIEND_LIST_REQUEST);
    requestFriendListDataPackage.putString(server.user.getName());
    Global.client.write(requestFriendListDataPackage);

    // Vector to store friend list to send to client, only friend now has same
    // server with requested user will be send
    Vector<Friend> toSendFriend = new Vector<Friend>();

    // Get infomation and build friend list
    Map<String, Friend> friends = Global.friendListCache.get(server.user.getName());
    if (friends != null) {
      for (String friendName : friends.keySet()) {
        Friend friend = friends.get(friendName);
        Server friendServer = Global.serverMap.get(friend.getUsername());
        if (friendServer != null) {
          String locationInfo = getUserLocation(friendServer.user.entity);
          friend.setOnline(true);
          friend.setLocationInfo(locationInfo);
          toSendFriend.add(friend);
        }
      }

      // Send friend list to client
      DataPackage friendListDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.FRIEND_LIST_RESPONSE);
      friendListDataPackage.putInt(toSendFriend.size());
      for (int i = 0; i < toSendFriend.size(); i++) {
        Friend friend = toSendFriend.get(i);
        friendListDataPackage.putString(friend.getUsername());
        friendListDataPackage.putInt(friend.isOnline() ? 1 : 0);
        friendListDataPackage.putString(friend.getLocationInfo());
      }
      server.write(friendListDataPackage);
    }
  }

  public static String getUserLocation(Entity entity) {
    if (entity == null) {
      return " ";
    }

    if (entity instanceof Table) {
      Table table = (Table) entity;
      Room room = table.getRoom();
      Game game = table.getGame();
      return "Bàn " + table.getName() + " Phòng " + room.getName() + " " + game.getId();
    }

    if (entity instanceof Room) {
      Room room = (Room) entity;
      Game game = room.getGame();
      return "Phòng " + room.getName() + " " + game.getId();
    }
    return "Phòng chờ";
  }

  public static void onAddFriendSuccessFromDefaultService(DataPackage dataPackage) {
    String fromUser = dataPackage.nextString();
    String toUser = dataPackage.nextString();

    // if fromUser online in this server, then forward the message
    Server fromUserServer = Global.serverMap.get(fromUser);
    if (fromUserServer != null) {
      DataPackage addFriendSuccessDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.ADD_FRIEND_SUCCESS_RESPONSE);
      addFriendSuccessDataPackage.putString(toUser);
      fromUserServer.write(addFriendSuccessDataPackage);
    }
  }

  public static void onAddFriendFailUserDenyFromDefaultService(DataPackage dataPackage) {
    String fromUser = dataPackage.nextString();
    String toUser = dataPackage.nextString();

    // if fromUser online in this server, then forward the message
    Server fromUserServer = Global.serverMap.get(fromUser);
    if (fromUserServer != null) {
      DataPackage addFriendFailDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.ADD_FRIEND_FAIL_USER_DENY_RESPONSE);
      addFriendFailDataPackage.putString(toUser);
      fromUserServer.write(addFriendFailDataPackage);
    }
  }

  public static void onAddFriendFailUserNotExistFromDefaultService(DataPackage dataPackage) {
    String fromUser = dataPackage.nextString();
    String toUser = dataPackage.nextString();

    // if fromUser online in this server, then forward the message
    Server fromUserServer = Global.serverMap.get(fromUser);
    if (fromUserServer != null) {
      DataPackage addFriendFailDataPackage = new DataPackage(
          ProtocolConstants.ResponseHeader.ADD_FRIEND_FAIL_USER_NOT_EXIST_RESPONSE);
      addFriendFailDataPackage.putString(toUser);
      fromUserServer.write(addFriendFailDataPackage);
    }
  }

  public static void onAddFriendRequestFromDefaultService(DataPackage dataPackage) {
    String fromUser = dataPackage.nextString();
    String toUser = dataPackage.nextString();

    // if toUser online in this server, then forward the message
    Server toUserServer = Global.serverMap.get(toUser);
    if (toUserServer != null) {
      DataPackage addFriendNotifyDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.ADD_FRIEND_NOTIFY_RESPONSE);
      addFriendNotifyDataPackage.putString(fromUser);
      toUserServer.write(addFriendNotifyDataPackage);
    }
  }

  public static void onAlreadyFriendResponseFromDefaultService(DataPackage dataPackage) {
    String fromUser = dataPackage.nextString();
    String toUser = dataPackage.nextString();

    // if toUser online in this server, then forward the message
    Server fromUserServer = Global.serverMap.get(fromUser);
    if (fromUserServer != null) {
      DataPackage alreadyFriendDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.ALREADY_FRIEND_RESPONSE);
      alreadyFriendDataPackage.putString(toUser);
      fromUserServer.write(alreadyFriendDataPackage);
    }
  }

  public static void onFriendListResponseFromDefaultService(DataPackage dataPackage) {
    String owner = dataPackage.nextString();

    // Receive friend list
    int friendListSize = dataPackage.nextInt();
    Map<String, Friend> receivedFriends = new HashMap<String, Friend>();
    for (int i = 0; i < friendListSize; i++) {
      String username = dataPackage.nextString();
      boolean isOnline = (dataPackage.nextInt() == 1);
      String locationInfo = dataPackage.nextString();

      Friend friend = new Friend().setUsername(username).setOnline(isOnline).setLocationInfo(locationInfo);
      Server friendServer = Global.serverMap.get(username);
      if (friendServer != null) {
        friend.setLocationInfo(getUserLocation(friendServer.user.entity));
      }
      receivedFriends.put(username, friend);
    }

    // Find owner connection
    Server ownerServer = Global.serverMap.get(owner);
    if (ownerServer == null) {
      return;
    }

    // Cache friend list
    Global.friendListCache.put(owner, receivedFriends);

    // Send friend list to client
    DataPackage friendListDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.FRIEND_LIST_RESPONSE);
    friendListDataPackage.putInt(receivedFriends.size());

    for (String friendName : receivedFriends.keySet()) {
      Friend friend = receivedFriends.get(friendName);
      friendListDataPackage.putString(friend.getUsername());
      friendListDataPackage.putInt(friend.isOnline() ? 1 : 0);
      friendListDataPackage.putString(friend.getLocationInfo());
    }
    ownerServer.write(friendListDataPackage);
  }
}
