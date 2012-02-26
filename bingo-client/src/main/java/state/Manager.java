package state;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import state.util.Color;

public class Manager {
	private Sprite baseSprite;
	private Sprite root;
	private Sprite last;
	private int backGroundColor = Color.WHITE_CODE;
	private GameForm parent;
	private Pointer pointer;
	private boolean useMouseMoveEvent = false;
	private Image backgroundImage;

	public Manager(GameForm parent) {
		this.parent = parent;
		baseSprite = new Sprite(this, 0, 0, GameConstants.SCREEN_WIDTH,
				GameConstants.SCREEN_HEIGHT);
		baseSprite.setDrawListener(new DrawListener() {
			public void paint(Sprite source, Graphics g) {
				Manager.this.parent.draw(g);
			}
		});
		root = baseSprite;
		last = root;
	}

	/**
	 * Sử dụng pointer trong State hiện tại
	 */
	public void enablePointer() {
		if (pointer == null) {
			pointer = new Pointer(this);
			if (useMouseMoveEvent) {
				pointer.useMouseMoveEvent();
			}
		}
	}

	/**
	 * Hủy sử dụng pointer
	 */
	public void disablePointer() {
		if (pointer != null) {
			remove(pointer);
			pointer.detroy();
			pointer = null;
		}
	}

	public Sprite getBaseSprite() {
		return baseSprite;
	}

	/**
	 * Lấy pointer đang xử dụng của manager
	 * 
	 * @return Pointer đang xử dụng của manager, trả về null nếu không có
	 *         pointer nào được xử dụng
	 */
	public Pointer getPointer() {
		return pointer;
	}

	/**
	 * Tự động cập nhật tất cả các thay đổi của các Sprite trong State lên
	 * Graphic. Phương thức này sẽ tự động cập nhật Graphic nên màn hình nếu
	 * tham số tự động cập nhật màn hình isAutoFlushGraphic được set là true
	 */
	public void updateFullScreen() {
		if (!parent.isRunning) {
			return;
		}

		Graphics g = getGraphics();
		if (g == null) {
			return;
		}

		g.setColor(backGroundColor);
		g
				.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
		g.fillRect(0, 0, GameConstants.SCREEN_WIDTH,
				GameConstants.SCREEN_HEIGHT);
		if (backgroundImage != null) {
			g.drawImage(backgroundImage, 0, 0, GameConstants.TOP_LEFT_ANCHOR);
		}

		if (root != null) {
			Sprite current = root;
			while (current != null) {
				g.setClip(current.getRealX(), current.getRealY(), current
						.getWidth(), current.getHeight());
				current.paint(g);
				current = current.next;
			}
		}
	}

	public void setBackgroundImage(Image backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	/**
	 * Lấy Graphic đang xử dụng hiện tại, có thể là Graphic màn hình hoặc Screen
	 * ảo đã được thiết lập
	 * 
	 * @return Graphic đang xử dụng
	 */
	public Graphics getGraphics() {
		Graphics g = null;
		if (parent.getScreen() != null) {
			g = parent.getScreen().getGraphics();
		} else {
			g = GameGlobal.systemCanvas.getGraphic();
		}
		return g;
	}

	/**
	 * Thiết lập mầu nền cho State
	 * 
	 * @param color
	 *            - Mầu nền
	 */
	public void setBackGroundColor(int color) {
		backGroundColor = color;
	}

	/**
	 * Lấy State hiện tại của manager này
	 * 
	 * @return State hiện tại của manager
	 */
	public GameForm getParent() {
		return parent;
	}

	public final void doKeyPressed(int keyCode) {
		if (root != null) {
			Sprite current = root;
			while (current != null) {
				if (current.keyListener != null) {
					current.keyListener.keyPressed(current, keyCode);
				}
				current = current.next;
			}
		}
	}

	public final void doKeyReleased(int keyCode) {
		if (root != null) {
			Sprite current = root;
			while (current != null) {
				if (current.keyListener != null) {
					current.keyListener.keyReleased(current, keyCode);
				}
				current = current.next;
			}
		}
	}

	public final void doKeyRepeated(int keyCode) {
		if (root != null) {
			Sprite current = root;
			while (current != null) {
				if (current.keyListener != null) {
					current.keyListener.keyRepeated(current, keyCode);
				}
				current = current.next;
			}
		}
	}

	public final void doMousePressed(int x, int y) {
		if (root != null) {
			Sprite current = last;
			while (current != null) {
				if ((current != pointer) && current.checkCollides(x, y)) {
					if (current.mouseListener != null) {
						current.mouseListener.mousePressed(current, x, y);
					}
					break;
				}
				current = current.previous;
			}
		}
	}

	public final void doMouseReleased(int x, int y) {
		if (root != null) {
			Sprite current = last;
			while (current != null) {
				if ((current != pointer) && current.checkCollides(x, y)) {
					if (current.mouseListener != null) {
						current.mouseListener.mouseReleased(current, x, y);
					}
					break;
				}
				current = current.previous;
			}
		}
	}

	public final void doMouseMoved(int x, int y) {
		if (root != null) {
			Sprite current = last;
			while (current != null) {
				if ((current != pointer) && current.checkCollides(x, y)) {
					if (current.mouseMoveListener != null) {
						pointer.setCurrentMoveOnItem(current);
						current.mouseMoveListener.mouseMoved(current, x, y);
					}
					break;
				}
				current = current.previous;
			}
		}
	}

	protected final void useMouseMoveEvent() {
		useMouseMoveEvent = true;
		if (pointer != null) {
			pointer.useMouseMoveEvent();
		}
	}

	/**
	 * Thêm 1 LinkedSprite vào danh sách quản lý của manager. Cần lưu ý là
	 * LinkedSprite thêm vào sau sẽ được vẽ phía trên các LinkedSprite thêm vào
	 * trước đó.
	 * 
	 * @param c
	 *            - LinkedSprite thêm vào danh sách quản lý
	 */
	public void append(Sprite c) {
		if (c.manager == this) {
			return;
		}

		if (root == null) {
			root = c;
			last = root;
		} else {
			last.next = c;
			c.previous = last;
			last = c;
		}
		c.manager = this;

		if (c.mouseMoveListener != null) {
			useMouseMoveEvent();
		}
	}

	public void insertToHead(Sprite c) {
		if (c.manager == this) {
			return;
		}

		if (root == null) {
			root = c;
			last = root;
		} else {
			c.next = root;
			root.previous = c;
			root = c;
		}
		c.manager = this;

		if (c.mouseMoveListener != null) {
			useMouseMoveEvent();
		}
	}

	/**
	 * Thêm 1 LinkedSprite vào danh sách quản lý của manager ở 1 vị trí sau
	 * LinkSprite source trong danh sách Cần lưu ý là các LinkedSprite được vẽ
	 * theo thứ tự index, LinkedSprite có index lớn hơn sẽ sẽ được vẽ phía trên.
	 * 
	 * @param newSprite
	 *            - LinkedSprite thêm vào danh sách quản lý
	 * @param source
	 *            - LinkedSprite để xác định thêm newSprite vào vị trí phía sau
	 */
	public void insertAfter(Sprite newSprite, Sprite source) {
		if (newSprite.manager == this) {
			return;
		}

		if (source.manager != this) {
			return;
		}

		newSprite.manager = this;
		if (source.next != null) {
			newSprite.next = source.next;
			newSprite.previous = source;
			source.next.previous = newSprite;
			source.next = newSprite;
		} else {
			source.next = newSprite;
			newSprite.previous = source;
		}

		if (newSprite.mouseMoveListener != null) {
			useMouseMoveEvent();
		}
	}

	/**
	 * Thêm 1 LinkedSprite vào danh sách quản lý của manager ở 1 vị trí trước
	 * LinkSprite source trong danh sách Cần lưu ý là các LinkedSprite được vẽ
	 * theo thứ tự index, LinkedSprite có index lớn hơn sẽ sẽ được vẽ phía trên.
	 * 
	 * @param newSprite
	 *            - LinkedSprite thêm vào danh sách quản lý
	 * @param source
	 *            - LinkedSprite để xác định thêm newSprite vào vị trí phía
	 *            trước
	 */
	public void insertBefore(Sprite newSprite, Sprite source) {
		if (newSprite.manager == this) {
			return;
		}

		if (source.manager != this) {
			return;
		}

		newSprite.manager = this;
		newSprite.previous = source.previous;
		newSprite.next = source;
		source.previous.next = newSprite;
		source.previous = newSprite;

		if (newSprite.mouseMoveListener != null) {
			useMouseMoveEvent();
		}
	}

	/**
	 * Bỏ 1 LinkedSprite ra khỏi danh sách quản lý của manager. Lưu ý là
	 * LinkedSprite đã được loại bỏ sẽ không được cập nhật hiển thị nữa
	 * 
	 * @param c
	 *            - LinkedSprite cần bỏ ra khỏi danh sách quản lý
	 */
	public void remove(Sprite c) {
		if ((c == null) || (c == baseSprite) || (c.manager != this)) {
			return;
		}

		if (root == c) {
			root = c.next;
		} else {
			c.previous.next = c.next;
		}

		if (last == c) {
			last = c.previous;
		} else {
			c.next.previous = c.previous;
		}

		c.previous = null;
		c.next = null;
		c.manager = null;
	}

	/**
	 * Loại bỏ tất cả các LinkedSprite đang quản lý ra khỏi danh sách
	 */
	public void removeAll() {
		if (pointer != null) {
			pointer.detroy();
			pointer = null;
		}
		useMouseMoveEvent = false;

		Sprite current = last;
		while (current != root) {
			current.manager = null;
			if (current.next != null) {
				current.next.previous = null;
				current.next = null;
			}
			current = current.previous;
		}
		if (root.next != null) {
			root.next.previous = null;
			root.next = null;
		}

		root = baseSprite;
		last = root;
	}

	/**
	 * Loại bỏ và detroy tất cả các LinkedSprite hiện tại trong danh sách
	 */
	public void detroyAll() {
		useMouseMoveEvent = false;
		backgroundImage = null;
		Sprite current = last;
		Sprite tmp;
		while (current != root) {
			tmp = current;
			current = current.previous;
			if (tmp.next != null) {
				tmp.next.previous = null;
				tmp.next = null;
			}

			if (tmp != baseSprite) {
				tmp.detroy();
			}
		}

		if (root != null) {
			if (root.next != null) {
				root.next.previous = null;
				root.next = null;
			}
		}

		root = baseSprite;
		last = root;
		pointer = null;
	}
}
