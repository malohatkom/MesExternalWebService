package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.XmlResultStruct;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "interactOptimizedBatchResponse",
   propOrder = {"result"}
)
public class InteractOptimizedBatchResponse {

   protected XmlResultStruct result;


   public XmlResultStruct getResult() {
      return this.result;
   }

   public void setResult(XmlResultStruct value) {
      this.result = value;
   }
}