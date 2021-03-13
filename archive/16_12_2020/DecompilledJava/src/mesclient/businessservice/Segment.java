package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.DataWrapper;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "segment",
   propOrder = {"key", "value"}
)
public class Segment {

   protected String key;
   protected DataWrapper value;


   public String getKey() {
      return this.key;
   }

   public void setKey(String value) {
      this.key = value;
   }

   public DataWrapper getValue() {
      return this.value;
   }

   public void setValue(DataWrapper value) {
      this.value = value;
   }
}