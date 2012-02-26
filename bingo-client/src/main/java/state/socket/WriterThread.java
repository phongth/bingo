package state.socket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import state.Control;

public class WriterThread extends Control {
	private DataOutputStream os;
	private Vector dataStack = new Vector();
	private boolean isRunning = true;
	private long lastTimeSend;
	private ClientConnection client;

	public WriterThread(DataOutputStream os, ClientConnection client) {
		this.os = os;
		this.client = client;
		start();
		lastTimeSend = System.currentTimeMillis();
	}

	public void perform() {
		while (isRunning) {
			while (dataStack.size() > 0) {
				DataPackage dataPackage = (DataPackage) dataStack.elementAt(0);
				dataStack.removeElementAt(0);
				try {
					byte[] data = dataPackage.getAllData();
					// System.out.println("Send data len: " + data.length);
					os.writeInt(data.length);
					os.write(data);
					os.flush();
					lastTimeSend = System.currentTimeMillis();
				} catch (IOException e) {
					System.out
							.println("ERROR: WriterThread : IOException when try to write data which has header: "
									+ dataPackage.getHeader());
					client.detroy();
					return;
				} catch (Throwable ex) {
					System.out
							.println("ERROR: WriterThread : faltal exception:"
									+ ex.getClass());
					ex.printStackTrace();
				}
			}
			try {
				sleep(20);
			} catch (InterruptedException e) {
			}
		}
	}

	public long getLastTimeSend() {
		return lastTimeSend;
	}

	public void write(DataPackage dataPackage) {
		dataStack.addElement(dataPackage);
	}

	public void detroy() {
		isRunning = false;
		dataStack = null;
		os = null;
	}
}
