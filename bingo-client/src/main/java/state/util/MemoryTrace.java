package state.util;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import state.GameConstants;

public class MemoryTrace {
	private static long maxUse;

	/**
	 * Ghi log ra Graphic tình trạng memory hiện tại với mầu đã cho
	 * 
	 * @param g
	 *            - Graphic dùng để ghi log
	 * @param color
	 *            - Mầu của text
	 */
	public static void trace(Graphics g, int color) {
		g.setColor(color);
		g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD,
				Font.SIZE_LARGE));
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		long use = total - free;
		if (use > maxUse) {
			maxUse = use;
		}
		g.drawString("all:", 90, 10, GameConstants.TOP_LEFT_ANCHOR);
		g.drawString(String.valueOf(total), 240, 10,
				GameConstants.TOP_RIGHT_ANCHOR);
		g.drawString("free:", 90, 30, GameConstants.TOP_LEFT_ANCHOR);
		g.drawString(String.valueOf(free), 240, 30,
				GameConstants.TOP_RIGHT_ANCHOR);
		g.drawString("use:", 90, 50, GameConstants.TOP_LEFT_ANCHOR);
		g.drawString(String.valueOf(use), 240, 50,
				GameConstants.TOP_RIGHT_ANCHOR);
		g.drawString("max:", 90, 70, GameConstants.TOP_LEFT_ANCHOR);
		g.drawString(String.valueOf(maxUse), 240, 70,
				GameConstants.TOP_RIGHT_ANCHOR);

		System.out.println("all: " + total);
		System.out.println("free: " + free);
		System.out.println("use: " + use);
		System.out.println("max: " + maxUse);

	}
}
