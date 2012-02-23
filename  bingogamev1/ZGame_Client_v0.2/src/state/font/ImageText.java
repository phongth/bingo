package state.font;

import javax.microedition.lcdui.Graphics;

public interface ImageText extends ImageFont {
	public int drawParagraph(Graphics g, String text, int color, int width,
			int x, int y);

	public int drawParagraph(Graphics g, String text, int width, int x, int y);

	public int drawParagraph(Graphics g, String text, int color, int width,
			int lineDistance, int x, int y);

	public void prepareCache(String[] texts, int width);

	public void prepareCache(String text, int width);
}
