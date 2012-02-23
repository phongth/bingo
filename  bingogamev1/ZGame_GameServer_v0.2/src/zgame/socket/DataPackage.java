package zgame.socket;

import java.io.UnsupportedEncodingException;

import zgame.utils.SerialUtil;

public class DataPackage {
  public static final int DEFAULT_PACKAGE_SIZE = 10000; // in byte
  private byte[] data; // 4 bytes đầu là header, 4 byte tiếp theo là độ dài, còn
                       // lại là data
  private int putDataIndex = 8;
  private int getDataIndex = 8;

  public DataPackage(byte[] dataPackage) {
    this.data = dataPackage;
  }

  public DataPackage(byte[] header, byte[] data) {
    this.data = new byte[8 + data.length];
    System.arraycopy(header, 0, this.data, 0, 4);
    System.arraycopy(SerialUtil.serialNumber(data.length), 0, this.data, 4, 4);
    System.arraycopy(data, 0, this.data, 8, data.length);
  }

  public DataPackage(int header) {
    data = new byte[DEFAULT_PACKAGE_SIZE];
    System.arraycopy(SerialUtil.serialNumber(header), 0, data, 0, 4);
  }

  public DataPackage(int header, int size) {
    if (size < 8) {
      throw new IllegalArgumentException("DataPackage :  size must be > 8");
    }
    data = new byte[size];
    System.arraycopy(SerialUtil.serialNumber(header), 0, data, 0, 4);
  }

  public int getHeader() {
    return SerialUtil.getInt(data, 0);
  }

  public void setHeader(int header) {
    System.arraycopy(SerialUtil.serialNumber(header), 0, data, 0, 4);
  }

  public int getDataLength() {
    return SerialUtil.getInt(data, 4);
  }

  public void putByteArray(byte[] bytes) {
    if (data.length - putDataIndex < bytes.length + 4) {
      throw new ArrayIndexOutOfBoundsException("putByteArray: No more space in data package: " + putDataIndex
          + " / require space: " + bytes.length);
    }
    putInt(bytes.length);
    System.arraycopy(bytes, 0, data, putDataIndex, bytes.length);
    putDataIndex += bytes.length;

  }

  public void putInt(int value) {
    if (data.length - putDataIndex < 4) {
      throw new ArrayIndexOutOfBoundsException("putInt: No more space in data package: " + putDataIndex);
    }
    System.arraycopy(SerialUtil.serialNumber(value), 0, data, putDataIndex, 4);
    putDataIndex += 4;
  }

  public void putDouble(double value) {
    if (data.length - putDataIndex < 12) {
      throw new ArrayIndexOutOfBoundsException("putDouble : No more space in data package: " + putDataIndex);
    }
    String str = String.valueOf(value);

    // Lấy vị trí của dấu .
    int tmp = str.indexOf('.');
    if (tmp == -1) {
      tmp = str.length();
    }

    // Lấy độ dài số chữ số sau dấu phẩy
    int tmp1 = str.length() - tmp - 1;
    if (tmp1 == -1) {
      tmp1 = 0;
    }

    // Đưa số double lên thành kiểu long
    long tmp3 = 1;
    for (int i = 0; i < tmp1; i++) {
      tmp3 *= 10;
    }
    long tmp2 = (long) (value * tmp3);
    putLong(tmp2);
    putInt(tmp1);
  }

  public void putLong(long value) {
    if (data.length - putDataIndex < 8) {
      throw new ArrayIndexOutOfBoundsException("putLong : No more space in data package: " + putDataIndex);
    }
    System.arraycopy(SerialUtil.serialNumber(value), 0, data, putDataIndex, 8);
    putDataIndex += 8;
  }

  public void putShort(short value) {
    if (data.length - putDataIndex < 2) {
      throw new ArrayIndexOutOfBoundsException("putShort : No more space in data package: " + putDataIndex);
    }
    System.arraycopy(SerialUtil.serialNumber(value), 0, data, putDataIndex, 2);
    putDataIndex += 2;
  }

  public void putByte(byte value) {
    if (data.length - putDataIndex < 1) {
      throw new ArrayIndexOutOfBoundsException("putByte : No more space in data package: " + putDataIndex);
    }
    data[putDataIndex] = value;
    putDataIndex++;
  }

  public void putString(String value) {
    byte[] byteValue = null;
    try {
      byteValue = value.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      byteValue = value.getBytes();
    }
    if (data.length - putDataIndex < byteValue.length + 4) {
      throw new ArrayIndexOutOfBoundsException("putString : No more space in data package: " + putDataIndex);
    }
    System.arraycopy(SerialUtil.serialNumber(byteValue.length), 0, data, putDataIndex, 4);
    System.arraycopy(byteValue, 0, data, putDataIndex + 4, byteValue.length);
    putDataIndex += (byteValue.length + 4);
  }

  public int nextInt() {
    if (data.length - getDataIndex < 4) {
      throw new ArrayIndexOutOfBoundsException("nextInt: No more space in data package: " + getDataIndex);
    }
    int value = SerialUtil.getInt(data, getDataIndex);
    getDataIndex += 4;
    return value;
  }

  public long nextLong() {
    if (data.length - getDataIndex < 8) {
      throw new ArrayIndexOutOfBoundsException("nextLong: No more space in data package: " + getDataIndex);
    }
    long value = SerialUtil.getLong(data, getDataIndex);
    getDataIndex += 8;
    return value;
  }

  public byte nextByte() {
    if (data.length - getDataIndex < 1) {
      throw new ArrayIndexOutOfBoundsException("nextByte: No more space in data package: " + getDataIndex);
    }
    byte value = data[getDataIndex];
    getDataIndex++;
    return value;
  }

  public short nextShort() {
    if (data.length - getDataIndex < 2) {
      throw new ArrayIndexOutOfBoundsException("nextShort: No more space in data package: " + getDataIndex);
    }
    short value = SerialUtil.getShort(data, getDataIndex);
    getDataIndex += 2;
    return value;
  }

  public String nextString() {
    int len = nextInt();
    if (data.length - getDataIndex < len) {
      throw new ArrayIndexOutOfBoundsException("nextString: No more space in data package: " + getDataIndex);
    }
    String value = SerialUtil.getString(data, getDataIndex, len);
    getDataIndex += len;
    return value;
  }

  public double nextDouble() {
    if (data.length - getDataIndex < 12) {
      throw new ArrayIndexOutOfBoundsException("nextDouble: No more space in data package: " + getDataIndex);
    }
    long tmp1 = nextLong();
    int tmp2 = nextInt();
    long tmp3 = 1;
    for (int i = 0; i < tmp2; i++) {
      tmp3 *= 10;
    }
    return tmp1 * 1.0 / tmp3;
  }

  public byte[] nextByteArray() {
    int len = nextInt();
    if (data.length - getDataIndex < len) {
      throw new ArrayIndexOutOfBoundsException("nextByteArray: No more space in data package: " + getDataIndex);
    }
    byte[] result = new byte[len];
    System.arraycopy(data, getDataIndex, result, 0, len);
    getDataIndex += len;
    return result;
  }

  public byte[] getAllData() {
    System.arraycopy(SerialUtil.serialNumber(putDataIndex - 8), 0, data, 4, 4); // write
                                                                                // the
                                                                                // length
                                                                                // of
                                                                                // data
                                                                                // to
                                                                                // data
                                                                                // package
    byte[] result = new byte[putDataIndex];
    System.arraycopy(data, 0, result, 0, putDataIndex);
    return result;
  }

  public DataPackage clone() {
    return new DataPackage(data);
  }

  public void detroy() {
    data = null;
  }
}
