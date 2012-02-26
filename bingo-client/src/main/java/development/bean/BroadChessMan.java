package development.bean;

import java.io.IOException;

import javax.microedition.lcdui.Image;

import state.GameConstants;
import state.Sprite;
import state.util.ImageUtil;
import development.FrmBroadChess;

public class BroadChessMan extends Sprite {
	/** Tá»‘t */
	public static final int TOT_TYPE = 0;

	/** Xe */
	public static final int XE_TYPE = 1;

	/** MÃ£ */
	public static final int MA_TYPE = 2;

	/** TÆ°á»£ng */
	public static final int TUONG_TYPE = 3;

	/** Háº­u */
	public static final int HAU_TYPE = 4;

	/** Vua */
	public static final int VUA_TYPE = 5;

	public static final int CHOOSE_AREA_TYPE = 7;

	/** QuÃ¢n cá»� bÃªn Ä‘á»� */
	public static final int RED_SIDE = 0;

	/** QuÃ¢n cá»� bÃªn Ä‘en */
	public static final int BLACK_SIDE = 1;

	/** QuÃ¢n khÃ´ng thuá»™c bÃªn nÃ o cáº£ */
	public static final int NO_SIDE = 2;

	// DÃ¹ng sá»‘ 99 Ä‘á»ƒ mÃ´ táº£ viá»‡c cÃ³ thá»ƒ di chuyá»ƒn Ä‘Æ°á»£c cáº£
	// hÃ ng hay cá»™t
	public static final int INFINITE = 99;

	private int column;
	private int row;
	private int type;
	private int side;
	private int[][] availableMove;

	public BroadChessMan(int side, int type, Image image, int column, int row) {
		super(image, FrmBroadChess.BOARD_DX + FrmBroadChess.CELL_SIZE * column,
				FrmBroadChess.BOARD_DY + FrmBroadChess.CELL_SIZE * row,
				GameConstants.CENTER_ANCHOR);
		this.type = type;
		this.column = column;
		this.row = row;
		this.side = side;

		switch (type) {
		case TOT_TYPE:
			if (side == RED_SIDE) {
				availableMove = new int[][] { { 0, -1 }, { 0, -2 }, null, null }; // DÃ nh
																					// 2
																					// chá»—
																					// trá»‘ng
																					// cho
																					// viá»‡c
																					// Äƒn
																					// chÃ©o
			} else {
				availableMove = new int[][] { { 0, 1 }, { 0, 2 }, null, null }; // DÃ nh
																				// 2
																				// chá»—
																				// trá»‘ng
																				// cho
																				// viá»‡c
																				// Äƒn
																				// chÃ©o
			}
			break;
		case XE_TYPE:
			availableMove = new int[][] { { INFINITE, 0 } };
			break;
		case MA_TYPE:
			availableMove = new int[][] { { -1, 2 }, { 1, 2 }, { -1, -2 },
					{ 1, -2 }, { 2, -1 }, { 2, 1 }, { -2, -1 }, { -1, 2 },
					{ -2, 1 } };
			break;
		case TUONG_TYPE:
			availableMove = new int[][] { { INFINITE, INFINITE } };
			break;
		case HAU_TYPE:
			availableMove = new int[][] { { INFINITE, 0 },
					{ INFINITE, INFINITE } };
			break;
		case VUA_TYPE:
			availableMove = new int[][] { { -1, 0 }, { 1, 0 }, { 0, -1 },
					{ 0, 1 }, { 1, -1 }, { 1, 1 }, { -1, -1 }, { -1, 1 } };
			break;
		}
	}

	public int[][] getAvailableMove() {
		if (availableMove == null) {
			return null;
		}

		int[][] array = new int[availableMove.length][];
		for (int i = 0; i < availableMove.length; i++) {
			if (availableMove[i] != null) {
				array[i] = new int[2];
				for (int j = 0; j < availableMove[i].length; j++) {
					array[i][j] = availableMove[i][j];
				}
			}
		}
		return array;
	}

	public Sprite move(int dColumn, int dRow) {
		column += dColumn;
		row += dRow;

		if (type == TOT_TYPE) {
			checkPassRiver();
		}
		setPosition(FrmBroadChess.BOARD_DX + FrmBroadChess.CELL_SIZE * column,
				FrmBroadChess.BOARD_DY + FrmBroadChess.CELL_SIZE * row);
		return this;
	}

	public void changePosition(int column, int row) {
		this.column = column;
		this.row = row;

		if (type == TOT_TYPE) {
			checkPassRiver();
		}
		setPosition(FrmBroadChess.BOARD_DX + FrmBroadChess.CELL_SIZE * column,
				FrmBroadChess.BOARD_DY + FrmBroadChess.CELL_SIZE * row);
	}

	private void checkPassRiver() {
		// Sau nÆ°á»›c Ä‘i Ä‘áº§u tiÃªn thÃ¬ bá»� nÆ°á»›c nháº£y Ä‘Ã´i cá»§a
		// tá»‘t
		availableMove[1] = null;

		if (side == RED_SIDE) {
			// Náº¿u tá»‘t tráº¯ng cháº¡m Ä‘áº¿n hÃ ng Ä‘áº§u thÃ¬ láº­p háº­u
			if (row == 0) {
				type = HAU_TYPE;
				availableMove = new int[][] { { INFINITE, 0 },
						{ INFINITE, INFINITE } };
				image = ImageUtil.getImage("CoVua05.png");
			}
			if (row == 7) {
				image = ImageUtil.getImage("CoVua05.png");
			}
		} else {
			// Náº¿u tá»‘t Ä‘en cháº¡m Ä‘áº¿n hÃ ng cuá»‘i thÃ¬ láº­p háº­u
			if (row == 0) {
				image = ImageUtil.getImage("CoVua15.png");
			}
			if (row == 7) {
				type = HAU_TYPE;
				availableMove = new int[][] { { INFINITE, 0 },
						{ INFINITE, INFINITE } };
				try {
					image = Image.createImage(GameConstants.IMAGE_FOLDER
							+ "/CoVua15.png");
				} catch (IOException e) {
				}
			}
		}
	}

	public int getColumn() {
		return column;
	}

	public int getRow() {
		return row;
	}

	public int getType() {
		return type;
	}

	public int getSide() {
		return side;
	}
}
