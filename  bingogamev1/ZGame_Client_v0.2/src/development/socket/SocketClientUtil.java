package development.socket;

import java.util.Enumeration;
import java.util.Vector;

import state.Alert;
import state.AlertListener;
import state.GameGlobal;
import state.Transformer;
import state.md5.MD5;
import state.socket.ClientConnection;
import state.socket.DataPackage;
import development.Constants;
import development.Global;
import development.Rms;

public class SocketClientUtil implements AlertListener {
	private static final int TIME_OUT = 10000;

	private static SocketClientUtil instance = new SocketClientUtil();

	public void alertEventPerform(int alertType, int eventType, int alertId) {
		switch (alertId) {
		case 99:
			Alert.instance().showAlert(instance, Alert.OK_TYPE,
					"Kết nối thất bại");
			break;
		case 98:
			Alert.instance().showAlert(instance, Alert.OK_TYPE,
					"Vào phòng thất bại");
			break;
		case 97:
			Alert.instance().showAlert(instance, Alert.OK_TYPE,
					"Vào bàn thất bại");
			break;
		case 96:
			Alert.instance().showAlert(instance, Alert.OK_TYPE,
					"Bắt đầu game thất bại");
			break;
		}
	}

	public static void connectToServerForAuthenticate(
			AuthenticateHandle authenticateHandle) {
		if (Global.authenClient != null) {
			Global.authenClient.detroy();
		}
		Global.authenClient = new ClientConnection(Constants.LOGIN_SERVER,
				Constants.PORT, authenticateHandle,
				Global.HEART_BREATH_SEQUENCE_TIME);
	}

	public static void connectToGameServer(String url, int port,
			GameActionHandle gameActionHandle) {
		if (Global.gameActionClient != null) {
			Global.gameActionClient.detroy();
		}
		Alert.instance().showAlert(instance, Alert.LOADING_WITH_NO_BUTTON_TYPE,
				"Đang kết nối", 99).setAlertTimeOut(TIME_OUT);
		Global.gameActionClient = new ClientConnection(url, port, gameActionHandle,
				Global.HEART_BREATH_SEQUENCE_TIME);
	}

	public static void checkForUpdate(String version, int providerId) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.CHECK_VERSION_REQUEST);
		dataPackage.putString(Constants.VERSION);
		dataPackage.putInt(Global.provider.getId());
		Global.authenClient.write(dataPackage);
	}

	public static void checkForDownloadResource() {
		Vector downloadedReources = new Vector();
		Enumeration images = GameGlobal.imageLocationTable.keys();
		for (; images.hasMoreElements();) {
			downloadedReources.addElement(images.nextElement());
		}

		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.DOWNLOADED_RESOURCE_LIST_REQUEST);
		dataPackage.putInt(downloadedReources.size());
		for (int i = 0; i < downloadedReources.size(); i++) {
			dataPackage.putString(String.valueOf(downloadedReources
					.elementAt(i)));
		}
		Global.authenClient.write(dataPackage);
	}

	/**
	 * Tiến hành tự động login ngầm mà người sử dụng ko biết
	 */
	public static void privateLogin() {
		Global.currentUser.setName(Global.loginInfo.getUserName());
		Global.currentUser.setPasswordMd5(MD5.toBase64(Global.loginInfo
				.getPassword().getBytes()));
		SocketClientUtil.startAuthenticateRequest(Global.currentUser.getName());
	}

	public static void login(boolean isShowLoading) {
		Global.currentUser.setName(Global.loginInfo.getUserName());
		Global.currentUser.setPasswordMd5(MD5.toBase64(Global.loginInfo
				.getPassword().getBytes()));

		// Nếu user đã được xác nhận rồi thì chuyển tiếp sang form tiếp theo
		if (Global.loginInfo.getUserName().equals(Global.validedUsername)) {
			GameGlobal.nextState(Global.frmGameService, null,
					Transformer.TRANSFORM_WITH_LOADING_FORM);
		} else { // Nếu là user chưa được xác nhận thì tiến hành xác nhận user
			Global.waitingToValidUsername = Global.loginInfo.getUserName();
			if (isShowLoading) {
				GameGlobal.alert.showAlert(instance,
						Alert.LOADING_WITH_NO_BUTTON_TYPE, "Đang đăng nhập...",
						99).setAlertTimeOut(TIME_OUT);
			}
			SocketClientUtil.startAuthenticateRequest(Global.currentUser
					.getName());
		}
	}

	public static void signOut() {
		if ((Global.authenClient == null) || !Global.authenClient.isRunning) {
			Global.authenticateHandle.waitingAction = AuthenticateHandle.SIGN_OUT_ACTION;
			connectToServerForAuthenticate(Global.authenticateHandle);
			return;
		}

		if (Global.session != null) {
			DataPackage dataPackage = new DataPackage(
					ProtocolConstants.RequestHeader.SIGN_OUT_REQUEST);
			dataPackage.putString(Global.session.getUsername());
			dataPackage.putString(MD5.toBase64(Global.session.getId()
					.getBytes()));
			Global.authenClient.write(dataPackage);
			Global.session = null;
			Rms.deleteSession();
		}
	}

	public static void startAuthenticateRequest(String username) {
		if ((Global.authenClient == null) || !Global.authenClient.isRunning) {
			Global.authenticateHandle.waitingAction = AuthenticateHandle.START_AUTHENTICATE_ACTION;
			connectToServerForAuthenticate(Global.authenticateHandle);
			return;
		}
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.AUTHENTICATE_REQUEST);
		dataPackage.putString(username);
		Global.authenClient.write(dataPackage);
	}

	public static void authenticateByPassword(String username,
			String passwordMd5, String salt) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.AUTHENTICATE_WITH_PASSWORD_REQUEST);
		dataPackage.putString(username);
		dataPackage.putString(MD5.toBase64((username + passwordMd5 + salt)
				.getBytes()));
		Global.authenClient.write(dataPackage);
	}

	public static void authenToGameServerBySessionId(String username,
			String sessionId, String salt) {
		String encodedData = MD5.toBase64((sessionId + salt).getBytes());
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.AUTHENTICATE_WITH_SESSION_ID_REQUEST);
		dataPackage.putString(username);
		dataPackage.putString(encodedData);
		Global.gameActionClient.write(dataPackage);
	}

	public static void register(String username, String password) {
		GameGlobal.alert.showAlert(instance, Alert.LOADING_WITH_NO_BUTTON_TYPE,
				"Đang đăng ký...", 99).setAlertTimeOut(TIME_OUT);

		String passMd5 = MD5.toBase64(password.getBytes());

		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.REGISTER_REQUEST);
		dataPackage.putString(username);
		dataPackage.putString(passMd5);
		dataPackage.putInt(Global.provider.getId());
		Global.authenClient.write(dataPackage);
	}

	public static void changePassword(String username, String oldPassword,
			String newPassword) {
		GameGlobal.alert.showAlert(instance, Alert.LOADING_WITH_NO_BUTTON_TYPE,
				"Đang đổi mật khẩu...", 99).setAlertTimeOut(TIME_OUT);

		String oldPassMd5 = MD5.toBase64(oldPassword.getBytes());
		String newPassMd5 = MD5.toBase64(newPassword.getBytes());

		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.CHANGE_PASSWORD_REQUEST);
		dataPackage.putString(username);
		dataPackage.putString(oldPassMd5);
		dataPackage.putString(newPassMd5);
		Global.authenClient.write(dataPackage);
	}

	public static void startAuthenToGameServer(String username) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.AUTHENTICATE_REQUEST);
		dataPackage.putString(username);
		Global.gameActionClient.write(dataPackage);
	}

	public static void gameServiceListRequest() {
		if ((Global.authenClient == null) || !Global.authenClient.isRunning) {
			Global.authenticateHandle.waitingAction = AuthenticateHandle.GAME_SERVICE_LIST;
			connectToServerForAuthenticate(Global.authenticateHandle);
			return;
		}
		Global.authenClient.write(new DataPackage(
				ProtocolConstants.RequestHeader.GAME_SERVICE_LIST_REQUEST));
	}

	// *************** GAME - ROOM - TABLE FUNCTION ***********************
	public static void joinGameRequest(String gameId) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.JOIN_GAME_REQUEST);
		dataPackage.putString(gameId);
		Global.gameActionClient.write(dataPackage);
	}

	public static void joinRoomRequest(String roomId) {
		Alert.instance().showAlert(instance, Alert.LOADING_WITH_NO_BUTTON_TYPE,
				"Đang vào phòng...", 98).setAlertTimeOut(TIME_OUT);
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.JOIN_ROOM_REQUEST);
		dataPackage.putString(roomId);
		Global.gameActionClient.write(dataPackage);
	}

	public static void joinTableRequest(String tableId, String password) {
		Alert.instance().showAlert(instance, Alert.LOADING_WITH_NO_BUTTON_TYPE,
				"Đang vào bàn...", 97).setAlertTimeOut(TIME_OUT);
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.JOIN_TABLE_REQUEST);
		dataPackage.putString(tableId);
		dataPackage.putString(password == null ? "" : password);
		Global.gameActionClient.write(dataPackage);
	}

	public static void leaveRoomRequest() {
		Global.gameActionClient.write(new DataPackage(
				ProtocolConstants.RequestHeader.LEAVE_ROOM_REQUEST));
	}

	public static void leaveTableRequest() {
		Global.gameActionClient.write(new DataPackage(
				ProtocolConstants.RequestHeader.LEAVE_TABLE_REQUEST));
	}

	public static void getRoomList(String gameId) {
		System.out.println("Request room list");
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.GET_ROOM_LIST_REQUEST);
		dataPackage.putString(gameId);
		Global.gameActionClient.write(dataPackage);
	}

	public static void getTableList(String roomId) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.GET_TABLE_LIST_REQUEST);
		dataPackage.putString(roomId);
		Global.gameActionClient.write(dataPackage);
	}

	public static void sendReady(boolean isReady) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.SEND_READY_REQUEST);
		dataPackage.putInt(isReady ? 1 : 0);
		Global.gameActionClient.write(dataPackage);
	}

	public static void configTable(int bid, String password) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.CONFIG_TABLE_REQUEST);
		dataPackage.putInt(bid);
		dataPackage.putString(password);
		Global.gameActionClient.write(dataPackage);
	}

	public static void startGame() {
		Alert.instance().showAlert(instance, Alert.LOADING_WITH_NO_BUTTON_TYPE,
				"Đang bắt đầu game...", 96).setAlertTimeOut(TIME_OUT);
		Global.gameActionClient.write(new DataPackage(
				ProtocolConstants.RequestHeader.START_GAME_REQUEST));
	}

	public static void getUserWantToPlayList() {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.LIST_USER_WANT_TO_PLAY_REQUEST);
		dataPackage.putInt(0); // TODO: Implement phân trang
		Global.gameActionClient.write(dataPackage);
	}

	// ********************************************************

	// *************** FRIEND FUNCTION ***********************
	public static void addFriendRequest(String toUsername) {
		if (toUsername == null || toUsername.equals("")) {
			GameGlobal.alert.showAlert(instance, Alert.OK_TYPE,
					"Người chơi không tồn tại");
			return;
		}

		if (toUsername.equals(Global.currentUser.getName())) {
			GameGlobal.alert.showAlert(instance, Alert.OK_TYPE,
					"Người chơi không tồn tại");
			return;
		}

		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.ADD_FRIEND_REQUEST);
		dataPackage.putString(toUsername);
		Global.gameActionClient.write(dataPackage);
	}

	/**
	 * @param fromUsername
	 *            Người đã yêu cầu addFriend
	 */
	public static void addFriendAgree(String fromUsername) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.ADD_FRIEND_AGREE_REQUEST);
		dataPackage.putString(fromUsername);
		Global.gameActionClient.write(dataPackage);
	}

	/**
	 * @param fromUsername
	 *            Người đã yêu cầu addFriend
	 */
	public static void addFriendDeny(String fromUsername) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.ADD_FRIEND_DENY_REQUEST);
		dataPackage.putString(fromUsername);
		Global.gameActionClient.write(dataPackage);
	}

	public static void requestFriendList() {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.GET_FRIEND_LIST_REQUEST);
		Global.gameActionClient.write(dataPackage);
	}

	// ********************************************************

	// *************** CHAT FUNCTION ***********************
	public static void sendMessage(String toId, String message) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.SEND_MESSAGE_REQUEST);
		dataPackage.putString(toId);
		dataPackage.putString(message);
		Global.gameActionClient.write(dataPackage);
	}

	public static void inviteUserToGroup(String groupId, String toUser) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.INVITE_USER_TO_GROUP_REQUEST);
		dataPackage.putString(toUser);
		dataPackage.putString(groupId);
		Global.gameActionClient.write(dataPackage);
	}

	public static void acceptJoinToGroup(String fromUser, String groupId) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.ACCEPT_JOIN_TO_GROUP_REQUEST);
		dataPackage.putString(fromUser);
		dataPackage.putString(groupId);
		Global.gameActionClient.write(dataPackage);
	}

	public static void denyJoinToGroup(String fromUser, String groupId) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.DENY_JOIN_TO_GROUP_REQUEST);
		dataPackage.putString(fromUser);
		dataPackage.putString(groupId);
		Global.gameActionClient.write(dataPackage);
	}
	// ********************************************************
}
