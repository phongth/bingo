package state;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import state.font.FontManager;
import state.font.ImageFont;
import state.util.Color;
import state.util.Point;

/**
 * Class thực hiện hiệu ứng chuyển màn hình
 * 
 * @author ptran2
 * @version 0.2
 */
public class Transformer extends GameForm implements TimerListener {

	/**
	 * Form hiện tại dịch sang trái, form tiếp theo từ bên phải màn hình dịch
	 * vào
	 */
	public static final int TRANSFORM_MOVE_LEFT = 0;

	/**
	 * Form hiện tại dịch sang phải, form tiếp theo từ bên trái màn hình dịch
	 * vào
	 */
	public static final int TRANSFORM_MOVE_RIGHT = 1;

	/** Kiểu cửa sổ mở ra từ giữa, mà hình mới sẽ xuất hiện ở giữa màn hình cũ */
	public static final int TRANSFORM_CLIP_WIDE_OPEN_FROM_CENTER = 2;

	/**
	 * Cửa sổ mới sẽ xuất hiện dưới dạng khung hình chữ thập ở giữa cửa sổ cũ
	 * rồi mở to dần đến khi chiếm hết màn hình
	 */
	public static final int TRANSFORM_CLIP_BREAK_OLD_FORM = 3;

	/**
	 * Kiểu cửa sổ đóng từ hai bên khéo vào giữa cửa sổ cũ sẽ bị hẹp dần cho đến
	 * khi biến mất hẳn
	 */
	public static final int TRANSFORM_CLIP_CLOSE_TO_CENTER = 4;

	/**
	 * Kiểu hình chữ thập khép dần từ ngoài vào giữa, cửa sổ cũ sẽ bị thu hẹp
	 * dần cho đến khi mất hẳn
	 */
	public static final int TRANSFORM_CLIP_JOIN_NEW_FORM = 5;

	/**
	 * Kiểu chuyển sang form loading trước rồi mới chuyển sang form mới sau,
	 * dùng trong việc tiết kiệm bộ nhớ
	 */
	public static final int TRANSFORM_WITH_LOADING_FORM = 6;

	private static final int MOVE_STEP = 40;

	private int transformType;
	protected GameForm currentState;
	protected GameForm nextState;

	private Image nextStateImage;
	private Image currentStateImage;
	private Image loadingImage1;
	private Image loadingImage2;
	private Image logoImage;

	private Point currentStateLocation;
	private Point nextStateLocation;
	private Point clipLocation;
	private Point clipSize;
	private int centerAreaSize;
	private ImageFont text8;
	private int count;
	private int loadingPercent;

	public void transform(GameForm currentState, GameForm nextState,
			final Hashtable params, int transformType) {
		this.currentState = currentState;
		this.nextState = nextState;
		this.transformType = transformType;

		if (transformType != TRANSFORM_WITH_LOADING_FORM) {
			nextStateImage = Image.createImage(GameConstants.SCREEN_WIDTH,
					GameConstants.SCREEN_HEIGHT);
			currentStateImage = Image.createImage(GameConstants.SCREEN_WIDTH,
					GameConstants.SCREEN_HEIGHT);
			currentState.setScreen(currentStateImage);
			nextState.setScreen(nextStateImage);
		}

		switch (transformType) {
		case TRANSFORM_MOVE_LEFT:
			currentStateLocation = Point.createNewOrSetValue(
					currentStateLocation, 0, 0);
			nextStateLocation = Point.createNewOrSetValue(nextStateLocation,
					GameConstants.SCREEN_WIDTH, 0);
			break;
		case TRANSFORM_MOVE_RIGHT:
			currentStateLocation = Point.createNewOrSetValue(
					currentStateLocation, 0, 0);
			nextStateLocation = Point.createNewOrSetValue(nextStateLocation,
					-GameConstants.SCREEN_WIDTH, 0);
			break;
		case TRANSFORM_CLIP_WIDE_OPEN_FROM_CENTER:
			clipSize = Point.createNewOrSetValue(clipSize, 0,
					GameConstants.SCREEN_HEIGHT);
			clipLocation = Point.createNewOrSetValue(clipLocation,
					GameConstants.SCREEN_WIDTH / 2, 0);
			break;
		case TRANSFORM_CLIP_CLOSE_TO_CENTER:
			clipSize = Point.createNewOrSetValue(clipSize,
					GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
			clipLocation = Point.createNewOrSetValue(clipLocation, 0, 0);
			break;
		case TRANSFORM_CLIP_BREAK_OLD_FORM:
			centerAreaSize = MOVE_STEP;
			break;
		case TRANSFORM_CLIP_JOIN_NEW_FORM:
			centerAreaSize = GameConstants.SCREEN_HEIGHT;
			break;
		}

		if (!this.nextState.isRunning
				&& (transformType != TRANSFORM_WITH_LOADING_FORM)) {
			this.nextState.isRunning = true;
			this.nextState.init(params);
		}

		isRunning = true;
		GameGlobal.systemCanvas.beginTransform();

		if (transformType == TRANSFORM_WITH_LOADING_FORM) {
			try {
				loadingImage1 = Image
						.createImage(GameConstants.CORE_IMAGE_FOLDER
								+ "/loading_1.png");
				loadingImage2 = Image
						.createImage(GameConstants.CORE_IMAGE_FOLDER
								+ "/loading_2.png");
				logoImage = Image.createImage(GameConstants.CORE_IMAGE_FOLDER
						+ "/logoMobiplay.png");
			} catch (IOException e) {
				e.printStackTrace();
			}

			setTimerListener(this);
			text8 = FontManager.getFont(FontManager.FONT_SIZE_8);
			count = 0;

			new Control() {
				public void perform() {
					loadingPercent = 20;
					if (Transformer.this.currentState != null) {
						Transformer.this.currentState.detroyAll();
						Transformer.this.currentState = null;
						System.gc();
					}
					loadingPercent = 40;
					Transformer.this.nextState.isRunning = true;
					Transformer.this.nextState.init(params);
					loadingPercent = 70;
					System.gc();
					loadingPercent = 80;
					GameGlobal.systemCanvas.endTransform(
							Transformer.this.nextState, false);
				}
			}.start();
		}
	}

	public GameForm getOldState() {
		return currentState;
	}

	public int getLoadingPercent() {
		return loadingPercent;
	}

	public void draw(Graphics g) {
		if (transformType == TRANSFORM_WITH_LOADING_FORM) {
			// Vẽ nền
			g.setColor(0x440000);
			g.setClip(0, 0, GameConstants.SCREEN_WIDTH,
					GameConstants.SCREEN_HEIGHT);
			g.fillRect(0, 0, GameConstants.SCREEN_WIDTH,
					GameConstants.SCREEN_HEIGHT);

			// Draw loading bar
			int index = count % 11;
			int x = 0;
			int y = 0;
			if (GameConstants.IS_240x320_SCREEN) {
				g.drawImage(logoImage, GameConstants.SCREEN_WIDTH / 2, 76,
						GameConstants.TOP_HCENTER_ANCHOR);
				text8.drawString(g, "Đang tải...", Color.WHITE_CODE,
						GameConstants.SCREEN_WIDTH / 2, 264,
						GameConstants.TOP_HCENTER_ANCHOR);
				x = 62;
				y = 247;
			} else {
				g.drawImage(logoImage, GameConstants.SCREEN_WIDTH / 2, 32,
						GameConstants.TOP_HCENTER_ANCHOR);
				text8.drawString(g, "Đang tải...", Color.WHITE_CODE,
						GameConstants.SCREEN_WIDTH / 2, 193,
						GameConstants.TOP_HCENTER_ANCHOR);
				x = 101;
				y = 172;
			}
			for (int i = 1; i < 11; i++) {
				if (i <= index) {
					g.drawImage(loadingImage1, x
							+ (loadingImage1.getWidth() + 1) * (i - 1), y,
							GameConstants.TOP_LEFT_ANCHOR);
				} else {
					g.drawImage(loadingImage2, x
							+ (loadingImage1.getWidth() + 1) * (i - 1), y,
							GameConstants.TOP_LEFT_ANCHOR);
				}
			}
			return;
		}

		currentState.updateFullScreen();
		nextState.updateFullScreen();
		g.setColor(Color.WHITE_CODE);
		g.fillRect(0, 0, GameConstants.SCREEN_WIDTH,
				GameConstants.SCREEN_HEIGHT);

		switch (transformType) {
		case TRANSFORM_MOVE_LEFT:
			if (nextStateLocation.x > 0) {
				currentStateLocation.x -= MOVE_STEP;
				nextStateLocation.x -= MOVE_STEP;
				g.drawImage(currentStateImage, currentStateLocation.x,
						currentStateLocation.y, GameConstants.TOP_LEFT_ANCHOR);
				g.drawImage(nextStateImage, nextStateLocation.x,
						nextStateLocation.y, GameConstants.TOP_LEFT_ANCHOR);
			} else {
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);
				GameGlobal.systemCanvas.endTransform(nextState, true);
			}
			break;
		case TRANSFORM_MOVE_RIGHT:
			if (nextStateLocation.x < 0) {
				currentStateLocation.x += MOVE_STEP;
				nextStateLocation.x += MOVE_STEP;
				g.drawImage(currentStateImage, currentStateLocation.x,
						currentStateLocation.y, GameConstants.TOP_LEFT_ANCHOR);
				g.drawImage(nextStateImage, nextStateLocation.x,
						nextStateLocation.y, GameConstants.TOP_LEFT_ANCHOR);
			} else {
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);
				GameGlobal.systemCanvas.endTransform(nextState, true);
			}
			break;
		case TRANSFORM_CLIP_WIDE_OPEN_FROM_CENTER:
			if (clipSize.x < GameConstants.SCREEN_WIDTH) {
				clipSize.x += MOVE_STEP;
				clipLocation.x -= (MOVE_STEP / 2);

				g.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
				g.drawImage(currentStateImage, 0, 0,
						GameConstants.TOP_LEFT_ANCHOR);
				g.setClip(clipLocation.x, clipLocation.y, clipSize.x,
						clipSize.y);
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);
			} else {
				g.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);
				GameGlobal.systemCanvas.endTransform(nextState, true);
			}
			break;
		case TRANSFORM_CLIP_CLOSE_TO_CENTER:
			if (clipSize.x > 0) {
				clipSize.x -= MOVE_STEP;
				clipLocation.x += (MOVE_STEP / 2);

				g.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);
				g.setClip(clipLocation.x, clipLocation.y, clipSize.x,
						clipSize.y);
				g.drawImage(currentStateImage, 0, 0,
						GameConstants.TOP_LEFT_ANCHOR);
			} else {
				g.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);
				GameGlobal.systemCanvas.endTransform(nextState, true);
			}
			break;
		case TRANSFORM_CLIP_BREAK_OLD_FORM:
			if (centerAreaSize < GameConstants.SCREEN_HEIGHT) {
				g.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);

				int width = (GameConstants.SCREEN_WIDTH - centerAreaSize) / 2;
				int height = (GameConstants.SCREEN_HEIGHT - centerAreaSize) / 2;
				g.setClip(0, 0, width, height);
				g.drawImage(currentStateImage, 0, 0,
						GameConstants.TOP_LEFT_ANCHOR);
				g.setClip((GameConstants.SCREEN_WIDTH + centerAreaSize) / 2, 0,
						width, height);
				g.drawImage(currentStateImage, 0, 0,
						GameConstants.TOP_LEFT_ANCHOR);
				g.setClip(0,
						(GameConstants.SCREEN_HEIGHT + centerAreaSize) / 2,
						width, height);
				g.drawImage(currentStateImage, 0, 0,
						GameConstants.TOP_LEFT_ANCHOR);
				g.setClip((GameConstants.SCREEN_WIDTH + centerAreaSize) / 2,
						(GameConstants.SCREEN_HEIGHT + centerAreaSize) / 2,
						width, height);
				g.drawImage(currentStateImage, 0, 0,
						GameConstants.TOP_LEFT_ANCHOR);
				centerAreaSize += MOVE_STEP;
			} else {
				g.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);
				GameGlobal.systemCanvas.endTransform(nextState, true);
			}
			break;
		case TRANSFORM_CLIP_JOIN_NEW_FORM:
			if (centerAreaSize > -MOVE_STEP) {
				if (centerAreaSize < 0) {
					centerAreaSize = 0;
				}
				g.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
				g.drawImage(currentStateImage, 0, 0,
						GameConstants.TOP_LEFT_ANCHOR);

				int width = (GameConstants.SCREEN_WIDTH - centerAreaSize) / 2;
				int height = (GameConstants.SCREEN_HEIGHT - centerAreaSize) / 2;
				g.setClip(0, 0, width, height);
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);
				g.setClip((GameConstants.SCREEN_WIDTH + centerAreaSize) / 2, 0,
						width, height);
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);
				g.setClip(0,
						(GameConstants.SCREEN_HEIGHT + centerAreaSize) / 2,
						width, height);
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);
				g.setClip((GameConstants.SCREEN_WIDTH + centerAreaSize) / 2,
						(GameConstants.SCREEN_HEIGHT + centerAreaSize) / 2,
						width, height);
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);
				centerAreaSize -= MOVE_STEP;
			} else {
				g.setClip(0, 0, GameConstants.SCREEN_WIDTH,
						GameConstants.SCREEN_HEIGHT);
				g
						.drawImage(nextStateImage, 0, 0,
								GameConstants.TOP_LEFT_ANCHOR);
				GameGlobal.systemCanvas.endTransform(nextState, true);
			}
			break;
		}
	}

	public void doTask() {
		count++;
	}

	public void destroy() {
		removeTimerListener();
		if (currentState != null) {
			currentState.isRunning = false;
			currentState = null;
		}
		nextState = null;
		nextStateImage = null;
		currentStateImage = null;
		loadingImage1 = null;
		loadingImage2 = null;
		logoImage = null;

		currentStateLocation = null;
		nextStateLocation = null;
		clipLocation = null;
		clipSize = null;

		System.gc();
	}
}
