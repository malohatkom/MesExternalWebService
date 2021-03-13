package de.mpdv.maintenanceManager.servlet.client;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.servlet.hydraServer.SoftwareStatusServlet;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SwStatusDownloadServlet extends HttpServlet {

   private static final long serialVersionUID = -7182942489026799973L;


   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      boolean alreadyLoggedIn = true;
      if(!SessionManager.isLoggedIn(request)) {
         alreadyLoggedIn = false;
         SessionManager.internalLogin(request);
      }

      Configuration config = null;

      try {
         config = Configuration.getConfiguration();
      } catch (Exception var9) {
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SW status download: Could not get configuration: " + var9.getMessage());
         return;
      }

      try {
         this.checkParams(config);
      } catch (Exception var8) {
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SW status download: Error at check of needed params: " + var8.getMessage());
         return;
      }

      String type = request.getParameter("type");

      try {
         SoftwareStatusServlet.getSwStatusDataAndAddToResponse(request, config, response, type);
      } catch (Exception var7) {
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SW status download: Error at creation of SW status: " + var7.getMessage());
         return;
      }

      if(!alreadyLoggedIn) {
         SessionManager.logout(request);
      }

   }

   private void checkParams(Configuration config) {
      String tomcatHostPort = config.getTomcatHostPort();
      if(tomcatHostPort != null && !tomcatHostPort.equals("")) {
         String updDirPath = config.getUpdateDirServer();
         if(updDirPath != null && !updDirPath.equals("")) {
            File updDir = new File(updDirPath);
            if(!updDir.exists()) {
               throw new IllegalStateException("The JAVA update dir does not exist: " + updDir.getAbsolutePath());
            } else {
               String clUpdDirPath = config.getUpdateDirClient();
               if(clUpdDirPath != null && !clUpdDirPath.equals("")) {
                  File clUpdDir = new File(clUpdDirPath);
                  if(!clUpdDir.exists()) {
                     throw new IllegalStateException("The client update dir does not exist: " + clUpdDir.getAbsolutePath());
                  } else {
                     String tempDirPath = config.getTempDir();
                     if(tempDirPath != null && !tempDirPath.equals("")) {
                        File tempDir = new File(tempDirPath);
                        if(!tempDir.exists()) {
                           throw new IllegalStateException("The temp dir does not exist: " + tempDir.getAbsolutePath());
                        } else {
                           String rtDir = config.getRuntimeDirServer();
                           if(rtDir != null && !rtDir.equals("")) {
                              File rtDirFile = new File(rtDir);
                              if(!rtDirFile.exists()) {
                                 throw new IllegalStateException("The JAVA runtime dir does not exist: " + rtDirFile.getAbsolutePath());
                              } else {
                                 String tcDir = config.getTomcatDir();
                                 if(tcDir != null && !tcDir.equals("")) {
                                    File tcDirFile = new File(tcDir);
                                    if(!tcDirFile.exists()) {
                                       throw new IllegalStateException("The tomcat dir does not exist: " + tcDirFile.getAbsolutePath());
                                    }
                                 } else {
                                    throw new IllegalStateException("The tomcat dir is not configured");
                                 }
                              }
                           } else {
                              throw new IllegalStateException("The JAVA runtime dir is not configured");
                           }
                        }
                     } else {
                        throw new IllegalStateException("The temp dir is not configured");
                     }
                  }
               } else {
                  throw new IllegalStateException("The client update dir is not configured");
               }
            }
         } else {
            throw new IllegalStateException("The JAVA update dir is not configured");
         }
      } else {
         throw new IllegalStateException("The tomcat host/port is not configured");
      }
   }
}