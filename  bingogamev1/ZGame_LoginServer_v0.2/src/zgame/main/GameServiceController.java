package zgame.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import zgame.bean.GameService;
import zgame.dao.GameServiceDao;
import zgame.socket.Server;
import zgame.socket.handle.GameServiceHandle;
import zgame.utils.Control;

public class GameServiceController extends Control {
  private static Logger log = Logger.getLogger(GameServiceController.class);
  private Map<String, GameService> gameServices = new HashMap<String, GameService>();
  private static boolean isRunning = true;

  @Override
  public void perform() {
    ServerSocket servSock = null;
    try {
      servSock = new ServerSocket(Global.SERVICE_CONTROL_PORT);
      log.info("-------------------------------------------------------");
      log.info("GAME SERVICE CONTROLLER WAS STARTED AT PORT " + Global.SERVICE_CONTROL_PORT);
      log.info("-------------------------------------------------------");
    } catch (IOException e1) {
      log.error("Game Service Controller can't start", e1);
      isRunning = false;
      return;
    }

    // Listening
    while (isRunning) {
      try {
        Socket clientSock = servSock.accept();
        log.info("GameServiceController : new GameService accept: " + clientSock.getInetAddress().getHostAddress() + ":"
            + clientSock.getPort());
        new GameServiceHandle(new Server(clientSock), this);
      } catch (IOException e) {
        log.warn("Game Service Controller accept client fail", e);
      }
    }
  }

  public void removeGameService(String gameServiceName) {
    gameServices.remove(gameServiceName);
  }

  public GameService getGameService(String gameServiceName) {
    return gameServices.get(gameServiceName);
  }

  public void storeGameService(GameService gameService) {
    gameServices.put(gameService.getName(), gameService);
  }

  public Map<String, GameService> getGameServices() {
    return gameServices;
  }

  public boolean isGameServiceValid(GameService gameService) {
    GameService fromDbGameService = null;
    GameServiceDao gameServiceDao = GameServiceDao.createInstance();
    try {
      fromDbGameService = gameServiceDao.getGameServiceById(gameService.getId());
    } finally {
      gameServiceDao.close();
    }

    if (fromDbGameService == null) {
      log.warn("GameService with id: " + gameService.getId() + " is not exist");
      return false;
    }

    if (!fromDbGameService.getName().equals(gameService.getName())) {
      log.warn("GameService with id: " + gameService.getId() + " has wrong name: " + gameService.getName());
      return false;
    }

    GameService gameServiceFromMap = getGameService(gameService.getName());
    if (gameServiceFromMap != null) {
      log.warn("GameService with name: " + gameService.getName() + " is dupplicate!");
      return false;
    }

    gameService.setMaxUser(fromDbGameService.getMaxUser());
    gameService.setUrl(fromDbGameService.getUrl());
    gameService.setPort(fromDbGameService.getPort());
    return true;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void stopGameService() {
    isRunning = false;
  }
}
