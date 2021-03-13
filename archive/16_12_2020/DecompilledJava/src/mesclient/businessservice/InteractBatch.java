package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.ServiceEnvironment;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "interactBatch",
   propOrder = {"env", "param"}
)
public class InteractBatch {

   protected ServiceEnvironment env;
   @XmlElement(
      nillable = true
   )
   protected List param;


   public ServiceEnvironment getEnv() {
      return this.env;
   }

   public void setEnv(ServiceEnvironment value) {
      this.env = value;
   }

   public List getParam() {
      if(this.param == null) {
         this.param = new ArrayList();
      }

      return this.param;
   }
}