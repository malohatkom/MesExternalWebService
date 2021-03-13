package de.mpdv.mesclient.businessservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "inputRow",
   propOrder = {"cells", "dummy"}
)
public class InputRow {

   @XmlElement(
      nillable = true
   )
   protected List cells;
   protected String dummy;


   public List getCells() {
      if(this.cells == null) {
         this.cells = new ArrayList();
      }

      return this.cells;
   }

   public String getDummy() {
      return this.dummy;
   }

   public void setDummy(String value) {
      this.dummy = value;
   }
}