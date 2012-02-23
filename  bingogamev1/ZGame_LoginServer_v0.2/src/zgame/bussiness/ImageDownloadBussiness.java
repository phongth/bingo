package zgame.bussiness;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.ServerConnection;

public class ImageDownloadBussiness {
  public static final int NUMBER_OF_RESOURCE_PER_PACKAGE = 10;

  public static void downloadedResourceList(ServerConnection server, DataPackage inputDataPackage) {

    // Lấy ra danh sách những resource mà người dùng đã download
    int numberOfDownloadedResource = inputDataPackage.nextInt();
    List<String> downloadedResourceList = new ArrayList<String>();
    for (int i = 0; i < numberOfDownloadedResource; i++) {
      downloadedResourceList.add(inputDataPackage.nextString());
    }

    // Kiểm tra và lọc ra danh sách những resource mà người dùng chưa download
    List<String> needToDownloadResource = new ArrayList<String>();
    Set<String> imageNames = Global.imagesMap.keySet();
    for (String imageName : imageNames) {
      boolean isExsit = false;
      for (String downloadedImage : downloadedResourceList) {
        if (imageName.equals(downloadedImage)) {
          isExsit = true;
          break;
        }
      }
      if (!isExsit) {
        needToDownloadResource.add(imageName);
      }
    }

    DataPackage totalImageDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.TOTAL_DOWNLOAD_IMAGE_RESPONSE);
    totalImageDataPackage.putInt(needToDownloadResource.size());
    server.write(totalImageDataPackage);

    // Gửi danh sách những resource người dùng chưa download về client
    if (needToDownloadResource.size() > 0) {
      int index = -1;
      while (index + 1 < needToDownloadResource.size()) {
        int maxInddex = index + NUMBER_OF_RESOURCE_PER_PACKAGE + 1;
        maxInddex = Math.min(maxInddex, needToDownloadResource.size());
        int size = maxInddex - index - 1;
        DataPackage outDataPackage = new DataPackage(ProtocolConstants.ResponseHeader.RESOURCE_RESPONSE, 1000000);
        outDataPackage.putInt(size);
        for (int i = index + 1; i < maxInddex; i++) {
          outDataPackage.putString(needToDownloadResource.get(i));
          outDataPackage.putByteArray(Global.imagesMap.get(needToDownloadResource.get(i)).getData());
        }
        index = maxInddex - 1;
        server.write(outDataPackage);
      }
    }

    // Báo download resource done
    server.write(new DataPackage(ProtocolConstants.ResponseHeader.DOWNLOAD_RESOURCE_DONE_RESPONSE));
  }
}
