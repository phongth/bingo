package state.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import state.Control;

public class ClientConnection extends Control implements DataReceiveListener {
	private SocketConnection sc;
	private DataInputStream is;
	private DataOutputStream os;

	private WriterThread writerThread;
	private ReaderThread readerThread;
	private DataReceiveListener listener;

	private String serverUrl;
	private int port;
	private boolean isConnectSuccess;

	public boolean isRunning = true;
	private int heartBreathSequenceTime;

	public ClientConnection(String serverUrl, int port, int heartBreathSequenceTime) {
		this(serverUrl, port, null, heartBreathSequenceTime);
	}

	public ClientConnection(String serverUrl, int port, DataReceiveListener listener,
			int heartBreathSequenceTime) {
		this.heartBreathSequenceTime = heartBreathSequenceTime;
		this.serverUrl = serverUrl;
		this.port = port;
		this.listener = listener;
		start();
	}

	public void perform() {
		connect();

		if (!isConnectSuccess) {
			return;
		}

		// Tạo heart Breath Signal
		DataPackage heartBreathDataPackage = new DataPackage(
				DefaultProtocolConstants.RequestHeader.HEART_BREATH_REQUEST);

		// Định kỳ tiến hành gửi HEART_BREATH_SIGNAL
		while (isRunning) {
			long tmp = writerThread.getLastTimeSend() + heartBreathSequenceTime
					- System.currentTimeMillis();
			if (tmp <= 0) {
				writerThread.write(heartBreathDataPackage);
				try {
					sleep(heartBreathSequenceTime);
				} catch (InterruptedException e) {
				}
			} else {
				try {
					sleep(tmp);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void connect() {
		try {
			sc = (SocketConnection) Connector.open("socket://" + serverUrl
					+ ":" + port);
			is = new DataInputStream(sc.openInputStream());
			os = new DataOutputStream(sc.openOutputStream());
			writerThread = new WriterThread(os, this);
			readerThread = new ReaderThread(is, this, this);
			isConnectSuccess = true;

			if (listener != null) {
				listener.onConnectDone();
			}
		} catch (IOException e) {
			if (listener != null) {
				listener.onConnectFail();
			}
			System.out.println(">>>>>>ERROR: can not connect to server");
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
			System.out.println(">>>>>>ERROR: writerThread is NULL");
		}
	}

	public int getHeartBreathSequenceTime() {
		return heartBreathSequenceTime;
	}

	public void setHeartBreathSequenceTime(int heartBreathSequenceTime) {
		this.heartBreathSequenceTime = heartBreathSequenceTime;
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
			if (sc != null) {
				sc.close();
				sc = null;
			}
		} catch (IOException e) {
		}

		listener = null;
		serverUrl = null;
	}
}
