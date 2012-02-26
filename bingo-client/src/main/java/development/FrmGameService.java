package development;

import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import state.Alert;
import state.GameConstants;
import state.GameForm;
import state.GameGlobal;
import state.Key;
import state.Transformer;
import state.component.Event;
import state.component.EventListener;
import state.component.Menu;
import state.font.FontManager;
import state.font.ImageText;
import state.util.Color;
import state.util.ImageUtil;
import development.bean.GameService;
import development.socket.SocketClientUtil;

public class FrmGameService extends GameForm implements EventListener {
	private static final int DISTANCE = 21;
	private static final int DX = 10;
	private static final int DY = 48;

	private Image muitenImage;
	private Image muitenFocusImage;

	private ImageText text8;
	private int chooseIndex;

	private Menu menu;
	private String[] MENU;

	public void init(Hashtable parameters) {
		muitenImage = ImageUtil.getImage("muiten.png");
		muitenFocusImage = ImageUtil.getImage("muiten_focus.png");
		text8 = FontManager.getFont(FontManager.FONT_SIZE_8);

		MENU = new String[] { "Cập nhật danh sách", "Đổi mật khẩu", "Đăng suất" };
		menu = new Menu(MENU, this, Constants.MENU_STYLE, 0,
				GameConstants.SCREEN_HEIGHT - 45);
	}

	public void draw(Graphics g) {
		g.setColor(0x440000);
		g.fillRect(0, 0, GameConstants.SCREEN_WIDTH,
				GameConstants.SCREEN_HEIGHT);
		g.setColor(0xA53A06);
		g.fillRect(4, 24, GameConstants.SCREEN_WIDTH - 4,
				GameConstants.SCREEN_HEIGHT - 25);

		// Draw game service list
		for (int i = 0; i < Global.gameServices.size(); i++) {
			g.setColor(Color.WHITE_CODE);
			g.drawLine(DX, DY - DISTANCE / 2 + i * DISTANCE,
					GameConstants.SCREEN_WIDTH - DX, DY - DISTANCE / 2 + i
							* DISTANCE);

			GameService gameService = (GameService) Global.gameServices
					.elementAt(i);
			String gameServiceName = gameService.getName() + " ("
					+ gameService.getCocurrentUser() + "/"
					+ gameService.getMaxUser() + ")";
			int color = Color.WHITE_CODE;
			if (i == chooseIndex) {
				g.setColor(0x662221);
				g.fillRect(DX, DY - DISTANCE / 2 + i * DISTANCE + 2,
						GameConstants.SCREEN_WIDTH - 2 * DX, DISTANCE - 3);
				color = Color.YELLOW_CODE;
				g.drawImage(muitenFocusImage, DX + 4, DY + i * DISTANCE,
						GameConstants.VCENTER_LEFT_ANCHOR);
			} else {
				g.drawImage(muitenImage, DX + 4, DY + i * DISTANCE,
						GameConstants.VCENTER_LEFT_ANCHOR);
			}
			text8.drawString(g, gameServiceName, color, 26, DY + i * DISTANCE,
					GameConstants.VCENTER_LEFT_ANCHOR);
		}

		// Command
		g.setColor(0x2B0102);// Màu nền task bar
		g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25,
				GameConstants.SCREEN_WIDTH, 25);
		if (menu.isShowing()) {
			text8.drawString(g, "Đóng", Color.WHITE_CODE, 5,
					GameConstants.SCREEN_HEIGHT - 5,
					GameConstants.BOTTOM_LEFT_ANCHOR);
		} else {
			text8.drawString(g, "Menu", Color.WHITE_CODE, 5,
					GameConstants.SCREEN_HEIGHT - 5,
					GameConstants.BOTTOM_LEFT_ANCHOR);
		}

		g
				.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
		menu.draw(g);
	}

	public void onActionPerform(Event event) {
		// "Cập nhật danh sách", "Đổi mật khẩu", "Đăng suất"
		String action = event.getAction();
		if (action.equals(MENU[0])) { // Cập nhật danh sách
			SocketClientUtil.gameServiceListRequest();
		} else if (action.equals(MENU[1])) { // Đổi mật khẩu
			GameGlobal.nextState(Global.frmChangePassword, null,
					Transformer.TRANSFORM_WITH_LOADING_FORM);
		} else if (action.equals(MENU[2])) { // Đăng suất
			GameGlobal.alert.showAlert(this, Alert.YES_NO_TYPE,
					"Bạn có muốn thoát ra?", 99);
		}
	}

	public void alertEventPerform(int alertType, int eventType, int alertId) {
		if (alertId == 99 && alertType == Alert.YES_NO_TYPE
				&& eventType == Alert.YES_BUTTON) { // Thoát ra
			SocketClientUtil.signOut();
			GameGlobal.nextState(Global.frmLogin, null,
					Transformer.TRANSFORM_WITH_LOADING_FORM);
		}
	}

	public void keyReleased(int keyCode) {
		keyCode = Key.getGameKey(keyCode);

		if (keyCode == Key.SOFT_LEFT) {
			if (menu.isShowing()) {
				menu.hide();
			} else {
				menu.show();
			}
			return;
		}

		if (menu.isShowing()) {
			return;
		}

		switch (keyCode) {
		case Key.UP:
		case Key.K_2:
			chooseIndex--;
			if (chooseIndex < 0) {
				chooseIndex = Global.gameServices.size() - 1;
			}
			break;
		case Key.DOWN:
		case Key.K_8:
			chooseIndex++;
			if (chooseIndex > Global.gameServices.size() - 1) {
				chooseIndex = 0;
			}
			break;
		case Key.FIRE:
		case Key.K_5:
			GameService gameService = (GameService) Global.gameServices
					.elementAt(chooseIndex);
			SocketClientUtil.connectToGameServer(gameService.getUrl(),
					gameService.getPort(), Global.gameActionHandle);
			break;
		case Key.SOFT_RIGHT:
			break;
		}
	}

	protected void destroy() {
		text8 = null;
		muitenImage = null;
		muitenFocusImage = null;
		MENU = null;
		if (menu != null) {
			menu.detroy();
			menu = null;
		}
	}
}
