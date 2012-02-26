package zgame.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SaltUtil {
  public static Map<String, String> saltCache = new HashMap<String, String>();

  public static String createSalt() {
    return UUID.randomUUID().toString();
  }

  public static String getSaltByUsername(String username) {
    return saltCache.get(username);
  }

  public static String createAndStoreSaltByUsername(String username) {
    String salt = createSalt();
    saltCache.put(username, salt);
    return salt;
  }
}
