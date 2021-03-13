package de.mpdv.maintenanceManager.data.javaServer;


public class VersionInfo {

   private String fileName;
   private String vendor;
   private String title;
   private Integer major;
   private Integer minor;
   private Integer revision;
   private String versionString;
   private String changeDate;


   public VersionInfo(String fileName, String vendor, String title, String versionString, String changeDate, Integer major, Integer minor, Integer revision) {
      this.fileName = fileName;
      this.vendor = vendor;
      this.title = title;
      this.versionString = versionString;
      this.changeDate = changeDate;
      this.major = major;
      this.minor = minor;
      this.revision = revision;
   }

   public String getFileName() {
      return this.fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }

   public String getVendor() {
      return this.vendor;
   }

   public void setVendor(String vendor) {
      this.vendor = vendor;
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String title) {
      this.title = title;
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

   public String getChangeDate() {
      return this.changeDate;
   }

   public void setChangeDate(String changeDate) {
      this.changeDate = changeDate;
   }

   public String toString() {
      return "VersionInfo [fileName=" + this.fileName + ", vendor=" + this.vendor + ", title=" + this.title + ", major=" + this.major + ", minor=" + this.minor + ", revision=" + this.revision + ", versionString=" + this.versionString + ", changeDate=" + this.changeDate + "]";
   }
}