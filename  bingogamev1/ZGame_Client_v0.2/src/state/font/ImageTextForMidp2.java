package state.font;

import java.util.Random;

import javax.microedition.lcdui.Graphics;

public class ImageTextForMidp2 extends ImageFontForMidp2 implements ImageText {
	private static final byte DEFAULT_LINE_DISTANCE = 3;
	protected static final byte DEFAULT_TEXT_CACHE_SIZE = 10;

	private Random random;

	private int[] textHashCodeCache;
	private short[][] beginIndexCache;
	private short[][] lenCache;

	protected ImageTextForMidp2(String fontName) {
		this(fontName, DEFAULT_TEXT_CACHE_SIZE);
	}

	protected ImageTextForMidp2(String fontName, int cacheSize) {
		super(fontName);
		textHashCodeCache = new int[cacheSize];
		beginIndexCache = new short[cacheSize][];
		lenCache = new short[cacheSize][];
		random = new Random();
	}

	public int drawParagraph(Graphics g, String text, int color, int width,
			int x, int y) {
		return drawParagraph(g, text, color, width, DEFAULT_LINE_DISTANCE, x, y);
	}

	public int drawParagraph(Graphics g, String text, int width, int x, int y) {
		return drawParagraph(g, text, 0xFF000000, width, DEFAULT_LINE_DISTANCE,
				x, y);
	}

	public int drawParagraph(Graphics g, String text, int color, int width,
			int lineDistance, int x, int y) {
		if ((text == null) || "".equals(text)) {
			return 0;
		}
		int hashCode = text.hashCode();

		// Nếu đoạn text này đã được vẽ trước đó thì lấy catch ra vẽ lại
		for (int i = 0; i < textHashCodeCache.length; i++) {
			if (hashCode == textHashCodeCache[i]) {
				for (int j = 0; j < beginIndexCache[i].length; j++) {
					drawSubstring(g, text, color, beginIndexCache[i][j],
							lenCache[i][j], x, y + j
									* (getHeight() + lineDistance),
							Graphics.TOP | Graphics.LEFT);
				}
				return y + beginIndexCache[i].length
						* (getHeight() + lineDistance);
			}
		}

		// Tìm vị trí còn trống trong cache
		int index = -1;
		for (int i = 0; i < textHashCodeCache.length; i++) {
			if (beginIndexCache[i] == null) { // Nếu cache chưa dùng
				index = i;
				break;
			}
		}

		if (index == -1) {
			index = random.nextInt(textHashCodeCache.length);
		}

		// Làm sạch cache
		textHashCodeCache[index] = text.hashCode();
		int numberOfLine = text.length() / 5; // Dự đoán ít nhất là 5 ký tự trên
												// 1 dòng
		beginIndexCache[index] = new short[numberOfLine];
		lenCache[index] = new short[numberOfLine];

		// Nếu chưa có thì tiến hành phân tách đoạn văn thành các dòng, lưu vào
		// cache và vẽ
		short lineIndex = 0;
		short charIndex = 0;
		short beginIndex = 0;
		short currentLineLen = 0;
		short lastSpaceIndex = -1;
		while (charIndex < text.length()) {
			char c = text.charAt(charIndex);
			int charLen = charWidth(c);
			boolean isNextLine = false;
			if (c == '\n') {
				beginIndexCache[index][lineIndex] = beginIndex;
				lenCache[index][lineIndex] = (short) (charIndex - beginIndex);
				drawSubstring(g, text, color,
						beginIndexCache[index][lineIndex],
						lenCache[index][lineIndex], x, y + lineIndex
								* (getHeight() + lineDistance), Graphics.TOP
								| Graphics.LEFT);
				beginIndex = (short) (charIndex + 1);
				currentLineLen = 0;
				isNextLine = true;
			} else if (currentLineLen + charLen > width) { // Nếu độ dài của
															// dòng hiện tại quá
															// độ dài cho phép
				beginIndexCache[index][lineIndex] = beginIndex;
				if (lastSpaceIndex > -1) { // Nếu có dấu space trước đó thì ngắt
											// tại vị trí space
					lenCache[index][lineIndex] = (short) (lastSpaceIndex - beginIndex);
					drawSubstring(g, text, color,
							beginIndexCache[index][lineIndex],
							lenCache[index][lineIndex], x, y + lineIndex
									* (getHeight() + lineDistance),
							Graphics.TOP | Graphics.LEFT);
					beginIndex = (short) (lastSpaceIndex + 1);
					currentLineLen = (short) (charIndex - lastSpaceIndex);
				} else { // Ngắt ký tự
					lenCache[index][lineIndex] = (short) (charIndex - beginIndex);
					drawSubstring(g, text, color,
							beginIndexCache[index][lineIndex],
							lenCache[index][lineIndex], x, y + lineIndex
									* (getHeight() + lineDistance),
							Graphics.TOP | Graphics.LEFT);
					beginIndex = charIndex;
					currentLineLen = 0;
				}
				isNextLine = true;
			} else { // Nếu ko xuống dòng thì duyệt tiếp
				currentLineLen += charLen;
				if (c == ' ') {
					lastSpaceIndex = charIndex;
				}
			}

			if (isNextLine) {
				// Kiểm tra xem đã hết cache chưa, nếu đã hết thì tạo cache gấp
				// rưỡi và copy dữ liệu sang
				if (lineIndex >= beginIndexCache[index].length) {
					resizecache(index, beginIndexCache[index].length
							+ beginIndexCache[index].length / 2);
				}
				lastSpaceIndex = -1;
				lineIndex++;
			}
			charIndex++;
		}

		// Nhét nốt phần cuối vào cache
		if (lineIndex >= beginIndexCache[index].length) {
			resizecache(index, beginIndexCache[index].length + 1);
		}
		beginIndexCache[index][lineIndex] = beginIndex;
		lenCache[index][lineIndex] = (short) (text.length() - beginIndex);
		drawSubstring(g, text, color, beginIndexCache[index][lineIndex],
				lenCache[index][lineIndex], x, y + lineIndex
						* (getHeight() + lineDistance), Graphics.TOP
						| Graphics.LEFT);

		// Trả ra tạo độ kết thúc của đoạn văn
		return y + beginIndexCache[index].length * (getHeight() + lineDistance);
	}

	/**
	 * Chuẩn bị sẵn cache để tăng tốc độ khi vẽ
	 * 
	 * @param texts
	 * @param width
	 */
	public void prepareCache(String[] texts, int width) {
		for (int i = 0; i < texts.length; i++) {
			prepareCache(texts[i], width);
		}
	}

	/**
	 * Chuẩn bị sẵn cache để tăng tốc độ khi vẽ
	 * 
	 * @param text
	 * @param width
	 */
	public void prepareCache(String text, int width) {
		if ((text == null) || "".equals(text)) {
			return;
		}
		int hashCode = text.hashCode();

		// Nếu đoạn text này đã được prepare thì thoát
		for (int i = 0; i < textHashCodeCache.length; i++) {
			if (hashCode == textHashCodeCache[i]) {
				return;
			}
		}

		// Tìm vị trí còn trống trong cache
		int index = -1;
		for (int i = 0; i < textHashCodeCache.length; i++) {
			if (beginIndexCache[i] == null) { // Nếu cache chưa dùng
				index = i;
				break;
			}
		}

		if (index == -1) {
			index = random.nextInt(textHashCodeCache.length);
		}

		// Làm sạch cache
		textHashCodeCache[index] = text.hashCode();
		int numberOfLine = text.length() / 5; // Dự đoán ít nhất là 5 ký tự trên
												// 1 dòng
		beginIndexCache[index] = new short[numberOfLine];
		lenCache[index] = new short[numberOfLine];

		// Nếu chưa có thì tiến hành phân tách đoạn văn thành các dòng, lưu vào
		// cache và vẽ
		short lineIndex = 0;
		short charIndex = 0;
		short beginIndex = 0;
		short currentLineLen = 0;
		short lastSpaceIndex = -1;
		while (charIndex < text.length()) {
			char c = text.charAt(charIndex);
			int charLen = charWidth(c);
			boolean isNextLine = false;
			if (c == '\n') {
				beginIndexCache[index][lineIndex] = beginIndex;
				lenCache[index][lineIndex] = (short) (charIndex - beginIndex);
				beginIndex = (short) (charIndex + 1);
				currentLineLen = 0;
				isNextLine = true;
			} else if (currentLineLen + charLen > width) { // Nếu độ dài của
															// dòng hiện tại quá
															// độ dài cho phép
				beginIndexCache[index][lineIndex] = beginIndex;
				if (lastSpaceIndex > -1) { // Nếu có dấu space trước đó thì ngắt
											// tại vị trí space
					lenCache[index][lineIndex] = (short) (lastSpaceIndex - beginIndex);
					beginIndex = (short) (lastSpaceIndex + 1);
					currentLineLen = (short) (charIndex - lastSpaceIndex);
				} else { // Ngắt ký tự
					lenCache[index][lineIndex] = (short) (charIndex - beginIndex);
					beginIndex = charIndex;
					currentLineLen = 0;
				}
				isNextLine = true;
			} else { // Nếu ko xuống dòng thì duyệt tiếp
				currentLineLen += charLen;
				if (c == ' ') {
					lastSpaceIndex = charIndex;
				}
			}

			if (isNextLine) {
				// Kiểm tra xem đã hết cache chưa, nếu đã hết thì tạo cache gấp
				// rưỡi và copy dữ liệu sang
				if (lineIndex >= beginIndexCache[index].length) {
					resizecache(index, beginIndexCache[index].length
							+ beginIndexCache[index].length / 2);
				}
				lastSpaceIndex = -1;
				lineIndex++;
			}
			charIndex++;
		}

		// Nhét nốt phần cuối vào cache
		beginIndexCache[index][lineIndex] = beginIndex;
		lenCache[index][lineIndex] = (short) (text.length() - beginIndex);

		// Tối ưu lại cache
		resizecache(index, lineIndex + 1);
	}

	private void resizecache(int index, int newLen) {
		int min = Math.min(beginIndexCache[index].length, newLen);
		short[] newCache = new short[newLen];
		System.arraycopy(beginIndexCache[index], 0, newCache, 0, min);
		beginIndexCache[index] = newCache;
		short[] newCache1 = new short[newLen];
		System.arraycopy(lenCache[index], 0, newCache1, 0, min);
		lenCache[index] = newCache1;
	}

	public void detroy() {
		super.detroy();
		textHashCodeCache = null;
		beginIndexCache = null;
		lenCache = null;
		random = null;
	}
}
