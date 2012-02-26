package state.component;

import state.font.ImageText;
import state.util.Color;

public class ListViewStyle extends Style {
	protected boolean isShowColumnTitle = true;
	protected boolean isFillTitleBackGround = true;
	protected int tileBackgroundColor = Color.WHITE_CODE;
	protected int titleForceGroundColor = Color.BLACK_CODE;
	protected int rowHeight = ListView.DEFAULT_ROW_HEIGHT;
	private int titleRowHeight = ListView.DEFAULT_ROW_HEIGHT;
	private ImageText titleFont;

	public boolean isShowColumnTitle() {
		return isShowColumnTitle;
	}

	public void setShowColumnTitle(boolean isShowColumnTitle) {
		this.isShowColumnTitle = isShowColumnTitle;
	}

	public boolean isFillTitleBackGround() {
		return isFillTitleBackGround;
	}

	public void setFillTitleBackGround(boolean isFillTitleBackGround) {
		this.isFillTitleBackGround = isFillTitleBackGround;
	}

	public int getTileBackgroundColor() {
		return tileBackgroundColor;
	}

	public void setTileBackgroundColor(int tileBackgroundColor) {
		this.tileBackgroundColor = tileBackgroundColor;
	}

	public int getTitleForceGroundColor() {
		return titleForceGroundColor;
	}

	public void setTitleForceGroundColor(int titleForceGroundColor) {
		this.titleForceGroundColor = titleForceGroundColor;
	}

	public int getRowHeight() {
		return rowHeight;
	}

	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}

	public int getTitleRowHeight() {
		return titleRowHeight;
	}

	public void setTitleRowHeight(int titleRowHeight) {
		this.titleRowHeight = titleRowHeight;
	}

	public ImageText getTitleFont() {
		return titleFont;
	}

	public void setTitleFont(ImageText titleFont) {
		this.titleFont = titleFont;
	}
}
