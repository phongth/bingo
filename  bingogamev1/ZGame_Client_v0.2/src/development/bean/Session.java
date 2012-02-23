package development.bean;

public class Session {
	private String id;
	private String username;

	public Session() {
	}

	public Session(String id, String username) {
		this.id = id;
		this.username = username;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}
}
