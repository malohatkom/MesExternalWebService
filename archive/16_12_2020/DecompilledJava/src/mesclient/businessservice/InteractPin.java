package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.GenericParam;
import de.mpdv.mesclient.businessservice.ServiceEnvironment;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "interactPin",
   propOrder = {"arg0", "arg1"}
)
public class InteractPin {

   protected ServiceEnvironment arg0;
   protected GenericParam arg1;


   public ServiceEnvironment getArg0() {
      return this.arg0;
   }

   public void setArg0(ServiceEnvironment value) {
      this.arg0 = value;
   }

   public GenericParam getArg1() {
      return this.arg1;
   }

   public void setArg1(GenericParam value) {
      this.arg1 = value;
   }
}