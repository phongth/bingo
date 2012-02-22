package state.component;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;


import state.GameConstants;
import state.Manager;
import state.font.ImageTextForMidp2;
import state.util.Color;
import state.util.ImageUtil;

public class MenuItem extends Component {
	protected Vector items = new Vector();
	protected MenuItem parent;
	protected Menu menu;

	protected boolean isShowChildrenMenuItem = false;
	protected int currentChooseIndex = -1;
	protected boolean isShowUpToDown;
	protected String label = "";

	protected Image menuImage;
	protected Image arowImage;

	protected MenuItem() {
	}

	public MenuItem(String label) {
		this.label = label;
	}

	protected void init() {
		super.init();
		setVisible(false);
	}

	public MenuItem getParent() {
		return parent;
	}

	public void setParent(MenuItem parent) {
		this.parent = parent;
	}

	public void setStyle(Style style) {
		super.setStyle(style);
	}

	public void setManager(Manager manager) {
		if (manager != null) {
			manager.append(this);
		}
	}

	public Menu getMenu() {
		return menu;
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
		setWidth(menu.itemWidth);
		setHeight(menu.itemHeight);
		if (menu.manager != null) {
			setManager(menu.manager);
		}
		for (int i = 0; i < items.size(); i++) {
			((MenuItem) items.elementAt(i)).setMenu(menu);
		}
	}

	public void addItems(String[] items) {
		for (int i = 0; i < items.length; i++) {
			addItem(items[i]);
		}
	}

	public void insertItem(MenuItem item, int index) {
		if (menu != null) {
			item.setMenu(menu);
			if (menu.currentMenuItem == menu) {
				menu.currentMenuItem = item;
				menu.currentMenuItem.setFocused(true);
			}
		}
		item.setParent(this);
		items.insertElementAt(item, index);
		isShowUpToDown = checkDrawUpOrDown();
		for (int i = 0; i < items.size(); i++) {
			updateChildPosition((MenuItem) items.elementAt(i), i, isShowUpToDown);
		}
	}
	
	public MenuItem insertItem(String itemLabel, int index) {
		MenuItem item = new MenuItem(itemLabel);
		insertItem(item, index);
		return item;
	}
	
	public MenuItem addItem(String itemLabel) {
		MenuItem item = new MenuItem(itemLabel);
		addItem(item);
		return item;
	}

	public void addItem(MenuItem item) {
		if (menu != null) {
			item.setMenu(menu);
			if (menu.currentMenuItem == menu) {
				menu.currentMenuItem = item;
				menu.currentMenuItem.setFocused(true);
			}
		}
		item.setParent(this);
		items.addElement(item);
		isShowUpToDown = checkDrawUpOrDown();
		updateChildPosition(item, items.size() - 1, isShowUpToDown);
	}

	public MenuItem getSubMenu(int index) {
		if (index > items.size() - 1) {
			return null;
		}
		return (MenuItem) items.elementAt(index);
	}
	
	public MenuItem getSubMenu(String itemLabel) {
		for (int i = 0; i < items.size(); i++) {
			MenuItem item = (MenuItem) items.elementAt(i);
			if (item.getLabel().equals(itemLabel)) {
				return item;
			}
		}
		return null;
	}

	public void setFocused(boolean isFocused) {
		super.setFocused(isFocused);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	protected boolean checkDrawUpOrDown() {
		if (menu == null) {
			return false;
		}

		if (items.size() * menu.itemHeight > GameConstants.SCREEN_HEIGHT - menu.menuY) {
			return false;
		} else {
			return true;
		}
	}

	public void removeItem(MenuItem item) {
		items.removeElement(item);
	}

	public void removeAll() {
		items.removeAllElements();
	}

	public MenuItem removeItem(int index) {
		if ((index < 0) || (index > items.size() - 1)) {
			throw new IllegalArgumentException("MenuItem : removeItem : index is invalid: " + index);
		}

		MenuItem item = (MenuItem) items.elementAt(index);
		items.removeElementAt(index);
		return item;
	}

	public MenuItem removeItem(String itemLabel) {
		for (int i = 0; i < items.size(); i++) {
			MenuItem item = (MenuItem) items.elementAt(i);
			if (itemLabel.equals(item.getLabel())) {
				items.removeElement(item);
				return item;
			}
		}
		return null;
	}

	public void setFont(ImageTextForMidp2 font) {
		super.setFont(font);
		for (int i = 0; i < items.size(); i++) {
			((MenuItem) items.elementAt(i)).setFont(font);
		}
	}

	protected void showChildMenu() {
		if (!isShowChildrenMenuItem && (items.size() > 0)) {
			isShowChildrenMenuItem = true;
			for (int i = 0; i < items.size(); i++) {
				MenuItem menuItem = (MenuItem) items.elementAt(i);
				menuItem.setVisible(true);
			}
			menu.currentMenuItem = ((MenuItem) items.elementAt(0));
			menu.currentMenuItem.setFocused(true);
			currentChooseIndex = 0;
		}
	}

	protected void hideChildMenu() {
		if (isShowChildrenMenuItem) {
			isShowChildrenMenuItem = false;
			currentChooseIndex = -1;
			setFocused(true);
			if (menu != null) {
				menu.currentMenuItem = this;
			}

			for (int i = 0; i < items.size(); i++) {
				MenuItem menuItem = (MenuItem) items.elementAt(i);
				menuItem.hideChildMenu();
				menuItem.setVisible(false);
				menuItem.setFocused(false);
			}
		}
	}

	protected void updateChildPosition(MenuItem item, int itemIndex, boolean isShowUpToDown) {
		if (menu == null) {
			return;
		}

		int x = getMenuX() + menu.itemWidth + 2;
		if (!menu.isOpenChildMenuToRight) {
			x = getMenuX() - menu.itemWidth - 2;
		}

		int y = 0;
		if (isShowUpToDown) {
			y = getMenuY() + itemIndex * (menu.itemHeight - 1) + menu.textDy;
		} else {
			y = getMenuY() - (items.size() - itemIndex - 1) * (menu.itemHeight - 1) - menu.textDy;
		}
		item.setPosition(x, y);
	}

	public void draw(Graphics g) {
		if (menu == null) {
			return;
		}

		if (!parent.isShowChildrenMenuItem) {
			return;
		}

		if (menu.isFillBackGround) {
			g.setColor(menu.backgroundColor);
			if (manager == null) {
				g.fillRect(realX + 3, realY, menu.itemWidth, menu.itemHeight);
			} else {
				g.fillRect(realX + 3, realY, menu.itemWidth - 1, menu.itemHeight);
			}
		}

		if (menu.isFillFocusItemBg && isFocused) {
			g.setColor(menu.focusBgColor);
			if (manager == null) {
				g.fillRect(realX + 3, realY, menu.itemWidth, menu.itemHeight);
			} else {
				g.fillRect(realX + 3, realY, menu.itemWidth - 1, menu.itemHeight);
			}
		}

		if (menu.hasBorder) {
			g.setColor(menu.borderColor);
			if (manager == null) {
				g.drawRect(realX, realY, menu.itemWidth - 1, menu.itemHeight - 1);
			} else {
				g.drawRect(realX, realY, menu.itemWidth - 2, menu.itemHeight - 1);
			}
		}

		if (isFocused) {
			getFont().drawString(g, label, menu.focusFgColor, realX + 3 + menu.textDx, realY + menu.itemHeight / 2, GameConstants.VCENTER_LEFT_ANCHOR);
		} else {
			getFont().drawString(g, label, menu.forceGroundColor, realX + 3 + menu.textDx, realY + menu.itemHeight / 2, GameConstants.VCENTER_LEFT_ANCHOR);
		}

		if (items.size() > 0) {
			if (arowImage == null) {
				try {
					arowImage = Image.createImage(GameConstants.CORE_IMAGE_FOLDER + "/Muiten_trang.png");
				} catch (IOException e) {
				}
			}
			g.drawImage(arowImage, realX + menu.itemWidth - 6, realY + menu.itemHeight / 2, GameConstants.VCENTER_RIGHT_ANCHOR);
		}
		
		if ((items.size() > 0) && isShowChildrenMenuItem) {
			// Vẽ ảnh background
			if ((menu.itemBackgroundImage != null) && isShowChildrenMenuItem) {
				g.drawImage(menu.itemBackgroundImage, realX, realY, GameConstants.TOP_LEFT_ANCHOR);
			}
			
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

			if (isShowUpToDown) {
				g.drawImage(menuImage, drawMenuBbX, getMenuY(), GameConstants.TOP_LEFT_ANCHOR);
			} else {
				g.drawImage(menuImage, drawMenuBbX, getMenuY() + menu.itemHeight, GameConstants.BOTTOM_LEFT_ANCHOR);
			}

			for (int i = 0; i < items.size(); i++) {
				MenuItem item = (MenuItem) items.elementAt(i);
				updateChildPosition(item, i, isShowUpToDown);
				item.draw(g);
			}
		}
	}

	public int getMenuX() {
		return realX;
	}

	public int getMenuY() {
		return realY;
	}

	public static Image createMemuBgImage(int size) {
		Image topImage = ImageUtil.getImage("menu_tren.png");
		Image bottomImage = ImageUtil.getImage("menu_duoi.png");
		Image bodyImage = ImageUtil.getImage("menu_giua.png");
		bodyImage = ImageUtil.joinAndCreateImages(bodyImage, bodyImage.getWidth(), size - topImage.getHeight() - bottomImage.getHeight(), true);
		return ImageUtil.joinImages(new Image[]{topImage, bodyImage, bottomImage}, new int[]{0, 0, 0}, new int[]{0, topImage.getHeight(), topImage.getHeight() + bodyImage.getHeight()}, new int[]{GameConstants.TOP_LEFT_ANCHOR, GameConstants.TOP_LEFT_ANCHOR,
				GameConstants.TOP_LEFT_ANCHOR}, bodyImage.getWidth(), size, Color.GREEN_CODE);
	}

	public void detroy() {
		super.detroy();
		items = null;
		menu = null;
		label = null;
		parent = null;
		menuImage = null;
		arowImage = null;
	}
}
