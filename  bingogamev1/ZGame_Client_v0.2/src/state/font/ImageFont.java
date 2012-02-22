package state.font;

import javax.microedition.lcdui.Graphics;

public interface ImageFont {
	public int drawString(Graphics g, String text, int x, int y);
	public int drawString(Graphics g, String text, int x, int y, int anchors);
	public int drawString(Graphics g, String text, int color, int x, int y, int anchors);
	
	public int drawSubstring(Graphics g, String text, int offset, int length, int x, int y);
	public int drawSubstring(Graphics g, String text, int offset, int length, int x, int y, int anchors);
	public int drawSubstring(Graphics g, String text, int color, int offset, int length, int x, int y, int anchors);
	
	public int stringWidth(String str);
	public int substringWidth(String str, int offset, int length);
	public int charWidth(char c);
	
	public int drawOneChar(Graphics g, char c, int x, int y);
	public int drawOneChar(Graphics g, char c, int color, int x, int y);
	public byte getHeight();
	
	public void detroy();
}
