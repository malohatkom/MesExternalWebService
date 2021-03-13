package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.DataWrapper;
import java.io.Serializable;
import java.util.Arrays;

public class InfoMessage implements Serializable {

   private static final long serialVersionUID = -5318558881195673175L;
   private String langKey;
   private DataWrapper[] params;


   public InfoMessage() {}

   public InfoMessage(String langKey) {
      this.langKey = langKey;
   }

   public InfoMessage(String langKey, DataWrapper[] params) {
      this.langKey = langKey;
      this.params = params;
   }

   public String getLangKey() {
      return this.langKey;
   }

   public void setLangKey(String langKey) {
      this.langKey = langKey;
   }

   public DataWrapper[] getParams() {
      return this.params;
   }

   public void setParams(DataWrapper[] params) {
      this.params = params;
   }

   public int hashCode() {
      boolean prime = true;
      byte result = 1;
      int result1 = 31 * result + (this.langKey == null?0:this.langKey.hashCode());
      result1 = 31 * result1 + Arrays.hashCode(this.params);
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
         InfoMessage other = (InfoMessage)obj;
         if(this.langKey == null) {
            if(other.langKey != null) {
               return false;
            }
         } else if(!this.langKey.equals(other.langKey)) {
            return false;
         }

         return Arrays.equals(this.params, other.params);
      }
   }

   public String toString() {
      String tab = "    \n";
      String retValue = "";
      retValue = "InfoMessage ( " + super.toString() + "    \n" + "langKey = " + this.langKey + "    \n" + "params = " + Arrays.deepToString(this.params) + "    \n" + " )";
      return retValue;
   }
}