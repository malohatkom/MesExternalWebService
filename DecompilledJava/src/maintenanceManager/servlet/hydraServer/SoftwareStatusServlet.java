package de.mpdv.maintenanceManager.servlet.hydraServer;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.data.client.ClientPackageMeta;
import de.mpdv.maintenanceManager.data.javaServer.DeploymentMeta;
import de.mpdv.maintenanceManager.data.javaServer.PackageElement;
import de.mpdv.maintenanceManager.data.javaServer.PackageMeta;
import de.mpdv.maintenanceManager.data.javaServer.VersionComparisonInfo;
import de.mpdv.maintenanceManager.data.javaServer.VersionInfo;
import de.mpdv.maintenanceManager.gui.CommonResponseFrame;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.logic.javaServer.VersionInformationRetriever;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.Util;
import de.mpdv.maintenanceManager.util.ZipUtil;
import de.mpdv.mesclient.businessservice.internalData.DataTableSortSpec;
import de.mpdv.mesclient.businessservice.internalData.DataTableUtil;
import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.ServiceInputFilterParam;
import de.mpdv.mesclient.businessservice.internalData.DataTableSortSpec.OrderDirection;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SoftwareStatusServlet extends HttpServlet {

   private static final long serialVersionUID = 3614040670713151430L;
   private static final String MODE_LIST_STATUS = "LIST_STATUS";
   private static final String MODE_EXPORT_STATUS = "EXPORT_STATUS";


   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      if(SessionManager.checkLogin(request, response)) {
         Configuration config = null;

         try {
            config = Configuration.getConfiguration();
         } catch (Exception var16) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var16));
            CommonResponseFrame.printToResponse(this.getErrorResponse("Could not load application for software status because: Could not get configuration: " + var16.getMessage()), response);
            return;
         }

         String mode = request.getParameter("mode");
         if(mode != null && !mode.equals("")) {
            try {
               this.checkParams(config, mode);
            } catch (Exception var15) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var15));
               CommonResponseFrame.printToResponse(this.getErrorResponse("Could not execute the software status application because: " + var15.getMessage()), response);
               return;
            }

            if(mode.equals("LIST_STATUS")) {
               try {
                  LinkedList e = new LinkedList();
                  e.add("softwarestatus.name");
                  e.add("softwarestatus.file_type");
                  e.add("softwarestatus.file_version");
                  e.add("softwarestatus.file_date");
                  e.add("softwarestatus.file_compilation");
                  e.add("softwarestatus.file_name");
                  e.add("softwarestatus.file_function");
                  e.add("softwarestatus.logged_ts");
                  e.add("softwarestatus.logged_count");
                  e.add("softwarestatus.error_ts");
                  e.add("softwarestatus.error_code");
                  e.add("softwarestatus.error_message");
                  String filterName = request.getParameter("filtername");
                  String filterType = request.getParameter("filtertype");
                  String filterFileName = request.getParameter("filterfilename");
                  LinkedList filterList = new LinkedList();
                  if(!Util.stringNullOrEmpty(filterName)) {
                     filterList.add(new ServiceInputFilterParam("softwarestatus.name", "LIKE", String.class, filterName));
                  }

                  if(!Util.stringNullOrEmpty(filterType)) {
                     filterList.add(new ServiceInputFilterParam("softwarestatus.file_type", "LIKE", String.class, filterType));
                  }

                  if(!Util.stringNullOrEmpty(filterFileName)) {
                     filterList.add(new ServiceInputFilterParam("softwarestatus.file_name", "LIKE", String.class, filterFileName));
                  }

                  IDataTable swStatusTable = SessionManager.callWebService(request, "Softwarestatus.list", e, filterList, (List)null, (Map)null, Integer.valueOf(1));
                  String sortColumn = request.getParameter("sortcolumn");
                  if(!Util.stringNullOrEmpty(sortColumn)) {
                     DataTableSortSpec sortSpec = new DataTableSortSpec(swStatusTable);
                     sortSpec.column(sortColumn, OrderDirection.ASC);
                     sortSpec.freeze();
                     swStatusTable = DataTableUtil.sort(sortSpec);
                  }

                  CommonResponseFrame.printToResponse(this.getSwStatusResponse(swStatusTable, filterName, filterType, filterFileName, config), response);
               } catch (Exception var13) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var13));
                  CommonResponseFrame.printToResponse(this.getErrorResponse("Could not execute the software status application because: " + var13.getMessage()), response);
               }
            } else if(mode.equals("EXPORT_STATUS")) {
               try {
                  getSwStatusDataAndAddToResponse(request, config, response, (String)null);
               } catch (Exception var14) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Error at software status export: " + Util.exceptionToString(var14));
                  CommonResponseFrame.printToResponse(this.getErrorResponse("Could not execute the software status application because: " + Util.recursiveExceptionMessageToString(var14)), response);
               }
            }
         } else {
            CommonResponseFrame.printToResponse(this.getPageEntryResponse(config), response);
         }
      }
   }

   public static void getSwStatusDataAndAddToResponse(HttpServletRequest request, Configuration config, HttpServletResponse response, String onlySingleType) throws IOException {
      String tempDir = config.getTempDir();
      File tempDirFile = new File(tempDir, UUID.randomUUID().toString());

      try {
         tempDirFile.mkdirs();
         gatherSwStatusData(tempDirFile, config, request, onlySingleType);
         response.setContentType("application/octet-stream");
         response.setHeader("Content-Disposition", "attachment; filename=\"softwarestatus.zip\"");
         ServletOutputStream outStr = null;

         try {
            outStr = response.getOutputStream();
            ZipUtil.zip(tempDirFile, tempDirFile.getAbsolutePath() + File.separator, outStr);
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished writing sw status response");
         } finally {
            if(outStr != null) {
               try {
                  outStr.close();
               } catch (IOException var25) {
                  ;
               }
            }

         }
      } finally {
         if(tempDirFile.exists()) {
            try {
               FileSystemUtil.deleteDir(tempDirFile);
            } catch (Exception var24) {
               ;
            }
         }

      }

   }

   private static void gatherSwStatusData(File tempDirFile, Configuration config, HttpServletRequest request, String onlySingleType) {
      try {
         tempDirFile.mkdirs();
         if(onlySingleType == null) {
            addLegacySwStatus(tempDirFile, request);
            addMaintMgrData(config, tempDirFile);
            addTcLogs(config, tempDirFile);
            addJhydraLogs(config, tempDirFile);
            addJhydraConfs(config, tempDirFile);
         } else {
            if("SW_STATUS".equals(onlySingleType)) {
               addLegacySwStatus(tempDirFile, request);
            }

            if("MAINT_MGR_VERSIONS".equals(onlySingleType)) {
               addMaintMgrData(config, tempDirFile);
            }

            if("TC_LOGS".equals(onlySingleType)) {
               addTcLogs(config, tempDirFile);
            }

            if("JH_LOGS".equals(onlySingleType)) {
               addJhydraLogs(config, tempDirFile);
            }

            if("JH_CONFS".equals(onlySingleType)) {
               addJhydraConfs(config, tempDirFile);
            }
         }

      } catch (Exception var5) {
         throw new RuntimeException("Error gathering data for sw status export", var5);
      }
   }

   private static void addLegacySwStatus(File tempDirFile, HttpServletRequest request) {
      LinkedList columnList = new LinkedList();
      columnList.add("softwarestatus.zipped_data");
      IDataTable swStatusTable = SessionManager.callWebService(request, "Softwarestatus.export", columnList, (List)null, (List)null, (Map)null, Integer.valueOf(1));
      if(swStatusTable.getRowCount() != 1) {
         throw new RuntimeException("Could not complete export of softwarestatus because the result table from webservice has another number of rows than 1");
      } else if(swStatusTable.probeColIdx("softwarestatus.zipped_data") == -1) {
         throw new RuntimeException("Could not complete export of softwarestatus because the result table from webservice does not contain the needed column");
      } else {
         byte[] data = (byte[])swStatusTable.getCellValue(0, "softwarestatus.zipped_data", byte[].class);
         if(data == null) {
            throw new RuntimeException("Could not complete export of softwarestatus because the result table from webservice has an empty value in the data column");
         } else {
            try {
               ZipUtil.unzip(data, tempDirFile.getAbsolutePath());
            } catch (IOException var6) {
               throw new RuntimeException("Could not extract the zipped SW status from server", var6);
            }
         }
      }
   }

   private static void addJhydraLogs(Configuration config, File tempDirFile) {
      try {
         File e = new File(config.getjHydraDir());
         File jhydraLogDir = new File(e, "MOC/1/err");
         Calendar oneWeekAgo = DateTimeUtil.createCalendar(Calendar.getInstance(), 6, -7);
         long oneWeekAgoMillis = oneWeekAgo.getTimeInMillis();
         if(jhydraLogDir.exists()) {
            File[] logFiles = jhydraLogDir.listFiles();
            TreeMap filesToUse = new TreeMap();
            if(logFiles != null) {
               for(int destDir = 0; destDir < logFiles.length; ++destDir) {
                  File filesReverseOrder = logFiles[destDir];
                  if(filesReverseOrder.isFile() && filesReverseOrder.lastModified() >= oneWeekAgoMillis) {
                     filesToUse.put(Long.valueOf(filesReverseOrder.lastModified()), filesReverseOrder);
                  }
               }
            }

            if(filesToUse.size() > 0) {
               File var16 = new File(tempDirFile, "Logs-Jhydra");
               var16.mkdirs();
               LinkedList var17 = new LinkedList();
               Iterator it = filesToUse.keySet().iterator();

               while(it.hasNext()) {
                  Long count = (Long)it.next();
                  var17.add(filesToUse.get(count));
               }

               int var18 = 0;

               for(int i = var17.size() - 1; i >= 0 && var18 < 3; --i) {
                  File f = (File)var17.get(i);
                  FileSystemUtil.copyFile(f, new File(var16, f.getName()), true);
                  ++var18;
               }
            }
         }

      } catch (Exception var15) {
         throw new RuntimeException("Error adding JHYDRA logs to SW status", var15);
      }
   }

   private static void addJhydraConfs(Configuration config, File tempDirFile) {
      try {
         File e = new File(config.getjHydraDir());
         File globalConf = new File(e, "MOC/config.properties");
         File instConf = new File(e, "MOC/1/config.properties");
         File destDir = new File(tempDirFile, "Conf-Jhydra");
         destDir.mkdirs();
         FileSystemUtil.copyFile(globalConf, new File(destDir, "config.global.properties"), true);
         FileSystemUtil.copyFile(instConf, new File(destDir, "config.instance.properties"), true);
      } catch (Exception var6) {
         throw new RuntimeException("Error adding JHYDRA configs to SW status", var6);
      }
   }

   private static void addTcLogs(Configuration config, File tempDirFile) {
      try {
         File e = new File(config.getTomcatDir());
         File tcLogDir = new File(e, "logs");
         Calendar oneWeekAgo = DateTimeUtil.createCalendar(Calendar.getInstance(), 6, -7);
         long oneWeekAgoMillis = oneWeekAgo.getTimeInMillis();
         if(tcLogDir.exists()) {
            File[] logFiles = tcLogDir.listFiles();
            LinkedList filesToUse = new LinkedList();
            if(logFiles != null) {
               for(int tcLogDestDir = 0; tcLogDestDir < logFiles.length; ++tcLogDestDir) {
                  File i = logFiles[tcLogDestDir];
                  if(i.isFile() && i.lastModified() >= oneWeekAgoMillis) {
                     filesToUse.add(i);
                  }
               }
            }

            if(filesToUse.size() > 0) {
               File var13 = new File(tempDirFile, "Logs-TC");
               var13.mkdirs();

               for(int var14 = 0; var14 < filesToUse.size(); ++var14) {
                  File f = (File)filesToUse.get(var14);
                  FileSystemUtil.copyFile(f, new File(var13, f.getName()), true);
               }
            }
         }

      } catch (Exception var12) {
         throw new RuntimeException("Error adding TC logs to SW status", var12);
      }
   }

   private static void addMaintMgrData(Configuration config, File tempDirFile) {
      try {
         StringBuilder e = new StringBuilder();
         File[] clientPackages = (new File(config.getUpdateDirClient())).listFiles();
         e.append("PackageName|Description|CreationTs|DeploymentTs|AppName|\n");
         String availableDeploymentTypes;
         String deplTypeIt;
         String deploymentType;
         String deplMeta;
         if(clientPackages != null) {
            LinkedHashMap javaInfoBuilder = new LinkedHashMap();

            for(int javaUpdatePackages = 0; javaUpdatePackages < clientPackages.length; ++javaUpdatePackages) {
               if(!clientPackages[javaUpdatePackages].getName().equals("Backups") && clientPackages[javaUpdatePackages].isDirectory()) {
                  try {
                     ClientPackageMeta versionInfoBuilder = ClientPackageMeta.loadPackageMeta(clientPackages[javaUpdatePackages].getAbsolutePath());
                     String rtDir = versionInfoBuilder.getName().replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\\|", "\\\\|");
                     availableDeploymentTypes = versionInfoBuilder.getDescription().replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\\|", "\\\\|");
                     deplTypeIt = versionInfoBuilder.getApplicationName().replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\\|", "\\\\|");
                     deploymentType = DateTimeUtil.calendarToPrintString(versionInfoBuilder.getCreationDate());
                     deplMeta = DateTimeUtil.calendarToPrintString(versionInfoBuilder.getDeploymentDate());
                     e.append(rtDir + "|" + availableDeploymentTypes + "|" + deploymentType + "|" + deplMeta + "|" + deplTypeIt + "|\n");
                     StringBuilder warName = new StringBuilder();
                     warName.append("Domain|Version|\n");
                     List warExists = versionInfoBuilder.getDomains();

                     for(int appDirInRtExists = 0; appDirInRtExists < warExists.size(); ++appDirInRtExists) {
                        String e1 = (String)warExists.get(appDirInRtExists);
                        String i = (String)versionInfoBuilder.getDomainVersions().get(e1);
                        if(i == null) {
                           i = "";
                        }

                        warName.append(e1.replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\\|", "\\\\|"));
                        warName.append("|");
                        warName.append(i.replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\\|", "\\\\|"));
                        warName.append("|\n");
                     }

                     javaInfoBuilder.put(DateTimeUtil.calendarUtcToIsoDateString(versionInfoBuilder.getCreationDate()) + "-" + versionInfoBuilder.getName(), warName.toString());
                  } catch (Exception var29) {
                     ;
                  }
               }
            }

            Iterator var32 = javaInfoBuilder.keySet().iterator();

            while(var32.hasNext()) {
               try {
                  String var34 = (String)var32.next();
                  FileSystemUtil.writeTextFile(tempDirFile, "MaintMgr-Client-Package-" + var34.replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\\|", "\\\\|") + "-Versions.lst", (String)javaInfoBuilder.get(var34));
               } catch (IOException var26) {
                  ;
               }
            }
         }

         try {
            FileSystemUtil.writeTextFile(tempDirFile, "MaintMgr-Client-Packages.lst", e.toString());
         } catch (IOException var25) {
            ;
         }

         StringBuilder var31 = new StringBuilder();
         File[] var33 = (new File(config.getUpdateDirServer())).listFiles();
         var31.append("PackageName|Description|CreationTs|DeploymentTs|AppName|\n");
         String var44;
         List var49;
         int var50;
         if(var33 != null) {
            LinkedHashMap var35 = new LinkedHashMap();

            for(int var37 = 0; var37 < var33.length; ++var37) {
               if(!var33[var37].getName().equals("Backups") && var33[var37].isDirectory()) {
                  try {
                     PackageMeta var40 = PackageMeta.loadPackageMeta(var33[var37].getAbsolutePath());
                     deplTypeIt = var40.getName().replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\\|", "\\\\|");
                     deploymentType = var40.getDescription().replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\\|", "\\\\|");
                     deplMeta = var40.getApplicationName().replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\\|", "\\\\|");
                     var44 = DateTimeUtil.calendarToPrintString(var40.getCreationDate());
                     String var45 = DateTimeUtil.calendarToPrintString(var40.getDeploymentDate());
                     var31.append(deplTypeIt + "|" + deploymentType + "|" + var44 + "|" + var45 + "|" + deplMeta + "|\n");
                     StringBuilder var47 = new StringBuilder();
                     var47.append("Domain|Version|\n");
                     var49 = var40.getElements();

                     for(var50 = 0; var50 < var49.size(); ++var50) {
                        String info = ((PackageElement)var49.get(var50)).getElementName();
                        String i1 = ((PackageElement)var49.get(var50)).getVersion();
                        if(i1 == null) {
                           i1 = "";
                        }

                        var47.append(info.replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\\|", "\\\\|"));
                        var47.append("|");
                        var47.append(i1.replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\\|", "\\\\|"));
                        var47.append("|\n");
                     }

                     var35.put(DateTimeUtil.calendarUtcToIsoDateString(var40.getCreationDate()) + "-" + var40.getName(), var47.toString());
                  } catch (Exception var28) {
                     ;
                  }
               }
            }

            Iterator var38 = var35.keySet().iterator();

            while(var38.hasNext()) {
               try {
                  availableDeploymentTypes = (String)var38.next();
                  FileSystemUtil.writeTextFile(tempDirFile, "MaintMgr-Java-Package-" + availableDeploymentTypes.replaceAll("\r\n", "\\\\r\\\\n").replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\\|", "\\\\|") + "-Versions.lst", (String)var35.get(availableDeploymentTypes));
               } catch (IOException var24) {
                  ;
               }
            }
         }

         try {
            FileSystemUtil.writeTextFile(tempDirFile, "MaintMgr-Java-Packages.lst", var31.toString());
         } catch (IOException var23) {
            ;
         }

         try {
            FileSystemUtil.writeTextFile(tempDirFile, "MaintMgr-config.txt", config.toString());
         } catch (IOException var22) {
            ;
         }

         StringBuilder var36 = new StringBuilder();
         File var39 = new File(config.getRuntimeDirServer());
         Set var41 = getAvailableDeploymentTypes(var39);
         Iterator var42 = var41.iterator();

         while(var42.hasNext()) {
            deploymentType = (String)var42.next();
            DeploymentMeta var43 = null;

            try {
               var43 = DeploymentMeta.loadDeploymentMeta(new File(var39, deploymentType));
            } catch (Exception var21) {
               ;
            }

            var44 = null;
            if(var43 != null) {
               var44 = var43.getWarName();
            }

            boolean var46 = false;
            if(var44 != null && !"".equals(var44) && (new File(config.getTomcatDir() + File.separator + "webapps" + File.separator + var44 + ".war")).exists()) {
               var46 = true;
            }

            boolean var48 = false;
            if((new File(var39, deploymentType)).exists()) {
               var48 = true;
            }

            try {
               if(var46 && var48) {
                  var49 = VersionInformationRetriever.getVersionInformationFolder((new File(var39, deploymentType)).getAbsolutePath(), config.getTempDir());
                  List var53 = VersionInformationRetriever.getVersionInformationWarFile((new File(config.getTomcatDir() + File.separator + "webapps" + File.separator + var44 + ".war")).getAbsolutePath(), config.getTempDir());
                  List var52 = VersionInformationRetriever.compareVersions(var49, var53);
                  var36.append("JarFile|Vendor|Title|VersionRt|ChangeDataRt|VersionActive|ChangeDataActive|\n");

                  for(int var54 = 0; var54 < var52.size(); ++var54) {
                     VersionComparisonInfo info1 = (VersionComparisonInfo)var52.get(var54);
                     var36.append(info1.getFileName() + "|" + info1.getVendor() + "|" + info1.getTitle() + "|" + info1.getLeftVersionString() + "|" + info1.getLeftChangeDate() + "|" + info1.getRightVersionString() + "|" + info1.getRightChangeDate() + "|\n");
                  }
               } else {
                  VersionInfo var51;
                  if(var48) {
                     var49 = VersionInformationRetriever.getVersionInformationFolder((new File(var39, deploymentType)).getAbsolutePath(), config.getTempDir());
                     var36.append("JarFile|Vendor|Title|Version|ChangeData|\n");

                     for(var50 = 0; var50 < var49.size(); ++var50) {
                        var51 = (VersionInfo)var49.get(var50);
                        var36.append(var51.getFileName() + "|" + var51.getVendor() + "|" + var51.getTitle() + "|" + var51.getVersionString() + "|" + var51.getChangeDate() + "|\n");
                     }
                  } else if(var46) {
                     var49 = VersionInformationRetriever.getVersionInformationWarFile((new File(config.getTomcatDir() + File.separator + "webapps" + File.separator + var44 + ".war")).getAbsolutePath(), config.getTempDir());
                     var36.append("JarFile|Vendor|Title|Version|ChangeData|\n");

                     for(var50 = 0; var50 < var49.size(); ++var50) {
                        var51 = (VersionInfo)var49.get(var50);
                        var36.append(var51.getFileName() + "|" + var51.getVendor() + "|" + var51.getTitle() + "|" + var51.getVersionString() + "|" + var51.getChangeDate() + "|\n");
                     }
                  }
               }
            } catch (Exception var27) {
               ;
            }

            if(var36.length() > 0) {
               try {
                  FileSystemUtil.writeTextFile(tempDirFile, "MaintMgr-Versions-" + deploymentType + ".lst", var36.toString());
               } catch (IOException var20) {
                  ;
               }
            }
         }

      } catch (Exception var30) {
         throw new RuntimeException("Error adding maintenance manager data to SW status", var30);
      }
   }

   private static Set getAvailableDeploymentTypes(File rtDir) {
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

   private void checkParams(Configuration config, String mode) {
      if(mode != null && !mode.equals("")) {
         if(!mode.equals("LIST_STATUS") && !mode.equals("EXPORT_STATUS")) {
            throw new IllegalArgumentException("Unknown mode specified: " + mode + ". Allowed modes are: " + "LIST_STATUS" + ", " + "EXPORT_STATUS");
         } else {
            String tomcatHostPort = config.getTomcatHostPort();
            if(tomcatHostPort != null && !tomcatHostPort.equals("")) {
               if(mode.equals("EXPORT_STATUS")) {
                  String updDirPath = config.getUpdateDirServer();
                  if(updDirPath == null || updDirPath.equals("")) {
                     throw new IllegalStateException("The JAVA update dir is not configured");
                  }

                  File updDir = new File(updDirPath);
                  if(!updDir.exists()) {
                     throw new IllegalStateException("The JAVA update dir does not exist: " + updDir.getAbsolutePath());
                  }

                  String clUpdDirPath = config.getUpdateDirClient();
                  if(clUpdDirPath == null || clUpdDirPath.equals("")) {
                     throw new IllegalStateException("The client update dir is not configured");
                  }

                  File clUpdDir = new File(clUpdDirPath);
                  if(!clUpdDir.exists()) {
                     throw new IllegalStateException("The client update dir does not exist: " + clUpdDir.getAbsolutePath());
                  }

                  String tempDirPath = config.getTempDir();
                  if(tempDirPath == null || tempDirPath.equals("")) {
                     throw new IllegalStateException("The temp dir is not configured");
                  }

                  File tempDir = new File(tempDirPath);
                  if(!tempDir.exists()) {
                     throw new IllegalStateException("The temp dir does not exist: " + tempDir.getAbsolutePath());
                  }

                  String rtDir = config.getRuntimeDirServer();
                  if(rtDir == null || rtDir.equals("")) {
                     throw new IllegalStateException("The JAVA runtime dir is not configured");
                  }

                  File rtDirFile = new File(rtDir);
                  if(!rtDirFile.exists()) {
                     throw new IllegalStateException("The JAVA runtime dir does not exist: " + rtDirFile.getAbsolutePath());
                  }

                  String tcDir = config.getTomcatDir();
                  if(tcDir == null || tcDir.equals("")) {
                     throw new IllegalStateException("The tomcat dir is not configured");
                  }

                  File tcDirFile = new File(tcDir);
                  if(!tcDirFile.exists()) {
                     throw new IllegalStateException("The tomcat dir does not exist: " + tcDirFile.getAbsolutePath());
                  }

                  String jhDir = config.getjHydraDir();
                  if(jhDir == null || jhDir.equals("")) {
                     throw new IllegalStateException("The jhydra dir is not configured");
                  }

                  File jhDirFile = new File(jhDir);
                  if(!jhDirFile.exists()) {
                     throw new IllegalStateException("The jhydradir dir does not exist: " + jhDirFile.getAbsolutePath());
                  }
               }

            } else {
               throw new IllegalStateException("The tomcat host/port is not configured");
            }
         }
      } else {
         throw new IllegalArgumentException("The mode is not specified");
      }
   }

   private String getErrorResponse(String message) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Software status</h1>\n");
      builder.append("<br />\n");
      builder.append("<span style=\"color:red;\">An error has occured: " + message + "</span><br />");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getSwStatusResponse(IDataTable swStatusTable, String filterName, String filterType, String filterFileName, Configuration config) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;height:667px;overflow-y:scroll;overflow-x:scroll;\">\n");
      builder.append("<h1>Software status</h1>\n");
      builder.append("<form action=\"SoftwareStatus\" method=\"post\">\n");
      builder.append("<div style=\"width:100%;\">");
      builder.append("<div style=\"width:32%;float:left;text-align:left;\">&nbsp;</div>");
      builder.append("<div style=\"width:17%;float:left;text-align:left;\">Filter by name:</div>");
      builder.append("<div style=\"width:4%;float:left;\">&nbsp;</div>");
      builder.append("<div style=\"width:47%;float:left;text-align:left;\"><input type=\"text\" name=\"filtername\" value=\"" + (filterName == null?"":filterName) + "\"/></div>");
      builder.append("<div style=\"width:32%;float:left;text-align:left;\">&nbsp;</div>");
      builder.append("<div style=\"width:17%;float:left;text-align:left;\">Filter by type:</div>");
      builder.append("<div style=\"width:4%;float:left;\">&nbsp;</div>");
      builder.append("<div style=\"width:47%;float:left;text-align:left;\"><input type=\"text\" name=\"filtertype\" value=\"" + (filterType == null?"":filterType) + "\"/></div>");
      builder.append("<div style=\"width:32%;float:left;text-align:left;\">&nbsp;</div>");
      builder.append("<div style=\"width:17%;float:left;text-align:left;\">Filter by file name:</div>");
      builder.append("<div style=\"width:4%;float:left;\">&nbsp;</div>");
      builder.append("<div style=\"width:47%;float:left;text-align:left;\"><input type=\"text\" name=\"filterfilename\" value=\"" + (filterFileName == null?"":filterFileName) + "\"/></div>");
      builder.append("</div>");
      builder.append("<br /><br /><br /><br />");
      builder.append("  <input type=\"hidden\" name=\"mode\" value=\"LIST_STATUS\"/>\n");
      builder.append("  <input type=\"submit\" name=\"Submit\" value=\"Show software status\"/>\n");
      builder.append("</form>\n");
      builder.append("<form action=\"SoftwareStatus\" method=\"post\">\n");
      builder.append("  <input type=\"hidden\" name=\"mode\" value=\"EXPORT_STATUS\"/>\n");
      builder.append("  <input type=\"submit\" name=\"Submit\" value=\"Export software status to Zip file\"/><br />\n");
      builder.append("</form>\n");
      builder.append("<span style=\"color:red;\">PLEASE NOTE: The software status export can take a couple of minutes.<br />Please don\'t leave the page before it is finished!</span><br />");
      builder.append("<script type=\"text/javascript\">\n");
      builder.append("function showstatusinnewwindow()");
      builder.append("{");
      builder.append("windowopts = \"toolbar=no,scrollbars=yes,location=0,statusbar=no,menubar=no,resizable=yes,outerWidth=1024,outerHeight=768,width=1024,height=768\";");
      builder.append("imgwindow = window.open(\"\", \"\", windowopts);");
      builder.append("imgwindow.focus();");
      builder.append("imgwindow.document.open();");
      builder.append("var tablecontent=document.getElementById(\"statustable\").innerHTML;");
      builder.append("with(imgwindow)");
      builder.append("{");
      builder.append("    document.write(\"<!DOCTYPE HTML PUBLIC \\\"-//W3C//DTD HTML 4.01 Transitional//EN\\\" \\\"http://www.w3.org/TR/html4/loose.dtd\\\">\");");
      builder.append("    document.write(\"<html>\");");
      builder.append("    document.write(\" <head>\");");
      builder.append("    document.write(\"  <title>MPDV Maintenance Manager - Software status</title>\");");
      builder.append("    document.write(\" </head>\");");
      builder.append("    document.write(\" <body>\");");
      builder.append("    document.write(\" <div style=\\\"text-align:center;width:100%;\\\">\");");
      builder.append("    document.write(\" <h1>Software status</h1>\");");
      builder.append("    document.write(\" <table border=\\\"2\\\" style=\\\"margin-left:auto;margin-right:auto;text-align:left;\\\">\");");
      builder.append("    document.write(\" <tr>\");");
      builder.append("    document.write(\" <td style=\\\"background-color:grey;\\\"><b>Filter by name</b></td>\");");
      builder.append("    document.write(\" <td>" + (filterName == null?"&nbsp;":filterName) + "</td>\");");
      builder.append("    document.write(\" </tr>\");");
      builder.append("    document.write(\" <tr>\");");
      builder.append("    document.write(\" <td style=\\\"background-color:grey;\\\"><b>Filter by type</b></td>\");");
      builder.append("    document.write(\" <td>" + (filterType == null?"&nbsp;":filterType) + "</td>\");");
      builder.append("    document.write(\" </tr>\");");
      builder.append("    document.write(\" <tr>\");");
      builder.append("    document.write(\" <td style=\\\"background-color:grey;\\\"><b>Filter by file name</b></td>\");");
      builder.append("    document.write(\" <td>" + (filterFileName == null?"&nbsp;":filterFileName) + "</td>\");");
      builder.append("    document.write(\" </tr>\");");
      builder.append("    document.write(\" </table>\");");
      builder.append("    document.write(\"<br /><br />\");");
      builder.append("    document.write(\" <table border=\\\"2\\\" style=\\\"margin-left:auto;margin-right:auto;font-size:70%;text-align:left;\\\">\");");
      builder.append("    document.write(tablecontent);");
      builder.append("    document.getElementById(\"thname\").innerHTML=\"Name\";");
      builder.append("    document.getElementById(\"thtype\").innerHTML=\"Type\";");
      builder.append("    document.getElementById(\"thprogver\").innerHTML=\"Program Version\";");
      builder.append("    document.getElementById(\"thprogdate\").innerHTML=\"Program Date\";");
      builder.append("    document.getElementById(\"thcompdate\").innerHTML=\"Compilation Date\";");
      builder.append("    document.getElementById(\"thfname\").innerHTML=\"File name\";");
      builder.append("    document.getElementById(\"thfct\").innerHTML=\"Function\";");
      builder.append("    document.getElementById(\"thlogtime\").innerHTML=\"Log Time\";");
      builder.append("    document.getElementById(\"thnostarts\").innerHTML=\"Number of starts\";");
      builder.append("    document.getElementById(\"therrtime\").innerHTML=\"Error Time\";");
      builder.append("    document.getElementById(\"therrcode\").innerHTML=\"Error Code\";");
      builder.append("    document.getElementById(\"therrmsg\").innerHTML=\"Error Message\";");
      builder.append("    document.write(\" </table>\");");
      builder.append("    document.write(\" </div>\");");
      builder.append("    document.write(\" </body>\");");
      builder.append("    document.write(\"</html>\");");
      builder.append("}");
      builder.append("}");
      builder.append("</script>\n");
      builder.append("<input type=\"button\" name=\"newWindow\" onClick=\"showstatusinnewwindow()\" value=\"Show table in new Window\" /><br /><br />\n");
      builder.append("<table border=\"2\" id=\"statustable\" name=\"statustable\" style=\"margin-left:auto;margin-right:auto;font-size:70%;text-align:left;\">\n");
      builder.append("<tr style=\"background-color:grey;\">\n");
      builder.append("<th colspan=\"7\">Program</th>\n");
      builder.append("<th colspan=\"2\">Log</th>\n");
      builder.append("<th colspan=\"3\">Error</th>\n");
      builder.append("</tr>\n");
      builder.append("<tr style=\"background-color:grey;\">\n");
      builder.append("<th id=\"thname\">" + this.getSortHeaderHtml(filterName, filterType, filterFileName, "Name", "softwarestatus.name") + "</th>\n");
      builder.append("<th id=\"thtype\">" + this.getSortHeaderHtml(filterName, filterType, filterFileName, "Type", "softwarestatus.file_type") + "</th>\n");
      builder.append("<th id=\"thprogver\">" + this.getSortHeaderHtml(filterName, filterType, filterFileName, "Program Version", "softwarestatus.file_version") + "</th>\n");
      builder.append("<th id=\"thprogdate\">" + this.getSortHeaderHtml(filterName, filterType, filterFileName, "Program Date", "softwarestatus.file_date") + "</th>\n");
      builder.append("<th id=\"thcompdate\">" + this.getSortHeaderHtml(filterName, filterType, filterFileName, "Compilation Date", "softwarestatus.file_compilation") + "</th>\n");
      builder.append("<th id=\"thfname\">" + this.getSortHeaderHtml(filterName, filterType, filterFileName, "File name", "softwarestatus.file_name") + "</th>\n");
      builder.append("<th id=\"thfct\">" + this.getSortHeaderHtml(filterName, filterType, filterFileName, "Function", "softwarestatus.file_function") + "</th>\n");
      builder.append("<th id=\"thlogtime\">" + this.getSortHeaderHtml(filterName, filterType, filterFileName, "Log Time", "softwarestatus.logged_ts") + "</th>\n");
      builder.append("<th id=\"thnostarts\">" + this.getSortHeaderHtml(filterName, filterType, filterFileName, "Number of starts", "softwarestatus.logged_count") + "</th>\n");
      builder.append("<th id=\"therrtime\">" + this.getSortHeaderHtml(filterName, filterType, filterFileName, "Error Time", "softwarestatus.error_ts") + "</th>\n");
      builder.append("<th id=\"therrcode\">" + this.getSortHeaderHtml(filterName, filterType, filterFileName, "Error Code", "softwarestatus.error_code") + "</th>\n");
      builder.append("<th id=\"therrmsg\">" + this.getSortHeaderHtml(filterName, filterType, filterFileName, "Error Message", "softwarestatus.error_message") + "</th>\n");
      builder.append("</tr>\n");
      int count = swStatusTable.getRowCount();

      for(int i = 0; i < count; ++i) {
         builder.append("<tr>\n");
         builder.append("<td>" + (swStatusTable.getCellValue(i, "softwarestatus.name", String.class) == null?"":(String)swStatusTable.getCellValue(i, "softwarestatus.name", String.class)) + "</td>\n");
         builder.append("<td>" + (swStatusTable.getCellValue(i, "softwarestatus.file_type", String.class) == null?"":(String)swStatusTable.getCellValue(i, "softwarestatus.file_type", String.class)) + "</td>\n");
         builder.append("<td>" + (swStatusTable.getCellValue(i, "softwarestatus.file_version", String.class) == null?"":(String)swStatusTable.getCellValue(i, "softwarestatus.file_version", String.class)) + "</td>\n");
         builder.append("<td>" + (swStatusTable.getCellValue(i, "softwarestatus.file_date", Calendar.class) == null?"":DateTimeUtil.calendarToString((Calendar)swStatusTable.getCellValue(i, "softwarestatus.file_date", Calendar.class))) + "</td>\n");
         builder.append("<td>" + (swStatusTable.getCellValue(i, "softwarestatus.file_compilation", Calendar.class) == null?"":DateTimeUtil.calendarToString((Calendar)swStatusTable.getCellValue(i, "softwarestatus.file_compilation", Calendar.class))) + "</td>\n");
         builder.append("<td>" + (swStatusTable.getCellValue(i, "softwarestatus.file_name", String.class) == null?"":(String)swStatusTable.getCellValue(i, "softwarestatus.file_name", String.class)) + "</td>\n");
         builder.append("<td>" + (swStatusTable.getCellValue(i, "softwarestatus.file_function", String.class) == null?"":(String)swStatusTable.getCellValue(i, "softwarestatus.file_function", String.class)) + "</td>\n");
         builder.append("<td>" + (swStatusTable.getCellValue(i, "softwarestatus.logged_ts", Calendar.class) == null?"":DateTimeUtil.calendarToPrintString((Calendar)swStatusTable.getCellValue(i, "softwarestatus.logged_ts", Calendar.class))) + "</td>\n");
         builder.append("<td>" + (swStatusTable.getCellValue(i, "softwarestatus.logged_count", Integer.class) == null?"":(Serializable)swStatusTable.getCellValue(i, "softwarestatus.logged_count", Integer.class)) + "</td>\n");
         builder.append("<td>" + (swStatusTable.getCellValue(i, "softwarestatus.error_ts", Calendar.class) == null?"":DateTimeUtil.calendarToPrintString((Calendar)swStatusTable.getCellValue(i, "softwarestatus.error_ts", Calendar.class))) + "</td>\n");
         builder.append("<td>" + (swStatusTable.getCellValue(i, "softwarestatus.error_code", Integer.class) == null?"":(Serializable)swStatusTable.getCellValue(i, "softwarestatus.error_code", Integer.class)) + "</td>\n");
         builder.append("<td>" + (swStatusTable.getCellValue(i, "softwarestatus.error_message", String.class) == null?"":(String)swStatusTable.getCellValue(i, "softwarestatus.error_message", String.class)) + "</td>\n");
         builder.append("</tr>\n");
      }

      builder.append("</table>\n");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getSortHeaderHtml(String filterName, String filterType, String filterFileName, String columnName, String sortKey) {
      StringBuilder builder = new StringBuilder();
      builder.append("<form action=\"SoftwareStatus\" method=\"post\">\n");
      builder.append("<input type=\"hidden\" name=\"mode\" value=\"LIST_STATUS\"/>\n");
      if(filterName != null) {
         builder.append("<input type=\"hidden\" name=\"filtername\" value=\"" + filterName + "\"/>\n");
      }

      if(filterType != null) {
         builder.append("<input type=\"hidden\" name=\"filtertype\" value=\"" + filterType + "\"/>\n");
      }

      if(filterFileName != null) {
         builder.append("<input type=\"hidden\" name=\"filterfilename\" value=\"" + filterFileName + "\"/>\n");
      }

      builder.append("<input type=\"hidden\" name=\"sortcolumn\" value=\"" + sortKey + "\"/>\n");
      builder.append("<input type=\"submit\" name=\"Submit\" value=\"" + columnName + "\"/>\n");
      builder.append("</form>\n");
      return builder.toString();
   }

   private String getPageEntryResponse(Configuration config) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Software status</h1>\n");
      builder.append("<br />\n");
      builder.append("<form action=\"SoftwareStatus\" method=\"post\">\n");
      builder.append("<div style=\"width:100%;\">");
      builder.append("<div style=\"width:32%;float:left;text-align:left;\">&nbsp;</div>");
      builder.append("<div style=\"width:17%;float:left;text-align:left;\">Filter by name:</div>");
      builder.append("<div style=\"width:4%;float:left;\">&nbsp;</div>");
      builder.append("<div style=\"width:47%;float:left;text-align:left;\"><input type=\"text\" name=\"filtername\" /></div>");
      builder.append("<div style=\"width:32%;float:left;text-align:left;\">&nbsp;</div>");
      builder.append("<div style=\"width:17%;float:left;text-align:left;\">Filter by type:</div>");
      builder.append("<div style=\"width:4%;float:left;\">&nbsp;</div>");
      builder.append("<div style=\"width:47%;float:left;text-align:left;\"><input type=\"text\" name=\"filtertype\" /></div>");
      builder.append("<div style=\"width:32%;float:left;text-align:left;\">&nbsp;</div>");
      builder.append("<div style=\"width:17%;float:left;text-align:left;\">Filter by file name:</div>");
      builder.append("<div style=\"width:4%;float:left;\">&nbsp;</div>");
      builder.append("<div style=\"width:47%;float:left;text-align:left;\"><input type=\"text\" name=\"filterfilename\" /></div>");
      builder.append("</div>");
      builder.append("<br /><br /><br /><br />");
      builder.append("  <input type=\"hidden\" name=\"mode\" value=\"LIST_STATUS\"/>\n");
      builder.append("  <input type=\"submit\" name=\"Submit\" value=\"Show software status\"/>\n");
      builder.append("</form>\n");
      builder.append("<form action=\"SoftwareStatus\" method=\"post\">\n");
      builder.append("  <input type=\"hidden\" name=\"mode\" value=\"EXPORT_STATUS\"/>\n");
      builder.append("  <input type=\"submit\" name=\"Submit\" value=\"Export software status to Zip file\"/><br />\n");
      builder.append("</form>\n");
      builder.append("<span style=\"color:red;\">PLEASE NOTE: The software status export can take a couple of minutes.<br />Please don\'t leave the page before it is finished!</span><br />");
      builder.append("</div>\n");
      return builder.toString();
   }
}