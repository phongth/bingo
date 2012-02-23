package zgame.socket.client;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import zgame.socket.DataPackage;
import zgame.socket.DataReceiveListener;
import zgame.utils.Control;

public class ReaderThread extends Control {
  private static final Logger log = Logger.getLogger(ReaderThread.class);

  private DataInputStream is;
  private long lastTimeReveive;
  private boolean isRunning = true;
  private DataReceiveListener listener;
  private Client client;

  public ReaderThread(DataInputStream is, DataReceiveListener listener, Client client) {
    this.is = is;
    this.listener = listener;
    this.client = client;
    start();
  }

  public void perform() {
    while (isRunning) {
      try {
        int len = is.readInt();
        if ((len < 0) || (len > 1000000)) {
          throw new IllegalArgumentException("ReaderThread: len is not valid: " + len);
        }
        // log.info("Read data len: " + len);

        byte[] data = new byte[len];
        is.readFully(data);
        lastTimeReveive = System.currentTimeMillis();
        listener.onRecieveData(new DataPackage(data));
      } catch (IOException e) {
        if (isRunning) {
          listener.onDisconnect();
        }
        log.info("connection close on IOException");
        client.detroy();
        return;
      } catch (Throwable ex) {
        log.warn("ERROR: ReaderThread : faltal exception:", ex);
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
