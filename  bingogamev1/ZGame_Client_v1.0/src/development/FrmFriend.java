package development;

import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import state.Alert;
import state.DrawListener;
import state.GameConstants;
import state.GameForm;
import state.GameGlobal;
import state.Key;
import state.Sprite;
import state.Transformer;
import state.component.Event;
import state.component.EventListener;
import state.component.Menu;
import state.component.TextField;
import state.font.FontManager;
import state.font.ImageText;
import state.util.Color;
import state.util.ImageUtil;
import development.bean.Friend;
import development.socket.SocketClientUtil;

public class FrmFriend extends GameForm implements EventListener {
	private static final int DISTANCE = 21;
	private static final int DX = 10;
	private static final int DY = 48;

	private Image onlineImage;
	private Image offlineImage;

	private ImageText text8;
	private int chooseIndex;
	
	private Menu menu;
	private String[] MENU;
	private TextField addFriendTextField;
	private Sprite addFriendLabel;

	public void init(Hashtable parameters) {
	  SocketClientUtil.requestFriendList();
	  
		onlineImage = ImageUtil.getImage("Trangthai_2.png");
		offlineImage = ImageUtil.getImage("Trangthai_1.png");
		
		text8 = FontManager.getFont(FontManager.FONT_SIZE_8);
		
		MENU = new String[] { "Cập nhật danh sách", "Mời chơi", "Mời chat", "Block", "Xóa", "Kết bạn" };
		menu = new Menu(MENU, this, Constants.MENU_STYLE, 0, GameConstants.SCREEN_HEIGHT - 45);
		
		addFriendTextField = new TextField(TextField.CONSTRAINT_USER_NAME, Constants.TEXT_FIELD_STYLE);
		addFriendTextField.setWidth(150);
		addFriendTextField.setPosition((GameConstants.SCREEN_WIDTH - 150) / 2, GameConstants.SCREEN_HEIGHT / 2);
		addFriendTextField.setClearCharKey(Key.SOFT_RIGHT);
		
		addFriendLabel = new Sprite(0, GameConstants.SCREEN_HEIGHT / 2 - 30, GameConstants.SCREEN_WIDTH, 20).setDrawListener(new DrawListener() {
      public void paint(Sprite source, Graphics g) {
        text8.drawString(g, "Kết bạn với người chơi", Color.WHITE_CODE, GameConstants.SCREEN_WIDTH / 2, source.getY(), GameConstants.TOP_HCENTER_ANCHOR);
      }
    });
	}

	public void draw(Graphics g) {
		g.setColor(0x440000);
		g.fillRect(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
		g.setColor(0xA53A06);
		g.fillRect(4, 24, GameConstants.SCREEN_WIDTH - 4, GameConstants.SCREEN_HEIGHT - 25);
		
		// Draw game service list
		for (int i = 0; i < Global.friends.size(); i++) {
			g.setColor(Color.WHITE_CODE);
			g.drawLine(DX, DY - DISTANCE / 2 + i * DISTANCE, GameConstants.SCREEN_WIDTH - DX, DY - DISTANCE / 2 + i * DISTANCE);
			
			Friend friend = (Friend) Global.friends.elementAt(i);
			int color = Color.WHITE_CODE;
			if (i == chooseIndex) {
				g.setColor(0x662221);
				g.fillRect(DX, DY - DISTANCE / 2  + i * DISTANCE + 2, GameConstants.SCREEN_WIDTH - 2 * DX, DISTANCE - 3);
				color = Color.YELLOW_CODE;
			}
			
			if (friend.isOnline()) {
			  g.drawImage(onlineImage, 15, DY + i * DISTANCE, GameConstants.CENTER_ANCHOR);
			} else {
			  g.drawImage(offlineImage, 15, DY + i * DISTANCE, GameConstants.CENTER_ANCHOR);
			}
			text8.drawString(g, friend.getUsername(), color, 26, DY + i * DISTANCE, GameConstants.VCENTER_LEFT_ANCHOR);
			text8.drawString(g, friend.getLocationInfo(), color, GameConstants.SCREEN_WIDTH / 2 - 30, DY + i * DISTANCE, GameConstants.VCENTER_LEFT_ANCHOR);
		}
		
		// Command
		g.setColor(0x2B0102);// Màu nền task bar
		g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25, GameConstants.SCREEN_WIDTH, 25);
		if (menu.isShowing()) {
			text8.drawString(g, "Đóng", Color.WHITE_CODE, 5, GameConstants.SCREEN_HEIGHT - 5, GameConstants.BOTTOM_LEFT_ANCHOR);
		} else {
			text8.drawString(g, "Menu", Color.WHITE_CODE, 5, GameConstants.SCREEN_HEIGHT - 5, GameConstants.BOTTOM_LEFT_ANCHOR);
			text8.drawString(g, "Trở về", Color.WHITE_CODE, GameConstants.SCREEN_WIDTH - 8, GameConstants.SCREEN_HEIGHT - 4, GameConstants.BOTTOM_RIGHT_ANCHOR);
		}
		
		g.setClip(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
		menu.draw(g);
	}

	public void onActionPerform(Event event) {
		// "Cập nhật danh sách", "Mời chơi", "Mời chat", "Block", "Xóa", "Kết bạn"
		String action = event.getAction();
		if (action.equals(MENU[0])) { // Cập nhật danh sách
			SocketClientUtil.requestFriendList();
		} else if (action.equals(MENU[1])) { // Mời chơi
		} else if (action.equals(MENU[2])) { // Mời chat
		} else if (action.equals(MENU[3])) { // Block
		} else if (action.equals(MENU[4])) { // Xóa
		} else if (action.equals(MENU[5])) { // Kết bạn
		  addFriendTextField.clearText();
		  GameGlobal.alert.showCustomUIAlert(this, new Sprite[] {addFriendLabel, addFriendTextField}, new String[] {"Thêm bạn", "Hủy"}, 
		      Alert.BUTTON_WIDTH_SMALL, 99, Alert.HORIZONTAL_ALINE);
		}
	}

	public void alertEventPerform(int alertType, int eventType, int alertId) {
	  if (alertId == 99) {
	    if (eventType == 0) { // Thêm bạn
	      SocketClientUtil.addFriendRequest(addFriendTextField.getText());
	    } else if (eventType == 1) {
	      System.out.println("Huy button pressed");
	    }
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
				chooseIndex = Global.friends.size() - 1;
			}
			break;
		case Key.DOWN:
		case Key.K_8:
			chooseIndex++;
			if (chooseIndex > Global.friends.size() - 1) {
				chooseIndex = 0;
			}
			break;
		case Key.FIRE:
		case Key.K_5:
			break;
		case Key.SOFT_RIGHT:
		  if (!menu.isShowing()) {
        GameGlobal.nextState(Global.frmChooseGame, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
      }
			break;
		}
	}

	protected void destroy() {
		text8 = null;
		onlineImage = null;
		offlineImage = null;
		MENU = null;
		if (menu != null) {
			menu.detroy();
			menu = null;
		}
	}
}
