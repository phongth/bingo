package zgame.dao.impl;

import zgame.bean.Provider;
import zgame.dao.ProviderDao;

public class ProviderDaoImpl extends ProviderDao {
  public Provider getProviderById(int providerId) {
    throw new UnsupportedOperationException("This function is not support");
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException("The method close is not supported");
  }

  @Override
  public void commit() {
    throw new UnsupportedOperationException("The method commit is not supported");
  }
}
