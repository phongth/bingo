package zgame.bean;

public class ImageInfo {
  private String fileName;
  private byte[] data;

  public ImageInfo(String fileName, byte[] data) {
    this.fileName = fileName;
    this.data = data;
  }

  public String getFileName() {
    return fileName;
  }

  public byte[] getData() {
    return data;
  }
}
