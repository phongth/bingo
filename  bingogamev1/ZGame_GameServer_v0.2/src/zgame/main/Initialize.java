package zgame.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import net.sf.ehcache.Element;
import zgame.bean.Game;
import zgame.bean.Room;
import zgame.bean.Table;
import zgame.utils.CacheUtil;

public class Initialize {
  private static Logger log = Logger.getLogger(Initialize.class);

  private static Properties properties;

  public static void init() throws IOException {
    // Init log4J
    Properties logProperties = new Properties();
    logProperties.load(new FileInputStream("conf/log4j.properties"));
    PropertyConfigurator.configure(logProperties);
    log.info("Init log4j done");

    properties = new Properties();
    properties.load(new FileInputStream("conf/server.properties"));

    // Get GameService info
    Global.GAME_SERVICE_ID = get("GAME_SERVICE_ID");
    Global.GAME_SERVICE_NAME = get("GAME_SERVICE_NAME").trim();

    // Lấy thông tin chung của hệ thống
    Global.PORT = Integer.parseInt(get("PORT"));
    Global.TIME_OUT = Integer.parseInt(get("TIME_OUT"));
    Global.HEART_BREATH_SEQUENCE_TIME = Integer.parseInt(get("HEART_BREATH_SEQUENCE_TIME"));
    Global.DATA_UPDATE_SEQUENCE_TIME = Integer.parseInt(get("DATA_UPDATE_SEQUENCE_TIME"));
    Global.DEFAULT_SERVICE_PORT = Integer.parseInt(get("DEFAULT_SERVICE_PORT"));
    Global.DEFAULT_SERVICE_URL = get("DEFAULT_SERVICE_URL");
    Global.MAX_POOL = Integer.parseInt(get("MAX_POOL"));
    
    Global.REQUEST_MANAGER_CORE_POLL_SIZE = Integer.parseInt(get("REQUEST_MANAGER_CORE_POLL_SIZE"));
    Global.REQUEST_MANAGER_MAX_POLL_SIZE = Integer.parseInt(get("REQUEST_MANAGER_MAX_POLL_SIZE"));
    Global.REQUEST_MANAGER_KEEP_ALIVE_TIME = Integer.parseInt(get("REQUEST_MANAGER_KEEP_ALIVE_TIME"));

    // Lấy thông tin để khởi tạo game
    String gameIds = get("GAME_ID").trim(); // GAME_ID = tala : tlmb : cotuong :
                                            // caro
    List<String> gameIdList = getElements(gameIds, ':');
    String gameNames = get("GAME_NAME").trim(); // GAME_NAME = Tá lả : TLMB : Cờ
                                                // tướng : Caro
    List<String> gameNameList = getElements(gameNames, ':');
    if (gameIdList.size() != gameNameList.size()) {
      throw new IllegalArgumentException("ERROR: Initialize : init : Number of gameIds and gameNames must be equal.");
    }

    // Lấy thông tin số lượng phòng và bàn
    int[] defaultNumberOfRoom = getElementsToInt("DEFAULT_NUMBER_OF_ROOM", ':');
    if (defaultNumberOfRoom == null) {
      throw new IllegalArgumentException(
          "ERROR: Initialize : init : Property DEFAULT_NUMBER_OF_ROOM is not exist. Plz check your config file.");
    }

    int[] defaultNumberOfTablePerRoom = getElementsToInt("DEFAULT_NUMER_OF_TABLE_PER_ROOM", ':');
    if (defaultNumberOfTablePerRoom == null) {
      throw new IllegalArgumentException(
          "ERROR: Initialize : init : Property DEFAULT_NUMER_OF_TABLE_PER_ROOM is not exist. Plz check your config file.");
    }

    int[] defaultMaxUserOfRoom = getElementsToInt("DEFAULT_MAX_USER_OF_ROOM", ':');
    if (defaultMaxUserOfRoom == null) {
      throw new IllegalArgumentException(
          "ERROR: Initialize : init : Property DEFAULT_MAX_USER_OF_ROOM is not exist. Plz check your config file.");
    }

    // Lấy thông tin về số người tối đa trong mỗi phòng tùy theo game
    int[] maxUserPerTableByGame = getElementsToInt("NUMBER_OF_USER_PER_TABLE_BY_GAME", ':');
    if (maxUserPerTableByGame == null) {
      throw new IllegalArgumentException(
          "ERROR: Initialize : init : Property NUMBER_OF_USER_PER_TABLE_BY_GAME is not exist. Plz check your config file.");
    }

    // Lấy min bid theo room type
    int[] minBidByRoomType = getElementsToInt("MIN_BID_BY_ROOM_TYPE", ':');
    if (minBidByRoomType == null) {
      throw new IllegalArgumentException(
          "ERROR: Initialize : init : Property MIN_BID_BY_ROOM_TYPE is not exist. Plz check your config file.");
    }
    Room.MIN_BID_BY_ROOM_TYPE = minBidByRoomType;

    // Lấy max bid theo room type
    int[] maxBidByRoomType = getElementsToInt("MAX_BID_BY_ROOM_TYPE", ':');
    if (maxBidByRoomType == null) {
      throw new IllegalArgumentException(
          "ERROR: Initialize : init : Property MAX_BID_BY_ROOM_TYPE is not exist. Plz check your config file.");
    }
    Room.MAX_BID_BY_ROOM_TYPE = maxBidByRoomType;

    // Khởi tạo danh sách game
    for (int i = 0; i < gameIdList.size(); i++) {
      Game game = new Game(gameIdList.get(i));
      game.setName(gameNameList.get(i));
      Global.lobby.addChild(game);

      // Get number of room by type
      int[] numberOfRoom = getElementsToInt(game.getId().toUpperCase() + "_NUMBER_OF_ROOM", ':');
      if (numberOfRoom == null) {
        numberOfRoom = defaultNumberOfRoom;
      }

      // Get number of table per room
      int[] numberOfTablePerRoom = getElementsToInt(game.getId().toUpperCase() + "_NUMER_OF_TABLE_PER_ROOM", ':');
      if (numberOfTablePerRoom == null) {
        numberOfTablePerRoom = defaultNumberOfTablePerRoom;
      }

      // Get max user of room
      int[] maxUserOfRoom = getElementsToInt(game.getId().toUpperCase() + "_MAX_USER_OF_ROOM", ':');
      if (maxUserOfRoom == null) {
        maxUserOfRoom = defaultMaxUserOfRoom;
      }

      // Create room
      createRoomsAndTable(numberOfRoom, game, numberOfTablePerRoom, maxUserPerTableByGame[i], maxUserOfRoom);
    }

    properties = null;
  }

  public static void initForCache() {
    Element element = new Element("initCacheTime", String.valueOf(System.currentTimeMillis()));
    CacheUtil.cacheLv1.put(element);
    CacheUtil.cacheLv2.put(element);
    CacheUtil.cacheLv3.put(element);
  }

  private static void createRoomsAndTable(int[] numberOfRoom, Game game, int[] numberOfTablePerRoom, int maxUserOfTable,
      int[] maxUserOfRoom) {
    int roomId = 1;
    for (int i = 0; i < numberOfRoom.length; i++) {
      for (int j = 0; j < numberOfRoom[i]; j++, roomId++) {
        String roomName = fillUpToLen(roomId, 2);
        Room room = new Room(game.getId() + "/" + roomName);
        room.setName(roomName);
        room.setType(i);
        room.setMaxUser(maxUserOfRoom[i]);
        game.addChild(room);
        room.setGame(game);

        // Create table
        createTables(numberOfTablePerRoom[i], room, game, maxUserOfTable);
      }
    }
  }

  private static void createTables(int numberOfTablePerRoom, Room room, Game game, int maxUserOfTable) {
    for (int i = 0; i < numberOfTablePerRoom; i++) {
      String tableName = fillUpToLen(i + 1, 2);
      Table table = new Table(room.getId() + "/" + tableName);
      table.setName(tableName);
      table.setGame(game);
      table.setBid(room.getMinBid());
      room.addChild(table);
      table.setRoom(room);
      table.setMaxUser(maxUserOfTable);
      table.setGameComponent(game.createGameComponent(table));
    }
  }

  private static String fillUpToLen(int number, int len) {
    String str = String.valueOf(number);
    int loopTime = len - str.length();

    if (loopTime <= 0) {
      return str;
    }

    for (int i = 0; i < loopTime; i++) {
      str = "0" + str;
    }
    return str;
  }

  private static String get(String id) throws IllegalArgumentException {
    String value = properties.getProperty(id);
    if (value == null) {
      throw new IllegalArgumentException("ERROR: Initialize : get : Property " + id
          + " is not exist. Plz check your config file.");
    }
    return value;
  }

  private static int[] getElementsToInt(String key, char seperator) {
    String source = properties.getProperty(key);
    if (source == null) {
      return null;
    }

    List<String> elements = getElements(source, seperator);
    int[] ints = new int[elements.size()];
    for (int i = 0; i < ints.length; i++) {
      ints[i] = Integer.parseInt(elements.get(i));
    }
    return ints;
  }

  private static List<String> getElements(String source, char seperator) {
    List<String> list = new ArrayList<String>();
    int beginIndex = 0;
    int endIndex = source.indexOf(seperator, beginIndex);
    while (endIndex != -1) {
      list.add(source.substring(beginIndex, endIndex).trim());
      beginIndex = endIndex + 1;
      endIndex = source.indexOf(seperator, beginIndex);
    }
    list.add(source.substring(beginIndex).trim());
    return list;
  }
}
