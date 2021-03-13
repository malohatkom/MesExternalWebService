package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.AuthToken;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "serviceEnvironment",
   propOrder = {"abortOnError", "auth", "batch", "clientId", "deviceId", "langId", "licenseTokenId", "requestId", "sessionId", "tx", "userId"}
)
public class ServiceEnvironment {

   protected Boolean abortOnError;
   protected AuthToken auth;
   protected Boolean batch;
   protected String clientId;
   protected String deviceId;
   protected String langId;
   protected String licenseTokenId;
   protected String requestId;
   protected String sessionId;
   protected Boolean tx;
   protected String userId;


   public Boolean isAbortOnError() {
      return this.abortOnError;
   }

   public void setAbortOnError(Boolean value) {
      this.abortOnError = value;
   }

   public AuthToken getAuth() {
      return this.auth;
   }

   public void setAuth(AuthToken value) {
      this.auth = value;
   }

   public Boolean isBatch() {
      return this.batch;
   }

   public void setBatch(Boolean value) {
      this.batch = value;
   }

   public String getClientId() {
      return this.clientId;
   }

   public void setClientId(String value) {
      this.clientId = value;
   }

   public String getDeviceId() {
      return this.deviceId;
   }

   public void setDeviceId(String value) {
      this.deviceId = value;
   }

   public String getLangId() {
      return this.langId;
   }

   public void setLangId(String value) {
      this.langId = value;
   }

   public String getLicenseTokenId() {
      return this.licenseTokenId;
   }

   public void setLicenseTokenId(String value) {
      this.licenseTokenId = value;
   }

   public String getRequestId() {
      return this.requestId;
   }

   public void setRequestId(String value) {
      this.requestId = value;
   }

   public String getSessionId() {
      return this.sessionId;
   }

   public void setSessionId(String value) {
      this.sessionId = value;
   }

   public Boolean isTx() {
      return this.tx;
   }

   public void setTx(Boolean value) {
      this.tx = value;
   }

   public String getUserId() {
      return this.userId;
   }

   public void setUserId(String value) {
      this.userId = value;
   }
}