package state;

public abstract class Control extends Thread {
	private GameForm parent;

	public void setParent(GameForm parent) {
		this.parent = parent;
	}

	public boolean isContinute() {
		return parent.isRunning;
	}

	public void run() {
		try {
			perform();
		} catch (RuntimeException ex) {
			ex.printStackTrace();
		}
	}

	public abstract void perform();
}
