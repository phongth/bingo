package state.util;

/**
 * @version 0.1
 */
public class RGB {
	public static byte UNIT = (byte) 0x11;
	
	public static int incr(int value, int rank) {
		int highByte = value & 0xFF000000;
		byte r = (byte) ((value & 0x00FF0000) >>> 16);
		byte g = (byte) ((value & 0x0000FF00) >> 8);
		byte b = (byte) (value & 0x000000FF);
		r = incrByte(r, rank);
		g = incrByte(g, rank);
		b = incrByte(b, rank);
		return highByte | (r << 16 & 0x00FF0000) | (g << 8 & 0x0000FF00) | (((int) b) & 0x000000FF);
	}
	
	public static int decr(int value, int rank) {
		int highByte = value & 0xFF000000;
		byte r = (byte) ((value & 0x00FF0000) >>> 16);
		byte g = (byte) ((value & 0x0000FF00) >> 8);
		byte b = (byte) (value & 0x000000FF);
		r = decrByte(r, rank);
		g = decrByte(g, rank);
		b = decrByte(b, rank);
		return highByte | (r << 16 & 0x00FF0000) | (g << 8 & 0x0000FF00) | (((int) b) & 0x000000FF);
	}
	
	private static byte incrByte(byte value, int rank) {
		byte tmp = value;
		tmp += (UNIT * rank);
		return ((tmp & 0xFF) < (value & 0xFF)) ? (byte) 0xFF : tmp;
	}
	
	private static byte decrByte(byte value, int rank) {
		byte tmp = value;
		tmp -= (UNIT * rank);
		return ((tmp & 0xFF) > (value & 0xFF)) ? (byte) 0x00 : tmp;
	}
}
