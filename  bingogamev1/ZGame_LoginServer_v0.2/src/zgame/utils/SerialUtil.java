package zgame.utils;

import java.io.UnsupportedEncodingException;

/**
 * @version 0.1
 */
public class SerialUtil {

  /**
   * Chuyển 1 giá trị kiểu int về 1 mảng 4 byte
   * 
   * @param intValue
   *          - Giá trị kiểu int cần chuyển
   * @return Mảng 4 byte
   */
  public static byte[] serialNumber(int intValue) {
    byte[] byteArray = new byte[4];
    byteArray[3] = (byte) intValue;
    intValue >>>= 8;
    byteArray[2] = (byte) intValue;
    intValue >>>= 8;
    byteArray[1] = (byte) intValue;
    intValue >>>= 8;
    byteArray[0] = (byte) intValue;
    return byteArray;
  }

  /**
   * Chuyển 1 giá trị kiểu long về 1 mảng 8 byte
   * 
   * @param longValue
   *          - Giá trị kiểu long cần chuyển
   * @return Mảng 8 byte
   */
  public static byte[] serialNumber(long longValue) {
    byte[] byteArray = new byte[8];
    byteArray[7] = (byte) longValue;
    longValue >>>= 8;
    byteArray[6] = (byte) longValue;
    longValue >>>= 8;
    byteArray[5] = (byte) longValue;
    longValue >>>= 8;
    byteArray[4] = (byte) longValue;
    longValue >>>= 8;
    byteArray[3] = (byte) longValue;
    longValue >>>= 8;
    byteArray[2] = (byte) longValue;
    longValue >>>= 8;
    byteArray[1] = (byte) longValue;
    longValue >>>= 8;
    byteArray[0] = (byte) longValue;
    return byteArray;
  }

  /**
   * Chuyển 1 giá trị kiểu short về 1 mảng 2 byte
   * 
   * @param shortValue
   *          - Giá trị kiểu short cần chuyển
   * @return Mảng 2 byte
   */
  public static byte[] serialNumber(short shortValue) {
    byte[] byteArray = new byte[2];
    byteArray[1] = (byte) shortValue;
    shortValue >>>= 8;
    byteArray[0] = (byte) shortValue;
    return byteArray;
  }

  /**
   * Chuyển 1 mảng byte thành 1 số kiểu int
   * 
   * @param byteArray
   *          - Mảng byte cần chuyển
   * @return Giá trị int của mảng byte
   */
  public static int deserialToInt(byte[] byteArray) {
    int value = 0;
    for (int i = 0; i < byteArray.length - 1; i++) {
      value = (value | toInt(byteArray[i])) << 8;
    }
    value = value | toInt(byteArray[byteArray.length - 1]);
    return value;
  }

  /**
   * Chuyển 1 mảng byte thành 1 số kiểu long
   * 
   * @param byteArray
   *          - Mảng byte cần chuyển
   * @return Giá trị long của mảng byte
   */
  public static long deserialToLong(byte[] byteArray) {
    long value = 0;
    value = (value | toInt(byteArray[0])) << 8;
    value = (value | toInt(byteArray[1])) << 8;
    value = (value | toInt(byteArray[2])) << 8;
    value = (value | toInt(byteArray[3])) << 8;
    value = (value | toInt(byteArray[4])) << 8;
    value = (value | toInt(byteArray[5])) << 8;
    value = (value | toInt(byteArray[6])) << 8;
    value = value | toInt(byteArray[7]);
    return value;
  }

  /**
   * Chuyển 1 mảng byte thành 1 số kiểu short
   * 
   * @param byteArray
   *          - Mảng byte cần chuyển
   * @return Giá trị short của mảng byte
   */
  public static short deserialToShort(byte[] byteArray) {
    short value = 0;
    value = (short) ((toInt(value) | toInt(byteArray[0])) << 8);
    value = (short) (toInt(value) | toInt(byteArray[1]));
    return value;
  }

  /**
   * Chuyển 2 byte thành 1 số kiểu short
   * 
   * @param byte1
   *          - Byte thứ nhất
   * @param byte2
   *          - Byte thứ hai
   * @return Giá trị short của 2 byte
   */
  public static short deserialToShort(byte byte1, byte byte2) {
    short value = 0;
    value = (short) ((toInt(value) | toInt(byte1)) << 8);
    value = (short) (toInt(value) | toInt(byte2));
    return value;
  }

  /**
   * Chuyển 1 byte thành 1 số kiểu short
   * 
   * @param byte2
   *          - Byte cần chuyển
   * @return Giá trị short của byte đã cho
   */
  public static short deserialToShort(byte byte2) {
    short value = 0;
    value = (short) (toInt(value) | toInt(byte2));
    return value;
  }

  private static int toInt(byte byteValue) {
    return ((int) byteValue) & 0x000000FF;
  }

  private static int toInt(short shortValue) {
    return ((int) shortValue) & 0x0000FFFF;
  }

  public static short getShort(byte[] data, int fromIndex) {
    byte[] value = new byte[2];
    System.arraycopy(data, fromIndex, value, 0, 2);
    return deserialToShort(value);
  }

  public static int getInt(byte[] data, int fromIndex) {
    byte[] value = new byte[4];
    System.arraycopy(data, fromIndex, value, 0, 4);
    return deserialToInt(value);
  }

  public static long getLong(byte[] data, int fromIndex) {
    byte[] value = new byte[8];
    System.arraycopy(data, fromIndex, value, 0, 8);
    return deserialToLong(value);
  }

  public static String getString(byte[] data, int fromIndex, int len) {
    byte[] value = new byte[len];
    System.arraycopy(data, fromIndex, value, 0, len);
    String str = "";
    try {
      str = new String(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      str = new String(value);
    }
    return str;
  }

  public static byte[] getByteArray(byte[] data, int fromIndex, int len) {
    byte[] value = new byte[len];
    System.arraycopy(data, fromIndex, value, 0, len);
    return value;
  }
}
