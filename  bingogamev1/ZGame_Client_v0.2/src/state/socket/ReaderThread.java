package state.socket;

import java.io.DataInputStream;
import java.io.IOException;

import state.Control;

public class ReaderThread extends Control {
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
//				System.out.println("Read data len: " + len);

				byte[] data = new byte[len];
				is.readFully(data);
				lastTimeReveive = System.currentTimeMillis();
				listener.onRecieveData(new DataPackage(data));
			} catch (IOException e) {
				if (isRunning) {
					listener.onDisconnect();
				}
				System.out.println("WARNING: ReaderThread : connection close on IOException");
				client.detroy();
				return;
			} catch (Throwable ex) {
				System.out.println("ERROR: ReaderThread : faltal exception:" + ex.getClass());
				ex.printStackTrace();
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
