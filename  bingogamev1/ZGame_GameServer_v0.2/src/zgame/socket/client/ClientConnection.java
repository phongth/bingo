package zgame.socket.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.DataReceiveListener;
import zgame.socket.ProtocolConstants;
import zgame.utils.Control;

public class ClientConnection extends Control implements DataReceiveListener {
  private static final Logger log = Logger.getLogger(ClientConnection.class);

  private Socket sock;
  private DataInputStream is;
  private DataOutputStream os;

  private WriterThread writerThread;
  private ReaderThread readerThread;
  private DataReceiveListener listener;

  private String serverUrl;
  private int port;
  private boolean isConnectSuccess;
  private boolean isRunning = true;

  public ClientConnection(String serverUrl, int port, DataReceiveListener listener) {
    this.listener = listener;
    this.serverUrl = serverUrl;
    this.port = port;
    start();
  }

  public void perform() {
    connect();

    if (!isConnectSuccess) {
      return;
    }

    // Định kỳ tiến hành cập nhật thông tin về GameService cho DefaultService
    while (isRunning) {
      long tmp = writerThread.getLastTimeSend() + Global.DATA_UPDATE_SEQUENCE_TIME - System.currentTimeMillis();
      if (tmp <= 0) {
        DataPackage updateInfoDataPackage = new DataPackage(ProtocolConstants.RequestHeader.UPDATE_GAME_SERVER_INFO_REQUEST);
        updateInfoDataPackage.putInt(Global.serverMap.size());
        writerThread.write(updateInfoDataPackage);
        try {
          sleep(Global.DATA_UPDATE_SEQUENCE_TIME);
        } catch (InterruptedException e) {
        }
      } else {
        try {
          sleep(tmp);
        } catch (InterruptedException e) {
        }
      }
    }
    detroy();
  }

  private void connect() {
    // Create a socket with a timeout
    try {
      sock = new Socket(serverUrl, port);
      is = new DataInputStream(sock.getInputStream());
      os = new DataOutputStream(sock.getOutputStream());
      writerThread = new WriterThread(os, this);
      readerThread = new ReaderThread(is, this, this);
      isConnectSuccess = true;

      if (listener != null) {
        listener.onConnectDone();
      }
    } catch (Exception e) {
      if (listener != null) {
        listener.onConnectFail();
      }
      log.error(">>>>>>ERROR: can not connect to DEFAULT SERVICE: IOException", e);
      isConnectSuccess = false;
    }
  }

  public void onRecieveData(DataPackage dataPackage) {
    if (listener != null) {
      listener.onRecieveData(dataPackage);
    }
  }

  public void onConnectDone() {
    if (listener != null) {
      listener.onConnectDone();
    }
  }

  public void onConnectFail() {
    if (listener != null) {
      listener.onConnectFail();
    }
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
      log.warn(">>>>>>ERROR: writerThread is NULL");
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
      if (sock != null) {
        sock.close();
        sock = null;
      }
    } catch (IOException e) {
    }
    listener = null;
    serverUrl = null;
  }
}
