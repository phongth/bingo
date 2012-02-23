package zgame.socket;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import zgame.utils.Control;

public class ReaderThread extends Control {
  private static Logger log = Logger.getLogger(ReaderThread.class);

  private DataInputStream is;
  private long lastTimeReveive;
  private boolean isRunning = true;
  private DataReceiveListener listener;
  private Server server;

  public ReaderThread(DataInputStream is, DataReceiveListener listener, Server protocol) {
    this.is = is;
    this.listener = listener;
    this.server = protocol;
    start();
  }

  public void perform() {
    while (isRunning) {
      try {
        int len = is.readInt();
        if (len < 0) {
          log.warn("Len is not valid: " + len + " at connection: " + server.name);
          server.detroy();
          return;
        }
        byte[] data = new byte[len];
        is.readFully(data);
        lastTimeReveive = System.currentTimeMillis();
        listener.onRecieveData(new DataPackage(data));
      } catch (IOException e) {
        if (isRunning) {
          listener.onDisconnect();
        }
        log.info("Connection close on IOException: " + server.name);
        server.detroy();
        return;
      } catch (Throwable ex) {
        log.warn("Faltal exception", ex);
      }
    }
  }

  public long getLastTimeReveive() {
    return lastTimeReveive;
  }

  public void detroy() {
    isRunning = false;
    is = null;
    listener = null;
  }
}
