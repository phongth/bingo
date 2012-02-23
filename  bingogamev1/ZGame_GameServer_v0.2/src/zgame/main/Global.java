package zgame.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zgame.bean.Friend;
import zgame.bean.GroupChat;
import zgame.bean.Lobby;
import zgame.bean.Room;
import zgame.bean.ServerSessionStore;
import zgame.bean.Session;
import zgame.bean.Table;
import zgame.socket.client.ClientConnection;
import zgame.socket.handle.SocketClientHandle;
import zgame.socket.server.ServerConnection;

public class Global {
  public static String GAME_SERVICE_ID;
  public static String GAME_SERVICE_NAME;

  public static int PORT;
  public static int TIME_OUT;
  public static int HEART_BREATH_SEQUENCE_TIME;
  public static int DATA_UPDATE_SEQUENCE_TIME;

  public static int DEFAULT_SERVICE_PORT;
  public static String DEFAULT_SERVICE_URL;
  public static int MAX_POOL;
  
  //Constant for RequestManager
  public static int REQUEST_MANAGER_CORE_POLL_SIZE;
  public static int REQUEST_MANAGER_MAX_POLL_SIZE;
  public static int REQUEST_MANAGER_KEEP_ALIVE_TIME; // Minutes
  
  //Danh sách các connection chưa xác nhận user
  public static List<ServerConnection> notAuthenConnectionList = new ArrayList<ServerConnection>();

  /** Lưu trữ các connection theo username */
  public static Map<String, ServerConnection> connectionMap = new HashMap<String, ServerConnection>(); // Key is username

  public static Map<String, Table> tableMap = new HashMap<String, Table>(); // Store table by table Id
  public static Map<String, Room> roomMap = new HashMap<String, Room>(); // Store room by room Id

  public static Map<String, GroupChat> groupChatMap = new HashMap<String, GroupChat>();

  public static Lobby lobby = new Lobby("");
  public static ClientConnection client;
  public static SocketClientHandle socketClientHandle = new SocketClientHandle();
  public static ServerListener serverListener = new ServerListener();

  public static Map<String, Map<String, Friend>> friendListCache = new HashMap<String, Map<String, Friend>>(); // Key is username

  /**
   * Session map này chỉ dùng tạm để kiểm chứng sessionId của user chứ không để
   * lưu trữ lâu dài
   */
  public static Map<String, Session> sessionMapTmp = new HashMap<String, Session>();
  public static Map<String, ServerSessionStore> encodedSessionFromClient = new HashMap<String, ServerSessionStore>();
}
