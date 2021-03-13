package de.mpdv.mesclient.businessservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "resultStruct",
   propOrder = {"resultItems"}
)
public class ResultStruct {

   @XmlElement(
      nillable = true
   )
   protected List resultItems;


   public List getResultItems() {
      if(this.resultItems == null) {
         this.resultItems = new ArrayList();
      }

      return this.resultItems;
   }
}