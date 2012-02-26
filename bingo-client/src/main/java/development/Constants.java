package development;

import state.component.ListViewStyle;
import state.component.MenuStyle;
import state.component.TextFieldStyle;
import state.font.FontManager;
import state.util.Color;

public class Constants {
	// public static final String LOGIN_SERVER = "122.201.15.5";
	public static final String LOGIN_SERVER = "localhost";
	public static final int PORT = 1369;

	public static final int DEFAULT_PROVIDER_ID = 1; // TODO: Provider mặc định
														// ban đầu của bản
														// client này, cần thay
														// đổi giá trị này tùy
														// theo đối tác
	public static final String VERSION = "0.0.1";

	public static int USER_NAME_MIN_LEN = 2;
	public static int USER_NAME_MAX_LEN = 10;

	public static int PASSWORD_MIN_LEN = 2;
	public static int PASSWORD_MAX_LEN = 10;

	public static final MenuStyle MENU_STYLE = new MenuStyle();
	static {
		MENU_STYLE.setFillBackGround(false);
		MENU_STYLE.setFillFocusItemBg(true);
		MENU_STYLE.setFocusBgColor(0xA53A06);
		MENU_STYLE.setForceGroundColor(Color.WHITE_CODE);
		MENU_STYLE.setFocusFgColor(Color.WHITE_CODE);
		MENU_STYLE.setHasBorder(false);
		MENU_STYLE.setItemHeight(18);
		MENU_STYLE.setItemWidth(116);
		MENU_STYLE.setFont(FontManager.getFont(FontManager.FONT_SIZE_8));
		MENU_STYLE.setTextDx(5);
		MENU_STYLE.setTextDy(2);
	}

	public static final ListViewStyle LIST_VIEW_STYLE = new ListViewStyle();
	static {
		LIST_VIEW_STYLE.setFillBackGround(false);
		LIST_VIEW_STYLE.setBorderColor(0x7d1013);
		LIST_VIEW_STYLE.setForceGroundColor(Color.WHITE_CODE);
		LIST_VIEW_STYLE.setFillTitleBackGround(false);
		LIST_VIEW_STYLE.setTitleForceGroundColor(Color.WHITE_CODE);
		LIST_VIEW_STYLE.setTitleFont(FontManager
				.getFont(FontManager.FONT_SIZE_11));
	}

	public static final TextFieldStyle TEXT_FIELD_STYLE = new TextFieldStyle();
	static {
		TEXT_FIELD_STYLE.setFillBackGround(true);
		TEXT_FIELD_STYLE.setBackgroundColor(Color.WHITE_CODE);
		TEXT_FIELD_STYLE.setHasBorder(false);
		TEXT_FIELD_STYLE.setForceGroundColor(Color.BLACK_CODE);
		TEXT_FIELD_STYLE.setFont(FontManager.getFont(FontManager.FONT_SIZE_11));
		TEXT_FIELD_STYLE.setAlign(TextFieldStyle.ALIGN_LEFT);
		TEXT_FIELD_STYLE.setFocusable(true);
		TEXT_FIELD_STYLE.setFocusBgColor(0xFDFBB7);
		TEXT_FIELD_STYLE.setFocusFgColor(Color.BLACK_CODE);
	}

	public static final String[] GAME_NAME = new String[] { "Phỏm",
			"Tiến lên Miền Bắc", "Cờ tướng", "Caro", "Cờ vua" };
	public static final String[] GAME_ID = new String[] { "tala", "tlmb",
			"cotuong", "caro", "covua" };

	public static final String ROOM_MASTER_ROLE = "owner";
	public static final String ROOM_MEMBER_ROLE = "participant";

	// Keypad
	public static final char[] LOGIN_KEY_NUM1_CHARS = { '1' };

	public static final char[] KEY_NUM1_CHARS = { '.', ',', '\'', '?', '!',
			'\"', '1' };
	public static final char[] KEY_NUM2_CHARS = { 'a', 'b', 'c', '2' };
	public static final char[] KEY_NUM3_CHARS = { 'd', 'e', 'f', '3' };
	public static final char[] KEY_NUM4_CHARS = { 'g', 'h', 'i', '4' };
	public static final char[] KEY_NUM5_CHARS = { 'j', 'k', 'l', '5' };
	public static final char[] KEY_NUM6_CHARS = { 'm', 'n', 'o', '6' };
	public static final char[] KEY_NUM7_CHARS = { 'p', 'q', 'r', 's', '7' };
	public static final char[] KEY_NUM8_CHARS = { 't', 'u', 'v', '8' };
	public static final char[] KEY_NUM9_CHARS = { 'w', 'x', 'y', 'z', '9' };
	public static final char[] KEY_NUM0_CHARS = { ' ', '0' };
	public static final char[] KEY_STAR_CHARS = { '*', '*', '*', '*' };
}
