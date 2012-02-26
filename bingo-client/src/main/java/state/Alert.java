package state;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import state.font.FontManager;
import state.font.ImageText;
import state.util.Color;
import state.util.ImageUtil;

public class Alert extends GameForm implements TimerListener {
	/** Kiểu Alert với 1 nút OK */
	public static final byte OK_TYPE = 0;

	/** Kiểu Alert với 2 nút Yes và No */
	public static final byte YES_NO_TYPE = 1;

	/** Alert kiểu loading form */
	public static final byte LOADING_FORM = 2;

	/** Alert kiểu loading và có nút cancel để dừng xử lý loading */
	public static final byte LOADING_WITH_CANCEL_BUTTON_TYPE = 3;

	/** Alert kiểu có danh sách các nút do người dùng quyết định */
	private static final byte CUSTOM_BUTTON_TYPE = 4;

	/** Alert kiểu cho phép người dùng input number */
	public static final byte INPUT_NUMBER_TYPE = 5;

	/** Alert kiểu cho phép người dùng config table */
	private static final byte CUSTOM_UI_TYPE = 6;

	/** Alert kiểu loading và không có button */
	public static final byte LOADING_WITH_NO_BUTTON_TYPE = 7;

	/** Sắp xếp ngang */
	public static final byte HORIZONTAL_ALINE = 0;

	/** Sắp xếp dọc */
	public static final byte VERTICAL_ALINE = 1;

	public static final byte BUTTON_WIDTH_SMALL = 0;
	public static final byte BUTTON_WIDTH_LARGE = 1;

	public static final byte ALERT_ID_NOT_DEFINE = -1;
	public static final byte OK_BUTTON = 0;
	public static final byte YES_BUTTON = 1;
	public static final byte NO_BUTTON = 2;
	public static final byte LOADING_DONE_EVENT = 3;
	public static final byte CANCEL_BUTTON = 4;

	private static final byte MAX_NUMBER_LEN = 5;
	private static final byte BUTTON_DISTANCE_HORIZONTAL = 15;
	private static final byte BUTTON_DISTANCE_VERTICAL = 5;

	private Image parentScreen;
	private Image bgImage;
	private Image buttonImage;
	private Image focusedButtonImage;
	private Image coverImage;
	private Image loadingImage1;
	private Image loadingImage2;
	private Image logoImage;
	private Image bgLoadingImage;

	private int buttonIndex;
	private int alertX;
	private int alertY;
	private int messageY;
	private ImageText vnText;
	private boolean isAutoHideInEvent;
	private int count;

	private static Alert instance;
	private AlertInfo alertInfo = new AlertInfo();
	private Vector alertInfoQueue = new Vector();
	protected GameForm parent;

	public Alert() {
		instance = this;
	}

	public static Alert instance() {
		return instance;
	}

	public Alert setListenner(AlertListener listenner) {
		alertInfo.listenner = listenner;
		return this;
	}

	/**
	 * Hiển thị Alert
	 * 
	 * @param parent
	 *            - State hiển thị nền
	 * @param listenner
	 *            - Listener để handle sự kiện Alert, bằng null nếu sự kiện trả
	 *            về cho parent
	 * @param alertType
	 *            - Kiểu Alert
	 * @param alertMessage
	 *            - chuỗi thông báo, bằng null hoặc empty nếu không có message
	 */
	public Alert showAlert(AlertListener listenner, byte alertType,
			String alertMessage) {
		return showAlert(listenner, alertType, alertMessage, 0);
	}

	/**
	 * Hiển thị Alert
	 * 
	 * @param parent
	 *            - State hiển thị nền
	 * @param listenner
	 *            - Listener để handle sự kiện Alert, bằng null nếu sự kiện trả
	 *            về cho parent
	 * @param alertType
	 *            - Kiểu Alert
	 * @param alertMessage
	 *            - chuỗi thông báo, bằng null hoặc empty nếu không có message
	 * @param alertId
	 *            - Id cho lần gọi alert này, dùng để nhận biết cho hàm
	 *            alertEventPerform trả về cho State. Giá trị của alertId do
	 *            người dùng thiết lập.
	 */
	public Alert showAlert(AlertListener listenner, byte alertType,
			String alertMessage, int alertId) {
		return showAlert(listenner, alertType, new String[] { alertMessage },
				alertId);
	}

	/**
	 * Hiển thị Alert
	 * 
	 * @param parent
	 *            - State hiển thị nền
	 * @param listenner
	 *            - Listener để handle sự kiện Alert, bằng null nếu sự kiện trả
	 *            về cho parent
	 * @param alertType
	 *            - Kiểu Alert
	 * @param alertMessages
	 *            - mảng chuỗi thông báo, bằng null hoặc empty nếu không có
	 *            message nào
	 */
	public Alert showAlert(AlertListener listenner, byte alertType,
			String[] alertMessages) {
		return showAlert(listenner, alertType, alertMessages, 0);
	}

	/**
	 * Hiển thị Alert
	 * 
	 * @param parent
	 *            - State hiển thị nền
	 * @param listenner
	 *            - Listener để handle sự kiện Alert, bằng null nếu sự kiện trả
	 *            về cho parent
	 * @param alertType
	 *            - Kiểu Alert
	 * @param alertMessages
	 *            - mảng chuỗi thông báo, bằng null hoặc empty nếu không có
	 *            message nào
	 * @param alertId
	 *            - Id cho lần gọi alert này, dùng để nhận biết cho hàm
	 *            alertEventPerform trả về cho State. Giá trị của alertId do
	 *            người dùng thiết lập.
	 */
	public Alert showAlert(AlertListener listenner, byte alertType,
			String[] alertMessages, int alertId) {
		AlertInfo info = new AlertInfo();
		info.alertType = alertType;
		parent = findParent();
		if (listenner == null) {
			info.listenner = parent;
		} else {
			info.listenner = listenner;
		}

		info.alertId = alertId;
		info.alertMessages.removeAllElements();
		if (alertMessages != null) {
			for (int i = 0; i < alertMessages.length; i++) {
				info.alertMessages.addElement(alertMessages[i]);
			}
		}
		return showAlert(info);
	}

	/**
	 * Hiển thị Alert với danh sách các nút do người dùng quyết định
	 * 
	 * @param parent
	 *            - State hiển thị nền
	 * @param listenner
	 *            - Listener để handle sự kiện Alert, bằng null nếu sự kiện trả
	 *            về cho parent
	 * @param buttonLabels
	 *            - Danh sách các nút cần hiển thị
	 * @param buttonWidth
	 *            - Kiểu chiều rộng của mỗi button
	 * @param alertMessages
	 *            - Mảng chuỗi thông báo, bằng null hoặc empty nếu không có
	 *            message nào
	 * @param alertId
	 *            - Id cho lần gọi alert này, dùng để nhận biết cho hàm
	 *            alertEventPerform trả về cho State. Giá trị của alertId do
	 *            người dùng thiết lập.
	 * @param alineType
	 *            - Kiểu sắp xếp ngang hay dọc (HORIZONTAL_ALINE hoặc
	 *            VERTICAL_ALINE)
	 */
	public Alert showCustomButtonAlert(AlertListener listenner,
			String[] buttonLabels, byte buttonWidth, String[] alertMessages,
			int alertId, byte alineType) {
		AlertInfo info = new AlertInfo();
		info.alertType = CUSTOM_BUTTON_TYPE;
		parent = findParent();
		if (listenner == null) {
			info.listenner = parent;
		} else {
			info.listenner = listenner;
		}

		info.alertMessages.removeAllElements();
		if (alertMessages != null) {
			for (int i = 0; i < alertMessages.length; i++) {
				info.alertMessages.addElement(alertMessages[i]);
			}
		}
		info.alertId = alertId;
		info.buttonLabels = buttonLabels;
		info.alineType = alineType;
		info.buttonWidth = buttonWidth;
		return showAlert(info);
	}

	/**
	 * Hiển thị Alert với danh sách các nút do người dùng quyết định
	 * 
	 * @param parent
	 *            - State hiển thị nền
	 * @param listenner
	 *            - Listener để handle sự kiện Alert, bằng null nếu sự kiện trả
	 *            về cho parent
	 * @param buttonLabels
	 *            - Danh sách các nút cần hiển thị
	 * @param buttonWidth
	 *            - Kiểu chiều rộng của mỗi button
	 * @param alertMessage
	 *            - Chuỗi thông báo, bằng null hoặc empty nếu không có message
	 * @param alertId
	 *            - Id cho lần gọi alert này, dùng để nhận biết cho hàm
	 *            alertEventPerform trả về cho State. Giá trị của alertId do
	 *            người dùng thiết lập.
	 * @param alineType
	 *            - Kiểu sắp xếp ngang hay dọc (HORIZONTAL_ALINE hoặc
	 *            VERTICAL_ALINE)
	 */
	public Alert showCustomButtonAlert(AlertListener listenner,
			String[] buttonLabels, byte buttonWidth, String alertMessage,
			int alertId, byte alineType) {
		AlertInfo info = new AlertInfo();
		info.alertType = CUSTOM_BUTTON_TYPE;
		parent = findParent();
		if (listenner == null) {
			info.listenner = parent;
		} else {
			info.listenner = listenner;
		}

		info.alertMessages.removeAllElements();
		if (alertMessage != null) {
			info.alertMessages.addElement(alertMessage);
		}
		info.alertId = alertId;
		info.buttonLabels = buttonLabels;
		info.alineType = alineType;
		info.buttonWidth = buttonWidth;
		return showAlert(info);
	}

	public Alert showCustomUIAlert(AlertListener listenner,
			Sprite[] customUISprites, String[] buttonLabels, byte buttonWidth,
			int alertId, byte alineType) {
		AlertInfo info = new AlertInfo();
		info.alertType = CUSTOM_UI_TYPE;
		info.customUISprites = customUISprites;
		parent = findParent();
		if (listenner == null) {
			info.listenner = parent;
		} else {
			info.listenner = listenner;
		}

		info.alertMessages.removeAllElements();
		info.alertId = alertId;
		info.buttonLabels = buttonLabels;
		info.alineType = alineType;
		info.buttonWidth = buttonWidth;
		return showAlert(info);
	}

	private GameForm findParent() {
		GameForm parent = GameGlobal.systemCanvas.frmCurrent;
		if (parent == this) {
			return this.parent;
		}

		if (parent == GameGlobal.systemCanvas.transformer) {
			return GameGlobal.systemCanvas.transformer.nextState;
		}
		return parent;
	}

	public boolean isShowing() {
		return GameGlobal.systemCanvas.frmCurrent == this;
	}

	public int getAlertType() {
		return alertInfo.alertType;
	}

	public int getAlertId() {
		return alertInfo.alertId;
	}

	public Alert checkToShowQueueAlert() {
		if (GameGlobal.systemCanvas.isInTransforming()
				|| GameGlobal.systemCanvas.isAlertShowing()) {
			return this;
		}

		if (alertInfoQueue.size() > 0) {
			AlertInfo info = (AlertInfo) alertInfoQueue.elementAt(0);
			alertInfoQueue.removeElementAt(0);
			showAlert(info);
		}
		return this;
	}

	private Alert showAlert(AlertInfo info) {
		if (GameGlobal.systemCanvas.isInTransforming()
				|| (GameGlobal.systemCanvas.isAlertShowing()
						&& alertInfo.alertType != LOADING_FORM
						&& alertInfo.alertType != LOADING_WITH_CANCEL_BUTTON_TYPE && alertInfo.alertType != LOADING_WITH_NO_BUTTON_TYPE)) {
			alertInfoQueue.addElement(info);
			return this;
		}

		alertInfo.copyDataFrom(info);
		if (parent == null || parent == GameGlobal.systemCanvas.transformer) {
			parent = findParent();
		}
		buttonIndex = 0;
		count = 0;
		if (alertInfo.alertType == INPUT_NUMBER_TYPE) {
			alertInfo.alertMessages.addElement("");
			alertInfo.alertMessages.addElement("");
		}

		messageY = 35;
		switch (alertInfo.alertType) {
		case OK_TYPE:
			alertInfo.buttonWidth = BUTTON_WIDTH_SMALL;
			alertInfo.buttonLabels = new String[] { "Đồng ý" };
			break;
		case YES_NO_TYPE:
			alertInfo.buttonWidth = BUTTON_WIDTH_SMALL;
			alertInfo.buttonLabels = new String[] { "Đồng ý", "Hủy" };
			break;
		case LOADING_FORM:
			alertInfo.buttonLabels = new String[0];
			break;
		case LOADING_WITH_CANCEL_BUTTON_TYPE:
			alertInfo.buttonWidth = BUTTON_WIDTH_SMALL;
			alertInfo.buttonLabels = new String[] { "Dừng" };
			break;
		case CUSTOM_BUTTON_TYPE:
			if (alertInfo.alineType == VERTICAL_ALINE) {
				messageY = 11;
			}
			break;
		case INPUT_NUMBER_TYPE:
			alertInfo.alertId = 0;
			alertInfo.buttonLabels = new String[] { "Đồng ý" };
			break;
		}

		loadImage();
		alertX = (GameConstants.SCREEN_WIDTH - bgImage.getWidth()) / 2;
		alertY = (GameConstants.SCREEN_HEIGHT - bgImage.getHeight()) / 2;
		vnText = FontManager.getFont(FontManager.FONT_SIZE_8);

		if (parent != this) {
			parent.setScreen(parentScreen);
		}
		setTimerListener(this);
		isRunning = true;
		GameGlobal.systemCanvas.changeHandleToAlert();
		return this;
	}

	private void loadImage() {
		try {
			bgImage = Image.createImage(GameConstants.CORE_IMAGE_FOLDER
					+ "/bg_popup.png");
			coverImage = Image.createImage(GameConstants.CORE_IMAGE_FOLDER
					+ "/pxiel_nen.png");
			coverImage = ImageUtil.joinAndCreateImages(coverImage,
					GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT,
					true);
			if (alertInfo.buttonWidth == BUTTON_WIDTH_SMALL) {
				buttonImage = Image.createImage(GameConstants.CORE_IMAGE_FOLDER
						+ "/button_nho.png");
				focusedButtonImage = Image
						.createImage(GameConstants.CORE_IMAGE_FOLDER
								+ "/button_nho_focus.png");
			} else {
				buttonImage = Image.createImage(GameConstants.CORE_IMAGE_FOLDER
						+ "/button_to.png");
				focusedButtonImage = Image
						.createImage(GameConstants.CORE_IMAGE_FOLDER
								+ "/button_to_focus.png");
			}

			if ((alertInfo.alertType == LOADING_FORM)
					|| (alertInfo.alertType == LOADING_WITH_CANCEL_BUTTON_TYPE)) {
				loadingImage1 = Image
						.createImage(GameConstants.CORE_IMAGE_FOLDER
								+ "/loading_1.png");
				loadingImage2 = Image
						.createImage(GameConstants.CORE_IMAGE_FOLDER
								+ "/loading_2.png");
				logoImage = Image.createImage(GameConstants.CORE_IMAGE_FOLDER
						+ "/logoMobiplay.png");
			} else if (alertInfo.alertType == LOADING_WITH_NO_BUTTON_TYPE) {
				loadingImage1 = Image
						.createImage(GameConstants.CORE_IMAGE_FOLDER
								+ "/loading_1.png");
				loadingImage2 = Image
						.createImage(GameConstants.CORE_IMAGE_FOLDER
								+ "/loading_2.png");
				bgLoadingImage = Image.createImage(GameConstants.IMAGE_FOLDER
						+ "/bg_loading.png");
			}
		} catch (IOException e) {
		}
		parentScreen = Image.createImage(GameConstants.SCREEN_WIDTH,
				GameConstants.SCREEN_HEIGHT);
	}

	/**
	 * Lấy State hiển thị nền của Alert
	 * 
	 * @return State là parent của Alert
	 */
	public GameForm getParent() {
		return parent;
	}

	public void doTask() {
		if (GameGlobal.systemCanvas.isAlertShowing()) {
			count++;
			if (alertInfo.alertType == LOADING_WITH_NO_BUTTON_TYPE
					&& count >= alertInfo.alertTimeOut) {
				GameGlobal.systemCanvas.hideAlert();
				alertInfo.listenner.alertEventPerform(alertInfo.alertType, 0,
						alertInfo.alertId); // Khi timeout thì báo lại sự kiện
											// ra ngoài
			}
		} else {
			destroy();
		}
	}

	public void draw(Graphics g) {
		// Vẽ nền
		if ((alertInfo.alertType == LOADING_FORM)
				|| (alertInfo.alertType == LOADING_WITH_CANCEL_BUTTON_TYPE)) {
			g.setColor(0x4F0202);
			g.fillRect(0, 0, GameConstants.SCREEN_WIDTH,
					GameConstants.SCREEN_HEIGHT);
		} else if (alertInfo.alertType == LOADING_WITH_NO_BUTTON_TYPE) {
			parent.updateFullScreen();
			g.drawImage(parentScreen, 0, 0, GameConstants.TOP_LEFT_ANCHOR);
			g.drawImage(coverImage, 0, 0, GameConstants.TOP_LEFT_ANCHOR);
		} else {
			parent.updateFullScreen();
			g.drawImage(parentScreen, 0, 0, GameConstants.TOP_LEFT_ANCHOR);
			g.drawImage(coverImage, 0, 0, GameConstants.TOP_LEFT_ANCHOR);
			g.drawImage(bgImage, alertX, alertY, GameConstants.TOP_LEFT_ANCHOR);
		}

		// Viết message
		if ((alertInfo.alertType != LOADING_FORM)
				&& (alertInfo.alertType != LOADING_WITH_CANCEL_BUTTON_TYPE)
				&& (alertInfo.alertType != LOADING_WITH_NO_BUTTON_TYPE)) {
			for (int i = 0; i < alertInfo.alertMessages.size(); i++) {
				vnText.drawString(g, String.valueOf(alertInfo.alertMessages
						.elementAt(i)), Color.WHITE_CODE,
						GameConstants.SCREEN_WIDTH / 2, alertY + messageY + 16
								* i, GameConstants.TOP_HCENTER_ANCHOR);
			}
		}

		// Draw loading bar
		if ((alertInfo.alertType == LOADING_FORM)
				|| (alertInfo.alertType == LOADING_WITH_CANCEL_BUTTON_TYPE)) {
			int index = count % 11;
			int x = 0;
			int y = 0;
			if (GameConstants.IS_240x320_SCREEN) {
				g.drawImage(logoImage, GameConstants.SCREEN_WIDTH / 2, 76,
						GameConstants.TOP_HCENTER_ANCHOR);
				vnText.drawString(g, "Đang tải...", Color.WHITE_CODE,
						GameConstants.SCREEN_WIDTH / 2, 264,
						GameConstants.TOP_HCENTER_ANCHOR);
				x = 62;
				y = 247;
			} else {
				g.drawImage(logoImage, GameConstants.SCREEN_WIDTH / 2, 32,
						GameConstants.TOP_HCENTER_ANCHOR);
				vnText.drawString(g, "Đang tải...", Color.WHITE_CODE,
						GameConstants.SCREEN_WIDTH / 2, 193,
						GameConstants.TOP_HCENTER_ANCHOR);
				x = 101;
				y = 172;
			}

			for (int i = 1; i < 11; i++) {
				if (i <= index) {
					g.drawImage(loadingImage1, x
							+ (loadingImage1.getWidth() + 1) * (i - 1), y,
							GameConstants.TOP_LEFT_ANCHOR);
				} else {
					g.drawImage(loadingImage2, x
							+ (loadingImage1.getWidth() + 1) * (i - 1), y,
							GameConstants.TOP_LEFT_ANCHOR);
				}
			}
		} else if (alertInfo.alertType == LOADING_WITH_NO_BUTTON_TYPE) {
			int x = 0;
			int index = count % 11;
			g.drawImage(bgLoadingImage, GameConstants.SCREEN_WIDTH / 2,
					GameConstants.SCREEN_HEIGHT / 2 - 2,
					GameConstants.CENTER_ANCHOR);
			if (GameConstants.IS_240x320_SCREEN) {
				if (alertInfo.alertMessages.size() > 0) {
					vnText.drawString(g, String.valueOf(alertInfo.alertMessages
							.elementAt(0)), Color.WHITE_CODE,
							GameConstants.SCREEN_WIDTH / 2,
							GameConstants.SCREEN_HEIGHT / 2 + 3,
							GameConstants.TOP_HCENTER_ANCHOR);
				}
				x = 62;
			} else {
				if (alertInfo.alertMessages.size() > 0) {
					vnText.drawString(g, String.valueOf(alertInfo.alertMessages
							.elementAt(0)), Color.WHITE_CODE,
							GameConstants.SCREEN_WIDTH / 2,
							GameConstants.SCREEN_HEIGHT / 2 + 3,
							GameConstants.TOP_HCENTER_ANCHOR);
				}
				x = 101;
			}
			for (int i = 1; i < 11; i++) {
				if (i <= index) {
					g.drawImage(loadingImage1, x
							+ (loadingImage1.getWidth() + 1) * (i - 1),
							GameConstants.SCREEN_HEIGHT / 2 - 15,
							GameConstants.TOP_LEFT_ANCHOR);
				} else {
					g.drawImage(loadingImage2, x
							+ (loadingImage1.getWidth() + 1) * (i - 1),
							GameConstants.SCREEN_HEIGHT / 2 - 15,
							GameConstants.TOP_LEFT_ANCHOR);
				}
			}
		} else if (alertInfo.alertType == INPUT_NUMBER_TYPE) { // Draw Input
																// text field
			g.setColor(Color.WHITE_CODE);
			g.fillRect(GameConstants.SCREEN_WIDTH / 2 - 50, alertY + 15
					+ (alertInfo.alertMessages.size() - 2) * 20, 100, 18);
			vnText.drawString(g, String.valueOf(alertInfo.alertId),
					Color.BLUE_CODE, GameConstants.SCREEN_WIDTH / 2 - 48,
					alertY + 13 + (alertInfo.alertMessages.size() - 2) * 20,
					GameConstants.TOP_LEFT_ANCHOR);
			vnText.drawString(g, "Xóa", Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH - 10, alertY - 5
							+ alertInfo.alertMessages.size() * 20,
					GameConstants.TOP_RIGHT_ANCHOR);
		} else if (alertInfo.alertType == CUSTOM_UI_TYPE) {
			if (alertInfo.customUISprites != null) {
				for (int i = 0; i < alertInfo.customUISprites.length; i++) {
					alertInfo.customUISprites[i].paint(g);
				}
			}
		}

		// Draw button
		switch (alertInfo.alertType) {
		case OK_TYPE:
		case YES_NO_TYPE:
		case LOADING_WITH_CANCEL_BUTTON_TYPE:
		case INPUT_NUMBER_TYPE:
			drawHorizontalAlineButton(g);
			break;
		case CUSTOM_BUTTON_TYPE:
		case CUSTOM_UI_TYPE:
			if (alertInfo.alineType == HORIZONTAL_ALINE) {
				drawHorizontalAlineButton(g);
			} else if (alertInfo.alineType == VERTICAL_ALINE) {
				drawVerticalAlineButton(g);
			}
		}
	}

	private void drawHorizontalAlineButton(Graphics g) {
		int leftX = (GameConstants.SCREEN_WIDTH - buttonImage.getWidth()
				* alertInfo.buttonLabels.length - (alertInfo.buttonLabels.length - 1)
				* BUTTON_DISTANCE_HORIZONTAL) / 2;
		int x = leftX;
		for (int i = 0; i < alertInfo.buttonLabels.length; i++) {
			if (i == buttonIndex) {
				g.drawImage(focusedButtonImage, x, alertY + 100,
						GameConstants.TOP_LEFT_ANCHOR);
			} else {
				g.drawImage(buttonImage, x, alertY + 100,
						GameConstants.TOP_LEFT_ANCHOR);
			}
			vnText.drawString(g, alertInfo.buttonLabels[i], Color.WHITE_CODE, x
					+ buttonImage.getWidth() / 2, alertY + 100
					+ buttonImage.getHeight() / 2, GameConstants.CENTER_ANCHOR);
			x += buttonImage.getWidth() + BUTTON_DISTANCE_HORIZONTAL;
		}
	}

	private void drawVerticalAlineButton(Graphics g) {
		int topY = alertY + 65;
		int y = topY;
		for (int i = 0; i < alertInfo.buttonLabels.length; i++) {
			if (i == buttonIndex) {
				g.drawImage(focusedButtonImage, GameConstants.SCREEN_WIDTH / 2,
						y, GameConstants.TOP_HCENTER_ANCHOR);
			} else {
				g.drawImage(buttonImage, GameConstants.SCREEN_WIDTH / 2, y,
						GameConstants.TOP_HCENTER_ANCHOR);
			}
			vnText.drawString(g, alertInfo.buttonLabels[i], Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH / 2, y + buttonImage.getHeight()
							/ 2, GameConstants.CENTER_ANCHOR);
			y += buttonImage.getHeight() + BUTTON_DISTANCE_VERTICAL;
		}
	}

	/**
	 * Thiết lập số % hiện tại của loading Alert, nếu đạt 100% thì loading Alert
	 * sẽ tự động callback lại hàm alertEventPerform của State
	 * 
	 * @param loadingPercent
	 *            - Số % hiện tại
	 */
	public void setLoadingPercent(int loadingPercent) {
		if (loadingPercent >= 100) {
			if (parent != null) {
				parent.alertEventPerform(alertInfo.alertType,
						LOADING_DONE_EVENT, alertInfo.alertId);
			}
		}
	}

	public void setAutoHideInEvent(boolean isAutoHideInEvent) {
		this.isAutoHideInEvent = isAutoHideInEvent;
	}

	public int getAlertTimeOut() {
		return alertInfo.alertTimeOut;
	}

	public Alert setAlertTimeOut(int alertTimeOut) {
		this.alertInfo.alertTimeOut = alertTimeOut
				/ GameGlobal.systemCanvas.getTimer().getDelay();
		return this;
	}

	public void keyPressed(int keyCode) {
		keyCode = Key.getGameKey(keyCode);
		if (alertInfo.customUISprites != null) {
			for (int i = 0; i < alertInfo.customUISprites.length; i++) {
				if (alertInfo.customUISprites[i].keyListener != null) {
					alertInfo.customUISprites[i].keyListener.keyPressed(
							alertInfo.customUISprites[i], keyCode);
				}
			}
		}
	}

	public void keyRepeated(int keyCode) {
		keyCode = Key.getGameKey(keyCode);
		if (alertInfo.customUISprites != null) {
			for (int i = 0; i < alertInfo.customUISprites.length; i++) {
				if (alertInfo.customUISprites[i].keyListener != null) {
					alertInfo.customUISprites[i].keyListener.keyRepeated(
							alertInfo.customUISprites[i], keyCode);
				}
			}
		}
	}

	public void keyReleased(int keyCode) {
		if ((alertInfo.buttonLabels == null)
				|| (alertInfo.buttonLabels.length == 0)) {
			return;
		}

		keyCode = Key.getGameKey(keyCode);
		if (alertInfo.customUISprites != null) {
			for (int i = 0; i < alertInfo.customUISprites.length; i++) {
				if (alertInfo.customUISprites[i].keyListener != null) {
					alertInfo.customUISprites[i].keyListener.keyReleased(
							alertInfo.customUISprites[i], keyCode);
				}
			}
		}

		switch (keyCode) {
		case Key.UP:
			buttonIndex--;
			if (buttonIndex < 0) {
				buttonIndex = (byte) (alertInfo.buttonLabels.length - 1);
			}
			break;
		case Key.DOWN:
			buttonIndex++;
			buttonIndex = (byte) (buttonIndex % alertInfo.buttonLabels.length);
			break;
		case Key.LEFT:
			buttonIndex--;
			if (buttonIndex < 0) {
				buttonIndex = (byte) (alertInfo.buttonLabels.length - 1);
			}
			break;
		case Key.RIGHT:
			buttonIndex++;
			buttonIndex = (byte) (buttonIndex % alertInfo.buttonLabels.length);
			break;
		case Key.FIRE:
			if (alertInfo.listenner == null) {
				GameGlobal.systemCanvas.hideAlert();
				return;
			}
			if (isAutoHideInEvent) {
				GameGlobal.systemCanvas.hideAlert();
			}
			switch (alertInfo.alertType) {
			case OK_TYPE:
				alertInfo.listenner.alertEventPerform(alertInfo.alertType,
						OK_BUTTON, alertInfo.alertId);
				break;
			case YES_NO_TYPE:
				switch (buttonIndex) {
				case 0:
					alertInfo.listenner.alertEventPerform(alertInfo.alertType,
							YES_BUTTON, alertInfo.alertId);
					break;
				case 1:
					alertInfo.listenner.alertEventPerform(alertInfo.alertType,
							NO_BUTTON, alertInfo.alertId);
					break;
				}
				break;
			case LOADING_WITH_CANCEL_BUTTON_TYPE:
				alertInfo.listenner.alertEventPerform(alertInfo.alertType,
						CANCEL_BUTTON, alertInfo.alertId);
				break;
			case CUSTOM_BUTTON_TYPE:
			case CUSTOM_UI_TYPE:
				alertInfo.listenner.alertEventPerform(alertInfo.alertType,
						buttonIndex, alertInfo.alertId);
				break;
			case INPUT_NUMBER_TYPE:
				alertInfo.listenner.alertEventPerform(alertInfo.alertType,
						buttonIndex, alertInfo.alertId);
				break;
			}
			break;
		case Key.SOFT_RIGHT:
			String str = String.valueOf(alertInfo.alertId);
			if (str.length() < 2) {
				alertInfo.alertId = 0;
			} else {
				alertInfo.alertId = Integer.parseInt(str.substring(0, str
						.length() - 1));
			}
			break;
		default:
			if (alertInfo.alertType == INPUT_NUMBER_TYPE) {
				if ((Key.K_0 <= keyCode) && (keyCode <= Key.K_9)) {
					int value = keyCode - Key.K_0;
					if (alertInfo.alertId == 0) {
						alertInfo.alertId = value;
					} else if (String.valueOf(alertInfo.alertId).length() < MAX_NUMBER_LEN) {
						alertInfo.alertId = Integer.parseInt(alertInfo.alertId
								+ String.valueOf(value));
					}
				}
			}
			break;
		}
	}

	public void destroy() {
		removeTimerListener();
		parentScreen = null;
		bgImage = null;
		buttonImage = null;
		focusedButtonImage = null;
		loadingImage1 = null;
		loadingImage2 = null;
		logoImage = null;
		coverImage = null;

		isRunning = false;
		parent = null;
		alertInfo.detroy();
	}
}
