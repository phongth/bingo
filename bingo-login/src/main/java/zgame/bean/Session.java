package zgame.bean;

import java.util.UUID;

public class Session {
  private String id;
  private int userId;
  private String username;
  private GameService currentGameService = null;
  private boolean isOnline = true;
  private long loginStartTime;
  private long lastTimeActive;

  public Session(int userId, String username) {
    this.id = UUID.randomUUID().toString();
    this.userId = userId;
    this.username = username;
    loginStartTime = System.currentTimeMillis();
  }

  public String getId() {
    return id;
  }

  public int getUserId() {
    return userId;
  }

  public String getUsername() {
    return username;
  }

  public GameService getCurrentGameService() {
    return currentGameService;
  }

  public Session setCurrentGameService(GameService currentGameService) {
    this.currentGameService = currentGameService;
    return this;
  }

  public long getLoginTime() {
    return loginStartTime;
  }

  public boolean isOnline() {
    return isOnline;
  }

  public long getLastTimeActive() {
    return lastTimeActive;
  }

  public Session setOnline(boolean isOnline) {
    if (this.isOnline && !isOnline) {
      lastTimeActive = System.currentTimeMillis();
    }
    this.isOnline = isOnline;
    return this;
  }
}
