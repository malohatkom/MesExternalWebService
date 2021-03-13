package de.mpdv.maintenanceManager.servlet.javaServer;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.data.javaServer.DeploymentMeta;
import de.mpdv.maintenanceManager.data.javaServer.VersionComparisonInfo;
import de.mpdv.maintenanceManager.data.javaServer.VersionInfo;
import de.mpdv.maintenanceManager.gui.CommonResponseFrame;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.logic.javaServer.VersionInformationRetriever;
import de.mpdv.maintenanceManager.servlet.javaServer.VersionInformationServlet.ParamStruct;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VersionInformationServlet extends HttpServlet {

class ParamStruct {

   String rtAppDir;
   String warFile;


}    
    
   private static final long serialVersionUID = 8884238222873458337L;
   private static final String MODE_RT_VERSION = "RT_VERSION";
   private static final String MODE_TC_VERSION = "TC_VERSION";
   private static final String MODE_COMPARE = "COMPARE";


   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      if(SessionManager.checkLogin(request, response)) {
         File mm2ConfigFile = Configuration.getMM2ConfigFile();
         if(mm2ConfigFile.exists()) {
            CommonResponseFrame.printToResponse("Maintananace Manager 2.0 is installed! Please use the new version!", response);
         } else {
            Configuration config = null;

            try {
               config = Configuration.getConfiguration();
            } catch (Exception var15) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var15));
               CommonResponseFrame.printToResponse(this.getErrorResponse("Could not load application for version information because: Could not get configuration: " + var15.getMessage()), response);
               return;
            }

            File rtDir;
            try {
               rtDir = this.checkEntryPageParams(config);
            } catch (Exception var14) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var14));
               CommonResponseFrame.printToResponse(this.getErrorResponse("Could not load application for version information because: " + var14.getMessage()), response);
               return;
            }

            String mode = request.getParameter("mode");
            if(mode != null && !mode.equals("")) {
               String deploymentType = request.getParameter("deploymentType");
               ParamStruct struct = null;

               try {
                  struct = this.checkParams(config, deploymentType, mode, rtDir);
               } catch (Exception var13) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var13));
                  CommonResponseFrame.printToResponse(this.getErrorResponse("Could not execute the version information application because: " + var13.getMessage()), response);
                  return;
               }

               try {
                  List e;
                  if(mode.equals("RT_VERSION")) {
                     e = VersionInformationRetriever.getVersionInformationFolder(struct.rtAppDir, config.getTempDir());
                     CommonResponseFrame.printToResponse(this.getVersionInformationResponse(struct.rtAppDir, e), response);
                  } else if(mode.equals("TC_VERSION")) {
                     e = VersionInformationRetriever.getVersionInformationWarFile(struct.warFile, config.getTempDir());
                     CommonResponseFrame.printToResponse(this.getVersionInformationResponse(struct.warFile, e), response);
                  } else if(mode.equals("COMPARE")) {
                     e = VersionInformationRetriever.getVersionInformationFolder(struct.rtAppDir, config.getTempDir());
                     List versionInfoTc = VersionInformationRetriever.getVersionInformationWarFile(struct.warFile, config.getTempDir());
                     List versionComparisonInfo = VersionInformationRetriever.compareVersions(e, versionInfoTc);
                     CommonResponseFrame.printToResponse(this.getVersionComparisonResponse(struct.rtAppDir, struct.warFile, versionComparisonInfo), response);
                  }
               } catch (Exception var12) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var12));
                  CommonResponseFrame.printToResponse(this.getErrorResponse("Could not execute the version information application because: " + var12.getMessage()), response);
               }
            } else {
               CommonResponseFrame.printToResponse(this.getPageEntryResponse(config, rtDir), response);
            }
         }
      }
   }

   private File checkEntryPageParams(Configuration config) {
      if(config == null) {
         throw new IllegalArgumentException("Could not get configuration");
      } else {
         String rtDir = config.getRuntimeDirServer();
         if(rtDir != null && !rtDir.equals("")) {
            File rtDirFile = new File(rtDir);
            if(!rtDirFile.exists()) {
               throw new IllegalStateException("The runtime dir does not exist: " + rtDirFile.getAbsolutePath());
            } else {
               return rtDirFile;
            }
         } else {
            throw new IllegalStateException("The runtime dir is not configured");
         }
      }
   }

   private ParamStruct checkParams(Configuration config, String deploymentType, String mode, File rtDir) {
      if(deploymentType != null && !deploymentType.equals("")) {
         if(mode != null && !mode.equals("")) {
            if(!mode.equals("RT_VERSION") && !mode.equals("TC_VERSION") && !mode.equals("COMPARE")) {
               throw new IllegalArgumentException("Unknown mode specified: " + mode + ". Allowed modes are: " + "RT_VERSION" + ", " + "TC_VERSION" + ", " + "COMPARE");
            } else {
               Set availableDeploymentTypes = this.getAvailableDeploymentTypes(rtDir);
               if(!availableDeploymentTypes.contains(deploymentType)) {
                  throw new IllegalArgumentException("Unknown deployment type specified: " + deploymentType + ". Allowed deployment types are: " + availableDeploymentTypes);
               } else {
                  DeploymentMeta deplMeta = null;

                  try {
                     deplMeta = DeploymentMeta.loadDeploymentMeta(new File(rtDir, deploymentType));
                  } catch (Exception var13) {
                     throw new RuntimeException("Deployment meta data for deployment type " + deploymentType + " could not be loaded", var13);
                  }

                  String warName = deplMeta.getWarName();
                  if((mode.equals("TC_VERSION") || mode.equals("COMPARE")) && (warName == null || warName.equals(""))) {
                     throw new IllegalArgumentException("The name of the war file for the deployment type " + deploymentType + " is missing");
                  } else {
                     ParamStruct struct = new ParamStruct();
                     String warFilePrefix = File.separator + "webapps" + File.separator + warName + ".war";
                     String rtDirPrefix = File.separator + deploymentType + File.separator;
                     if(mode.equals("RT_VERSION") || mode.equals("COMPARE")) {
                        File tcDir = new File(rtDir, rtDirPrefix);
                        if(!tcDir.exists()) {
                           throw new IllegalStateException("The runtime dir for deployment type " + deploymentType + " does not exist: " + tcDir.getAbsolutePath());
                        }

                        struct.rtAppDir = tcDir.getAbsolutePath();
                     }

                     if(mode.equals("TC_VERSION") || mode.equals("COMPARE")) {
                        String tcDir1 = config.getTomcatDir();
                        if(tcDir1 == null || tcDir1.equals("")) {
                           throw new IllegalStateException("The tomcat dir is not configured");
                        }

                        File warFile = new File(tcDir1 + warFilePrefix);
                        if(!warFile.exists()) {
                           throw new IllegalStateException("The webarchive for deployment type " + deploymentType + " does not exist: " + warFile.getAbsolutePath());
                        }

                        struct.warFile = warFile.getAbsolutePath();
                     }

                     return struct;
                  }
               }
            }
         } else {
            throw new IllegalArgumentException("The mode is not specified");
         }
      } else {
         throw new IllegalArgumentException("The deployment type is not specified");
      }
   }

   private Set getAvailableDeploymentTypes(File rtDir) {
      HashSet deploymentTypesSet = new HashSet();
      File[] subFolders = rtDir.listFiles();
      int count = subFolders.length;

      for(int i = 0; i < count; ++i) {
         File currFolder = subFolders[i];
         if(currFolder.isDirectory()) {
            deploymentTypesSet.add(currFolder.getName());
         }
      }

      return deploymentTypesSet;
   }

   private String getErrorResponse(String message) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Version information</h1>\n");
      builder.append("<br />\n");
      builder.append("<span style=\"color:red;\">An error has occured: " + message + "</span><br />");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getVersionInformationResponse(String informationSource, List versionInfo) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Version information</h1>\n");
      builder.append("<b>Source: " + informationSource + "</b>\n");
      builder.append("<br />\n");
      builder.append("<br />\n");
      builder.append("<table border=\"2\" style=\"margin-left:auto;margin-right:auto;font-size:75%;text-align:left;\">\n");
      builder.append("<tr style=\"background-grey;\">\n");
      builder.append("<th>File name</th>\n");
      builder.append("<th>Version</th>\n");
      builder.append("<th>Last change</th>\n");
      builder.append("</tr>\n");
      int count = versionInfo.size();

      for(int i = 0; i < count; ++i) {
         VersionInfo currInfo = (VersionInfo)versionInfo.get(i);
         builder.append("<tr>\n");
         builder.append("<td>" + currInfo.getFileName() + "</td>\n");
         builder.append("<td>" + (currInfo.getVersionString() == null?"&nbsp;":currInfo.getVersionString()) + "</td>\n");
         builder.append("<td>" + (currInfo.getChangeDate() == null?"&nbsp;":currInfo.getChangeDate()) + "</td>\n");
         builder.append("</tr>\n");
      }

      builder.append("</table>\n");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getVersionComparisonResponse(String rtSource, String activeSource, List comparisonInfo) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Version information</h1>\n");
      builder.append("<b>Comparison between runtime (" + rtSource + ") and active versions (" + activeSource + ")</b>\n");
      builder.append("<br />\n");
      builder.append("<br />\n");
      builder.append("<table border=\"2\" style=\"margin-left:auto;margin-right:auto;font-size:75%;text-align:left;\">\n");
      builder.append("<tr style=\"background-grey;\">\n");
      builder.append("<th>File name</th>\n");
      builder.append("<th>Runtime Version</th>\n");
      builder.append("<th>Runtime Last change</th>\n");
      builder.append("<th>Active Version</th>\n");
      builder.append("<th>Active Last change</th>\n");
      builder.append("</tr>\n");
      int count = comparisonInfo.size();

      for(int i = 0; i < count; ++i) {
         VersionComparisonInfo currInfo = (VersionComparisonInfo)comparisonInfo.get(i);
         boolean leftVersionSet = currInfo.getLeftMajor() != null && currInfo.getLeftMinor() != null && currInfo.getLeftRevision() != null;
         boolean rightVersionSet = currInfo.getRightMajor() != null && currInfo.getRightMinor() != null && currInfo.getRightRevision() != null;
         if(!leftVersionSet && rightVersionSet) {
            builder.append("<tr style=\"background-color:grey;\">\n");
         } else if(leftVersionSet && !rightVersionSet) {
            builder.append("<tr style=\"background-color:grey;\">\n");
         } else if(!leftVersionSet && !rightVersionSet) {
            builder.append("<tr>\n");
         } else if(leftVersionSet && rightVersionSet) {
            if(currInfo.getLeftMajor().intValue() == currInfo.getRightMajor().intValue() && currInfo.getLeftMinor().intValue() == currInfo.getRightMinor().intValue() && currInfo.getLeftRevision().intValue() == currInfo.getRightRevision().intValue()) {
               builder.append("<tr>\n");
            } else if(currInfo.getLeftMajor().intValue() <= currInfo.getRightMajor().intValue() && (currInfo.getLeftMajor().intValue() != currInfo.getRightMajor().intValue() || currInfo.getLeftMinor().intValue() <= currInfo.getRightMinor().intValue()) && (currInfo.getLeftMajor().intValue() != currInfo.getRightMajor().intValue() || currInfo.getLeftMinor().intValue() != currInfo.getRightMinor().intValue() || currInfo.getLeftRevision().intValue() <= currInfo.getRightRevision().intValue())) {
               if(currInfo.getLeftMajor().intValue() < currInfo.getRightMajor().intValue() || currInfo.getLeftMajor().intValue() == currInfo.getRightMajor().intValue() && currInfo.getLeftMinor().intValue() < currInfo.getRightMinor().intValue() || currInfo.getLeftMajor().intValue() == currInfo.getRightMajor().intValue() && currInfo.getLeftMinor().intValue() == currInfo.getRightMinor().intValue() && currInfo.getLeftRevision().intValue() < currInfo.getRightRevision().intValue()) {
                  builder.append("<tr style=\"background-color:red;\">\n");
               }
            } else {
               builder.append("<tr style=\"background-color:green;\">\n");
            }
         }

         builder.append("<td>" + currInfo.getFileName() + "</td>\n");
         builder.append("<td>" + (currInfo.getLeftVersionString() == null?"&nbsp;":currInfo.getLeftVersionString()) + "</td>\n");
         builder.append("<td>" + (currInfo.getLeftChangeDate() == null?"&nbsp;":currInfo.getLeftChangeDate()) + "</td>\n");
         builder.append("<td>" + (currInfo.getRightVersionString() == null?"&nbsp;":currInfo.getRightVersionString()) + "</td>\n");
         builder.append("<td>" + (currInfo.getRightChangeDate() == null?"&nbsp;":currInfo.getRightChangeDate()) + "</td>\n");
         builder.append("</tr>\n");
      }

      builder.append("</table>\n");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getPageEntryResponse(Configuration config, File rtDir) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Version information</h1>\n");
      builder.append("<br />\n");
      builder.append("<form action=\"VersionInformation\" method=\"post\">\n");
      builder.append("Select mode: <select name=\"mode\" size=\"1\">\n");
      builder.append("<option value=\"RT_VERSION\">Version information of runtime</option>\n");
      builder.append("<option value=\"TC_VERSION\">Version information of active versions</option>\n");
      builder.append("<option value=\"COMPARE\">Compare runtime and active versions</option>\n");
      builder.append("</select><br />\n");
      builder.append("<br />\n");
      builder.append("Select deployment type: <select name=\"deploymentType\" size=\"1\">\n");
      Set availableDeploymentTypes = this.getAvailableDeploymentTypes(rtDir);
      Iterator it = availableDeploymentTypes.iterator();

      while(it.hasNext()) {
         String deploymentType = (String)it.next();
         builder.append("<option value=\"" + deploymentType + "\">" + deploymentType + "</option>\n");
      }

      builder.append("</select><br />\n");
      builder.append("<br />\n");
      builder.append("<input type=\"submit\" name=\"Submit\" value=\"Execute\"/><br />\n");
      builder.append("</form>\n");
      builder.append("</div>\n");
      return builder.toString();
   }
}