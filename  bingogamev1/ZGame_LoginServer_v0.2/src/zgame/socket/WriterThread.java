package zgame.socket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import zgame.utils.Control;

public class WriterThread extends Control {
  private static Logger log = Logger.getLogger(WriterThread.class);
	private DataOutputStream os;
	private Vector<DataPackage> dataStack = new Vector<DataPackage>();
	private boolean isRunning = true;
	private long lastTimeSend;
	private Server server;
	
	public WriterThread(DataOutputStream os, Server server) {
		this.server = server;
		this.os = os;
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
					os.writeInt(data.length);
					os.write(data);
					os.flush();
					lastTimeSend = System.currentTimeMillis();
				} catch (IOException e) {
				  log.warn("Error when try to write data which has header: " + dataPackage.getHeader());
					server.detroy();
					return;
				} catch (Throwable ex) {
				  log.warn("Faltal exception: ", ex);
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
