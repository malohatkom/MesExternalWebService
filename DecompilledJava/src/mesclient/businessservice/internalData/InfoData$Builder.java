package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.InfoData;
import de.mpdv.mesclient.businessservice.internalData.InfoMessage;
//import de.mpdv.mesclient.businessservice.internalData.InfoMessage.Builder;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class Builder {

   private final String level;
   private final String type;
   private final List messages = new ArrayList();
   private Builder currentMessage;
   private InfoData cause;
   private String stackTrace;
   private String shortMsg;


   public Builder(String level, String type) {
      this.level = level;
      this.type = type;
   }

   public Builder shortMsg(String pShortMsg) {
      this.shortMsg = pShortMsg;
      return this;
   }

   public Builder message(String langKey) {
      if(this.currentMessage != null) {
         this.messages.add(this.currentMessage.build());
      }

      this.currentMessage = new Builder(langKey);
      return this;
   }

   public Builder param(String val) {
      if(this.currentMessage == null) {
         throw new IllegalStateException("Try to set parameter for a non-existing message");
      } else {
         this.currentMessage.param(val);
         return this;
      }
   }

   public Builder param(String[] val) {
      if(this.currentMessage == null) {
         throw new IllegalStateException("Try to set parameter for a non-existing message");
      } else {
         this.currentMessage.param(val);
         return this;
      }
   }

   public Builder param(Calendar val) {
      if(this.currentMessage == null) {
         throw new IllegalStateException("Try to set parameter for a non-existing message");
      } else {
         this.currentMessage.param(val);
         return this;
      }
   }

   public Builder param(BigDecimal val) {
      if(this.currentMessage == null) {
         throw new IllegalStateException("Try to set parameter for a non-existing message");
      } else {
         this.currentMessage.param(val);
         return this;
      }
   }

   public Builder param(Integer val) {
      if(this.currentMessage == null) {
         throw new IllegalStateException("Try to set parameter for a non-existing message");
      } else {
         this.currentMessage.param(val);
         return this;
      }
   }

   public Builder param(Boolean val) {
      if(this.currentMessage == null) {
         throw new IllegalStateException("Try to set parameter for a non-existing message");
      } else {
         this.currentMessage.param(val);
         return this;
      }
   }

   public Builder cause(InfoData pCause) {
      this.cause = pCause;
      return this;
   }

   public Builder stackTrace(String pStackTrace) {
      this.stackTrace = pStackTrace;
      return this;
   }

   public InfoData build() {
      InfoData instance = new InfoData();
      instance.setType(this.type);
      instance.setLevel(this.level);
      if(this.currentMessage != null) {
         this.messages.add(this.currentMessage.build());
      }

      int size = this.messages.size();
      InfoMessage[] msgArr = new InfoMessage[size];
      this.messages.toArray(msgArr);
      instance.setMessages(msgArr);
      instance.setCause(this.cause);
      instance.setStackTrace(this.stackTrace);
      instance.setShortMsg(this.shortMsg);
      return instance;
   }
}