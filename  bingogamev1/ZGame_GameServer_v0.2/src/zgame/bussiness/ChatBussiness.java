package zgame.bussiness;

import java.util.Collection;

import org.apache.log4j.Logger;

import zgame.bean.GroupChat;
import zgame.bean.Table;
import zgame.bean.User;
import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.server.Server;

public class ChatBussiness {
  private static final Logger log = Logger.getLogger(ChatBussiness.class);
  
  public static void onSendMessage(Server server, DataPackage dataPackage) {
    String fromUser = server.user.getName();
    String toId = dataPackage.nextString();
    String message = dataPackage.nextString(); // TODO: Cần lọc message (loại các từ ngữ ko cho phép)
    
    Table table = Global.tableMap.get(toId);
    GroupChat groupChat = Global.groupChatMap.get(toId);
    Server toUserServer = Global.serverMap.get(toId);
    
    if (table != null) { // Gửi cho table
      log.info(">>>> User " + fromUser + " send mesage to table " + table.getId() + ": \"" + message + "\"");
      
      Collection<User> users = table.getUsers();
      for (User toUser : users) {
        Server toServer = Global.serverMap.get(toUser.getName());
        if (toServer != null) {
          DataPackage sendMessageDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.MESSAGE_RESPONSE);
          sendMessageDataPackage.putString(fromUser);
          sendMessageDataPackage.putString(table.getId());
          sendMessageDataPackage.putString(message);
          toServer.write(sendMessageDataPackage);
        }
      }
    } else if (groupChat != null) { // gửi cho group chat
      log.info(">>>> User " + fromUser + " send mesage to group " + groupChat.getId() + ": \"" + message + "\"");
      
      for (User toUser : groupChat.values()) {
        Server toServer = Global.serverMap.get(toUser.getName());
        if (toServer != null) {
          DataPackage sendMessageDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.MESSAGE_RESPONSE);
          sendMessageDataPackage.putString(fromUser);
          sendMessageDataPackage.putString(groupChat.getId());
          sendMessageDataPackage.putString(message);
          toServer.write(sendMessageDataPackage);
        }
      }
    } else if (toUserServer != null) { // gửi cho user đang online ở Game Server này
      log.info(">>>> User " + fromUser + " send mesage to user " + toUserServer.user.getName() + ": \"" + message + "\"");
      
      DataPackage sendMessageDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.MESSAGE_RESPONSE);
      sendMessageDataPackage.putString(fromUser);
      sendMessageDataPackage.putString(fromUser);
      sendMessageDataPackage.putString(message);
      toUserServer.write(sendMessageDataPackage);
    } else { // User không online ở Game Server này
      // TODO: forward message to Default Service
    }
  }
  
  public static void onInviteToGroup(Server server, DataPackage dataPackage) {
  }
  
  public static void onAcceptJoinToGroup(Server server, DataPackage dataPackage) {
  }
  
  public static void onDenyJoinToGroup(Server server, DataPackage dataPackage) {
  }
}
