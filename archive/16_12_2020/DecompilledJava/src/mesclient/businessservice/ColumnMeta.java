package de.mpdv.mesclient.businessservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "columnMeta",
   propOrder = {"cp", "t", "k"}
)
public class ColumnMeta {

   protected String cp;
   protected String t;
   protected String k;


   public String getCp() {
      return this.cp;
   }

   public void setCp(String value) {
      this.cp = value;
   }

   public String getT() {
      return this.t;
   }

   public void setT(String value) {
      this.t = value;
   }

   public String getK() {
      return this.k;
   }

   public void setK(String value) {
      this.k = value;
   }
}