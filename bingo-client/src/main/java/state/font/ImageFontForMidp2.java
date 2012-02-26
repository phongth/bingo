package state.font;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import state.util.Color;

public class ImageFontForMidp2 implements ImageFont {
	private byte[] baseData;
	private byte[] widthes;
	private short[] x;
	private String characterMap;
	private short baseWidth;

	private byte height;
	private byte baseline;
	private byte xIndent;
	private byte yIndent;
	private byte spaceWidth;

	protected ImageFontForMidp2(String fontName) {
		try {
			InputStream input = new Object().getClass().getResourceAsStream(
					fontName);
			DataInputStream data = new DataInputStream(input);

			data.readByte(); // Bỏ qua trường version
			this.height = data.readByte();
			this.baseline = data.readByte();
			this.xIndent = data.readByte();
			this.yIndent = data.readByte();
			this.spaceWidth = data.readByte();

			characterMap = data.readUTF();
			int count = characterMap.length();

			// read characters widthes
			this.widthes = new byte[count];
			this.x = new short[count];
			for (int i = 0; i < count; i++) {
				widthes[i] = data.readByte();
			}

			data.readByte(); // Bỏ qua số lượng image
			int imageLength = data.readShort();
			byte[] buffer = new byte[imageLength];
			data.read(buffer, 0, imageLength);
			Image tmp = Image.createImage(buffer, 0, imageLength);
			baseWidth = (short) tmp.getWidth();
			short baseHeight = (short) tmp.getHeight();
			baseData = new byte[baseWidth * baseHeight];

			int[] tmpData = new int[baseData.length];
			tmp.getRGB(tmpData, 0, baseWidth, 0, 0, baseWidth, baseHeight);
			tmp = null;
			for (int i = 0; i < tmpData.length; i++) {
				if (tmpData[i] != 0x00FFFFFF) {
					baseData[i] = 1;
				}
			}
			tmpData = null;

			// calculate characters coordinates
			short curX = 0;
			int curImageWidth = baseWidth;
			for (int i = 0; i < count; i++) {
				if (widthes[i] < 0) {
					// negative width points to another character
					int sourceIndex = -widthes[i];
					widthes[i] = widthes[sourceIndex];
					x[i] = x[sourceIndex];
				} else {
					if (curX + widthes[i] > curImageWidth) {
						curX = 0;
					}
					x[i] = curX;
					curX += widthes[i];
				}
			}
		} catch (IOException ex) {
			// ex.printStackTrace();
		}
		System.gc();
	}

	/**
	 * 
	 * @param g
	 * @param text
	 * @param x
	 * @param y
	 * @return Vị trí kết thúc của chuỗi
	 */
	public int drawString(Graphics g, String text, int x, int y) {
		return drawSubstring(g, text, 0xFF000000, 0, text.length(), x, y,
				Graphics.TOP | Graphics.LEFT);
	}

	/**
	 * 
	 * @param g
	 * @param text
	 * @param x
	 * @param y
	 * @param anchors
	 * @return Vị trí kết thúc của chuỗi
	 */
	public int drawString(Graphics g, String text, int x, int y, int anchors) {
		return drawSubstring(g, text, 0xFF000000, 0, text.length(), x, y,
				anchors);
	}

	/**
	 * 
	 * @param g
	 * @param text
	 * @param color
	 * @param x
	 * @param y
	 * @param anchors
	 * @return Vị trí kết thúc của chuỗi
	 */
	public int drawString(Graphics g, String text, int color, int x, int y,
			int anchors) {
		return drawSubstring(g, text, color, 0, text.length(), x, y, anchors);
	}

	/**
	 * 
	 * @param g
	 * @param text
	 * @param offset
	 * @param length
	 * @param x
	 * @param y
	 * @return Vị trí kết thúc của chuỗi
	 */
	public int drawSubstring(Graphics g, String text, int offset, int length,
			int x, int y) {
		return drawSubstring(g, text, 0xFF000000, offset, length, x, y,
				Graphics.TOP | Graphics.LEFT);
	}

	/**
	 * 
	 * @param g
	 * @param text
	 * @param offset
	 * @param length
	 * @param x
	 * @param y
	 * @param anchors
	 * @return Vị trí kết thúc của chuỗi
	 */
	public int drawSubstring(Graphics g, String text, int offset, int length,
			int x, int y, int anchors) {
		return drawSubstring(g, text, 0xFF000000, offset, length, x, y, anchors);
	}

	/**
	 * 
	 * @param g
	 * @param text
	 * @param color
	 * @param offset
	 * @param length
	 * @param x
	 * @param y
	 * @param anchors
	 * @return Vị trí kết thúc của chuỗi
	 */
	public int drawSubstring(Graphics g, String text, int color, int offset,
			int length, int x, int y, int anchors) {
		color = color | 0xFF000000;
		int xx = getX(substringWidth(text, offset, length), x, anchors);
		int yy = getY(y, anchors);
		for (int i = offset; i < offset + length; i++) {
			xx = drawOneChar(g, text.charAt(i), color, xx, yy);
		}
		return xx;
	}

	private int getX(int w, int x, int anchors) {
		if ((anchors & Graphics.RIGHT) != 0) {
			return x - w;
		}

		if ((anchors & Graphics.HCENTER) != 0) {
			return x - w / 2;
		}
		return x;
	}

	private int getY(int y, int anchors) {
		if ((anchors & Graphics.BOTTOM) != 0) {
			return y - height;
		}

		if ((anchors & Graphics.VCENTER) != 0) {
			return y - height / 2;
		}

		if ((anchors & Graphics.BASELINE) != 0) {
			return y - baseline;
		}
		return y;
	}

	public int stringWidth(String str) {
		return substringWidth(str, 0, str.length());
	}

	public int substringWidth(String str, int offset, int length) {
		int w = 0;
		for (int i = offset; i < offset + length; i++) {
			w += charWidth(str.charAt(i));
		}
		return w;
	}

	public int charWidth(char c) {
		if (c == ' ') {
			return spaceWidth + xIndent;
		}
		int index = charIndex(c);
		if (index < 0) {
			return spaceWidth + xIndent;
		}
		return widthes[index] + xIndent;
	}

	public int drawOneChar(Graphics g, char c, int x, int y) {
		return drawOneChar(g, c, Color.BLACK_CODE, x, y);
	}

	/**
	 * 
	 * @param g
	 * @param c
	 * @param color
	 * @param x
	 * @param y
	 * @return Vị trí cho ký tự tiếp theo
	 */
	public int drawOneChar(Graphics g, char c, int color, int x, int y) {
		// skip if it is a space
		if (c == ' ') {
			return x + this.spaceWidth + xIndent;
		}
		int charIndex = charIndex(c);

		// draw the unknown character as a rectangle
		if (charIndex < 0) {
			int squareWidth = this.spaceWidth + xIndent;
			g.drawRect(x, y, squareWidth - 1, height - 1);
			return x + squareWidth;
		}

		int charX = this.x[charIndex];
		int cw = widthes[charIndex];
		y += yIndent / 2;

		// Tạo mảng mầu của ký tự cần vẽ
		int[] charData = new int[cw * height];
		for (int i = 0; i < cw; i++) {
			for (int j = 0; j < height; j++) {
				if (baseData[j * baseWidth + i + charX] == 1) {
					charData[j * cw + i] = color;
				}
			}
		}

		// Vẽ ký tự cần vẽ
		g.drawRGB(charData, 0, cw, x, y, cw, height, true);
		return x + cw + xIndent;
	}

	public byte getHeight() {
		return height;
	}

	private int charIndex(char c) {
		return characterMap.indexOf(c);
	}

	public void detroy() {
		baseData = null;
		widthes = null;
		x = null;
		characterMap = null;
	}
}
