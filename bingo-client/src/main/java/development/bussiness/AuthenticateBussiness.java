package development.bussiness;

import javax.microedition.io.ConnectionNotFoundException;

import state.Alert;
import state.AlertListener;
import state.GameGlobal;
import state.ResourceRms;
import state.Transformer;
import state.socket.DataPackage;
import development.Constants;
import development.Global;
import development.Rms;
import development.bean.GameService;
import development.bean.Session;
import development.socket.ProtocolConstants;

public class AuthenticateBussiness implements AlertListener {
	private String updateUrl;

	public void alertEventPerform(int alertType, int eventType, int alertId) {
		if (alertId == 98) {
			// Tự động cập nhật phiên bản
			try {
				GameGlobal.getMidlet().platformRequest(updateUrl);
			} catch (ConnectionNotFoundException e) {
			}
			GameGlobal.getMidlet().notifyDestroyed();
		}
	}

	public void onAuthenticateSuccess(DataPackage dataPackage) {
		String username = dataPackage.nextString();
		String sessionId = dataPackage.nextString();
		Global.HEART_BREATH_SEQUENCE_TIME = dataPackage.nextInt();
		Global.authenClient
				.setHeartBreathSequenceTime(Global.HEART_BREATH_SEQUENCE_TIME);

		int numberOfGameServices = dataPackage.nextInt();
		Global.gameServices.removeAllElements();

		for (int i = 0; i < numberOfGameServices; i++) {
			GameService gameService = new GameService();
			gameService.setId(dataPackage.nextString());
			gameService.setName(dataPackage.nextString());
			gameService.setUrl(dataPackage.nextString());
			gameService.setPort(dataPackage.nextInt());
			gameService.setCocurrentUser(dataPackage.nextInt());
			gameService.setMaxUser(dataPackage.nextInt());
			Global.gameServices.addElement(gameService);
		}

		Global.session = new Session(sessionId, Global.loginInfo.getUserName());
		Rms.saveLoginInfo(Global.loginInfo);

		// Nếu là tự động đăng nhập thì lưu lại là đã valid user này
		if (!username.equals(Global.waitingToValidUsername)) {
			Global.validedUsername = username;
		} else { // Nếu là do user đăng nhập thì chuyển sang form tiếp theo
			Global.waitingToValidUsername = null;
			GameGlobal.nextState(Global.frmGameService, null,
					Transformer.TRANSFORM_WITH_LOADING_FORM);
		}
	}

	public void onAuthenticateFail(DataPackage dataPackage) {
		String username1 = dataPackage.nextString();

		// Nếu là xác nhận user ngầm thì không làm gì cả
		if (!username1.equals(Global.waitingToValidUsername)) {
			return;
		}

		int errorId = dataPackage.nextInt();
		String errorMessage = "";
		switch (errorId) {
		case ProtocolConstants.ErrorCode.ALREADY_ONLINE_ERROR:
			errorMessage = "Tài khoản đang sử dụng";
			break;
		case ProtocolConstants.ErrorCode.USER_BLOCK_ERROR:
			errorMessage = "Tài khoản đã bị khóa";
			break;
		}

		if (!Global.isAutoLoginDone) {
			Global.frmLogo.message = "Tự động đăng nhập thất bại";
			Global.frmLogo.message1 = errorMessage;
			GameGlobal.nextState(Global.frmLogin, null);

			Global.isAutoLoginDone = true;
			if (isReadyToGoToLoginForm()) {
				gotoLoginForm();
			}
		} else {
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {
					"Đăng nhập thất bại", errorMessage });
		}

		Global.waitingToValidUsername = null;
		Global.session = null;
		Rms.deleteSession();
	}

	private boolean isReadyToGoToLoginForm() {
		return (Global.isAutoLoginDone && Global.isCheckForDownloadResourceDone && Global.isCheckForUpdateDone);
	}

	private void gotoLoginForm() {
		if (!Global.loginInfo.isAutoLogin()) {
			GameGlobal.nextState(Global.frmLogin, null);
		}
	}

	public void onGameServiceList(DataPackage dataPackage) {
		int size = dataPackage.nextInt();
		for (int i = 0; i < size; i++) {
			String id = dataPackage.nextString();
			int concurrentUser = dataPackage.nextInt();

			for (int j = 0; j < Global.gameServices.size(); j++) {
				GameService gameService = (GameService) Global.gameServices
						.elementAt(j);
				if (gameService.getId().equals(id)) {
					gameService.setCocurrentUser(concurrentUser);
					break;
				}
			}
		}
	}

	public void onDownloadResourceDone(DataPackage dataPackage) {
		ResourceRms.saveMasterRecord(GameGlobal.imageLocationTable);
		Global.frmLogo.message = "Tải dữ liệu kết thúc";
		Global.frmLogo.message1 = "";

		Global.isCheckForDownloadResourceDone = true;
		if (isReadyToGoToLoginForm()) {
			gotoLoginForm();
		}
	}

	public void onNoUpdateResponse(DataPackage dataPackage) {
		Global.frmLogo.message = "Đang kiểm tra dữ liệu...";
		Global.isCheckForUpdateDone = true;
		if (isReadyToGoToLoginForm()) {
			gotoLoginForm();
		}
	}

	public void onUpdateClienResponse(DataPackage dataPackage) {
		updateUrl = dataPackage.nextString();
		GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
				"Chương trình tự động cập nhật phiên bản", 98);
	}

	public void onReceiveResource(DataPackage dataPackage) {
		int size = dataPackage.nextInt();
		for (int i = 0; i < size; i++) {
			String fileName = dataPackage.nextString();
			byte[] data = dataPackage.nextByteArray();
			GameGlobal.imageLocationTable.put(fileName, new Integer(ResourceRms
					.saveImageData(data)));
			GameGlobal.downloadedCount++;
			Global.frmLogo.message = "Đang tải dữ liệu...";
			Global.frmLogo.message1 = "Đang tải " + GameGlobal.downloadedCount
					+ " / " + GameGlobal.totalDownloadResource;
		}
	}

	public void onRegisterSuccess(DataPackage dataPackage) {
		GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Đăng ký thành công");
	}

	public void onRegisterFail(DataPackage dataPackage) {
		dataPackage.nextString(); // get username (not use)
		int errorCode = dataPackage.nextInt();
		switch (errorCode) {
		case ProtocolConstants.ErrorCode.USER_NAME_EXIST_ERROR:
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
					"Tên tài khoản đã được sử dụng");
			break;
		case ProtocolConstants.ErrorCode.USER_NAME_TOO_LONG_ERROR:
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {
					"Tên đăng nhập phải",
					"ngắn hơn " + Constants.USER_NAME_MAX_LEN + " ký tự" });
			break;
		case ProtocolConstants.ErrorCode.USER_NAME_TOO_SHORT_ERROR:
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {
					"Tên đăng nhập phải",
					"dài hơn " + Constants.USER_NAME_MIN_LEN + " ký tự" });
			break;
		case ProtocolConstants.ErrorCode.USER_NAME_NOT_VALID:
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
					"Tên tài khoản không hợp lệ");
			break;
		default:
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
					"Tạo tài khoản thất bại");
			break;
		}
	}

	public void onChangePasswordSuccess(DataPackage dataPackage) {
		GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
				"Đổi mật khẩu thành công");
	}

	public void onChangePasswordFail(DataPackage dataPackage) {
		dataPackage.nextString(); // get username (not use)
		int errorCode = dataPackage.nextInt();
		switch (errorCode) {
		case ProtocolConstants.ErrorCode.PASSWORD_NOT_CORRECT_ERROR:
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
					"Mật khẩu cũ không đúng");
			break;
		case ProtocolConstants.ErrorCode.USER_NOT_EXIST_ERROR:
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
					"Tài khoản không tồn tại");
			break;
		default:
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
					"Đổi mật khẩu thất bại");
			break;
		}
	}
}
