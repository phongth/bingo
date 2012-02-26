package development.bean;

public class LoginInfo {
	private boolean isSaveUserNameAndPassword = false;
	private boolean isAutoLogin = false;
	private String userName = "";
	private String password = "";

	public boolean isSaveUserNameAndPassword() {
		return isSaveUserNameAndPassword;
	}

	public void setSaveUserNameAndPassword(boolean isSaveUserNameAndPassword) {
		this.isSaveUserNameAndPassword = isSaveUserNameAndPassword;
	}

	public boolean isAutoLogin() {
		return isAutoLogin;
	}

	public void setAutoLogin(boolean isAutoLogin) {
		this.isAutoLogin = isAutoLogin;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
