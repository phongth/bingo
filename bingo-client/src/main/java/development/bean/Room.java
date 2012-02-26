package development.bean;

public class Room extends Entity {
	public static final int ROOM_BASIC = 0;
	public static final int ROOM_ADVANCE = 1;
	public static final int ROOM_VIP = 2;

	private Game game;
	private int type = ROOM_BASIC;
	private int maxBid;
	private int minBid;

	public Room(String id) {
		super(id);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
		this.parent = game;
	}

	public int getMaxBid() {
		return maxBid;
	}

	public void setMaxBid(int maxBid) {
		this.maxBid = maxBid;
	}

	public int getMinBid() {
		return minBid;
	}

	public void setMinBid(int minBid) {
		this.minBid = minBid;
	}
}
