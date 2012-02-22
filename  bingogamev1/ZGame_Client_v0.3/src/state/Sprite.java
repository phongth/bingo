package state;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;


/**
 * Class dùng để lưu thông tin xử lý về các đối tượng Image và tự động cập nhật màn hình thông qua manager 
 * 
 * @author ptran2
 * @version 0.1
 */
public class Sprite {
	protected Sprite previous;
	protected Sprite next;
	protected Manager manager;
	protected Image image;
	
	protected KeySpriteListener keyListener;
	protected MouseListener mouseListener;
	protected MouseMoveListener mouseMoveListener;
	protected TimerListener timerListener;
	public DrawListener drawListener;
	
	protected byte id;
	protected int width;
	protected int height;
	protected int x;
	protected int y;
	protected boolean isVisible = true;
	protected int anchor = GameConstants.TOP_LEFT_ANCHOR;
	
	protected int realX;
	protected int realY;
	
	public Sprite() {
	}
	
	/**
	 * Constructor
	 * 
	 * @param image - the Image to use for Sprite
	 * @param x - Tọa độ x khởi tạo cho Sprite
	 * @param y - Tọa độ y khởi tạo cho Sprite
	 */
	public Sprite(Image image, int x, int y) {
		this.x = x;
		this.y = y;
		this.realX = x;
		this.realY = y;
		this.image = image;
		this.width = image.getWidth();
		this.height = image.getHeight();
	}
	
	public Sprite(Image image, int x, int y, int anchor) {
		this.x = x;
		this.y = y;
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.realX = calculateX(x, anchor, width);
		this.realY = calculateY(y, anchor, height);
		this.image = image;
		this.anchor = anchor;
	}
	
	/**
	 * Constructor
	 * 
	 * @param x - Tọa độ x của Sprite
	 * @param y - Tọa độ y của Sprite
	 * @param width - Chiều rộng của Sprite
	 * @param height - Chiều dài của Sprite
	 */
	public Sprite(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.realX = x;
		this.realY = y;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Constructor
	 * 
	 * @param image - the Image to use for Sprite
	 * @param manager - Manager dùng để quản lý việc hiển thị
	 * @param x - Tọa độ x khởi tạo cho Sprite
	 * @param y - Tọa độ y khởi tạo cho Sprite
	 */
	public Sprite(Image image, Manager manager, int x, int y) {
		manager.append(this);
		this.x = x;
		this.y = y;
		this.realX = x;
		this.realY = y;
		this.image = image;
		this.width = image.getWidth();
		this.height = image.getHeight();
	}
	
	public Sprite(Image image, Manager manager, int x, int y, int anchor) {
		manager.append(this);
		this.x = x;
		this.y = y;
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.realX = calculateX(x, anchor, width);
		this.realY = calculateY(y, anchor, height);
		this.image = image;
		this.anchor = anchor;
	}
	
	/**
	 * Constructor
	 * 
	 * @param manager - Manager dùng để quản lý việc hiển thị
	 */
	public Sprite(Manager manager) {
		manager.append(this);
	}
	
	/**
	 * Constructor
	 * 
	 * @param manager - Manager dùng để quản lý việc hiển thị
	 * @param width - Chiều rộng của Sprite
	 * @param height - Chiều dài của Sprite
	 */
	public Sprite(Manager manager, int width, int height) {
		manager.append(this);
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Constructor
	 * 
	 * @param manager - Manager dùng để quản lý việc hiển thị
	 * @param x - Tọa độ x của Sprite
	 * @param y - Tọa độ y của Sprite
	 * @param width - Chiều rộng của Sprite
	 * @param height - Chiều dài của Sprite
	 */
	public Sprite(Manager manager, int x, int y, int width, int height) {
		manager.append(this);
		this.x = x;
		this.y = y;
		this.realX = x;
		this.realY = y;
		this.width = width;
		this.height = height;
	}
	
	public byte getId() {
		return id;
	}

	public Sprite setId(byte id) {
		this.id = id;
		return this;
	}

	public Sprite setAnchor(int anchor) {
		this.anchor = anchor;
		this.realX = calculateX(x, anchor, width);
		this.realY = calculateY(y, anchor, height);
		return this;
	}

	/**
	 * Xác định class sẽ handle xử lý sự kiện phím
	 * 
	 * @param keyListener - Class handle xử lý phím 
	 */
	public Sprite setKeyListener(KeySpriteListener keyListener) {
		this.keyListener = keyListener;
		return this;
	}

	/**
	 * Xác định class sẽ handle xử lý sự kiện chuột
	 * 
	 * @param mouseListener - Class handle xử lý chuột
	 */
	public Sprite setMouseListener(MouseListener mouseListener) {
		this.mouseListener = mouseListener;
		return this;
	}

	public Sprite setMouseMoveListener(MouseMoveListener mouseMoveListener) {
		if (manager == null) {
			throw new IllegalArgumentException("setMouseMoveListener : manager is null");
		}
		
		this.mouseMoveListener = mouseMoveListener;
		manager.useMouseMoveEvent();
		return this;
	}

	/**
	 * Xác định class sẽ handle xử lý vẽ cho Sprite này
	 * 
	 * @param drawListener - Class handle xử lý vẽ 
	 */
	public Sprite setDrawListener(DrawListener drawListener) {
		this.drawListener = drawListener;
		return this;
	}
	
	public Sprite setTimerListener(TimerListener timerListener) {
		if (manager == null) {
			throw new IllegalArgumentException("setTimerListener : manager is null");
		}
		
		if (this.timerListener != null) {
			GameGlobal.systemCanvas.getTimer().removeTarget(this.timerListener);
			this.timerListener = null;
		}
		GameGlobal.systemCanvas.getTimer().addTarget(timerListener);
		this.timerListener = timerListener;
		return this;
	}
	
	public Sprite removeTimerListener() {
		if (manager == null) {
		  return this;
		}
		
		if (timerListener != null) {
			GameGlobal.systemCanvas.getTimer().removeTarget(timerListener);
			timerListener = null;
		}
		return this;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public Sprite setWidth(int width) {
		this.width = width;
		this.realX = calculateX(x, anchor, width);
		return this;
	}

	public Sprite setHeight(int height) {
		this.height = height;
		this.realY = calculateY(y, anchor, height);
		return this;
	}
	
	public Sprite setSize(int width, int height) {
		if (this.width != width) {
			setWidth(width);
		}
		if (this.height != height) {
			setHeight(height);
		}
		return this;
	}

	public int getRealX() {
		return realX;
	}

	public int getRealY() {
		return realY;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Manager getManager() {
		return manager;
	}

	protected int calculateX(int value, int anchor, int width) {
		switch(anchor) {
		case GameConstants.TOP_LEFT_ANCHOR:
		case GameConstants.VCENTER_LEFT_ANCHOR:
		case GameConstants.BOTTOM_LEFT_ANCHOR:
			return value;
		case GameConstants.TOP_HCENTER_ANCHOR:
		case GameConstants.CENTER_ANCHOR:
		case GameConstants.BOTTOM_HCENTER_ANCHOR:
			return value - width / 2;
		case GameConstants.TOP_RIGHT_ANCHOR:
		case GameConstants.VCENTER_RIGHT_ANCHOR:
		case GameConstants.BOTTOM_RIGHT_ANCHOR:
			return value - width;
		default:
			return value;
		}
	}
	
	protected int calculateY(int value, int anchor, int height) {
		switch(anchor) {
		case GameConstants.TOP_LEFT_ANCHOR:
		case GameConstants.TOP_HCENTER_ANCHOR:
		case GameConstants.TOP_RIGHT_ANCHOR:
			return value;
		case GameConstants.VCENTER_LEFT_ANCHOR:
		case GameConstants.CENTER_ANCHOR:
		case GameConstants.VCENTER_RIGHT_ANCHOR:
			return value - height / 2;
		case GameConstants.BOTTOM_LEFT_ANCHOR:
		case GameConstants.BOTTOM_HCENTER_ANCHOR:
		case GameConstants.BOTTOM_RIGHT_ANCHOR:
			return value - height;
		default:
			return value;
		}
	}
	
	public boolean isVisible() {
		return isVisible;
	}

	public Sprite setVisible(boolean isVisible) {
		if (this.isVisible != isVisible) {
			this.isVisible = isVisible;
		}
		return this;
	}

	/**
	 * Cập nhật tọa độ cho Sprite, vị trí mới của Sprite sẽ được tự động cập nhật vào Graphic
	 * 
	 * @param x - Tọa độ x
	 * @param y - Tọa độ y
	 */
	public Sprite setPosition(int x, int y) {
		if ((this.x == x) && (this.y == y)) {
		  return this;
		}
		
		this.x = x;
		this.y = y;
		this.realX = calculateX(x, anchor, width);
		this.realY = calculateY(y, anchor, height);
		return this;
	}
	
	/**
	 * Di chuyển Sprite 1 khoảng dx, dy , vị trí mới của Sprite sẽ được tự động cập nhật vào Graphic
	 * 
	 * @param dx - Khoảng di chuyển x
	 * @param dy - Khoảng di chuyển y
	 */
	public Sprite move(int dx, int dy) {
		if ((dx == 0) && (dy == 0)) {
			return this;
		}
		
		x += dx;
		y += dy;
		this.realX = calculateX(x, anchor, width);
		this.realY = calculateY(y, anchor, height);
		return this;
	}
	
	/**
	 * Thay đổi ảnh của Sprite
	 * 
	 * @param img - Ảnh mới cập nhật cho Sprite
	 * @param frameWidth - Chiều rộng của frame ảnh trong img
	 * @param frameHeight - Chiều dài của frame ảnh trong img
	 */
	public Sprite setImage(Image image) {
		setImage(image, true);
		return this;
	}
	
	public Sprite setImage(Image image, boolean changeSpriteSizeByImage) {
		if (image == this.image) {
		  return this;
		}
		
		if (image == null) {
			this.image = null;
			return this;
		}
		
		this.image = image;
		if (changeSpriteSizeByImage) {
			width = image.getWidth();
			height = image.getHeight();
			this.realX = calculateX(x, anchor, width);
			this.realY = calculateY(y, anchor, height);
		}
		return this;
	}
	
	public Image getImage() {
		return image;
	}
	
	/**
	 * Wrapper lại hàm paint(g) của Sprite, sử dụng cho class CustomSprite implement lại 
	 * 
	 * @param g - Graphic
	 */
	public Sprite paint(Graphics g) {
		if (!isVisible) {
		  return this;
		}
		
		if (image != null) {
			g.drawImage(image, x, y, anchor);
		} 
		if (drawListener != null) {
			drawListener.paint(this, g);
		}
		return this;
	}
	
	/**
	 * Kiểm tra va chạm với 1 image bằng cách kiểm tra đường bao 
	 * 
	 * @param image - the Image to test for collision
	 * @param x - Tọa độ x xác định của image
	 * @param y - Tọa độ y xác định của image
	 */
	public boolean checkCollides(Image image, int x, int y) {
		return checkCollides(x, y, image.getWidth(), image.getHeight());
	}
	
	/**
	 * Kiểm tra va chạm với 1 sprite khác bằng cách kiểm tra đường bao 
	 * 
	 * @param linkedSprite - the Image to test for collision
	 */
	public boolean checkCollides(Sprite linkedSprite) {
		if (!linkedSprite.isVisible) {
			return false;
		}
		
		if (linkedSprite == this) {
			return true;
		}
		
		return checkCollides(linkedSprite.getRealX(), linkedSprite.getRealY(), linkedSprite.getWidth(), linkedSprite.getHeight());
	}
	
	/**
	 * Kiểm tra va chạm với 1 điểm bằng cách kiểm tra xem điểm tương ứng trên Sprite có trong suốt hay không, nếu là điểm không trong suốt thì được coi là va chạm 
	 * 
	 * @param x - Tọa độ x của điểm cần kiểm tra
	 * @param y - Tọa độ y của điểm cần kiểm tra
	 */
	public boolean checkCollides(int x, int y) {
		if (!isVisible) {
			return false;
		}
		
		// Chuyển từ tọa độ màn hình (x, y) về tọa độ của ảnh (x1, y1)
		int x1 = x - getRealX();
		int y1 = y - getRealY();
		
		// Nếu tọa độ nằm ngoài ảnh thì coi như không va chạm
		if ((0 > x1) || (x1 > width) || (0 > y1) || (y1 > height)) {
			return false;
		}
		
		if (image == null) {
			return true;
		}
		
		// Kiểm tra xem điểm ảnh cần xét có trong suốt không
		Image tmp = Image.createImage(1, 1);
		Graphics g = tmp.getGraphics();
		g.drawImage(image, -x1, -y1, GameConstants.TOP_LEFT_ANCHOR);
		int[] rgbData = new int[1];
		tmp.getRGB(rgbData, 0, 1, 0, 0, 1, 1);
		int value = rgbData[0];
		
		if ((value == 0x00000000) || ((value & 0xFF000000) == 0x00000000) || (value == 0xFFFFFFFF)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Kiểm tra va chạm với 1 khoảng chữ nhật bằng cách kiểm tra đường bao 
	 * 
	 * @param image - the Image to test for collision
	 * @param x - Tọa độ x xác định của image
	 * @param y - Tọa độ y xác định của image
	 * @param width - Chiều rộng hình chữ nhật
	 * @param height - Chiều dài hình chữ nhật
	 */
	public boolean checkCollides(int x, int y, int width, int height) {
		if (!isVisible) {
			return false;
		}
		
		if ((x + width < getRealX()) || (getRealX() + getWidth() < x) || (y + height < getRealY()) || (getRealY() + getHeight() < y)) {
			return false;
		}
		return true;
	}
	
	public void detroy() {
		removeTimerListener();
		image = null;
		drawListener = null;
		timerListener = null;
		mouseListener = null;
		mouseMoveListener = null;
		keyListener = null;
		manager = null;
	}
}
