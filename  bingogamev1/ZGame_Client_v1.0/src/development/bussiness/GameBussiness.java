package development.bussiness;

import java.util.Vector;

import state.Alert;
import state.AlertListener;
import state.GameGlobal;
import state.Transformer;
import state.socket.DataPackage;
import development.Global;
import development.bean.Game;
import development.bean.Room;
import development.bean.Table;
import development.bean.User;
import development.socket.ProtocolConstants;
import development.socket.SocketClientUtil;

public class GameBussiness implements AlertListener {
  public void alertEventPerform(int alertType, int eventType, int alertId) {
    // Khi user không đủ tiền so với đặt cược mới, thì thông báo và thoát ra danh sách bàn
    if (alertId == 98) {
      GameGlobal.nextState(Global.frmListTable, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
    }
    
    // Khi người điểm cược thay đổi và người chơi đủ tiền, hỏi người chơi là có muốn chơi tiếp không
    else if (alertId == 97 && eventType == Alert.NO_BUTTON) {
      SocketClientUtil.leaveTableRequest();
      GameGlobal.nextState(Global.frmListTable, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
    }
  }
  
  public void onUserInfoReceive(DataPackage dataPackage) {
    Global.currentUser.setMoney(dataPackage.nextInt());
    Global.currentUser.setAvatarId(dataPackage.nextInt());
  }
  
  public void onStartGame() {
    if (Global.currentGame.getId().equals("caro")) {
      GameGlobal.nextState(Global.frmCaro, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
    } else if (Global.currentGame.getId().equals("cotuong")) {
      GameGlobal.nextState(Global.frmChess, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
    } else if (Global.currentGame.getId().equals("covua")) {
      GameGlobal.nextState(Global.frmBroadChess, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
    }
  }
  
  public void onGameStartFail(DataPackage dataPackage) {
    int errorCode = dataPackage.nextInt();
    switch (errorCode) {
    case ProtocolConstants.ErrorCode.NOT_ENOUGH_USER_TO_START_GAME_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Không đủ người chơi để bắt đầu", 99);
      break;
    case ProtocolConstants.ErrorCode.USER_NOT_READY_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Có người chơi chưa sẵn sàng", 99);
      break;
    case ProtocolConstants.ErrorCode.NO_AUTHEN_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Mất kết nối với máy chủ", 99);
      System.err.println("Game start fail: NO_AUTHEN_ERROR");
      break;
    default:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {"Game bắt đầu thất bại"});
      break;
    }
  }
  
  public void onConfigTableFail(DataPackage dataPackage) {
    int errorCode = dataPackage.nextInt();
    switch (errorCode) {
    case ProtocolConstants.ErrorCode.TABLE_PASSWORD_TOO_LONG_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {"Thiết lập bàn thất bại", "Mật khẩu quá dài"});
      break;
    case ProtocolConstants.ErrorCode.NOT_ENOUGH_MONEY_TO_SET_TABLE_CONFIG_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {"Thiết lập bàn thất bại", "Bạn không đủ tiền"});
      break;
    case ProtocolConstants.ErrorCode.NO_AUTHEN_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Mất kết nối với máy chủ", 99);
      System.err.println("Table config fail: NO_AUTHEN_ERROR");
      break;
    default:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {"Thiết lập bàn thất bại"});
      break;
    }
  }
  
  public void onConfigTableChange(DataPackage dataPackage) {
    int bid = dataPackage.nextInt();
    
    if (bid != Global.currentTable.getBid()) {
      Global.currentTable.setBid(bid);
      
      // Nếu người chơi không phải là chủ bàn
      if (!Global.currentUser.getName().equals(Global.currentTable.getTableMasterName())) {
        // Nếu người chơi không đủ tiền 
        if (Global.currentUser.getMoney() < Global.currentTable.getBid()) {
          GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {"Điểm cược đã thay đổi thành " + bid, "Bạn không đủ tiền để chơi ở bàn này"}, 98);
        } else {
          GameGlobal.alert.showAlert(this, Alert.YES_NO_TYPE, new String[] {"Điểm cược đã thay đổi thành " + bid, "Bạn có muốn chơi tiếp?"}, 97);
        }
      }
    }
  }
  
  public void onJoinTableFail(DataPackage dataPackage) {
    int errorCode = dataPackage.nextInt();
    switch (errorCode) {
    case ProtocolConstants.ErrorCode.TABLE_FULL_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Bàn đã đầy");
      break;
    case ProtocolConstants.ErrorCode.NOT_ENOUGH_MONEY_TO_JOIN_TABLE_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Bạn không đủ tiền để vào bàn");
      break;
    case ProtocolConstants.ErrorCode.TABLE_PASSWORD_WRONG_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Mật khẩu không đúng");
      break;
    case ProtocolConstants.ErrorCode.TABLE_ALREADY_PLAY_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Bàn đang chơi");
      break;
    case ProtocolConstants.ErrorCode.NO_AUTHEN_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Mất kết nối với máy chủ", 99);
      System.err.println("Join table fail: NO_AUTHEN_ERROR");
      break;
    default:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {"Vào bàn thất bại"});
      break;
    }
  }
  
  public void onJoinRoomFail(DataPackage dataPackage) {
    int errorCode = dataPackage.nextInt();
    switch (errorCode) {
    case ProtocolConstants.ErrorCode.NOT_ENOUGN_MONEY_TO_JOIN_ROOM_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Bạn không đủ tiền để vào phòng");
      break;
    case ProtocolConstants.ErrorCode.ROOM_FULL_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Phòng đã đầy");
      break;
    case ProtocolConstants.ErrorCode.NO_AUTHEN_ERROR:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Mất kết nối với máy chủ", 99);
      System.err.println("Join room fail: NO_AUTHEN_ERROR");
      break;
    default:
      GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {"Vào phòng thất bại"});
      break;
    }
  }
  
  public void getTableUserList(DataPackage dataPackage) {
    Global.tableUsers.removeAllElements();
    Vector users = new Vector();
    int index = -1;
    Global.currentTable.setTableMasterName(dataPackage.nextString());
    
    int size = dataPackage.nextInt();
    for (int i = 0; i < size; i++) {
      User user = new User(dataPackage.nextString());
      user.setMoney(dataPackage.nextInt());
      user.setReady(dataPackage.nextInt() == 0 ? false : true);
      user.setAvatarId(dataPackage.nextInt());
      
      users.addElement(user);
      if (user.equals(Global.currentUser)) {
        index = i;
        Global.currentUser.setReady(user.isReady());
        Global.currentUser.setMoney(user.getMoney());
        Global.currentUser.setAvatarId(user.getAvatarId());
      }
    }
    
    // Đưa người dùng hiện tại lên đầu danh sách
    if (index == -1 || index == 0) {
      for (int i = 0; i < users.size(); i++) {
        Global.tableUsers.addElement(users.elementAt(i));
      }
    } else {
      for (int i = index; i < users.size(); i++) {
        Global.tableUsers.addElement(users.elementAt(i));
      }
      for (int i = 0; i < index; i++) {
        Global.tableUsers.addElement(users.elementAt(i));
      }
    }
  }
  
  public void getTableList(DataPackage dataPackage) {
    String roomId = dataPackage.nextString();
    Room room = (Room) Global.roomMap.get(roomId);
    if (room == null) {
      return;
    }
    
    room.clearChildList();
    Global.tableMap.clear();
    int size = dataPackage.nextInt();
    for (int i = 0; i < size; i++) {
      Table table = new Table(dataPackage.nextString());
      table.setName(dataPackage.nextString());
      table.setBid(dataPackage.nextInt());
      table.setLocked(dataPackage.nextInt() == 0 ? false : true);
      table.setConcurrentUser(dataPackage.nextInt());
      table.setMaxUser(dataPackage.nextInt());
      table.setPlaying(dataPackage.nextInt() == 0 ? false : true);
      
      room.putChild(table);
      Global.tableMap.put(table.getId(), table);
    }
  }
  
  public void getRoomList(DataPackage dataPackage) {
    String gameId = dataPackage.nextString();
    Game game = (Game) Global.gameMap.get(gameId);
    if (game == null) {
      return;
    }
    
    game.clearChildList();
    Global.roomMap.clear();
    int size = dataPackage.nextInt();
    for (int i = 0; i < size; i++) {
      Room room = new Room(dataPackage.nextString());
      room.setName(dataPackage.nextString());
      room.setType(dataPackage.nextInt());
      room.setConcurrentUser(dataPackage.nextInt());
      room.setMaxUser(dataPackage.nextInt());
      room.setMinBid(dataPackage.nextInt());
      room.setMaxBid(dataPackage.nextInt());
      
      game.putChild(room);
      Global.roomMap.put(room.getId(), room);
    }
  }
}
