package de.mpdv.mesclient.businessservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "genericParam",
   propOrder = {"colMetas", "functionId", "rows", "segments"}
)
public class GenericParam {

   @XmlElement(
      nillable = true
   )
   protected List colMetas;
   protected String functionId;
   @XmlElement(
      nillable = true
   )
   protected List rows;
   @XmlElement(
      nillable = true
   )
   protected List segments;


   public List getColMetas() {
      if(this.colMetas == null) {
         this.colMetas = new ArrayList();
      }

      return this.colMetas;
   }

   public String getFunctionId() {
      return this.functionId;
   }

   public void setFunctionId(String value) {
      this.functionId = value;
   }

   public List getRows() {
      if(this.rows == null) {
         this.rows = new ArrayList();
      }

      return this.rows;
   }

   public List getSegments() {
      if(this.segments == null) {
         this.segments = new ArrayList();
      }

      return this.segments;
   }
}