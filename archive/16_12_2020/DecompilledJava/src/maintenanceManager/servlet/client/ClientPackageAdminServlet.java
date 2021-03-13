package de.mpdv.maintenanceManager.servlet.client;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.data.client.ClientPackageMeta;
import de.mpdv.maintenanceManager.data.client.VersionComparisonInfo;
import de.mpdv.maintenanceManager.gui.CommonResponseFrame;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.logic.client.ClientPackageDeployment;
import de.mpdv.maintenanceManager.servlet.client.ClientPackageAdminServlet.ParamStruct;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class ClientPackageAdminServlet extends HttpServlet {

   private static final long serialVersionUID = -3617889367529114906L;
   private static final String ACTION_DEPLOY = "DEPLOY";
   private static final String ACTION_UNDEPLOY = "UNDEPLOY";
   private static final String ACTION_SHOW_DETAIL = "SHOW_DETAIL";
   private static final String ACTION_DELETE = "DELETE";
   
   class ParamStruct {

   File updateDir;
   File rtDir;
}
   
   class JavaPackageDeployment$1 implements FilenameFilter {

   // $FF: synthetic field
   final File val$destFile;


   JavaPackageDeployment$1(File var1) {
      this.val$destFile = var1;
   }

   public boolean accept(File dir, String name) {
      return name.equalsIgnoreCase(this.val$destFile.getName());
   }
}


   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      if(SessionManager.checkLogin(request, response)) {
         File mm2ConfigFile = Configuration.getMM2ConfigFile();
         if(mm2ConfigFile.exists()) {
            CommonResponseFrame.printToResponse("Maintananace Manager 2.0 is installed! Please use the new version!", response);
         } else {
            Configuration config = null;

            try {
               config = Configuration.getConfiguration();
            } catch (Exception var23) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var23));
               CommonResponseFrame.printToResponse(this.getErrorResponse("Could not get configuration: " + var23.getMessage()), response);
               return;
            }

            String action = request.getParameter("action");
            String packageFolder = request.getParameter("packagefolder");
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Client package administration was called with action " + action + " for package " + packageFolder);
            ParamStruct struct = null;

            try {
               struct = this.checkParams(config, action, packageFolder);
            } catch (Exception var22) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var22));
               CommonResponseFrame.printToResponse(this.getErrorResponse(var22.getMessage()), response);
               return;
            }

            if(action != null && !action.equals("")) {
               String e;
               List e1;
               if(action.equals("DEPLOY")) {
                  e = null;
                  e1 = null;

                  Map e3;
                  try {
                     ClientPackageMeta overrideVersionCheck = ClientPackageMeta.loadPackageMeta(struct.updateDir + File.separator + packageFolder);
                     Map overrideVersionCheckStr = ClientPackageDeployment.getPackageDomainVersions(overrideVersionCheck);
                     Map e2 = ClientPackageDeployment.getRuntimeDomainsVersions(new File(struct.rtDir, overrideVersionCheck.getApplicationName()), overrideVersionCheckStr);
                     e3 = ClientPackageDeployment.getOlderDomainsInPackage(overrideVersionCheckStr, e2);
                     e1 = ClientPackageDeployment.getSameVersionDomainsInPackage(overrideVersionCheckStr, e2);
                  } catch (Exception var15) {
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var15));
                     CommonResponseFrame.printToResponse(this.getErrorResponse("Could not deploy client package because: Could not check versions of package and runtime: " + var15.getMessage()), response);
                     return;
                  }

                  boolean overrideVersionCheck1 = true;
                  String overrideVersionCheckStr1 = request.getParameter("overrideversioncheck");
                  if(overrideVersionCheckStr1 == null || !"true".equals(overrideVersionCheckStr1)) {
                     overrideVersionCheck1 = false;
                  }

                  if(overrideVersionCheckStr1 == null && e3.size() > 0) {
                     CommonResponseFrame.printToResponse(this.getPackageOlderVersionsResponse(e3, packageFolder, config), response);
                  } else {
                     try {
                        String e4 = ClientPackageDeployment.deployPackage(struct.updateDir, struct.rtDir, packageFolder, overrideVersionCheck1, e3, e1);
                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Result of client pacakge deployment (" + packageFolder + ")\n" + e4);
                     } catch (Exception var14) {
                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var14));
                        CommonResponseFrame.printToResponse(this.getErrorResponse("Could not deploy package because: " + var14.getMessage()), response);
                        return;
                     }

                     try {
                        CommonResponseFrame.printToResponse(this.getPageEntryResponse(config, struct), response);
                     } catch (Exception var13) {
                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var13));
                        CommonResponseFrame.printToResponse(this.getErrorResponse("Could load package administration entry page because: " + var13.getMessage()), response);
                     }

                  }
               } else {
                  if(action.equals("SHOW_DETAIL")) {
                     try {
                        CommonResponseFrame.printToResponse(this.getPackageDetailViewResponse(new File(struct.updateDir, packageFolder), config), response);
                     } catch (Exception var20) {
                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var20));
                        CommonResponseFrame.printToResponse(this.getErrorResponse("Could not load package details because: " + var20.getMessage()), response);
                     }
                  } else {
                     if(action.equals("UNDEPLOY")) {
                        e = request.getParameter("overridedeplordercheck");
                        if(e == null || !"true".equals(e)) {
                           try {
                              e1 = this.getMetaOfPackagesDeployedAfter(packageFolder, struct.updateDir);
                              if(e1.size() > 0) {
                                 CommonResponseFrame.printToResponse(this.getNewerDeployedPackagesResponse(e1, packageFolder, config), response);
                                 return;
                              }
                           } catch (Exception var18) {
                              System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var18));
                              CommonResponseFrame.printToResponse(this.getErrorResponse("Could not undeploy package because: Could not check deployment order of packages: " + var18.getMessage()), response);
                              return;
                           }
                        }

                        try {
                           ClientPackageDeployment.undeployPackage(struct.updateDir, struct.rtDir, packageFolder);
                        } catch (Exception var17) {
                           System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var17));
                           CommonResponseFrame.printToResponse(this.getErrorResponse("Could not undeploy package because: " + var17.getMessage()), response);
                           return;
                        }

                        try {
                           CommonResponseFrame.printToResponse(this.getPageEntryResponse(config, struct), response);
                        } catch (Exception var16) {
                           System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var16));
                           CommonResponseFrame.printToResponse(this.getErrorResponse("Could not load package adminiostration entry page because: " + var16.getMessage()), response);
                        }

                        return;
                     }

                     if(action.equals("DELETE")) {
                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Delete client package " + packageFolder);
                        if(FileSystemUtil.deleteDir(new File(struct.updateDir, packageFolder))) {
                           System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Package successfully deleted");

                           try {
                              CommonResponseFrame.printToResponse(this.getPageEntryResponse(config, struct), response);
                           } catch (Exception var19) {
                              System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var19));
                              CommonResponseFrame.printToResponse(this.getErrorResponse("Could not load client package administration entry page because: " + var19.getMessage()), response);
                           }
                        } else {
                           System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Package could not be deleted");
                           CommonResponseFrame.printToResponse(this.getErrorResponse("Package could not be deleted"), response);
                        }
                     }
                  }

               }
            } else {
               try {
                  CommonResponseFrame.printToResponse(this.getPageEntryResponse(config, struct), response);
               } catch (Exception var21) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var21));
                  CommonResponseFrame.printToResponse(this.getErrorResponse("Could not load package administration entry page because: " + var21.getMessage()), response);
               }

            }
         }
      }
   }

   private List getMetaOfPackagesDeployedAfter(String packageFolder, File updDir) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, ParseException {
      LinkedList metaList = new LinkedList();
      ClientPackageMeta meta = ClientPackageMeta.loadPackageMeta(updDir.getAbsolutePath() + File.separator + packageFolder);
      Calendar deplDate = meta.getDeploymentDate();
      File[] packages = updDir.listFiles();
      int count = packages.length;

      for(int i = 0; i < count; ++i) {
         File f = packages[i];
         if(!f.getName().equals("Backups")) {
            ClientPackageMeta currMeta = ClientPackageMeta.loadPackageMeta(f.getAbsolutePath());
            Calendar currDeplDate = currMeta.getDeploymentDate();
            if(currDeplDate != null && deplDate.compareTo(currDeplDate) < 0) {
               metaList.add(currMeta);
            }
         }
      }

      return metaList;
   }

   private String getPackageOlderVersionsResponse(Map compList, String packageFolder, Configuration config) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Client Package administration</h1>\n");
      builder.append("<br />\n");
      builder.append("<h2>Older versions in deployment package</h2>\n");
      builder.append("<br />\n");
      builder.append("The deployment package contains some domains in older versions than the runtime folder:<br />\n");
      builder.append("<table border=\"2\" style=\"font-size:75%;margin-left:auto;margin-right:auto;\">\n");
      builder.append(" <tr>\n");
      builder.append("  <td><b>Domain</b></td>\n");
      builder.append("  <td>Package version</td>\n");
      builder.append("  <td>Runtime version</td>\n");
      builder.append(" </tr>\n");
      Iterator it = compList.keySet().iterator();

      while(it.hasNext()) {
         VersionComparisonInfo compInfo = (VersionComparisonInfo)compList.get(it.next());
         builder.append(" <tr>\n");
         builder.append("  <td>" + compInfo.getDomain() + "</td>\n");
         builder.append("  <td>" + compInfo.getLeftVersionString() + "</td>\n");
         builder.append("  <td>" + compInfo.getRightVersionString() + "</td>\n");
         builder.append(" </tr>\n");
      }

      builder.append("</table>\n");
      builder.append("<br /><br />\n");
      builder.append("<b>How do you want to continue?</b><br /><br />\n");
      builder.append("<form action=\"ClientPackageAdmin\" method=\"post\">\n");
      builder.append(" <input type=\"hidden\" name=\"overrideversioncheck\" value=\"true\"/>\n");
      builder.append(" <input type=\"hidden\" name=\"action\" value=\"DEPLOY\"/>\n");
      builder.append(" <input type=\"hidden\" name=\"packagefolder\" value=\"" + packageFolder + "\"/>\n");
      builder.append(" <input type=\"submit\" name=\"Submit\" value=\"Continue and deploy older\"/><br /><br />\n");
      builder.append("</form>\n");
      builder.append("<form action=\"ClientPackageAdmin\" method=\"post\">\n");
      builder.append(" <input type=\"hidden\" name=\"overrideversioncheck\" value=\"false\"/>\n");
      builder.append(" <input type=\"hidden\" name=\"action\" value=\"DEPLOY\"/>\n");
      builder.append(" <input type=\"hidden\" name=\"packagefolder\" value=\"" + packageFolder + "\"/>\n");
      builder.append(" <input type=\"submit\" name=\"Submit\" value=\"Continue and NOT deploy older\"/><br /><br />\n");
      builder.append("</form>\n");
      builder.append("<a href=\"ClientPackageAdmin\"><input type=\"button\" value=\"Cancel\" /></a>\n");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getNewerDeployedPackagesResponse(List metaList, String packageFolder, Configuration config) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Client Package undeployment</h1>\n");
      builder.append("<br />\n");
      builder.append("<h2>Newer deployed packages</h2>\n");
      builder.append("<br />\n");
      builder.append("You are trying to undeploy the package " + packageFolder + " but there are packages that were deployed after this:<br />\n");
      builder.append("<table border=\"2\" style=\"font-size:75%;margin-left:auto;margin-right:auto;\">\n");
      builder.append(" <tr>\n");
      builder.append("  <td>Package name</td>\n");
      builder.append("  <td>Deployment date</td>\n");
      builder.append(" </tr>\n");
      int count = metaList.size();

      for(int i = 0; i < count; ++i) {
         ClientPackageMeta meta = (ClientPackageMeta)metaList.get(i);
         builder.append(" <tr>\n");
         builder.append("  <td>" + meta.getName() + "</td>\n");
         builder.append("  <td>" + DateTimeUtil.calendarUtcToPrintString(meta.getDeploymentDate()) + "</td>\n");
         builder.append(" </tr>\n");
      }

      builder.append("</table>\n");
      builder.append("<br /><br />\n");
      builder.append("<b>Are you sure you want to continue? Perhaps you will overwrite newer files from one of these packages by old ones from the backup of " + packageFolder + "!</b>\n");
      builder.append("<form action=\"ClientPackageAdmin\" method=\"post\">\n");
      builder.append(" <input type=\"hidden\" name=\"overridedeplordercheck\" value=\"true\"/><br />\n");
      builder.append(" <input type=\"hidden\" name=\"action\" value=\"UNDEPLOY\"/><br />\n");
      builder.append(" <input type=\"hidden\" name=\"packagefolder\" value=\"" + packageFolder + "\"/><br />\n");
      builder.append(" <input type=\"submit\" name=\"Submit\" value=\"Continue undeployment\"/><br />\n");
      builder.append("</form>\n");
      builder.append("<a href=\"ClientPackageAdmin\"><input type=\"button\" value=\"Cancel\" /></a>\n");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getPackageDetailViewResponse(File packageFolder, Configuration config) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, ParseException {
      ClientPackageMeta meta = ClientPackageMeta.loadPackageMeta(packageFolder.getAbsolutePath());
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Client Package detail view</h1>\n");
      builder.append("<br />\n");
      builder.append("<h2>Package details</h2>\n");
      builder.append("<br />\n");
      builder.append("<table border=\"2\" style=\"font-size:75%;margin-left:auto;margin-right:auto;\">\n");
      builder.append(" <tr>\n");
      builder.append("  <td><b>Package name</b></td>\n");
      builder.append("  <td colspan=\"2\">" + Util.newlinesToXHTMLBreaks(Util.escapeHTML(meta.getName())) + "</td>\n");
      builder.append(" </tr>\n");
      builder.append(" <tr>\n");
      builder.append("  <td><b>Package creation date</b></td>\n");
      builder.append("  <td colspan=\"2\">" + DateTimeUtil.calendarUtcToPrintString(meta.getCreationDate()) + "</td>\n");
      builder.append(" </tr>\n");
      Calendar deploymentDate = meta.getDeploymentDate();
      builder.append(" <tr>\n");
      builder.append("  <td><b>Deployed at (UTC)</b></td>\n");
      if(deploymentDate != null) {
         builder.append("  <td colspan=\"2\">" + DateTimeUtil.calendarUtcToPrintString(meta.getDeploymentDate()) + "</td>\n");
      } else {
         builder.append("  <td colspan=\"2\">Not yet deployed</td>\n");
      }

      builder.append(" </tr>\n");
      builder.append(" <tr>\n");
      builder.append("  <td><b>Package description</b></td>\n");
      builder.append("  <td colspan=\"2\">" + Util.newlinesToXHTMLBreaks(Util.escapeHTML(meta.getDescription())) + "</td>\n");
      builder.append(" </tr>\n");
      List domains = meta.getDomains();
      int count = domains.size();
      builder.append(" <tr>\n");
      builder.append("  <td rowspan=\"" + (count + 1) + "\"><b>Package content</b></td>\n");
      builder.append("  <td><b>Name</b></td>\n");
      builder.append("  <td><b>Version</b></td>\n");
      builder.append(" </tr>\n");

      for(int i = 0; i < count; ++i) {
         String domain = (String)domains.get(i);
         builder.append(" <tr>\n");
         builder.append("  <td>" + domain + "</td>\n");
         String version = "";
         if(meta.getDomainVersions().containsKey(domain)) {
            version = (String)meta.getDomainVersions().get(domain);
         }

         builder.append("  <td>" + version + "</td>\n");
         builder.append(" </tr>\n");
      }

      builder.append("</table>\n");
      builder.append("<br />\n");
      builder.append("<form action=\"ClientPackageAdmin\" method=\"post\">\n");
      builder.append(" <input type=\"hidden\" name=\"packagefolder\" value=\"" + packageFolder.getName() + "\"/><br />\n");
      builder.append(" <input type=\"submit\" name=\"Submit\" value=\"Back\"/><br />\n");
      builder.append("</form>\n");
      builder.append("</div>\n");
      return builder.toString();
   }

   private ParamStruct checkParams(Configuration config, String action, String packageFolder) {
      if(config == null) {
         throw new IllegalArgumentException("Could not get configuration");
      } else {
         String updDirPath = config.getUpdateDirClient();
         if(updDirPath != null && !updDirPath.equals("")) {
            File updDir = new File(updDirPath);
            if(!updDir.exists()) {
               throw new IllegalStateException("Can not load client package administration function because the configured update dir does not exist: " + updDir.getAbsolutePath());
            } else {
               ParamStruct struct = new ParamStruct();
               struct.updateDir = updDir;
               if(action != null && !action.equals("")) {
                  if(!action.equals("DEPLOY") && !action.equals("UNDEPLOY") && !action.equals("SHOW_DETAIL") && !action.equals("DELETE")) {
                     throw new IllegalArgumentException("Unknown action specified: " + action + ". Allowed actions are: " + "DEPLOY" + ", " + "UNDEPLOY" + ", " + "SHOW_DETAIL" + ", " + "DELETE");
                  } else {
                     if(action.equals("DEPLOY") || action.equals("UNDEPLOY")) {
                        String rtDirPath = config.getRuntimeDirClient();
                        if(rtDirPath == null || rtDirPath.equals("")) {
                           throw new IllegalStateException("Can not load client package administration function because the runtime dir is not configured");
                        }

                        File rtDir = new File(rtDirPath);
                        if(!rtDir.exists()) {
                           throw new IllegalStateException("Can not load client package administration function because the configured runtime dir does not exist: " + rtDir.getAbsolutePath());
                        }

                        struct.rtDir = rtDir;
                     }

                     if(packageFolder != null && !packageFolder.equals("")) {
                        return struct;
                     } else {
                        throw new IllegalArgumentException("Can not do action " + action + " because package folder is missing");
                     }
                  }
               } else {
                  return struct;
               }
            }
         } else {
            throw new IllegalStateException("Can not load client package administration function because the update dir is not configured");
         }
      }
   }

   private String getErrorResponse(String message) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Client package administration</h1>\n");
      builder.append("<br />\n");
      builder.append("<span style=\"color:red;\">An error has occured: " + message + "</span><br />");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getPageEntryResponse(Configuration config, ParamStruct struct) throws ParserConfigurationException, SAXException, IOException, ParseException {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Client package administration</h1>\n");
      builder.append("<br />\n");
      TreeMap deployedPackMeta = new TreeMap();
      TreeMap undeployedPackMeta = new TreeMap();
      File[] packages = struct.updateDir.listFiles();
      int packCount = packages.length;

      for(int it = 0; it < packCount; ++it) {
         if(!packages[it].getName().equals("Backups") && packages[it].isDirectory()) {
            ClientPackageMeta cal = ClientPackageMeta.loadPackageMeta(packages[it].getAbsolutePath());
            Object meta;
            if(cal.getDeploymentDate() != null) {
               meta = (List)deployedPackMeta.get(cal.getDeploymentDate());
               if(meta == null) {
                  meta = new LinkedList();
                  deployedPackMeta.put(cal.getDeploymentDate(), meta);
               }

               ((List)meta).add(cal);
            } else {
               meta = (List)undeployedPackMeta.get(cal.getCreationDate());
               if(meta == null) {
                  meta = new LinkedList();
                  undeployedPackMeta.put(cal.getCreationDate(), meta);
               }

               ((List)meta).add(cal);
            }
         }
      }

      builder.append("<br />\n");
      builder.append("<table border=\"2\" style=\"font-size:75%;margin-left:auto;margin-right:auto;\">\n");
      builder.append("<tr>\n");
      builder.append("<th>Package name</th>\n");
      builder.append("<th>Deployed at (UTC)</th>\n");
      builder.append("<th>&nbsp;</th>\n");
      builder.append("<th>&nbsp;</th>\n");
      builder.append("<th>&nbsp;</th>\n");
      builder.append("<th>&nbsp;</th>\n");
      builder.append("</tr>\n");
      Iterator var12 = deployedPackMeta.keySet().iterator();

      int i;
      Calendar var13;
      List var14;
      while(var12.hasNext()) {
         var13 = (Calendar)var12.next();
         var14 = (List)deployedPackMeta.get(var13);

         for(i = 0; i < var14.size(); ++i) {
            this.appendPackageToPageEntryList(builder, (ClientPackageMeta)var14.get(i), struct);
         }
      }

      var12 = undeployedPackMeta.keySet().iterator();

      while(var12.hasNext()) {
         var13 = (Calendar)var12.next();
         var14 = (List)undeployedPackMeta.get(var13);

         for(i = 0; i < var14.size(); ++i) {
            this.appendPackageToPageEntryList(builder, (ClientPackageMeta)var14.get(i), struct);
         }
      }

      builder.append("</table>\n");
      builder.append("</div>\n");
      return builder.toString();
   }

   private void appendPackageToPageEntryList(StringBuilder builder, ClientPackageMeta meta, ParamStruct struct) {
      builder.append("<tr>\n");
      builder.append("<td>" + Util.newlinesToXHTMLBreaks(Util.escapeHTML(Util.appendNewlineEachGivenChars(45, meta.getName()))) + "</td>\n");
      Calendar deploymentDate = meta.getDeploymentDate();
      if(deploymentDate != null) {
         builder.append("<td>" + DateTimeUtil.calendarUtcToPrintString(deploymentDate) + "</td>\n");
      } else {
         builder.append("<td>&nbsp;</td>\n");
      }

      builder.append("<td>\n");
      builder.append("<form action=\"ClientPackageAdmin\" method=\"post\">\n");
      builder.append("<input type=\"hidden\" id=\"action\" name=\"action\" value=\"SHOW_DETAIL\" />\n");
      builder.append("<input type=\"hidden\" id=\"packagefolder\" name=\"packagefolder\" value=\"" + DateTimeUtil.calendarUtcToIsoDateString(meta.getCreationDate()) + "-" + meta.getName() + "\" />\n");
      builder.append("<input type=\"submit\" name=\"Submit\" value=\"Details\"/><br />\n");
      builder.append("</form>\n");
      builder.append("</td>\n");
      builder.append("<td>\n");
      builder.append("<form action=\"ClientPackageAdmin\" method=\"post\">\n");
      builder.append("<input type=\"hidden\" id=\"action\" name=\"action\" value=\"DEPLOY\" />\n");
      builder.append("<input type=\"hidden\" id=\"packagefolder\" name=\"packagefolder\" value=\"" + DateTimeUtil.calendarUtcToIsoDateString(meta.getCreationDate()) + "-" + meta.getName() + "\" />\n");
      File backupDir;
      if(deploymentDate == null) {
         backupDir = new File(struct.updateDir, DateTimeUtil.calendarUtcToIsoDateString(meta.getCreationDate()) + "-" + meta.getName());
         File[] packFiles = backupDir.listFiles();
         if(packFiles.length == 1 && packFiles[0].getName().equals("clientPackageMeta.xml")) {
            builder.append("<input type=\"submit\" name=\"Submit\" value=\"Deploy\" disabled=\"disabled\"/><br />\n");
         } else {
            builder.append("<input type=\"submit\" name=\"Submit\" value=\"Deploy\"/><br />\n");
         }
      } else {
         builder.append("<input type=\"submit\" name=\"Submit\" value=\"Deploy\" disabled=\"disabled\"/><br />\n");
      }

      builder.append("</form>\n");
      builder.append("</td>\n");
      builder.append("<td>\n");
      builder.append("<form action=\"ClientPackageAdmin\" method=\"post\">\n");
      builder.append("<input type=\"hidden\" id=\"action\" name=\"action\" value=\"UNDEPLOY\" />\n");
      builder.append("<input type=\"hidden\" id=\"packagefolder\" name=\"packagefolder\" value=\"" + DateTimeUtil.calendarUtcToIsoDateString(meta.getCreationDate()) + "-" + meta.getName() + "\" />\n");
      if(deploymentDate == null) {
         builder.append("<input type=\"submit\" name=\"Submit\" value=\"Undeploy\" disabled=\"disabled\"/><br />\n");
      } else {
         backupDir = new File(struct.updateDir, "Backups/" + DateTimeUtil.calendarUtcToIsoDateString(meta.getCreationDate()) + "-" + meta.getName());
         if(backupDir.exists()) {
            builder.append("<input type=\"submit\" name=\"Submit\" value=\"Undeploy\"/><br />\n");
         } else {
            builder.append("<input type=\"submit\" name=\"Submit\" value=\"Undeploy\" disabled=\"disabled\"/><br />\n");
         }
      }

      builder.append("</form>\n");
      builder.append("</td>\n");
      builder.append("<td>\n");
      builder.append("<form action=\"ClientPackageAdmin\" method=\"post\">\n");
      builder.append("<input type=\"hidden\" id=\"action\" name=\"action\" value=\"DELETE\" />\n");
      builder.append("<input type=\"hidden\" id=\"packagefolder\" name=\"packagefolder\" value=\"" + DateTimeUtil.calendarUtcToIsoDateString(meta.getCreationDate()) + "-" + meta.getName() + "\" />\n");
      if(deploymentDate == null) {
         builder.append("<input type=\"submit\" name=\"Submit\" value=\"Delete\"/><br />\n");
      } else {
         builder.append("<input type=\"submit\" name=\"Submit\" value=\"Delete\" disabled=\"disabled\"/><br />\n");
      }

      builder.append("</form>\n");
      builder.append("</td>\n");
      builder.append("</tr>\n");
   }
}