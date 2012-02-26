package development.bean;

import java.util.Vector;

import development.socket.ProtocolConstants;

import state.socket.DataPackage;
import state.socket.DataReceiveListener;

public class Game extends Entity {
	private Vector[] roomByType = new Vector[3];
	private DataReceiveListener listener;

	public Game(String id, DataReceiveListener listener) {
		super(id);
		for (int i = 0; i < roomByType.length; i++) {
			roomByType[i] = new Vector();
		}
		this.listener = listener;
	}

	public void putChild(Entity entity) {
		super.putChild(entity);
		Room room = (Room) entity;
		roomByType[room.getType()].addElement(room);
	}

	public Vector getRoomListByType(int type) {
		return roomByType[type];
	}

	public Room getChildByType(int type, int index) {
		return (Room) roomByType[type].elementAt(index);
	}

	public void clearChildList() {
		super.clearChildList();
		for (int i = 0; i < roomByType.length; i++) {
			roomByType[i].removeAllElements();
		}
	}

	public void onRecieveData(DataPackage dataPackage) {
		listener.onRecieveData(dataPackage);
	}

	public static final DataPackage createPackage(int header) {
		DataPackage dataPackage = new DataPackage(
				ProtocolConstants.RequestHeader.GAME_ACTION_REQUEST);
		dataPackage.putInt(header);
		return dataPackage;
	}
}
