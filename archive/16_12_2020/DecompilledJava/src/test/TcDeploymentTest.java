package de.mpdv.test;

import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.Util;
import de.mpdv.test.HttpCallResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;

public class TcDeploymentTest {

    
   private static final String TC_USER = "ad";
   private static final String TC_PASS = "pw";


   public static void main(String[] args) {
      if(checkUndeploymentNeeded("MocServices", "localhost", 8085)) {
         undeployFromTomcat("MocServices", "localhost", 8085);
      }

      deployToTomcat("MocServices", "localhost", 8085, new File("d:\\Build\\install\\dist\\MocServices.war"));
   }

   private static void deployToTomcat(String warName, String host, int port, File warFile) {
      byte[] data;
      try {
         data = FileSystemUtil.fileToByteArray(warFile);
      } catch (IOException var8) {
         throw new RuntimeException("Could not read war file " + warFile.getAbsolutePath() + " to memory", var8);
      }

      HttpCallResult result = null;

      try {
         result = callUrl(host, port, "ad", "pw", "/manager/deploy?path=/" + warName, data);
      } catch (Exception var7) {
         throw new RuntimeException("Could not deploy " + warName + ".", var7);
      }

      if(result.retCode != 200) {
         throw new RuntimeException("Could not deploy " + warName + ". Return code was: " + result.retCode + ", response was: " + result.response);
      } else if(result.response.startsWith("FAIL")) {
         throw new RuntimeException("Error at deployment of " + warName + ". Response from server was: " + result.response);
      }
   }

   private static void undeployFromTomcat(String warName, String host, int port) {
      HttpCallResult result = null;

      try {
         result = callUrl(host, port, "ad", "pw", "/manager/undeploy?path=/" + warName, (byte[])null);
      } catch (Exception var5) {
         throw new RuntimeException("Could not undeploy " + warName + ".", var5);
      }

      if(result.retCode != 200) {
         throw new RuntimeException("Could not undeploy " + warName + ". Return code was: " + result.retCode + ", response was: " + result.response);
      } else if(result.response.startsWith("FAIL")) {
         throw new RuntimeException("Error at undeployment of " + warName + ". Response from server was: " + result.response);
      }
   }

   private static boolean checkUndeploymentNeeded(String warName, String host, int port) {
      HttpCallResult result = null;

      try {
         result = callUrl(host, port, "ad", "pw", "/manager/list", (byte[])null);
      } catch (Exception var8) {
         throw new RuntimeException("Could not determine if Undeployment of " + warName + " is needed.", var8);
      }

      if(result.retCode != 200) {
         throw new RuntimeException("Could not determine if Undeployment of " + warName + " is needed. Return code was: " + result.retCode + ", response was: " + result.response);
      } else {
         String[] lines = result.response.split("\\n");
         int len = lines.length;

         for(int i = 0; i < len; ++i) {
            String line = lines[i];
            if(line.startsWith("/" + warName + ":")) {
               return true;
            }
         }

         return false;
      }
   }

   private static HttpCallResult callUrl(String host, int port, String user, String pass, String url, byte[] requestData) throws IOException {
      String realUrl = "http://" + host + ":" + port + url;
      HttpClient client = new HttpClient();
      Object method = null;
      if(requestData == null) {
         method = new GetMethod(realUrl);
      } else {
         method = new PutMethod(realUrl);
      }

      ((HttpMethod)method).getParams().setParameter("http.method.retry-handler", new DefaultHttpMethodRetryHandler(3, false));
      if(!Util.stringNullOrEmpty(user) && !Util.stringNullOrEmpty(pass)) {
         client.getParams().setAuthenticationPreemptive(true);
         UsernamePasswordCredentials result = new UsernamePasswordCredentials(user, pass);
         client.getState().setCredentials(new AuthScope(host, port, AuthScope.ANY_REALM), result);
      }

      if(requestData != null) {
         ((PutMethod)method).setRequestEntity(new ByteArrayRequestEntity(requestData));
      }

      try {
         HttpCallResult result1 = new HttpCallResult();
         int statusCode = client.executeMethod((HttpMethod)method);
         result1.retCode = statusCode;
         String respCharSet = null;
         if(requestData == null) {
            respCharSet = ((GetMethod)method).getResponseCharSet();
         } else {
            respCharSet = ((PutMethod)method).getResponseCharSet();
         }

         InputStream respStream = ((HttpMethod)method).getResponseBodyAsStream();
         ByteArrayOutputStream outStr = new ByteArrayOutputStream();
         boolean read = true;

         int read1;
         while((read1 = respStream.read()) != -1) {
            outStr.write(read1);
         }

         outStr.flush();
         byte[] responseBody = outStr.toByteArray();
         result1.response = new String(responseBody, respCharSet);
         System.out.println(result1.response);
         HttpCallResult var16 = result1;
         return var16;
      } finally {
         ((HttpMethod)method).releaseConnection();
      }
   }
}