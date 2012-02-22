package state.component;

public class MenuStyle extends Style {
	protected int itemWidth = 80;
	protected int itemHeight = 30;
	protected boolean isOpenChildMenuToRight = true;
	protected int textDx;
	protected int textDy;
	protected boolean isFillFocusItemBg;

	public int getItemWidth() {
		return itemWidth;
	}

	public MenuStyle setItemWidth(int itemWidth) {
		this.itemWidth = itemWidth;
		return this;
	}

	public int getItemHeight() {
		return itemHeight;
	}

	public MenuStyle setItemHeight(int itemHeight) {
		this.itemHeight = itemHeight;
		return this;
	}

	public boolean isOpenChildMenuToRight() {
		return isOpenChildMenuToRight;
	}

	public MenuStyle setOpenChildMenuToRight(boolean isOpenChildMenuToRight) {
		this.isOpenChildMenuToRight = isOpenChildMenuToRight;
		return this;
	}

	public int getTextDx() {
		return textDx;
	}

	public MenuStyle setTextDx(int textDx) {
		this.textDx = textDx;
		return this;
	}

	public int getTextDy() {
		return textDy;
	}

	public MenuStyle setTextDy(int textDy) {
		this.textDy = textDy;
		return this;
	}

	public boolean isFillFocusItemBg() {
		return isFillFocusItemBg;
	}

	public MenuStyle setFillFocusItemBg(boolean isFillFocusItemBg) {
		this.isFillFocusItemBg = isFillFocusItemBg;
		return this;
	}
}
