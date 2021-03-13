package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.InfoData;
import javax.xml.ws.WebFault;

@WebFault(
   name = "BusinessException",
   targetNamespace = "http://businessService.mesClient.mpdv.de/"
)
public class BusinessException extends Exception {

   private InfoData faultInfo;


   public BusinessException(String message, InfoData faultInfo) {
      super(message);
      this.faultInfo = faultInfo;
   }

   public BusinessException(String message, InfoData faultInfo, Throwable cause) {
      super(message, cause);
      this.faultInfo = faultInfo;
   }

   public InfoData getFaultInfo() {
      return this.faultInfo;
   }
}