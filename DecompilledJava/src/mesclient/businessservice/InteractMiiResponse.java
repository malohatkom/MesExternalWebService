package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.ResultItemMii;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "interactMiiResponse",
   propOrder = {"result"}
)
public class InteractMiiResponse {

   protected ResultItemMii result;


   public ResultItemMii getResult() {
      return this.result;
   }

   public void setResult(ResultItemMii value) {
      this.result = value;
   }
}