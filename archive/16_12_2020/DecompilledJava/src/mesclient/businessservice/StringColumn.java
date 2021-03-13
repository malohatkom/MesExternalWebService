package de.mpdv.mesclient.businessservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "stringColumn",
   propOrder = {"c", "dummy"}
)
public class StringColumn {

   protected List c;
   protected String dummy;


   public List getC() {
      if(this.c == null) {
         this.c = new ArrayList();
      }

      return this.c;
   }

   public String getDummy() {
      return this.dummy;
   }

   public void setDummy(String value) {
      this.dummy = value;
   }
}