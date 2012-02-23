package zgame.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import zgame.socket.handle.SocketServerHandle;
import zgame.socket.server.ServerConnection;
import zgame.utils.Control;

public class ServerListener extends Control {
  private static final Logger log = Logger.getLogger(ServerListener.class);
  private static boolean isRunning = true;

  @Override
  public void perform() {
    ServerSocket servSock = null;
    try {
      servSock = new ServerSocket(Global.PORT);
      log.info("-------------------------------------------------------");
      log.info("GAME SERVER " + Global.GAME_SERVICE_NAME + " WAS STARTED AT PORT " + Global.PORT);
      log.info("-------------------------------------------------------");
    } catch (IOException e) {
      log.error("Server can't start.Please check your config file");
      return;
    }

    // Listening
    while (isRunning) {
      try {
        Socket clientSocket = servSock.accept();
        ServerConnection server = new ServerConnection(clientSocket);
        new SocketServerHandle(server);
      } catch (IOException e) {
        log.warn("Exception on accept client", e);
      }
    }
  }

  public void cancel() {
    isRunning = false;
  }
}
