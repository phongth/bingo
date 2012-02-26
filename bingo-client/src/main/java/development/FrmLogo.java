package development;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import state.GameConstants;
import state.GameForm;
import state.TimerListener;
import state.font.FontManager;
import state.font.ImageText;
import state.util.Color;
import development.socket.SocketClientUtil;

public class FrmLogo extends GameForm implements TimerListener {
	private Image loadingImage1;
	private Image loadingImage2;
	private Image logoImage;

	private ImageText vnText;

	public String message;
	public String message1;

	private int count;

	public void init(Hashtable hashtable) {
		SocketClientUtil
				.connectToServerForAuthenticate(Global.authenticateHandle);
		try {
			loadingImage1 = Image.createImage(GameConstants.CORE_IMAGE_FOLDER
					+ "/loading_1.png");
			loadingImage2 = Image.createImage(GameConstants.CORE_IMAGE_FOLDER
					+ "/loading_2.png");
			logoImage = Image.createImage(GameConstants.CORE_IMAGE_FOLDER
					+ "/logoMobiplay.png");
		} catch (IOException e) {
		}
		vnText = FontManager.getFont(FontManager.FONT_SIZE_8);

		setTimerListener(this);
		message = "Đang kết nối, xin vui lòng chờ";
		message1 = "";
	}

	public void draw(Graphics g) {
		g.setColor(0x4F0202);
		g.fillRect(0, 0, GameConstants.SCREEN_WIDTH,
				GameConstants.SCREEN_HEIGHT);

		int index = count % 11;
		int x = 0;
		int y = 0;
		if (GameConstants.IS_240x320_SCREEN) {
			g.drawImage(logoImage, GameConstants.SCREEN_WIDTH / 2, 76,
					GameConstants.TOP_HCENTER_ANCHOR);
			vnText.drawString(g, message, Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH / 2, 210,
					GameConstants.TOP_HCENTER_ANCHOR);
			vnText.drawString(g, message1, Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH / 2, 264,
					GameConstants.TOP_HCENTER_ANCHOR);
			x = 62;
			y = 247;
		} else {
			g.drawImage(logoImage, GameConstants.SCREEN_WIDTH / 2, 12,
					GameConstants.TOP_HCENTER_ANCHOR);
			vnText.drawString(g, message, Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH / 2, 140,
					GameConstants.TOP_HCENTER_ANCHOR);
			vnText.drawString(g, message1, Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH / 2, 193,
					GameConstants.TOP_HCENTER_ANCHOR);
			x = 101;
			y = 172;
		}

		for (int i = 1; i < 11; i++) {
			if (i <= index) {
				g.drawImage(loadingImage1, x + (loadingImage1.getWidth() + 1)
						* (i - 1), y, GameConstants.TOP_LEFT_ANCHOR);
			} else {
				g.drawImage(loadingImage2, x + (loadingImage1.getWidth() + 1)
						* (i - 1), y, GameConstants.TOP_LEFT_ANCHOR);
			}
		}
	}

	public void doTask() {
		count++;
	}

	protected void destroy() {
		loadingImage1 = null;
		loadingImage2 = null;
		logoImage = null;

		vnText = null;
	}
}
