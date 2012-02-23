package zgame.bussiness;

import java.util.Collection;

import org.apache.log4j.Logger;

import zgame.bean.Table;
import zgame.bean.User;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.server.ServerConnection;

public abstract class GameComponent {
  private static final Logger log = Logger.getLogger(GameComponent.class);

  protected Table table;

  public GameComponent(Table table) {
    this.table = table;
  }

  public static final DataPackage createPackage(int header) {
    DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.GAME_ACTION_RESPONSE);
    dataPackage.putInt(header);
    return dataPackage;
  }

  public final void onActionPerform(ServerConnection server, DataPackage dataPackage) {
    try {
      dataPackage.setHeader(dataPackage.nextInt()); // Lấy data thứ 2 làm header
    } catch (ArrayIndexOutOfBoundsException ex) {
      log.warn("ERROR : GameComponent : onActionPerform : dataPackage has wrong format");
    }
    onActionPerform(server.user, dataPackage);
  }

  public final void sendMessageToAllUser(DataPackage dataPackage) {
    Collection<User> users = table.getUsers();
    for (User user : users) {
      user.server.write(dataPackage);
    }
  }

  public abstract void onUserLeaveTable(User user);

  public abstract void init();

  protected abstract void onActionPerform(User user, DataPackage inputDataPackage);

  public abstract void detroy();
}
