package state.font;

public class FontManager {
	public static double MIDP_VERSION = 1;
	static {
		String profiles = System.getProperty("microedition.profiles");
		int index = profiles.indexOf("MIDP-");
		if (index == 0) {
			MIDP_VERSION = Double.parseDouble(profiles.substring(5));
		}
	}
	
	private static final String FONT_8_FILE = "/vn8.fnt";
	private static final String FONT_11_FILE = "/vn11.fnt";
	private static final String FONT_13_FILE = "/vn13.fnt";

	public static final int FONT_SIZE_8 = 0;
	public static final int FONT_SIZE_11 = 1;
	public static final int FONT_SIZE_13 = 2;

	private static ImageText text8;
	private static ImageText text11;
	private static ImageText text13;

	private static int currentCacheSize = ImageTextForMidp2.DEFAULT_TEXT_CACHE_SIZE;
	
	public static ImageText getFont(int fontSize) {
		return getFont(fontSize, ImageTextForMidp2.DEFAULT_TEXT_CACHE_SIZE);
	}
	
	public static ImageText getFont(int fontSize, int cacheSize) {
		switch (fontSize) {
		case FONT_SIZE_8:
			if ((text8 == null) || (cacheSize > currentCacheSize)) {
				if (MIDP_VERSION < 2) {
					text8 = new ImageTextForMidp1(FONT_8_FILE);
				} else {
					text8 = new ImageTextForMidp2(FONT_8_FILE, cacheSize);
				}
			}
			return text8;
		case FONT_SIZE_11:
			if ((text11 == null) || (cacheSize > currentCacheSize)) {
				if (MIDP_VERSION < 2.0) {
					text11 = new ImageTextForMidp1(FONT_11_FILE);
				} else {
					text11 = new ImageTextForMidp2(FONT_11_FILE, cacheSize);
				}
			}
			return text11;
		case FONT_SIZE_13:
			if ((text13 == null) || (cacheSize > currentCacheSize)) {
				if (MIDP_VERSION < 2) {
					text13 = new ImageTextForMidp1(FONT_13_FILE);
				} else {
					text13 = new ImageTextForMidp2(FONT_13_FILE, cacheSize);
				}
			}
			return text13;
		default:
			throw new IllegalArgumentException("FontManager : getFont : fontSize is wrong");
		}
	}

	public static void detroy() {
		if (text8 != null) {
			text8.detroy();
			text8 = null;
		}
		if (text11 != null) {
			text11.detroy();
			text11 = null;
		}
		if (text13 != null) {
			text13.detroy();
			text13 = null;
		}
	}
}
