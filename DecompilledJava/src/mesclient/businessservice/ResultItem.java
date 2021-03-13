package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.InfoData;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "resultItem",
   propOrder = {"additionalInfo", "functionId", "resultSetArray"}
)
public class ResultItem {

   protected InfoData additionalInfo;
   protected String functionId;
   @XmlElement(
      nillable = true
   )
   protected List resultSetArray;


   public InfoData getAdditionalInfo() {
      return this.additionalInfo;
   }

   public void setAdditionalInfo(InfoData value) {
      this.additionalInfo = value;
   }

   public String getFunctionId() {
      return this.functionId;
   }

   public void setFunctionId(String value) {
      this.functionId = value;
   }

   public List getResultSetArray() {
      if(this.resultSetArray == null) {
         this.resultSetArray = new ArrayList();
      }

      return this.resultSetArray;
   }
}