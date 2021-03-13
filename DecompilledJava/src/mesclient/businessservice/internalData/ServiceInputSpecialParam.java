package de.mpdv.mesclient.businessservice.internalData;


public class ServiceInputSpecialParam {

   private final String key;
   private final String operator;
   private final Object value;


   public ServiceInputSpecialParam(String key, String operator, Object value) {
      this.key = key;
      this.operator = operator;
      this.value = value;
   }

   public String getKey() {
      return this.key;
   }

   public String getOperator() {
      return this.operator;
   }

   public Object getValue() {
      return this.value;
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("CombiParam [");
      if(this.key != null) {
         builder.append("key=");
         builder.append(this.key);
         builder.append(", ");
      }

      if(this.operator != null) {
         builder.append("operator=");
         builder.append(this.operator);
         builder.append(", ");
      }

      if(this.value != null) {
         builder.append("value=");
         builder.append(this.value);
      }

      builder.append("]");
      return builder.toString();
   }
}