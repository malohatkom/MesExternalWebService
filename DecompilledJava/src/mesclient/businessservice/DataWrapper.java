package de.mpdv.mesclient.businessservice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "dataWrapper",
   propOrder = {"bn", "bna", "b", "ba", "t", "f", "fa", "d", "da", "n", "na", "s", "sa"}
)
public class DataWrapper {

   protected byte[] bn;
   protected List bna;
   protected Boolean b;
   @XmlElement(
      type = Boolean.class
   )
   protected List ba;
   protected String t;
   protected BigDecimal f;
   protected List fa;
   @XmlSchemaType(
      name = "dateTime"
   )
   protected XMLGregorianCalendar d;
   @XmlSchemaType(
      name = "dateTime"
   )
   protected List da;
   protected Integer n;
   @XmlElement(
      type = Integer.class
   )
   protected List na;
   protected String s;
   protected List sa;


   public byte[] getBn() {
      return this.bn;
   }

   public void setBn(byte[] value) {
      this.bn = (byte[])value;
   }

   public List getBna() {
      if(this.bna == null) {
         this.bna = new ArrayList();
      }

      return this.bna;
   }

   public Boolean isB() {
      return this.b;
   }

   public void setB(Boolean value) {
      this.b = value;
   }

   public List getBa() {
      if(this.ba == null) {
         this.ba = new ArrayList();
      }

      return this.ba;
   }

   public String getT() {
      return this.t;
   }

   public void setT(String value) {
      this.t = value;
   }

   public BigDecimal getF() {
      return this.f;
   }

   public void setF(BigDecimal value) {
      this.f = value;
   }

   public List getFa() {
      if(this.fa == null) {
         this.fa = new ArrayList();
      }

      return this.fa;
   }

   public XMLGregorianCalendar getD() {
      return this.d;
   }

   public void setD(XMLGregorianCalendar value) {
      this.d = value;
   }

   public List getDa() {
      if(this.da == null) {
         this.da = new ArrayList();
      }

      return this.da;
   }

   public Integer getN() {
      return this.n;
   }

   public void setN(Integer value) {
      this.n = value;
   }

   public List getNa() {
      if(this.na == null) {
         this.na = new ArrayList();
      }

      return this.na;
   }

   public String getS() {
      return this.s;
   }

   public void setS(String value) {
      this.s = value;
   }

   public List getSa() {
      if(this.sa == null) {
         this.sa = new ArrayList();
      }

      return this.sa;
   }
}