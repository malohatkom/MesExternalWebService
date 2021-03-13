package de.mpdv.mesclient.businessservice.internalData;


enum WebServiceType {

   STRING("STRING", 0, "string"),
   DATETIME("DATETIME", 1, "datetime"),
   DECIMAL("DECIMAL", 2, "decimal"),
   INTEGER("INTEGER", 3, "integer"),
   BOOLEAN("BOOLEAN", 4, "boolean"),
   BINARY("BINARY", 5, "binary"),
   A_STRING("A_STRING", 6, "string[]"),
   A_DATETIME("A_DATETIME", 7, "datetime[]"),
   A_DECIMAL("A_DECIMAL", 8, "decimal[]"),
   A_INTEGER("A_INTEGER", 9, "integer[]"),
   A_BOOLEAN("A_BOOLEAN", 10, "boolean[]"),
   A_BINARY("A_BINARY", 11, "binary[]");
   private final String strDataType;
   // $FF: synthetic field
   private static final WebServiceType[] $VALUES = new DataTypes$WebServiceType[]{STRING, DATETIME, DECIMAL, INTEGER, BOOLEAN, BINARY, A_STRING, A_DATETIME, A_DECIMAL, A_INTEGER, A_BOOLEAN, A_BINARY};


   private WebServiceType(String var1, int var2, String strDataType) {
      this.strDataType = strDataType;
   }

   public String getStrDataType() {
      return this.strDataType;
   }

}