package zgame.bussiness;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import zgame.bean.GameService;
import zgame.bean.Session;
import zgame.bean.User;
import zgame.dao.AuthenticateDao;
import zgame.main.Global;
import zgame.main.Main;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.Server;
import zgame.utils.MD5;

public class GameServiceBussiness {
  private static Logger log = Logger.getLogger(GameServiceBussiness.class);
  
	public static void userJoinInInform(Server server, DataPackage inputDataPackage) throws SQLException {
	  AuthenticateDao authenticateDao = AuthenticateDao.createInstance();
		String username = inputDataPackage.nextString();
		
		Session session = Global.sessionMap.get(username);
		if (session == null) {
		  log.warn("GameServiceBussiness : userJoinInInform : user " + username + " has joined to GameService " + server.name + " without session in DefaultService");
			return;
		} 
		
		GameService gameService = Main.gameServiceController.getGameService(server.name);
		session.setCurrentGameService(gameService);
		session.setOnline(true);
		
		// Cập nhật thông tin user cho GameService
		User user = authenticateDao.getUserInfo(username);
		
		DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.USER_INFO_REPONSE);
		dataPackage.putString(username);
		dataPackage.putInt(user.getMoney());
		dataPackage.putInt(user.getAvatarId());
		server.write(dataPackage);
		
		log.info("User " + username + " JOIN in server: " + gameService.getName());
		authenticateDao.close();
		
		gameService.onUserJoinIn(user);
		FriendBussiness.onUserJoinToGameService(username, server);
	}
	
	public static void userOutInform(Server server, DataPackage inputDataPackage) {
		String username = inputDataPackage.nextString();
		
		Session session = Global.sessionMap.get(username);
		if (session == null) {
		  log.warn("GameServiceBussiness : userOutInform : user " + username + " has joined to GameService " + server.name + " without session in DefaultService");
			return;
		}
		
		GameService gameService = Main.gameServiceController.getGameService(server.name);
		if ((session.getCurrentGameService() != null) && session.getCurrentGameService().getId().equals(gameService.getId())) {
			session.setCurrentGameService(null);
			session.setOnline(false);
			gameService.onUserGetOut(username);
		}
		
		log.info("User " + username + " OUT server: " + gameService.getName());
	}
	
	public static void updateGameServiceInfo(Server server, DataPackage inputDataPackage) {
		int concurrentUser = inputDataPackage.nextInt();
		GameService gameService = Main.gameServiceController.getGameService(server.name);
		gameService.setCocurrentUser(concurrentUser);
		
//		log.info("Receive game service info from: " + gameService.getName());
	}
	
	public static void authenRequestHandle(Server server, DataPackage inputDataPackage) {
		String username = inputDataPackage.nextString();
		String salt1 = inputDataPackage.nextString();
		
		Session session = Global.sessionMap.get(username);
		if (session == null) {
			DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.SESSION_NOT_EXIST);
			dataPackage.putString(username);
			server.write(dataPackage);
			return;
		}
		
		String encodeData = MD5.toBase64((session.getId() + salt1).getBytes());
		DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.SESSION_RESPONSE);
		dataPackage.putString(username);
		dataPackage.putString(encodeData);
		server.write(dataPackage);
		
		log.info("Response sessionId for user " + username);
	}
	
	public static void validGameServiceInfo(Server server, DataPackage inputDataPackage) {
		String id = inputDataPackage.nextString();
		String name = inputDataPackage.nextString();
		GameService gameService = new GameService(id, name);
		
		if (Main.gameServiceController.isGameServiceValid(gameService)) {
			Main.gameServiceController.storeGameService(gameService);
			server.name = gameService.getName();
			gameService.setServer(server);
			
			log.info("GameService regist success: " + gameService.toString());
			
			DataPackage dataPackage = new DataPackage(ProtocolConstants.RequestHeader.GAME_SERVICE_REGISTER_SUCCESS_REQUEST);
			dataPackage.putInt(Global.TIME_OUT);
			dataPackage.putInt(Global.DATA_UPDATE_SEQUENCE_TIME);
			server.write(dataPackage);
		} else {
			log.warn("GameService is invalid: " + gameService.toString());
			server.write(new DataPackage(ProtocolConstants.RequestHeader.GAME_SERVICE_REGISTER_FAIL_REQUEST));
			server.write(new DataPackage(ProtocolConstants.RequestHeader.CLOSE_CONNECTION_REQUEST));
			server.detroy();
		}
	}
	
	public static void releaseAllConnectionToUser(Server server) {
		GameService gameService = Main.gameServiceController.getGameService(server.name);
		List<User> users = new ArrayList<User>(gameService.getUserMap().values());
		for (User user : users) {
			AuthenticateBussiness.signOut(user.getUsername());
		}
		
	}
}
