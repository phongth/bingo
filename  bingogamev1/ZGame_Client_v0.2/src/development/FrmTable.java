package development;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Display;
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
import development.bean.User;
import development.socket.SocketClientUtil;

public class FrmTable extends GameForm implements TimerListener, EventListener {
	private static final String CHAT_SEND = "CHAT";
	public static final String INVITE_HEADER = "INVITE";
	public static final String DAT_CUOC_LABEL = "Thiết lập bàn";

	private String[] messageChats;
	private int[] messageDelayCount;
	private boolean isInChatState;
	private int inputWidth;
	private int inputHeight;
	private StringBuffer currentText;
	private int caretIndex;
	private int caretLeft;
	private boolean caretBlinkOn;
	private long caretBlinkDelay;
	private long lastCaretBlink;
	private int lastPressedKey;
	private int myIndex;
	private int currentKeyStep;
	private int inputTranslationX;
	private long lastKeyTimestamp;
	private long maxKeyDelay;
	private boolean goToNextChar;

	private static final int MAX_WAIT_TIME = 100; // 10s
	private static final int MAX_START_TIME = 150; // 15s

	public String password;
	public boolean isShowSettingBoard;
	public boolean isConfigTableDone;
	public int bid;
	public String[] tabStrings;

	private Image numberBgImage;
	private Image leftChatImage;
	private Image rightChatImage;
	// private Image tabImage;
	private Image tabLongImage;
	private Image tabLongFocusImage;
	// private Image tabFocusImage;
	private Image avatarImage;
	private Image ballImage;
	private Image popupImage;
	private Image buttonImage;
	private Image buttonFocusImage;

	// ảnh cho popup
	private Image bodyFrameImage;
	private Image lImage;
	private Image rightArrowImage;
	private Image leftArrowImage;
	private Menu menu;

	private String[] MENU;
	private String[] nameUsers;
	private int[] indexFriend;
	public boolean isReturnFromGame = false;

	private ImageText text8;
	private StringBuffer tablePassword;

	private int[] index;
	private long timeDelay;
	private char[] lastCharsKey;
	private char lastChar;
	private int tabIndex;
	private int indexChatUser;
	private int dx;

	private int selectItemIndexInBoard;
	private boolean isReadyToStart;
	private int inRoomTime;
	private boolean isSendReady;
	private boolean isStartGame;
	private int waitToStartGameTime;

	public void init(Hashtable parameters) {
		tabIndex = 0;
		isSendReady = false;
		isStartGame = false;
		dx = 0;
		waitToStartGameTime = -1;
		timeDelay = 0;
		index = new int[11];
		for (int i = 0; i < 11; i++) {
			index[i] = 0;
		}

		tablePassword = new StringBuffer();
		selectItemIndexInBoard = 0;
		isReadyToStart = false;
		inRoomTime = 0;

		tabStrings = new String[2];
		// tabStrings[1] = "DS bạn bè";
		switch (Global.currentRoom.getType()) {
		case 0:
			tabStrings[0] = Global.currentGame.getName() + " - Sơ cấp - Phòng "
					+ Global.currentRoom.getName() + " - Bàn "
					+ Global.currentTable.getName();
			break;
		case 1:
			tabStrings[0] = Global.currentGame.getName()
					+ " - Trung cấp - Phòng " + Global.currentRoom.getName()
					+ " - Bàn " + Global.currentTable.getName();
			break;
		case 2:
			tabStrings[0] = Global.currentGame.getName() + " - VIP - Phòng "
					+ Global.currentRoom.getName() + " - Bàn"
					+ Global.currentTable.getName();
			break;
		default:
			tabStrings[0] = Global.currentGame.getName() + " - Sơ cấp - Phòng "
					+ Global.currentRoom.getName() + " - Bàn "
					+ Global.currentTable.getName();
			break;
		}
		bid = Global.currentRoom.getMinBid();

		text8 = FontManager.getFont(FontManager.FONT_SIZE_8);
		isInChatState = false;
		inputWidth = 90;
		inputHeight = text8.getHeight();
		resetText();

		// tab Image
		// tabImage = ImageUtil.getImage("tab.png");
		tabLongImage = ImageUtil.getImage("tab_320240.png");
		tabLongFocusImage = ImageUtil.getImage("tab_focus_320240.png");
		// tabFocusImage = ImageUtil.getImage("tab_focus.png");

		ballImage = ImageUtil.getImage("datcuoc.png");
		numberBgImage = ImageUtil.getImage("Nen_So.png");

		leftChatImage = ImageUtil.getImage("Chat.png");
		rightChatImage = ImageUtil.getImage("Chat1.png");
		avatarImage = ImageUtil.getImage("Avatar1.png");

		// ảnh cho popUp
		Image popUpImage = ImageUtil.getImage("PopUp3.png");
		bodyFrameImage = ImageUtil.getSubImage(popUpImage, 17, 0, 1, 98, true);
		bodyFrameImage = ImageUtil.joinAndCreateImages(bodyFrameImage, 157, 98,
				true);
		popupImage = ImageUtil.getImage("bg_popup.png");
		popUpImage = null;
		try {
			buttonImage = Image.createImage(GameConstants.CORE_IMAGE_FOLDER
					+ "/button_nho.png");
			buttonFocusImage = Image
					.createImage(GameConstants.CORE_IMAGE_FOLDER
							+ "/button_nho_focus.png");
		} catch (IOException e1) {
		}
		lImage = ImageUtil.getSubImage(ImageUtil.getImage("Nen_chat.png"), 0,
				0, 1, 15, true);
		rightArrowImage = ImageUtil.getImage("Len.png");
		rightArrowImage = Image.createImage(rightArrowImage, 0, 0,
				rightArrowImage.getWidth(), rightArrowImage.getHeight(),
				Sprite.TRANS_ROT90);
		leftArrowImage = ImageUtil.getImage("Xuong.png");
		leftArrowImage = Image.createImage(leftArrowImage, 0, 0, leftArrowImage
				.getWidth(), leftArrowImage.getHeight(), Sprite.TRANS_ROT90);

		messageChats = new String[Global.tableUsers.size()];
		messageDelayCount = new int[Global.tableUsers.size()];
		for (int i = 0; i < Global.tableUsers.size(); i++) {
			messageDelayCount[i] = -1;
			messageChats[i] = "";
		}

		MENU = new String[] { "Về menu chính", "Thông tin TK", "Mời chơi" };
		menu = new Menu(MENU, this, Constants.MENU_STYLE, 0,
				GameConstants.SCREEN_HEIGHT - 45);

		if (Global.currentUser.getName().equals(
				Global.currentTable.getTableMasterName())
				&& !isReturnFromGame) {
			isShowSettingBoard = true;
			isConfigTableDone = false;
		}
		isReturnFromGame = false;
		setTimerListener(this);
	}

	public void draw(Graphics g) {
		g.setColor(0x440000);
		g.fillRect(0, 0, GameConstants.SCREEN_WIDTH,
				GameConstants.SCREEN_HEIGHT);
		g.setColor(0xA53A06);
		g.fillRect(4, 25, GameConstants.SCREEN_WIDTH - 8,
				GameConstants.SCREEN_HEIGHT - 50); // Tab background

		// Draw bottom bar
		g.setColor(0x2B0102);// Màu nền task bar
		g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25,
				GameConstants.SCREEN_WIDTH, 25);

		if (menu.isShowing()) {
			text8.drawString(g, "Đóng", Color.WHITE_CODE, 5,
					GameConstants.SCREEN_HEIGHT - 5,
					GameConstants.BOTTOM_LEFT_ANCHOR);
		} else {
			if (!isShowSettingBoard) {
				if (!isInChatState) {
					text8.drawString(g, "Menu", Color.WHITE_CODE, 5,
							GameConstants.SCREEN_HEIGHT - 5,
							GameConstants.BOTTOM_LEFT_ANCHOR);
					text8.drawString(g, "Trở về", Color.WHITE_CODE,
							GameConstants.SCREEN_WIDTH - 8,
							GameConstants.SCREEN_HEIGHT - 4,
							GameConstants.BOTTOM_RIGHT_ANCHOR);
				}

			}
		}

		if (Global.currentUser.getName().equals(
				Global.currentTable.getTableMasterName())) {
			if (isShowSettingBoard) {
				text8.drawString(g, "Chọn", Color.WHITE_CODE,
						GameConstants.SCREEN_WIDTH / 2,
						GameConstants.SCREEN_HEIGHT - 4,
						GameConstants.BOTTOM_HCENTER_ANCHOR);
				if (selectItemIndexInBoard == 1 && tablePassword.length() > 0) {
					text8.drawString(g, "Xóa", Color.WHITE_CODE,
							GameConstants.SCREEN_WIDTH - 8,
							GameConstants.SCREEN_HEIGHT - 4,
							GameConstants.BOTTOM_RIGHT_ANCHOR);
				} else {
					text8.drawString(g, "Trở về", Color.WHITE_CODE,
							GameConstants.SCREEN_WIDTH - 8,
							GameConstants.SCREEN_HEIGHT - 4,
							GameConstants.BOTTOM_RIGHT_ANCHOR);
				}
			} else {
				if (isReadyToStart) {
					text8.drawString(g, "Bắt đầu", Color.WHITE_CODE,
							GameConstants.SCREEN_WIDTH / 2,
							GameConstants.SCREEN_HEIGHT - 4,
							GameConstants.BOTTOM_HCENTER_ANCHOR);
				} else {
					text8.drawString(g, "Bắt đầu", 0x969696,
							GameConstants.SCREEN_WIDTH / 2,
							GameConstants.SCREEN_HEIGHT - 4,
							GameConstants.BOTTOM_HCENTER_ANCHOR);
				}
			}
		} else {
			if (!Global.currentUser.isReady()) {
				text8.drawString(g, "Sẵn sàng", Color.WHITE_CODE,
						GameConstants.SCREEN_WIDTH / 2,
						GameConstants.SCREEN_HEIGHT - 4,
						GameConstants.BOTTOM_HCENTER_ANCHOR);
			}
		}

		// Vẽ ceilling của bàn
		g.drawImage(ballImage, GameConstants.SCREEN_WIDTH / 2,
				GameConstants.SCREEN_HEIGHT / 2 - 10,
				GameConstants.CENTER_ANCHOR);
		text8.drawString(g, "Đặt cược", Color.WHITE_CODE,
				GameConstants.SCREEN_WIDTH / 2,
				GameConstants.SCREEN_HEIGHT / 2 - 25,
				GameConstants.TOP_HCENTER_ANCHOR);
		text8.drawString(g, String.valueOf(Global.currentTable.getBid()),
				Color.WHITE_CODE, GameConstants.SCREEN_WIDTH / 2,
				GameConstants.SCREEN_HEIGHT / 2 - 10,
				GameConstants.TOP_HCENTER_ANCHOR);

		if (GameConstants.IS_240x320_SCREEN) {
			draw240320(g);
			drawChat(g);
		} else {
			draw320x240(g);
			drawChat320240(g);
		}

		g.drawImage(tabLongFocusImage, 4, 4, GameConstants.TOP_LEFT_ANCHOR);
		// g.drawImage(tabImage, 8 + tabLongImage.getWidth(), 4,
		// GameConstants.TOP_LEFT_ANCHOR);
		// text8.drawString(g, tabStrings[1], Color.WHITE_CODE, 14 +
		// tabLongImage.getWidth(), 8, GameConstants.TOP_LEFT_ANCHOR);
		g.setClip(5, 0, tabLongImage.getWidth() - 8, 25);
		text8.drawString(g, tabStrings[0], Color.WHITE_CODE, 65 + dx, 8,
				GameConstants.TOP_LEFT_ANCHOR);
		dx -= 2;
		if (dx < 0 - text8.stringWidth(tabStrings[0]) - 40) {
			dx = 0;
		}

		g
				.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
		menu.draw(g);
	}

	private void resetText() {
		if (messageDelayCount != null) {
			messageDelayCount[myIndex] = 0;
			messageChats[myIndex] = new String();
		}

		currentText = new StringBuffer();
		lastPressedKey = Integer.MIN_VALUE;
		currentKeyStep = 0;

		inputTranslationX = 0;

		lastKeyTimestamp = 0;
		maxKeyDelay = 500L;

		caretIndex = 0;
		caretLeft = 0;
		caretBlinkOn = true;
		caretBlinkDelay = 500L;
		lastCaretBlink = 0;

		goToNextChar = true;
	}

	public void checkTimestamps() {
		long currentTime = System.currentTimeMillis();

		if (lastCaretBlink + caretBlinkDelay < currentTime) {
			caretBlinkOn = !caretBlinkOn;

			lastCaretBlink = currentTime;
		}

		if (!goToNextChar && lastKeyTimestamp + maxKeyDelay < currentTime) {
			goToNextChar = true;
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

	private void updateCaretPosition() {
		caretLeft = text8.substringWidth(messageChats[myIndex], 0, caretIndex);
		if (caretLeft + inputTranslationX < 0) {
			inputTranslationX = -caretLeft;
		} else if (caretLeft + inputTranslationX > inputWidth) {
			inputTranslationX = inputWidth - caretLeft;
		}
	}

	private void clearChar() {
		if (currentText.length() > 0 && caretIndex > 0) {
			caretIndex--;
			currentText.deleteCharAt(caretIndex);
			messageChats[myIndex] = currentText.toString();
		}
	}

	public void draw240320(Graphics g) {
		if (isShowSettingBoard) {
			g.drawImage(popupImage, GameConstants.SCREEN_WIDTH / 2, 80,
					GameConstants.TOP_HCENTER_ANCHOR);
			text8.drawString(g, "Đặt cược", Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH / 2, 92,
					GameConstants.TOP_HCENTER_ANCHOR);
			g.setColor(Color.WHITE_CODE);
			g.fillRect(71, 111, 99, 17);
			text8.drawString(g, "Đặt mật khẩu", Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH / 2, 135,
					GameConstants.TOP_HCENTER_ANCHOR);
			g.fillRect(71, 153, 99, 17);
			g.drawImage(rightArrowImage, 158, 113,
					GameConstants.TOP_LEFT_ANCHOR);
			g
					.drawImage(leftArrowImage, 150, 113,
							GameConstants.TOP_LEFT_ANCHOR);
			text8.drawString(g, tablePassword.toString(), Color.BLACK_CODE, 75,
					154, GameConstants.TOP_LEFT_ANCHOR);
			g.setColor(Color.BLACK_CODE);
			text8.drawString(g, String.valueOf(bid), Color.BLACK_CODE, 75, 110,
					GameConstants.TOP_LEFT_ANCHOR);
			g.drawImage(buttonImage, 50, 183, GameConstants.TOP_LEFT_ANCHOR);
			g.drawImage(buttonImage, 130, 183, GameConstants.TOP_LEFT_ANCHOR);
			if (selectItemIndexInBoard == 0) {
				g.drawImage(lImage,
						75 + text8.stringWidth(String.valueOf(bid)), 125,
						GameConstants.BOTTOM_LEFT_ANCHOR);
			} else if (selectItemIndexInBoard == 1) {
				g.drawImage(lImage, 75 + text8.stringWidth(tablePassword
						.toString()), 156, GameConstants.TOP_LEFT_ANCHOR);
			} else if (selectItemIndexInBoard == 2) {
				g.drawImage(buttonFocusImage, 50, 183,
						GameConstants.TOP_LEFT_ANCHOR);
			}
			if (selectItemIndexInBoard == 3) {
				g.drawImage(buttonFocusImage, 130, 183,
						GameConstants.TOP_LEFT_ANCHOR);
			}
			text8.drawString(g, "Đồng ý", Color.WHITE_CODE, 57, 185,
					GameConstants.TOP_LEFT_ANCHOR);
			text8.drawString(g, "Hủy", Color.WHITE_CODE, 147, 185,
					GameConstants.TOP_LEFT_ANCHOR);
			return;
		}

		// Draw user
		drawUser(g, 0, 26, 210, 84, 196, 54, 188, leftChatImage);
		drawUser(g, 1, 176, 74, 160, 60, 130, 52, rightChatImage);
		drawUser(g, 2, 176, 210, 160, 196, 130, 188, rightChatImage);
		drawUser(g, 3, 26, 74, 84, 60, 54, 52, leftChatImage);

		if (Global.currentUser.getName().equals(
				Global.currentTable.getTableMasterName())) {
			if ((waitToStartGameTime > -1)
					&& (waitToStartGameTime <= MAX_START_TIME)) {
				g
						.drawImage(numberBgImage, 77, 219,
								GameConstants.CENTER_ANCHOR);
				text8.drawString(g, String
						.valueOf((MAX_START_TIME - waitToStartGameTime) / 10),
						Color.WHITE_CODE, 77, 219, GameConstants.CENTER_ANCHOR);
			}
		} else {
			if ((inRoomTime <= MAX_WAIT_TIME) && !Global.currentUser.isReady()) {
				g
						.drawImage(numberBgImage, 77, 219,
								GameConstants.CENTER_ANCHOR);
				text8.drawString(g, String
						.valueOf((MAX_WAIT_TIME - inRoomTime) / 10),
						Color.WHITE_CODE, 77, 219, GameConstants.CENTER_ANCHOR);
			}
		}
	}

	public void draw320x240(Graphics g) {
		if (isShowSettingBoard) {
			g.drawImage(popupImage, GameConstants.SCREEN_WIDTH / 2, 65,
					GameConstants.TOP_HCENTER_ANCHOR);
			text8.drawString(g, "Đặt cược", Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH / 2, 70,
					GameConstants.TOP_HCENTER_ANCHOR);
			g.setColor(Color.WHITE_CODE);
			g.fillRect(111, 86, 99, 17);
			text8.drawString(g, "Đặt mật khẩu", Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH / 2, 110,
					GameConstants.TOP_HCENTER_ANCHOR);
			g.fillRect(111, 128, 99, 17);
			g
					.drawImage(rightArrowImage, 198, 88,
							GameConstants.TOP_LEFT_ANCHOR);
			g.drawImage(leftArrowImage, 190, 88, GameConstants.TOP_LEFT_ANCHOR);
			text8.drawString(g, tablePassword.toString(), Color.BLACK_CODE,
					115, 129, GameConstants.TOP_LEFT_ANCHOR);
			g.setColor(Color.BLACK_CODE);
			text8.drawString(g, String.valueOf(bid), Color.BLACK_CODE, 115, 85,
					GameConstants.TOP_LEFT_ANCHOR);
			g.drawImage(buttonImage, 90, 158, GameConstants.TOP_LEFT_ANCHOR);
			g.drawImage(buttonImage, 170, 158, GameConstants.TOP_LEFT_ANCHOR);
			if (selectItemIndexInBoard == 0) {
				g.drawImage(lImage, 115 + text8
						.stringWidth(String.valueOf(bid)), 100,
						GameConstants.BOTTOM_LEFT_ANCHOR);
			} else if (selectItemIndexInBoard == 1) {
				g.drawImage(lImage, 115 + text8.stringWidth(tablePassword
						.toString()), 131, GameConstants.TOP_LEFT_ANCHOR);
			} else if (selectItemIndexInBoard == 2) {
				g.drawImage(buttonFocusImage, 90, 158,
						GameConstants.TOP_LEFT_ANCHOR);
			}
			if (selectItemIndexInBoard == 3) {
				g.drawImage(buttonFocusImage, 170, 158,
						GameConstants.TOP_LEFT_ANCHOR);
			}
			text8.drawString(g, "Đồng ý", Color.WHITE_CODE, 97, 160,
					GameConstants.TOP_LEFT_ANCHOR);
			text8.drawString(g, "Hủy", Color.WHITE_CODE, 187, 160,
					GameConstants.TOP_LEFT_ANCHOR);
			return;
		}

		// Draw user
		drawUser(g, 0, 34, 154, 92, 140, 63, 133, leftChatImage);
		drawUser(g, 1, 242, 58, 225, 44, 197, 36, rightChatImage);
		drawUser(g, 2, 242, 154, 225, 140, 197, 133, rightChatImage);
		drawUser(g, 3, 34, 58, 92, 44, 63, 36, leftChatImage);

		if (Global.currentUser.getName().equals(
				Global.currentTable.getTableMasterName())) {
			if ((waitToStartGameTime > -1)
					&& (waitToStartGameTime <= MAX_START_TIME)) {
				g
						.drawImage(numberBgImage, 80, 160,
								GameConstants.CENTER_ANCHOR);
				text8.drawString(g, String
						.valueOf((MAX_START_TIME - waitToStartGameTime) / 10),
						Color.WHITE_CODE, 80, 160, GameConstants.CENTER_ANCHOR);
			}
		} else {
			if ((inRoomTime <= MAX_WAIT_TIME) && !Global.currentUser.isReady()) {
				g
						.drawImage(numberBgImage, 80, 160,
								GameConstants.CENTER_ANCHOR);
				text8.drawString(g, String
						.valueOf((MAX_WAIT_TIME - inRoomTime) / 10),
						Color.WHITE_CODE, 80, 160, GameConstants.CENTER_ANCHOR);
			}
		}
	}

	private void drawUser(Graphics g, int userIndex, int avatarX, int avatarY,
			int textX, int textY, int chatX, int chatY, Image chatImage) {
		if (userIndex > Global.tableUsers.size() - 1) {
			return;
		}

		User user = ((User) Global.tableUsers.elementAt(userIndex));
		Image avatar = (Image) Global.getAvatar(user.getName());
		g.drawImage(chatImage, chatX, chatY, GameConstants.TOP_LEFT_ANCHOR);

		if (user.getName().equals(Global.currentTable.getTableMasterName())) {
			text8.drawString(g, "Chủ phòng", Color.YELLOW_CODE, textX, textY,
					GameConstants.CENTER_ANCHOR);
		} else {
			if (user.isReady()) {
				text8.drawString(g, "Sẵn sàng", Color.WHITE_CODE, textX, textY,
						GameConstants.CENTER_ANCHOR);
			}
		}
		if (avatar == null) {
			g.drawImage(avatarImage, avatarX, avatarY,
					GameConstants.TOP_LEFT_ANCHOR);
		} else {
			g
					.drawImage(avatar, avatarX, avatarY,
							GameConstants.TOP_LEFT_ANCHOR);
		}
		text8.drawString(g, user.getName(), Color.WHITE_CODE, avatarX - 2,
				avatarY + 42, GameConstants.TOP_LEFT_ANCHOR);
		text8.drawString(g, "Đ:" + user.getMoney(), Color.YELLOW_CODE,
				avatarX + 1, avatarY + 54, GameConstants.TOP_LEFT_ANCHOR);
	}

	private void drawChat320240(Graphics g) {
		if (messageChats == null) {
			return;
		}

		for (int i = 0; i < messageChats.length; i++) {
			if (messageChats[i].equals("")) {
				continue;
			}

			switch (indexChatUser) {
			case 2:
				g.setColor(0xffffff);
				g.fillRect(216, 170, 95, 25);
				g.setColor(0x000000);
				g.fillRect(217, 171, 93, 23);
				text8.drawString(g, messageChats[i], Color.WHITE_CODE, 222,
						114, GameConstants.TOP_LEFT_ANCHOR);
				break;
			case 1:
				g.setColor(0xffffff);
				g.fillRect(215, 23, 95, 25);
				g.setColor(0x000000);
				g.fillRect(216, 24, 93, 23);
				text8.drawString(g, messageChats[i], Color.WHITE_CODE, 217, 25,
						GameConstants.TOP_LEFT_ANCHOR);
				break;
			case 3:
				g.setColor(0xffffff);
				g.fillRect(11, 170, 95, 25);
				g.setColor(0x000000);
				g.fillRect(12, 100, 93, 23);
				text8.drawString(g, messageChats[i], Color.WHITE_CODE, 17, 103,
						GameConstants.TOP_LEFT_ANCHOR);
				break;
			}
		}

		if (isInChatState) {
			g.setColor(0xffffff);
			g.fillRect(98, 192, 95, 25);
			g.setColor(0x000000);
			g.fillRect(99, 193, 93, 23);
			text8.drawString(g, messageChats[myIndex], Color.WHITE_CODE, 104,
					196, GameConstants.TOP_LEFT_ANCHOR);
			if (caretBlinkOn && goToNextChar) {
				g.drawLine(caretLeft, 0, caretLeft, inputHeight);
			}

			text8.drawString(g, "Quay lại", 0xffffff, 5,
					GameConstants.SCREEN_HEIGHT - 4,
					GameConstants.TOP_LEFT_ANCHOR);
			text8.drawString(g, "Gửi", 0xffffff,
					GameConstants.SCREEN_WIDTH / 2,
					GameConstants.SCREEN_HEIGHT - 4,
					GameConstants.TOP_HCENTER_ANCHOR);
			text8.drawString(g, "Xóa", 0xffffff,
					GameConstants.SCREEN_WIDTH - 5,
					GameConstants.SCREEN_HEIGHT - 4,
					GameConstants.TOP_RIGHT_ANCHOR);
		}
	}

	private void drawChat(Graphics g) {
		if (messageChats == null) {
			return;
		}

		for (int i = 0; i < messageChats.length; i++) {
			if (messageChats[i].equals("")) {
				continue;
			}

			switch (indexChatUser) {
			case 2:
				g.setColor(0xffffff);
				g.fillRect(140, 204, 95, 25);
				g.setColor(0x000000);
				g.fillRect(141, 205, 93, 23);
				text8.drawString(g, messageChats[i], Color.WHITE_CODE, 142,
						208, GameConstants.TOP_LEFT_ANCHOR);
				break;
			case 1:
				g.setColor(0xffffff);
				g.fillRect(100, 10, 95, 25);
				g.setColor(0x000000);
				g.fillRect(101, 11, 93, 23);
				text8.drawString(g, messageChats[i], Color.WHITE_CODE, 106, 14,
						GameConstants.TOP_LEFT_ANCHOR);
				break;
			case 3:
				g.setColor(0xffffff);
				g.fillRect(5, 75, 95, 25);
				g.setColor(0x000000);
				g.fillRect(6, 76, 93, 23);
				text8.drawString(g, messageChats[i], Color.WHITE_CODE, 7, 79,
						GameConstants.TOP_LEFT_ANCHOR);
				break;
			}
		}

		if (isInChatState) {
			g.setColor(0xffffff);
			g.fillRect(45, 270, 95, 25);
			g.setColor(0x000000);
			g.fillRect(46, 271, 93, 23);
			text8.drawString(g, messageChats[myIndex], 0xffffff, 51, 275,
					GameConstants.TOP_LEFT_ANCHOR);
			if (caretBlinkOn && goToNextChar) {
				g.drawLine(caretLeft, 0, caretLeft, inputHeight);
			}

			text8.drawString(g, "Quay lại", 0xffffff, 11, 303,
					GameConstants.TOP_LEFT_ANCHOR);
			text8.drawString(g, "Gửi", 0xffffff,
					GameConstants.SCREEN_WIDTH / 2, 303,
					GameConstants.TOP_HCENTER_ANCHOR);
			text8.drawString(g, "Xóa", 0xffffff,
					GameConstants.SCREEN_WIDTH - 10, 303,
					GameConstants.TOP_RIGHT_ANCHOR);
		}
	}

	public void writeKeyPressed320x240(int key) {
		if (goToNextChar || (key != lastPressedKey)) {
			goToNextChar = true;
			lastPressedKey = key;
			currentKeyStep = 0;
		} else {
			currentKeyStep++;
		}

		char charKey = (char) key;
		if (goToNextChar) {
			if (currentText.length() < 16) {
				currentText.insert(caretIndex, charKey);
				caretIndex++;
			}
		} else {
			currentText.setCharAt(caretIndex - 1, charKey);
		}
		messageChats[myIndex] = currentText.toString();
		updateCaretPosition();
		lastKeyTimestamp = System.currentTimeMillis();
		goToNextChar = false;
	}

	public void keyReleased(int keyCode) {
		keyCode = Key.getGameKey(keyCode);

		if (keyCode == Key.SOFT_LEFT) {
			if (Global.currentUser.getName().equals(
					Global.currentTable.getTableMasterName())
					&& !menu.getSubMenu(1).getLabel().equals(DAT_CUOC_LABEL)) {
				menu.insertItem(DAT_CUOC_LABEL, 1);
			}

			if (menu.isShowing()) {
				menu.hide();
			} else {
				if (Global.tableUsers.size() > 1) {
					nameUsers = new String[Global.tableUsers.size() - 1];
					indexFriend = new int[Global.tableUsers.size() - 1];
					int count = 0;
					for (int i = 0; i < Global.tableUsers.size(); i++) {
						if (!((User) Global.tableUsers.elementAt(i)).getName()
								.equals(Global.currentUser.getName())) {
							indexFriend[count] = i;
							nameUsers[count] = ((User) Global.tableUsers
									.elementAt(i)).getName();
							count++;
						}
					}
				}
				menu.show();
			}
			return;
		}

		if (menu.isShowing()) {
			return;
		}

		if ((keyCode >= Key.K_0) && (keyCode <= Key.K_9)
				&& GameConstants.IS_240x320_SCREEN) {
			if (!isInChatState) {
				clearChar();
			}

			if (isShowSettingBoard) {
				int order = keyCode - Key.K_0;
				writeKey(order, getChars(keyCode));
				return;
			} else {
				isInChatState = true;
				writeKeyPressed(keyCode);
			}
			return;
		}

		boolean isCommandKey = Key.isCommandKey;
		if (!GameConstants.IS_240x320_SCREEN && !isCommandKey) {
			if (!isInChatState) {
				resetText();
			}

			if (isShowSettingBoard) {
				writeKey320x240((char) keyCode);
				return;
			} else {
				isInChatState = true;
				writeKeyPressed320x240(keyCode);
			}
			return;
		}

		switch (keyCode) {
		case Key.SOFT_RIGHT:
			if (isShowSettingBoard) {
				if (tablePassword.length() > 0) {
					tablePassword.deleteCharAt(tablePassword.length() - 1);
				} else {
					isShowSettingBoard = false;
				}
			} else {
				if (isInChatState) {
					clearChar();
				} else {
					SocketClientUtil.leaveTableRequest();
					GameGlobal.nextState(Global.frmListTable, null,
							Transformer.TRANSFORM_WITH_LOADING_FORM);
				}
			}
			break;
		case Key.FIRE:
			if (Global.currentUser.getName().equals(
					Global.currentTable.getTableMasterName())) {
				if (isShowSettingBoard) {
					isInChatState = false;
					isShowSettingBoard = false;
					if (selectItemIndexInBoard != 3) {
						if (bid > Global.currentUser.getMoney()) {
							GameGlobal.alert
									.showAlert(
											this,
											Alert.OK_TYPE,
											new String[] {
													"Không thể đặt tiền cược",
													"lớn hơn số tiền bạn đang có" },
											98);
						} else {
							SocketClientUtil.configTable(bid, tablePassword
									.toString());
						}
					} else {

					}
				} else {
					if (isReadyToStart) {
						SocketClientUtil.startGame();
					}
				}
			} else {
				if (!Global.currentUser.isReady()) {
					SocketClientUtil.sendReady(true);
				}
			}
			if (isInChatState) {
				sendMessageToAllUser(messageChats[myIndex], CHAT_SEND);
				messageDelayCount[myIndex] = 0;
				isInChatState = false;
				return;
			}
			break;
		case Key.UP:
			if (isShowSettingBoard) {
				isInChatState = false;
				if (selectItemIndexInBoard > 0 && selectItemIndexInBoard < 3) {
					selectItemIndexInBoard--;
				} else if (selectItemIndexInBoard == 3) {
					selectItemIndexInBoard -= 2;
				}
			}
			break;
		case Key.DOWN:
			if (isShowSettingBoard) {
				isInChatState = false;
				selectItemIndexInBoard = (selectItemIndexInBoard + 1) % 3;
			}
			break;
		case Key.RIGHT:
			if (selectItemIndexInBoard == 0) {
				bid += 50;
				if (bid > Global.currentRoom.getMaxBid()) {
					bid = Global.currentRoom.getMaxBid();
				}
			} else if (selectItemIndexInBoard == 2) {
				selectItemIndexInBoard++;
			} else if (selectItemIndexInBoard == 3) {
				selectItemIndexInBoard = 2;
			}
			if (!isShowSettingBoard) {
				tabIndex = (tabIndex + 1) % 2;
				if (tabIndex == 1) {
					// Global.selectedIndexForFrmFriend = 0;
					// XmppUtil.getContactList();
				}
			}
			break;
		case Key.LEFT:
			if (selectItemIndexInBoard == 0) {
				if (selectItemIndexInBoard == 0) {
					bid -= 50;
					if (bid < Global.currentRoom.getMinBid()) {
						bid = Global.currentRoom.getMinBid();
					}
				}
			} else if (selectItemIndexInBoard == 3) {
				selectItemIndexInBoard = 2;
			} else if (selectItemIndexInBoard == 2) {
				selectItemIndexInBoard = 3;
			}
			if (!isShowSettingBoard) {
				tabIndex--;
				if (tabIndex < 0) {
					tabIndex = 1;
				}
				if (tabIndex == 1) {
					// Global.selectedIndexForFrmFriend = 0;
					// XmppUtil.getContactList();
				}
			}
			break;
		}
	}

	private void sendMessageToAllUser(String str, String header) {
		// for (int i = 0; i < Global.tableUsers.size(); i++) {
		// User user = (User) Global.tableUsers.elementAt(i);
		// if (!user.getJid().equals(Global.currentUser.getJid())) {
		// XmppUtil.sendMessage(user.getJid(), header + str);
		// }
		// }
	}

	private void writeKey320x240(char charKey) {
		if (selectItemIndexInBoard == 1) {
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
	}

	private void writeKey(int order, char[] charOfKeys) {
		if (selectItemIndexInBoard == 1) {
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
	}

	public void writeKeyPressed(int key) {
		if (goToNextChar || (key != lastPressedKey)) {
			goToNextChar = true;
			lastPressedKey = key;
			currentKeyStep = 0;
		} else {
			currentKeyStep++;
		}

		char[] chars = getChars(key);
		if (chars != null) {
			if (currentKeyStep >= chars.length) {
				currentKeyStep -= chars.length;
			}
			if (goToNextChar) {
				if (currentText.length() < 16) {
					currentText.insert(caretIndex, chars[currentKeyStep]);
					caretIndex++;
				}
			} else {
				currentText.setCharAt(caretIndex - 1, chars[currentKeyStep]);
			}
			messageChats[myIndex] = currentText.toString();
			updateCaretPosition();
			lastKeyTimestamp = System.currentTimeMillis();
			goToNextChar = false;
		}
	}

	private boolean checkIfAllUserReady() {
		if (Global.tableUsers.size() <= 1) {
			return false;
		}
		boolean isAllUserReady = true;
		for (int i = 0; i < Global.tableUsers.size(); i++) {
			User user = (User) Global.tableUsers.elementAt(i);
			if (user.getName().equals(Global.currentTable.getTableMasterName())) {
				continue;
			}

			if (!user.isReady()) {
				isAllUserReady = false;
				break;
			}
		}
		return isAllUserReady;
	}

	public void doTask() {
		if (Display.getDisplay(GameGlobal.getMidlet()).getCurrent() == GameGlobal.systemCanvas) {
			checkTimestamps();
		}
		if (Global.currentUser.getName().equals(
				Global.currentTable.getTableMasterName())) {
			isReadyToStart = checkIfAllUserReady();
			if (isReadyToStart && (!isShowSettingBoard || isConfigTableDone)
					&& !isStartGame) {
				if (waitToStartGameTime == -1) {
					waitToStartGameTime = 0;
				}

				if (Global.currentGame.getId().equals("tala")
						|| Global.currentGame.getId().equals("tlmb")
						|| Global.currentGame.getId().equals("tlmn")) {
					if (Global.tableUsers.size() < 4) {
						waitToStartGameTime = -1;
					}
				}

				if (waitToStartGameTime > -1) {
					waitToStartGameTime++;
					if (waitToStartGameTime > MAX_START_TIME) {
						isStartGame = true;
						SocketClientUtil.startGame();
					}
				}
			} else if (!isReadyToStart) {
				waitToStartGameTime = -1;
			}
		} else {
			inRoomTime++;
			if (inRoomTime > MAX_WAIT_TIME && !Global.currentUser.isReady()
					&& !isSendReady) {
				isSendReady = true;
				SocketClientUtil.sendReady(true);
			}
		}

		if (messageDelayCount != null) {
			for (int i = 0; i < messageDelayCount.length; i++) {
				if (messageDelayCount[i] > -1) {
					messageDelayCount[i]++;
					if (messageDelayCount[i] > 60) {
						messageChats[myIndex] = "";
						messageChats[i] = "";
						messageDelayCount[i] = -1;
					}
				}
			}
		}
	}

	public void onActionPerform(Event event) {
		// "Về menu chính", "Thông tin TK", "Mời chơi"
		String action = event.getAction();
		if (action.equals(MENU[0])) { // "Về menu chính"
			GameGlobal.alert.showAlert(this, Alert.YES_NO_TYPE,
					"Bạn có muốn thoát ra?", 97);
		} else if (action.equals(MENU[1])) { // Thông tin tài khoản
		} else if (action.equals(MENU[2])) { // Mời chơi
			SocketClientUtil.getUserWantToPlayList();
			System.out.println(">>>>>>>Send get user free list");
		} else if (action.equals("Thiết lập bàn")) { // Thiết lập bàn
			isShowSettingBoard = true;
		}
	}

	public void alertEventPerform(int alertType, int eventType, int alertId) {
		if (alertId == 99) { // Trong trường hợp người chơi bị kick ra ngoài
			GameGlobal.nextState(Global.frmListTable, null,
					Transformer.TRANSFORM_WITH_LOADING_FORM);
		} else if (alertId == 98) { // Khi chủ bàn không đủ tiền để đặt tiền
									// cược
			isShowSettingBoard = true;
			isInChatState = false;
		} else if (alertId == 97 && eventType == Alert.YES_BUTTON) { // Trong
																		// trường
																		// hợp
																		// người
																		// chơi
																		// thoát
																		// ra
																		// ngoài
			SocketClientUtil.leaveTableRequest();
			SocketClientUtil.leaveRoomRequest();
			GameGlobal.nextState(Global.frmChooseGame, null,
					Transformer.TRANSFORM_WITH_LOADING_FORM);
		} else if (alertId == 55 && alertType == Alert.YES_NO_TYPE
				&& eventType == Alert.YES_BUTTON) { // Nạp tiền bằng SMS
		// SMSHandler.getInstance().sendMsg(GameConstants.ADD_COIN_NUMBER, null,
		// "MGA " + Global.currentUser.getAcountNumber());
		} else if (alertId == 10 && eventType == Alert.NO_BUTTON) { // Trường
																	// hợp chủ
																	// bàn đặt
																	// lại điểm
																	// cược
			SocketClientUtil.leaveTableRequest();
			GameGlobal.nextState(Global.frmListTable, null,
					Transformer.TRANSFORM_WITH_LOADING_FORM);
		}
	}

	protected void destroy() {
		tabLongImage = null;
		messageChats = null;
		messageDelayCount = null;
		currentText = null;
		password = null;
		tabStrings = null;
		// tabImage = null;
		// tabFocusImage = null;
		popupImage = null;
		buttonImage = null;
		buttonFocusImage = null;
		menu = null;
		MENU = null;
		nameUsers = null;
		indexFriend = null;
		index = null;
		lastCharsKey = null;

		text8 = null;
		numberBgImage = null;
		leftChatImage = null;
		rightChatImage = null;
		avatarImage = null;
		ballImage = null;
		lImage = null;
		rightArrowImage = null;
		leftArrowImage = null;

		bodyFrameImage = null;
		tablePassword = null;
	}
}
