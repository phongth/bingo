package zgame.bean;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo {
	private String username;
	private String playerName;
	private int money;
	private int moneyFinal;
	private boolean canPlay;
	private boolean isBoss;
	private List<Integer> cards = new ArrayList<Integer>();
	private int order;
	private boolean isBoVong;

	public String getUserName() {
		return username;
	}

	public void setUserName(String username) {
		this.username = username;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public boolean getCanPlay() {
		return canPlay;
	}

	public void setCanPlay(boolean canPlay) {
		this.canPlay = canPlay;
	}

	public boolean isBoss() {
		return isBoss;
	}

	public void setBoss(boolean isBoss) {
		this.isBoss = isBoss;
	}

	public void setCards(List<Integer> cards) {
		this.cards = cards;
	}

	public List<Integer> getCards() {
		return cards;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public void setBoVong(boolean isBoVong) {
		this.isBoVong = isBoVong;
	}

	public boolean isBoVong() {
		return isBoVong;
	}

	/**
	 * @return the moneyFinal
	 */
	public int getMoneyFinal() {
		return moneyFinal;
	}

	/**
	 * @param moneyFinal the moneyFinal to set
	 */
	public void setMoneyFinal(int moneyFinal) {
		this.moneyFinal = moneyFinal;
	}
}
