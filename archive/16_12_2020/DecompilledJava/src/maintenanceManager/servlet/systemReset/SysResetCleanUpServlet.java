package de.mpdv.maintenanceManager.servlet.systemReset;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.servlet.systemReset.SysResetCleanUpServlet.HttpCallResult;
import de.mpdv.maintenanceManager.servlet.systemReset.SysResetCleanUpServlet.ParamStruct;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.Util;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;

public class SysResetCleanUpServlet extends HttpServlet {

   private static final long serialVersionUID = 5342361624636035221L;
   private static final String TC_USER = "ad";
   private static final String TC_PASS = "Mosbach74821";
   private static final String SYS_RESET_TOOL_HASH = "c5f40773a0660abf0e9782485f1e35d1";
   
class HttpCallResult {

   String response;
   int retCode;


}   

public class ParamStruct {

   File jHydraDir;
   File tempDir;
   String tomcatHostPort;
   File tomcatDir;
   File updDirClient;
   File updDirServer;
   File rtDirClient;
   File rtDirServer;


   public String toString() {
      return "ParamStruct [jHydraDir=" + this.jHydraDir + ", tempDir=" + this.tempDir + ", tomcatHostPort=" + this.tomcatHostPort + ", tomcatDir=" + this.tomcatDir + ", updDirClient=" + this.updDirClient + ", updDirServer=" + this.updDirServer + ", rtDirClient=" + this.rtDirClient + ", rtDirServer=" + this.rtDirServer + "]";
   }
}


   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      boolean alreadyLoggedIn = true;
      if(!SessionManager.isLoggedIn(request)) {
         alreadyLoggedIn = false;
         SessionManager.internalLogin(request);
      }

      try {
         Configuration config = null;

         try {
            config = Configuration.getConfiguration();
         } catch (Exception var21) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetCleanUp: " + Util.exceptionToString(var21));
            this.printToResponse(response, "ERROR:\n\n" + Util.exceptionToString(var21));
            return;
         }

         String internalPassword = request.getParameter("internal_password");
         if(!"c5f40773a0660abf0e9782485f1e35d1".equals(internalPassword)) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetCleanUp: Internal password not specified or wrong!");
            this.printToResponse(response, "ERROR:\n\nInternal password not specified or wrong!");
         } else {
            ParamStruct struct;
            try {
               struct = this.checkParams(config);
            } catch (Exception var19) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetCleanUp: " + Util.exceptionToString(var19));
               this.printToResponse(response, "ERROR:\n\n" + Util.exceptionToString(var19));
               return;
            }

            try {
               String[] e = struct.tomcatHostPort.split(":");
               if(e.length != 2) {
                  throw new IllegalArgumentException("Could not split tomcat host and port " + struct.tomcatHostPort + " to host and port");
               } else {
                  String i = e[0];
                  Integer f = null;

                  try {
                     f = Integer.valueOf(e[1]);
                  } catch (NumberFormatException var18) {
                     throw new IllegalArgumentException("Could not split tomcat host and port " + struct.tomcatHostPort + " to host and port because the port part is not numeric: " + e[1]);
                  }

                  if(this.checkUndeploymentNeeded("MocServices", i, f.intValue(), config.getTomcatVersion())) {
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Undeploy from TC");
                     this.undeployFromTomcat("MocServices", i, f.intValue(), config.getTomcatVersion());
                  }

                  (new File(struct.tomcatDir, "webapps/MocServices.war")).delete();
                  FileSystemUtil.deleteDir(new File(struct.tomcatDir, "webapps/MocServices"));
                  this.truncateFolder(new File(struct.tomcatDir, "logs"));
                  this.truncateFolder(struct.rtDirClient);
                  this.truncateFolder(struct.rtDirServer);
                  this.truncateFolder(struct.updDirClient);
                  this.truncateFolder(struct.updDirServer);
                  this.truncateFolder(struct.tempDir);
                  this.truncateFolder(new File(struct.tempDir.getParentFile(), "logs/client"));
                  this.truncateFolder(new File(struct.tempDir.getParentFile(), "logs/hydra"));
                  this.truncateFolder(new File(struct.tempDir.getParentFile(), "logs/java"));
                  File[] var23 = (new File(struct.jHydraDir, "MOC")).listFiles();

                  int var24;
                  File var25;
                  for(var24 = 0; var24 < var23.length; ++var24) {
                     var25 = var23[var24];
                     if(!"1".equals(var25.getName()) && !"config.properties".equals(var25.getName())) {
                        if(var25.isFile()) {
                           var25.delete();
                        } else {
                           FileSystemUtil.deleteDir(var25);
                        }
                     }
                  }

                  var23 = (new File(struct.jHydraDir, "MOC/1")).listFiles();

                  for(var24 = 0; var24 < var23.length; ++var24) {
                     var25 = var23[var24];
                     if(!"mpark".equals(var25.getName()) && !"config.properties".equals(var25.getName())) {
                        if(var25.isFile()) {
                           var25.delete();
                        } else {
                           FileSystemUtil.deleteDir(var25);
                        }
                     }
                  }

                  this.printToResponse(response, "Clean up (dev system) successfully finished!");
               }
            } catch (Exception var20) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetCleanUp: " + Util.exceptionToString(var20));
               this.printToResponse(response, "ERROR:\n\n" + Util.exceptionToString(var20));
            }
         }
      } finally {
         if(!alreadyLoggedIn) {
            SessionManager.logout(request);
         }

      }
   }

   private void truncateFolder(File folder) throws FileNotFoundException {
      List content = getFullFileListing(folder);
      content.remove(folder);

      for(int i = content.size() - 1; i >= 0; --i) {
         File f = (File)content.get(i);
         f.delete();
      }

   }

   private static List getFullFileListing(File dir) throws FileNotFoundException {
      if(dir == null) {
         throw new NullPointerException("Parameter dir is null");
      } else if(!dir.exists()) {
         return new ArrayList();
      } else {
         ArrayList result = new ArrayList();
         File[] filesAndDirs = dir.listFiles();
         List filesDirs = Arrays.asList(filesAndDirs);
         int fileCount = filesDirs.size();

         for(int i = 0; i < fileCount; ++i) {
            File file = (File)filesDirs.get(i);
            if(file.isFile()) {
               result.add(file);
            }

            if(!file.isFile()) {
               List deeperList = getFullFileListing(file);
               result.addAll(deeperList);
            }
         }

         result.add(dir);
         Collections.sort(result);
         return result;
      }
   }

   private void undeployFromTomcat(String warName, String host, int port, int tomcatVersion) {
      HttpCallResult result = null;

      try {
         if(tomcatVersion < 7) {
            result = this.callUrl(host, port, "ad", "Mosbach74821", "/manager/undeploy?path=/" + warName, (byte[])null);
         } else {
            result = this.callUrl(host, port, "ad", "Mosbach74821", "/manager/text/undeploy?path=/" + warName, (byte[])null);
         }
      } catch (Exception var7) {
         throw new RuntimeException("Could not undeploy " + warName + ".", var7);
      }

      if(result.retCode != 200) {
         throw new RuntimeException("Could not undeploy " + warName + ". Return code was: " + result.retCode + ", response was: " + result.response);
      } else if(result.response.startsWith("FAIL")) {
         throw new RuntimeException("Error at undeployment of " + warName + ". Response from server was: " + result.response);
      }
   }

   private boolean checkUndeploymentNeeded(String warName, String host, int port, int tomcatVersion) {
      HttpCallResult result = null;

      try {
         if(tomcatVersion < 7) {
            result = this.callUrl(host, port, "ad", "Mosbach74821", "/manager/list", (byte[])null);
         } else {
            result = this.callUrl(host, port, "ad", "Mosbach74821", "/manager/text/list", (byte[])null);
         }
      } catch (Exception var10) {
         throw new RuntimeException("Could not determine if Undeployment of " + warName + " is needed.", var10);
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

   private HttpCallResult callUrl(String host, int port, String user, String pass, String url, byte[] requestData) throws IOException {
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
         HttpCallResult var17 = result1;
         return var17;
      } finally {
         ((HttpMethod)method).releaseConnection();
      }
   }

   private void printToResponse(HttpServletResponse response, String content) {
      try {
         response.setContentType("text/html");
         response.setCharacterEncoding("UTF-8");
         PrintWriter e = response.getWriter();
         e.println(content);
      } catch (IOException var4) {
         ;
      }

   }

   private ParamStruct checkParams(Configuration config) {
      ParamStruct struct = new ParamStruct();
      String jHydraDir = config.getjHydraDir();
      if(jHydraDir != null && jHydraDir.length() != 0) {
         File jHydraDirFile = new File(jHydraDir);
         if(!jHydraDirFile.exists()) {
            throw new IllegalStateException("The JHYDRADIR does not exist: " + jHydraDirFile.getAbsolutePath());
         } else {
            struct.jHydraDir = jHydraDirFile;
            String tempDir = config.getTempDir();
            if(tempDir != null && tempDir.length() != 0) {
               File tempDirFile = new File(tempDir);
               if(!tempDirFile.exists()) {
                  throw new IllegalStateException("The temp dir does not exist: " + tempDirFile.getAbsolutePath());
               } else {
                  struct.tempDir = tempDirFile;
                  String tcHostPort = config.getTomcatHostPort();
                  if(tcHostPort != null && tcHostPort.length() != 0) {
                     struct.tomcatHostPort = tcHostPort;
                     String tomcatDir = config.getTomcatDir();
                     if(tomcatDir != null && tomcatDir.length() != 0) {
                        File tomcatDirFile = new File(tomcatDir);
                        if(!tomcatDirFile.exists()) {
                           throw new IllegalStateException("The tomcat dir does not exist: " + tomcatDirFile.getAbsolutePath());
                        } else {
                           struct.tomcatDir = tomcatDirFile;
                           String updDirClient = config.getUpdateDirClient();
                           if(updDirClient != null && updDirClient.length() != 0) {
                              File updDirClientFile = new File(updDirClient);
                              if(!updDirClientFile.exists()) {
                                 throw new IllegalStateException("The client update dir does not exist: " + updDirClientFile.getAbsolutePath());
                              } else {
                                 struct.updDirClient = updDirClientFile;
                                 String updDirServer = config.getUpdateDirServer();
                                 if(updDirServer != null && updDirServer.length() != 0) {
                                    File updDirServerFile = new File(updDirServer);
                                    if(!updDirServerFile.exists()) {
                                       throw new IllegalStateException("The server update dir does not exist: " + updDirServerFile.getAbsolutePath());
                                    } else {
                                       struct.updDirServer = updDirServerFile;
                                       String rtDirClient = config.getRuntimeDirClient();
                                       if(rtDirClient != null && rtDirClient.length() != 0) {
                                          File rtDirClientFile = new File(rtDirClient);
                                          if(!rtDirClientFile.exists()) {
                                             throw new IllegalStateException("The client rt dir does not exist: " + rtDirClientFile.getAbsolutePath());
                                          } else {
                                             struct.rtDirClient = rtDirClientFile;
                                             String rtDirServer = config.getRuntimeDirServer();
                                             if(rtDirServer != null && rtDirServer.length() != 0) {
                                                File rtDirServerFile = new File(rtDirServer);
                                                if(!rtDirServerFile.exists()) {
                                                   throw new IllegalStateException("The server rt dir does not exist: " + rtDirServerFile.getAbsolutePath());
                                                } else {
                                                   struct.rtDirServer = rtDirServerFile;
                                                   return struct;
                                                }
                                             } else {
                                                throw new IllegalStateException("The server rt dir is not configured");
                                             }
                                          }
                                       } else {
                                          throw new IllegalStateException("The client rt dir is not configured");
                                       }
                                    }
                                 } else {
                                    throw new IllegalStateException("The server update dir is not configured");
                                 }
                              }
                           } else {
                              throw new IllegalStateException("The client update dir is not configured");
                           }
                        }
                     } else {
                        throw new IllegalStateException("The tomcat dir is not configured");
                     }
                  } else {
                     throw new IllegalStateException("The tomcat host and port is not configured");
                  }
               }
            } else {
               throw new IllegalStateException("The temp dir is not configured");
            }
         }
      } else {
         throw new IllegalStateException("The JHYDRADIR is not configured");
      }
   }
}