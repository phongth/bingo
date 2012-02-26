package state.component;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import state.GameConstants;
import state.GameGlobal;
import state.Key;
import state.Manager;
import state.Sprite;

public class Menu extends MenuItem {
	public static final double ANIMED_STEP = 5.0;

	protected EventListener listener;
	protected MenuItem currentMenuItem;
	protected Image itemBackgroundImage;
	protected boolean isFillFocusItemBg;

	protected int itemWidth = 80;
	protected int itemHeight = 30;
	protected boolean isOpenChildMenuToRight = true;

	protected int menuX = 0;
	protected int menuY = 0;
	protected int textDx = 0;
	protected int textDy = 0;

	private int animedCount;

	public Menu() {
	}

	public Menu(String[] menuItems, EventListener listener, Style style) {
		if (menuItems != null) {
			addItems(menuItems);
		}

		if (listener != null) {
			setListener(listener);
		}

		if (style != null) {
			setStyle(style);
		}
	}

	public Menu(Manager manager, String[] menuItems, EventListener listener,
			Style style) {
		this(menuItems, listener, style);
		if (manager != null) {
			setManager(manager);
		}
	}

	public Menu(Manager manager, String[] menuItems, EventListener listener,
			Style style, int x, int y) {
		this(manager, menuItems, listener, style);
		menuX = x;
		menuY = y;
	}

	public Menu(String[] menuItems, EventListener listener, Style style, int x,
			int y) {
		this(menuItems, listener, style);
		menuX = x;
		menuY = y;
	}

	protected void init() {
		super.init();
		menu = this;
		animedCount = -1;
		parent = this;
		currentMenuItem = this;
		setWidth(GameConstants.SCREEN_WIDTH);
		setHeight(GameConstants.SCREEN_HEIGHT);
		realX = 0;
		realY = 0;
		setVisible(true);
		GameGlobal.systemCanvas.getKeyManagement().addTarget(this);
	}

	public Sprite setPosition(int x, int y) {
		menuX = x;
		menuY = y;
		return this;
	}

	public void setItemSize(int itemWidthm, int itemHeight) {
		setItemWidth(itemWidthm);
		setItemHeight(itemHeight);
	}

	public void setStyle(Style style) {
		super.setStyle(style);
		if (style instanceof MenuStyle) {
			MenuStyle menuStyle = (MenuStyle) style;
			itemWidth = menuStyle.itemWidth;
			itemHeight = menuStyle.itemHeight;
			isOpenChildMenuToRight = menuStyle.isOpenChildMenuToRight;
			textDx = menuStyle.textDx;
			textDy = menuStyle.textDy;
			isFillFocusItemBg = menuStyle.isFillFocusItemBg;
		}
	}

	public void setMenuItemBgImage(Image image) {
		setImage(image, false);
	}

	public boolean isFillFocusItemBg() {
		return isFillFocusItemBg;
	}

	public void setFillFocusItemBg(boolean isFillFocusItemBg) {
		this.isFillFocusItemBg = isFillFocusItemBg;
	}

	public void setMenuX(int menuX) {
		this.menuX = menuX;
	}

	public void setMenuY(int menuY) {
		this.menuY = menuY;
	}

	public int getMenuX() {
		return menuX;
	}

	public int getMenuY() {
		if ((menuImage == null) || (animedCount == -1)) {
			return menuY;
		}

		if (isShowUpToDown) {
			return menuY
					- menuImage.getWidth()
					+ (int) (menuImage.getWidth() * (animedCount / ANIMED_STEP));
		}

		return menuY + menuImage.getWidth()
				- (int) (menuImage.getWidth() * (animedCount / ANIMED_STEP));
	}

	public int getTextDx() {
		return textDx;
	}

	public void setTextDx(int textDx) {
		this.textDx = textDx;
	}

	public int getTextDy() {
		return textDy;
	}

	public void setTextDy(int textDy) {
		this.textDy = textDy;
	}

	public void show() {
		animedCount = 0;
		showChildMenu();
	}

	public boolean isShowing() {
		return isShowChildrenMenuItem;
	}

	public void hide() {
		hideChildMenu();
	}

	public boolean isOpenChildMenuToRight() {
		return isOpenChildMenuToRight;
	}

	public void setOpenChildMenuToRight(boolean isOpenChildMenuToRight) {
		this.isOpenChildMenuToRight = isOpenChildMenuToRight;
	}

	public void setListener(EventListener listener) {
		this.listener = listener;
	}

	public int getItemWidth() {
		return itemWidth;
	}

	public void setItemWidth(int itemWidth) {
		this.itemWidth = itemWidth;
		for (int i = 0; i < items.size(); i++) {
			((Component) items.elementAt(i)).setWidth(itemWidth);
		}
	}

	public Image getItemBackgroundImage() {
		return itemBackgroundImage;
	}

	public void setItemBackgroundImage(Image itemBackgroundImage) {
		this.itemBackgroundImage = itemBackgroundImage;
	}

	public int getItemHeight() {
		return itemHeight;
	}

	public void setItemHeight(int itemHeight) {
		this.itemHeight = itemHeight;
		for (int i = 0; i < items.size(); i++) {
			((Component) items.elementAt(i)).setHeight(itemHeight);
		}
	}

	public void draw(Graphics g) {
		if (!isVisible()) {
			return;
		}

		if (isShowChildrenMenuItem && (items.size() > 0)) {
			int menuHeight = items.size() * menu.itemHeight + menu.textDy * 2;
			if (menuImage == null) {
				menuImage = createMemuBgImage(menuHeight);
			} else {
				if (menuImage.getHeight() != menuHeight) {
					menuImage = createMemuBgImage(menuHeight);
				}
			}
			isShowUpToDown = checkDrawUpOrDown();

			int drawMenuBbX = getMenuX();
			if (!(this instanceof Menu)) {
				drawMenuBbX = getMenuX() + menu.itemWidth + 2;
				if (!menu.isOpenChildMenuToRight) {
					drawMenuBbX = getMenuX() - menu.itemWidth - 2;
				}
			}

			if (animedCount > -1) {
				if (isShowUpToDown) {
					g.setClip(drawMenuBbX, menuY, menuImage.getWidth(),
							menuImage.getHeight());
				} else {
					g.setClip(drawMenuBbX, menuY - menuImage.getHeight()
							+ menu.itemHeight, menuImage.getWidth(), menuImage
							.getHeight());
				}
			}

			int y = getMenuY();
			if (isShowUpToDown) {
				g.drawImage(menuImage, drawMenuBbX, y + 2,
						GameConstants.TOP_LEFT_ANCHOR);
			} else {
				g.drawImage(menuImage, drawMenuBbX, y + 2 + menu.itemHeight,
						GameConstants.BOTTOM_LEFT_ANCHOR);
			}

			for (int i = 0; i < items.size(); i++) {
				MenuItem item = (MenuItem) items.elementAt(i);
				updateChildPosition(item, i, isShowUpToDown);
				item.draw(g);
			}

			if (animedCount > -1) {
				g.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
				animedCount++;
				if (animedCount > ANIMED_STEP) {
					animedCount = -1;
				}
			}
		}
	}

	public void keyReleased(int keyCode) {
		if (!isShowing()) {
			return;
		}

		MenuItem currentParent = menu.currentMenuItem.parent;
		if (currentParent.currentChooseIndex > currentParent.items.size() - 1) {
			currentChooseIndex = currentParent.items.size() - 1;
		}

		int key = Key.getGameKey(keyCode);
		switch (key) {
		case Key.FIRE:
			if (menu.currentMenuItem.items.size() > 0) {
				menu.currentMenuItem.showChildMenu();
			} else {
				if (menu.listener != null) {
					menu.listener.onActionPerform(new Event(
							menu.currentMenuItem, menu.currentMenuItem.label));
					hide();
				}
			}
			break;
		case Key.RIGHT:
			if (menu.isOpenChildMenuToRight) {
				menu.currentMenuItem.showChildMenu();
			} else {
				if (currentParent != this) {
					currentParent.hideChildMenu();
				}
			}
			break;
		case Key.LEFT:
			if (!menu.isOpenChildMenuToRight) {
				menu.currentMenuItem.showChildMenu();
			} else {
				if (currentParent != this) {
					currentParent.hideChildMenu();
				}
			}
			break;
		case Key.UP:
			if (currentParent.items.size() > 0) {
				menu.currentMenuItem.setFocused(false);
				currentParent.currentChooseIndex--;
				if (currentParent.currentChooseIndex < 0) {
					currentParent.currentChooseIndex = currentParent.items
							.size() - 1;
				}
				menu.currentMenuItem = ((MenuItem) currentParent.items
						.elementAt(currentParent.currentChooseIndex));
				menu.currentMenuItem.setFocused(true);
			}
			break;
		case Key.DOWN:
			if (currentParent.items.size() > 0) {
				menu.currentMenuItem.setFocused(false);
				currentParent.currentChooseIndex++;
				if (currentParent.currentChooseIndex > currentParent.items
						.size() - 1) {
					currentParent.currentChooseIndex = 0;
				}
				menu.currentMenuItem = ((MenuItem) currentParent.items
						.elementAt(currentParent.currentChooseIndex));
				menu.currentMenuItem.setFocused(true);
			}
			break;
		}
	}

	protected void updateChildPosition(MenuItem item, int itemIndex,
			boolean isShowUpToDown) {
		int x = menuX;
		int y = getMenuY();
		if (isShowUpToDown) {
			y = y + itemIndex * (menu.itemHeight - 1) + textDy;
		} else {
			y = y - (items.size() - itemIndex - 1) * (menu.itemHeight - 1)
					- textDy;
		}
		item.setPosition(x, y);
	}

	public void detroy() {
		super.detroy();
		listener = null;
		currentMenuItem = null;
		itemBackgroundImage = null;
		GameGlobal.systemCanvas.getKeyManagement().removeTarget(this);
	}
}
