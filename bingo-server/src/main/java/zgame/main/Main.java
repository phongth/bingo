package zgame.main;

import java.io.IOException;

import org.apache.log4j.Logger;

import zgame.socket.client.ClientConnection;

public class Main {
  private static final Logger log = Logger.getLogger(Main.class);

  public static void main(String[] args) {
    try {
      Initialize.init();
      Initialize.initForCache();
    } catch (IOException e) {
      log.error("Server can't start.Please check your config file");
      return;
    }

    // Connect to DefaultService to regist GameService
    log.info("Connect to DEFAULT SERVICE at " + Global.DEFAULT_SERVICE_URL + ":" + Global.DEFAULT_SERVICE_PORT);
    Global.client = new ClientConnection(Global.DEFAULT_SERVICE_URL, Global.DEFAULT_SERVICE_PORT, Global.socketClientHandle);
  }
}
