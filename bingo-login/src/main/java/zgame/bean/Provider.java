package zgame.bean;

public class Provider {
  private int id;
  private String accountName = "";
  private String currentVersion = "";
  private String jarUrl = "";
  private String jadUrl = "";

  public Provider(int id) {
    this.id = id;
  }

  public String getAccountName() {
    return accountName;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  public int getId() {
    return id;
  }

  public String getCurrentVersion() {
    return currentVersion;
  }

  public void setCurrentVersion(String currentVersion) {
    this.currentVersion = currentVersion;
  }

  public String getJarUrl() {
    return jarUrl;
  }

  public void setJarUrl(String jarUrl) {
    this.jarUrl = jarUrl;
  }

  public String getJadUrl() {
    return jadUrl;
  }

  public void setJadUrl(String jadUrl) {
    this.jadUrl = jadUrl;
  }
}
