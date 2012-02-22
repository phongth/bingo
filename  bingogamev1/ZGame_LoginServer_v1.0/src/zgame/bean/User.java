package zgame.bean;

public class User {
	private int userId = -1;
	private String username = null;
	private String md5Pass = null;
	private int avatarId;
	private int money;
	private int providerId;
	private char isBlock = '0';

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMd5Pass() {
		return md5Pass;
	}

	public void setMd5Pass(String md5Pass) {
		this.md5Pass = md5Pass;
	}

	public char getIsBlock() {
		return isBlock;
	}

	public void setIsBlock(char isBlock) {
		this.isBlock = isBlock;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public int getAvatarId() {
		return avatarId;
	}

	public void setAvatarId(int avatarId) {
		this.avatarId = avatarId;
	}

  public int getProviderId() {
    return providerId;
  }

  public void setProviderId(int providerId) {
    this.providerId = providerId;
  }
}
