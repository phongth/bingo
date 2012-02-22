package development;

import java.util.Hashtable;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

import state.AlertListener;
import state.GameConstants;
import state.GameForm;
import state.GameGlobal;
import state.Key;
import state.TimerListener;
import state.Transformer;
import state.component.Event;
import state.component.EventListener;
import state.component.Menu;
import state.font.FontManager;
import state.font.ImageText;
import state.util.Color;
import state.util.ImageUtil;
import development.bean.Game;
import development.socket.GameActionHandle;
import development.socket.SocketClientUtil;

public class FrmChooseGame extends GameForm implements EventListener, TimerListener, AlertListener {
	private Image[][] itemsImages;
	private Image bgtileImage;
	private Image rightWhiteArrowImage;
	private Image leftWhiteArrowImage;

	private ImageText text8;

	private int index = 0;
	private int lastIndex;
	private int firstIndex;

	private int choosedIndex;
	private int textScrolCount;
	private int numberOfEvents;

	private int count;
	private int actionState;
	private boolean isLockLeftRightKey;
	private int currentKey;

	private Menu menu;
	private String[] MENU;

	public void init(Hashtable parameters) {
		choosedIndex = 0;
		count = -1;
		actionState = 0;
		isLockLeftRightKey = false;
		currentKey = -1;
		
		firstIndex = index - 1 < 0 ? (Constants.GAME_ID.length - 1) : index - 1;
		lastIndex = index + 1 > (Constants.GAME_ID.length - 1) ? 0 : index + 1;

		text8 = FontManager.getFont(FontManager.FONT_SIZE_8);

		itemsImages = new Image[4][Constants.GAME_ID.length];
		itemsImages[0][0] = ImageUtil.makeDarker(ImageUtil.getImage("Tala_1.png"), 6);
		itemsImages[0][1] = ImageUtil.makeDarker(ImageUtil.getImage("TienLen_1.png"), 6);
		itemsImages[0][2] = ImageUtil.makeDarker(ImageUtil.getImage("CoTuong_1.png"), 6);
		itemsImages[0][3] = ImageUtil.makeDarker(ImageUtil.getImage("Caro_1.png"), 6);
		itemsImages[0][4] = ImageUtil.makeDarker(ImageUtil.getImage("icon_covua.png"), 6);

		itemsImages[1][0] = ImageUtil.makeDarker(ImageUtil.getImage("Tala_nho1.png"), 3);
		itemsImages[1][1] = ImageUtil.makeDarker(ImageUtil.getImage("tienlen_nho1.png"), 3);
		itemsImages[1][2] = ImageUtil.makeDarker(ImageUtil.getImage("cotuong_nho1.png"), 3);
		itemsImages[1][3] = ImageUtil.makeDarker(ImageUtil.getImage("Caro_nho1.png"), 3);
		itemsImages[1][4] = ImageUtil.makeDarker(ImageUtil.getImage("icon_covua.png"), 3);

		itemsImages[2][0] = ImageUtil.getImage("Tala_nho2.png");
		itemsImages[2][1] = ImageUtil.getImage("tienlen_nho2.png");
		itemsImages[2][2] = ImageUtil.getImage("cotuong_Nho2.png");
		itemsImages[2][3] = ImageUtil.getImage("Caro_nho2.png");
		itemsImages[2][4] = ImageUtil.getImage("icon_covua.png");

		itemsImages[3][0] = ImageUtil.makeBrighter(ImageUtil.getImage("Tala_2.png"), 2);
		itemsImages[3][1] = ImageUtil.makeBrighter(ImageUtil.getImage("TienLen_2.png"), 2);
		itemsImages[3][2] = ImageUtil.makeBrighter(ImageUtil.getImage("CoTuong_2.png"), 2);
		itemsImages[3][3] = ImageUtil.makeBrighter(ImageUtil.getImage("Caro_2.png"), 2);
		itemsImages[3][4] = ImageUtil.makeBrighter(ImageUtil.getImage("icon_covua.png"), 2);

		rightWhiteArrowImage = ImageUtil.getImage("Phai_trai_2.png");

		leftWhiteArrowImage = Image.createImage(rightWhiteArrowImage, 0, 0, rightWhiteArrowImage.getWidth(), rightWhiteArrowImage.getHeight(), Sprite.TRANS_ROT180);

		bgtileImage = ImageUtil.getImage("bg_sukien.png");
		if (GameConstants.IS_240x320_SCREEN) {
			numberOfEvents = 5;
		} else {
			numberOfEvents = 3;
		}

		MENU = new String[]{"Bạn bè", "Nạp điểm", "Tặng Game", "Thông tin TK", "Cài đặt", "Trợ giúp"};
		menu = new Menu(MENU, this, Constants.MENU_STYLE, 0, GameConstants.SCREEN_HEIGHT - 45);

		setTimerListener(this);
		GameGlobal.setTimerDelay(50);
	}

	public void draw(Graphics g) {
		int distance = 70;
		if (!GameConstants.IS_240x320_SCREEN) {
			distance = 77;
		}
		int step = distance / 5;

		int next = 0;
		if (currentKey == Key.LEFT) {
			next = lastIndex + 1 > (Constants.GAME_ID.length - 1) ? 0 : lastIndex + 1;
		} else {
			next = firstIndex - 1 < 0 ? (Constants.GAME_ID.length - 1) : firstIndex - 1;
		}
		
		g.setColor(0x440000);
		g.fillRect(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
		g.setClip(GameConstants.SCREEN_WIDTH / 2 - distance - 30, 0, 2 * distance + 60, GameConstants.SCREEN_HEIGHT);

		switch (actionState) {
		case 0:
			g.drawImage(itemsImages[0][firstIndex], GameConstants.SCREEN_WIDTH / 2 - distance, 70, GameConstants.CENTER_ANCHOR);
			g.drawImage(itemsImages[3][index], GameConstants.SCREEN_WIDTH / 2, 70, GameConstants.CENTER_ANCHOR);
			g.drawImage(itemsImages[0][lastIndex], GameConstants.SCREEN_WIDTH / 2 + distance, 70, GameConstants.CENTER_ANCHOR);
			break;
		case 1:
			if (currentKey == Key.LEFT) {
				g.drawImage(itemsImages[0][firstIndex], GameConstants.SCREEN_WIDTH / 2 - distance - step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[3][index], GameConstants.SCREEN_WIDTH / 2 - step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][lastIndex], GameConstants.SCREEN_WIDTH / 2 + distance - step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][next], GameConstants.SCREEN_WIDTH / 2 + 2 * distance - step, 70, GameConstants.CENTER_ANCHOR);
			} else {
				g.drawImage(itemsImages[0][next], GameConstants.SCREEN_WIDTH / 2 - 2 * distance + step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][firstIndex], GameConstants.SCREEN_WIDTH / 2 - distance + step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[3][index], GameConstants.SCREEN_WIDTH / 2 + step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][lastIndex], GameConstants.SCREEN_WIDTH / 2 + distance + step, 70, GameConstants.CENTER_ANCHOR);
			}
			break;
		case 2:
			if (currentKey == Key.LEFT) {
				g.drawImage(itemsImages[0][firstIndex], GameConstants.SCREEN_WIDTH / 2 - distance - 2 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[2][index], GameConstants.SCREEN_WIDTH / 2 - 2 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[1][lastIndex], GameConstants.SCREEN_WIDTH / 2 + distance - 2 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][next], GameConstants.SCREEN_WIDTH / 2 + 2 * distance - 2 * step, 70, GameConstants.CENTER_ANCHOR);
			} else {
				g.drawImage(itemsImages[0][next], GameConstants.SCREEN_WIDTH / 2 - 2 * distance + 2 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[1][firstIndex], GameConstants.SCREEN_WIDTH / 2 - distance + 2 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[2][index], GameConstants.SCREEN_WIDTH / 2 + 2 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][lastIndex], GameConstants.SCREEN_WIDTH / 2 + distance + 2 * step, 70, GameConstants.CENTER_ANCHOR);
			}
			break;
		case 3:
			if (currentKey == Key.LEFT) {
				g.drawImage(itemsImages[0][firstIndex], GameConstants.SCREEN_WIDTH / 2 - distance - 3 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[2][index], GameConstants.SCREEN_WIDTH / 2 - 3 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[1][lastIndex], GameConstants.SCREEN_WIDTH / 2 + distance - 3 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][next], GameConstants.SCREEN_WIDTH / 2 + 2 * distance - 3 * step, 70, GameConstants.CENTER_ANCHOR);
			} else {
				g.drawImage(itemsImages[0][next], GameConstants.SCREEN_WIDTH / 2 - 2 * distance + 3 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[1][firstIndex], GameConstants.SCREEN_WIDTH / 2 - distance + 3 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[2][index], GameConstants.SCREEN_WIDTH / 2 + 3 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][lastIndex], GameConstants.SCREEN_WIDTH / 2 + distance + 3 * step, 70, GameConstants.CENTER_ANCHOR);
			}
			break;
		case 4:
			if (currentKey == Key.LEFT) {
				g.drawImage(itemsImages[0][firstIndex], GameConstants.SCREEN_WIDTH / 2 - distance - 4 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[1][index], GameConstants.SCREEN_WIDTH / 2 - 4 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[2][lastIndex], GameConstants.SCREEN_WIDTH / 2 + distance - 4 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][next], GameConstants.SCREEN_WIDTH / 2 + 2 * distance - 4 * step, 70, GameConstants.CENTER_ANCHOR);
			} else {
				g.drawImage(itemsImages[0][next], GameConstants.SCREEN_WIDTH / 2 - 2 * distance + 4 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[2][firstIndex], GameConstants.SCREEN_WIDTH / 2 - distance + 4 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[1][index], GameConstants.SCREEN_WIDTH / 2 + 4 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][lastIndex], GameConstants.SCREEN_WIDTH / 2 + distance + 4 * step, 70, GameConstants.CENTER_ANCHOR);
			}
			break;
		case 5:
			if (currentKey == Key.LEFT) {
				g.drawImage(itemsImages[0][firstIndex], GameConstants.SCREEN_WIDTH / 2 - distance - 5 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][index], GameConstants.SCREEN_WIDTH / 2 - 5 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[3][lastIndex], GameConstants.SCREEN_WIDTH / 2 + distance - 5 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][next], GameConstants.SCREEN_WIDTH / 2 + 2 * distance - 5 * step, 70, GameConstants.CENTER_ANCHOR);
			} else {
				g.drawImage(itemsImages[0][next], GameConstants.SCREEN_WIDTH / 2 - 2 * distance + 5 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[3][firstIndex], GameConstants.SCREEN_WIDTH / 2 - distance + 5 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][index], GameConstants.SCREEN_WIDTH / 2 + 5 * step, 70, GameConstants.CENTER_ANCHOR);
				g.drawImage(itemsImages[0][lastIndex], GameConstants.SCREEN_WIDTH / 2 + distance + 5 * step, 70, GameConstants.CENTER_ANCHOR);
			}
			break;
		}

		g.setClip(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
		g.drawImage(leftWhiteArrowImage, 10, 70, GameConstants.VCENTER_LEFT_ANCHOR);
		g.drawImage(rightWhiteArrowImage, GameConstants.SCREEN_WIDTH - 10, 70, GameConstants.VCENTER_LEFT_ANCHOR);

		drawEvent(g);
		
		g.setColor(0x2B0102);// Màu nền task bar
		g.fillRect(0, GameConstants.SCREEN_HEIGHT - 25, GameConstants.SCREEN_WIDTH, 25);
		if (menu.isShowing()) {
			text8.drawString(g, "Đóng", Color.WHITE_CODE, 5, GameConstants.SCREEN_HEIGHT - 5, GameConstants.BOTTOM_LEFT_ANCHOR);
		} else {
			text8.drawString(g, "Trở về", Color.WHITE_CODE, GameConstants.SCREEN_WIDTH - 8, GameConstants.SCREEN_HEIGHT - 18, GameConstants.TOP_RIGHT_ANCHOR);
			text8.drawString(g, "Menu", Color.WHITE_CODE, 5, GameConstants.SCREEN_HEIGHT - 4, GameConstants.BOTTOM_LEFT_ANCHOR);
			text8.drawString(g, "Chọn Game", Color.WHITE_CODE, GameConstants.SCREEN_WIDTH / 2, GameConstants.SCREEN_HEIGHT - 4, GameConstants.BOTTOM_HCENTER_ANCHOR);
		}

		// pain menu
		g.setClip(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
		menu.paint(g);
	}

	private void drawEvent(Graphics g) {
		g.setClip(0, GameConstants.SCREEN_HEIGHT / 2, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT / 2);
		g.drawImage(bgtileImage, GameConstants.SCREEN_WIDTH / 2, GameConstants.SCREEN_HEIGHT / 2, GameConstants.TOP_HCENTER_ANCHOR);
		g.setColor(0xA53A06);
		if (GameConstants.IS_240x320_SCREEN) {
			g.fillRect(5, GameConstants.SCREEN_HEIGHT / 2 + 8, GameConstants.SCREEN_WIDTH - 8, GameConstants.SCREEN_HEIGHT / 2);
		} else {
			g.fillRect(5, GameConstants.SCREEN_HEIGHT / 2 + 8, GameConstants.SCREEN_WIDTH - 10, GameConstants.SCREEN_HEIGHT / 2);
		}
	}

	public void keyReleased(int keyCode) {
		keyCode = Key.getGameKey(keyCode);

		if (keyCode == Key.SOFT_LEFT) {
			if (!menu.isShowing()) {
				menu.show();
			} else {
				menu.hide();
			}
			return;
		}
		if (menu.isShowing()) {
			return;
		}
		switch (keyCode) {
		case Key.DOWN:
			choosedIndex++;
			textScrolCount = 0;
			if (choosedIndex > numberOfEvents - 1) {
				choosedIndex = 0;
			}
			break;
		case Key.UP:
			choosedIndex--;
			textScrolCount = 0;
			if (choosedIndex < 0) {
				choosedIndex = numberOfEvents - 1;
			}
			break;
		case Key.RIGHT:
			if (isLockLeftRightKey) {
				return;
			}
			currentKey = Key.RIGHT;
			isLockLeftRightKey = true;
			count = 0;
			actionState = 1;
			break;
		case Key.LEFT:
			if (isLockLeftRightKey) {
				return;
			}
			currentKey = Key.LEFT;
			isLockLeftRightKey = true;
			count = 0;
			actionState = 1;
			break;
		case Key.FIRE:
			String gameId = Constants.GAME_ID[index];
			System.out.println(gameId);
			Global.currentGame = (Game) Global.gameMap.get(gameId);
			SocketClientUtil.joinGameRequest(gameId);
			GameGlobal.nextState(Global.frmListRoom, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
			break;
		case Key.SOFT_RIGHT:
			GameActionHandle.isOnSignOut = true;
			Global.gameActionClient.detroy();
			GameGlobal.nextState(Global.frmGameService, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
			break;
		default:
			break;
		}
	}

	public void onActionPerform(Event event) {
	  // "Bạn bè", "Nạp điểm", "Tặng Game", "Thông tin TK", "Cài đặt", "Trợ giúp"
	  String action = event.getAction();
	  if (action.equals(MENU[0])) { // Bạn bè
	    GameGlobal.nextState(Global.frmFriend, null, Transformer.TRANSFORM_WITH_LOADING_FORM);
	  } else if (action.equals(MENU[1])) { // Nạp điểm
	  } else if (action.equals(MENU[2])) { // Tặng Game
	    Display.getDisplay(GameGlobal.getMidlet()).setCurrent(new FrmSendGame(null));
	  } else if (action.equals(MENU[3])) { // Thông tin TK
	  } else if (action.equals(MENU[4])) { // Cài đặt
	  } else if (action.equals(MENU[5])) { // Trợ giúp
	  }
	}

	public void doTask() {
		textScrolCount++;

		if (count > -1) {
			count++;
			actionState++;
			if (actionState == 6) {
				actionState = 0;
				count = -1;
				isLockLeftRightKey = false;

				if (currentKey == Key.LEFT) {
					index = (index + 1) % Constants.GAME_ID.length;
					firstIndex = index - 1 < 0 ? (Constants.GAME_ID.length - 1) : index - 1;
					lastIndex = index + 1 > (Constants.GAME_ID.length - 1) ? 0 : index + 1;
				} else {
					index--;
					if (index < 0) {
						index = (Constants.GAME_ID.length - 1);
					}
					firstIndex = index - 1 < 0 ? (Constants.GAME_ID.length - 1) : index - 1;
					lastIndex = index + 1 > (Constants.GAME_ID.length - 1) ? 0 : index + 1;
				}
			}
		}
	}

	protected void destroy() {
		if (menu != null) {
			menu.detroy();
			menu = null;
		}

		itemsImages = null;

		text8 = null;
		bgtileImage = null;
		rightWhiteArrowImage = null;
		leftWhiteArrowImage = null;

		MENU = null;
	}
}
