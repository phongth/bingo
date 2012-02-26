package development;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Image;

import state.socket.ClientConnection;
import state.socket.DataReceiveListener;
import development.bean.Game;
import development.bean.LoginInfo;
import development.bean.Provider;
import development.bean.Room;
import development.bean.Session;
import development.bean.Table;
import development.bean.User;
import development.socket.AuthenticateHandle;
import development.socket.GameActionHandle;

public class Global {
	public static FrmLogo frmLogo = new FrmLogo();
	public static FrmLogin frmLogin;
	public static FrmRegister frmRegister;
	public static FrmGameService frmGameService;
	public static FrmChooseGame frmChooseGame;
	public static FrmListRoom frmListRoom;
	public static FrmListTable frmListTable;
	public static FrmTable frmTable;
	public static FrmChangePassword frmChangePassword;
	public static FrmFriend frmFriend;

	public static FrmCaro frmCaro;
	public static FrmChess frmChess;
	public static FrmBroadChess frmBroadChess;

	public static AuthenticateHandle authenticateHandle;
	public static GameActionHandle gameActionHandle;

	public static boolean isCheckForUpdateDone = false;
	public static boolean isCheckForDownloadResourceDone = false;
	public static boolean isAutoLoginDone = false;

	public static void init() {
		authenticateHandle = new AuthenticateHandle();
		gameActionHandle = new GameActionHandle();

		frmLogin = new FrmLogin();
		frmRegister = new FrmRegister();
		frmGameService = new FrmGameService();
		frmChooseGame = new FrmChooseGame();
		frmListRoom = new FrmListRoom();
		frmListTable = new FrmListTable();
		frmTable = new FrmTable();
		frmChangePassword = new FrmChangePassword();
		frmFriend = new FrmFriend();

		frmCaro = new FrmCaro();
		frmChess = new FrmChess();
		frmBroadChess = new FrmBroadChess();

		DataReceiveListener[] listeners = new DataReceiveListener[] { null,
				null, frmChess, frmCaro, frmBroadChess };
		for (int i = 0; i < Constants.GAME_ID.length; i++) {
			Game game = new Game(Constants.GAME_ID[i], listeners[i]);
			game.setName(Constants.GAME_NAME[i]);
			Global.gameMap.put(game.getId(), game);
		}
	}

	public static int HEART_BREATH_SEQUENCE_TIME = 15000;

	public static ClientConnection authenClient;
	public static ClientConnection gameActionClient;

	public static Provider provider = Rms.loadProvider();
	public static LoginInfo loginInfo = Rms.loadLoginInfo();
	public static Session session;

	public static Vector gameServices = new Vector();
	public static Hashtable gameMap = new Hashtable();
	public static Hashtable roomMap = new Hashtable();
	public static Hashtable tableMap = new Hashtable();
	public static Vector tableUsers = new Vector(); // Danh sách user trong bàn
													// chơi hiện tại
	public static Vector friends = new Vector();

	public static User currentUser = new User();
	public static Game currentGame;
	public static Room currentRoom;
	public static Table currentTable;
	public static Image userAvatar;

	public static String waitingToValidUsername; // Username mà người dùng sử
													// dụng để đăng nhập
	public static String validedUsername; // Username mà đã xác nhận ngầm

	public static Image getAvatar(String name) {
		return null;
	}
}
