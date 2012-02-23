package state.component;

import javax.microedition.lcdui.Graphics;

import state.GameConstants;
import state.Key;
import state.Sprite;
import state.font.ImageText;

public class TextField extends Component {
	public static final long DEFAULT_KEY_DELAY = 1000;

	public static final int CONSTRAINT_ANY = 0;
	public static final int CONSTRAINT_PASSWORD = 1;
	public static final int CONSTRAINT_USER_NAME = 2;
	public static final int CONSTRAINT_PHONE_NUMBER = 3;// allow input phone
														// number
	public static final int CONSTRAINT_NUMERIC = 4; // allow input integer
													// number
	// public static final int CONSTRAINT_EMAIL_ADDRESS = 5; // allow input
	// email address
	// public static final int CONSTRAINT_URL = 6; // allow input url

	private static final char[][] REPLACE_CHAR = new char[][] { { 'ư', 'u' },
			{ 'Ư', 'U' }, { 'ơ', 'o' }, { 'Ơ', 'O' }, { 'Đ', 'D' },
			{ 'đ', 'd' }, { 'Ă', 'A' }, { 'ă', 'a' }, { 'Â', 'A' },
			{ 'â', 'a' } };

	private static final char[][][] CONSTRAINT_KEY_MAP = new char[][][] { { // CONSTRAINT_ANY
			{ '.', ',', '\'', '?', '!', '\"', '1' }, { 'a', 'b', 'c', '2' },
					{ 'd', 'e', 'f', '3' }, { 'g', 'h', 'i', '4' },
					{ 'j', 'k', 'l', '5' }, { 'm', 'n', 'o', '6' },
					{ 'p', 'q', 'r', 's', '7' }, { 't', 'u', 'v', '8' },
					{ 'w', 'x', 'y', 'z', '9' }, { ' ', '0' }, { '*' } }, { // CONSTRAINT_PASSWORD
			{ '1' }, { 'a', 'b', 'c', '2' }, { 'd', 'e', 'f', '3' },
					{ 'g', 'h', 'i', '4' }, { 'j', 'k', 'l', '5' },
					{ 'm', 'n', 'o', '6' }, { 'p', 'q', 'r', 's', '7' },
					{ 't', 'u', 'v', '8' }, { 'w', 'x', 'y', 'z', '9' },
					{ '0' }, {} }, { // CONSTRAINT_USER_NAME
			{ '1' }, { 'a', 'b', 'c', '2' }, { 'd', 'e', 'f', '3' },
					{ 'g', 'h', 'i', '4' }, { 'j', 'k', 'l', '5' },
					{ 'm', 'n', 'o', '6' }, { 'p', 'q', 'r', 's', '7' },
					{ 't', 'u', 'v', '8' }, { 'w', 'x', 'y', 'z', '9' },
					{ '0' }, {} }, { // CONSTRAINT_PHONE_NUMBER
			{ '1' }, { '2' }, { '3' }, { '4' }, { '5' }, { '6' }, { '7' },
					{ '8' }, { '9' }, { '0' }, {} }, { // CONSTRAINT_NUMERIC
			{ '1' }, { '2' }, { '3' }, { '4' }, { '5' }, { '6' }, { '7' },
					{ '8' }, { '9' }, { '0' }, {} } };

	private StringBuffer showText;
	private StringBuffer realText;

	private int textX;
	private int textY;

	private int constraint = CONSTRAINT_ANY;
	private int align = TextFieldStyle.ALIGN_LEFT;
	private int maxLength = Integer.MAX_VALUE;

	private int lastKeyCode;
	private long lastTimeKey;
	private int lastCharIndex;
	private boolean isShowLineOnFocus;
	private long lastTimeDrawLine;
	private int clearKey = Integer.MAX_VALUE;

	public TextField(int constraint) {
		this.constraint = constraint;
	}

	public TextField(int constraint, TextFieldStyle style) {
		this.constraint = constraint;
		setStyle(style);
	}

	protected void init() {
		super.init();

		showText = new StringBuffer();
		realText = new StringBuffer();
		setSize(150, 20);
	}

	public void setStyle(TextFieldStyle style) {
		super.setStyle(style);
		setAlign(style.align);
	}

	/**
	 * User for screen 240x320
	 */
	private char[] getChars(int keyCode) {
		switch (keyCode) {
		case Key.K_1:
		case Key.K_2:
		case Key.K_3:
		case Key.K_4:
		case Key.K_5:
		case Key.K_6:
		case Key.K_7:
		case Key.K_8:
		case Key.K_9:
			return CONSTRAINT_KEY_MAP[constraint][keyCode - Key.K_1];
		case Key.K_0:
			return CONSTRAINT_KEY_MAP[constraint][9];
		case Key.STAR:
			return CONSTRAINT_KEY_MAP[constraint][10];
		}
		return null;
	}

	private char getChar(int keyCode) {
		char c = (char) keyCode;
		return checkReplaceChar(c);
	}

	private char checkReplaceChar(char c) {
		for (int i = 0; i < REPLACE_CHAR.length; i++) {
			if (c == REPLACE_CHAR[i][0]) {
				return REPLACE_CHAR[i][1];
			}
		}
		return c;
	}

	private boolean isCharValid(char c) {
		char[][] chars = CONSTRAINT_KEY_MAP[constraint];
		for (int i = 0; i < chars.length; i++) {
			for (int j = 0; j < chars[i].length; j++) {
				if (c == chars[i][j]) {
					return true;
				}
			}
		}
		return false;
	}

	public void draw(Graphics g) {
		if (!isVisible) {
			return;
		}

		if (isFillBackGround) {
			if (focusable && isFocused) {
				g.setColor(focusBgColor);
			} else {
				g.setColor(backgroundColor);
			}
			g.fillRoundRect(realX, realY, width, height, 5, 5);
		}

		if (hasBorder) {
			g.setColor(borderColor);
			g.drawRoundRect(realX, realY, width, height, 5, 5);
		}

		ImageText font = getFont();
		if (focusable && isFocused) {
			font.drawString(g, showText.toString(), focusFgColor, textX, textY,
					align);
			if (isShowLineOnFocus) {
				int lineX = textX + font.stringWidth(showText.toString()) + 1;
				g.setColor(focusFgColor);
				g.drawLine(lineX, textY - font.getHeight() / 2 + 1, lineX,
						textY + font.getHeight() / 2 - 1);
			}

			long now = System.currentTimeMillis();
			if (now - lastTimeDrawLine > DEFAULT_KEY_DELAY) {
				isShowLineOnFocus = !isShowLineOnFocus;
				lastTimeDrawLine = now;
				if (constraint == CONSTRAINT_PASSWORD) {
					if (this.showText.length() > 0) {
						this.showText.deleteCharAt(this.showText.length() - 1);
						this.showText.append('*');
					}
				}
			}
		} else {
			font.drawString(g, showText.toString(), forceGroundColor, textX,
					textY, align);
		}
	}

	public void keyReleased(int keyCode) {
		if (GameConstants.IS_240x320_SCREEN) {
			keyReleased240x320(keyCode);
		} else {
			keyReleased320x240(keyCode);
		}
	}

	private void keyReleased240x320(int keyCode) {
		if (keyCode == clearKey) {
			deleteLastChar();
			return;
		}

		char[] keyMap = getChars(keyCode);
		if ((keyMap == null) || (keyMap.length == 0)) {
			return;
		}

		long now = System.currentTimeMillis();
		if (keyCode == lastKeyCode) {
			if (now - lastTimeKey < DEFAULT_KEY_DELAY) {
				lastCharIndex = (lastCharIndex + 1) % keyMap.length;
				replaceLastChar(keyMap[lastCharIndex]);
			} else {
				lastCharIndex = 0;
				append(keyMap[lastCharIndex]);
			}
		} else {
			if (constraint == CONSTRAINT_PASSWORD) {
				if (this.showText.length() > 0) {
					this.showText.deleteCharAt(this.showText.length() - 1);
					this.showText.append('*');
				}
			}

			lastCharIndex = 0;
			lastKeyCode = keyCode;
			append(keyMap[lastCharIndex]);
		}
		lastTimeKey = now;
		lastTimeDrawLine = now;
		isShowLineOnFocus = false;
	}

	private void keyReleased320x240(int keyCode) {
		keyCode = Key.getGameKey(keyCode);
		if (!Key.isCommandKey) {
			char c = getChar(keyCode);
			if (!isCharValid(c)) {
				return;
			}
			append(c);
		} else {
			if (keyCode == clearKey) {
				deleteLastChar();
			}
		}
	}

	private int getTextY() {
		return realY + height / 2;
	}

	private int getTextX() {
		int textX = realX + 2;
		switch (align) {
		case TextFieldStyle.ALIGN_LEFT:
			textX = realX + 2;
			break;
		case TextFieldStyle.ALIGN_CENTER:
			textX = realX + width / 2;
			anchor = GameConstants.CENTER_ANCHOR;
			break;
		case TextFieldStyle.ALIGN_RIGHT:
			textX = realX + width - 2;
			anchor = GameConstants.VCENTER_RIGHT_ANCHOR;
			break;
		}
		return textX;
	}

	public int getConstraint() {
		return constraint;
	}

	public TextField setConstraint(int constraint) {
		this.constraint = constraint;
		return this;
	}

	public int getAlign() {
		return align;
	}

	public TextField setAlign(int align) {
		this.align = align;
		updateTextPosition();
		return this;
	}

	public Sprite setHeight(int height) {
		super.setHeight(height);
		updateTextPosition();
		return this;
	}

	public Sprite setPosition(int x, int y) {
		super.setPosition(x, y);
		updateTextPosition();
		return this;
	}

	public Sprite setSize(int width, int height) {
		super.setSize(width, height);
		updateTextPosition();
		return this;
	}

	public Sprite setWidth(int width) {
		super.setWidth(width);
		updateTextPosition();
		return this;
	}

	private void updateTextPosition() {
		textX = getTextX();
		textY = getTextY();
	}

	public int getMaxLength() {
		return maxLength;
	}

	public TextField setMaxLength(int maxLength) {
		this.maxLength = maxLength;
		return this;
	}

	public String getText() {
		return realText.toString();
	}

	private void replaceLastChar(char c) {
		if (this.realText.length() == 0) {
			return;
		}
		this.realText.deleteCharAt(this.realText.length() - 1);
		this.realText.append(c);

		this.showText.deleteCharAt(this.showText.length() - 1);
		this.showText.append(c);
	}

	public TextField setText(String text) {
		this.realText.setLength(0);
		this.realText.append(text);

		this.showText.setLength(0);
		if (constraint == CONSTRAINT_PASSWORD) {
			for (int i = 0; i < text.length(); i++) {
				this.showText.append('*');
			}
		} else {
			this.showText.append(text);
		}
		return this;
	}

	public TextField append(String text) {
		if (text.length() > maxLength) {
			text = text.substring(0, maxLength);
		}

		this.realText.append(text);
		if (constraint == CONSTRAINT_PASSWORD) {
			for (int i = 0; i < text.length(); i++) {
				this.showText.append('*');
			}
		} else {
			this.showText.append(text);
		}
		return this;
	}

	public TextField append(char text) {
		if (realText.length() >= maxLength) {
			return this;
		}

		this.realText.append(text);
		this.showText.append(text);
		return this;
	}

	public TextField clearText() {
		this.realText.setLength(0);
		;
		this.showText.setLength(0);
		return this;
	}

	public TextField deleteLastChar() {
		if (this.realText.length() > 0) {
			this.realText.deleteCharAt(this.realText.length() - 1);
			this.showText.deleteCharAt(this.showText.length() - 1);
		}
		return this;
	}

	public TextField setClearCharKey(int clearCharKey) {
		this.clearKey = clearCharKey;
		return this;
	}

	public void detroy() {
		super.detroy();
		showText = null;
		realText = null;
	}
}
