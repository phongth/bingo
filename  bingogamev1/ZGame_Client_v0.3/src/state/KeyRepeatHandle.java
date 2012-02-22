package state;


public class KeyRepeatHandle extends Control {
	private long startTime;
	private boolean isKeyActive = false;
	private int currentKeyCode;
	private boolean isRunning = true;
	private int delay;
	
	private SystemCanvas systemCanvas;
	
	public KeyRepeatHandle(SystemCanvas systemCanvas) {
		this.systemCanvas = systemCanvas;
		delay = 30;
	}
	
	public void perform() {
		long lastTime = 0;
		long spendTime = 0;
		while(isRunning) {
			lastTime = System.currentTimeMillis();
			if (isKeyActive) {
				systemCanvas.keyRepeated(currentKeyCode);
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
	
	public void keyPressed(int keyCode) {
		currentKeyCode = keyCode;
		startTime = System.currentTimeMillis();
		isKeyActive = true;
	}
		
	public void keyReleased() {
		isKeyActive = false;
	}
	
	public long getKeyRepeatStartTime() {
		return startTime;
	}

	public void cancel() {
		isRunning = false;
	}

	public int getDelay() {
		return delay;
	}

	public void serDelay(int sleepTime) {
		this.delay = sleepTime;
	}
	
	public void resetDelay() {
		delay = 30;
	}
}
