package state;

import java.util.Hashtable;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;

public class SystemCanvas extends Canvas {
	private static final int DEFAULT_SLEEP_TIME = 100;

	public GameForm frmCurrent;
	public GameForm lastState;
	
	private MIDlet mid;
	protected Transformer transformer;
	private KeyRepeatHandle keyRepeatHandle;
	private KeyManagement keyManagement;
	private GameTimer timer;
	private Graphics g;
	
	// =========================== Constructor =============================

	public SystemCanvas() {
		setFullScreenMode(true);
		
		new Control() {
			public void perform() {
				init();
			}
		}.start();

		timer = new GameTimer(DEFAULT_SLEEP_TIME);
		timer.start();
		timer.addTarget(new TimerListener() {
			public void doTask() {
				doRepaint();
			}
		});
	}
	
	// =========================== Init =====================================

	public void init() {
		keyRepeatHandle = new KeyRepeatHandle(this);
		keyRepeatHandle.start();
		keyManagement = new KeyManagement();

		GameGlobal.alert = new Alert();
		GameGlobal.alert.setAutoHideInEvent(true);
		transformer = new Transformer();
	}

	public void setTimerDelay(int delay) {
		timer.setDelay(delay);
	}

	public void resetTimerDelay() {
		timer.setDelay(DEFAULT_SLEEP_TIME);
	}

	// =========================== Next Form ================================

	/**
	 * Tiến hành chuyển sang State tiếp theo có sử dụng các hiệu ứng Transform Các hiệu ứng transoform có thể sử dụng nằm trong class {@link Transformer} <br>
	 * 
	 * @param state - State tiếp theo
	 * @param params - Bảng chứa các tham số cần chuyển cho State tiếp theo
	 * @param transformType - Kiểu transform xử dụng
	 */
	public void nextState(GameForm state, Hashtable params, int transformType) {
		try {
			if (frmCurrent == GameGlobal.alert) {
				hideAlert();
			}
			resetTimerDelay();
			lastState = frmCurrent;
			transformer.transform(frmCurrent, state, params, transformType);
			System.gc();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tiến hành chuyển sang State tiếp theo mà không xử dụng hiệu ứng transform
	 * 
	 * @param state - State tiếp theo
	 * @param params - Bảng chứa các tham số cần chuyển cho State tiếp theo
	 */
	public void nextState(GameForm state, Hashtable params) {
		try {
			if (keyRepeatHandle != null) {
				keyRepeatHandle.keyReleased();
			}

			if (frmCurrent instanceof Alert) {
				hideAlert();
			}
			resetTimerDelay();
			lastState = frmCurrent;
			if (lastState != null) {
				lastState.isRunning = false;
			}
			state.isRunning = true;
			state.init(params);
			frmCurrent = state;
			if (lastState != null) {
				lastState.detroyAll();
			}
			System.gc();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void changeHandleToAlert() {
		frmCurrent = GameGlobal.alert;
	}

	/**
	 * Ẩn Alert hiện tại và quay trở lại form nền
	 */
	public void hideAlert() {
		if (frmCurrent == GameGlobal.alert) {
			frmCurrent = GameGlobal.alert.getParent();
			if (frmCurrent != null) {
				frmCurrent.removeScreen();
			}
			GameGlobal.alert.destroy();
			GameGlobal.alert.checkToShowQueueAlert();
		}
		System.gc();
	}

	public void beginTransform() {
		frmCurrent = transformer;
	}

	public void endTransform(GameForm handleState, boolean needToDetroyOldState) {
		frmCurrent = handleState;
		frmCurrent.removeScreen();

		if (needToDetroyOldState) {
			GameForm frmTmp = transformer.getOldState();
			if (frmTmp != null) {
				frmTmp.detroyAll();
			}
		}
		transformer.destroy();
		GameGlobal.alert.checkToShowQueueAlert();
		System.gc();
	}
	
	public boolean isInTransforming() {
	  return frmCurrent == transformer;
	}
	
	public boolean isAlertShowing() {
	  return frmCurrent == GameGlobal.alert;
	}

	public KeyRepeatHandle getKeyRepeatHandle() {
		return keyRepeatHandle;
	}
	
	public KeyManagement getKeyManagement() {
		return keyManagement;
	}

	public GameTimer getTimer() {
		return timer;
	}

	/**
	 * Lấy Graphic của màn hình
	 * 
	 * @return Graphic của màn hình
	 */
	public Graphics getGraphic() {
		return g;
	}

	/**
	 * Lấy midlet của chương trình
	 */
	public MIDlet getMidlet() {
		return mid;
	}
	
	public void setMidlet(MIDlet midlet) {
		mid = midlet;
		Display.getDisplay(mid).setCurrent(this);
	}

	// =========================== Key =======================================

	protected void keyPressed(int keyCode) {
		try {
			keyRepeatHandle.keyPressed(keyCode);
			if (frmCurrent != null) {
				frmCurrent.doKeyPressed(keyCode);
			}
			keyManagement.keyPressed(keyCode);
		} catch (RuntimeException ex) {
			ex.printStackTrace();
		}
	}

	public void keyRepeated(int keyCode) {
		try {
			if (frmCurrent != null) {
				frmCurrent.doKeyRepeated(keyCode);
			}
			keyManagement.keyRepeated(keyCode);
		} catch (RuntimeException ex) {
			ex.printStackTrace();
		}
	}

	protected void keyReleased(int keyCode) {
		try {
			keyRepeatHandle.keyReleased();
			if (frmCurrent != null) {
				frmCurrent.doKeyReleased(keyCode);
			}
			keyManagement.keyReleased(keyCode);
		} catch (RuntimeException ex) {
			ex.printStackTrace();
		}
	}

	protected void paint(Graphics g) {
		if (this.g == null) {
			this.g = g;
		}
		
		if (frmCurrent == null) {
			return;
		}

		try {
			if (frmCurrent != null) {
				frmCurrent.updateFullScreen();
			}
		} catch (RuntimeException ex) {
			ex.printStackTrace();
		}
	}

	public void doRepaint() {
		repaint();
		serviceRepaints();
	}

	public void destroy() {
		ResourceRms.close();
		if (GameGlobal.alert != null) {
			GameGlobal.alert.destroy();
		}
		if (transformer != null) {
			transformer.destroy();
		}
		mid = null;
		if (keyRepeatHandle != null) {
			keyRepeatHandle.cancel();
			keyRepeatHandle = null;
		}
		System.gc();
	}

	protected void showNotify() {
		try {
			if (frmCurrent != null) {
				frmCurrent.updateFullScreen();
			}
		} catch (RuntimeException ex) {
			ex.printStackTrace();
		}
	}
}
