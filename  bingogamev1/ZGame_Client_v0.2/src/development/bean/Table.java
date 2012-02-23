package development.bean;

import java.util.Hashtable;

public class Table extends Entity {
	private Game game;
	private Room room;

	private int bid = 0;
	private boolean isLocked;
	private boolean isPlaying;
	private String tableMasterName = "";

	private Hashtable viewUsers = new Hashtable();

	public Table(String id) {
		super(id);
	}

	public void addViewUser(User user) {
		viewUsers.put(user.getName(), user);
	}

	public void getViewUser(String username) {
		viewUsers.get(username);
	}

	public void removeViewUser(String username) {
		viewUsers.remove(username);
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
		this.parent = room;
		if (room.getGame() != null) {
			this.game = room.getGame();
		}
	}

	public int getBid() {
		return bid;
	}

	public void setBid(int bid) {
		this.bid = bid;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public String getTableMasterName() {
		return tableMasterName;
	}

	public void setTableMasterName(String tableMasterName) {
		this.tableMasterName = tableMasterName;
	}
}
