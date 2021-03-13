package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.maintenanceManager.util.DateTimeUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ServiceInputFilterParam {

   private final String key;
   private final String operator;
   private final List values;
   private final Class valueType;


   public ServiceInputFilterParam(String key, String operator, Class valueType, Object[] arrValues) {
      this.key = key;
      this.operator = operator;
      this.values = new ArrayList();
      this.valueType = valueType;
      int arrLen = arrValues.length;

      for(int i = 0; i < arrLen; ++i) {
         this.values.add(arrValues[i]);
      }

   }

   public ServiceInputFilterParam(String key, String operator, Class valueType, Object value) {
      this.key = key;
      this.operator = operator;
      this.values = new ArrayList();
      this.valueType = valueType;
      this.values.add(value);
   }

   public String getKey() {
      return this.key;
   }

   public String getOperator() {
      return this.operator;
   }

   public List getValues() {
      return this.values;
   }

   public Class getValueType() {
      return this.valueType;
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      if(this.valueType.equals(Calendar.class)) {
         builder.append(this.key + " " + this.operator + " ");
         int count = this.values.size();
         builder.append("[");

         for(int i = 0; i < count; ++i) {
            if(i > 0) {
               builder.append(",");
            }

            builder.append(DateTimeUtil.calendarToPrintString((Calendar)this.values.get(i)));
         }

         builder.append("]\n");
      } else {
         builder.append(this.key + " " + this.operator + " " + this.values + "\n");
      }

      builder.append("\n");
      return builder.toString();
   }
}