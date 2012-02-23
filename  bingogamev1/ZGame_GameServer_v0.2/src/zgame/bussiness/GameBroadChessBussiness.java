package zgame.bussiness;

import java.util.Collection;

import zgame.bean.BroadChessMan;
import zgame.bean.Table;
import zgame.bean.User;
import zgame.job.Job;
import zgame.socket.DataPackage;

public class GameBroadChessBussiness extends GameComponent {
  private static final int MOVE_REQUEST = 1001; // Gửi thông tin về nước đã đi

  private static final int WIN_GAME_NOTIFY_RESPONSE = 2006; // Báo cho các
  // user về user
  // đã thắng và
  // danh sách các
  // nước đi thắng
  private static final int INIT_DATA_RESPONSE = 2007; // Thiết đặt các thông
  // số cho client
  private static final int MOVE_ERROR_RESPONSE = 2008; // Báo lại cho ng chơi
  // vừa đánh là nước
  // đánh không được
  // chấp nhận
  private static final int MOVE_NOTIFY_RESPONSE = 2009; // Báo nước đi của
  // người vừa đánh

  private static final int NOT_YOUR_TURN_ERROR = 3001;
  private static final int MOVE_IN_NOT_ALLOW_POSITION = 3002;

  private static final int NORMAL_WIN_REASON = 4001;
  private static final int OPPONENT_RESIGN_WIN_REASON = 4002;
  private static final int OPPONENT_TIMEOUT_WIN_REASON = 4003;

  private static final int NUMBER_OF_ROW = 8;
  private static final int NUMBER_OF_COLUMN = 8;

  public static final int TIME_PER_MATCH = 30 * 60; // Seconds
  public static final int TIME_PER_MOVE = 30; // Seconds // TODO: Đưa vào
  // config

  private static final int[][] downSide = new int[][] { { 0, 6 }, { 1, 6 }, { 2, 6 }, { 3, 6 }, { 4, 6 }, { 5, 6 }, { 6, 6 },
      { 7, 6 }, { 0, 7 }, { 7, 7 }, { 1, 7 }, { 6, 7 }, { 2, 7 }, { 5, 7 }, { 3, 7 }, { 4, 7 } };
  private static final int[][] upSide = new int[][] { { 0, 1 }, { 1, 1 }, { 2, 1 }, { 3, 1 }, { 4, 1 }, { 5, 1 }, { 6, 1 },
      { 7, 1 }, { 0, 0 }, { 7, 0 }, { 1, 0 }, { 6, 0 }, { 2, 0 }, { 5, 0 }, { 3, 0 }, { 4, 0 } };

  private User redSide;
  private User blackSide; // Bên được đi đầu tiên
  private User currentTurn;

  private BroadChessMan[][] chessMans;

  private int[] timePlay = new int[2];

  private int timeCount; // int second
  private Job timer;

  public GameBroadChessBussiness(Table table) {
    super(table);

    chessMans = new BroadChessMan[2][16];
    chessMans[BroadChessMan.RED_SIDE] = new BroadChessMan[16];
    chessMans[BroadChessMan.BLACK_SIDE] = new BroadChessMan[16];
  }

  @Override
  public void init() {
    // Khởi tạo các bên chơi
    Collection<User> users = table.getUsers();

    // Tìm xem nếu người đánh thắng ván trước đó vẫn còn trong bàn thì cho
    // ng đó đánh trước
    if (table.getLastWinUser() != null) {
      for (User user : users) {
        if (user.getName().equals(table.getLastWinUser())) {
          redSide = user;
          break;
        }
      }
    }

    // Nếu người đánh thắng ván trước không còn trong bàn thì cho chủ bàn
    // đánh trước
    if (redSide == null) {
      redSide = table.getTableMaster();
    }

    // Xác định nốt người chơi còn lại
    for (User user : users) {
      if (!user.equals(redSide)) {
        blackSide = user;
        break;
      }
    }
    currentTurn = redSide;

    initChessBoard();

    // Gửi thông tin init cho tất cả các người chơi
    DataPackage initPackage = createPackage(INIT_DATA_RESPONSE);
    initPackage.putInt(TIME_PER_MATCH);
    initPackage.putInt(TIME_PER_MOVE);
    initPackage.putString(redSide.getName());
    initPackage.putString(blackSide.getName());
    sendMessageToAllUser(initPackage);

    // Khởi tạo đồng hồ đếm giờ
    initTimer();
  }

  private void initTimer() {
    timer = new Job() {
      protected void init() {
        timeCount = 0;
        timePlay[0] = TIME_PER_MATCH + 15;
        timePlay[1] = TIME_PER_MATCH + 15;
      }

      public void loop() {
        try {
          sleep(1000);
        } catch (InterruptedException e) {
        }

        int side = BroadChessMan.BLACK_SIDE;
        if (currentTurn.equals(redSide)) {
          side = BroadChessMan.RED_SIDE;
        }

        timeCount++;
        timePlay[side]--;

        if ((timeCount > TIME_PER_MOVE + 5) || (timePlay[side] < 0)) {
          onTimeOut();
          cancel();
        }
      }
    };
    timer.start();
  }

  private void onTimeOut() {
    int winSide = BroadChessMan.RED_SIDE;
    if (currentTurn.equals(redSide)) {
      winSide = BroadChessMan.BLACK_SIDE;
    }
    onEndGameAction(winSide, OPPONENT_TIMEOUT_WIN_REASON);
  }

  @Override
  protected void onActionPerform(User user, DataPackage inputDataPackage) {
    if (!user.equals(currentTurn)) {
      DataPackage dataPackage = createPackage(MOVE_ERROR_RESPONSE);
      dataPackage.putInt(NOT_YOUR_TURN_ERROR);
      user.server.write(dataPackage);
      return;
    }

    int header = inputDataPackage.getHeader();
    switch (header) {
    case MOVE_REQUEST:
      int chessIndex = inputDataPackage.nextInt();
      int column = inputDataPackage.nextInt();
      int row = inputDataPackage.nextInt();
      int side = BroadChessMan.BLACK_SIDE;
      int oppSide = BroadChessMan.RED_SIDE;
      if (user.equals(redSide)) {
        side = BroadChessMan.RED_SIDE;
        oppSide = BroadChessMan.BLACK_SIDE;
      }

      boolean isMoveAlowed = false;
      boolean isEndGame = false;
      int[][] currentAvailableMoves = filtAvailableMoveList(chessMans[side][chessIndex], side);
      if (currentAvailableMoves != null) {
        for (int i = 0; i < currentAvailableMoves.length; i++) {
          if (currentAvailableMoves[i] != null) {
            if ((column - chessMans[side][chessIndex].getColumn() == currentAvailableMoves[i][0])
                && row - chessMans[side][chessIndex].getRow() == currentAvailableMoves[i][1]) {
              isMoveAlowed = true;
              BroadChessMan enemy = getChessMan(oppSide, column, row);
              if (enemy != null) { // vị trí vừa đi đến đang có
                // quân cờ của đối thủ
                if (enemy.getType() == BroadChessMan.VUA_TYPE) {
                  isEndGame = true;
                }
                killBroadChessMan(enemy);
              }
              chessMans[side][chessIndex].changePosition(column, row); // Dịch
                                                                       // chuyển
                                                                       // quân
                                                                       // cờ
                                                                       // trên
                                                                       // bàn cờ
                                                                       // đến
              // vị trí client gửi lên phía server

              // Báo về nước vừa đi cho user còn lại
              DataPackage movePackage = createPackage(MOVE_NOTIFY_RESPONSE);
              movePackage.putInt(side);
              movePackage.putInt(chessIndex);
              movePackage.putInt(column);
              movePackage.putInt(row);
              sendMessageToAllUser(movePackage);
              if (isEndGame) {
                onEndGameAction(side, NORMAL_WIN_REASON);
              }

              changeTurn();
              break;
            }
          }
        }
      }

      if (!isMoveAlowed) {
        DataPackage errorPackage = createPackage(MOVE_ERROR_RESPONSE);
        errorPackage.putInt(MOVE_IN_NOT_ALLOW_POSITION);
        user.server.write(errorPackage);
      }
      break;
    }
  }

  private void changeTurn() {
    if (currentTurn == redSide) {
      currentTurn = blackSide;
    } else {
      currentTurn = redSide;
    }
    timeCount = 0;
  }

  @Override
  public void onUserLeaveTable(User user) {
    int winSide = BroadChessMan.RED_SIDE;
    if (user.equals(redSide)) {
      winSide = BroadChessMan.BLACK_SIDE;
    }
    onEndGameAction(winSide, OPPONENT_RESIGN_WIN_REASON);
  }

  /**
   * Khởi tạo bàn cờ
   */
  public void initChessBoard() {
    int[][] redSidePosition = downSide;
    for (int i = 0; i < 8; i++) {
      chessMans[BroadChessMan.RED_SIDE][i] = new BroadChessMan(BroadChessMan.RED_SIDE, BroadChessMan.TOT_TYPE,
          redSidePosition[i][0], redSidePosition[i][1]);
    }
    chessMans[BroadChessMan.RED_SIDE][8] = new BroadChessMan(BroadChessMan.RED_SIDE, BroadChessMan.XE_TYPE,
        redSidePosition[8][0], redSidePosition[8][1]);
    chessMans[BroadChessMan.RED_SIDE][9] = new BroadChessMan(BroadChessMan.RED_SIDE, BroadChessMan.XE_TYPE,
        redSidePosition[9][0], redSidePosition[9][1]);

    chessMans[BroadChessMan.RED_SIDE][10] = new BroadChessMan(BroadChessMan.RED_SIDE, BroadChessMan.MA_TYPE,
        redSidePosition[10][0], redSidePosition[10][1]);
    chessMans[BroadChessMan.RED_SIDE][11] = new BroadChessMan(BroadChessMan.RED_SIDE, BroadChessMan.MA_TYPE,
        redSidePosition[11][0], redSidePosition[11][1]);

    chessMans[BroadChessMan.RED_SIDE][12] = new BroadChessMan(BroadChessMan.RED_SIDE, BroadChessMan.TUONG_TYPE,
        redSidePosition[12][0], redSidePosition[12][1]);
    chessMans[BroadChessMan.RED_SIDE][13] = new BroadChessMan(BroadChessMan.RED_SIDE, BroadChessMan.TUONG_TYPE,
        redSidePosition[13][0], redSidePosition[13][1]);

    chessMans[BroadChessMan.RED_SIDE][14] = new BroadChessMan(BroadChessMan.RED_SIDE, BroadChessMan.VUA_TYPE,
        redSidePosition[14][0], redSidePosition[14][1]);
    chessMans[BroadChessMan.RED_SIDE][15] = new BroadChessMan(BroadChessMan.RED_SIDE, BroadChessMan.HAU_TYPE,
        redSidePosition[15][0], redSidePosition[15][1]);

    int[][] blackSidePosition = upSide;
    for (int i = 0; i < 8; i++) {
      chessMans[BroadChessMan.BLACK_SIDE][i] = new BroadChessMan(BroadChessMan.BLACK_SIDE, BroadChessMan.TOT_TYPE,
          blackSidePosition[i][0], blackSidePosition[i][1]);
    }
    chessMans[BroadChessMan.BLACK_SIDE][8] = new BroadChessMan(BroadChessMan.BLACK_SIDE, BroadChessMan.XE_TYPE,
        blackSidePosition[8][0], blackSidePosition[8][1]);
    chessMans[BroadChessMan.BLACK_SIDE][9] = new BroadChessMan(BroadChessMan.BLACK_SIDE, BroadChessMan.XE_TYPE,
        blackSidePosition[9][0], blackSidePosition[9][1]);

    chessMans[BroadChessMan.BLACK_SIDE][10] = new BroadChessMan(BroadChessMan.BLACK_SIDE, BroadChessMan.MA_TYPE,
        blackSidePosition[10][0], blackSidePosition[10][1]);
    chessMans[BroadChessMan.BLACK_SIDE][11] = new BroadChessMan(BroadChessMan.BLACK_SIDE, BroadChessMan.MA_TYPE,
        blackSidePosition[11][0], blackSidePosition[11][1]);

    chessMans[BroadChessMan.BLACK_SIDE][12] = new BroadChessMan(BroadChessMan.BLACK_SIDE, BroadChessMan.TUONG_TYPE,
        blackSidePosition[12][0], blackSidePosition[12][1]);
    chessMans[BroadChessMan.BLACK_SIDE][13] = new BroadChessMan(BroadChessMan.BLACK_SIDE, BroadChessMan.TUONG_TYPE,
        blackSidePosition[13][0], blackSidePosition[13][1]);

    chessMans[BroadChessMan.BLACK_SIDE][14] = new BroadChessMan(BroadChessMan.BLACK_SIDE, BroadChessMan.VUA_TYPE,
        blackSidePosition[14][0], blackSidePosition[14][1]);
    chessMans[BroadChessMan.BLACK_SIDE][15] = new BroadChessMan(BroadChessMan.BLACK_SIDE, BroadChessMan.HAU_TYPE,
        blackSidePosition[15][0], blackSidePosition[15][1]);
  }

  private void onEndGameAction(int winSide, int winReason) {
    if (winSide == BroadChessMan.RED_SIDE) {
      table.setLastWinUser(redSide.getName());
    } else {
      table.setLastWinUser(blackSide.getName());
    }

    // Báo là có người đã thắng và các nước đi thắng
    DataPackage winPackage = createPackage(WIN_GAME_NOTIFY_RESPONSE);
    winPackage.putInt(winSide);
    winPackage.putInt(winReason);

    // TODO: Chưa thực hiện xử lý tính tiền nên trả về số tiền là 0
    winPackage.putInt(0);
    winPackage.putInt(0);

    sendMessageToAllUser(winPackage);
    table.endGame();

    if (timer != null) {
      timer.cancel();
    }
  }

  private int getOpponentSide(int side) {
    if (side == BroadChessMan.RED_SIDE) {
      return BroadChessMan.BLACK_SIDE;
    } else {
      return BroadChessMan.RED_SIDE;
    }
  }

  private int[][] filtAvailableMoveList(BroadChessMan chessMan, int side) {
    if (chessMan == null) {
      return null;
    }
    int iside = getOpponentSide(side);
    int[][] currentAvailableMoves = chessMan.getAvailableMove();

    System.out.println("Bugggggggggg" + currentAvailableMoves);

    System.out.println("Log1");
    for (int i = 0; i < currentAvailableMoves.length; i++) {
      if (currentAvailableMoves[i] != null) {
        System.out.println("Cac nuoc di chuyen duoc:" + currentAvailableMoves[i][0] + ":" + currentAvailableMoves[i][1]);
      }
    }
    int x = chessMan.getColumn();
    int y = chessMan.getRow();
    System.out.println("Log2");
    for (int i = 0; i < currentAvailableMoves.length; i++) {
      if (currentAvailableMoves[i] != null) {
        System.out.println("Log - 1 - 1");
        if ((currentAvailableMoves[i][0] != BroadChessMan.INFINITE) && (currentAvailableMoves[i][1] != BroadChessMan.INFINITE)) {
          if (getChessMan(side, currentAvailableMoves[i][0] + x, currentAvailableMoves[i][1] + y) != null) {
            currentAvailableMoves[i] = null;
            if ((i == 0) && (chessMan.getType() == BroadChessMan.TOT_TYPE)) {
              currentAvailableMoves[1] = null;
            }
          }

          if (chessMan.getType() == BroadChessMan.TOT_TYPE) {
            if (currentAvailableMoves[i] != null) {
              if (getChessMan(iside, currentAvailableMoves[i][0] + x, currentAvailableMoves[i][1] + y) != null) {
                currentAvailableMoves[i] = null;
                if (i == 0) {
                  currentAvailableMoves[1] = null;
                }
              }
            }
            if (chessMan.getSide() == BroadChessMan.RED_SIDE) {
              if (getChessMan(iside, x + 1, y - 1) != null) {
                currentAvailableMoves[2] = new int[] { 1, -1 };
              }
              if (getChessMan(iside, x - 1, y - 1) != null) {
                currentAvailableMoves[3] = new int[] { -1, -1 };
              }
            } else {
              if (getChessMan(iside, x + 1, y + 1) != null) {
                currentAvailableMoves[2] = new int[] { 1, 1 };
              }
              if (getChessMan(iside, x - 1, y + 1) != null) {
                currentAvailableMoves[3] = new int[] { -1, 1 };
              }
            }
          }
        } else {
          if ((currentAvailableMoves[i][0] == BroadChessMan.INFINITE) && (currentAvailableMoves[i][1] != BroadChessMan.INFINITE)) {
            int[][] newAvailableMove;
            int index = 0;
            if (currentAvailableMoves != null) {
              index = currentAvailableMoves.length;
              newAvailableMove = new int[14 + currentAvailableMoves.length][];
              for (int j = 0; j < currentAvailableMoves.length; j++) {
                if (currentAvailableMoves[j] != null) {
                  newAvailableMove[j] = new int[2];
                  newAvailableMove[j][0] = currentAvailableMoves[j][0];
                  newAvailableMove[j][1] = currentAvailableMoves[j][1];
                }
              }
            } else {
              newAvailableMove = new int[14][];
            }
            if (x > 0) {
              for (int j = x - 1; j >= 0; j--) {
                if (getChessMan(side, j, y) != null) {
                  break;
                }
                newAvailableMove[index] = new int[2];
                newAvailableMove[index][0] = j - x;
                newAvailableMove[index][1] = 0;
                index++;

                if (getChessMan(iside, j, y) != null) {
                  break;
                }
              }
            }
            if (x < 7) {
              for (int j = x + 1; j <= 7; j++) {
                if (getChessMan(side, j, y) != null) {
                  break;
                }
                newAvailableMove[index] = new int[2];
                newAvailableMove[index][0] = j - x;
                newAvailableMove[index][1] = 0;
                index++;

                if (getChessMan(iside, j, y) != null) {
                  break;
                }
              }
            }
            if (y > 0) {
              for (int j = y - 1; j >= 0; j--) {
                if (getChessMan(side, x, j) != null) {
                  break;
                }

                newAvailableMove[index] = new int[2];
                newAvailableMove[index][0] = 0;
                newAvailableMove[index][1] = j - y;
                index++;

                if (getChessMan(iside, x, j) != null) {
                  break;
                }
              }
            }

            if (y < 7) {
              for (int j = y + 1; j <= 7; j++) {
                if (getChessMan(side, x, j) != null) {
                  break;
                }

                newAvailableMove[index] = new int[2];
                newAvailableMove[index][0] = 0;
                newAvailableMove[index][1] = j - y;
                index++;

                if (getChessMan(iside, x, j) != null) {
                  break;
                }
              }
            }
            currentAvailableMoves = newAvailableMove;

          } else if ((currentAvailableMoves[i][0] == BroadChessMan.INFINITE)
              && (currentAvailableMoves[i][1] == BroadChessMan.INFINITE)) {
            int[][] newAvailableMove;
            int index = 0;
            if (currentAvailableMoves != null) {
              index = currentAvailableMoves.length;
              newAvailableMove = new int[14 + currentAvailableMoves.length][];
              for (int j = 0; j < currentAvailableMoves.length; j++) {
                if (currentAvailableMoves[j] != null) {
                  newAvailableMove[j] = new int[2];
                  newAvailableMove[j][0] = currentAvailableMoves[j][0];
                  newAvailableMove[j][1] = currentAvailableMoves[j][1];
                }
              }
            } else {
              newAvailableMove = new int[14][];
            }
            if ((x > 0) && (y > 0)) {
              for (int j = x - 1, k = y - 1; j >= 0 && k >= 0; j--, k--) {
                if (getChessMan(side, j, k) != null) {
                  break;
                }

                newAvailableMove[index] = new int[2];
                newAvailableMove[index][0] = j - x;
                newAvailableMove[index][1] = k - y;
                index++;

                if (getChessMan(iside, j, k) != null) {
                  break;
                }
              }
            }

            if ((x < 7) && (y > 0)) {
              for (int j = x + 1, k = y - 1; j <= 7 && k >= 0; j++, k--) {
                if (getChessMan(side, j, k) != null) {
                  break;
                }

                newAvailableMove[index] = new int[2];
                newAvailableMove[index][0] = j - x;
                newAvailableMove[index][1] = k - y;
                index++;

                if (getChessMan(iside, j, k) != null) {
                  break;
                }
              }
            }

            if ((x > 0) && (y < 7)) {
              for (int j = x - 1, k = y + 1; j >= 0 && k <= 7; j--, k++) {
                if (getChessMan(side, j, k) != null) {
                  break;
                }

                newAvailableMove[index] = new int[2];
                newAvailableMove[index][0] = j - x;
                newAvailableMove[index][1] = k - y;
                index++;

                if (getChessMan(iside, j, k) != null) {
                  break;
                }
              }
            }

            if ((x < 7) && (y < 7)) {
              for (int j = x + 1, k = y + 1; j <= 7 && k <= 7; j++, k++) {
                if (getChessMan(side, j, k) != null) {
                  break;
                }

                newAvailableMove[index] = new int[2];
                newAvailableMove[index][0] = j - x;
                newAvailableMove[index][1] = k - y;
                index++;

                if (getChessMan(iside, j, k) != null) {
                  break;
                }
              }
            }
            currentAvailableMoves = newAvailableMove;
          }
        }
      }
    }
    return currentAvailableMoves;
  }

  private BroadChessMan getChessMan(int side, int column, int row) {
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

  private void killBroadChessMan(BroadChessMan chessMan) {
    if (chessMan == null) {
      return;
    }
    for (int i = 0; i < chessMans[chessMan.getSide()].length; i++) {
      if (chessMans[chessMan.getSide()][i] == chessMan) {
        chessMans[chessMan.getSide()][i] = null;
        return;
      }
    }
  }

  @Override
  public void detroy() {
  }
}
