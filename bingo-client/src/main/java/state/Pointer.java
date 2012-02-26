package state;

import java.io.IOException;

import javax.microedition.lcdui.Image;

public class Pointer extends Sprite implements KeySpriteListener, TimerListener {
	private static final Image DEFAULT_ICON = createDefaultIcon();
	private int activeKeyCode = Key.FIRE;
	private int step;
	private int dx;
	private int dy;
	private boolean isAllowUseNumberKey = false;
	private Sprite lastMoveOnItem;
	private Sprite currentMoveOnItem;

	private int boundX;
	private int boundY;
	private int boundWidth = GameConstants.SCREEN_WIDTH;
	private int boundHeight = GameConstants.SCREEN_HEIGHT;

	/**
	 * Khởi tạo pointer
	 * 
	 * @param manager
	 *            - Manager quản lý
	 * @param systemCanvas
	 *            - SystemCanvas của hệ thống
	 */
	public Pointer(Manager manager) {
		super(DEFAULT_ICON, manager, GameConstants.SCREEN_WIDTH / 2,
				GameConstants.SCREEN_HEIGHT / 2);
		setKeyListener(this);
	}

	private static Image createDefaultIcon() {
		try {
			return Image.createImage(GameConstants.CORE_IMAGE_FOLDER
					+ "/p_arrow.png");
		} catch (IOException e) {
			return null;
		}
	}

	public void resetToDefaultImage() {
		setImage(DEFAULT_ICON);
	}

	public boolean isUseDefaultImage() {
		return getImage() == DEFAULT_ICON;
	}

	/**
	 * Lấy active code hiện tại đang xử dụng
	 * 
	 * @return active code
	 */
	public int getActiveKeyCode() {
		return activeKeyCode;
	}

	/**
	 * Thay đổi phím active của pointer (giống như Mouse Left của windows)
	 * 
	 * @param activeKeyCode
	 *            - active code cập nhật
	 */
	public void setActiveKeyCode(int activeKeyCode) {
		this.activeKeyCode = activeKeyCode;
	}

	protected final void useMouseMoveEvent() {
		setTimerListener(this);
	}

	public void setActivePosition(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}

	public int getPointerX() {
		return x + dx;
	}

	public int getPointerY() {
		return y + dy;
	}

	public boolean isAllowUseNumberKey() {
		return isAllowUseNumberKey;
	}

	public void setAllowUseNumberKey(boolean isAllowUseNumberKey) {
		this.isAllowUseNumberKey = isAllowUseNumberKey;
	}

	public void keyPressed(Sprite source, int keyCode) {
		if (Key.getGameKey(keyCode) == activeKeyCode) {
			manager.getParent().doMousePressed(x + dx, y + dy);
		}
	}

	public void keyReleased(Sprite source, int keyCode) {
		if (Key.getGameKey(keyCode) == activeKeyCode) {
			manager.getParent().doMouseReleased(x + dx, y + dy);
		}
	}

	public void keyRepeated(Sprite source, int keyCode) {
		keyCode = Key.getGameKey(keyCode);
		step = (int) ((System.currentTimeMillis() - GameGlobal.systemCanvas
				.getKeyRepeatHandle().getKeyRepeatStartTime()) >> 4L);
		int x = this.x + dx;
		int y = this.y + dy;

		switch (keyCode) {
		case Key.LEFT:
			if (x > boundX + step) {
				x -= step;
			} else {
				x = boundX;
			}
			break;
		case Key.RIGHT:
			if (boundX + boundWidth > x + step) {
				x += step;
			} else {
				x = boundX + boundWidth;
			}
			break;
		case Key.UP:
			if (y > boundY + step) {
				y -= step;
			} else {
				y = boundY;
			}
			break;
		case Key.DOWN:
			if (boundY + boundHeight > y + step) {
				y += step;
			} else {
				y = boundY + boundHeight;
			}
			break;
		}

		if (isAllowUseNumberKey) {
			switch (keyCode) {
			case Key.K_4: // Left
				if (x > boundX + step) {
					x -= step;
				} else {
					x = boundX;
				}
				break;
			case Key.K_6: // Right
				if (boundX + boundWidth > x + step) {
					x += step;
				} else {
					x = boundX + boundWidth;
				}
				break;
			case Key.K_2: // Up
				if (y > boundY + step) {
					y -= step;
				} else {
					y = boundY;
				}
				break;
			case Key.K_8: // Down
				if (boundY + boundHeight > y + step) {
					y += step;
				} else {
					y = boundY + boundHeight;
				}
				break;
			}
		}
		setPosition(x - dx, y - dy);
	}

	public void doTask() {
		if (manager != null) {
			currentMoveOnItem = null;
			manager.getParent().doMouseMoved(x + dx, y + dy);

			if ((lastMoveOnItem != null)
					&& (lastMoveOnItem != currentMoveOnItem)) {
				lastMoveOnItem.mouseMoveListener.mouseMoveOut(lastMoveOnItem, x
						+ dx, y + dy);
			}
			lastMoveOnItem = currentMoveOnItem;
			currentMoveOnItem = null;
		}
	}

	public void setCurrentMoveOnItem(Sprite currentMoveOnItem) {
		this.currentMoveOnItem = currentMoveOnItem;
	}

	public Sprite getCurrentMoveOnItem() {
		return lastMoveOnItem;
	}

	public void setBound(int x, int y, int width, int height) {
		boundX = x;
		boundY = y;
		boundWidth = width;
		boundHeight = height;
	}

	public void detroy() {
		super.detroy();
		lastMoveOnItem = null;
		currentMoveOnItem = null;
	}
}
