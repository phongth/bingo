package development.bussiness;

import development.Constants;
import development.Global;
import development.bean.Friend;
import development.socket.ProtocolConstants;
import development.socket.SocketClientUtil;
import state.Alert;
import state.AlertListener;
import state.GameGlobal;
import state.socket.DataPackage;

public class FriendBussiness implements AlertListener {
	private String addFriendFromUser;

	public void onAddFriendNotify(DataPackage dataPackage) {
		addFriendFromUser = dataPackage.nextString();
		GameGlobal.alert.showAlert(this, Alert.YES_NO_TYPE, new String[] {
				addFriendFromUser + " đề nghị được kết bạn.",
				"Bạn có đồng ý không?" }, 99);
	}

	public void onAddFriendSuccess(DataPackage dataPackage) {
		String toUser = dataPackage.nextString();
		GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Kết bạn với " + toUser
				+ " thành công");
		SocketClientUtil.requestFriendList();
	}

	public void onAddFriendFailUserDeny(DataPackage dataPackage) {
		String toUser = dataPackage.nextString();
		GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {
				"Kết bạn thất bại.", "Người chơi " + toUser,
				" từ chối kết bạn." });
	}

	public void onAddFriendFailUserNotExist(DataPackage dataPackage) {
		String toUser = dataPackage.nextString();
		GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
				new String[] { "Kết bạn thất bại.", "Người chơi " + toUser,
						" không tồn tại." });
	}

	public void onGetFriendList(DataPackage dataPackage) {
		Global.friends.removeAllElements();
		int numberOfFriend = dataPackage.nextInt();
		for (int i = 0; i < numberOfFriend; i++) {
			String username = dataPackage.nextString();
			boolean isOnline = (dataPackage.nextInt() == 1);
			String locationInfo = dataPackage.nextString();

			// Replace gameId to game name
			for (int j = 0; j < Constants.GAME_ID.length; j++) {
				int index = locationInfo.indexOf(Constants.GAME_ID[j]);
				if (index > 0) {
					locationInfo = locationInfo.substring(0, index)
							+ Constants.GAME_NAME[j];
					break;
				}
			}

			Friend friend = new Friend().setUsername(username).setOnline(
					isOnline).setLocationInfo(locationInfo);
			Global.friends.addElement(friend);
		}
	}

	public void onAlreadyFriend(DataPackage dataPackage) {
		String toUser = dataPackage.nextString();
		GameGlobal.alert.showAlert(this, Alert.OK_TYPE, toUser + " đã là bạn.");
	}

	public void alertEventPerform(int alertType, int eventType, int alertId) {
		if (alertId == 99) { // Alert from onAddFriendNotify : {fromUser +
								// " đề nghị được kết bạn.",
								// "Bạn có đồng ý không?"}

			// If agree
			if (eventType == Alert.YES_BUTTON) {
				DataPackage agreeRequestDataPackage = new DataPackage(
						ProtocolConstants.RequestHeader.ADD_FRIEND_AGREE_REQUEST);
				agreeRequestDataPackage.putString(addFriendFromUser);
				Global.gameActionClient.write(agreeRequestDataPackage);
				return;
			}

			// if deny
			if (eventType == Alert.NO_BUTTON) {
				DataPackage denyRequestDataPackage = new DataPackage(
						ProtocolConstants.RequestHeader.ADD_FRIEND_DENY_REQUEST);
				denyRequestDataPackage.putString(addFriendFromUser);
				Global.gameActionClient.write(denyRequestDataPackage);
				return;
			}
		}
	}
}
