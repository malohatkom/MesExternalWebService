package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.ResultStruct;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "interactBatchResponse",
   propOrder = {"result"}
)
public class InteractBatchResponse {

   protected ResultStruct result;


   public ResultStruct getResult() {
      return this.result;
   }

   public void setResult(ResultStruct value) {
      this.result = value;
   }
}