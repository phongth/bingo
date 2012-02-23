package development;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import state.Alert;
import state.GameConstants;
import state.GameForm;
import state.GameGlobal;
import state.Key;
import state.TimerListener;
import state.Transformer;
import state.component.Event;
import state.component.EventListener;
import state.component.Menu;
import state.component.TextField;
import state.font.FontManager;
import state.font.ImageText;
import state.md5.MD5;
import state.util.Color;
import state.util.ImageUtil;
import development.bean.User;
import development.socket.SocketClientUtil;

public class FrmLogin extends GameForm implements TimerListener, EventListener {
	public static final int TIME_OUT = 300; // 30s

	private Image checkImage;
	private Image uncheckImage;
	private Image logoImage;
	private ImageText text8;

	private int chooseIndex;
	private Menu menu;
	private String[] MENU;

	private TextField userNameTextField;
	private TextField passwordTextField;

	public void init(Hashtable parameters) {
		checkImage = ImageUtil.getImage("Tickbox_1.png");
		uncheckImage = ImageUtil.getImage("Tickbox_2.png");

		if (GameConstants.IS_240x320_SCREEN) {
			try {
				logoImage = Image.createImage(GameConstants.CORE_IMAGE_FOLDER
						+ "/logoMobiplay.png");
			} catch (IOException e) {
			}
		} else {
			logoImage = ImageUtil.getImage("logo_dangnhap.png");
		}

		text8 = FontManager.getFont(FontManager.FONT_SIZE_8);

		userNameTextField = new TextField(TextField.CONSTRAINT_USER_NAME,
				Constants.TEXT_FIELD_STYLE);
		passwordTextField = new TextField(TextField.CONSTRAINT_PASSWORD,
				Constants.TEXT_FIELD_STYLE);

		if (GameConstants.IS_240x320_SCREEN) {
			userNameTextField.setPosition(70, 155);
			userNameTextField.setSize(150, 20);
			passwordTextField.setPosition(70, 185);
			passwordTextField.setSize(150, 20);
		} else {
			userNameTextField.setPosition(128, 124);
			userNameTextField.setSize(150, 20);
			passwordTextField.setPosition(128, 153);
			passwordTextField.setSize(150, 20);
		}

		chooseIndex = 0;// up first
		updateFocus();

		if (Global.loginInfo.isSaveUserNameAndPassword()
				|| Global.loginInfo.isAutoLogin()) {
			Global.currentUser = new User(Global.loginInfo.getUserName());
			Global.currentUser.setPasswordMd5(MD5.toBase64(Global.loginInfo
					.getPassword().getBytes()));
			userNameTextField.setText(Global.currentUser.getName());
			passwordTextField.setText(Global.loginInfo.getPassword());

			// Nếu không phải là tự động đăng nhập thì xác nhận user ngầm để
			// tăng performent
			if (!Global.loginInfo.isAutoLogin()
					&& !Global.loginInfo.getUserName().equals(
							Global.validedUsername)) {
				SocketClientUtil.privateLogin();
			}
		}

		MENU = new String[] { "Đký tài khoản", "Gọi điện hỗ trợ", "Trợ giúp",
				"Thoát" };
		menu = new Menu(MENU, this, Constants.MENU_STYLE, 0,
				GameConstants.SCREEN_HEIGHT - 45);
		menu.getSubMenu(2).addItems(Constants.GAME_NAME);

		setTimerListener(this);
	}

	public void draw(Graphics g) {
		g.setColor(0x440000);
		g.fillRect(0, 0, GameConstants.SCREEN_WIDTH,
				GameConstants.SCREEN_HEIGHT);
		g.setColor(0x2B0102);// Màu nền task bar
		g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25,
				GameConstants.SCREEN_WIDTH, 25);

		// Draw command bar
		if (menu.isShowing()) {
			text8.drawString(g, "Đóng", Color.WHITE_CODE, 5,
					GameConstants.SCREEN_HEIGHT - 5,
					GameConstants.BOTTOM_LEFT_ANCHOR);
		} else {
			text8.drawString(g, "Menu", Color.WHITE_CODE, 5,
					GameConstants.SCREEN_HEIGHT - 5,
					GameConstants.BOTTOM_LEFT_ANCHOR);
			text8.drawString(g, "Xóa", Color.WHITE_CODE,
					GameConstants.SCREEN_WIDTH - 5,
					GameConstants.SCREEN_HEIGHT - 5,
					GameConstants.BOTTOM_RIGHT_ANCHOR);
			if (chooseIndex < 2) {
				text8.drawString(g, "Đăng nhập", Color.WHITE_CODE,
						GameConstants.SCREEN_WIDTH / 2,
						GameConstants.SCREEN_HEIGHT - 5,
						GameConstants.BOTTOM_HCENTER_ANCHOR);
			}
		}
		text8.drawString(g, "Hotline: 19001530", Color.YELLOW_CODE,
				GameConstants.SCREEN_WIDTH - 5,
				GameConstants.SCREEN_HEIGHT - 25,
				GameConstants.BOTTOM_RIGHT_ANCHOR);

		if (GameConstants.IS_240x320_SCREEN) {
			drawFor240x320(g);
		} else {
			drawFor320x240(g);
		}
		menu.paint(g);
	}

	private void drawFor240x320(Graphics g) {
		g.drawImage(logoImage, GameConstants.SCREEN_WIDTH / 2, 27,
				GameConstants.TOP_HCENTER_ANCHOR);
		text8.drawString(g, "Tài khoản", Color.WHITE_CODE, 15, 160,
				GameConstants.TOP_LEFT_ANCHOR);
		userNameTextField.draw(g);
		text8.drawString(g, "Mật khẩu", Color.WHITE_CODE, 15, 190,
				GameConstants.TOP_LEFT_ANCHOR);
		passwordTextField.draw(g);

		text8.drawString(g, "Tự động đăng nhập", Color.WHITE_CODE, 72, 213,
				GameConstants.TOP_LEFT_ANCHOR);
		text8.drawString(g, "Nhớ mật khẩu", Color.WHITE_CODE, 72, 229,
				GameConstants.TOP_LEFT_ANCHOR);

		if (Global.loginInfo.isAutoLogin()) {
			g.drawImage(checkImage, 54, 215, GameConstants.TOP_LEFT_ANCHOR);
		} else {
			g.drawImage(uncheckImage, 54, 215, GameConstants.TOP_LEFT_ANCHOR);
		}
		if (Global.loginInfo.isSaveUserNameAndPassword()) {
			g.drawImage(checkImage, 54, 231, GameConstants.TOP_LEFT_ANCHOR);
		} else {
			g.drawImage(uncheckImage, 54, 231, GameConstants.TOP_LEFT_ANCHOR);
		}

		if (chooseIndex == 2) {
			g.setColor(Color.WHITE_CODE);
			g.drawRect(47, 213, 139, 15);
		} else if (chooseIndex == 3) {
			g.setColor(Color.WHITE_CODE);
			g.drawRect(47, 229, 139, 15);
		}
	}

	private void drawFor320x240(Graphics g) {
		g.drawImage(logoImage, GameConstants.SCREEN_WIDTH / 2, 20,
				GameConstants.TOP_HCENTER_ANCHOR);
		text8.drawString(g, "Tài khoản", Color.WHITE_CODE, 55, 126,
				GameConstants.TOP_LEFT_ANCHOR);
		userNameTextField.draw(g);
		text8.drawString(g, "Mật khẩu", Color.WHITE_CODE, 55, 155,
				GameConstants.TOP_LEFT_ANCHOR);
		passwordTextField.draw(g);

		text8.drawString(g, "Tự động đăng nhập", Color.WHITE_CODE, 90, 180,
				GameConstants.TOP_LEFT_ANCHOR);
		text8.drawString(g, "Nhớ mật khẩu", Color.WHITE_CODE, 90, 196,
				GameConstants.TOP_LEFT_ANCHOR);

		if (Global.loginInfo.isAutoLogin()) {
			g.drawImage(checkImage, 72, 182, GameConstants.TOP_LEFT_ANCHOR);
		} else {
			g.drawImage(uncheckImage, 72, 182, GameConstants.TOP_LEFT_ANCHOR);
		}
		if (Global.loginInfo.isSaveUserNameAndPassword()) {
			g.drawImage(checkImage, 72, 198, GameConstants.TOP_LEFT_ANCHOR);
		} else {
			g.drawImage(uncheckImage, 72, 198, GameConstants.TOP_LEFT_ANCHOR);
		}

		if (chooseIndex == 2) {
			g.setColor(Color.WHITE_CODE);
			g.drawRect(65, 180, 139, 15);
		} else if (chooseIndex == 3) {
			g.setColor(Color.WHITE_CODE);
			g.drawRect(65, 196, 139, 15);
		}
	}

	public void keyReleased(int keyCode) {
		keyCode = Key.getGameKey(keyCode);

		// Kiểm tra để hiển thị menu
		if (keyCode == Key.SOFT_LEFT) {
			if (menu.isShowing()) {
				menu.hide();
			} else {
				menu.show();
			}
			return;
		}

		// Nếu menu đang hiển thị thì thoát
		if (menu.isShowing()) {
			return;
		}

		switch (keyCode) {
		case Key.FIRE:
		case Key.ENTER:
			if (chooseIndex < 2) {
				Global.loginInfo.setUserName(userNameTextField.getText());
				Global.loginInfo.setPassword(passwordTextField.getText());
				SocketClientUtil.login(true);
			} else if (chooseIndex == 2) {
				Global.loginInfo.setAutoLogin(!Global.loginInfo.isAutoLogin());
			} else if (chooseIndex == 3) {
				Global.loginInfo.setSaveUserNameAndPassword(!Global.loginInfo
						.isSaveUserNameAndPassword());
			}
			break;
		case Key.UP:
			chooseIndex--;
			if (chooseIndex < 0) {
				chooseIndex = 3;
			}
			updateFocus();
			break;
		case Key.DOWN:
			chooseIndex++;
			if (chooseIndex > 3) {
				chooseIndex = 0;
			}
			updateFocus();
			break;
		case Key.LEFT:
			break;
		case Key.RIGHT:
			break;
		case Key.SOFT_RIGHT:
		case Key.BACKSPACE:
			if (chooseIndex == 0) {
				userNameTextField.deleteLastChar();
			} else if (chooseIndex == 1) {
				passwordTextField.deleteLastChar();
			}
			break;
		default:
			if (chooseIndex == 0) {
				userNameTextField.keyReleased(keyCode);
			} else if (chooseIndex == 1) {
				passwordTextField.keyReleased(keyCode);
			}
			break;
		}
	}

	public void updateFocus() {
		if (chooseIndex == 0) {
			userNameTextField.setFocused(true);
			passwordTextField.setFocused(false);
		} else if (chooseIndex == 1) {
			userNameTextField.setFocused(false);
			passwordTextField.setFocused(true);
		} else {
			userNameTextField.setFocused(false);
			passwordTextField.setFocused(false);
		}
	}

	public void onActionPerform(Event event) {
		// "Đký tài khoản", "Gọi điện hỗ trợ", "Trợ giúp", "Thoát"
		String action = event.getAction();
		if (action.equals(MENU[0])) { // Đký tài khoản
			GameGlobal.nextState(Global.frmRegister, null,
					Transformer.TRANSFORM_WITH_LOADING_FORM);
		} else if (action.equals(MENU[1])) { // Gọi điện hỗ trợ
		} else if (action.equals(MENU[2])) { // "Trợ giúp"
		} else if (action.equals(MENU[3])) { // Thoát
			GameGlobal.getMidlet().notifyDestroyed();
		}
	}

	public void alertEventPerform(int alertType, int eventType, int alertId) {
		if ((alertId == 99) && (eventType == Alert.YES_BUTTON)) { // Thoát
			GameGlobal.getMidlet().notifyDestroyed();
		}
	}

	public void doTask() {
	}

	protected void destroy() {
		text8 = null;
		checkImage = null;
		uncheckImage = null;
		logoImage = null;

		MENU = null;
		if (menu != null) {
			menu.detroy();
			menu = null;
		}
	}
}
