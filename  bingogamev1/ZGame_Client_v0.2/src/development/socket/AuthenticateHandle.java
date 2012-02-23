package development.socket;

import state.Alert;
import state.AlertListener;
import state.GameGlobal;
import state.socket.DataPackage;
import state.socket.DataReceiveListener;
import development.Constants;
import development.Global;
import development.Rms;
import development.bean.Provider;
import development.bussiness.AuthenticateBussiness;

public class AuthenticateHandle implements DataReceiveListener, AlertListener {
	public static final int START_AUTHENTICATE_ACTION = 1;
	public static final int SIGN_OUT_ACTION = 2;
	public static final int GAME_SERVICE_LIST = 3;

	public int waitingAction = -1;
	private AuthenticateBussiness authenticateBussiness;

	public AuthenticateHandle() {
		authenticateBussiness = new AuthenticateBussiness();
	}

	public void onConnectDone() {
		switch (waitingAction) {
		case START_AUTHENTICATE_ACTION:
			SocketClientUtil.startAuthenticateRequest(Global.loginInfo
					.getUserName());
			break;
		case SIGN_OUT_ACTION:
			SocketClientUtil.signOut();
			break;
		case GAME_SERVICE_LIST:
			SocketClientUtil.gameServiceListRequest();
			break;
		default:
			if (Global.loginInfo.isAutoLogin()) {
				SocketClientUtil.login(false);
			} else {
				Global.isAutoLoginDone = true;
				SocketClientUtil.privateLogin();
			}

			SocketClientUtil.checkForUpdate(Constants.VERSION, Global.provider
					.getId());
			SocketClientUtil.checkForDownloadResource();

			Global.frmLogo.message = "Đang kiểm tra phiên bản...";
			break;
		}
		waitingAction = -1;
	}

	public void onConnectFail() {
		GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
				"Kết nối đến server thất bại", 99);
	}

	public void onDisconnect() {
	}

	public void alertEventPerform(int alertType, int eventType, int alertId) {
		if (alertId == 99) {
			GameGlobal.nextState(Global.frmLogin, null);
		}
	}

	public void onRecieveData(DataPackage dataPackage) {
		int header = dataPackage.getHeader();
		switch (header) {

		/** Check for update */
		case ProtocolConstants.ResponseHeader.UPDATE_PROVIDER_RESPONSE:
			Global.provider = new Provider(dataPackage.nextInt());
			Rms.saveProvider(Global.provider);
			break;
		case ProtocolConstants.ResponseHeader.UPDATE_CLIENT_RESPONSE:
			authenticateBussiness.onUpdateClienResponse(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.NO_UPDATE_RESPONSE:
			authenticateBussiness.onNoUpdateResponse(dataPackage);
			break;

		/** Download resource */
		case ProtocolConstants.ResponseHeader.RESOURCE_RESPONSE:
			authenticateBussiness.onReceiveResource(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.TOTAL_DOWNLOAD_IMAGE_RESPONSE:
			GameGlobal.totalDownloadResource = dataPackage.nextInt();
			break;
		case ProtocolConstants.ResponseHeader.DOWNLOAD_RESOURCE_DONE_RESPONSE:
			authenticateBussiness.onDownloadResourceDone(dataPackage);
			break;

		/** Authenticate */
		case ProtocolConstants.ResponseHeader.SALT_REPONSE:
			String salt = dataPackage.nextString();
			SocketClientUtil.authenticateByPassword(Global.currentUser
					.getName(), Global.currentUser.getPasswordMd5(), salt);
			break;
		case ProtocolConstants.ResponseHeader.AUTHENTICATE_SUCCESS_RESPONSE:
			authenticateBussiness.onAuthenticateSuccess(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.AUTHENTICATE_FAIL_RESPONSE:
			authenticateBussiness.onAuthenticateFail(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.REGISTER_SUCCESS_RESPONSE:
			authenticateBussiness.onRegisterSuccess(dataPackage);
			break;
		case ProtocolConstants.ResponseHeader.REGISTER_FAIL_RESPONSE:
			authenticateBussiness.onRegisterFail(dataPackage);
			break;

		/** Update GameService list */
		case ProtocolConstants.ResponseHeader.GAME_SERVICE_LIST_RESPONSE:
			authenticateBussiness.onGameServiceList(dataPackage);
			break;

		default:
			System.err.println("Receive unknown message header " + header);
			break;
		}
	}
}
