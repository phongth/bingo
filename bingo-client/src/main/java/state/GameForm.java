package state;

import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class GameForm implements AlertListener, KeyListener {
	protected Manager manager;
	private Image screen;
	private TimerListener timerListener;
	public boolean isRunning = false;
	public GameForm backForm;

	public GameForm() {
		manager = new Manager(this);
	}

	public final Image getScreen() {
		return screen;
	}

	public final void setScreen(Image screen) {
		this.screen = screen;
	}

	public final void setTimerListener(TimerListener timerListener) {
		if (this.timerListener != null) {
			GameGlobal.systemCanvas.getTimer().removeTarget(this.timerListener);
			this.timerListener = null;
		}
		GameGlobal.systemCanvas.getTimer().addTarget(timerListener);
		this.timerListener = timerListener;
	}

	public final void removeTimerListener() {
		if (timerListener != null) {
			GameGlobal.systemCanvas.getTimer().removeTarget(timerListener);
			timerListener = null;
		}
	}

	public final void removeScreen() {
		screen = null;
	}

	public void init(Hashtable parameters) {
	}

	public void alertEventPerform(int alertType, int eventType, int alertId) {
	}

	public void draw(Graphics g) {
	}

	public void keyPressed(int keyCode) {
	}

	public void keyReleased(int keyCode) {
	}

	public void keyRepeated(int keyCode) {
	}

	protected void destroy() {
	}

	public void mousePressed(int x, int y) {
	}

	public void mouseReleased(int x, int y) {
	}

	public void mouseMoved(int x, int y) {
	}

	public final void doKeyPressed(int keyCode) {
		manager.doKeyPressed(keyCode);
		keyPressed(keyCode);
	}

	public final void doKeyReleased(int keyCode) {
		manager.doKeyReleased(keyCode);
		keyReleased(keyCode);
	}

	public final void doKeyRepeated(int keyCode) {
		manager.doKeyRepeated(keyCode);
		keyRepeated(keyCode);
	}

	public final void doMousePressed(int x, int y) {
		manager.doMousePressed(x, y);
		mousePressed(x, y);
	}

	public final void doMouseReleased(int x, int y) {
		manager.doMouseReleased(x, y);
		mouseReleased(x, y);
	}

	public final void doMouseMoved(int x, int y) {
		manager.doMouseMoved(x, y);
		mouseMoved(x, y);
	}

	public final void updateFullScreen() {
		manager.updateFullScreen();
	}

	public final void goBack(Hashtable params, int transformType) {
		if (backForm == null) {
			System.err.println("Can not go back: backForm is NULL");
			return;
		}
		GameGlobal.nextState(backForm, params, transformType);
	}

	public final void detroyAll() {
		removeScreen();
		isRunning = false;
		removeTimerListener();
		destroy();
		manager.detroyAll();
	}
}
