package de.mpdv.maintenanceManager.data.client;


public class VersionComparisonInfo {

   private final String domain;
   private final String leftVersionString;
   private final Integer leftMajor;
   private final Integer leftMinor;
   private final Integer leftRevision;
   private final String leftCustomerFlag;
   private String rightVersionString;
   private Integer rightMajor;
   private Integer rightMinor;
   private Integer rightRevision;
   private String rightCustomerFlag;


   public VersionComparisonInfo(String domain, String leftVersionString, Integer leftMajor, Integer leftMinor, Integer leftRevision, String leftCustomerFlag, String rightVersionString, Integer rightMajor, Integer rightMinor, Integer rightRevision, String rightCustomerFlag) {
      this.domain = domain;
      this.leftVersionString = leftVersionString;
      this.leftMajor = leftMajor;
      this.leftMinor = leftMinor;
      this.leftRevision = leftRevision;
      this.leftCustomerFlag = leftCustomerFlag;
      this.rightVersionString = rightVersionString;
      this.rightMajor = rightMajor;
      this.rightMinor = rightMinor;
      this.rightRevision = rightRevision;
      this.rightCustomerFlag = rightCustomerFlag;
   }

   public String getRightVersionString() {
      return this.rightVersionString;
   }

   public void setRightVersionString(String rightVersionString) {
      this.rightVersionString = rightVersionString;
   }

   public Integer getRightMajor() {
      return this.rightMajor;
   }

   public void setRightMajor(Integer rightMajor) {
      this.rightMajor = rightMajor;
   }

   public Integer getRightMinor() {
      return this.rightMinor;
   }

   public void setRightMinor(Integer rightMinor) {
      this.rightMinor = rightMinor;
   }

   public Integer getRightRevision() {
      return this.rightRevision;
   }

   public void setRightRevision(Integer rightRevision) {
      this.rightRevision = rightRevision;
   }

   public String getRightCustomerFlag() {
      return this.rightCustomerFlag;
   }

   public void setRightCustomerFlag(String rightCustomerFlag) {
      this.rightCustomerFlag = rightCustomerFlag;
   }

   public String getDomain() {
      return this.domain;
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

   public String getLeftCustomerFlag() {
      return this.leftCustomerFlag;
   }
}