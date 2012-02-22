package development.bean;

public class User {
	private String name = "";
	private int money; 
	private String passwordMd5 = "";
	private boolean isReady = false;
	private int avatarId;
	
	public Entity entity;
	
	public User() {
	}
	
	public User(String name) {
		this.name = name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getPasswordMd5() {
		return passwordMd5;
	}

	public void setPasswordMd5(String passwordMd5) {
		this.passwordMd5 = passwordMd5;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
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

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (obj instanceof User) {
			User user = (User) obj;
			if (user.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
