package de.mpdv.maintenanceManager.data.javaServer;


public class VersionComparisonInfo {

   private final String fileName;
   private final String vendor;
   private final String title;
   private final String leftVersionString;
   private final Integer leftMajor;
   private final Integer leftMinor;
   private final Integer leftRevision;
   private String leftChangeDate;
   private String rightVersionString;
   private Integer rightMajor;
   private Integer rightMinor;
   private Integer rightRevision;
   private String rightChangeDate;


   public VersionComparisonInfo(String fileName, String vendor, String title, String leftVersionString, Integer leftMajor, Integer leftMinor, Integer leftRevision, String leftChangeDate, String rightVersionString, Integer rightMajor, Integer rightMinor, Integer rightRevision, String rightChangeDate) {
      this.fileName = fileName;
      this.vendor = vendor;
      this.title = title;
      this.leftVersionString = leftVersionString;
      this.leftMajor = leftMajor;
      this.leftMinor = leftMinor;
      this.leftRevision = leftRevision;
      this.leftChangeDate = leftChangeDate;
      this.rightVersionString = rightVersionString;
      this.rightMajor = rightMajor;
      this.rightMinor = rightMinor;
      this.rightRevision = rightRevision;
      this.rightChangeDate = rightChangeDate;
   }

   public void setRightChangeDate(String rightChangeDate) {
      this.rightChangeDate = rightChangeDate;
   }

   public String getFileName() {
      return this.fileName;
   }

   public String getVendor() {
      return this.vendor;
   }

   public String getTitle() {
      return this.title;
   }

   public String getLeftChangeDate() {
      return this.leftChangeDate;
   }

   public String getRightChangeDate() {
      return this.rightChangeDate;
   }

   public void setLeftChangeDate(String leftChangeDate) {
      this.leftChangeDate = leftChangeDate;
   }

   public String getLeftVersionString() {
      return this.leftVersionString;
   }

   public Integer getLeftMajor() {
      return this.leftMajor;
   }

   public Integer getLeftMinor() {
      return this.leftMinor;
   }

   public Integer getLeftRevision() {
      return this.leftRevision;
   }

   public String getRightVersionString() {
      return this.rightVersionString;
   }

   public Integer getRightMajor() {
      return this.rightMajor;
   }

   public Integer getRightMinor() {
      return this.rightMinor;
   }

   public Integer getRightRevision() {
      return this.rightRevision;
   }

   public void setRightVersionString(String rightVersionString) {
      this.rightVersionString = rightVersionString;
   }

   public void setRightMajor(Integer rightMajor) {
      this.rightMajor = rightMajor;
   }

   public void setRightMinor(Integer rightMinor) {
      this.rightMinor = rightMinor;
   }

   public void setRightRevision(Integer rightRevision) {
      this.rightRevision = rightRevision;
   }
}