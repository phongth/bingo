package development;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.Image;

public class AvatarCache {
	private static final int MAX_AVATAR_USE = 10;

	private static Hashtable avatarTable = new Hashtable();
	private static Hashtable lastTimeUseAvatar = new Hashtable();

	private static int numberOfAvatar;

	public static void putAvatar(String jid, Image image) {
		if (numberOfAvatar >= MAX_AVATAR_USE) {
			// Tìm và remove avatar lâu chưa xử dụng nhất
			Object oldestKey = "";
			long oldestTime = Long.MAX_VALUE;
			Enumeration keys = avatarTable.keys();
			for (; keys.hasMoreElements();) {
				Object key = keys.nextElement();
				long lastTime = ((Long) lastTimeUseAvatar.get(key)).longValue();
				if (lastTime < oldestTime) {
					oldestTime = lastTime;
					oldestKey = key;
				}
			}
			avatarTable.remove(oldestKey);
			lastTimeUseAvatar.remove(oldestKey);
		}
		avatarTable.put(jid, image);
		lastTimeUseAvatar.put(jid, new Long(System.currentTimeMillis()));
	}

	public static Image getAvatar(String jid) {
		Image image = (Image) avatarTable.get(jid);
		if (image != null) {
			lastTimeUseAvatar.put(jid, new Long(System.currentTimeMillis()));
		}
		return image;
	}

	public static void clearAvatarCache() {
		Enumeration keys = avatarTable.keys();
		for (; keys.hasMoreElements();) {
			avatarTable.remove(keys.nextElement());
		}
		lastTimeUseAvatar = new Hashtable();
		numberOfAvatar = 0;
		System.gc();
	}
}
