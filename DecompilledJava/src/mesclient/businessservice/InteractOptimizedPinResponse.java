package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.XmlResultItem;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "interactOptimizedPinResponse",
   propOrder = {"_return"}
)
public class InteractOptimizedPinResponse {

   @XmlElement(
      name = "return"
   )
   protected XmlResultItem _return;


   public XmlResultItem getReturn() {
      return this._return;
   }

   public void setReturn(XmlResultItem value) {
      this._return = value;
   }
}