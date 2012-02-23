package zgame.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

public class Server implements Runnable, DataReceiveListener {
  private static Logger log = Logger.getLogger(Server.class);
  private Socket clientSocket;

  private DataInputStream is;
  private DataOutputStream os;

  private WriterThread writerThread;
  private ReaderThread readerThread;

  protected boolean isRunning = true;

  private DataReceiveListener listener;
  public String name;

  public Server() {
  }

  public Server(Socket clientSocket) throws IOException {
    this.clientSocket = clientSocket;
    is = new DataInputStream(this.clientSocket.getInputStream());
    os = new DataOutputStream(this.clientSocket.getOutputStream());
    writerThread = new WriterThread(os, this);
    readerThread = new ReaderThread(is, this, this);
  }

  public void run() {
    // while (isRunning) {
    // try {
    // Thread.sleep(Global.HEART_BREATH_SEQUENCE_TIME);
    // } catch (InterruptedException e) {
    // }
    //
    // if ((readerThread == null) || (writerThread == null)) {
    // return;
    // }
    //
    // // Check time out of client
    // if (System.currentTimeMillis() - readerThread.getLastTimeReveive() >
    // Global.TIME_OUT) {
    // writerThread.write(new
    // DataPackage(ProtocolConstants.ResponseHeader.TIME_OUT_NOTIFY_RESPONSE));
    // writerThread.write(new
    // DataPackage(ProtocolConstants.ResponseHeader.CLOSE_CONNECTION_RESPONSE));
    // detroy();
    // }
    // }
  }

  public void onRecieveData(DataPackage dataPackage) {
    if (listener != null) {
      listener.onRecieveData(dataPackage);
    }
  }

  public void onConnectDone() {
  }

  public void onConnectFail() {
  }

  public void onDisconnect() {
    if (listener != null) {
      listener.onDisconnect();
    }
  }

  public void setListener(DataReceiveListener listener) {
    this.listener = listener;
  }

  public void write(DataPackage dataPackage) {
    if (writerThread != null) {
      writerThread.write(dataPackage);
    } else {
      log.warn(" writerThread is NULL at connection: " + name);
    }
  }

  public void detroy() {
    isRunning = false;
    listener = null;
    if (readerThread != null) {
      readerThread.detroy();
      readerThread = null;
    }
    if (writerThread != null) {
      writerThread.detroy();
      writerThread = null;
    }

    try {
      if (is != null) {
        is.close();
        is = null;
      }
    } catch (IOException e) {
    }
    try {
      if (os != null) {
        os.close();
        os = null;
      }
    } catch (IOException e) {
    }
    try {
      if (clientSocket != null) {
        clientSocket.close();
        clientSocket = null;
      }
    } catch (IOException e) {
    }
  }
}
