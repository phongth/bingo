package state;

import javax.microedition.lcdui.Graphics;

import state.component.ListViewStyle;
import state.component.MenuStyle;
import state.font.FontManager;
import state.util.Color;

public class GameConstants {
	public static boolean IS_240x320_SCREEN = true;
	public static int SCREEN_WIDTH = 240;
	public static int SCREEN_HEIGHT = 320;

	public static final String SOUND_FOLDER = "/sound";
	public static final String IMAGE_FOLDER = "/image";

	public static final String CORE_IMAGE_FOLDER = "/core/image";

	public static final int TOP_LEFT_ANCHOR = Graphics.TOP | Graphics.LEFT;
	public static final int TOP_RIGHT_ANCHOR = Graphics.TOP | Graphics.RIGHT;
	public static final int TOP_HCENTER_ANCHOR = Graphics.TOP
			| Graphics.HCENTER;
	public static final int VCENTER_LEFT_ANCHOR = Graphics.VCENTER
			| Graphics.LEFT;
	public static final int VCENTER_RIGHT_ANCHOR = Graphics.VCENTER
			| Graphics.RIGHT;
	public static final int CENTER_ANCHOR = Graphics.VCENTER | Graphics.HCENTER;
	public static final int BOTTOM_HCENTER_ANCHOR = Graphics.BOTTOM
			| Graphics.HCENTER;
	public static final int BOTTOM_LEFT_ANCHOR = Graphics.BOTTOM
			| Graphics.LEFT;
	public static final int BOTTOM_RIGHT_ANCHOR = Graphics.BOTTOM
			| Graphics.RIGHT;

	public static MenuStyle DEFAULT_MENU_STYLE = new MenuStyle();
	static {
		DEFAULT_MENU_STYLE.setFillBackGround(false);
		DEFAULT_MENU_STYLE.setFillFocusItemBg(true);
		DEFAULT_MENU_STYLE.setFocusBgColor(0xA53A06);
		DEFAULT_MENU_STYLE.setForceGroundColor(Color.WHITE_CODE);
		DEFAULT_MENU_STYLE.setFocusFgColor(Color.WHITE_CODE);
		DEFAULT_MENU_STYLE.setHasBorder(false);
		DEFAULT_MENU_STYLE.setItemHeight(18);
		DEFAULT_MENU_STYLE.setItemWidth(116);
		DEFAULT_MENU_STYLE
				.setFont(FontManager.getFont(FontManager.FONT_SIZE_8));
		DEFAULT_MENU_STYLE.setTextDx(5);
		DEFAULT_MENU_STYLE.setTextDy(2);
	}

	public static ListViewStyle DEFAULT_LIST_VIEW_STYLE = new ListViewStyle();
	static {
		DEFAULT_LIST_VIEW_STYLE.setFillBackGround(false);
		DEFAULT_LIST_VIEW_STYLE.setBorderColor(0x7d1013);
		DEFAULT_LIST_VIEW_STYLE.setForceGroundColor(Color.WHITE_CODE);
		DEFAULT_LIST_VIEW_STYLE.setFillTitleBackGround(false);
		DEFAULT_LIST_VIEW_STYLE.setTitleForceGroundColor(Color.WHITE_CODE);
		DEFAULT_LIST_VIEW_STYLE.setTitleFont(FontManager
				.getFont(FontManager.FONT_SIZE_11));
	}

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
