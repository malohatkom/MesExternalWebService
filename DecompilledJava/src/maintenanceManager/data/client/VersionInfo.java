package de.mpdv.maintenanceManager.data.client;


public class VersionInfo {

   private final String domain;
   private Integer major;
   private Integer minor;
   private Integer revision;
   private final String customerFlag;
   private String versionString;


   public VersionInfo(String domain, String versionString, Integer major, Integer minor, Integer revision, String customerFlag) {
      this.domain = domain;
      this.versionString = versionString;
      this.major = major;
      this.minor = minor;
      this.revision = revision;
      this.customerFlag = customerFlag;
   }

   public Integer getMajor() {
      return this.major;
   }

   public void setMajor(Integer major) {
      this.major = major;
   }

   public Integer getMinor() {
      return this.minor;
   }

   public void setMinor(Integer minor) {
      this.minor = minor;
   }

   public Integer getRevision() {
      return this.revision;
   }

   public void setRevision(Integer revision) {
      this.revision = revision;
   }

   public String getVersionString() {
      return this.versionString;
   }

   public void setVersionString(String versionString) {
      this.versionString = versionString;
   }

   public String getDomain() {
      return this.domain;
   }

   public String getCustomerFlag() {
      return this.customerFlag;
   }

   public String toString() {
      return "VersionInfo [domain=" + this.domain + ", major=" + this.major + ", minor=" + this.minor + ", revision=" + this.revision + ", customerFlag=" + this.customerFlag + ", versionString=" + this.versionString + "]";
   }
}