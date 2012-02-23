package zgame.dao;

import zgame.bean.Provider;
import zgame.dao.stub.ProviderDaoStub;

public abstract class ProviderDao extends AbstractDao {
  public static ProviderDao createInstance() {
    return new ProviderDaoStub();
  }

  public abstract Provider getProviderById(int providerId);

  public abstract void commit();

  public abstract void close();
}
