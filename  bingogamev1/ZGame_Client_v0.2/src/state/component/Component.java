package state.component;

import javax.microedition.lcdui.Graphics;

import state.GameConstants;
import state.DrawListener;
import state.KeyListener;
import state.KeySpriteListener;
import state.Sprite;
import state.Manager;
import state.font.FontManager;
import state.font.ImageText;
import state.util.Color;
import state.util.ImageUtil;

public abstract class Component extends Sprite implements DrawListener,
		KeyListener, KeySpriteListener {
	protected boolean isFillBackGround = true;
	protected int backgroundColor = Color.WHITE_CODE;

	protected boolean isEnable = true;
	protected int forceGroundColor = Color.BLACK_CODE;

	protected boolean hasBorder = true;
	protected int borderColor = Color.RED2_CODE;

	protected boolean isFocused = false;
	protected boolean focusable = true;
	protected int focusBgColor = Color.GREEN_CODE;
	protected int focusFgColor = Color.RED2_CODE;

	private ImageText font;

	public Component() {
		super(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
		init();
	}

	/**
	 * Constructor
	 * 
	 * @param x
	 *            - Tọa độ x của Component
	 * @param y
	 *            - Tọa độ y của Component
	 * @param width
	 *            - Chiều rộng của Component
	 * @param height
	 *            - Chiều dài của Component
	 */
	public Component(int x, int y, int width, int height) {
		super(x, y, width, height);
		init();
	}

	/**
	 * Constructor
	 * 
	 * @param manager
	 *            - Manager dùng để quản lý việc hiển thị
	 * @param width
	 *            - Chiều rộng của Component
	 * @param height
	 *            - Chiều dài của Component
	 */
	public Component(Manager manager, int width, int height) {
		super(manager, width, height);
		init();
	}

	/**
	 * Constructor
	 * 
	 * @param manager
	 *            - Manager dùng để quản lý việc hiển thị
	 * @param x
	 *            - Tọa độ x của Component
	 * @param y
	 *            - Tọa độ y của Component
	 * @param width
	 *            - Chiều rộng của Component
	 * @param height
	 *            - Chiều dài của Component
	 */
	public Component(Manager manager, int x, int y, int width, int height) {
		super(manager, x, y, width, height);
		init();
	}

	protected void init() {
		setDrawListener(this);
		setKeyListener(this);
	}

	public abstract void draw(Graphics g);

	public void paint(Sprite source, Graphics g) {
		draw(g);
	}

	public void setStyle(Style style) {
		forceGroundColor = style.forceGroundColor;
		backgroundColor = style.backgroundColor;
		isEnable = style.isEnable;
		hasBorder = style.hasBorder;
		borderColor = style.borderColor;
		isFocused = style.isFocused;
		focusable = style.focusable;
		isFillBackGround = style.isFillBackGround;
		focusBgColor = style.focusBgColor;
		focusFgColor = style.focusFgColor;
		borderColor = style.borderColor;
		setFont(style.font);
		if (style.backgroundImageName == null) {
			setImage(null, false);
		} else {
			setImage(ImageUtil.getImage(style.backgroundImageName), false);
		}
	}

	public ImageText getFont() {
		if (font == null) {
			font = FontManager.getFont(FontManager.FONT_SIZE_8);
		}
		return font;
	}

	public void setFont(ImageText font) {
		this.font = font;
	}

	public void keyPressed(int keyCode) {
	}

	public void keyReleased(int keyCode) {
	}

	public void keyRepeated(int keyCode) {
	}

	// BEGIN For KeySpriteListener

	public void keyPressed(Sprite source, int keyCode) {
		keyPressed(keyCode);
	}

	public void keyReleased(Sprite source, int keyCode) {
		keyReleased(keyCode);
	}

	public void keyRepeated(Sprite source, int keyCode) {
		keyRepeated(keyCode);
	}

	// END For KeySpriteListener

	/** Focus back ground color */
	public int getFocusBgColor() {
		return focusBgColor;
	}

	/** Focus back ground color */
	public void setFocusBgColor(int focusBgColor) {
		this.focusBgColor = focusBgColor;
	}

	/** Focus force ground color */
	public int getFocusFgColor() {
		return focusFgColor;
	}

	/** Focus force ground color */
	public void setFocusFgColor(int focusFgColor) {
		this.focusFgColor = focusFgColor;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		isFillBackGround = true;
		this.backgroundColor = backgroundColor;
	}

	public boolean isFillBackGround() {
		return isFillBackGround;
	}

	public void setFillBackGround(boolean isFillBackGround) {
		this.isFillBackGround = isFillBackGround;
	}

	public int getForceGroundColor() {
		return forceGroundColor;
	}

	public void setForceGroundColor(int forceGroundColor) {
		this.forceGroundColor = forceGroundColor;
	}

	public boolean isEnable() {
		return isEnable;
	}

	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}

	public boolean isHasBorder() {
		return hasBorder;
	}

	public void setHasBorder(boolean hasBorder) {
		this.hasBorder = hasBorder;
	}

	public int getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(int borderColor) {
		hasBorder = true;
		this.borderColor = borderColor;
	}

	public boolean isFocused() {
		return isFocused;
	}

	public void setFocused(boolean isFocused) {
		if (focusable) {
			this.isFocused = isFocused;
		}
	}

	public boolean isFocusable() {
		return focusable;
	}

	public void setFocusable(boolean focusable) {
		this.focusable = focusable;
		if (!focusable) {
			isFocused = false;
		}
	}

	public void detroy() {
		super.detroy();
		font = null;
	}
}
