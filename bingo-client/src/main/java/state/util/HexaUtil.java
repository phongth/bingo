package state.util;

/**
 * @version 0.1
 */
public class HexaUtil {

	/**
	 * Chuyển 1 mảng byte thành 1 chuỗi dạng hexa
	 * 
	 * @param byteArray
	 *            - Mảng byte cần chuyển
	 * @param isTrim
	 *            - Có loại bỏ các phần tử 0 ở 2 đầu của chuỗi hexa hay không
	 * @return Chuỗi hexa là kết quả chuyển đổi của mảng byte
	 */
	public static String toHexaString(byte[] byteArray, boolean isTrim) {
		return toHexaString(byteArray, 0, byteArray.length, isTrim);
	}

	/**
	 * Chuyển 1 giá trị kiểu int về chuỗi hexa
	 * 
	 * @param value
	 *            - Giá trị int cần chuyển đổi
	 * @param isTrim
	 *            - Có loại bỏ các phần tử 0 ở 2 đầu của chuỗi hexa hay không
	 * @return Chuỗi hexa là kết quả chuyển đổi của giá trị kiểu int
	 */
	public static String toHexaString(int value, boolean isTrim) {
		byte[] byteArray = SerialUtil.serialNumber(value);
		return toHexaString(byteArray, 0, byteArray.length, isTrim);
	}

	/**
	 * Chuyển 1 phần của mảng byte thành 1 chuỗi dạng hexa
	 * 
	 * @param byteArray
	 *            - Mảng byte cần chuyển
	 * @param off
	 *            - Index của phần tử đầu của đoạn các phần tử cần chuyển đổi
	 *            trong mảng
	 * @param len
	 *            - Số lượng các phần tử cần chuyển đổi
	 * @param isTrim
	 *            - Có loại bỏ các phần tử 0 ở 2 đầu của chuỗi hexa hay không
	 * @return Chuỗi hexa là kết quả chuyển đổi của mảng byte
	 */
	public static String toHexaString(byte[] byteArray, int off, int len,
			boolean isTrim) {
		StringBuffer returnString = new StringBuffer();
		boolean isHaveAnyValue = false;
		int end = off + len;
		for (int i = off; i < end; i++) {
			String high = toChar((byteArray[i] & 0xF0) >>> 4);
			String low = toChar(byteArray[i] & 0x0F);

			if (isTrim && !isHaveAnyValue && high.equals("0")
					&& low.equals("0")) {
				continue;
			}

			isHaveAnyValue = true;
			returnString.append(high);
			returnString.append(low);
			returnString.append(" ");
		}
		return returnString.toString();
	}

	private static String toChar(final int value) {
		if ((-1 < value) && (value < 10)) {
			return String.valueOf(value);
		}
		switch (value) {
		case 10:
			return "A";
		case 11:
			return "B";
		case 12:
			return "C";
		case 13:
			return "D";
		case 14:
			return "E";
		case 15:
			return "F";
		default:
			throw new IllegalArgumentException("Value sai: " + value);
		}
	}

	/**
	 * Chuyển 1 byte thành giá trị chuỗi dạng hexa
	 */
	public static String toString(byte byteValue) {
		StringBuffer returnString = new StringBuffer();
		returnString.append(toChar((byteValue & 0xF0) >>> 4));
		returnString.append(toChar(byteValue & 0x0F));
		return returnString.toString();
	}
}
