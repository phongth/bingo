package zgame.bean;

public class ChessMan {
  protected int x;
  protected int y;
  protected int realX;
  protected int realY;
  protected boolean isVisible = true;

  /** Tốt */
  public static final int TOT_TYPE = 0;

  /** Pháo */
  public static final int PHAO_TYPE = 1;

  /** Xe */
  public static final int XE_TYPE = 2;

  /** Mã */
  public static final int MA_TYPE = 3;

  /** Tượng */
  public static final int TUONG_TYPE = 4;

  /** Sỹ */
  public static final int SY_TYPE = 5;

  /** Vua */
  public static final int VUA_TYPE = 6;

  public static final int CHOOSE_AREA_TYPE = 7;

  /** Quân cờ bên đỏ */
  public static final int RED_SIDE = 0;

  /** Quân cờ bên đen */
  public static final int BLACK_SIDE = 1;

  /** Quân không thuộc bên nào cả */
  public static final int NO_SIDE = 2;

  // Dùng số 99 để mô tả việc có thể di chuyển được cả hàng hay cột
  public static final int INFINITE = 99;

  private int column;
  private int row;
  private int type;
  private int side;
  private int[][] availableMove;

  /**
   * 
   * @param side
   *          bên đỏ hay đen
   * @param type
   *          loại quân cờ
   * @param column
   * @param row
   */
  public ChessMan(int side, int type, int column, int row) {
    this.type = type;
    this.column = column;
    this.row = row;
    this.side = side;

    switch (type) {
    case TOT_TYPE:
      if (row > 5) {
        availableMove = new int[][] { { 0, -1 }, null, null }; // Để trống 2
                                                               // phần tử cuối
                                                               // dành cho ăn
                                                               // ngang khi sang
                                                               // sông
      } else {
        availableMove = new int[][] { { 0, 1 }, null, null }; // Để trống 2 phần
                                                              // tử cuối dành
                                                              // cho ăn ngang
                                                              // khi sang sông
      }
      break;
    case PHAO_TYPE:
      availableMove = new int[][] { { INFINITE, 0 } };
      break;
    case XE_TYPE:
      availableMove = new int[][] { { INFINITE, 0 } };
      break;
    case MA_TYPE:
      availableMove = new int[][] { { -1, 2 }, { 1, 2 }, { -1, -2 }, { 1, -2 }, { 2, -1 }, { 2, 1 }, { -2, -1 }, { -2, 1 } };
      break;
    case TUONG_TYPE:
      availableMove = new int[][] { { 2, 2 }, { -2, 2 }, { 2, -2 }, { -2, -2 } };
      break;
    case SY_TYPE:
      availableMove = new int[][] { { 1, -1 }, { 1, 1 }, { -1, -1 }, { -1, 1 } };
      break;
    case VUA_TYPE:
      availableMove = new int[][] { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
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

  public void move(int dColumn, int dRow) {
    column += dColumn;
    row += dRow;

    if (type == TOT_TYPE) {
      checkPassRiver();
    }

    // setPosition(ChinesChess.BOARD_DX + ChinesChess.CELL_SIZE * column,
    // ChinesChess.BOARD_DY + ChinesChess.CELL_SIZE * row);
  }

  public void changePosition(int column, int row) {
    this.column = column;
    this.row = row;

    if (type == TOT_TYPE) {
      checkPassRiver();
    }

    // setPosition(ChinesChess.BOARD_DX + ChinesChess.CELL_SIZE * column,
    // ChinesChess.BOARD_DY + ChinesChess.CELL_SIZE * row);
  }

  private void checkPassRiver() {// loan fix sua > 4 thanh >=4
    if (((availableMove[0][1] < 0) && (row <= 4)) || ((availableMove[0][1] > 0) && (row >= 5))) {
      availableMove[1] = new int[] { 1, 0 };
      availableMove[2] = new int[] { -1, 0 };
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

  public boolean isVisible() {
    return isVisible;
  }

  public void setVisible(boolean isVisible) {
    if (this.isVisible != isVisible) {
      this.isVisible = isVisible;
    }
  }
}
