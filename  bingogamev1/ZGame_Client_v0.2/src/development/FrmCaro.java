package development;

import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import state.Alert;
import state.GameConstants;
import state.GameForm;
import state.GameGlobal;
import state.Key;
import state.TimerListener;
import state.Transformer;
import state.font.FontManager;
import state.font.ImageText;
import state.socket.DataPackage;
import state.socket.DataReceiveListener;
import state.util.Color;
import state.util.ImageUtil;
import development.bean.Game;
import development.socket.SocketClientUtil;

public class FrmCaro extends GameForm implements TimerListener,
		DataReceiveListener {
	private static final int MOVE_REQUEST = 1001; // Gửi thông tin về nước đã đi

	private static final int MOVE_ERROR_RESPONSE = 2001; // Báo lại cho ng chơi
															// vừa đánh là nước
															// đánh không được
															// chấp nhận
	private static final int MOVE_NOTIFY_RESPONSE = 2002; // Báo nước đi của
															// người vừa đánh
	private static final int WIN_GAME_NOTIFY_RESPONSE = 2005; // Báo cho các
																// user về user
																// đã thắng và
																// danh sách các
																// nước đi thắng
	private static final int INIT_DATA_RESPONSE = 2007; // Thiết đặt các thông
														// số từ server

	private static final int NOT_YOUR_TURN_ERROR = 3001;
	private static final int MOVE_IN_NOT_ALLOW_POSITION = 3002;

	private static final int NORMAL_WIN_REASON = 4001;
	private static final int OPPONENT_RESIGN_WIN_REASON = 4002;
	private static final int OPPONENT_TIMEOUT_WIN_REASON = 4003;

	private static final int WAIT_FOR_INIT_STATUS = 0;
	private static final int PLAY_STATUS = 1;
	private static final int CHAT_STATUS = 2;
	private static final int SHOW_WIN_OR_LOST_ALERT_STATUS = 3;
	private static final int SHOW_FINAL_RESULL = 4;

	private static final int NUMBER_OF_ROW = 100;
	private static final int NUMBER_OF_COLUMN = 100;
	private static final int ROUND_SIDE = -1;
	private static final int CROSS_SIDE = 1;

	public static final int CELL_SIZE = 15;

	private static int TIME_PER_MOVE;

	private static int NUMBER_COLUMN_DISPLAY;
	private static int NUMBER_ROW_DISPLAY;
	public static int BOARD_DX;
	public static int BOARD_DY;

	private Image lastMoveImage;
	private Image roundImage;
	private Image crossImage;
	private Image avatarImage;
	private Image bgImage;
	private Image pointerImage;

	private Image matnaImage;
	private Image randManaImage;
	private Image starImage;
	private Image thangImage;
	private Image thuaImage;
	private Image thuaImage1;
	private Image thangImage1;

	private Image leftImage;
	private Image rightImage;
	private Image bodyFrameImage;

	private int currentGameStatus;

	private int[][] matrix = new int[NUMBER_OF_COLUMN][NUMBER_OF_ROW];
	private int currentX;
	private int currentY;

	private int currentTurn;
	private int mySide;
	private int opponentSide;
	private String oponentName;

	private boolean isTurnOnAutoCamera;
	private int desX;
	private int desY;
	private int dRow;
	private int dColumn;
	private int minRow;
	private int maxRow;
	private int minColumn;
	private int maxColumn;
	private int step;

	private int timeMove;
	private int count;

	private int winSide;
	private int winReason;
	private int winPointX1;
	private int winPointY1;
	private int winPointX2;
	private int winPointY2;
	private String[] moneyChangePoint = new String[2];

	private ImageText text8;

	public void init(Hashtable inputParameters) {
		Image image = ImageUtil.getImage("caro.png");
		crossImage = ImageUtil.getSubImage(image, 0, 0, 11, 12, true);
		roundImage = ImageUtil.getSubImage(image, 11, 0, 11, 12, true);
		image = null;
		if (GameConstants.IS_240x320_SCREEN) {
			bgImage = ImageUtil.getImage("caro_bg.jpg");
		} else {
			bgImage = ImageUtil.getImage("caro_bg_320240.png");
		}

		pointerImage = ImageUtil.getImage("caro_pointer.png");
		lastMoveImage = ImageUtil.getImage("b.png");
		starImage = ImageUtil.getImage("HieuUng.png");
		avatarImage = ImageUtil.getImage("Avatar.png");
		randManaImage = ImageUtil.getImage("pxiel_nen.png");
		matnaImage = ImageUtil.joinAndCreateImages(randManaImage,
				GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT - 20,
				true);
		thuaImage = ImageUtil.getImage("Thua_1.png");
		thuaImage1 = ImageUtil.getImage("Thua_2.png");
		thangImage = ImageUtil.getImage("Thang_1.png");
		thangImage1 = ImageUtil.getImage("Thang_2.png");

		Image popUpImage = ImageUtil.getImage("PopUp3.png");
		leftImage = ImageUtil.getSubImage(popUpImage, 0, 0, 14, 98, true);
		rightImage = ImageUtil.getSubImage(popUpImage, 21, 0, 12, 98, true);
		bodyFrameImage = ImageUtil.getSubImage(popUpImage, 17, 0, 1, 98, true);
		bodyFrameImage = ImageUtil.joinAndCreateImages(bodyFrameImage, 157, 98,
				true);
		popUpImage = null;

		if (GameConstants.IS_240x320_SCREEN) {
			BOARD_DX = 15;
			BOARD_DY = 70;
			NUMBER_COLUMN_DISPLAY = 14;
			NUMBER_ROW_DISPLAY = 14;
		} else {
			BOARD_DX = 67;
			BOARD_DY = 36;
			NUMBER_COLUMN_DISPLAY = 14;
			NUMBER_ROW_DISPLAY = 12;
		}

		currentX = 50;
		currentY = 50;
		dRow = 43;
		dColumn = 43;
		minRow = currentY;
		maxRow = currentY;
		minColumn = currentX;
		maxColumn = currentX;
		count = 0;
		step = 0;

		winPointX1 = 0;
		winPointY1 = 0;
		winPointX2 = 0;
		winPointY2 = 0;

		recalculateGround(currentX, currentY);

		for (int i = 0; i < NUMBER_OF_COLUMN; i++) {
			for (int j = 0; j < NUMBER_OF_ROW; j++) {
				matrix[i][j] = 0;
			}
		}
		text8 = FontManager.getFont(FontManager.FONT_SIZE_8);

		isTurnOnAutoCamera = false;
		desX = 0;
		desY = 0;
		GameGlobal.setTimerDelay(100);
		setTimerListener(this);
	}

	public void onConnectDone() {
	}

	public void onConnectFail() {
	}

	public void onDisconnect() {
	}

	public void onRecieveData(DataPackage dataPackage) {
		int header = dataPackage.getHeader();
		switch (header) {
		case MOVE_NOTIFY_RESPONSE:
			int side = dataPackage.nextInt();
			int x = dataPackage.nextInt();
			int y = dataPackage.nextInt();
			fire(side, x, y);
			currentTurn = mySide;
			break;
		case WIN_GAME_NOTIFY_RESPONSE:
			winSide = dataPackage.nextInt();
			winReason = dataPackage.nextInt();
			winPointX1 = dataPackage.nextInt();
			winPointY1 = dataPackage.nextInt();
			winPointX2 = dataPackage.nextInt();
			winPointY2 = dataPackage.nextInt();
			moneyChangePoint[0] = dataPackage.nextString();
			moneyChangePoint[1] = dataPackage.nextString();

			// Kiểm tra xem là thắng cuộc trong trường hợp nào
			if (winReason == NORMAL_WIN_REASON) {
				currentGameStatus = SHOW_WIN_OR_LOST_ALERT_STATUS;
			} else if (winReason == OPPONENT_RESIGN_WIN_REASON) {
				GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {
						"Đối thủ đã bỏ cuộc", "Bạn đã thắng" }, 97);
			} else if (winReason == OPPONENT_TIMEOUT_WIN_REASON) {
				if (winSide == mySide) {
					GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
							new String[] { "Đối thủ đã hết thời gian",
									"Bạn đã thắng" }, 97);
				} else {
					GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
							new String[] { "Thời gian cho nước đi đã hết",
									"Bạn đã thua" }, 97);
				}
			}
			break;
		case MOVE_ERROR_RESPONSE:
			int error = dataPackage.nextInt();
			if (error == NOT_YOUR_TURN_ERROR) {
				GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
						"Không phải lượt đánh của bạn");
			} else if (error == MOVE_IN_NOT_ALLOW_POSITION) {
				GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
						"Nước đi không hợp lệ");
			} else {
				GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
						"Nước đi không được phép");
			}
			break;
		case INIT_DATA_RESPONSE:
			TIME_PER_MOVE = dataPackage.nextInt();
			timeMove = TIME_PER_MOVE;

			for (int i = 0; i < 2; i++) {
				int side1 = dataPackage.nextInt();
				String userName = dataPackage.nextString();
				if (userName.equals(Global.currentUser.getName())) {
					mySide = side1;
				} else {
					oponentName = userName;
					opponentSide = side1;
				}
			}
			if (mySide == CROSS_SIDE) {
				currentTurn = mySide;
			}
			currentGameStatus = PLAY_STATUS;
			break;
		}
	}

	public void draw(Graphics g) {
		// Vẽ nền game caro
		g.drawImage(bgImage, 0, 0, GameConstants.TOP_LEFT_ANCHOR);

		// Vẽ ô bàn đấu
		g.setColor(0xA9C2A6);
		for (int i = 0; i <= NUMBER_ROW_DISPLAY; i++) {
			g.drawLine(BOARD_DX, BOARD_DY + CELL_SIZE * i, BOARD_DX + CELL_SIZE
					* NUMBER_COLUMN_DISPLAY, BOARD_DY + CELL_SIZE * i);
			if (i < NUMBER_ROW_DISPLAY) {
				text8.drawString(g, String.valueOf(dRow + i), Color.BLACK_CODE,
						BOARD_DX - 13, BOARD_DY + CELL_SIZE * i,
						GameConstants.TOP_LEFT_ANCHOR);
			}
		}
		for (int j = 0; j <= NUMBER_COLUMN_DISPLAY; j++) {
			g.drawLine(BOARD_DX + CELL_SIZE * j, BOARD_DY, BOARD_DX + CELL_SIZE
					* j, BOARD_DY + CELL_SIZE * NUMBER_ROW_DISPLAY);
			if (j < NUMBER_COLUMN_DISPLAY) {
				text8.drawString(g, String.valueOf(dColumn + j),
						Color.BLACK_CODE, BOARD_DX + CELL_SIZE * j + 3,
						BOARD_DY - 14, GameConstants.TOP_LEFT_ANCHOR);
			}
		}

		// Vẽ các điểm đã đánh của cả 2 bên
		for (int i = 0; i < NUMBER_COLUMN_DISPLAY; i++) {
			for (int j = 0; j < NUMBER_ROW_DISPLAY; j++) {
				if (matrix[dColumn + i][dRow + j] == CROSS_SIDE) {
					g.drawImage(crossImage, BOARD_DX + i * CELL_SIZE + 2,
							BOARD_DY + j * CELL_SIZE + 2,
							GameConstants.TOP_LEFT_ANCHOR);
				} else if (matrix[dColumn + i][dRow + j] == ROUND_SIDE) {
					g.drawImage(roundImage, BOARD_DX + i * CELL_SIZE + 2,
							BOARD_DY + j * CELL_SIZE + 2,
							GameConstants.TOP_LEFT_ANCHOR);
				}
			}
		}

		// Vẽ nước đánh trước đó
		if (currentTurn == mySide) {
			g.drawImage(lastMoveImage, BOARD_DX + 8 + CELL_SIZE
					* (desX - dColumn), BOARD_DY + 8 + CELL_SIZE
					* (desY - dRow), GameConstants.CENTER_ANCHOR);
		}

		// Vẽ con trỏ
		g.drawImage(pointerImage, BOARD_DX + 5 + CELL_SIZE
				* (currentX - dColumn), BOARD_DY + 5 + CELL_SIZE
				* (currentY - dRow), GameConstants.TOP_LEFT_ANCHOR);

		// Vẽ command
		g.setColor(0x2B0102);// Màu nền task bar
		g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25,
				GameConstants.SCREEN_WIDTH, 25);
		if (currentGameStatus != SHOW_FINAL_RESULL
				&& currentGameStatus != SHOW_WIN_OR_LOST_ALERT_STATUS) {
			text8.drawString(g, "Thoát", Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH - 8,
					GameConstants.SCREEN_HEIGHT - 4,
					GameConstants.BOTTOM_RIGHT_ANCHOR);
		}

		// Vẽ avatar của người chơi
		Image avatar = Global.getAvatar(oponentName);
		if (avatar == null) {
			g.drawImage(avatarImage, 3, 26, GameConstants.TOP_LEFT_ANCHOR);
		} else {
			g.drawImage(avatar, 3, 26, GameConstants.TOP_LEFT_ANCHOR);
		}
		text8.drawString(g, "" + oponentName, Color.WHITE_CODE, 5, 8,
				GameConstants.TOP_LEFT_ANCHOR);

		// Vẽ đếm thời gian và báo lượt đi
		if (currentTurn == mySide) {
			text8.drawString(g, "Lượt đi của " + Global.currentUser.getName(),
					Color.YELLOW_CODE, GameConstants.SCREEN_WIDTH - 55, 8,
					GameConstants.TOP_RIGHT_ANCHOR);
		} else {
			text8.drawString(g, "Lượt đi của " + oponentName,
					Color.YELLOW_CODE, GameConstants.SCREEN_WIDTH - 55, 8,
					GameConstants.TOP_RIGHT_ANCHOR);
		}
		text8.drawString(g, "00:" + String.valueOf(timeMove), Color.WHITE_CODE,
				GameConstants.SCREEN_WIDTH - 50, 8,
				GameConstants.TOP_LEFT_ANCHOR);

		if (Global.userAvatar != null) {
			g.drawImage(Global.userAvatar, 3, GameConstants.SCREEN_HEIGHT - 40,
					GameConstants.BOTTOM_LEFT_ANCHOR);
		} else {
			g.drawImage(avatarImage, 3, GameConstants.SCREEN_HEIGHT - 40,
					GameConstants.BOTTOM_LEFT_ANCHOR);
		}
		text8
				.drawString(g, Global.currentUser.getName(), Color.WHITE_CODE,
						5, GameConstants.SCREEN_HEIGHT - 30,
						GameConstants.TOP_LEFT_ANCHOR);

		switch (currentGameStatus) {
		case SHOW_WIN_OR_LOST_ALERT_STATUS:
			g.drawImage(matnaImage, 0, 0, GameConstants.TOP_LEFT_ANCHOR);
			g.drawImage(starImage, GameConstants.SCREEN_WIDTH / 2,
					(GameConstants.SCREEN_HEIGHT - 20) / 2,
					GameConstants.CENTER_ANCHOR);

			if (winReason == NORMAL_WIN_REASON) {
				g.setColor(Color.BLACK_CODE);
				g.drawLine((winPointX1 - dColumn) * 15 + 7 + BOARD_DX,
						(winPointY1 - dRow) * 15 + 7 + BOARD_DY,
						(winPointX2 - dColumn) * 15 + 7 + BOARD_DX,
						(winPointY2 - dRow) * 15 + 7 + BOARD_DY);
			}

			if (winSide == mySide) {
				if (step % 5 == 0) {
					g.drawImage(thangImage, GameConstants.SCREEN_WIDTH / 2,
							(GameConstants.SCREEN_HEIGHT - 20) / 2,
							GameConstants.CENTER_ANCHOR);
				} else {
					g.drawImage(thangImage1, GameConstants.SCREEN_WIDTH / 2,
							(GameConstants.SCREEN_HEIGHT - 20) / 2,
							GameConstants.CENTER_ANCHOR);
				}
			} else {
				if (step % 5 == 0) {
					g.drawImage(thuaImage, GameConstants.SCREEN_WIDTH / 2,
							(GameConstants.SCREEN_HEIGHT - 20) / 2,
							GameConstants.CENTER_ANCHOR);
				} else {
					g.drawImage(thuaImage1, GameConstants.SCREEN_WIDTH / 2,
							(GameConstants.SCREEN_HEIGHT - 20) / 2,
							GameConstants.CENTER_ANCHOR);
				}
			}
			break;
		case SHOW_FINAL_RESULL:
			if (GameConstants.IS_240x320_SCREEN) {
				g.drawImage(leftImage, 28, 82, GameConstants.TOP_LEFT_ANCHOR);
				g.drawImage(bodyFrameImage, 42, 82,
						GameConstants.TOP_LEFT_ANCHOR);
				g.drawImage(rightImage, 199, 82, GameConstants.TOP_LEFT_ANCHOR);
				text8.drawString(g, "KẾT QUẢ", Color.WHITE_CODE,
						GameConstants.SCREEN_WIDTH / 2, 104,
						GameConstants.TOP_HCENTER_ANCHOR);
				g.setColor(Color.RED2_CODE);
				g.drawLine(35, 121, 205, 121);
				text8.drawString(g, "Thắng:", Color.WHITE_CODE, 43, 129,
						GameConstants.TOP_LEFT_ANCHOR);
				text8.drawString(g, "Thua:", Color.WHITE_CODE, 43, 147,
						GameConstants.TOP_LEFT_ANCHOR);
				if (winSide == mySide) {
					text8.drawString(g, Global.currentUser.getName(),
							Color.WHITE_CODE, 88, 129,
							GameConstants.TOP_LEFT_ANCHOR);
					text8.drawString(g, oponentName, Color.WHITE_CODE, 88, 147,
							GameConstants.TOP_LEFT_ANCHOR);
				} else {
					text8.drawString(g, oponentName, Color.WHITE_CODE, 88, 129,
							GameConstants.TOP_LEFT_ANCHOR);
					text8.drawString(g, Global.currentUser.getName(),
							Color.WHITE_CODE, 88, 147,
							GameConstants.TOP_LEFT_ANCHOR);
				}
				text8.drawString(g, moneyChangePoint[0] + " điểm",
						Color.WHITE_CODE, 161, 129,
						GameConstants.TOP_LEFT_ANCHOR);
				text8.drawString(g, moneyChangePoint[1] + " điểm",
						Color.WHITE_CODE, 161, 147,
						GameConstants.TOP_LEFT_ANCHOR);
			} else {
				g.drawImage(leftImage, 68, 82, GameConstants.TOP_LEFT_ANCHOR);
				g.drawImage(bodyFrameImage, 82, 82,
						GameConstants.TOP_LEFT_ANCHOR);
				g.drawImage(rightImage, 239, 82, GameConstants.TOP_LEFT_ANCHOR);
				text8.drawString(g, "KẾT QUẢ", Color.WHITE_CODE,
						GameConstants.SCREEN_WIDTH / 2, 104,
						GameConstants.TOP_HCENTER_ANCHOR);
				g.setColor(Color.RED2_CODE);
				g.drawLine(75, 121, 205, 121);
				text8.drawString(g, "Thắng:", Color.WHITE_CODE, 83, 129,
						GameConstants.TOP_LEFT_ANCHOR);
				text8.drawString(g, "Thua:", Color.WHITE_CODE, 83, 147,
						GameConstants.TOP_LEFT_ANCHOR);
				if (winSide == mySide) {
					text8.drawString(g, Global.currentUser.getName(),
							Color.WHITE_CODE, 128, 129,
							GameConstants.TOP_LEFT_ANCHOR);
					text8.drawString(g, oponentName, Color.WHITE_CODE, 128,
							147, GameConstants.TOP_LEFT_ANCHOR);
				} else {
					text8.drawString(g, oponentName, Color.WHITE_CODE, 128,
							129, GameConstants.TOP_LEFT_ANCHOR);
					text8.drawString(g, Global.currentUser.getName(),
							Color.WHITE_CODE, 128, 147,
							GameConstants.TOP_LEFT_ANCHOR);
				}
				text8.drawString(g, moneyChangePoint[0] + " điểm",
						Color.WHITE_CODE, 201, 129,
						GameConstants.TOP_LEFT_ANCHOR);
				text8.drawString(g, moneyChangePoint[1] + " điểm",
						Color.WHITE_CODE, 201, 147,
						GameConstants.TOP_LEFT_ANCHOR);
			}
			break;

		case CHAT_STATUS:
			// if (Constants.IS_240x320_SCREEN) {
			// drawChat(g);
			// } else {
			// drawChat320_240(g);
			// }
			break;
		}
	}

	// private void drawChat320_240(Graphics g) {
	// if (currentString.length() > 0) {
	// g.setColor(0x2B0102);// Màu nền task bar
	// g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25,
	// GameConstants.SCREEN_WIDTH, 25);
	// text8.drawString(g, "Quay lại", 0xffffff, 11, 303,
	// GameConstants.TOP_LEFT_ANCHOR);
	// text8.drawString(g, "Gửi", 0xffffff, GameConstants.SCREEN_WIDTH / 2,
	// GameConstants.SCREEN_HEIGHT - 8, GameConstants.BOTTOM_HCENTER_ANCHOR);
	// text8.drawString(g, "Xóa", 0xffffff, GameConstants.SCREEN_WIDTH - 10,
	// GameConstants.SCREEN_HEIGHT - 8, GameConstants.BOTTOM_RIGHT_ANCHOR);
	//
	// g.setColor(0xffffff);
	// g.fillRect(56, 220, 95, 25);
	// g.setColor(0x000000);
	// g.fillRect(57, 221, 93, 23);
	// text8.drawString(g, currentString, 0xffffff, 58, 225,
	// GameConstants.TOP_LEFT_ANCHOR);
	// if (caretBlinkOn && goToNextChar && isInChatState) {
	// g.drawLine(caretLeft, 0, caretLeft, inputHeight);
	// }
	//
	// }
	//
	// if (oppMessageChat != null && oppMessageChat.length() > 0) {
	// g.setColor(0xffffff);
	// g.fillRect(110, 0, 95, 25);
	// g.setColor(0x000000);
	// g.fillRect(111, 1, 93, 23);
	// text8.drawString(g, oppMessageChat, Color.WHITE_CODE, 111, 5,
	// GameConstants.TOP_LEFT_ANCHOR);
	// }
	// }

	// private void drawChat(Graphics g) {
	// if (currentString.length() > 0) {
	// g.setColor(0xffffff);
	// g.fillRect(49, 270, 95, 25);
	// g.setColor(0x000000);
	// g.fillRect(50, 271, 93, 23);
	// text8.drawString(g, currentString, 0xffffff, 55, 275,
	// GameConstants.TOP_LEFT_ANCHOR);
	// if (caretBlinkOn && goToNextChar && isInChatState) {
	// g.drawLine(caretLeft, 0, caretLeft, inputHeight);
	// }
	//
	// g.setColor(0x2B0102);// Màu nền task bar
	// g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25,
	// GameConstants.SCREEN_WIDTH, 25);
	// text8.drawString(g, "Quay lại", 0xffffff, 11, 303,
	// GameConstants.TOP_LEFT_ANCHOR);
	// text8.drawString(g, "Gửi", 0xffffff, GameConstants.SCREEN_WIDTH / 2, 303,
	// GameConstants.TOP_HCENTER_ANCHOR);
	// text8.drawString(g, "Xóa", 0xffffff, GameConstants.SCREEN_WIDTH - 10,
	// 303, GameConstants.TOP_RIGHT_ANCHOR);
	// }
	//
	// if (oppMessageChat != null && oppMessageChat.length() > 0) {
	// g.setColor(0xffffff);
	// g.fillRect(6, 34, 95, 25);
	// g.setColor(0x000000);
	// g.fillRect(7, 35, 93, 23);
	// text8.drawString(g, oppMessageChat, Color.WHITE_CODE, 12, 38,
	// GameConstants.TOP_LEFT_ANCHOR);
	// }
	// }

	public void keyReleased(int keyCode) {
		if (currentGameStatus == WAIT_FOR_INIT_STATUS) {
			return;
		}
		isTurnOnAutoCamera = false;
		keyCode = Key.getGameKey(keyCode);

		if (currentGameStatus == SHOW_WIN_OR_LOST_ALERT_STATUS) {
			if (keyCode == Key.FIRE) {
				currentGameStatus = SHOW_FINAL_RESULL;
			}
			return;
		}

		if (currentGameStatus == SHOW_FINAL_RESULL) {
			if (keyCode == Key.FIRE) {
				Global.frmTable.isReturnFromGame = true;
				GameGlobal.nextState(Global.frmTable, null,
						Transformer.TRANSFORM_WITH_LOADING_FORM);
			}
			return;
		}

		switch (keyCode) {
		case Key.UP:
			if (currentY > dRow) {
				currentY--;
			} else if (dRow > 0) {
				if (dRow > minRow - NUMBER_ROW_DISPLAY / 2) {
					dRow--;
					currentY--;
				}
			}
			break;
		case Key.DOWN:
			if (currentY < dRow + NUMBER_ROW_DISPLAY - 1) {
				currentY++;
			} else if (dRow < NUMBER_OF_ROW) {
				if (dRow < maxRow - NUMBER_ROW_DISPLAY / 2) {
					dRow++;
					currentY++;
				}
			}
			break;
		case Key.LEFT:
			if (currentX > dColumn) {
				currentX--;
			} else if (dColumn > 0) {
				if (dColumn > minColumn - NUMBER_COLUMN_DISPLAY / 2) {
					dColumn--;
					currentX--;
				}
			}
			break;
		case Key.RIGHT:
			if (currentX < dColumn + NUMBER_COLUMN_DISPLAY - 1) {
				currentX++;
			} else if (dColumn < NUMBER_OF_COLUMN) {
				if (dColumn < maxColumn - NUMBER_COLUMN_DISPLAY / 2) {
					dColumn++;
					currentX++;
				}
			}
			break;
		case Key.SOFT_RIGHT:
			if ((currentGameStatus != SHOW_FINAL_RESULL)
					&& (currentGameStatus != SHOW_WIN_OR_LOST_ALERT_STATUS)) {
				GameGlobal.alert.showAlert(this, Alert.YES_NO_TYPE,
						new String[] { "Thoát ra bị tính là thua cuộc",
								"Bạn có muốn thoát ra?" }, 98);
			}
			break;
		case Key.SOFT_LEFT:
			break;
		case Key.FIRE:
			if (matrix[currentX][currentY] != 0) {
				return;
			}

			if (mySide != currentTurn) {
				return;
			}

			fire(mySide, currentX, currentY);
			break;
		}
	}

	private void fire(int side, int x, int y) {
		matrix[x][y] = side;

		if (side == mySide) {
			// Gửi yêu cầu đánh
			DataPackage dataPackage = Game.createPackage(MOVE_REQUEST);
			dataPackage.putInt(x);
			dataPackage.putInt(y);
			Global.gameActionClient.write(dataPackage);
			currentTurn = opponentSide;
		} else {
			// Turn on camera
			desX = x;
			desY = y;
			recalculateGround(x, y);
			isTurnOnAutoCamera = true;

			// Change turn
			currentTurn = mySide;
		}
		timeMove = TIME_PER_MOVE;
	}

	private void recalculateGround(int cellX, int cellY) {
		if (minRow > cellY) {
			minRow = cellY;
		}
		if (maxRow < cellY) {
			maxRow = cellY;
		}
		if (minColumn > cellX) {
			minColumn = cellX;
		}
		if (maxColumn < cellX) {
			maxColumn = cellX;
		}
	}

	public void alertEventPerform(int alertType, int eventType, int alertId) {
		if (alertId == 98) { // Sự kiện thoát ra giữa ván
			if (eventType == Alert.YES_BUTTON) {
				SocketClientUtil.leaveTableRequest();
				GameGlobal.nextState(Global.frmListTable, null,
						Transformer.TRANSFORM_WITH_LOADING_FORM);
			}
			return;
		} else if (alertId == 97) { // Sự kiện báo đối phương đã thoát hoặc time
									// out
			currentGameStatus = SHOW_FINAL_RESULL;
		}
	}

	public void doTask() {
		step = (step + 1) % 200;
		count = (count + 1) % 200;
		if (count % 10 == 0) {
			if (timeMove > 0) {
				timeMove--;
			}
		}
		runAutoCamera();
	}

	private void runAutoCamera() {
		if (isTurnOnAutoCamera) {
			if (dColumn < desX - NUMBER_COLUMN_DISPLAY / 2
					&& dColumn + NUMBER_COLUMN_DISPLAY < NUMBER_OF_COLUMN) {
				dColumn++;
				currentX++;
			} else if (dColumn > desX - NUMBER_COLUMN_DISPLAY / 2
					&& dColumn > 0) {
				dColumn--;
				currentX--;
			}
			if (dRow < desY - NUMBER_ROW_DISPLAY / 2
					&& dRow + NUMBER_ROW_DISPLAY < NUMBER_OF_ROW) {
				dRow++;
				currentY++;
			} else if (dRow > desY - NUMBER_ROW_DISPLAY / 2 && dRow > 0) {
				dRow--;
				currentY--;
			}
		}
	}

	protected void destroy() {
		currentGameStatus = WAIT_FOR_INIT_STATUS;
		lastMoveImage = null;
		roundImage = null;
		crossImage = null;

		pointerImage = null;
		bgImage = null;

		avatarImage = null;
		matnaImage = null;
		randManaImage = null;
		starImage = null;
		thangImage = null;
		thuaImage = null;
		thangImage1 = null;
		thuaImage1 = null;
		oponentName = null;
		leftImage = null;
		rightImage = null;
		bodyFrameImage = null;

		text8 = null;
		oponentName = null;
	}
}
