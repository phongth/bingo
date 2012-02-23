package development.bean;

public class GameService {
	private String id;
	private String name;
	private String url;
	private int port;
	private int cocurrentUser;
	private int maxUser;
	private boolean isValidate = false;

	public GameService() {
	}

	public GameService(String id, String name, String url, int port, int maxUser) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.port = port;
		this.maxUser = maxUser;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCocurrentUser() {
		return cocurrentUser;
	}

	public void setCocurrentUser(int cocurrentUser) {
		this.cocurrentUser = cocurrentUser;
	}

	public int getMaxUser() {
		return maxUser;
	}

	public void setMaxUser(int maxUser) {
		this.maxUser = maxUser;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public boolean isValidate() {
		return isValidate;
	}

	public void setValidate(boolean isValidate) {
		this.isValidate = isValidate;
	}

	public String toString() {
		return "GameService [id=" + id + ", name=" + name + ", url=" + url
				+ ", port=" + port + "]";
	}
}
