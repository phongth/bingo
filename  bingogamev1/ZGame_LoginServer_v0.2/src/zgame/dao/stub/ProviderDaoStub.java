package zgame.dao.stub;

import java.util.HashMap;
import java.util.Map;

import zgame.bean.Provider;
import zgame.dao.ProviderDao;

public class ProviderDaoStub extends ProviderDao {
  private static Map<String, Provider> providerMap = new HashMap<String, Provider>();

  static {
    initMockDB();
  }

  private static void initMockDB() {
    Provider provider = new Provider(1);
    provider.setAccountName("VinaZip");
    provider.setCurrentVersion("0.0.1");
    provider.setJadUrl("http://...ZGame.jad");
    provider.setJarUrl("http://...ZGame.jar");
    providerMap.put(String.valueOf(provider.getId()), provider);
  }

  /**
   * Lấy thông tin tương ứng với providerId từ bảng Provider và bảng
   * Update_Version
   */
  public Provider getProviderById(int providerId) {
    return providerMap.get(String.valueOf(providerId));
  }

  @Override
  public void close() {
  }

  @Override
  public void commit() {
  }
}
