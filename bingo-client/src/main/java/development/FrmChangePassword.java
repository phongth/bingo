package development;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import development.socket.SocketClientUtil;

import state.Alert;
import state.GameConstants;
import state.GameForm;
import state.GameGlobal;
import state.Key;
import state.Transformer;
import state.component.TextField;
import state.font.FontManager;
import state.font.ImageText;
import state.util.Color;
import state.util.ImageUtil;

public class FrmChangePassword extends GameForm {
	public static final int TIME_OUT = 300; // 30s

	private Image logoImage;
	private ImageText text8;

	private int chooseIndex;

	private TextField oldPasswordTextField;
	private TextField newPasswordTextField;
	private TextField newPasswordTextField1;

	public void init(Hashtable parameters) {
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

		oldPasswordTextField = new TextField(TextField.CONSTRAINT_PASSWORD,
				Constants.TEXT_FIELD_STYLE);
		newPasswordTextField = new TextField(TextField.CONSTRAINT_PASSWORD,
				Constants.TEXT_FIELD_STYLE);
		newPasswordTextField1 = new TextField(TextField.CONSTRAINT_PASSWORD,
				Constants.TEXT_FIELD_STYLE);

		if (GameConstants.IS_240x320_SCREEN) {
			oldPasswordTextField.setPosition(70, 155);
			oldPasswordTextField.setSize(150, 20);
			newPasswordTextField.setPosition(70, 185);
			newPasswordTextField.setSize(150, 20);
			newPasswordTextField1.setPosition(70, 225);
			newPasswordTextField1.setSize(150, 20);
		} else {
			oldPasswordTextField.setPosition(128, 124);
			oldPasswordTextField.setSize(150, 20);
			newPasswordTextField.setPosition(128, 153);
			newPasswordTextField.setSize(150, 20);
			newPasswordTextField1.setPosition(128, 192);
			newPasswordTextField1.setSize(150, 20);
		}

		chooseIndex = 0;// up first
		updateFocus();
	}

	private void drawFor240x320(Graphics g) {
		g.drawImage(logoImage, GameConstants.SCREEN_WIDTH / 2, 27,
				GameConstants.TOP_HCENTER_ANCHOR);
		text8.drawString(g, "MK cũ", Color.WHITE_CODE, 15, 160,
				GameConstants.TOP_LEFT_ANCHOR);
		oldPasswordTextField.draw(g);
		text8.drawString(g, "MK mới", Color.WHITE_CODE, 15, 190,
				GameConstants.TOP_LEFT_ANCHOR);
		newPasswordTextField.draw(g);
		text8.drawString(g, "Nhập lại", Color.WHITE_CODE, 15, 220,
				GameConstants.TOP_LEFT_ANCHOR);
		text8.drawString(g, "mật khẩu", Color.WHITE_CODE, 15, 235,
				GameConstants.TOP_LEFT_ANCHOR);
		newPasswordTextField1.draw(g);
	}

	private void drawFor320x240(Graphics g) {
		g.drawImage(logoImage, GameConstants.SCREEN_WIDTH / 2, 20,
				GameConstants.TOP_HCENTER_ANCHOR);
		text8.drawString(g, "Mật khẩu cũ", Color.WHITE_CODE, 55, 126,
				GameConstants.TOP_LEFT_ANCHOR);
		oldPasswordTextField.draw(g);
		text8.drawString(g, "Mật khẩu mới", Color.WHITE_CODE, 55, 155,
				GameConstants.TOP_LEFT_ANCHOR);
		newPasswordTextField.draw(g);
		text8.drawString(g, "Nhập lại", Color.WHITE_CODE, 55, 184,
				GameConstants.TOP_LEFT_ANCHOR);
		text8.drawString(g, "mật khẩu", Color.WHITE_CODE, 55, 199,
				GameConstants.TOP_LEFT_ANCHOR);
		newPasswordTextField1.draw(g);
	}

	public void draw(Graphics g) {
		g.setColor(0x440000);
		g.fillRect(0, 0, GameConstants.SCREEN_WIDTH,
				GameConstants.SCREEN_HEIGHT);
		g.setColor(0x2B0102);// Màu nền task bar
		g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25,
				GameConstants.SCREEN_WIDTH, 25);

		// Draw command bar
		text8.drawString(g, "Trở về", Color.WHITE_CODE, 5,
				GameConstants.SCREEN_HEIGHT - 5,
				GameConstants.BOTTOM_LEFT_ANCHOR);
		text8.drawString(g, "Thay đổi", Color.WHITE_CODE,
				GameConstants.SCREEN_WIDTH / 2,
				GameConstants.SCREEN_HEIGHT - 5,
				GameConstants.BOTTOM_HCENTER_ANCHOR);
		text8.drawString(g, "Xóa", Color.WHITE_CODE,
				GameConstants.SCREEN_WIDTH - 5,
				GameConstants.SCREEN_HEIGHT - 5,
				GameConstants.BOTTOM_RIGHT_ANCHOR);

		if (GameConstants.IS_240x320_SCREEN) {
			drawFor240x320(g);
		} else {
			drawFor320x240(g);
		}
	}

	public void keyReleased(int keyCode) {
		keyCode = Key.getGameKey(keyCode);

		switch (keyCode) {
		case Key.FIRE:
		case Key.ENTER:
			changePassword();
			break;
		case Key.UP:
			chooseIndex--;
			if (chooseIndex < 0) {
				chooseIndex = 2;
			}
			updateFocus();
			break;
		case Key.DOWN:
			chooseIndex++;
			if (chooseIndex > 2) {
				chooseIndex = 0;
			}
			updateFocus();
			break;
		case Key.LEFT:
			break;
		case Key.RIGHT:
			break;
		case Key.SOFT_LEFT:
			GameGlobal.nextState(Global.frmGameService, null,
					Transformer.TRANSFORM_WITH_LOADING_FORM);
			break;
		case Key.SOFT_RIGHT:
		case Key.BACKSPACE:
			if (chooseIndex == 0) {
				oldPasswordTextField.deleteLastChar();
			} else if (chooseIndex == 1) {
				newPasswordTextField.deleteLastChar();
			} else if (chooseIndex == 2) {
				newPasswordTextField1.deleteLastChar();
			}
			break;
		default:
			if (chooseIndex == 0) {
				oldPasswordTextField.keyReleased(keyCode);
			} else if (chooseIndex == 1) {
				newPasswordTextField.keyReleased(keyCode);
			} else if (chooseIndex == 2) {
				newPasswordTextField1.keyReleased(keyCode);
			}
			break;
		}
	}

	private void changePassword() {
		String oldPassword = oldPasswordTextField.getText();
		String newPassword = newPasswordTextField.getText();
		String newPassword1 = newPasswordTextField1.getText();

		if (oldPassword.length() == 0) {
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
					new String[] { "Bạn cần nhập vào mật khẩu cũ" });
			return;
		}

		if (!newPassword.equals(newPassword1)) {
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE,
					new String[] { "Mật khẩu nhập lại không đúng" });
			return;
		}

		if (newPassword.length() < Constants.PASSWORD_MIN_LEN) {
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {
					"Mật khẩu phải",
					"dài hơn " + Constants.PASSWORD_MIN_LEN + " ký tự" });
			return;
		}

		if (newPassword.length() > Constants.PASSWORD_MAX_LEN) {
			GameGlobal.alert.showAlert(this, Alert.OK_TYPE, new String[] {
					"Mật khẩu phải",
					"ngắn hơn " + Constants.PASSWORD_MAX_LEN + " ký tự" });
			return;
		}

		SocketClientUtil.changePassword(Global.currentUser.getName(),
				oldPassword, newPassword);
	}

	public void updateFocus() {
		if (chooseIndex == 0) {
			oldPasswordTextField.setFocused(true);
			newPasswordTextField.setFocused(false);
			newPasswordTextField1.setFocused(false);
		} else if (chooseIndex == 1) {
			oldPasswordTextField.setFocused(false);
			newPasswordTextField.setFocused(true);
			newPasswordTextField1.setFocused(false);
		} else if (chooseIndex == 2) {
			oldPasswordTextField.setFocused(false);
			newPasswordTextField.setFocused(false);
			newPasswordTextField1.setFocused(true);
		}
	}

	protected void destroy() {
		text8 = null;
		logoImage = null;
	}
}
