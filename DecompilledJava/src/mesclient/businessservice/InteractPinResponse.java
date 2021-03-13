package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.ResultItem;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "interactPinResponse",
   propOrder = {"_return"}
)
public class InteractPinResponse {

   @XmlElement(
      name = "return"
   )
   protected ResultItem _return;


   public ResultItem getReturn() {
      return this._return;
   }

   public void setReturn(ResultItem value) {
      this._return = value;
   }
}