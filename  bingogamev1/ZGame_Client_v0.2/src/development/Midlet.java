package development;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import state.GameGlobal;

public class Midlet extends MIDlet {
	public Midlet() {
	}

	public void destroyApp() {
		try {
			destroyApp(true);
		} catch (MIDletStateChangeException e) {
		}
	}
	
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		if (GameGlobal.systemCanvas != null) {
			GameGlobal.systemCanvas.destroy();
		}
		super.notifyDestroyed();
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
	  GameGlobal.init();
	  Global.init();
	  GameGlobal.setMidlet(this);
		GameGlobal.nextState(Global.frmLogo, null);
	}
}
