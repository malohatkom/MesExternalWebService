package de.mpdv.mesclient.businessservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "infoData",
   propOrder = {"cause", "level", "messages", "shortMsg", "stackTrace", "type"}
)
public class InfoData {

   protected InfoData cause;
   protected String level;
   @XmlElement(
      nillable = true
   )
   protected List messages;
   protected String shortMsg;
   protected String stackTrace;
   protected String type;


   public InfoData getCause() {
      return this.cause;
   }

   public void setCause(InfoData value) {
      this.cause = value;
   }

   public String getLevel() {
      return this.level;
   }

   public void setLevel(String value) {
      this.level = value;
   }

   public List getMessages() {
      if(this.messages == null) {
         this.messages = new ArrayList();
      }

      return this.messages;
   }

   public String getShortMsg() {
      return this.shortMsg;
   }

   public void setShortMsg(String value) {
      this.shortMsg = value;
   }

   public String getStackTrace() {
      return this.stackTrace;
   }

   public void setStackTrace(String value) {
      this.stackTrace = value;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String value) {
      this.type = value;
   }
}