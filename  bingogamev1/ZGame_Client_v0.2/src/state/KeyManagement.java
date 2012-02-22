package state;

public class KeyManagement implements KeyListener {
	private static final int POOL_SIZE = 30;
	private KeyListener[] keyListeners = new KeyListener[POOL_SIZE];

	public void addTarget(KeyListener keyListener) {
		// Check if this listener exist
		for (int i = 0; i < keyListeners.length; i++) {
			if (keyListeners[i] == keyListener) {
				return;
			}
		}

		// Put this listener to list
		boolean isNotEnoughSpace = true;
		for (int i = 0; i < keyListeners.length; i++) {
			if (keyListeners[i] == null) {
				keyListeners[i] = keyListener;
				isNotEnoughSpace = false;
				break;
			}
		}
		
		if (isNotEnoughSpace) {
			throw new IllegalArgumentException("KeyManagement : addTarget : Not enoungh space in game timer");
		}
	}

	public void removeTarget(KeyListener keyListener) {
		for (int i = 0; i < keyListeners.length; i++) {
			if (keyListeners[i] == keyListener) {
				keyListeners[i] = null;
				break;
			}
		}
	}

	public void keyPressed(int keyCode) {
		for (int i = 0; i < keyListeners.length; i++) {
			if (keyListeners[i] != null) {
				keyListeners[i].keyPressed(keyCode);
			}
		}
	}

	public void keyReleased(int keyCode) {
		for (int i = 0; i < keyListeners.length; i++) {
			if (keyListeners[i] != null) {
				keyListeners[i].keyReleased(keyCode);
			}
		}
	}

	public void keyRepeated(int keyCode) {
		for (int i = 0; i < keyListeners.length; i++) {
			if (keyListeners[i] != null) {
				keyListeners[i].keyRepeated(keyCode);
			}
		}
	}
}
