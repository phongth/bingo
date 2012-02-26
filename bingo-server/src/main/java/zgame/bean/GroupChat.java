package zgame.bean;

import java.util.HashMap;

public class GroupChat extends HashMap<String, User> {
  private static final long serialVersionUID = 1L;
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
