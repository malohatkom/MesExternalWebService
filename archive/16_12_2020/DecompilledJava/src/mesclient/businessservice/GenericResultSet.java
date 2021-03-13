package de.mpdv.mesclient.businessservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "genericResultSet",
   propOrder = {"m", "resultSetId", "r"}
)
public class GenericResultSet {

   protected List m;
   protected String resultSetId;
   protected List r;


   public List getM() {
      if(this.m == null) {
         this.m = new ArrayList();
      }

      return this.m;
   }

   public String getResultSetId() {
      return this.resultSetId;
   }

   public void setResultSetId(String value) {
      this.resultSetId = value;
   }

   public List getR() {
      if(this.r == null) {
         this.r = new ArrayList();
      }

      return this.r;
   }
}