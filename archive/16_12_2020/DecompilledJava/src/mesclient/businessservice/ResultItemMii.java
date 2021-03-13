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
   name = "resultItemMii",
   propOrder = {"additionalInfo", "errorData", "errorMessage", "errorOccurred", "functionId", "resultSetArray"}
)
public class ResultItemMii {

   protected InfoData additionalInfo;
   protected InfoData errorData;
   protected String errorMessage;
   protected boolean errorOccurred;
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

   public InfoData getErrorData() {
      return this.errorData;
   }

   public void setErrorData(InfoData value) {
      this.errorData = value;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public void setErrorMessage(String value) {
      this.errorMessage = value;
   }

   public boolean isErrorOccurred() {
      return this.errorOccurred;
   }

   public void setErrorOccurred(boolean value) {
      this.errorOccurred = value;
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