package development;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

import state.Alert;
import state.GameConstants;
import state.GameForm;
import state.GameGlobal;
import state.Key;
import state.TimerListener;
import state.Transformer;
import state.component.Event;
import state.component.EventListener;
import state.component.Menu;
import state.font.FontManager;
import state.font.ImageText;
import state.util.Color;
import state.util.ImageUtil;
import development.bean.Table;
import development.socket.SocketClientUtil;

public class FrmListTable extends GameForm implements TimerListener,
		EventListener {
	public static final int MONEY_CHAR_DISTANCE = 0;
	public static final int TABLE_HOZ_DISTANCE = 73;
	public static final int TABLE_VER_DISTANCE = 86;
	public static final int TABLE_DX = 51;
	public static final int TABLE_DY = 40;
	public static final int MOVE_STEP = 5;

	public static int NUMBER_OF_TABLE_PER_ROW = 3;
	public static boolean isNeedToRefreshList = false;
	public static int currentTableIndex;

	private Image tableImage;
	private Image focusTableImage;
	private Image lockFocusImage;
	private Image lockImage;
	private Image[] personFocusImages;
	private Image[] personImages;
	private Image titleImage;
	private Image moneyImage;
	private Image[] numberImages;
	private Image popupImage;
	private Image buttonImage;
	private Image buttonFocusImage;

	private ImageText text8;

	private boolean isShowInputPasswordBox;
	private StringBuffer tablePassword;
	private int[] index;
	private long timeDelay;
	private char[] lastCharsKey;
	private char lastChar;
	private int tableDy;
	private boolean isLockKey;
	private boolean isButtonLeft;

	private Menu menu;
	private String[] MENU;

	public void init(Hashtable parameters) {
		if (Global.currentGame.getId().equals("tala")
				|| Global.currentGame.getId().equals("tlmb")
				|| Global.currentGame.getId().equals("tlmn")) {
			tableImage = ImageUtil.getImage("Ban.png");
			focusTableImage = ImageUtil.getImage("Ban_focus.png");
		} else {
			tableImage = ImageUtil.getImage("bg_cotuong.png");
			focusTableImage = ImageUtil.getImage("bg_cotuong_focus.png");
		}

		lockImage = ImageUtil.getImage("Lock.png");
		lockFocusImage = ImageUtil.getImage("Lock_focus.png");
		titleImage = ImageUtil.getImage("bg_danhsach.png");
		popupImage = ImageUtil.getImage("bg_popup.png");

		personFocusImages = new Image[4];
		personFocusImages[0] = ImageUtil.getImage("Nguoi_focus.png");
		personFocusImages[1] = Image.createImage(personFocusImages[0], 0, 0,
				personFocusImages[0].getWidth(), personFocusImages[0]
						.getHeight(), Sprite.TRANS_ROT90);
		personFocusImages[2] = Image.createImage(personFocusImages[0], 0, 0,
				personFocusImages[0].getWidth(), personFocusImages[0]
						.getHeight(), Sprite.TRANS_ROT180);
		personFocusImages[3] = Image.createImage(personFocusImages[0], 0, 0,
				personFocusImages[0].getWidth(), personFocusImages[0]
						.getHeight(), Sprite.TRANS_ROT270);

		personImages = new Image[4];
		personImages[0] = ImageUtil.getImage("Nguoi.png");
		personImages[1] = Image.createImage(personImages[0], 0, 0,
				personImages[0].getWidth(), personImages[0].getHeight(),
				Sprite.TRANS_ROT90);
		personImages[2] = Image.createImage(personImages[0], 0, 0,
				personImages[0].getWidth(), personImages[0].getHeight(),
				Sprite.TRANS_ROT180);
		personImages[3] = Image.createImage(personImages[0], 0, 0,
				personImages[0].getWidth(), personImages[0].getHeight(),
				Sprite.TRANS_ROT270);

		Image numberImage = ImageUtil.getImage("number_yellow.png");
		try {
			buttonImage = Image.createImage(GameConstants.CORE_IMAGE_FOLDER
					+ "/button_nho.png");
			buttonFocusImage = Image
					.createImage(GameConstants.CORE_IMAGE_FOLDER
							+ "/button_nho_focus.png");
		} catch (IOException e1) {
		}

		numberImages = new Image[10];
		for (int i = 0; i < numberImages.length; i++) {
			numberImages[i] = ImageUtil.getSubImage(numberImage, 0, i * 8, 5,
					8, true);
		}
		moneyImage = ImageUtil.getSubImage(numberImage, 0, 103, 5, 9, true);
		numberImage = null;

		isButtonLeft = true;
		currentTableIndex = 0;
		isLockKey = false;
		index = new int[11];
		for (int i = 0; i < 11; i++) {
			index[i] = 0;
		}

		text8 = FontManager.getFont(FontManager.FONT_SIZE_8);
		tablePassword = new StringBuffer();
		tableDy = TABLE_DY;
		isShowInputPasswordBox = false;

		if (GameConstants.IS_240x320_SCREEN) {
			NUMBER_OF_TABLE_PER_ROW = 3;
		} else {
			NUMBER_OF_TABLE_PER_ROW = 4;
		}

		MENU = new String[] { "Cập nhật danh sách", "Về menu chính",
				"Thông tin TK" };
		menu = new Menu(MENU, this, Constants.MENU_STYLE, 0,
				GameConstants.SCREEN_HEIGHT - 45);
		setTimerListener(this);
		GameGlobal.setTimerDelay(30);
	}

	public void draw(Graphics g) {
		g.setColor(0x440000);
		g.fillRect(0, 0, GameConstants.SCREEN_WIDTH,
				GameConstants.SCREEN_HEIGHT);
		g.drawImage(titleImage, GameConstants.SCREEN_WIDTH / 2, 6,
				GameConstants.TOP_HCENTER_ANCHOR);

		g.setColor(0xA53A06);
		g.fillRect(4, 32, GameConstants.SCREEN_WIDTH - 8, 5);

		g.setColor(Color.WHITE_CODE);
		g.drawLine(11, 33, GameConstants.SCREEN_WIDTH - 11, 33);

		String levelText = "SƠ CẤP";
		if (Global.currentRoom.getType() == 1) {
			levelText = "TRUNG CẤP";
		} else if (Global.currentRoom.getType() == 2) {
			levelText = "VIP";
		}
		text8.drawString(g, Global.currentGame.getName() + " - " + levelText
				+ " - Phòng " + Global.currentRoom.getName(), Color.WHITE_CODE,
				GameConstants.SCREEN_WIDTH / 2, 21,
				GameConstants.BOTTOM_HCENTER_ANCHOR);

		g.setClip(0, 36, GameConstants.SCREEN_WIDTH,
				GameConstants.SCREEN_HEIGHT - 61);
		g.setColor(0xA53A06);
		g.fillRect(4, 28, GameConstants.SCREEN_WIDTH - 7,
				GameConstants.SCREEN_HEIGHT - 55);

		if (Global.currentRoom.numberOfChilds() == 0) {
			g.drawImage(popupImage, GameConstants.SCREEN_WIDTH / 2,
					GameConstants.SCREEN_HEIGHT / 2,
					GameConstants.CENTER_ANCHOR);
			text8.drawString(g, "Đang cập nhật danh sách bàn",
					Color.WHITE_CODE, GameConstants.SCREEN_WIDTH / 2,
					GameConstants.SCREEN_HEIGHT / 2,
					GameConstants.CENTER_ANCHOR);
			return;
		}

		int currentRow = currentTableIndex / NUMBER_OF_TABLE_PER_ROW;
		int beginTableIndex = (currentRow - 2) * NUMBER_OF_TABLE_PER_ROW; // bàn
																			// đầu
																			// tiên
																			// cần
																			// vẽ
																			// (bàn
																			// đầu
																			// tiên
																			// của
																			// 2
																			// dòng
																			// trước
																			// đó)
		beginTableIndex = (beginTableIndex < 0) ? 0 : beginTableIndex;
		int endTableIndex = (currentRow + 2) * NUMBER_OF_TABLE_PER_ROW
				+ NUMBER_OF_TABLE_PER_ROW - 1; // bàn cuối cùng cần vẽ
		endTableIndex = (endTableIndex > Global.currentRoom.numberOfChilds() - 1) ? Global.currentRoom
				.numberOfChilds() - 1
				: endTableIndex;

		for (int i = beginTableIndex; i <= endTableIndex; i++) {
			Table table = (Table) Global.currentRoom.getChild(i);
			int row = i / NUMBER_OF_TABLE_PER_ROW;
			int x = TABLE_DX + i % NUMBER_OF_TABLE_PER_ROW * TABLE_HOZ_DISTANCE;
			int y = 36 + tableDy + TABLE_VER_DISTANCE * row;

			if (i == currentTableIndex) {
				g.drawImage(focusTableImage, x, y, GameConstants.CENTER_ANCHOR);
				if (table.isLocked()) {
					g.drawImage(lockFocusImage, x + 25, y - 25,
							GameConstants.CENTER_ANCHOR);
				}
				drawMoneyByCenterAnchor(g, table.getBid(), x, y + 45);
			} else {
				g.drawImage(tableImage, x, y, GameConstants.CENTER_ANCHOR);
				if (table.isLocked()) {
					g.drawImage(lockImage, x + 16, y - 16,
							GameConstants.CENTER_ANCHOR);
				}
				drawMoneyByCenterAnchor(g, table.getBid(), x, y + 33);
			}
			text8.drawString(g, table.getName().substring(
					table.getName().length() - 2), Color.WHITE_CODE, x, y,
					GameConstants.CENTER_ANCHOR);

			for (int j = 0; j < table.getConcurrentUser(); j++) {
				switch (j) {
				case 0:
					if (i == currentTableIndex) {
						g.drawImage(personFocusImages[1], x, y - 28,
								GameConstants.TOP_HCENTER_ANCHOR);
					} else {
						g.drawImage(personImages[1], x, y - 20,
								GameConstants.TOP_HCENTER_ANCHOR);
					}
					break;
				case 1:
					if (i == currentTableIndex) {
						g.drawImage(personFocusImages[3], x, y + 32,
								GameConstants.BOTTOM_HCENTER_ANCHOR);
					} else {
						g.drawImage(personImages[3], x, y + 22,
								GameConstants.BOTTOM_HCENTER_ANCHOR);
					}
					break;
				case 2:
					if (i == currentTableIndex) {
						g.drawImage(personFocusImages[0], x - 32, y,
								GameConstants.VCENTER_LEFT_ANCHOR);
					} else {
						g.drawImage(personImages[0], x - 22, y,
								GameConstants.VCENTER_LEFT_ANCHOR);
					}
					break;
				case 3:
					if (i == currentTableIndex) {
						g.drawImage(personFocusImages[2], x + 32, y,
								GameConstants.VCENTER_RIGHT_ANCHOR);
					} else {
						g.drawImage(personImages[2], x + 22, y,
								GameConstants.VCENTER_RIGHT_ANCHOR);
					}
					break;
				}
			}
		}

		if (isShowInputPasswordBox) {
			g.drawImage(popupImage, GameConstants.SCREEN_WIDTH / 2,
					GameConstants.SCREEN_HEIGHT / 2,
					GameConstants.CENTER_ANCHOR);
			text8.drawString(g, "Nhập mật khẩu của phòng", Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH / 2,
					GameConstants.SCREEN_HEIGHT / 2 - 40,
					GameConstants.TOP_HCENTER_ANCHOR);
			g.setColor(Color.WHITE_CODE);
			g.fillRect(GameConstants.SCREEN_WIDTH / 2 - 50,
					GameConstants.SCREEN_HEIGHT / 2 - 10, 100, 20);
			text8.drawString(g, tablePassword.toString(), Color.BLACK_CODE,
					GameConstants.SCREEN_WIDTH / 2 - 45,
					GameConstants.SCREEN_HEIGHT / 2 - 8,
					GameConstants.TOP_LEFT_ANCHOR);
			g.setColor(Color.BLACK_CODE);
			g.drawLine(GameConstants.SCREEN_WIDTH / 2 - 45
					+ text8.stringWidth(tablePassword.toString()),
					GameConstants.SCREEN_HEIGHT / 2 - 8,
					GameConstants.SCREEN_WIDTH / 2 - 45
							+ text8.stringWidth(tablePassword.toString()),
					GameConstants.SCREEN_HEIGHT / 2 + 7);
			if (isButtonLeft) {
				g.drawImage(buttonFocusImage,
						GameConstants.SCREEN_WIDTH / 2 - 78,
						GameConstants.SCREEN_HEIGHT / 2 + 30,
						GameConstants.TOP_LEFT_ANCHOR);
				g.drawImage(buttonImage, GameConstants.SCREEN_WIDTH / 2 + 22,
						GameConstants.SCREEN_HEIGHT / 2 + 30,
						GameConstants.TOP_LEFT_ANCHOR);
			} else {
				g.drawImage(buttonImage, GameConstants.SCREEN_WIDTH / 2 - 78,
						GameConstants.SCREEN_HEIGHT / 2 + 30,
						GameConstants.TOP_LEFT_ANCHOR);
				g.drawImage(buttonFocusImage,
						GameConstants.SCREEN_WIDTH / 2 + 22,
						GameConstants.SCREEN_HEIGHT / 2 + 30,
						GameConstants.TOP_LEFT_ANCHOR);
			}

			text8.drawString(g, "Đồng ý", Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH / 2 - 67,
					GameConstants.SCREEN_HEIGHT / 2 + 33,
					GameConstants.TOP_LEFT_ANCHOR);
			text8.drawString(g, "Hủy", Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH / 2 + 42,
					GameConstants.SCREEN_HEIGHT / 2 + 33,
					GameConstants.TOP_LEFT_ANCHOR);
		}

		g
				.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
		g.setColor(0x2B0102);// Màu nền task bar
		g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25,
				GameConstants.SCREEN_WIDTH, 25);
		if ((menu != null) && menu.isShowing()) {
			text8.drawString(g, "Đóng", Color.WHITE_CODE, 5,
					GameConstants.SCREEN_HEIGHT - 5,
					GameConstants.BOTTOM_LEFT_ANCHOR);
		} else {
			text8.drawString(g, "Menu", Color.WHITE_CODE, 8,
					GameConstants.SCREEN_HEIGHT - 5,
					GameConstants.BOTTOM_LEFT_ANCHOR);
		}
		if (isShowInputPasswordBox) {
			if (tablePassword != null && tablePassword.length() > 0) {
				text8.drawString(g, "Xóa", Color.WHITE_CODE,
						GameConstants.SCREEN_WIDTH - 8,
						GameConstants.SCREEN_HEIGHT - 5,
						GameConstants.BOTTOM_RIGHT_ANCHOR);
			} else {
				text8.drawString(g, "Trở về", Color.WHITE_CODE,
						GameConstants.SCREEN_WIDTH - 8,
						GameConstants.SCREEN_HEIGHT - 5,
						GameConstants.BOTTOM_RIGHT_ANCHOR);
			}
		} else {
			text8.drawString(g, "Trở về", Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH - 8,
					GameConstants.SCREEN_HEIGHT - 5,
					GameConstants.BOTTOM_RIGHT_ANCHOR);
		}
		menu.draw(g);
	}

	private void drawMoneyByCenterAnchor(Graphics g, int money, int x, int y) {
		String moneyStr = String.valueOf(money);
		Image[] images = new Image[moneyStr.length() + 1];
		int totalWidth = 0;
		for (int i = 0; i < moneyStr.length(); i++) {
			images[i] = numberImages[Integer.parseInt(String.valueOf(moneyStr
					.charAt(i)))];
			totalWidth += images[i].getWidth() + MONEY_CHAR_DISTANCE;
		}
		images[images.length - 1] = moneyImage;
		totalWidth += images[images.length - 1].getWidth()
				+ MONEY_CHAR_DISTANCE;
		x -= totalWidth / 2;

		for (int i = 0; i < images.length; i++) {
			g.drawImage(images[i], x, y, GameConstants.VCENTER_LEFT_ANCHOR);
			x += images[i].getWidth() + MONEY_CHAR_DISTANCE;
		}
	}

	private char[] getChars(int key) {
		switch (key) {
		case Key.K_1:
			return GameConstants.KEY_NUM1_CHARS;
		case Key.K_2:
			return GameConstants.KEY_NUM2_CHARS;
		case Key.K_3:
			return GameConstants.KEY_NUM3_CHARS;
		case Key.K_4:
			return GameConstants.KEY_NUM4_CHARS;
		case Key.K_5:
			return GameConstants.KEY_NUM5_CHARS;
		case Key.K_6:
			return GameConstants.KEY_NUM6_CHARS;
		case Key.K_7:
			return GameConstants.KEY_NUM7_CHARS;
		case Key.K_8:
			return GameConstants.KEY_NUM8_CHARS;
		case Key.K_9:
			return GameConstants.KEY_NUM9_CHARS;
		case Key.K_0:
			return GameConstants.KEY_NUM0_CHARS;
		case Key.STAR:
			return GameConstants.KEY_STAR_CHARS;
		}
		return null;
	}

	public void keyReleased(int keyCode) {
		keyCode = Key.getGameKey(keyCode);

		if (keyCode == Key.SOFT_LEFT) {
			if (menu.isShowing()) {
				menu.hide();
			} else {
				menu.show();
			}
			return;
		}

		if (menu.isShowing()) {
			return;
		}

		if ((keyCode >= Key.K_0) && (keyCode <= Key.K_9)
				&& GameConstants.IS_240x320_SCREEN) {
			if (isShowInputPasswordBox) {
				int order = keyCode - Key.K_0;
				writeKey(order, getChars(keyCode));
			}
			return;
		}

		boolean isCommandKey = Key.isCommandKey;
		if (!GameConstants.IS_240x320_SCREEN && !isCommandKey) {
			if (isShowInputPasswordBox) {
				writeKey320x240((char) keyCode);
			}
			return;
		}

		switch (keyCode) {
		case Key.UP:
		case Key.K_2:
			if (isLockKey) {
				return;
			}

			if (currentTableIndex - NUMBER_OF_TABLE_PER_ROW >= 0) {
				currentTableIndex -= NUMBER_OF_TABLE_PER_ROW;
				isLockKey = true;
			}
			break;
		case Key.DOWN:
		case Key.K_8:
			if (isLockKey) {
				return;
			}

			if (currentTableIndex + NUMBER_OF_TABLE_PER_ROW < Global.currentRoom
					.numberOfChilds()) {
				currentTableIndex += NUMBER_OF_TABLE_PER_ROW;
				isLockKey = true;
			}
			break;
		case Key.LEFT:
		case Key.K_4:
			if (isShowInputPasswordBox) {
				isButtonLeft = !isButtonLeft;
			}
			if (isLockKey) {
				return;
			}
			if (!isShowInputPasswordBox) {
				if (currentTableIndex - 1 >= 0) {
					currentTableIndex--;
					isLockKey = true;
				}
			}
			break;
		case Key.RIGHT:
		case Key.K_6:
			if (isShowInputPasswordBox) {
				isButtonLeft = !isButtonLeft;
			}
			if (isLockKey) {
				return;
			}
			if (!isShowInputPasswordBox) {
				if (currentTableIndex + 1 < Global.currentRoom.numberOfChilds()) {
					currentTableIndex++;
					isLockKey = true;
				}
			}
			break;
		case Key.FIRE:
			if (Global.currentRoom.numberOfChilds() == 0) {
				return;
			}

			Global.currentTable = (Table) Global.currentRoom
					.getChild(currentTableIndex);
			if (Global.currentTable.getConcurrentUser() >= Global.currentTable
					.getMaxUser()) {
				GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Bàn đã đầy");
				return;
			}

			if (Global.currentUser.getMoney() < Global.currentTable.getBid()) {
				GameGlobal.alert.showCustomButtonAlert(this, new String[] {
						"Nạp điểm", "Quay lại" }, Alert.BUTTON_WIDTH_LARGE,
						new String[] { "Bạn không đủ điểm để vào bàn",
								"Để tham gia bạn phải nạp thêm điểm!" }, 99,
						Alert.VERTICAL_ALINE);
				return;
			}

			if (Global.currentTable.isPlaying()) {
				GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
						new String[] { "Phòng đang chơi" });
				return;
			}

			if (isLockKey) {
				return;
			}

			if (isShowInputPasswordBox) {
				if (!isButtonLeft) {
					isShowInputPasswordBox = false;
				} else {
					SocketClientUtil.joinTableRequest(Global.currentTable
							.getId(), tablePassword.toString());
					isLockKey = true;
					isShowInputPasswordBox = false;
				}
				return;
			}

			if (Global.currentTable.isLocked()) {
				isShowInputPasswordBox = true;
				return;
			}
			SocketClientUtil
					.joinTableRequest(Global.currentTable.getId(), null);
			break;
		case Key.SOFT_RIGHT:
			if (isShowInputPasswordBox) {
				if (tablePassword != null && tablePassword.length() > 0) {
					tablePassword.deleteCharAt(tablePassword.length() - 1);
				} else {
					isShowInputPasswordBox = false;
				}
			} else {
				SocketClientUtil.leaveRoomRequest();
				GameGlobal.nextState(Global.frmListRoom, null,
						Transformer.TRANSFORM_WITH_LOADING_FORM);
			}
			break;
		}
	}

	private void writeKey320x240(char charKey) {
		long distance = System.currentTimeMillis() - timeDelay;
		if ((lastChar != charKey) || (distance > 1000)) {
			tablePassword.append(charKey);
			lastChar = charKey;
		} else {
			if (tablePassword.length() == 0) {
				tablePassword.append(charKey);
			}
			tablePassword.setCharAt(tablePassword.length() - 1, charKey);
		}
		timeDelay = System.currentTimeMillis();
	}

	private void writeKey(int order, char[] charOfKeys) {
		long distance = System.currentTimeMillis() - timeDelay;
		if ((lastCharsKey != charOfKeys) || (distance > 1000)) {
			index[order] = 0;
			tablePassword.append(charOfKeys[index[order]]);
			lastCharsKey = charOfKeys;
		} else {
			if (tablePassword.length() == 0) {
				tablePassword.append(charOfKeys[index[order]]);
			}
			index[order] = (index[order] + 1) % (charOfKeys.length);
			tablePassword.setCharAt(tablePassword.length() - 1,
					charOfKeys[index[order]]);
		}
		timeDelay = System.currentTimeMillis();
	}

	public void doTask() {
		runAutoCamera();
	}

	private void runAutoCamera() {
		int currentRow = currentTableIndex / NUMBER_OF_TABLE_PER_ROW;
		int desY = 0;
		if (currentRow == 0) {
			desY = TABLE_DY;
		} else if (currentRow == Global.currentRoom.numberOfChilds()
				/ NUMBER_OF_TABLE_PER_ROW) {
			if (GameConstants.IS_240x320_SCREEN) {
				desY = TABLE_DY - (currentRow - 2) * TABLE_VER_DISTANCE;
			} else {
				desY = TABLE_DY - (currentRow - 1) * TABLE_VER_DISTANCE;
			}
		} else {
			desY = TABLE_DY - (currentRow - 1) * TABLE_VER_DISTANCE;
		}

		int tmp = desY - tableDy;
		if (tmp > 0) {
			tableDy += Math.min(tmp, MOVE_STEP);
		} else {
			tableDy += Math.max(tmp, -MOVE_STEP);
		}

		if (tableDy == desY) {
			isLockKey = false;
		}
	}

	public void alertEventPerform(int alertType, int eventType, int alertId) {
		// Khi người chơi nạp điểm
		if (alertId == 99 && eventType == 0) {
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
					"Chức năng nạp điểm đang phát triển");
		}
	}

	public void onActionPerform(Event event) {
		// "Cập nhật danh sách", "Về menu chính", "Thông tin TK"
		String action = event.getAction();
		if (action.equals(MENU[0])) { // Cập nhật danh sách
			SocketClientUtil.getTableList(Global.currentRoom.getId());
		} else if (action.equals(MENU[1])) { // Về Menu chính
			SocketClientUtil.leaveRoomRequest();
			GameGlobal.nextState(Global.frmChooseGame, null,
					Transformer.TRANSFORM_WITH_LOADING_FORM);
		} else if (action.equals(MENU[2])) { // Thông tin tài khoản
		}
	}

	protected void destroy() {
		index = null;
		lastCharsKey = null;
		tableImage = null;
		focusTableImage = null;
		lockFocusImage = null;
		lockImage = null;
		personFocusImages = null;
		personImages = null;
		titleImage = null;
		moneyImage = null;
		numberImages = null;
		popupImage = null;
		tablePassword = null;
		text8 = null;
		buttonImage = null;
		;
		buttonFocusImage = null;
		MENU = null;
		if (menu != null) {
			menu.detroy();
			menu = null;
		}
	}
}
