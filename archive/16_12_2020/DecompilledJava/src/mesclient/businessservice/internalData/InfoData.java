package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.InfoMessage;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class InfoData implements Serializable {
  
    
   private static final long serialVersionUID = 1806767143194499216L;
   private String level;
   private String type;
   private InfoMessage[] messages;
   private InfoData cause;
   private String stackTrace;
   private String shortMsg;
   
   public InfoData() {}

   public InfoData(String errorLanguageKey, String level) {
      this.messages = new InfoMessage[]{new InfoMessage(errorLanguageKey)};
      this.level = level;
      this.type = "SYSTEM";
   }

   public InfoData(String errorLanguageKey, String level, String type) {
      this.messages = new InfoMessage[]{new InfoMessage(errorLanguageKey)};
      this.level = level;
      this.type = type;
   }

   public String getLevel() {
      return this.level;
   }

   public void setLevel(String level) {
      this.level = level;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public InfoMessage[] getMessages() {
      return this.messages;
   }

   public void setMessages(InfoMessage[] messages) {
      this.messages = messages;
   }

   public InfoData getCause() {
      return this.cause;
   }

   public void setCause(InfoData cause) {
      this.cause = cause;
   }

   public String getStackTrace() {
      return this.stackTrace;
   }

   public void setStackTrace(String stackTrace) {
      this.stackTrace = stackTrace;
   }

   public String getShortMsg() {
      return this.shortMsg;
   }

   public void setShortMsg(String shortMsg) {
      this.shortMsg = shortMsg;
   }

   public int hashCode() {
      boolean prime = true;
      byte result = 1;
      int result1 = 31 * result + (this.cause == null?0:this.cause.hashCode());
      result1 = 31 * result1 + (this.level == null?0:this.level.hashCode());
      result1 = 31 * result1 + Arrays.hashCode(this.messages);
      result1 = 31 * result1 + (this.shortMsg == null?0:this.shortMsg.hashCode());
      result1 = 31 * result1 + (this.stackTrace == null?0:this.stackTrace.hashCode());
      result1 = 31 * result1 + (this.type == null?0:this.type.hashCode());
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
         InfoData other = (InfoData)obj;
         if(this.cause == null) {
            if(other.cause != null) {
               return false;
            }
         } else if(!this.cause.equals(other.cause)) {
            return false;
         }

         if(this.level == null) {
            if(other.level != null) {
               return false;
            }
         } else if(!this.level.equals(other.level)) {
            return false;
         }

         if(!Arrays.equals(this.messages, other.messages)) {
            return false;
         } else {
            if(this.shortMsg == null) {
               if(other.shortMsg != null) {
                  return false;
               }
            } else if(!this.shortMsg.equals(other.shortMsg)) {
               return false;
            }

            if(this.stackTrace == null) {
               if(other.stackTrace != null) {
                  return false;
               }
            } else if(!this.stackTrace.equals(other.stackTrace)) {
               return false;
            }

            if(this.type == null) {
               if(other.type != null) {
                  return false;
               }
            } else if(!this.type.equals(other.type)) {
               return false;
            }

            return true;
         }
      }
   }

   public String toString() {
      String delimiter = "|\n";
      StringBuilder retValue = new StringBuilder();
      retValue.append("InfoData ( ").append("level = ").append(this.level).append("|\n").append("type = ").append(this.type).append("|\n").append("messages = ").append(Arrays.deepToString(this.messages)).append("|\n").append("cause = ").append(this.cause).append("|\n").append("stackTrace = ").append(this.stackTrace).append("|\n").append("shortMsg = ").append(this.shortMsg).append("|\n").append(" )");
      return retValue.toString();
   }
}