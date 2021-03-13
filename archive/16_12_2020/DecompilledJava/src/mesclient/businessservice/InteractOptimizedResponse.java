package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.XmlResultItem;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "interactOptimizedResponse",
   propOrder = {"result"}
)
public class InteractOptimizedResponse {

   protected XmlResultItem result;


   public XmlResultItem getResult() {
      return this.result;
   }

   public void setResult(XmlResultItem value) {
      this.result = value;
   }
}