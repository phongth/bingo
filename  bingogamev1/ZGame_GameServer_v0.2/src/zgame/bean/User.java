package zgame.bean;

import zgame.socket.server.Server;

public class User {
	private String name;
	private int money;
	private boolean isReady;
	private int avatarId;
	
	public Entity entity;
	public Server server;

	public User(String name, Server server) {
		this.name = name;
		this.server = server;
	}

	public String getName() {
		return name;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}
	
	public int getAvatarId() {
		return avatarId;
	}

	public void setAvatarId(int avatarId) {
		this.avatarId = avatarId;
	}

	@Override
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
