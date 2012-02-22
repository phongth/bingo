package zgame.socket.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import zgame.socket.DataPackage;
import zgame.utils.Control;

public class WriterThread extends Control {
  private static final Logger log = Logger.getLogger(WriterThread.class);
  
	private DataOutputStream os;
	private Vector<DataPackage> dataStack = new Vector<DataPackage>();
	private boolean isRunning = true;
	private long lastTimeSend;
	private Client client;
	
	public WriterThread(DataOutputStream os, Client client) {
		this.os = os;
		this.client = client;
		start();
		lastTimeSend = System.currentTimeMillis();
	}
	
	public void perform() {
		while(isRunning) {
			while(dataStack.size() > 0) {
				DataPackage dataPackage = dataStack.elementAt(0);
				dataStack.removeElementAt(0);
				try {
					byte[] data = dataPackage.getAllData();
//				log.info("Send data len: " + data.length);
					os.writeInt(data.length);
					os.write(data);
					os.flush();
					lastTimeSend = System.currentTimeMillis();
				} catch (IOException e) {
					log.warn("ERROR: WriterThread: IOException when try to write data which has header: " + dataPackage.getHeader());
					client.detroy();
					return;
				} catch (Throwable ex) {
					log.warn("ERROR: WriterThread : faltal exception:", ex);
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
