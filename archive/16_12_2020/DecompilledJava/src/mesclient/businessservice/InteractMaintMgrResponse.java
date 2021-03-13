package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.ResultItem;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "interactMaintMgrResponse",
   propOrder = {"result"}
)
public class InteractMaintMgrResponse {

   protected ResultItem result;


   public ResultItem getResult() {
      return this.result;
   }

   public void setResult(ResultItem value) {
      this.result = value;
   }
}