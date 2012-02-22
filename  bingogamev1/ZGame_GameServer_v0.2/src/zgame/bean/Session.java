package zgame.bean;

import java.util.UUID;

public class Session {
	private String id;
	private long timeOut;
	private int userId;
	private String username;
	private String currentGameService = null;
	private boolean isOnline = true;
	
	public Session(String id) {
		this.id = id;
	}
	
	public Session(long timeOut, int userId, String username) {
		this.id = UUID.randomUUID().toString();
		this.timeOut = timeOut;
		this.userId = userId;
		this.username = username;
	}

	public String getId() {
		return id;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public int getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public String getCurrentGameService() {
		return currentGameService;
	}

	public void setCurrentGameService(String currentGameService) {
		this.currentGameService = currentGameService;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
}
