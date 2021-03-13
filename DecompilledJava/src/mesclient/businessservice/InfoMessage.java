package de.mpdv.mesclient.businessservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "infoMessage",
   propOrder = {"langKey", "params"}
)
public class InfoMessage {

   protected String langKey;
   @XmlElement(
      nillable = true
   )
   protected List params;


   public String getLangKey() {
      return this.langKey;
   }

   public void setLangKey(String value) {
      this.langKey = value;
   }

   public List getParams() {
      if(this.params == null) {
         this.params = new ArrayList();
      }

      return this.params;
   }
}