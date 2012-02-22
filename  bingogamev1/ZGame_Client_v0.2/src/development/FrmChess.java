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
import development.bean.ChessMan;
import development.bean.Game;
import development.socket.SocketClientUtil;

public class FrmChess extends GameForm implements TimerListener, DataReceiveListener {
	private static final int MOVE_REQUEST = 1001; // Gửi thông tin về nước đã đi

	private static final int CHIEU_TUONG_NOTIFY_RESPONSE = 2005;
	private static final int WIN_GAME_NOTIFY_RESPONSE = 2006; // Báo cho các user về user đã thắng và danh sách các nước đi thắng
	private static final int INIT_DATA_RESPONSE = 2007; // Thiết đặt các thông số cho client
	private static final int MOVE_ERROR_RESPONSE = 2008; // Báo lại cho ng chơi vừa đánh là nước đánh không được chấp nhận
	private static final int MOVE_NOTIFY_RESPONSE = 2009; // Báo nước đi của người vừa đánh

	private static final int NOT_YOUR_TURN_ERROR = 3001;
	private static final int MOVE_IN_NOT_ALLOW_POSITION = 3002;

	private static final int NORMAL_WIN_REASON = 4001;
	private static final int OPPONENT_RESIGN_WIN_REASON = 4002;
	private static final int OPPONENT_TIMEOUT_WIN_REASON = 4003;

	public static int BOARD_DX;
	public static int BOARD_DY;
	public static int CELL_SIZE;
	
	private static final int NUMBER_OF_ROW = 10;
	private static final int NUMBER_OF_COLUMN = 9;
	private static int TIME_PER_MOVE;

	private static final int WAIT_FOR_INIT_STATUS = 0;
	private static final int PLAY_STATUS = 1;
	private static final int CHAT_STATUS = 2;
	private static final int SHOW_WIN_OR_LOST_ALERT_STATUS = 3;
	private static final int SHOW_FINAL_RESULL = 4;

	private Image areaImage;
	private Image lastMoveSmallImage;
	private Image lastMoveLargeImage;
	private Image avatarImage;
	private Image chessBoardImage;
	private Image chessMansImage;
	private Image bgImage;
	private Image oppImage;
	private Image myImage;

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

	private ChessMan[][] chessMans;
	private ChessMan currentSelectArea;
	private ChessMan currentSelectArea1;
	private ChessMan selectedChessMan;
	private ChessMan lastMoveSmallSprite;
	private ChessMan lastMoveLargeSprite;

	private ImageText text11;
	private ImageText text8;

	private int mySide;
	private int opponentSide;
	private int currentTurn;
	private int chessManIndex;
	private int currentGameStatus;

	private int winSide;
	private int winReason;
	private String[] moneyChangePoint = new String[2];
	
	private int[] timePlay = new int[2];
	private int timeMove;
	private int step;
	private int count;

	private boolean isChieuTuong;
	private String oponentName;

	public void init(Hashtable parameters) {
		chessBoardImage = ImageUtil.getImage("cotuong_banco.png");
		bgImage = ImageUtil.getImage("cotuong_nen.jpg");

		isChieuTuong = false;
		text11 = FontManager.getFont(FontManager.FONT_SIZE_11);
		text8 = FontManager.getFont(FontManager.FONT_SIZE_8);

		step = 0;
		GameGlobal.setTimerDelay(100);
		setTimerListener(this);
	}

	public void onRecieveData(DataPackage dataPackage) {
		int header = dataPackage.getHeader();
		switch (header) {
		case MOVE_NOTIFY_RESPONSE:
			int side = dataPackage.nextInt();
			int chessManIndex = dataPackage.nextInt();
			int column = dataPackage.nextInt();
			int row = dataPackage.nextInt();
			
			int otherSide = ChessMan.RED_SIDE;
			if (side == ChessMan.RED_SIDE) {
				otherSide = ChessMan.BLACK_SIDE;
			}
			
			if (mySide == ChessMan.BLACK_SIDE) {
				chessManIndex = getInverseIndex(chessManIndex);
				column = NUMBER_OF_COLUMN - column - 1;
				row = NUMBER_OF_ROW - row - 1;
			}
			
			ChessMan chessMan = chessMans[side][chessManIndex];
			if (side == opponentSide) {
				lastMoveSmallSprite.changePosition(chessMan.getColumn(), chessMan.getRow());
				lastMoveSmallSprite.setVisible(true);
			}
			
			chessMan.changePosition(column, row);
			ChessMan myChessMan = getChessMan(otherSide, column, row);
			if (myChessMan != null) {
				killChessMan(myChessMan);
			}
			
			if (side == opponentSide) {
				lastMoveLargeSprite.changePosition(column, row);
				lastMoveLargeSprite.setVisible(true);
			}
			changeTurn();
			break;
		case WIN_GAME_NOTIFY_RESPONSE:
			winSide = dataPackage.nextInt();
			winReason = dataPackage.nextInt();
			moneyChangePoint[0] = dataPackage.nextString();
			moneyChangePoint[1] = dataPackage.nextString();
			
			// Kiểm tra xem là thắng cuộc trong trường hợp nào
			if (winReason == NORMAL_WIN_REASON) {
				currentGameStatus = SHOW_WIN_OR_LOST_ALERT_STATUS;
			} else if (winReason == OPPONENT_RESIGN_WIN_REASON) {
			  GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {"Đối thủ đã bỏ cuộc", "Bạn đã thắng"}, 97);
			} else if (winReason == OPPONENT_TIMEOUT_WIN_REASON) {
				if (winSide == mySide) {
				  GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {"Đối thủ đã hết thời gian", "Bạn đã thắng"}, 97);
				} else {
				  GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {"Thời gian cho nước đi đã hết", "Bạn đã thua"}, 97);
				}
			}
			break;
		case MOVE_ERROR_RESPONSE:
			int error = dataPackage.nextInt();
			if (error == NOT_YOUR_TURN_ERROR) {
			  GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Không phải lượt đánh của bạn");
			} else if (error == MOVE_IN_NOT_ALLOW_POSITION) {
//				systemCanvas.getAlert().showAlert(this, Alert.OK_TYPE, "Nước đi không hợp lệ");
			} else {
			  GameGlobal.alert.showAlert(this, Alert.OK_TYPE, "Nước đi không được phép");
			}
			break;
		case CHIEU_TUONG_NOTIFY_RESPONSE:
			isChieuTuong = true;
			break;
		case INIT_DATA_RESPONSE:
			initForGame(dataPackage);
			break;
		}
	}

	private void initForGame(DataPackage dataPackage) {
		// Init time
		int timePerMatch = dataPackage.nextInt();
		TIME_PER_MOVE = dataPackage.nextInt();
		timeMove = TIME_PER_MOVE;
		timePlay = new int[2];
		timePlay[0] = timePerMatch;
		timePlay[1] = timePerMatch;

		// Init side
		String redSideName = dataPackage.nextString();
		if (redSideName.equals(Global.currentUser.getName())) {
			mySide = ChessMan.RED_SIDE;
			opponentSide = ChessMan.BLACK_SIDE;
			oponentName = dataPackage.nextString();
		} else {
			mySide = ChessMan.BLACK_SIDE;
			opponentSide = ChessMan.RED_SIDE;
			oponentName = redSideName;
		}
		currentTurn = ChessMan.RED_SIDE;

		initChessBoard();
		currentGameStatus = PLAY_STATUS;
	}

	private void initChessBoard() {
		if (GameConstants.IS_240x320_SCREEN) {
			CELL_SIZE = 24;
			BOARD_DX = 25;
			BOARD_DY = 45;
		} else {
			CELL_SIZE = 21;
			BOARD_DX = 23;
			BOARD_DY = 16;
		}
		
		chessMans = new ChessMan[2][16];
		chessMans[ChessMan.RED_SIDE] = new ChessMan[16];
		chessMans[ChessMan.BLACK_SIDE] = new ChessMan[16];

		int[][] upSide = new int[][]{{0, 3}, {2, 3}, {4, 3}, {6, 3}, {8, 3}, {1, 2}, {7, 2}, {0, 0}, {8, 0}, {1, 0}, {7, 0}, {2, 0}, {6, 0}, {3, 0}, {5, 0}, {4, 0}};
		int[][] downSide = new int[][]{{0, 6}, {2, 6}, {4, 6}, {6, 6}, {8, 6}, {1, 7}, {7, 7}, {0, 9}, {8, 9}, {1, 9}, {7, 9}, {2, 9}, {6, 9}, {3, 9}, {5, 9}, {4, 9}};
		int[][] redSide = null;
		int[][] blackSide = null;
		if (mySide == ChessMan.RED_SIDE) {
			redSide = downSide;
			blackSide = upSide;

		} else {
			redSide = upSide;
			blackSide = downSide;
		}

		areaImage = ImageUtil.getImage("a.png");
		currentSelectArea = new ChessMan(ChessMan.NO_SIDE, ChessMan.CHOOSE_AREA_TYPE, areaImage, 4, 7);
		currentSelectArea1 = new ChessMan(ChessMan.NO_SIDE, ChessMan.CHOOSE_AREA_TYPE, areaImage, 0, 0);
		currentSelectArea1.setVisible(false);

		chessMansImage = ImageUtil.getImage("cotuong_quanco.png");
		Image tmpImage = null;
		tmpImage = ImageUtil.getSubImage(chessMansImage, 144, 29, 22, 22, true);
		chessMans[ChessMan.RED_SIDE][0] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TOT_TYPE, tmpImage, redSide[0][0], redSide[0][1]);
		chessMans[ChessMan.RED_SIDE][1] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TOT_TYPE, tmpImage, redSide[1][0], redSide[1][1]);
		chessMans[ChessMan.RED_SIDE][2] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TOT_TYPE, tmpImage, redSide[2][0], redSide[2][1]);
		chessMans[ChessMan.RED_SIDE][3] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TOT_TYPE, tmpImage, redSide[3][0], redSide[3][1]);
		chessMans[ChessMan.RED_SIDE][4] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TOT_TYPE, tmpImage, redSide[4][0], redSide[4][1]);
		tmpImage = ImageUtil.getSubImage(chessMansImage, 120, 29, 22, 22, true);
		chessMans[ChessMan.RED_SIDE][5] = new ChessMan(ChessMan.RED_SIDE, ChessMan.PHAO_TYPE, tmpImage, redSide[5][0], redSide[5][1]);
		chessMans[ChessMan.RED_SIDE][6] = new ChessMan(ChessMan.RED_SIDE, ChessMan.PHAO_TYPE, tmpImage, redSide[6][0], redSide[6][1]);
		tmpImage = ImageUtil.getSubImage(chessMansImage, 5, 29, 22, 22, true);
		chessMans[ChessMan.RED_SIDE][7] = new ChessMan(ChessMan.RED_SIDE, ChessMan.XE_TYPE, tmpImage, redSide[7][0], redSide[7][1]);
		chessMans[ChessMan.RED_SIDE][8] = new ChessMan(ChessMan.RED_SIDE, ChessMan.XE_TYPE, tmpImage, redSide[8][0], redSide[8][1]);
		tmpImage = ImageUtil.getSubImage(chessMansImage, 28, 29, 22, 22, true);
		chessMans[ChessMan.RED_SIDE][9] = new ChessMan(ChessMan.RED_SIDE, ChessMan.MA_TYPE, tmpImage, redSide[9][0], redSide[9][1]);
		chessMans[ChessMan.RED_SIDE][10] = new ChessMan(ChessMan.RED_SIDE, ChessMan.MA_TYPE, tmpImage, redSide[10][0], redSide[10][1]);
		tmpImage = ImageUtil.getSubImage(chessMansImage, 51, 29, 22, 22, true);
		chessMans[ChessMan.RED_SIDE][11] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TUONG_TYPE, tmpImage, redSide[11][0], redSide[11][1]);
		chessMans[ChessMan.RED_SIDE][12] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TUONG_TYPE, tmpImage, redSide[12][0], redSide[12][1]);
		tmpImage = ImageUtil.getSubImage(chessMansImage, 74, 29, 22, 22, true);
		chessMans[ChessMan.RED_SIDE][13] = new ChessMan(ChessMan.RED_SIDE, ChessMan.SY_TYPE, tmpImage, redSide[13][0], redSide[13][1]);
		chessMans[ChessMan.RED_SIDE][14] = new ChessMan(ChessMan.RED_SIDE, ChessMan.SY_TYPE, tmpImage, redSide[14][0], redSide[14][1]);
		tmpImage = ImageUtil.getSubImage(chessMansImage, 97, 29, 22, 22, true);
		chessMans[ChessMan.RED_SIDE][15] = new ChessMan(ChessMan.RED_SIDE, ChessMan.VUA_TYPE, tmpImage, redSide[15][0], redSide[15][1]);

		tmpImage = ImageUtil.getSubImage(chessMansImage, 144, 3, 22, 22, true);
		chessMans[ChessMan.BLACK_SIDE][0] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TOT_TYPE, tmpImage, blackSide[0][0], blackSide[0][1]);
		chessMans[ChessMan.BLACK_SIDE][1] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TOT_TYPE, tmpImage, blackSide[1][0], blackSide[1][1]);
		chessMans[ChessMan.BLACK_SIDE][2] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TOT_TYPE, tmpImage, blackSide[2][0], blackSide[2][1]);
		chessMans[ChessMan.BLACK_SIDE][3] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TOT_TYPE, tmpImage, blackSide[3][0], blackSide[3][1]);
		chessMans[ChessMan.BLACK_SIDE][4] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TOT_TYPE, tmpImage, blackSide[4][0], blackSide[4][1]);
		tmpImage = ImageUtil.getSubImage(chessMansImage, 120, 3, 22, 22, true);
		chessMans[ChessMan.BLACK_SIDE][5] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.PHAO_TYPE, tmpImage, blackSide[5][0], blackSide[5][1]);
		chessMans[ChessMan.BLACK_SIDE][6] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.PHAO_TYPE, tmpImage, blackSide[6][0], blackSide[6][1]);
		tmpImage = ImageUtil.getSubImage(chessMansImage, 5, 3, 22, 22, true);
		chessMans[ChessMan.BLACK_SIDE][7] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.XE_TYPE, tmpImage, blackSide[7][0], blackSide[7][1]);
		chessMans[ChessMan.BLACK_SIDE][8] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.XE_TYPE, tmpImage, blackSide[8][0], blackSide[8][1]);
		tmpImage = ImageUtil.getSubImage(chessMansImage, 28, 3, 22, 22, true);
		chessMans[ChessMan.BLACK_SIDE][9] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.MA_TYPE, tmpImage, blackSide[9][0], blackSide[9][1]);
		chessMans[ChessMan.BLACK_SIDE][10] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.MA_TYPE, tmpImage, blackSide[10][0], blackSide[10][1]);
		tmpImage = ImageUtil.getSubImage(chessMansImage, 51, 3, 22, 22, true);
		chessMans[ChessMan.BLACK_SIDE][11] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TUONG_TYPE, tmpImage, blackSide[11][0], blackSide[11][1]);
		chessMans[ChessMan.BLACK_SIDE][12] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TUONG_TYPE, tmpImage, blackSide[12][0], blackSide[12][1]);
		tmpImage = ImageUtil.getSubImage(chessMansImage, 74, 3, 22, 22, true);
		chessMans[ChessMan.BLACK_SIDE][13] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.SY_TYPE, tmpImage, blackSide[13][0], blackSide[13][1]);
		chessMans[ChessMan.BLACK_SIDE][14] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.SY_TYPE, tmpImage, blackSide[14][0], blackSide[14][1]);
		tmpImage = ImageUtil.getSubImage(chessMansImage, 97, 3, 22, 22, true);
		chessMans[ChessMan.BLACK_SIDE][15] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.VUA_TYPE, tmpImage, blackSide[15][0], blackSide[15][1]);
		chessMansImage = null;

		// ảnh vẽ hiệu ứng
		starImage = ImageUtil.getImage("HieuUng.png");
		avatarImage = ImageUtil.getImage("Avatar.png");
		randManaImage = ImageUtil.getImage("pxiel_nen.png");
		matnaImage = ImageUtil.joinAndCreateImages(randManaImage, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT - 20, true);
		thuaImage = ImageUtil.getImage("Thua_1.png");
		thuaImage1 = ImageUtil.getImage("Thua_2.png");
		thangImage = ImageUtil.getImage("Thang_1.png");
		thangImage1 = ImageUtil.getImage("Thang_2.png");

		Image popUpImage = ImageUtil.getImage("PopUp3.png");
		leftImage = ImageUtil.getSubImage(popUpImage, 0, 0, 14, 98, true);
		rightImage = ImageUtil.getSubImage(popUpImage, 21, 0, 12, 98, true);
		bodyFrameImage = ImageUtil.getSubImage(popUpImage, 17, 0, 1, 98, true);
		bodyFrameImage = ImageUtil.joinAndCreateImages(bodyFrameImage, 157, 98, true);
		popUpImage = null;

		lastMoveSmallImage = ImageUtil.getImage("c.png");
		lastMoveLargeImage = ImageUtil.getImage("b.png");
		
		lastMoveSmallSprite = new ChessMan(ChessMan.NO_SIDE, ChessMan.CHOOSE_AREA_TYPE, lastMoveSmallImage, 4, 7);
		lastMoveSmallSprite.setVisible(false);
		lastMoveLargeSprite = new ChessMan(ChessMan.NO_SIDE, ChessMan.CHOOSE_AREA_TYPE, lastMoveLargeImage, 4, 7);
		lastMoveLargeSprite.setVisible(false);
	}

	public void draw(Graphics g) {
		g.drawImage(bgImage, 0, 0, GameConstants.TOP_LEFT_ANCHOR);
		g.drawImage(chessBoardImage, BOARD_DX - 1, BOARD_DY, GameConstants.TOP_LEFT_ANCHOR);

		// Draw chess man
		for (int i = 0; i < chessMans.length; i++) {
			for (int j = 0; j < chessMans[i].length; j++) {
				ChessMan chessMan = chessMans[i][j];
				if ((chessMan != null) && chessMan.isVisible()) {
					g.drawImage(chessMan.getImage(), chessMan.getRealX(), chessMan.getRealY(), GameConstants.TOP_LEFT_ANCHOR);
				}
			}
		}

		// Draw last move lastMoveSmallSprite and lastMoveLargeSprite
		if ((lastMoveSmallSprite != null) && lastMoveSmallSprite.isVisible()) {
			g.drawImage(lastMoveSmallSprite.getImage(), lastMoveSmallSprite.getRealX(), lastMoveSmallSprite.getRealY(), GameConstants.TOP_LEFT_ANCHOR);
		}
		if ((lastMoveLargeSprite != null) && lastMoveLargeSprite.isVisible()) {
			g.drawImage(lastMoveLargeSprite.getImage(), lastMoveLargeSprite.getRealX(), lastMoveLargeSprite.getRealY(), GameConstants.TOP_LEFT_ANCHOR);
		}
		if ((currentSelectArea != null) && currentSelectArea.isVisible()) {
			g.drawImage(currentSelectArea.getImage(), currentSelectArea.getRealX(), currentSelectArea.getRealY(), GameConstants.TOP_LEFT_ANCHOR);
		}
		if ((currentSelectArea1 != null) && currentSelectArea1.isVisible()) {
			g.drawImage(currentSelectArea1.getImage(), currentSelectArea1.getRealX(), currentSelectArea1.getRealY(), GameConstants.TOP_LEFT_ANCHOR);
		}

		// Draw message chiếu tướng
		if (isChieuTuong) {
			text11.drawString(g, "Chiếu tướng", Color.BLACK_CODE, GameConstants.SCREEN_WIDTH / 2, 145, GameConstants.TOP_HCENTER_ANCHOR);
		}

		if (GameConstants.IS_240x320_SCREEN) {
			text11.drawString(g, oponentName, 0x8E2823, 6, 8, GameConstants.TOP_LEFT_ANCHOR);
			int tmp0 = timePlay[0] % 60;
			int tmp1 = timePlay[1] % 60;
			if (mySide == ChessMan.RED_SIDE) {
				text11.drawString(g, fillUpStringNumberToLen(timePlay[1] / 60, 2) + ":" + fillUpStringNumberToLen(tmp1, 2), 0x8E2823, 178, 4, GameConstants.TOP_LEFT_ANCHOR);
			} else {
				text11.drawString(g, fillUpStringNumberToLen(timePlay[0] / 60, 2) + ":" + fillUpStringNumberToLen(tmp0, 2), 0x8E2823, 178, 4, GameConstants.TOP_LEFT_ANCHOR);
			}
			if (currentTurn == opponentSide) {
				text11.drawString(g, "00:" + fillUpStringNumberToLen(timeMove, 2), 0x8E2823, 170, 4, GameConstants.TOP_RIGHT_ANCHOR);
			}

			text11.drawString(g, Global.currentUser.getName(), 0x8E2823, 6, 281, GameConstants.TOP_LEFT_ANCHOR);
			tmp0 = timePlay[0] % 60;
			tmp1 = timePlay[1] % 60;
			if (mySide == ChessMan.RED_SIDE) {
				text11.drawString(g, fillUpStringNumberToLen(timePlay[0] / 60, 2) + ":" + fillUpStringNumberToLen(tmp0, 2), 0x8E2823, 178, 274, GameConstants.TOP_LEFT_ANCHOR);
			} else {
				text11.drawString(g, fillUpStringNumberToLen(timePlay[1] / 60, 2) + ":" + fillUpStringNumberToLen(tmp1, 2), 0x8E2823, 178, 274, GameConstants.TOP_LEFT_ANCHOR);
			}
			if (currentTurn == mySide) {
				text11.drawString(g, "00:" + fillUpStringNumberToLen(timeMove, 2), 0x8E2823, 170, 274, GameConstants.TOP_RIGHT_ANCHOR);
			}

		} else {// 320x240
			if (oppImage == null) {
				g.drawImage(avatarImage, 213, 50, GameConstants.BOTTOM_LEFT_ANCHOR);
			} else {
				g.drawImage(oppImage, 213, 50, GameConstants.BOTTOM_LEFT_ANCHOR);
			}

			text11.drawString(g, oponentName, 0x8E2823, 213, 51, GameConstants.TOP_LEFT_ANCHOR);
			int tmp0 = timePlay[0] % 60;
			int tmp1 = timePlay[1] % 60;
			if (mySide == ChessMan.RED_SIDE) {
				text11.drawString(g, fillUpStringNumberToLen(timePlay[1] / 60, 2) + ":" + fillUpStringNumberToLen(tmp1, 2), 0x8E2823, 316, 16, GameConstants.TOP_RIGHT_ANCHOR);
			} else {
				text11.drawString(g, fillUpStringNumberToLen(timePlay[0] / 60, 2) + ":" + fillUpStringNumberToLen(tmp0, 2), 0x8E2823, 316, 16, GameConstants.TOP_RIGHT_ANCHOR);
			}

			if (myImage == null) {
				g.drawImage(avatarImage, 213, 170, GameConstants.TOP_LEFT_ANCHOR);
			} else {
				g.drawImage(myImage, 213, 170, GameConstants.TOP_LEFT_ANCHOR);
			}

			text11.drawString(g, Global.currentUser.getName(), 0x8E2823, 213, 169, GameConstants.BOTTOM_LEFT_ANCHOR);
			if (mySide == ChessMan.RED_SIDE) {
				text11.drawString(g, fillUpStringNumberToLen(timePlay[0] / 60, 2) + ":" + fillUpStringNumberToLen(tmp0, 2), 0x8E2823, 316, 185, GameConstants.TOP_RIGHT_ANCHOR);
			} else {
				text11.drawString(g, fillUpStringNumberToLen(timePlay[1] / 60, 2) + ":" + fillUpStringNumberToLen(tmp1, 2), 0x8E2823, 316, 185, GameConstants.TOP_RIGHT_ANCHOR);
			}

			// time per move
			text11.drawString(g, "00:" + fillUpStringNumberToLen(timeMove, 2), 0x8E2823, 248, 102, GameConstants.TOP_LEFT_ANCHOR);
			if (currentTurn == ChessMan.RED_SIDE) {
				g.setColor(Color.RED2_CODE);
			} else {
				g.setColor(Color.BLACK_CODE);
			}
			g.fillArc(235, 106, 10, 10, 0, 360);
		}

		// Draw command bar
		g.setColor(0x2B0102);// Màu nền task bar
		g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25, GameConstants.SCREEN_WIDTH, 25);
		if (currentGameStatus != SHOW_FINAL_RESULL && currentGameStatus != SHOW_WIN_OR_LOST_ALERT_STATUS) {
			text8.drawString(g, "Thoát", Color.WHITE_CODE, GameConstants.SCREEN_WIDTH - 8, GameConstants.SCREEN_HEIGHT - 4, GameConstants.BOTTOM_RIGHT_ANCHOR);
		}

		// draw hiệu ứng cuối ván
		switch (currentGameStatus) {
		case SHOW_WIN_OR_LOST_ALERT_STATUS:
			g.drawImage(matnaImage, 0, 0, GameConstants.TOP_LEFT_ANCHOR);
			g.drawImage(starImage, GameConstants.SCREEN_WIDTH / 2, (GameConstants.SCREEN_HEIGHT - 20) / 2, GameConstants.CENTER_ANCHOR);
			
			if (winSide == mySide) {
				if (step % 5 == 0) {
					g.drawImage(thangImage, GameConstants.SCREEN_WIDTH / 2, (GameConstants.SCREEN_HEIGHT - 20) / 2, GameConstants.CENTER_ANCHOR);
				} else {
					g.drawImage(thangImage1, GameConstants.SCREEN_WIDTH / 2, (GameConstants.SCREEN_HEIGHT - 20) / 2, GameConstants.CENTER_ANCHOR);
				}
			} else {
				if (step % 5 == 0) {
					g.drawImage(thuaImage, GameConstants.SCREEN_WIDTH / 2, (GameConstants.SCREEN_HEIGHT - 20) / 2, GameConstants.CENTER_ANCHOR);
				} else {
					g.drawImage(thuaImage1, GameConstants.SCREEN_WIDTH / 2, (GameConstants.SCREEN_HEIGHT - 20) / 2, GameConstants.CENTER_ANCHOR);
				}
			}
			break;
		case SHOW_FINAL_RESULL:
			if (GameConstants.IS_240x320_SCREEN) {
				g.drawImage(leftImage, 28, 82, GameConstants.TOP_LEFT_ANCHOR);
				g.drawImage(bodyFrameImage, 42, 82, GameConstants.TOP_LEFT_ANCHOR);
				g.drawImage(rightImage, 199, 82, GameConstants.TOP_LEFT_ANCHOR);
				text8.drawString(g, "KẾT QUẢ", Color.WHITE_CODE, GameConstants.SCREEN_WIDTH / 2, 104, GameConstants.TOP_HCENTER_ANCHOR);
				g.setColor(Color.RED2_CODE);
				g.drawLine(35, 121, 205, 121);
				text8.drawString(g, "Thắng:", Color.WHITE_CODE, 43, 129, GameConstants.TOP_LEFT_ANCHOR);
				text8.drawString(g, "Thua:", Color.WHITE_CODE, 43, 147, GameConstants.TOP_LEFT_ANCHOR);
				if (winSide == mySide) {
					text8.drawString(g, Global.currentUser.getName(), Color.WHITE_CODE, 88, 129, GameConstants.TOP_LEFT_ANCHOR);
					text8.drawString(g, oponentName, Color.WHITE_CODE, 88, 147, GameConstants.TOP_LEFT_ANCHOR);
				} else {
					text8.drawString(g, oponentName, Color.WHITE_CODE, 88, 129, GameConstants.TOP_LEFT_ANCHOR);
					text8.drawString(g, Global.currentUser.getName(), Color.WHITE_CODE, 88, 147, GameConstants.TOP_LEFT_ANCHOR);
				}
				text8.drawString(g, moneyChangePoint[0] + " điểm", Color.WHITE_CODE, 161, 129, GameConstants.TOP_LEFT_ANCHOR);
				text8.drawString(g, moneyChangePoint[1] + " điểm", Color.WHITE_CODE, 161, 147, GameConstants.TOP_LEFT_ANCHOR);
			} else {
				g.drawImage(leftImage, 68, 82, GameConstants.TOP_LEFT_ANCHOR);
				g.drawImage(bodyFrameImage, 82, 82, GameConstants.TOP_LEFT_ANCHOR);
				g.drawImage(rightImage, 239, 82, GameConstants.TOP_LEFT_ANCHOR);
				text8.drawString(g, "KẾT QUẢ", Color.WHITE_CODE, GameConstants.SCREEN_WIDTH / 2, 104, GameConstants.TOP_HCENTER_ANCHOR);
				g.setColor(Color.RED2_CODE);
				g.drawLine(75, 121, 205, 121);
				text8.drawString(g, "Thắng:", Color.WHITE_CODE, 83, 129, GameConstants.TOP_LEFT_ANCHOR);
				text8.drawString(g, "Thua:", Color.WHITE_CODE, 83, 147, GameConstants.TOP_LEFT_ANCHOR);
				if (winSide == mySide) {
					text8.drawString(g, Global.currentUser.getName(), Color.WHITE_CODE, 128, 129, GameConstants.TOP_LEFT_ANCHOR);
					text8.drawString(g, oponentName, Color.WHITE_CODE, 128, 147, GameConstants.TOP_LEFT_ANCHOR);
				} else {
					text8.drawString(g, oponentName, Color.WHITE_CODE, 128, 129, GameConstants.TOP_LEFT_ANCHOR);
					text8.drawString(g, Global.currentUser.getName(), Color.WHITE_CODE, 128, 147, GameConstants.TOP_LEFT_ANCHOR);
				}
				text8.drawString(g, moneyChangePoint[0] + " điểm", Color.WHITE_CODE, 201, 129, GameConstants.TOP_LEFT_ANCHOR);
				text8.drawString(g, moneyChangePoint[1] + " điểm", Color.WHITE_CODE, 201, 147, GameConstants.TOP_LEFT_ANCHOR);
			}
			break;

		case CHAT_STATUS:
			// Draw chat
//			if (GameConstants.IS_240x320_SCREEN) {
//				drawChat(g);
//			} else {
//				drawChat320_240(g);
//			}
			break;
		}
	}

//	private void drawChat(Graphics g) {
//		if (currentString.length() > 0) {
//			g.setColor(0xffffff);
//			g.fillRect(49, 270, 95, 25);
//			g.setColor(0x000000);
//			g.fillRect(50, 271, 93, 23);
//			text8.drawString(g, currentString, 0xffffff, 55, 275, GameConstants.TOP_LEFT_ANCHOR);
//			if (caretBlinkOn && goToNextChar && isInChatState) {
//				g.drawLine(caretLeft, 0, caretLeft, inputHeight);
//			}
//
//			g.setColor(0x2B0102);// Màu nền task bar
//			g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25, GameConstants.SCREEN_WIDTH, 25);
//			text8.drawString(g, "Quay lại", 0xffffff, 11, 303, GameConstants.TOP_LEFT_ANCHOR);
//			text8.drawString(g, "Gửi", 0xffffff, GameConstants.SCREEN_WIDTH / 2, 303, GameConstants.TOP_HCENTER_ANCHOR);
//			text8.drawString(g, "Xóa", 0xffffff, GameConstants.SCREEN_WIDTH - 10, 303, GameConstants.TOP_RIGHT_ANCHOR);
//		}
//
//		if (oppMessageChat != null && oppMessageChat.length() > 0) {
//			g.setColor(0xffffff);
//			g.fillRect(70, 6, 95, 25);
//			g.setColor(0x000000);
//			g.fillRect(71, 7, 93, 23);
//			text8.drawString(g, oppMessageChat, Color.WHITE_CODE, 72, 11, GameConstants.TOP_LEFT_ANCHOR);
//		}
//	}
//
//	private void drawChat320_240(Graphics g) {
//		if (currentString.length() > 0) {
//			g.setColor(0xffffff);
//			g.fillRect(205, 123, 95, 25);
//			g.setColor(0x000000);
//			g.fillRect(206, 124, 93, 23);
//			text8.drawString(g, currentString, 0xffffff, 207, 125, GameConstants.TOP_LEFT_ANCHOR);
//			if (caretBlinkOn && goToNextChar && isInChatState) {
//				g.drawLine(caretLeft, 0, caretLeft, inputHeight);
//			}
//
//			g.setColor(0x2B0102);// Màu nền task bar
//			g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25, GameConstants.SCREEN_WIDTH, 25);
//			text8.drawString(g, "Quay lại", 0xffffff, 11, 303, GameConstants.TOP_LEFT_ANCHOR);
//			text8.drawString(g, "Gửi", 0xffffff, GameConstants.SCREEN_WIDTH / 2, GameConstants.SCREEN_HEIGHT - 8, GameConstants.BOTTOM_HCENTER_ANCHOR);
//			text8.drawString(g, "Xóa", 0xffffff, GameConstants.SCREEN_WIDTH - 10, GameConstants.SCREEN_HEIGHT - 8, GameConstants.BOTTOM_RIGHT_ANCHOR);
//		}
//
//		if (oppMessageChat != null && oppMessageChat.length() > 0) {
//			g.setColor(0xffffff);
//			g.fillRect(200, 72, 95, 25);
//			g.setColor(0x000000);
//			g.fillRect(201, 72, 93, 23);
//			text8.drawString(g, oppMessageChat, Color.WHITE_CODE, 202, 73, GameConstants.TOP_LEFT_ANCHOR);
//		}
//	}

	public void keyReleased(int keyCode) {
		if (currentGameStatus == WAIT_FOR_INIT_STATUS) {
			return;
		}
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
				GameGlobal.nextState(Global.frmTable, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
			}
			return;
		}
		
		switch (keyCode) {
		case Key.LEFT:
		case Key.K_4:
			if (currentSelectArea.getColumn() > 0) {
				currentSelectArea.move(-1, 0);
			}
			break;
		case Key.RIGHT:
		case Key.K_6:
			if (currentSelectArea.getColumn() < 8) {
				currentSelectArea.move(1, 0);
			}
			break;
		case Key.UP:
		case Key.K_2:
			if (currentSelectArea.getRow() > 0) {
				currentSelectArea.move(0, -1);
			}
			break;
		case Key.DOWN:
		case Key.K_8:
			if (currentSelectArea.getRow() < 9) {
				currentSelectArea.move(0, 1);
			}
			break;
		case Key.SOFT_RIGHT:
			if ((currentGameStatus != SHOW_FINAL_RESULL) && (currentGameStatus != SHOW_WIN_OR_LOST_ALERT_STATUS)) {
			  GameGlobal.alert.showAlert(this, Alert.YES_NO_TYPE, new String[]{"Thoát ra bị tính là thua cuộc", "Bạn có muốn thoát ra?"}, 98);
			}
			break;
		case Key.FIRE:
		case Key.K_5:
			ChessMan selectedChessMan = getChessMan(mySide, currentSelectArea.getColumn(), currentSelectArea.getRow());
			if (selectedChessMan != null) {
				// Nếu chọn quân cờ phe mình và đang nước đi của mình
				if ((selectedChessMan.getSide() == mySide) && (currentTurn == mySide)) {
					currentSelectArea1.changePosition(currentSelectArea.getColumn(), currentSelectArea.getRow());
					currentSelectArea1.setVisible(true);
					this.selectedChessMan = selectedChessMan;
				}
			} else {
				// Nếu chọn nước đi
				if (currentSelectArea1.isVisible()) {
					chessManIndex = getIndexOfChessMan(this.selectedChessMan);
					if (currentTurn == ChessMan.RED_SIDE) {
						sendMoveAction(chessManIndex, currentSelectArea.getColumn(), currentSelectArea.getRow());
					} else {
						sendMoveAction(getInverseIndex(chessManIndex), NUMBER_OF_COLUMN - currentSelectArea.getColumn() - 1, NUMBER_OF_ROW - currentSelectArea.getRow() - 1);
					}
				}
			}
			break;
		}
	}
	
	private void sendMoveAction(int chessManIndex, int column, int row) {
		DataPackage movePackage = Game.createPackage(MOVE_REQUEST);
		movePackage.putInt(chessManIndex);
		movePackage.putInt(column);
		movePackage.putInt(row);
		Global.gameActionClient.write(movePackage);
		isChieuTuong = false;
	}

	public void alertEventPerform(int alertType, int eventType, int alertId) {
		if (alertId == 98) { // Sự kiện thoát ra giữa ván
			if (eventType == Alert.YES_BUTTON) {
				SocketClientUtil.leaveTableRequest();
				GameGlobal.nextState(Global.frmListTable, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
			}
			return;
		} else if (alertId == 97) { // Sự kiện báo đối phương đã thoát hoặc time out
			currentGameStatus = SHOW_FINAL_RESULL;
		}
	}

	private String fillUpStringNumberToLen(int input, int len) {
		String str = String.valueOf(input);
		int lackLen = len - str.length();
		if (lackLen > 0) {
			for (int i = 0; i < lackLen; i++) {
				str = "0" + str;
			}
		}
		return str;
	}

	private void changeTurn() {
		if (currentTurn == ChessMan.BLACK_SIDE) {
			currentTurn = ChessMan.RED_SIDE;
		} else {
			currentTurn = ChessMan.BLACK_SIDE;
		}
		selectedChessMan = null;
		currentSelectArea1.setVisible(false);
		timeMove = TIME_PER_MOVE;
	}

	private int getIndexOfChessMan(ChessMan chessMan) {
		if (chessMan == null) {
			throw new IllegalArgumentException("getIndexOfChessMan : chessMan is null");
		}
		for (int i = 0; i < chessMans[chessMan.getSide()].length; i++) {
			if (chessMans[chessMan.getSide()][i] == chessMan) {
				return i;
			}
		}
		return -1;
	}

	private ChessMan getChessMan(int side, int column, int row) {
		if ((column < 0) || (column > NUMBER_OF_COLUMN - 1) || (row < 0) || (row > NUMBER_OF_ROW - 1)) {
			return null;
		}

		for (int i = 0; i < chessMans[side].length; i++) {
			if (chessMans[side][i] != null) {
				if ((chessMans[side][i].getColumn() == column) && (chessMans[side][i].getRow() == row)) {
					return chessMans[side][i];
				}
			}
		}
		return null;
	}

	private void killChessMan(ChessMan chessMan) {
		if (chessMan == null) {
			return;
		}

		for (int i = 0; i < chessMans[chessMan.getSide()].length; i++) {
			if (chessMans[chessMan.getSide()][i] == chessMan) {
				chessMans[chessMan.getSide()][i] = null;
				chessMan.setVisible(false);
				return;
			}
		}
	}

	public void doTask() {
		step = (step + 1) % 200;
		
		count = (count + 1) % 1200;
		if (count % 10 == 0) {
			if ((currentGameStatus != SHOW_FINAL_RESULL) && (currentGameStatus != SHOW_WIN_OR_LOST_ALERT_STATUS)) {
				if (count % 10 == 0) {
					if (timeMove > 0) {
						timeMove--;
					}

					if (currentTurn == ChessMan.RED_SIDE) {
						if (timePlay[0] > 0) {
							timePlay[0]--;
						}
					} else {
						if (timePlay[1] > 0) {
							timePlay[1]--;
						}
					}
				}
			}
		}
	}

	public int getInverseIndex(int index) {
		int tmp = 0;
		switch (index) {
		case 0:
			tmp = 4;
			break;
		case 1:
			tmp = 3;
			break;
		case 2:
			tmp = 2;
			break;
		case 3:
			tmp = 1;
			break;
		case 4:
			tmp = 0;
			break;
		case 5:
			tmp = 6;
			break;
		case 6:
			tmp = 5;
			break;
		case 7:
			tmp = 8;
			break;
		case 8:
			tmp = 7;
			break;
		case 9:
			tmp = 10;
			break;
		case 10:
			tmp = 9;
			break;
		case 11:
			tmp = 12;
			break;
		case 12:
			tmp = 11;
			break;
		case 13:
			tmp = 14;
			break;
		case 14:
			tmp = 13;
			break;
		case 15:
			tmp = 15;
			break;
		default:
			break;
		}
		return tmp;
	}

	protected void destroy() {
		currentGameStatus = WAIT_FOR_INIT_STATUS;

		chessMansImage = null;
		chessBoardImage = null;
		bgImage = null;
		areaImage = null;
		lastMoveSmallImage = null;
		lastMoveLargeImage = null;
		chessMans = null;
		currentSelectArea = null;
		currentSelectArea1 = null;
		selectedChessMan = null;
		lastMoveSmallSprite = null;
		lastMoveLargeSprite = null;
		matnaImage = null;
		randManaImage = null;
		starImage = null;
		thangImage = null;
		thuaImage = null;
		thuaImage1 = null;
		thangImage1 = null;
		leftImage = null;
		rightImage = null;
		bodyFrameImage = null;
		text11 = null;
		text8 = null;
		oponentName = null;
		currentSelectArea = null;
		currentSelectArea1 = null;
		selectedChessMan = null;
		lastMoveSmallSprite = null;
		lastMoveLargeSprite = null;
		timePlay = null;
	}

	public void onConnectDone() {
	}

	public void onConnectFail() {
	}

	public void onDisconnect() {
	}
}
