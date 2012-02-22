package development;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

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
import development.bean.Room;
import development.socket.SocketClientUtil;

public class FrmListRoom extends GameForm implements EventListener {
	private static final int DISTANCE = 21;
	private static final int DX = 10;
	private static final int DY = 48;

	private Image tabImage;
	private Image tabFocusImage;
	private Image muitenImage;
	private Image muitenFocusImage;
	private Image popupImage;

	private ImageText text8;
	private int[] currentRow;
	private int[] rowDy;
	private String[] roomTypeName;
	private int tabIndex;
	
	private Menu menu;
	private String[] MENU;

	public void init(Hashtable parameters) {
		popupImage = ImageUtil.getImage("bg_popup.png");
		tabImage = ImageUtil.getImage("tab.png");
		tabFocusImage = ImageUtil.getImage("tab_focus.png");
		muitenImage = ImageUtil.getImage("muiten.png");
		muitenFocusImage = ImageUtil.getImage("muiten_focus.png");
		roomTypeName = new String[]{"SƠ CẤP", "TRUNG CẤP", "VIP"};
		text8 = FontManager.getFont(FontManager.FONT_SIZE_8);
		tabIndex = 0;
		currentRow = new int[3];
		rowDy = new int[] {DY, DY, DY};
		
		MENU = new String[] {"Cập nhật danh sách", "Về menu chính", "Thông tin TK"};
		menu = new Menu(MENU, this, Constants.MENU_STYLE, 0, GameConstants.SCREEN_HEIGHT - 45);
	}

	public void draw(Graphics g) {
		g.setColor(0x440000);
		g.fillRect(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
		g.setColor(0xA53A06);
		g.fillRect(4, 24, GameConstants.SCREEN_WIDTH - 4, GameConstants.SCREEN_HEIGHT - 25);
		
		Vector rooms = Global.currentGame.getRoomListByType(tabIndex);
		
		// draw tab
		for (int i = 0; i < 3; i++) {
			if (i == tabIndex) {
				g.drawImage(tabFocusImage, 4 + i * (tabImage.getWidth() + 4), 4, GameConstants.TOP_LEFT_ANCHOR);
			}
			g.drawImage(tabImage, 4 + i * (tabImage.getWidth() + 4), 4, GameConstants.TOP_LEFT_ANCHOR);
			text8.drawString(g, roomTypeName[i], Color.WHITE_CODE, 40 + i * (tabImage.getWidth() + 4), 14, GameConstants.CENTER_ANCHOR);
			if (i == tabIndex && rooms.size() > 0) {
				Room room = (Room) rooms.elementAt(0);
				text8.drawString(g, "(" + room.getMinBid() + " - " + room.getMaxBid() + ")", Color.WHITE_CODE, 40 + i * (tabImage.getWidth() + 4), 30, GameConstants.CENTER_ANCHOR);
			}
		}
		
		if (Global.currentGame.numberOfChilds() == 0) {
			g.drawImage(popupImage, GameConstants.SCREEN_WIDTH / 2, GameConstants.SCREEN_HEIGHT / 2, GameConstants.CENTER_ANCHOR);
			text8.drawString(g, "Đang cập nhật danh sách phòng", Color.WHITE_CODE, GameConstants.SCREEN_WIDTH / 2, GameConstants.SCREEN_HEIGHT / 2, GameConstants.CENTER_ANCHOR);
		} else {
			// Set clip for draw room list
			if (GameConstants.IS_240x320_SCREEN) {
				g.setClip(10, 30, 221, 267);
			} else {
				g.setClip(8, 30, 305, 186);
			}
			
			// Draw room list
			for (int i = 0; i < rooms.size(); i++) {
				g.setColor(Color.WHITE_CODE);
				g.drawLine(DX, rowDy[tabIndex] - DISTANCE / 2 + i * DISTANCE, GameConstants.SCREEN_WIDTH - DX, rowDy[tabIndex] - DISTANCE / 2 + i * DISTANCE);
				
				Room room = (Room) rooms.elementAt(i);
				String roomName = "Phòng " + room.getName() + " (" + room.getConcurrentUser() + "/" + room.getMaxUser() + ")";
				int color = Color.WHITE_CODE;
				if (i == currentRow[tabIndex]) {
					g.setColor(0x662221);
					g.fillRect(DX, rowDy[tabIndex] - DISTANCE / 2  + i * DISTANCE + 2, GameConstants.SCREEN_WIDTH - 2 * DX, DISTANCE - 3);
					color = Color.YELLOW_CODE;
					g.drawImage(muitenFocusImage, DX + 4, rowDy[tabIndex]  + i * DISTANCE, GameConstants.VCENTER_LEFT_ANCHOR);
				} else {
					g.drawImage(muitenImage, DX + 4, rowDy[tabIndex] + i * DISTANCE, GameConstants.VCENTER_LEFT_ANCHOR);
				}
				text8.drawString(g, roomName, color, 26, rowDy[tabIndex] + i * DISTANCE, GameConstants.VCENTER_LEFT_ANCHOR);
			}
		}
		
		// Command
		g.setClip(0, GameConstants.SCREEN_HEIGHT - 25, GameConstants.SCREEN_WIDTH, 25);
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
		String action = event.getAction();
		if (action.equals(MENU[0])) { // Cập nhật danh sách
			SocketClientUtil.getRoomList(Global.currentGame.getId());
		} else if (action.equals(MENU[1])) { // Về Menu chính
			GameGlobal.nextState(Global.frmChooseGame, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
		} else if (action.equals(MENU[2])) { // Thông tin TK
		}
	}

	public void alertEventPerform(int alertType, int eventType, int alertId) {
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
		case Key.RIGHT:
			if (Global.currentGame.numberOfChilds() == 0) {
				return;
			}
			tabIndex = (tabIndex + 1) % 3;
			break;
		case Key.LEFT:
			if (Global.currentGame.numberOfChilds() == 0) {
				return;
			}
			tabIndex--;
			if (tabIndex < 0) {
				tabIndex = 2;
			}
		case Key.UP:
		case Key.K_2:
			if (Global.currentGame.getRoomListByType(tabIndex).size() == 0) {
				return;
			}
			if (currentRow[tabIndex] > 0) {
				currentRow[tabIndex]--;
			} else {
				currentRow[tabIndex] = Global.currentGame.getRoomListByType(tabIndex).size() - 1;
			}
			break;
		case Key.DOWN:
		case Key.K_8:
			if (Global.currentGame.getRoomListByType(tabIndex).size() == 0) {
				return;
			}
			if (currentRow[tabIndex] < Global.currentGame.getRoomListByType(tabIndex).size() - 1) {
				currentRow[tabIndex]++;
			} else {
				currentRow[tabIndex] = 0;
			}
			break;
		case Key.FIRE:
		case Key.K_5:
			if (Global.currentGame.getRoomListByType(tabIndex).size() == 0) {
				return;
			}
			Global.currentRoom = (Room) Global.currentGame.getChildByType(tabIndex, currentRow[tabIndex]);
			SocketClientUtil.joinRoomRequest(Global.currentRoom.getId());
			break;
		case Key.SOFT_RIGHT:
		  if (!menu.isShowing()) {
		    GameGlobal.nextState(Global.frmChooseGame, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
		  }
			break;
		}
	}

	protected void destroy() {
		popupImage = null;
		text8 = null;
		currentRow = null;
		rowDy = null;
		roomTypeName = null;
		tabImage = null;
		tabFocusImage = null;
		muitenImage = null;
		muitenFocusImage = null;
		MENU = null;
		if (menu != null) {
			menu.detroy();
			menu = null;
		}
	}
}
