package zgame.socket;

public interface DataReceiveListener {
	void onRecieveData(DataPackage dataPackage);
	void onConnectDone();
	void onConnectFail();
	void onDisconnect();
}
