package zgame.socket.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import zgame.bean.User;
import zgame.bussiness.AuthenticateBussiness;
import zgame.main.RequestManager;
import zgame.socket.DataPackage;
import zgame.socket.DataReceiveListener;
import zgame.socket.handle.SocketServerHandle;

public class ServerConnection implements DataReceiveListener {
  private static final Logger log = Logger.getLogger(ServerConnection.class);

  private Socket clientSocket;

  private DataInputStream is;
  private DataOutputStream os;

  private WriterThread writerThread;
  private ReaderThread readerThread;

  private DataReceiveListener listener;
  public User user;
  protected boolean isRunning = true;

  public ServerConnection() {
  }

  public ServerConnection(Socket clientSocket) throws IOException {
    this.clientSocket = clientSocket;
    is = new DataInputStream(this.clientSocket.getInputStream());
    os = new DataOutputStream(this.clientSocket.getOutputStream());
    writerThread = new WriterThread(os, this);
    readerThread = new ReaderThread(is, this, this);
  }

  public void onRecieveData(DataPackage dataPackage) {
    if (listener != null) {
      listener.onRecieveData(dataPackage);
    }
    RequestManager.instance().addRequest(new SocketServerHandle(this, dataPackage));
  }

  public void onConnectDone() {
  }

  public void onConnectFail() {
  }

  public void onDisconnect() {
    if (listener != null) {
      listener.onDisconnect();
    }
    AuthenticateBussiness.onDisconnect(this);
  }

  public void setListener(DataReceiveListener listener) {
    this.listener = listener;
  }

  public void write(DataPackage dataPackage) {
    if (writerThread != null) {
      writerThread.write(dataPackage);
    } else {
      log.info(">>>>>>ERROR: writerThread is NULL");
    }
  }

  public void detroy() {
    isRunning = false;
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
