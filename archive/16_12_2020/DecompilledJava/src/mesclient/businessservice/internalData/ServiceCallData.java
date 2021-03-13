package de.mpdv.mesclient.businessservice.internalData;

import java.util.List;
import java.util.Map;

public class ServiceCallData {

   private final String functionId;
   private final String licenseToken;
   private final String sessionId;
   private final Integer clientId;
   private final String tomcatHostPort;
   private final List requestedColumns;
   private final List filterCriterias;
   private final List specialParams;
   private final Map directParams;


   public ServiceCallData(String functionId, List requestedColumns, List filterCriterias, List specialParams, Map directParams, String licenseToken, String sessionId, Integer clientId, String tomcatHostPort) {
      this.functionId = functionId;
      this.requestedColumns = requestedColumns;
      this.filterCriterias = filterCriterias;
      this.specialParams = specialParams;
      this.directParams = directParams;
      this.licenseToken = licenseToken;
      this.sessionId = sessionId;
      this.clientId = clientId;
      this.tomcatHostPort = tomcatHostPort;
   }

   public String getFunctionId() {
      return this.functionId;
   }

   public List getRequestedColumns() {
      return this.requestedColumns;
   }

   public List getFilterCriterias() {
      return this.filterCriterias;
   }

   public List getSpecialParams() {
      return this.specialParams;
   }

   public Map getDirectParams() {
      return this.directParams;
   }

   public String getLicenseToken() {
      return this.licenseToken;
   }

   public String getSessionId() {
      return this.sessionId;
   }

   public Integer getClientId() {
      return this.clientId;
   }

   public String getTomcatHostPort() {
      return this.tomcatHostPort;
   }

   public String toString() {
      return "ServiceCallData [functionId=" + this.functionId + ", licenseToken=" + this.licenseToken + ", sessionId=" + this.sessionId + ", clientId=" + this.clientId + ", tomcatHostPort=" + this.tomcatHostPort + ", requestedColumns=" + this.requestedColumns + ", filterCriterias=" + this.filterCriterias + ", specialParams=" + this.specialParams + ", directParams=" + this.directParams + "]";
   }
}