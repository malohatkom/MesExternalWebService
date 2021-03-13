package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.GenericParam;
import de.mpdv.mesclient.businessservice.ServiceEnvironment;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "interactOptimized",
   propOrder = {"env", "param"}
)
public class InteractOptimized {

   protected ServiceEnvironment env;
   protected GenericParam param;


   public ServiceEnvironment getEnv() {
      return this.env;
   }

   public void setEnv(ServiceEnvironment value) {
      this.env = value;
   }

   public GenericParam getParam() {
      return this.param;
   }

   public void setParam(GenericParam value) {
      this.param = value;
   }
}