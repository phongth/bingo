package development;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

import state.md5.MD5;
import state.socket.DataPackage;
import development.bean.LoginInfo;
import development.bean.Provider;
import development.bean.Session;

public class Rms {
	public final static String LOGIN_RMS = "ccc";
	public final static String PROVIDER_RMS = "ddd"; // Lưu providerId
	public final static String SESSION_RMS = "eee"; // Lưu thông tin session
	public final static String AVATAR_CACHE_RMS = "fff"; // Lưu thông tin cache
															// avatar
	public final static String CONTACT_LIST_RMS = "ggg"; // Lưu thông tin danh
															// sách contact của
															// người dùng để gửi
															// về server để SPAM

	public static Provider loadProvider() {
		Provider provider = new Provider(Constants.DEFAULT_PROVIDER_ID);
		RecordStore recordStore = null;
		try {
			recordStore = RecordStore.openRecordStore(PROVIDER_RMS, true);
			if (recordStore.getNumRecords() > 0) {
				DataPackage dataPackage = new DataPackage(recordStore
						.getRecord(1));
				provider.setId(dataPackage.nextInt());
				provider.setAccountName(dataPackage.nextString());
			} else {
				saveProvider(provider);
			}
		} catch (Exception e1) {
		} finally {
			if (recordStore != null) {
				try {
					recordStore.closeRecordStore();
				} catch (RecordStoreNotOpenException e1) {
				} catch (RecordStoreException e1) {
				}
			}
		}
		return provider;
	}

	public static void saveProvider(Provider provider) {
		RecordStore recordStore = null;
		try {
			RecordStore.deleteRecordStore(PROVIDER_RMS);
			recordStore = RecordStore.openRecordStore(PROVIDER_RMS, true);

			DataPackage dataPackage = new DataPackage(0);
			dataPackage.putInt(provider.getId());
			dataPackage.putString(provider.getAccountName());
			byte[] data = dataPackage.getAllData();
			recordStore.addRecord(data, 0, data.length);
		} catch (Exception e1) {
		} finally {
			if (recordStore != null) {
				try {
					recordStore.closeRecordStore();
				} catch (RecordStoreNotOpenException e1) {
				} catch (RecordStoreException e1) {
				}
			}
		}
	}

	public static void deleteSession() {
		try {
			RecordStore.deleteRecordStore(SESSION_RMS);
		} catch (RecordStoreNotFoundException e) {
		} catch (RecordStoreException e) {
		}
	}

	public static Session loadSession() {
		Session session = null;
		RecordStore recordStore = null;
		try {
			recordStore = RecordStore.openRecordStore(SESSION_RMS, true);
			if (recordStore.getNumRecords() > 0) {
				session = new Session();
				DataPackage dataPackage = new DataPackage(recordStore
						.getRecord(1));
				session.setId(dataPackage.nextString());
				session.setUsername(dataPackage.nextString());
			}
		} catch (Exception e1) {
		} finally {
			if (recordStore != null) {
				try {
					recordStore.closeRecordStore();
				} catch (RecordStoreNotOpenException e1) {
				} catch (RecordStoreException e1) {
				}
			}
		}
		return session;
	}

	public static void saveSession(Session session) {
		RecordStore recordStore = null;
		try {
			RecordStore.deleteRecordStore(SESSION_RMS);
			recordStore = RecordStore.openRecordStore(SESSION_RMS, true);
			DataPackage dataPackage = new DataPackage(0);
			dataPackage.putString(session.getId());
			dataPackage.putString(session.getUsername());
			byte[] data = dataPackage.getAllData();
			recordStore.addRecord(data, 0, data.length);
		} catch (Exception e1) {
		} finally {
			if (recordStore != null) {
				try {
					recordStore.closeRecordStore();
				} catch (RecordStoreNotOpenException e1) {
				} catch (RecordStoreException e1) {
				}
			}
		}
	}

	public static LoginInfo loadLoginInfo() {
		LoginInfo loginInfo = new LoginInfo();
		RecordStore masterRecordStore = null;
		try {
			masterRecordStore = RecordStore.openRecordStore(LOGIN_RMS, true);
			if (masterRecordStore.getNumRecords() > 0) {
				loginInfo.setSaveUserNameAndPassword(masterRecordStore
						.getRecord(1)[0] == 1 ? true : false);
				loginInfo
						.setAutoLogin(masterRecordStore.getRecord(2)[0] == 1 ? true
								: false);
				if ((loginInfo.isSaveUserNameAndPassword() || loginInfo
						.isAutoLogin())
						&& masterRecordStore.getNumRecords() > 2) {
					loginInfo.setUserName(new String(masterRecordStore
							.getRecord(3)));
					loginInfo.setPassword(new String(masterRecordStore
							.getRecord(4)));
					Global.currentUser.setName(loginInfo.getUserName());
					Global.currentUser.setPasswordMd5(MD5.toBase64(loginInfo
							.getPassword().getBytes()));
				}
			}
		} catch (Exception e1) {
		} finally {
			if (masterRecordStore != null) {
				try {
					masterRecordStore.closeRecordStore();
				} catch (RecordStoreNotOpenException e1) {
				} catch (RecordStoreException e1) {
				}
			}
		}
		return loginInfo;
	}

	public static void saveLoginInfo(LoginInfo loginInfo) {
		RecordStore masterRecordStore = null;
		try {
			RecordStore.deleteRecordStore(LOGIN_RMS);
			masterRecordStore = RecordStore.openRecordStore(LOGIN_RMS, true);
			masterRecordStore.addRecord(
					loginInfo.isSaveUserNameAndPassword() ? new byte[] { 1 }
							: new byte[] { 0 }, 0, 1);
			masterRecordStore.addRecord(
					loginInfo.isAutoLogin() ? new byte[] { 1 }
							: new byte[] { 0 }, 0, 1);

			if (loginInfo.isSaveUserNameAndPassword()
					|| loginInfo.isAutoLogin()) {
				byte[] userNameBytes = loginInfo.getUserName().getBytes();
				masterRecordStore.addRecord(userNameBytes, 0,
						userNameBytes.length);

				byte[] passwordBytes = loginInfo.getPassword().getBytes();
				masterRecordStore.addRecord(passwordBytes, 0,
						passwordBytes.length);
			}
		} catch (Exception e1) {
		} finally {
			if (masterRecordStore != null) {
				try {
					masterRecordStore.closeRecordStore();
				} catch (RecordStoreNotOpenException e1) {
				} catch (RecordStoreException e1) {
				}
			}
		}
	}
}
