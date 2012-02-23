package zgame.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zgame.bean.ImageInfo;
import zgame.bean.Session;
import zgame.bean.User;
import zgame.jobs.CheckToReleaseTimeOutDBConnectionJob;
import zgame.jobs.CommitPaymentDataJob;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.ServerConnection;

public class Global {
  public static boolean DEBUG_MODE;

  public static int TIME_OUT;
  public static int HEART_BREATH_SEQUENCE_TIME;
  public static int PORT;
  public static int SERVICE_CONTROL_PORT;
  public static int MAX_POOL;

  public static long SESSION_TIME_OUT;
  public static int DATA_UPDATE_SEQUENCE_TIME;

  public static String DB_USER;
  public static String DB_PASSWORD;
  public static String DB_DRIVER;
  public static String DB_URL;
  public static int DB_CONNECTION_MAX_POOL;
  public static int DB_CONNECTION_TIME_OUT;
  public static int DB_COMMIT_PAYMENT_DATA_SEQUENCE_TIME;

  public static int DEFAULT_PROVIDER_ID;
  public static int TAX_PERCENT;

  public static int USER_NAME_MAX_LEN;
  public static int USER_NAME_MIN_LEN;

  public static int PASSWORD_MAX_LEN;
  public static int PASSWORD_MIN_LEN;
  
  // Constant for RequestManager
  public static int REQUEST_MANAGER_CORE_POLL_SIZE;
  public static int REQUEST_MANAGER_MAX_POLL_SIZE;
  public static int REQUEST_MANAGER_KEEP_ALIVE_TIME; // Minutes
  
  public static CommitPaymentDataJob commitPaymentDataJob = new CommitPaymentDataJob();
  public static CheckToReleaseTimeOutDBConnectionJob checkToReleaseTimeOutDBConnectionJob = new CheckToReleaseTimeOutDBConnectionJob();

  // Danh sách các connection chưa xác nhận user
  public static List<ServerConnection> notAuthenConnectionList = new ArrayList<ServerConnection>();
  
  // Lưu các connection đến client theo username
  public static Map<String, ServerConnection> connectionMap = new HashMap<String, ServerConnection>();
  
  //Store user session by username
  public static Map<String, Session> sessionMap = new HashMap<String, Session>(); 

  //Cache thông tin user by username
  public static Map<String, User> userInfoCache = new HashMap<String, User>(); 
  
  //Map username by userId
  public static Map<String, String> userNameByUserIdMap = new HashMap<String, String>(); 
  
  //Cache user friend list by username
  public static Map<String, Map<String, String>> friendListCache = new HashMap<String, Map<String, String>>(); 
  
  // Cache requestAddFriend by user
  public static Map<String, Map<String, String>> requestAddFriendCache = new HashMap<String, Map<String, String>>(); 

  // Lưu danh sách resource client cần
  public static Map<String, ImageInfo> imagesMap = new HashMap<String, ImageInfo>(); 
  
  public static Map<String, List<ImageInfo>> avatarMap = new HashMap<String, List<ImageInfo>>();
  public static DataPackage categoryListDataPackage = new DataPackage(
      ProtocolConstants.ResponseHeader.AVATAR_CATEGORY_LIST_RESPONSE);
}
