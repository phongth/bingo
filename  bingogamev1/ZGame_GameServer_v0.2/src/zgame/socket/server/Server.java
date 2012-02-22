package zgame.socket.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import zgame.bean.User;
import zgame.socket.DataPackage;
import zgame.socket.DataReceiveListener;

public class Server implements Runnable, DataReceiveListener {
  private static final Logger log = Logger.getLogger(Server.class);
  
	private Socket clientSocket;

	private DataInputStream is;
	private DataOutputStream os;

	private WriterThread writerThread;
	private ReaderThread readerThread;

	private DataReceiveListener listener;
	public User user;
	protected boolean isRunning = true;
	
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
//		while (isRunning) {
//			try {
//				Thread.sleep(Global.HEART_BREATH_SEQUENCE_TIME);
//			} catch (InterruptedException e) {
//			}
//			
//			if ((readerThread == null) || (writerThread == null)) {
//				return;
//			}
//			
//			// Check time out of client
//			if (System.currentTimeMillis() - readerThread.getLastTimeReveive() > Global.TIME_OUT) {
//				writerThread.write(new DataPackage(ProtocolConstants.ResponseHeader.TIME_OUT_NOTIFY_RESPONSE));
//				writerThread.write(new DataPackage(ProtocolConstants.ResponseHeader.CLOSE_CONNECTION_RESPONSE));
//				log.warn("ERROR: Server : Client was closed by TIME OUT");
//				detroy();
//			}
//		}
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
