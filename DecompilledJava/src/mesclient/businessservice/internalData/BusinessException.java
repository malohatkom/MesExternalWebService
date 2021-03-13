package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.maintenanceManager.util.Util;
import de.mpdv.mesclient.businessservice.internalData.InfoData;
import de.mpdv.mesclient.businessservice.internalData.InfoData.Builder;
import javax.xml.ws.WebFault;

@WebFault(
   name = "BusinessException"
)
public class BusinessException extends Exception {

   private static final long serialVersionUID = -689212415357728949L;
   private InfoData error;


   public BusinessException() {}

   public BusinessException(String errorLanguageKey) {
      super(errorLanguageKey);
      this.error = new InfoData(errorLanguageKey, "ERROR", "SYSTEM");
   }

   public BusinessException(String errorLanguageKey, String shortMsg) {
      super(shortMsg);
      this.error = (new Builder("ERROR", "SYSTEM")).message(errorLanguageKey).shortMsg(shortMsg).build();
   }

   public BusinessException(String errorLanguageKey, Throwable exc) {
      super(errorLanguageKey, exc);
      this.error = new InfoData(errorLanguageKey, "ERROR", "SYSTEM");
      InfoData cause;
      if(exc instanceof BusinessException) {
         BusinessException busExc = (BusinessException)exc;
         cause = busExc.error;
      } else {
         cause = new InfoData(Util.exceptionToString(exc), "ERROR", "SYSTEM");
      }

      this.error.setCause(cause);
   }

   public BusinessException(String errorLanguageKey, String shortMsg, Throwable exc) {
      super(shortMsg, exc);
      this.error = new InfoData(errorLanguageKey, "ERROR", "SYSTEM");
      InfoData cause;
      if(exc instanceof BusinessException) {
         BusinessException busExc = (BusinessException)exc;
         cause = busExc.error;
      } else {
         cause = (new Builder("ERROR", "SYSTEM")).message(Util.exceptionToString(exc)).shortMsg(shortMsg).build();
      }

      this.error.setCause(cause);
   }

   public BusinessException(String shortMsg, InfoData error) {
      super(shortMsg);
      this.error = error;
   }

   public BusinessException(String shortMsg, InfoData error, Throwable exc) {
      super(shortMsg, exc);
      this.error = error;
   }

   public String getMessage() {
      return super.getMessage();
   }

   public InfoData getFaultInfo() {
      return this.error;
   }
}