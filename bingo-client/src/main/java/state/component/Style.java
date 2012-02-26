package state.component;

import state.font.ImageText;
import state.util.Color;

public class Style {
	protected int forceGroundColor = Color.BLACK_CODE;
	protected int backgroundColor = Color.WHITE_CODE;
	protected boolean isEnable = true;
	protected boolean hasBorder = true;
	protected int borderColor = Color.RED2_CODE;
	protected boolean isFocused = false;
	protected boolean focusable = true;
	protected boolean isFillBackGround = true;
	protected int focusBgColor = Color.GREEN_CODE;
	protected int focusFgColor = Color.RED2_CODE;
	protected String backgroundImageName;
	protected ImageText font;

	public int getForceGroundColor() {
		return forceGroundColor;
	}

	public Style setForceGroundColor(int forceGroundColor) {
		this.forceGroundColor = forceGroundColor;
		return this;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public Style setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public boolean isEnable() {
		return isEnable;
	}

	public Style setEnable(boolean isEnable) {
		this.isEnable = isEnable;
		return this;
	}

	public boolean isHasBorder() {
		return hasBorder;
	}

	public Style setHasBorder(boolean hasBorder) {
		this.hasBorder = hasBorder;
		return this;
	}

	public int getBorderColor() {
		return borderColor;
	}

	public Style setBorderColor(int borderColor) {
		this.borderColor = borderColor;
		return this;
	}

	public boolean isFocused() {
		return isFocused;
	}

	public Style setFocused(boolean isFocused) {
		this.isFocused = isFocused;
		return this;
	}

	public boolean isFocusable() {
		return focusable;
	}

	public Style setFocusable(boolean focusable) {
		this.focusable = focusable;
		return this;
	}

	public boolean isFillBackGround() {
		return isFillBackGround;
	}

	public Style setFillBackGround(boolean isFillBackGround) {
		this.isFillBackGround = isFillBackGround;
		return this;
	}

	public int getFocusBgColor() {
		return focusBgColor;
	}

	public Style setFocusBgColor(int focusBgColor) {
		this.focusBgColor = focusBgColor;
		return this;
	}

	public int getFocusFgColor() {
		return focusFgColor;
	}

	public Style setFocusFgColor(int focusFgColor) {
		this.focusFgColor = focusFgColor;
		return this;
	}

	public ImageText getFont() {
		return font;
	}

	public Style setFont(ImageText font) {
		this.font = font;
		return this;
	}

	public String getBackgroundImageName() {
		return backgroundImageName;
	}

	public Style setBackgroundImageName(String backgroundImageName) {
		this.backgroundImageName = backgroundImageName;
		return this;
	}
}
