package zgame.bussiness;

import java.util.Collection;

import zgame.bean.ChessMan;
import zgame.bean.Table;
import zgame.bean.User;
import zgame.job.Job;
import zgame.socket.DataPackage;

public class GameChessBussiness extends GameComponent {
  private static final int MOVE_REQUEST = 1001; // Gửi thông tin về nước đã đi

  private static final int CHIEU_TUONG_NOTIFY_RESPONSE = 2005;
  private static final int WIN_GAME_NOTIFY_RESPONSE = 2006; // Báo cho các user
                                                            // về user đã thắng
                                                            // và danh sách các
                                                            // nước đi thắng
  private static final int INIT_DATA_RESPONSE = 2007; // Thiết đặt các thông số
                                                      // cho client
  private static final int MOVE_ERROR_RESPONSE = 2008; // Báo lại cho ng chơi
                                                       // vừa đánh là nước đánh
                                                       // không được chấp nhận
  private static final int MOVE_NOTIFY_RESPONSE = 2009; // Báo nước đi của người
                                                        // vừa đánh

  private static final int NOT_YOUR_TURN_ERROR = 3001;
  private static final int MOVE_IN_NOT_ALLOW_POSITION = 3002;

  private static final int NORMAL_WIN_REASON = 4001;
  private static final int OPPONENT_RESIGN_WIN_REASON = 4002;
  private static final int OPPONENT_TIMEOUT_WIN_REASON = 4003;

  private static final int NUMBER_OF_ROW = 10;
  private static final int NUMBER_OF_COLUMN = 9;

  public static final int TIME_PER_MATCH = 30 * 60; // Seconds
  public static final int TIME_PER_MOVE = 30; // Seconds // TODO: Đưa vào config

  private static final int[][] upSide = new int[][] { { 0, 3 }, { 2, 3 }, { 4, 3 }, { 6, 3 }, { 8, 3 }, { 1, 2 }, { 7, 2 },
      { 0, 0 }, { 8, 0 }, { 1, 0 }, { 7, 0 }, { 2, 0 }, { 6, 0 }, { 3, 0 }, { 5, 0 }, { 4, 0 } };
  private static final int[][] downSide = new int[][] { { 0, 6 }, { 2, 6 }, { 4, 6 }, { 6, 6 }, { 8, 6 }, { 1, 7 }, { 7, 7 },
      { 0, 9 }, { 8, 9 }, { 1, 9 }, { 7, 9 }, { 2, 9 }, { 6, 9 }, { 3, 9 }, { 5, 9 }, { 4, 9 } };

  private User redSide;
  private User blackSide; // Bên được đi đầu tiên
  private User currentTurn;

  private ChessMan[][] chessMans;

  private int[] timePlay = new int[2];

  private int timeCount; // int second
  private Job timer;

  public GameChessBussiness(Table table) {
    super(table);

    chessMans = new ChessMan[2][16];
    chessMans[ChessMan.RED_SIDE] = new ChessMan[16];
    chessMans[ChessMan.BLACK_SIDE] = new ChessMan[16];
  }

  @Override
  public void init() {
    // Khởi tạo các bên chơi
    Collection<User> users = table.getUsers();

    // Tìm xem nếu người đánh thắng ván trước đó vẫn còn trong bàn thì cho ng đó
    // đánh trước
    if (table.getLastWinUser() != null) {
      for (User user : users) {
        if (user.getName().equals(table.getLastWinUser())) {
          redSide = user;
          break;
        }
      }
    }

    // Nếu người đánh thắng ván trước không còn trong bàn thì cho chủ bàn đánh
    // trước
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

        int side = ChessMan.BLACK_SIDE;
        if (currentTurn.equals(redSide)) {
          side = ChessMan.RED_SIDE;
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
    int winSide = ChessMan.RED_SIDE;
    if (currentTurn.equals(redSide)) {
      winSide = ChessMan.BLACK_SIDE;
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

      int side = ChessMan.BLACK_SIDE;
      int oppSide = ChessMan.RED_SIDE;
      User opponetUser = redSide;
      if (user.equals(redSide)) {
        side = ChessMan.RED_SIDE;
        oppSide = ChessMan.BLACK_SIDE;
        opponetUser = blackSide;
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
              ChessMan enemy = getChessMan(oppSide, column, row);
              if (enemy != null) { // vị trí vừa đi đến đang có quân cờ của đối
                                   // thủ
                if (enemy.getType() == ChessMan.VUA_TYPE) {
                  isEndGame = true;
                }
                killChessMan(enemy);
              }
              chessMans[side][chessIndex].changePosition(column, row); // Dịch
                                                                       // chuyển
                                                                       // quân
                                                                       // cờ
                                                                       // trên
                                                                       // bàn cờ
                                                                       // đến vị
                                                                       // trí
                                                                       // client
                                                                       // gửi
                                                                       // lên
                                                                       // phía
                                                                       // server

              // Báo về nước vừa đi cho user còn lại
              DataPackage movePackage = createPackage(MOVE_NOTIFY_RESPONSE);
              movePackage.putInt(side);
              movePackage.putInt(chessIndex);
              movePackage.putInt(column);
              movePackage.putInt(row);
              sendMessageToAllUser(movePackage);

              if (isBiChieuTuong(oppSide)) {
                DataPackage chieuTuongPackage = createPackage(CHIEU_TUONG_NOTIFY_RESPONSE);
                opponetUser.server.write(chieuTuongPackage);
              }

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
    int winSide = ChessMan.RED_SIDE;
    if (user.equals(redSide)) {
      winSide = ChessMan.BLACK_SIDE;
    }
    onEndGameAction(winSide, OPPONENT_RESIGN_WIN_REASON);
  }

  /**
   * Khởi tạo bàn cờ
   */
  public void initChessBoard() {
    int[][] redSidePosition = downSide;
    chessMans[ChessMan.RED_SIDE][0] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TOT_TYPE, redSidePosition[0][0],
        redSidePosition[0][1]);
    chessMans[ChessMan.RED_SIDE][1] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TOT_TYPE, redSidePosition[1][0],
        redSidePosition[1][1]);
    chessMans[ChessMan.RED_SIDE][2] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TOT_TYPE, redSidePosition[2][0],
        redSidePosition[2][1]);
    chessMans[ChessMan.RED_SIDE][3] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TOT_TYPE, redSidePosition[3][0],
        redSidePosition[3][1]);
    chessMans[ChessMan.RED_SIDE][4] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TOT_TYPE, redSidePosition[4][0],
        redSidePosition[4][1]);
    chessMans[ChessMan.RED_SIDE][5] = new ChessMan(ChessMan.RED_SIDE, ChessMan.PHAO_TYPE, redSidePosition[5][0],
        redSidePosition[5][1]);
    chessMans[ChessMan.RED_SIDE][6] = new ChessMan(ChessMan.RED_SIDE, ChessMan.PHAO_TYPE, redSidePosition[6][0],
        redSidePosition[6][1]);
    chessMans[ChessMan.RED_SIDE][7] = new ChessMan(ChessMan.RED_SIDE, ChessMan.XE_TYPE, redSidePosition[7][0],
        redSidePosition[7][1]);
    chessMans[ChessMan.RED_SIDE][8] = new ChessMan(ChessMan.RED_SIDE, ChessMan.XE_TYPE, redSidePosition[8][0],
        redSidePosition[8][1]);
    chessMans[ChessMan.RED_SIDE][9] = new ChessMan(ChessMan.RED_SIDE, ChessMan.MA_TYPE, redSidePosition[9][0],
        redSidePosition[9][1]);
    chessMans[ChessMan.RED_SIDE][10] = new ChessMan(ChessMan.RED_SIDE, ChessMan.MA_TYPE, redSidePosition[10][0],
        redSidePosition[10][1]);
    chessMans[ChessMan.RED_SIDE][11] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TUONG_TYPE, redSidePosition[11][0],
        redSidePosition[11][1]);
    chessMans[ChessMan.RED_SIDE][12] = new ChessMan(ChessMan.RED_SIDE, ChessMan.TUONG_TYPE, redSidePosition[12][0],
        redSidePosition[12][1]);
    chessMans[ChessMan.RED_SIDE][13] = new ChessMan(ChessMan.RED_SIDE, ChessMan.SY_TYPE, redSidePosition[13][0],
        redSidePosition[13][1]);
    chessMans[ChessMan.RED_SIDE][14] = new ChessMan(ChessMan.RED_SIDE, ChessMan.SY_TYPE, redSidePosition[14][0],
        redSidePosition[14][1]);
    chessMans[ChessMan.RED_SIDE][15] = new ChessMan(ChessMan.RED_SIDE, ChessMan.VUA_TYPE, redSidePosition[15][0],
        redSidePosition[15][1]);

    int[][] blackSidePosition = upSide;
    chessMans[ChessMan.BLACK_SIDE][0] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TOT_TYPE, blackSidePosition[0][0],
        blackSidePosition[0][1]);
    chessMans[ChessMan.BLACK_SIDE][1] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TOT_TYPE, blackSidePosition[1][0],
        blackSidePosition[1][1]);
    chessMans[ChessMan.BLACK_SIDE][2] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TOT_TYPE, blackSidePosition[2][0],
        blackSidePosition[2][1]);
    chessMans[ChessMan.BLACK_SIDE][3] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TOT_TYPE, blackSidePosition[3][0],
        blackSidePosition[3][1]);
    chessMans[ChessMan.BLACK_SIDE][4] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TOT_TYPE, blackSidePosition[4][0],
        blackSidePosition[4][1]);
    chessMans[ChessMan.BLACK_SIDE][5] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.PHAO_TYPE, blackSidePosition[5][0],
        blackSidePosition[5][1]);
    chessMans[ChessMan.BLACK_SIDE][6] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.PHAO_TYPE, blackSidePosition[6][0],
        blackSidePosition[6][1]);
    chessMans[ChessMan.BLACK_SIDE][7] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.XE_TYPE, blackSidePosition[7][0],
        blackSidePosition[7][1]);
    chessMans[ChessMan.BLACK_SIDE][8] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.XE_TYPE, blackSidePosition[8][0],
        blackSidePosition[8][1]);
    chessMans[ChessMan.BLACK_SIDE][9] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.MA_TYPE, blackSidePosition[9][0],
        blackSidePosition[9][1]);
    chessMans[ChessMan.BLACK_SIDE][10] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.MA_TYPE, blackSidePosition[10][0],
        blackSidePosition[10][1]);
    chessMans[ChessMan.BLACK_SIDE][11] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TUONG_TYPE, blackSidePosition[11][0],
        blackSidePosition[11][1]);
    chessMans[ChessMan.BLACK_SIDE][12] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.TUONG_TYPE, blackSidePosition[12][0],
        blackSidePosition[12][1]);
    chessMans[ChessMan.BLACK_SIDE][13] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.SY_TYPE, blackSidePosition[13][0],
        blackSidePosition[13][1]);
    chessMans[ChessMan.BLACK_SIDE][14] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.SY_TYPE, blackSidePosition[14][0],
        blackSidePosition[14][1]);
    chessMans[ChessMan.BLACK_SIDE][15] = new ChessMan(ChessMan.BLACK_SIDE, ChessMan.VUA_TYPE, blackSidePosition[15][0],
        blackSidePosition[15][1]);
  }

  private void onEndGameAction(int winSide, int winReason) {
    if (winSide == ChessMan.RED_SIDE) {
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
    if (side == ChessMan.RED_SIDE) {
      return ChessMan.BLACK_SIDE;
    } else {
      return ChessMan.RED_SIDE;
    }
  }

  private int[][] filtAvailableMoveList(ChessMan chessMan, int side) {
    if (chessMan == null) {
      return null;
    }

    int[][] currentAvailableMoves = chessMan.getAvailableMove();

    for (int i = 0; i < currentAvailableMoves.length; i++) {
      if (chessMan.getType() == ChessMan.TOT_TYPE) {
        for (int j = 0; j < currentAvailableMoves.length; j++) {
          if (currentAvailableMoves[j] != null) {
            int nextColumn = chessMan.getColumn() + currentAvailableMoves[j][0];
            int nextRow = chessMan.getRow() + currentAvailableMoves[j][1];

            if (getChessMan(side, nextColumn, nextRow) != null) {
              currentAvailableMoves[j] = null;
            }
          }
        }
      } else if ((chessMan.getType() == ChessMan.VUA_TYPE) || (chessMan.getType() == ChessMan.SY_TYPE)) {
        for (int j = 0; j < currentAvailableMoves.length; j++) {
          if (currentAvailableMoves[j] != null) {
            int nextColumn = chessMan.getColumn() + currentAvailableMoves[j][0];
            int nextRow = chessMan.getRow() + currentAvailableMoves[j][1];

            // Check để ko cho tướng đi đối mặt tướng
            ChessMan opponentKing = chessMans[getOpponentSide(side)][15];
            boolean isKingsFaceToFace = true;
            if (opponentKing.getColumn() == nextColumn) {
              ChessMan[] chessMans2 = getAllChessManInColumn(nextColumn);// Lấy
                                                                         // các
                                                                         // con
                                                                         // cờ
                                                                         // cùng
                                                                         // cột
                                                                         // với
                                                                         // tướng
              for (int k = 0; k < chessMans2.length; k++) {
                if ((chessMans2[k] != null) && (chessMans2[k].getType() != ChessMan.VUA_TYPE)) {
                  if (chessMan.getType() == ChessMan.VUA_TYPE) {
                    if (side == ChessMan.RED_SIDE) {
                      if ((chessMans2[k].getRow() < nextRow) && (chessMans2[k].getRow() > opponentKing.getRow())) {
                        isKingsFaceToFace = false;
                        break;
                      }
                    } else {
                      if ((chessMans2[k].getRow() > nextRow) && (chessMans2[k].getRow() < opponentKing.getRow())) {
                        isKingsFaceToFace = false;
                        break;
                      }
                    }
                  } else {
                    if (side == ChessMan.RED_SIDE) {
                      if ((chessMans2[k].getRow() <= nextRow) && (chessMans2[k].getRow() > opponentKing.getRow())) {
                        isKingsFaceToFace = false;
                        break;
                      }
                    } else {
                      if ((chessMans2[k].getRow() >= nextRow) && (chessMans2[k].getRow() < opponentKing.getRow())) {
                        isKingsFaceToFace = false;
                        break;
                      }
                    }
                  }
                }
              }

              if (isKingsFaceToFace) {
                currentAvailableMoves[j] = null;
                continue;
              }
            }

            if (getChessMan(side, nextColumn, nextRow) != null) {
              currentAvailableMoves[j] = null;
              continue;
            }

            if ((nextColumn < 3) || (nextColumn > 5)) {
              currentAvailableMoves[j] = null;
              continue;
            }

            if (((chessMan.getRow() < 5) && (nextRow > 2)) || ((chessMan.getRow() > 4) && (nextRow < 7))) {
              currentAvailableMoves[j] = null;
              continue;
            }
          }
        }
      } else if (chessMan.getType() == ChessMan.TUONG_TYPE) {
        for (int j = 0; j < currentAvailableMoves.length; j++) {
          if (currentAvailableMoves[j] != null) {
            int nextColumn = chessMan.getColumn() + currentAvailableMoves[j][0];
            int nextRow = chessMan.getRow() + currentAvailableMoves[j][1];
            if (getChessMan(side, nextColumn, nextRow) != null
                || getChessMan(ChessMan.BLACK_SIDE, (nextColumn + chessMan.getColumn()) / 2, (nextRow + chessMan.getRow()) / 2) != null
                || getChessMan(ChessMan.RED_SIDE, (nextColumn + chessMan.getColumn()) / 2, (nextRow + chessMan.getRow()) / 2) != null) {
              currentAvailableMoves[j] = null;
              continue;
            }

            if (((chessMan.getRow() < 5) && nextRow > 4) || ((chessMan.getRow() > 4) && nextRow < 5)) {
              currentAvailableMoves[j] = null;
              continue;
            }
          }
        }
      } else if (chessMan.getType() == ChessMan.MA_TYPE) {
        for (int j = 0; j < currentAvailableMoves.length; j++) {
          if (currentAvailableMoves[j] != null) {
            int nextColumn = chessMan.getColumn() + currentAvailableMoves[j][0];
            int nextRow = chessMan.getRow() + currentAvailableMoves[j][1];

            if (getChessMan(side, nextColumn, nextRow) != null) {
              currentAvailableMoves[j] = null;
              continue;
            }

            if (Math.abs(currentAvailableMoves[j][0]) == 2) {
              if ((getChessMan(side, chessMan.getColumn() + (currentAvailableMoves[j][0] > 0 ? 1 : -1), chessMan.getRow()) != null)
                  || getChessMan(getOpponentSide(side), chessMan.getColumn() + (currentAvailableMoves[j][0] > 0 ? 1 : -1),
                      chessMan.getRow()) != null) {
                currentAvailableMoves[j] = null;
                continue;
              }
            } else if (Math.abs(currentAvailableMoves[j][1]) == 2) {
              if ((getChessMan(side, chessMan.getColumn(), chessMan.getRow() + (currentAvailableMoves[j][1] > 0 ? 1 : -1)) != null)
                  || getChessMan(getOpponentSide(side), chessMan.getColumn(), chessMan.getRow()
                      + (currentAvailableMoves[j][1] > 0 ? 1 : -1)) != null) {
                currentAvailableMoves[j] = null;
                continue;
              }
            }
          }
        }
      } else if ((chessMan.getType() == ChessMan.XE_TYPE) || (chessMan.getType() == ChessMan.PHAO_TYPE)) {
        int[][] availableMoveByRow = getAvailableMoveByRow(chessMan);
        int[][] availableMoveByColumn = getAvailableMoveByColumn(chessMan);
        int[][] availableMovesTmp = new int[availableMoveByRow.length + availableMoveByColumn.length][];
        for (int j = 0; j < availableMoveByRow.length; j++) {
          availableMovesTmp[j] = availableMoveByRow[j];
        }
        for (int j = 0; j < availableMoveByColumn.length; j++) {
          availableMovesTmp[j + availableMoveByRow.length] = availableMoveByColumn[j];
        }
        currentAvailableMoves = availableMovesTmp;
      }
    }

    filterToDontMakeTheKingsFaceToFace(chessMan, currentAvailableMoves);
    return currentAvailableMoves;
  }

  private void filterToDontMakeTheKingsFaceToFace(ChessMan chessMan, int[][] currentAvailableMoves) {
    // Nếu là quân Tướng di chuyển thì không cần check nữa vì đã check ở trên
    // rồi
    if (chessMan.getType() == ChessMan.VUA_TYPE) {
      return;
    }

    ChessMan king0 = chessMans[0][15];
    ChessMan king1 = chessMans[1][15];

    if ((king0 == null) || (king1 == null)) {
      return;
    }

    // Nếu 2 vua đứng khác cột so với nhau thì không cần check
    if (king0.getColumn() != king1.getColumn()) {
      return;
    }

    ChessMan[] chessMan2 = getAllChessManInColumn(king0.getColumn());
    for (int i = 0; i < chessMan2.length; i++) {
      if ((chessMan2[i] != null) && (chessMan2[i].getType() != ChessMan.VUA_TYPE)) {
        // Nếu có quân cờ khác nằm ở cùng cột
        if (chessMan2[i] != chessMan) {
          // Nếu quân cờ đó nằm giữa 2 tướng thì 2 tướng không thể đối mặt được
          if ((chessMan2[i].getRow() < king0.getRow()) && (chessMan2[i].getRow() > king1.getRow())) {
            return;
          }
          continue;
        }
      }
    }

    // Chỉ còn mỗi quân đang định di chuyển là đứng giữa 2 vua
    for (int i = 0; i < currentAvailableMoves.length; i++) {
      if (currentAvailableMoves[i] != null) {
        if (currentAvailableMoves[i][0] != 0) {
          currentAvailableMoves[i] = null;
          continue;
        }
      }
    }
  }

  private int[][] getAvailableMoveByRow(ChessMan chessMan) {
    ChessMan[] chessManInSameRow = getAllChessManInRow(chessMan.getRow());
    int minColumn = -1;
    int maxColumn = NUMBER_OF_COLUMN;
    int beforeMinEnemyColumn = -1;
    int afterMaxEnemyColumn = -1;
    for (int j = 0; j < chessManInSameRow.length; j++) {
      if ((minColumn < j) && (j < chessMan.getColumn()) && (chessManInSameRow[j] != null)) {
        if ((minColumn != -1) && (chessManInSameRow[minColumn].getSide() != chessMan.getSide())) {
          beforeMinEnemyColumn = minColumn;
        }
        minColumn = j;
      } else if ((maxColumn == NUMBER_OF_COLUMN) && (j > chessMan.getColumn()) && (chessManInSameRow[j] != null)) {
        maxColumn = j;
        if ((chessManInSameRow[j].getSide() != chessMan.getSide()) && (chessMan.getType() == ChessMan.XE_TYPE)) {
          maxColumn = j + 1;
        }
      } else if ((maxColumn != NUMBER_OF_COLUMN) && (afterMaxEnemyColumn == -1) && (chessManInSameRow[j] != null)
          && (chessManInSameRow[j].getSide() != chessMan.getSide())) {
        afterMaxEnemyColumn = j;
      }
    }

    if (minColumn == -1) {
      minColumn = 0;
    } else if (chessManInSameRow[minColumn].getSide() == chessMan.getSide()) {
      minColumn++;
    } else if ((chessManInSameRow[minColumn].getSide() != chessMan.getSide()) && (chessMan.getType() == ChessMan.PHAO_TYPE)) {
      minColumn++;
    }

    int[][] availableMove = new int[maxColumn - minColumn + 2][];
    for (int i = minColumn; i < maxColumn; i++) {
      if (i != chessMan.getColumn()) {
        availableMove[i - minColumn] = new int[] { i - chessMan.getColumn(), 0 };
      }
    }
    if ((beforeMinEnemyColumn > -1) && (chessMan.getType() == ChessMan.PHAO_TYPE)) {
      // Đếm số quân cờ nằm giữa 2 quân đang xét
      int count = 0;
      for (int i = beforeMinEnemyColumn + 1; i < chessMan.getColumn(); i++) {
        if (chessManInSameRow[i] != null) {
          count++;
        }
      }
      if (count == 1) {
        availableMove[availableMove.length - 2] = new int[] { beforeMinEnemyColumn - chessMan.getColumn(), 0 };
      }
    }
    if ((afterMaxEnemyColumn > -1) && (chessMan.getType() == ChessMan.PHAO_TYPE)) {
      // Đếm số quân cờ nằm giữa 2 quân đang xét
      int count = 0;
      for (int i = chessMan.getColumn() + 1; i < afterMaxEnemyColumn; i++) {
        if (chessManInSameRow[i] != null) {
          count++;
        }
      }
      if (count == 1) {
        availableMove[availableMove.length - 1] = new int[] { afterMaxEnemyColumn - chessMan.getColumn(), 0 };
      }
    }
    return availableMove;
  }

  private int[][] getAvailableMoveByColumn(ChessMan chessMan) {
    ChessMan[] chessManInSameColumn = getAllChessManInColumn(chessMan.getColumn());
    int minRow = -1;
    int maxRow = NUMBER_OF_ROW;
    int beforeMinEnemyRow = -1;
    int afterMaxEnemyRow = -1;
    for (int j = 0; j < chessManInSameColumn.length; j++) {
      if ((minRow < j) && (j < chessMan.getRow()) && (chessManInSameColumn[j] != null)) {
        if ((minRow != -1) && (chessManInSameColumn[minRow].getSide() != chessMan.getSide())) {
          beforeMinEnemyRow = minRow;
        }
        minRow = j;
      } else if ((maxRow == NUMBER_OF_ROW) && (j > chessMan.getRow()) && (chessManInSameColumn[j] != null)) {
        maxRow = j;
        if ((chessManInSameColumn[j].getSide() != chessMan.getSide()) && (chessMan.getType() == ChessMan.XE_TYPE)) {
          maxRow = j + 1;
        }
      } else if ((maxRow != NUMBER_OF_ROW) && (afterMaxEnemyRow == -1) && (chessManInSameColumn[j] != null)
          && (chessManInSameColumn[j].getSide() != chessMan.getSide())) {
        afterMaxEnemyRow = j;
      }
    }

    if (minRow == -1) {
      minRow = 0;
    } else if (chessManInSameColumn[minRow].getSide() == chessMan.getSide()) {
      minRow++;
    } else if ((chessManInSameColumn[minRow].getSide() != chessMan.getSide()) && (chessMan.getType() == ChessMan.PHAO_TYPE)) {
      minRow++;
    }

    int[][] availableMove = new int[maxRow - minRow + 2][];
    for (int i = minRow; i < maxRow; i++) {
      if (i != chessMan.getRow()) {
        availableMove[i - minRow] = new int[] { 0, i - chessMan.getRow() };
      }
    }
    if ((beforeMinEnemyRow > -1) && (chessMan.getType() == ChessMan.PHAO_TYPE)) {
      // Đếm số quân cờ nằm giữa 2 quân đang xét
      int count = 0;
      for (int i = beforeMinEnemyRow + 1; i < chessMan.getRow(); i++) {
        if (chessManInSameColumn[i] != null) {
          count++;
        }
      }
      if (count == 1) {
        availableMove[availableMove.length - 2] = new int[] { 0, beforeMinEnemyRow - chessMan.getRow() };
      }
    }
    if ((afterMaxEnemyRow > -1) && (chessMan.getType() == ChessMan.PHAO_TYPE)) {
      // Đếm số quân cờ nằm giữa 2 quân đang xét
      int count = 0;
      for (int i = chessMan.getRow() + 1; i < afterMaxEnemyRow; i++) {
        if (chessManInSameColumn[i] != null) {
          count++;
        }
      }
      if (count == 1) {
        availableMove[availableMove.length - 1] = new int[] { 0, afterMaxEnemyRow - chessMan.getRow() };
      }
    }
    return availableMove;
  }

  private ChessMan[] getAllChessManInRow(int row) {
    ChessMan[] chessMansTmp = new ChessMan[9];
    for (int i = 0; i < chessMans[ChessMan.BLACK_SIDE].length; i++) {
      if (chessMans[ChessMan.BLACK_SIDE][i] != null) {
        if (chessMans[ChessMan.BLACK_SIDE][i].getRow() == row) {
          chessMansTmp[chessMans[ChessMan.BLACK_SIDE][i].getColumn()] = chessMans[ChessMan.BLACK_SIDE][i];
        }
      }
    }
    for (int i = 0; i < chessMans[ChessMan.RED_SIDE].length; i++) {
      if (chessMans[ChessMan.RED_SIDE][i] != null) {
        if (chessMans[ChessMan.RED_SIDE][i].getRow() == row) {
          chessMansTmp[chessMans[ChessMan.RED_SIDE][i].getColumn()] = chessMans[ChessMan.RED_SIDE][i];
        }
      }
    }
    return chessMansTmp;
  }

  private ChessMan[] getAllChessManInColumn(int column) {
    ChessMan[] chessMansTmp = new ChessMan[10];
    for (int i = 0; i < chessMans[ChessMan.RED_SIDE].length; i++) {
      if (chessMans[ChessMan.RED_SIDE][i] != null) {
        if (chessMans[ChessMan.RED_SIDE][i].getColumn() == column) {
          chessMansTmp[chessMans[ChessMan.RED_SIDE][i].getRow()] = chessMans[ChessMan.RED_SIDE][i];
        }
      }
    }

    for (int i = 0; i < chessMans[ChessMan.BLACK_SIDE].length; i++) {
      if (chessMans[ChessMan.BLACK_SIDE][i] != null) {
        if (chessMans[ChessMan.BLACK_SIDE][i].getColumn() == column) {
          chessMansTmp[chessMans[ChessMan.BLACK_SIDE][i].getRow()] = chessMans[ChessMan.BLACK_SIDE][i];
        }
      }
    }
    return chessMansTmp;
  }

  private boolean isBiChieuTuong(int side) {
    ChessMan king = chessMans[side][15];
    if (king == null) {
      return false;
    }
    int opponentSide = getOpponentSide(side);
    for (int i = 0; i <= 10; i++) {
      if (chessMans[opponentSide][i] != null) {
        int[][] availableMoveTmp = filtAvailableMoveList(chessMans[opponentSide][i], opponentSide);
        for (int j = 0; j < availableMoveTmp.length; j++) {
          if (availableMoveTmp[j] != null) {
            if ((availableMoveTmp[j][0] + chessMans[opponentSide][i].getColumn() == king.getColumn())
                && (availableMoveTmp[j][1] + chessMans[opponentSide][i].getRow() == king.getRow())) {
              return true;
            }
          }
        }
      }
    }
    return false;
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
        return;
      }
    }
  }

  @Override
  public void detroy() {
  }
}
