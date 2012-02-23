package zgame.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import zgame.socket.Server;
import zgame.socket.handle.SocketServerHandle;

public class Main {
  private static Logger log = Logger.getLogger(Main.class);
  private static boolean isRunning = true;
  public static GameServiceController gameServiceController;

  public static void main(String[] args) {
    ServerSocket servSock = null;
    // Executor service = null;
    try {
      Initialize.init();
      Initialize.initForDB();
      Initialize.loadImageToCatche("res");
      Initialize.loadAvatarToCatche("avatars");
      Initialize.initForCache();

      servSock = new ServerSocket(Global.PORT);

      gameServiceController = new GameServiceController();
      gameServiceController.start();

      // Create executor to manage threat pool
      // service = Executors.newFixedThreadPool(Global.MAX_POOL);

      log.info("-------------------------------------------------------");
      log.info("AUTHENTICATE SERVER WAS STARTED AT PORT " + Global.PORT);
      log.info("-------------------------------------------------------");
    } catch (Exception e) {
      log.error("Server can't start.Please check your config file ", e);
      isRunning = false;
      return;
    }

    // Listening
    while (isRunning) {
      try {
        Socket clientSocket = servSock.accept();
        Server server = new Server(clientSocket);
        new SocketServerHandle(server);
        // service.execute(server);
      } catch (IOException e) {
        log.warn("Accept client fail", e);
      }
    }
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void stop() {
    isRunning = false;
    // TODO: stop all current connection when server stop
  }
}
