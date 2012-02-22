package development.socket;

import state.Alert;
import state.AlertListener;
import state.GameGlobal;
import state.Transformer;
import state.socket.DataPackage;
import state.socket.DataReceiveListener;
import state.socket.DefaultProtocolConstants;
import development.Global;
import development.bussiness.FriendBussiness;
import development.bussiness.GameBussiness;

public class GameActionHandle implements DataReceiveListener, AlertListener {
	public static boolean isOnSignOut = false; 
	
	private GameBussiness gameBussiness;
	private FriendBussiness friendBussiness;
	
	public GameActionHandle() {
	  gameBussiness = new GameBussiness();
	  friendBussiness = new FriendBussiness();
	}
	
	public void alertEventPerform(int alertType, int eventType, int alertId) {
    // Nếu đăng nhập vào GameService thất bại thì cho out ra màn hình đăng nhập để đăng nhập lại
    if (alertId == 99) {
      GameGlobal.nextState(Global.frmLogin, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
    }
    
    // Khi mất kết nối với máy chủ
    else if (alertId == 96) {
      GameGlobal.nextState(Global.frmLogin, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
    }
  }
	
	public void onRecieveData(DataPackage dataPackage) {
		int header = dataPackage.getHeader();
		switch (header) {
		
		/** Authenticate */
		case ProtocolConstants.ResponseHeader.SALT_REPONSE:
			String salt = dataPackage.nextString();
			SocketClientUtil.authenToGameServerBySessionId(Global.loginInfo.getUserName(), Global.session.getId(), salt);
			break;
		case ProtocolConstants.ResponseHeader.AUTHENTICATE_SUCCESS_RESPONSE:
			onAuthenSuccess();
			break;
		case ProtocolConstants.ResponseHeader.AUTHENTICATE_FAIL_RESPONSE:
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {"Đăng nhập vào server game thất bại", "Hãy thử đăng nhập lại"}, 99);
			break;
		case ProtocolConstants.ResponseHeader.GAME_SERVER_FULL_RESPONSE:
		  GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Server đã đầy");
			break;
			
		/** Game, room and table */
		case ProtocolConstants.ResponseHeader.ROOM_LIST_RESPONSE:
		  gameBussiness.getRoomList(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.TABLE_LIST_RESPONSE:
		  gameBussiness.getTableList(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.JOIN_GAME_SUCCESS_RESPONSE:
		  gameBussiness.getRoomList(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.JOIN_GAME_FAIL_RESPONSE:
			break;
		case ProtocolConstants.ResponseHeader.JOIN_ROOM_SUCCESS_RESPONSE:
		  gameBussiness.getTableList(dataPackage);
		  GameGlobal.nextState(Global.frmListTable, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
			break;
		case ProtocolConstants.ResponseHeader.JOIN_ROOM_FAIL_RESPONSE:
		  gameBussiness.onJoinRoomFail(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.JOIN_TABLE_SUCCESS_RESPONSE:
		  GameGlobal.nextState(Global.frmTable, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
			break;
		case ProtocolConstants.ResponseHeader.JOIN_TABLE_FAIL_RESPONSE:
		  gameBussiness.onJoinTableFail(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.USER_LIST_OF_TABLE_RESPONSE:
		  gameBussiness.getTableUserList(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.CONFIG_TABLE_SUCCESS_RESPONSE:
		  Global.frmTable.isConfigTableDone = true;
			break;
		case ProtocolConstants.ResponseHeader.TABLE_CONFIG_CHANGE_RESPONSE:
		  gameBussiness.onConfigTableChange(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.CONFIG_TABLE_FAIL_RESPONSE:
		  gameBussiness.onConfigTableFail(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.GAME_START_RESPONSE:
		  gameBussiness.onStartGame();
			break;
		case ProtocolConstants.ResponseHeader.GAME_START_FAIL_RESPONSE:
		  gameBussiness.onGameStartFail(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.USER_INFO_REPONSE:
		  gameBussiness.onUserInfoReceive(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.LIST_USER_WANT_TO_PLAY_RESPONSE:
		  gameBussiness.onListUsersWantToPlay(dataPackage);
      break;
    case ProtocolConstants.ErrorCode.NO_MORE_FREE_USER:
      System.out.println("No more free user");
      break;
			
		/** Game Action*/
		case ProtocolConstants.ResponseHeader.GAME_ACTION_RESPONSE:
			Global.currentGame.onRecieveData(getGameActionPackage(dataPackage));
			break;
		
			
	  /** Friend function */
		case ProtocolConstants.ResponseHeader.ADD_FRIEND_NOTIFY_RESPONSE:
		  friendBussiness.onAddFriendNotify(dataPackage);
		  break;
		case ProtocolConstants.ResponseHeader.ADD_FRIEND_SUCCESS_RESPONSE:
		  friendBussiness.onAddFriendSuccess(dataPackage);
      break;
		case ProtocolConstants.ResponseHeader.ADD_FRIEND_FAIL_USER_DENY_RESPONSE:
		  friendBussiness.onAddFriendFailUserDeny(dataPackage);
      break;
		case ProtocolConstants.ResponseHeader.ADD_FRIEND_FAIL_USER_NOT_EXIST_RESPONSE:
		  friendBussiness.onAddFriendFailUserNotExist(dataPackage);
      break;
		case ProtocolConstants.ResponseHeader.FRIEND_LIST_RESPONSE:
		  friendBussiness.onGetFriendList(dataPackage);
		  break;
		case ProtocolConstants.ResponseHeader.ALREADY_FRIEND_RESPONSE:
		  friendBussiness.onAlreadyFriend(dataPackage);
		  break;
		default:
			System.err.println("Receive unknown message header " + header);
			break;
		}
	}
	
	private static DataPackage getGameActionPackage(DataPackage dataPackage) {
		try {
			dataPackage.setHeader(dataPackage.nextInt()); // Lấy data thứ 2 làm header
		} catch (ArrayIndexOutOfBoundsException ex) {
			System.err.println("ERROR : GameActionHandle : getGameActionPackage : dataPackage has wrong format");
		}
		return dataPackage;
	}
	
	public void onConnectDone() {
		SocketClientUtil.startAuthenToGameServer(Global.loginInfo.getUserName());
	}

	public void onConnectFail() {
	  GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Kết nối đến server game thất bại");
	}
	
	public void onDisconnect() {
		if (!isOnSignOut) {
			Alert.instance().showAlert(this, Alert.OK_TYPE, "Mất kết nối với máy chủ", 96);
		}
		isOnSignOut = false;
	}
	
	public void onAuthenSuccess() {
    // Ngắt kết nối tới DefaultService 
    if (Global.authenClient != null) {
      Global.authenClient.write(new DataPackage(DefaultProtocolConstants.RequestHeader.CLOSE_CONNECTION_REQUEST));
      Global.authenClient.detroy();
      Global.authenClient = null;
    }
    
    // Sang form chọn game
    GameGlobal.nextState(Global.frmChooseGame, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
  }
}
