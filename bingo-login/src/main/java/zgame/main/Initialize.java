package zgame.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import zgame.bean.ImageInfo;
import zgame.dao.impl.ConnectionManager;
import zgame.utils.CacheUtil;

public class Initialize {
  private static Logger log = Logger.getLogger(Initialize.class);

  private static Properties properties;

  public static void init() throws IOException {
    // Init log4J
    Properties logProperties = new Properties();
    logProperties.load(new FileInputStream("conf/log4j.properties"));
    PropertyConfigurator.configure(logProperties);
    log.info("Init log4j done");

    properties = new Properties();
    properties.load(new FileInputStream("conf/server.properties"));

    Global.DEBUG_MODE = Boolean.parseBoolean(get("DEBUG_MODE"));

    Global.PORT = Integer.parseInt(get("PORT"));
    Global.SERVICE_CONTROL_PORT = Integer.parseInt(get("SERVICE_CONTROL_PORT"));
    Global.MAX_POOL = Integer.parseInt(get("MAX_POOL"));
    Global.TIME_OUT = Integer.parseInt(get("TIME_OUT"));
    Global.HEART_BREATH_SEQUENCE_TIME = Integer.parseInt(get("HEART_BREATH_SEQUENCE_TIME"));
    Global.DATA_UPDATE_SEQUENCE_TIME = Integer.parseInt(get("DATA_UPDATE_SEQUENCE_TIME"));

    Global.SESSION_TIME_OUT = Long.parseLong(get("SESSION_TIME_OUT"));

    Global.DB_USER = String.valueOf(get("DB_USER"));
    Global.DB_PASSWORD = String.valueOf(get("DB_PASS"));
    Global.DB_URL = String.valueOf(get("DB_URL"));
    Global.DB_DRIVER = String.valueOf(get("DB_DRIVER"));
    Global.DB_CONNECTION_MAX_POOL = Integer.parseInt(get("DB_CONNECTION_MAX_POOL"));
    Global.DB_CONNECTION_TIME_OUT = Integer.parseInt(get("DB_CONNECTION_TIME_OUT"));
    Global.DB_COMMIT_PAYMENT_DATA_SEQUENCE_TIME = Integer.parseInt(get("DB_COMMIT_PAYMENT_DATA_SEQUENCE_TIME"));

    Global.DEFAULT_PROVIDER_ID = Integer.parseInt(get("DEFAULT_PROVIDER_ID"));
    Global.TAX_PERCENT = Integer.parseInt(get("TAX_PERCENT"));

    Global.USER_NAME_MIN_LEN = Integer.parseInt(get("USER_NAME_MIN_LEN"));
    Global.USER_NAME_MAX_LEN = Integer.parseInt(get("USER_NAME_MAX_LEN"));

    Global.PASSWORD_MIN_LEN = Integer.parseInt(get("PASSWORD_MIN_LEN"));
    Global.PASSWORD_MAX_LEN = Integer.parseInt(get("PASSWORD_MAX_LEN"));
    
    Global.REQUEST_MANAGER_CORE_POLL_SIZE = Integer.parseInt(get("REQUEST_MANAGER_CORE_POLL_SIZE"));
    Global.REQUEST_MANAGER_MAX_POLL_SIZE = Integer.parseInt(get("REQUEST_MANAGER_MAX_POLL_SIZE"));
    Global.REQUEST_MANAGER_KEEP_ALIVE_TIME = Integer.parseInt(get("REQUEST_MANAGER_KEEP_ALIVE_TIME"));

    properties = null;
  }

  public static void initForDB() {
    ConnectionManager.init();
    log.info("Init DB done");

    Global.commitPaymentDataJob.setDelay(Global.DB_COMMIT_PAYMENT_DATA_SEQUENCE_TIME).start();
    Global.checkToReleaseTimeOutDBConnectionJob.setDelay(Global.DB_CONNECTION_TIME_OUT / 2).start();
  }

  public static void initForCache() {
    Element element = new Element("initCacheTime", String.valueOf(System.currentTimeMillis()));
    CacheUtil.cacheLv1.put(element);
    CacheUtil.cacheLv2.put(element);
    CacheUtil.cacheLv3.put(element);
  }

  private static String get(String id) {
    String value = properties.getProperty(id);
    if (value == null) {
      throw new IllegalArgumentException("Property " + id + " is not exist or invalid. Plz check your config file");
    }
    return value;
  }

  public static void loadImageToCatche(String path) {
    File folder = new File(path);
    File[] files = folder.listFiles();
    for (File file : files) {
      String fileName = file.getName();
      if (fileName.equals("Thumbs.db") || fileName.equals(".svn")) {
        continue;
      }

      FileInputStream fis = null;
      try {
        fis = new FileInputStream(file);
        byte[] data = new byte[fis.available()];
        fis.read(data);
        Global.imagesMap.put(fileName, new ImageInfo(fileName, data));
      } catch (IOException e) {
        log.warn("Can not load image " + file.getAbsolutePath(), e);
      } finally {
        if (fis != null) {
          try {
            fis.close();
          } catch (IOException e) {
          }
        }
      }
    }
    log.info("Load resource done");
  }

  public static void loadAvatarToCatche(String path) {
    File folder = new File(path);
    File[] files = folder.listFiles();
    List<String> avatarCategoryList = new ArrayList<String>();
    for (File category : files) {
      List<ImageInfo> imageInfos = new ArrayList<ImageInfo>();
      String categoryName = category.getName();
      if (categoryName.equals("Thumbs.db") || categoryName.equals(".svn")) {
        continue;
      }

      File[] avatarArray = category.listFiles();
      for (File avatar : avatarArray) {
        String fileName = avatar.getName();
        if (fileName.equals("Thumbs.db") || fileName.equals(".svn")) {
          continue;
        }

        FileInputStream fis = null;
        try {
          fis = new FileInputStream(avatar);
          byte[] data = new byte[fis.available()];
          fis.read(data);
          imageInfos.add(new ImageInfo(fileName, data));
        } catch (IOException e) {
          log.warn("Can not load the avatar " + avatar.getAbsolutePath(), e);
        } finally {
          if (fis != null) {
            try {
              fis.close();
            } catch (IOException e) {
            }
          }
        }
      }
      Global.avatarMap.put(categoryName, imageInfos);
      avatarCategoryList.add(categoryName);
    }

    for (int i = 0; i < avatarCategoryList.size(); i++) {
      String categoryName = avatarCategoryList.get(i);
      List<ImageInfo> imageInfos = Global.avatarMap.get(categoryName);
      Global.categoryListDataPackage.putString(categoryName);
      Global.categoryListDataPackage.putInt(imageInfos.size());
    }
    log.info("Load avatar done");
  }
}
