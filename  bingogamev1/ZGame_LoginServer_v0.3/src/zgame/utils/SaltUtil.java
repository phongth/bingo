package zgame.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SaltUtil {
	public static Map<String, String> saltCache = new HashMap<String, String>();
	
	private static String createSalt() {
		return UUID.randomUUID().toString();
	}
	
	public static String getSaltByUsername(String username) {
		// Lấy ra và remove salt khỏi map
		return saltCache.remove(username);
	}
	
	public static String createAndStoreSaltByUsername(String username) {
		String salt = createSalt();
		saltCache.put(username, salt);
		return salt;
	}
}
