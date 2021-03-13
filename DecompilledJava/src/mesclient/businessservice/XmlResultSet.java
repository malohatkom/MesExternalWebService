package de.mpdv.mesclient.businessservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "xmlResultSet",
   propOrder = {"binCols", "binNullCols", "boolCols", "boolNullCols", "m", "decCols", "decNullCols", "dtCols", "dtNullCols", "intCols", "intNullCols", "resultSetId", "strCols", "strNullCols"}
)
public class XmlResultSet {

   @XmlElement(
      nillable = true
   )
   protected List binCols;
   @XmlElement(
      nillable = true
   )
   protected List binNullCols;
   @XmlElement(
      nillable = true
   )
   protected List boolCols;
   @XmlElement(
      nillable = true
   )
   protected List boolNullCols;
   protected List m;
   @XmlElement(
      nillable = true
   )
   protected List decCols;
   @XmlElement(
      nillable = true
   )
   protected List decNullCols;
   @XmlElement(
      nillable = true
   )
   protected List dtCols;
   @XmlElement(
      nillable = true
   )
   protected List dtNullCols;
   @XmlElement(
      nillable = true
   )
   protected List intCols;
   @XmlElement(
      nillable = true
   )
   protected List intNullCols;
   protected String resultSetId;
   @XmlElement(
      nillable = true
   )
   protected List strCols;
   @XmlElement(
      nillable = true
   )
   protected List strNullCols;


   public List getBinCols() {
      if(this.binCols == null) {
         this.binCols = new ArrayList();
      }

      return this.binCols;
   }

   public List getBinNullCols() {
      if(this.binNullCols == null) {
         this.binNullCols = new ArrayList();
      }

      return this.binNullCols;
   }

   public List getBoolCols() {
      if(this.boolCols == null) {
         this.boolCols = new ArrayList();
      }

      return this.boolCols;
   }

   public List getBoolNullCols() {
      if(this.boolNullCols == null) {
         this.boolNullCols = new ArrayList();
      }

      return this.boolNullCols;
   }

   public List getM() {
      if(this.m == null) {
         this.m = new ArrayList();
      }

      return this.m;
   }

   public List getDecCols() {
      if(this.decCols == null) {
         this.decCols = new ArrayList();
      }

      return this.decCols;
   }

   public List getDecNullCols() {
      if(this.decNullCols == null) {
         this.decNullCols = new ArrayList();
      }

      return this.decNullCols;
   }

   public List getDtCols() {
      if(this.dtCols == null) {
         this.dtCols = new ArrayList();
      }

      return this.dtCols;
   }

   public List getDtNullCols() {
      if(this.dtNullCols == null) {
         this.dtNullCols = new ArrayList();
      }

      return this.dtNullCols;
   }

   public List getIntCols() {
      if(this.intCols == null) {
         this.intCols = new ArrayList();
      }

      return this.intCols;
   }

   public List getIntNullCols() {
      if(this.intNullCols == null) {
         this.intNullCols = new ArrayList();
      }

      return this.intNullCols;
   }

   public String getResultSetId() {
      return this.resultSetId;
   }

   public void setResultSetId(String value) {
      this.resultSetId = value;
   }

   public List getStrCols() {
      if(this.strCols == null) {
         this.strCols = new ArrayList();
      }

      return this.strCols;
   }

   public List getStrNullCols() {
      if(this.strNullCols == null) {
         this.strNullCols = new ArrayList();
      }

      return this.strNullCols;
   }
}