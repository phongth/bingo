package state;

public class GameTimer extends Control {
	private static final int POOL_SIZE = 50;

	private int delay;
	private boolean isRunning;
	private TimerListener[] timerListeners = new TimerListener[POOL_SIZE];
	private boolean isPause = false;

	public GameTimer(int delay) {
		this.delay = delay;
		isRunning = true;
	}

	public void addTarget(TimerListener timerListener) {
		// Check if this listener exist
		for (int i = 0; i < timerListeners.length; i++) {
			if (timerListeners[i] == timerListener) {
				return;
			}
		}

		// Put this listener to list
		boolean isNotEnoughSpace = true;
		for (int i = 0; i < timerListeners.length; i++) {
			if (timerListeners[i] == null) {
				timerListeners[i] = timerListener;
				isNotEnoughSpace = false;
				break;
			}
		}

		if (isNotEnoughSpace) {
			throw new IllegalArgumentException(
					"GameTimer : addTarget : Not enoungh space in game timer");
		}
	}

	public void removeTarget(TimerListener timerListener) {
		for (int i = 0; i < timerListeners.length; i++) {
			if (timerListeners[i] == timerListener) {
				timerListeners[i] = null;
				break;
			}
		}
	}

	public void perform() {
		long lastTime = 0;
		long spendTime = 0;
		while (isRunning) {
			if (isPause) {
				continue;
			}
			lastTime = System.currentTimeMillis();

			for (int i = 0; i < timerListeners.length; i++) {
				try {
					if (timerListeners[i] != null) {
						timerListeners[i].doTask();
					}
				} catch (RuntimeException ex) {
					ex.printStackTrace();
				}
			}

			spendTime = System.currentTimeMillis() - lastTime;
			try {
				if (delay - spendTime > 0) {
					sleep(delay - spendTime);
				}
			} catch (InterruptedException e) {
			}
		}
	}

	public void pause() {
		isPause = true;
	}

	public void resumeLast() {
		isPause = false;
	}

	public void cancel() {
		isRunning = false;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getDelay() {
		return delay;
	}
}
