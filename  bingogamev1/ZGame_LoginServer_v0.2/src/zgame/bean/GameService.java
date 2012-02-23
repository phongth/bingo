package zgame.bean;

import java.util.HashMap;
import java.util.Map;

import zgame.socket.ServerConnection;

public class GameService {
  private String id;
  private String name;
  private String url;
  private int port;
  private int cocurrentUser;
  private int maxUser;
  private ServerConnection server;
  private Map<String, User> users = new HashMap<String, User>();

  public GameService(String id, String name) {
    this.id = id;
    this.name = name;
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

  public GameService setName(String name) {
    this.name = name;
    return this;
  }

  public int getCocurrentUser() {
    return cocurrentUser;
  }

  public GameService setCocurrentUser(int cocurrentUser) {
    this.cocurrentUser = cocurrentUser;
    return this;
  }

  public int getMaxUser() {
    return maxUser;
  }

  public GameService setMaxUser(int maxUser) {
    this.maxUser = maxUser;
    return this;
  }

  public String getId() {
    return id;
  }

  public GameService setUrl(String url) {
    this.url = url;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public GameService setPort(int port) {
    this.port = port;
    return this;
  }

  public int getPort() {
    return port;
  }

  public ServerConnection getServer() {
    return server;
  }

  public GameService setServer(ServerConnection server) {
    this.server = server;
    return this;
  }

  public void onUserJoinIn(User user) {
    users.put(user.getUsername(), user);
  }

  public void onUserGetOut(String username) {
    users.remove(username);
  }

  public Map<String, User> getUserMap() {
    return users;
  }

  @Override
  public String toString() {
    return "GameService [id=" + id + ", name=" + name + ", url=" + url + ", port=" + port + "]";
  }
}
