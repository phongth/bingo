package zgame.bussiness;

import net.sf.ehcache.Element;
import zgame.bean.Provider;
import zgame.dao.ProviderDao;
import zgame.main.Global;
import zgame.socket.DataPackage;
import zgame.socket.ProtocolConstants;
import zgame.socket.ServerConnection;
import zgame.utils.CacheUtil;

public class CheckForUpdateBussiness {
  public static void checkToUpdate(ServerConnection server, DataPackage inputDataPackage) {
    String version = inputDataPackage.nextString();
    int providerId = inputDataPackage.nextInt();

    Provider provider = null;
    Element cacheVersion = CacheUtil.cacheLv3.get(String.valueOf(providerId));
    if (cacheVersion == null) {
      ProviderDao providerDao = ProviderDao.createInstance();
      try {
        provider = providerDao.getProviderById(providerId);
      } finally {
        providerDao.close();
      }

      // Nếu provider này không tồn tại thì yêu cầu client đổi về provider mặc
      // định
      if (provider == null) {
        DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.UPDATE_PROVIDER_RESPONSE);
        dataPackage.putInt(Global.DEFAULT_PROVIDER_ID);
        server.write(dataPackage);
        return;
      }

      cacheVersion = new Element(String.valueOf(providerId), provider);
      CacheUtil.cacheLv3.put(cacheVersion);
    } else {
      provider = (Provider) cacheVersion.getObjectValue();
    }

    if (version.compareTo(provider.getCurrentVersion()) < 0) {
      DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.UPDATE_CLIENT_RESPONSE);
      dataPackage.putString(provider.getJarUrl());
      server.write(dataPackage);
      return;
    }

    // Nếu không cần update version thì báo về client là không cần update
    DataPackage dataPackage = new DataPackage(ProtocolConstants.ResponseHeader.NO_UPDATE_RESPONSE);
    server.write(dataPackage);
  }
}
