package de.mpdv.mesclient.businessservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "inputColumnMeta",
   propOrder = {"dataType", "key"}
)
public class InputColumnMeta {

   protected String dataType;
   protected String key;


   public String getDataType() {
      return this.dataType;
   }

   public void setDataType(String value) {
      this.dataType = value;
   }

   public String getKey() {
      return this.key;
   }

   public void setKey(String value) {
      this.key = value;
   }
}