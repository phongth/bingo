package zgame.utils;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;

public class CacheUtil {
  private static final CacheManager cacheManager;
  public static Cache cacheLv1;
  public static Cache cacheLv2;
  public static Cache cacheLv3;

  static {
    cacheManager = CacheManager.create();
    cacheLv1 = new Cache(new CacheConfiguration("cacheLv1", 10000).maxElementsOnDisk(1000).eternal(false).overflowToDisk(true)
        .diskSpoolBufferSizeMB(20).timeToIdleSeconds(60).timeToLiveSeconds(120).memoryStoreEvictionPolicy("LFU"));
    cacheLv2 = new Cache(new CacheConfiguration("cacheLv2", 10000).maxElementsOnDisk(1000).eternal(false).overflowToDisk(true)
        .diskSpoolBufferSizeMB(20).timeToIdleSeconds(300).timeToLiveSeconds(600).memoryStoreEvictionPolicy("LFU"));
    cacheLv3 = new Cache(new CacheConfiguration("cacheLv3", 10000).maxElementsOnDisk(1000).eternal(false).overflowToDisk(true)
        .diskSpoolBufferSizeMB(20).timeToIdleSeconds(1800).timeToLiveSeconds(3600).memoryStoreEvictionPolicy("LFU"));
    cacheManager.addCache(cacheLv1);
    cacheManager.addCache(cacheLv2);
    cacheManager.addCache(cacheLv3);
  }
}
