package development.bean;

public class Friend {
	private String username;
	private boolean isOnline;
	private String locationInfo;

	public String getUsername() {
		return username;
	}

	public Friend setUsername(String username) {
		this.username = username;
		return this;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public Friend setOnline(boolean isOnline) {
		this.isOnline = isOnline;
		return this;
	}

	public String getLocationInfo() {
		return locationInfo;
	}

	public Friend setLocationInfo(String locationInfo) {
		this.locationInfo = locationInfo;
		return this;
	}
}
