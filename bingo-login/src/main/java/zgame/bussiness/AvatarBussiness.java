package zgame.bussiness;

import java.util.List;

import zgame.bean.ImageInfo;
import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.ServerConnection;

public class AvatarBussiness {
  public static void getCategoryList(ServerConnection server, DataPackage inputDataPackage) {
    server.write(Global.categoryListDataPackage);
  }

  public static void getAvatarListByCategory(ServerConnection server, DataPackage inputDataPackage) {
    String category = inputDataPackage.nextString();
    int index = inputDataPackage.nextInt();

    List<ImageInfo> avatarList = Global.avatarMap.get(category);
    if ((avatarList == null) || (index > avatarList.size() - 1)) {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.AVATAR_NOT_FOUND_RESPONSE);
      server.write(dataPackage);
      return;
    }

    DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.GET_AVATAR_BY_CATEGORY_RESPONSE, 1000000);
    dataPackage.putString(category);
    dataPackage.putInt(index);
    dataPackage.putByteArray(avatarList.get(index).getData());
    server.write(dataPackage);
  }
}
