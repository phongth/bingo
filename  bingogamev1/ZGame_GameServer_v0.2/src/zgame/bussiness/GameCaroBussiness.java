package zgame.bussiness;

import java.util.Collection;

import zgame.bean.Table;
import zgame.bean.User;
import zgame.job.Job;
import zgame.socket.DataPackage;

public class GameCaroBussiness extends GameComponent {
  private static final int MOVE_REQUEST = 1001; // Gửi thông tin về nước đã đi

  private static final int MOVE_ERROR_RESPONSE = 2001; // Báo lại cho ng chơi
                                                       // vừa đánh là nước đánh
                                                       // không được chấp nhận
  private static final int MOVE_NOTIFY_RESPONSE = 2002; // Báo nước đi của người
                                                        // vừa đánh
  private static final int WIN_GAME_NOTIFY_RESPONSE = 2005; // Báo cho các user
                                                            // về user đã thắng
                                                            // và danh sách các
                                                            // nước đi thắng
  private static final int INIT_DATA_RESPONSE = 2007; // Thiết đặt các thông số
                                                      // cho client

  private static final int NOT_YOUR_TURN_ERROR = 3001;
  private static final int MOVE_IN_NOT_ALLOW_POSITION = 3002;

  private static final int NORMAL_WIN_REASON = 4001;
  private static final int OPPONENT_RESIGN_WIN_REASON = 4002;
  private static final int OPPONENT_TIMEOUT_WIN_REASON = 4003;

  private static final int NUMBER_OF_ROW = 100;
  private static final int NUMBER_OF_COLUMN = 100;
  private static final int ROUND_SIDE = -1;
  private static final int CROSS_SIDE = 1;

  public static final int TIME_PER_MOVE = 15; // Seconds // TODO: Đưa vào config

  private User roundSide;
  private User crossSide; // Bên được đi đầu tiên
  private User currentTurn;

  // Các điểm để vẽ gạch thắng
  private int winPointX1;
  private int winPointY1;
  private int winPointX2;
  private int winPointY2;

  private boolean isCheckWinSuccess;
  private int[][] matrix = new int[NUMBER_OF_COLUMN][NUMBER_OF_ROW];
  private int timeCount; // int second
  private Job timer;

  public GameCaroBussiness(Table table) {
    super(table);
  }

  @Override
  public void init() {
    isCheckWinSuccess = false;
    winPointX1 = -1;
    winPointY1 = -1;
    winPointX2 = -1;
    winPointY2 = -1;

    // Khởi tạo bàn chơi
    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
        matrix[i][j] = 0;
      }
    }

    // Khởi tạo các bên chơi
    Collection<User> users = table.getUsers();

    // Tìm xem nếu người đánh thắng ván trước đó vẫn còn trong bàn thì cho ng đó
    // đánh trước
    if (table.getLastWinUser() != null) {
      for (User user : users) {
        if (user.getName().equals(table.getLastWinUser())) {
          crossSide = user;
          break;
        }
      }
    }

    // Nếu người đánh thắng ván trước không còn trong bàn thì cho chủ bàn đánh
    // trước
    if (crossSide == null) {
      crossSide = table.getTableMaster();
    }

    // Xác định nốt người chơi còn lại
    for (User user : users) {
      if (!user.equals(crossSide)) {
        roundSide = user;
        break;
      }
    }
    currentTurn = crossSide;

    // Gửi thông tin init cho tất cả các người chơi
    DataPackage initPackage = createPackage(INIT_DATA_RESPONSE);
    initPackage.putInt(TIME_PER_MOVE);
    initPackage.putInt(CROSS_SIDE); // Là bên được đi trước
    initPackage.putString(crossSide.getName());
    initPackage.putInt(ROUND_SIDE);
    initPackage.putString(roundSide.getName());
    sendMessageToAllUser(initPackage);

    // Khởi tạo đồng hồ đếm giờ
    initTimer();
  }

  private void initTimer() {
    timer = new Job() {
      protected void init() {
        timeCount = 0;
      }

      public void loop() {
        try {
          sleep(1000);
        } catch (InterruptedException e) {
        }

        timeCount++;
        if (timeCount > TIME_PER_MOVE + 5) {
          onTimeOut();
          cancel();
        }
      }
    };
    timer.start();
  }

  private void onTimeOut() {
    int winSide = CROSS_SIDE;
    if (currentTurn.equals(crossSide)) {
      winSide = ROUND_SIDE;
    }
    onEndGameAction(winSide, OPPONENT_TIMEOUT_WIN_REASON);
  }

  @Override
  public void onActionPerform(User user, DataPackage inputDataPackage) {
    int header = inputDataPackage.getHeader();
    switch (header) {
    case MOVE_REQUEST:
      onMoveRequest(user, inputDataPackage);
      break;
    }
  }

  @Override
  public void onUserLeaveTable(User user) {
    int winSide = CROSS_SIDE;
    if (user.equals(crossSide)) {
      winSide = ROUND_SIDE;
    }
    onEndGameAction(winSide, OPPONENT_RESIGN_WIN_REASON);
  }

  private void onMoveRequest(User user, DataPackage inputDataPackage) {
    if (!user.equals(currentTurn)) {
      DataPackage dataPackage = createPackage(MOVE_ERROR_RESPONSE);
      dataPackage.putInt(NOT_YOUR_TURN_ERROR);
      user.server.write(dataPackage);
      return;
    }

    int x = inputDataPackage.nextInt();
    int y = inputDataPackage.nextInt();

    if (matrix[x][y] != 0) {
      DataPackage errorPackage = createPackage(MOVE_ERROR_RESPONSE);
      errorPackage.putInt(MOVE_IN_NOT_ALLOW_POSITION);
      user.server.write(errorPackage);
      return;
    }

    int side = ROUND_SIDE;
    if (user.equals(crossSide)) {
      side = CROSS_SIDE;
    }

    matrix[x][y] = side;

    // Báo về nước vừa đi cho user còn lại
    DataPackage movePackage = createPackage(MOVE_NOTIFY_RESPONSE);
    movePackage.putInt(side);
    movePackage.putInt(x);
    movePackage.putInt(y);
    if (side == ROUND_SIDE) {
      crossSide.server.write(movePackage);
    } else {
      roundSide.server.write(movePackage);
    }
    changeTurn();

    // Kiểm tra xem sau nước đi đã thắng chưa
    checkWinDoc(side, x, y);
    checkWinNgang(side, x, y);
    checkWinCheo(side, x, y);
    checkWinCheo2(side, x, y);

    if (isCheckWinSuccess) {
      onEndGameAction(side, NORMAL_WIN_REASON);
    }
  }

  private void changeTurn() {
    if (currentTurn == roundSide) {
      currentTurn = crossSide;
    } else {
      currentTurn = roundSide;
    }
    timeCount = 0;
  }

  private void onEndGameAction(int winSide, int winReason) {
    if (winSide == CROSS_SIDE) {
      table.setLastWinUser(crossSide.getName());
    } else {
      table.setLastWinUser(roundSide.getName());
    }

    // Báo là có người đã thắng và các nước đi thắng
    DataPackage winPackage = createPackage(WIN_GAME_NOTIFY_RESPONSE);
    winPackage.putInt(winSide);
    winPackage.putInt(winReason);
    winPackage.putInt(winPointX1);
    winPackage.putInt(winPointY1);
    winPackage.putInt(winPointX2);
    winPackage.putInt(winPointY2);

    // TODO: Chưa thực hiện xử lý tính tiền nên trả về số tiền là 0
    winPackage.putInt(0);
    winPackage.putInt(0);

    sendMessageToAllUser(winPackage);
    table.endGame();

    if (timer != null) {
      timer.cancel();
    }
  }

  private void checkWinDoc(int playerSide, int x, int y) {
    int minY = y - 1;
    while ((minY >= 0) && (matrix[x][minY] == playerSide)) {
      minY--;
    }
    minY = minY + 1;

    int maxY = y + 1;
    while ((maxY < NUMBER_OF_ROW) && (matrix[x][maxY] == playerSide)) {
      maxY++;
    }
    maxY = maxY - 1;
    if (maxY - minY >= 4) {
      winPointX1 = x;
      winPointY1 = minY;
      winPointX2 = x;
      winPointY2 = maxY;
      isCheckWinSuccess = true;
    }
  }

  private void checkWinNgang(int playerSide, int x, int y) {
    int minX = x - 1;
    while ((minX >= 0) && (matrix[minX][y] == playerSide)) {
      minX--;
    }
    minX = minX + 1;

    int maxX = x + 1;
    while ((maxX < NUMBER_OF_COLUMN) && (matrix[maxX][y] == playerSide)) {
      maxX++;
    }
    maxX = maxX - 1;

    if (maxX - minX >= 4) {
      winPointX1 = minX;
      winPointY1 = y;
      winPointX2 = maxX;
      winPointY2 = y;
      isCheckWinSuccess = true;
    }
  }

  private void checkWinCheo(int playerSide, int x, int y) {
    int minX = x - 1;
    int minY = y - 1;
    while ((minX >= 0) && (minY >= 0) && (matrix[minX][minY] == playerSide)) {
      minX--;
      minY--;
    }
    minX = minX + 1;
    minY = minY + 1;

    int maxX = x + 1;
    int maxY = y + 1;
    while ((maxX < NUMBER_OF_COLUMN) && (maxY < NUMBER_OF_ROW) && (matrix[maxX][maxY] == playerSide)) {
      maxX++;
      maxY++;
    }
    maxX = maxX - 1;
    maxY = maxY - 1;

    if (maxX - minX >= 4) {
      winPointX1 = minX;
      winPointY1 = minY;
      winPointX2 = maxX;
      winPointY2 = maxY;
      isCheckWinSuccess = true;
    }
  }

  private void checkWinCheo2(int playerSide, int x, int y) {
    int maxX = x + 1;
    int minY = y - 1;
    while ((maxX < NUMBER_OF_COLUMN) && (minY >= 0) && (matrix[maxX][minY] == playerSide)) {
      maxX++;
      minY--;
    }
    maxX = maxX - 1;
    minY = minY + 1;

    int minX = x - 1;
    int maxY = y + 1;
    while ((minX >= 0) && (maxY < NUMBER_OF_ROW) && (matrix[minX][maxY] == playerSide)) {
      minX--;
      maxY++;
    }
    minX = minX + 1;
    maxY = maxY - 1;

    if (maxX - minX >= 4) {
      winPointX1 = minX;
      winPointY1 = maxY;
      winPointX2 = maxX;
      winPointY2 = minY;
      isCheckWinSuccess = true;
    }
  }

  @Override
  public void detroy() {
    roundSide = null;
    crossSide = null;
    currentTurn = null;
    timer = null;
  }
}