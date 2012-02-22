package state.util;

import java.io.InputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

import state.GameGlobal;



public class SoundAndVibrateUtil {
	public static boolean isVibrate;
	public static int volume  = 50; // 0 ~ 100;
	public static Player player;
	
	public static void play(String fileName) {
		if (player != null) {
			try {
				player.stop();
			} catch (MediaException e) {
			}
			player = null;
		}
		
		InputStream is = GameGlobal.class.getResourceAsStream(fileName);
		try {
			player = Manager.createPlayer(is, "audio/midi");
			player.prefetch();
			((VolumeControl) player.getControl("VolumeControl")).setLevel(volume);
			player.start();
		} catch (Throwable e) {
			System.out.println("MEDIA ERROR: " + fileName);
			e.printStackTrace();
		}
	}
}
