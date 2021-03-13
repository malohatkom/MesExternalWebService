package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.DataTypes.WebServiceType;
import java.io.Serializable;

public class DataTableColumnMeta implements Serializable {

   private static final long serialVersionUID = -2319663698170288038L;
   private String colName;
   private WebServiceType colWsType;


   public DataTableColumnMeta(String colName, WebServiceType colWsType) {
      this.colName = colName;
      this.colWsType = colWsType;
   }

   public DataTableColumnMeta(DataTableColumnMeta original) {
      if(original == null) {
         throw new NullPointerException("Data table column meta to copy is null");
      } else {
         this.colName = original.colName;
         this.colWsType = original.colWsType;
      }
   }

   public String getColName() {
      return this.colName;
   }

   public void setColName(String colName) {
      this.colName = colName;
   }

   public WebServiceType getColWsType() {
      return this.colWsType;
   }

   public void setColWsType(WebServiceType colWsType) {
      this.colWsType = colWsType;
   }

   public String toString() {
      return "DataTableColumnMeta [colName=" + this.colName + ", colWsType=" + this.colWsType + "]";
   }

   public String toTabularString() {
      String colDelimiter = "|";
      StringBuilder result = new StringBuilder();
      result.append(this.colName);
      result.append("(" + this.colWsType + ")");
      result.append("|");
      return result.toString();
   }

   public int hashCode() {
      boolean prime = true;
      byte result = 1;
      int result1 = 31 * result + (this.colName == null?0:this.colName.hashCode());
      result1 = 31 * result1 + (this.colWsType == null?0:this.colWsType.hashCode());
      return result1;
   }

   public boolean equals(Object obj) {
      if(this == obj) {
         return true;
      } else if(obj == null) {
         return false;
      } else if(this.getClass() != obj.getClass()) {
         return false;
      } else {
         DataTableColumnMeta other = (DataTableColumnMeta)obj;
         if(this.colName == null) {
            if(other.colName != null) {
               return false;
            }
         } else if(!this.colName.equals(other.colName)) {
            return false;
         }

         if(this.colWsType == null) {
            if(other.colWsType != null) {
               return false;
            }
         } else if(!this.colWsType.equals(other.colWsType)) {
            return false;
         }

         return true;
      }
   }
}