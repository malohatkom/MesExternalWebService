package de.mpdv.maintenanceManager.data.javaServer;

import de.mpdv.maintenanceManager.data.javaServer.PackageElement.PackageElementType;

public class PackageElement {

   public enum PackageElementType {

   DOMAIN("DOMAIN", 0),
   CUST_DOMAIN("CUST_DOMAIN", 1),
   THIRD_PARTY_LIBS("THIRD_PARTY_LIBS", 2);
   // $FF: synthetic field
   private static final PackageElementType[] $VALUES = new PackageElementType[]{DOMAIN, CUST_DOMAIN, THIRD_PARTY_LIBS};


   private PackageElementType(String var1, int var2) {}

}
    
    
    
   private final String elementName;
   private final PackageElementType elementType;
   private final String customerName;
   private final String elementPathRelative;
   private final String version;
   


   public PackageElement(String elementName, PackageElementType elementType, String customerName, String elementPathRelative, String version) {
      this.elementName = elementName;
      this.elementType = elementType;
      this.customerName = customerName;
      this.elementPathRelative = elementPathRelative;
      this.version = version;
   }

   public String getElementName() {
      return this.elementName;
   }

   public PackageElementType getElementType() {
      return this.elementType;
   }

   public String getCustomerName() {
      return this.customerName;
   }

   public String getElementPathRelative() {
      return this.elementPathRelative;
   }

   public String getVersion() {
      return this.version;
   }

   public String toString() {
      return "PackageElement [elementName=" + this.elementName + ", elementType=" + this.elementType + ", customerName=" + this.customerName + ", elementPathRelative=" + this.elementPathRelative + ", version=" + this.version + "]";
   }
}