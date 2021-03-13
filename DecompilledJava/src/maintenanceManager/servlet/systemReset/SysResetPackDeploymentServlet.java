package de.mpdv.maintenanceManager.servlet.systemReset;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.data.client.ClientPackageMeta;
import de.mpdv.maintenanceManager.data.javaServer.PackageMeta;
import de.mpdv.maintenanceManager.data.javaServer.VersionInfo;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.logic.client.ClientPackageDeployment;
import de.mpdv.maintenanceManager.logic.hydra.HydraPackageDeployment;
import de.mpdv.maintenanceManager.logic.javaServer.JavaPackageDeployment;
import de.mpdv.maintenanceManager.logic.javaServer.VersionInformationRetriever;
import de.mpdv.maintenanceManager.servlet.systemReset.SysResetPackDeploymentServlet.ParamStruct;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.Util;
import de.mpdv.maintenanceManager.util.ZipUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;
import org.xml.sax.SAXException;

public class SysResetPackDeploymentServlet extends HttpServlet {

   private static final long serialVersionUID = -8467492058570208909L;
   private static final String PACKAGE_EXTENSION = ".upd";
   private static final String SYS_RESET_TOOL_HASH = "c5f40773a0660abf0e9782485f1e35d1";

class ParamStruct {

   File updDirClient;
   File rtDirClient;
   File updDirServer;
   File rtDirServer;
   File tempDir;
   File jhydraDir;
   File tomcatDir;


}   
   

   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      boolean alreadyLoggedIn = true;
      if(!SessionManager.isLoggedIn(request)) {
         alreadyLoggedIn = false;
         SessionManager.internalLogin(request);
      }

      FileItem packageFile = null;

      try {
         Configuration config = null;

         try {
            config = Configuration.getConfiguration();
         } catch (Exception var41) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: " + Util.exceptionToString(var41));
            this.printToResponse(response, "ERROR:\n\nCould not get configuration\n" + Util.exceptionToString(var41));
            return;
         }

         String tempDirPath = config.getTempDir();
         if(tempDirPath == null || tempDirPath.length() == 0) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: The temp dir is not configured");
            this.printToResponse(response, "ERROR:\n\nThe temp dir is not configured");
         } else {
            File tempDir = new File(tempDirPath);
            if(!tempDir.exists()) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: The configured temp dir does not exist: " + tempDir.getAbsolutePath());
               this.printToResponse(response, "ERROR:\n\nThe configured temp dir does not exist: " + tempDir.getAbsolutePath());
            } else {
               String overrideVersionCheck = null;
               String internalPassword = null;
               DiskFileItemFactory struct;
               FileItem olderVersionResultMap;
               if(ServletFileUpload.isMultipartContent(request)) {
                  struct = this.newDiskFileItemFactory(this.getServletContext(), tempDir);
                  ServletFileUpload clientPackageList = new ServletFileUpload(struct);

                  try {
                     List javaPackageList = clientPackageList.parseRequest(request);
                     Iterator hydraPackageList = javaPackageList.iterator();

                     while(hydraPackageList.hasNext()) {
                        olderVersionResultMap = (FileItem)hydraPackageList.next();
                        if(!olderVersionResultMap.isFormField() && olderVersionResultMap.getFieldName().equals("package")) {
                           packageFile = olderVersionResultMap;
                        } else if(olderVersionResultMap.isFormField()) {
                           if(olderVersionResultMap.getFieldName().equals("overrideVersionCheck")) {
                              overrideVersionCheck = olderVersionResultMap.getString();
                           } else if(olderVersionResultMap.getFieldName().equals("internal_password")) {
                              internalPassword = olderVersionResultMap.getString();
                           }
                        }
                     }
                  } catch (FileUploadException var45) {
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: " + Util.exceptionToString(var45));
                     this.printToResponse(response, "ERROR:\n\nA file upload exception has occured\n" + Util.exceptionToString(var45));
                     return;
                  }
               } else {
                  overrideVersionCheck = request.getParameter("overrideVersionCheck");
                  internalPassword = request.getParameter("internal_password");
               }

               if(!"c5f40773a0660abf0e9782485f1e35d1".equals(internalPassword)) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: Internal password not specified or wrong!");
                  this.printToResponse(response, "ERROR:\n\nInternal password not specified or wrong!\n");
               } else {
                  struct = null;

                  ParamStruct struct1;
                  try {
                     struct1 = this.checkParams(config, packageFile);
                  } catch (Exception var42) {
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: " + Util.exceptionToString(var42));
                     this.printToResponse(response, "ERROR:\n\n" + Util.exceptionToString(var42));
                     return;
                  }

                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: Received request overrideVersionCheck=" + overrideVersionCheck + " package file supplied");
                  TreeMap clientPackageList1 = new TreeMap();
                  TreeMap javaPackageList1 = new TreeMap();
                  LinkedList hydraPackageList1 = new LinkedList();
                  olderVersionResultMap = null;
                  Map olderVersionResultMapClient = null;
                  List hydraInstallLogs = null;
                  File tempPackFolder = null;

                  try {
                     if(packageFile == null) {
                        this.printToResponse(response, "ERROR:\n\nThe package file to deploy is missing\n");
                     } else {
                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: Start deployment of package " + packageFile.getName());

                        try {
                           tempPackFolder = this.saveAndUnzip(packageFile, struct1);
                           System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: Finished save and unzip of package " + packageFile.getName());
                        } catch (Exception var43) {
                           System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: " + Util.exceptionToString(var43));
                           this.printToResponse(response, "ERROR:\n\nError saving/unzipping package\n" + Util.exceptionToString(var43));
                           return;
                        }

                        this.unzipAndGetPackageContents(tempPackFolder, clientPackageList1, javaPackageList1, hydraPackageList1);
                        if(clientPackageList1.size() == 0 && javaPackageList1.size() == 0 && hydraPackageList1.size() == 0) {
                           System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: No usable content in package found!");
                           this.printToResponse(response, "ERROR:\n\nNo usable content in package found!");
                        } else {
                           this.movePackagesToRightPlace(clientPackageList1, javaPackageList1, struct1);
                           Map olderVersionResultMap1 = this.deployJavaPackages(overrideVersionCheck, struct1, javaPackageList1);
                           olderVersionResultMapClient = this.deployClientPackages(overrideVersionCheck, struct1, clientPackageList1);
                           hydraInstallLogs = this.deployHydraPackages(struct1, hydraPackageList1, request);
                           if(tempPackFolder != null && tempPackFolder.exists()) {
                              FileSystemUtil.deleteDir(tempPackFolder);
                           }

                           this.printResult(javaPackageList1, olderVersionResultMap1, clientPackageList1, olderVersionResultMapClient, hydraInstallLogs, response);
                        }
                     }
                  } catch (Exception var44) {
                     this.handleError(struct1, javaPackageList1, clientPackageList1, tempPackFolder, var44, response);
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: " + Util.exceptionToString(var44));
                     this.printToResponse(response, "ERROR:\n\nError at deployment of packages\n" + Util.exceptionToString(var44));
                  }
               }
            }
         }
      } finally {
         if(packageFile != null) {
            try {
               packageFile.delete();
            } catch (Exception var40) {
               ;
            }
         }

         if(!alreadyLoggedIn) {
            SessionManager.logout(request);
         }

      }
   }

   private void handleError(ParamStruct struct, Map javaPackageList, Map clientPackageList, File tempPackFolder, Exception e, HttpServletResponse response) {
      Iterator it;
      String packFolder;
      if(clientPackageList != null) {
         it = clientPackageList.keySet().iterator();

         while(it.hasNext()) {
            packFolder = (String)it.next();

            try {
               ClientPackageMeta exc = ClientPackageMeta.loadPackageMeta((new File(struct.updDirClient, packFolder.substring(packFolder.indexOf(46) + 1))).getAbsolutePath());
               if(exc.getDeploymentDate() != null) {
                  ClientPackageDeployment.undeployPackage(struct.updDirClient, struct.rtDirClient, packFolder.substring(packFolder.indexOf(46) + 1));
               }

               FileSystemUtil.deleteDir(new File(struct.updDirClient, packFolder.substring(packFolder.indexOf(46) + 1)));
            } catch (Exception var11) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: Error at undeploy package for error case " + Util.exceptionToString(var11));
            }
         }
      }

      if(javaPackageList != null) {
         it = javaPackageList.keySet().iterator();

         while(it.hasNext()) {
            packFolder = (String)it.next();

            try {
               PackageMeta exc1 = PackageMeta.loadPackageMeta((new File(struct.updDirServer, packFolder.substring(packFolder.indexOf(46) + 1))).getAbsolutePath());
               if(exc1.getDeploymentDate() != null) {
                  JavaPackageDeployment.undeployPackage(struct.updDirServer, struct.rtDirServer, packFolder.substring(packFolder.indexOf(46) + 1));
               }

               FileSystemUtil.deleteDir(new File(struct.updDirServer, packFolder.substring(packFolder.indexOf(46) + 1)));
            } catch (Exception var10) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: Error at undeploy package for error case " + Util.exceptionToString(var10));
            }
         }
      }

      if(tempPackFolder != null && tempPackFolder.exists()) {
         FileSystemUtil.deleteDir(tempPackFolder);
      }

   }

   private void printResult(Map javaPackageList, Map olderVersionResultMap, Map clientPackageList, Map olderVersionResultMapClient, List hydraInstallLogs, HttpServletResponse response) {
      StringBuilder builder = new StringBuilder();
      builder.append("Deployment successfully finished\n");
      builder.append("--------------------------------\n\n\n");
      builder.append("Java packages:\n");
      builder.append("--------------\n\n");
      Iterator it = javaPackageList.keySet().iterator();

      String packFolder;
      while(it.hasNext()) {
         packFolder = (String)it.next();
         builder.append("\t- ").append(packFolder.substring(packFolder.indexOf(45) + 1)).append("\n");
         if(olderVersionResultMap.containsKey(packFolder.substring(packFolder.indexOf(46) + 1))) {
            builder.append("\t====================\n");
            builder.append((String)olderVersionResultMap.get(packFolder.substring(packFolder.indexOf(46) + 1)));
            builder.append("\n\n");
         }
      }

      builder.append("\n");
      builder.append("Client packages:\n");
      builder.append("--------------\n\n");
      it = clientPackageList.keySet().iterator();

      while(it.hasNext()) {
         packFolder = (String)it.next();
         builder.append("\t- ").append(packFolder.substring(packFolder.indexOf(45) + 1)).append("\n");
         if(olderVersionResultMapClient.containsKey(packFolder.substring(packFolder.indexOf(46) + 1))) {
            builder.append("\t====================\n");
            builder.append((String)olderVersionResultMapClient.get(packFolder.substring(packFolder.indexOf(46) + 1)));
            builder.append("\n\n");
         }
      }

      builder.append("\n");
      builder.append("Server packages:\n");
      builder.append("--------------\n\n");
      it = hydraInstallLogs.iterator();

      while(it.hasNext()) {
         builder.append("\t====================\n");
         builder.append((String)it.next());
         builder.append("\n\n");
      }

      this.printToResponse(response, builder.toString());
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

   private List deployHydraPackages(ParamStruct struct, List hydraPackageList, HttpServletRequest request) {
      LinkedList hydraInstallLogs = new LinkedList();
      if(hydraPackageList.size() > 0) {
         boolean useDirectDataMode = true;
         File systemDomJarFile = new File(struct.tomcatDir, "webapps/MocServices/WEB-INF/lib/MpdvDomSvcSystem.jar");
         if(systemDomJarFile.exists()) {
            LinkedList it2 = new LinkedList();
            it2.add(systemDomJarFile);

            try {
               List e = VersionInformationRetriever.getVersionInformationJarFiles(it2, struct.tempDir.getAbsolutePath());
               if(e.size() == 1) {
                  VersionInfo info = (VersionInfo)e.get(0);
                  if(info.getRevision() != null && info.getRevision().intValue() >= 2014) {
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: HYDRA install: Domain SvcSystem is in version 2014 or higher. Don\'t put data in service call.");
                     useDirectDataMode = false;
                  }
               }
            } catch (Exception var11) {
               ;
            }
         }

         Iterator it21 = hydraPackageList.iterator();

         while(it21.hasNext()) {
            try {
               File e1 = (File)it21.next();
               hydraInstallLogs.add(HydraPackageDeployment.deployPackage(e1, request, useDirectDataMode, struct.jhydraDir));
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: Finished HYDRA install for package " + e1.getName());
            } catch (Exception var10) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: ERROR - " + Util.exceptionToString(var10));
               throw new RuntimeException("Could not deploy HYDRA package because: " + var10.getMessage());
            }
         }
      }

      return hydraInstallLogs;
   }

   private Map deployClientPackages(String overrideVersionCheck, ParamStruct struct, Map clientPackageList) {
      HashMap olderVersionResultMapClient = new HashMap();
      List sameVersList = null;
      Iterator it = clientPackageList.keySet().iterator();

      while(it.hasNext()) {
         String packFolder = (String)it.next();
         packFolder = packFolder.substring(packFolder.indexOf(46) + 1);
         Map compList = null;

         try {
            ClientPackageMeta e = ClientPackageMeta.loadPackageMeta(struct.updDirClient + File.separator + packFolder);
            Map packageVersionMap = ClientPackageDeployment.getPackageDomainVersions(e);
            Map rtVersionMap = ClientPackageDeployment.getRuntimeDomainsVersions(new File(struct.rtDirClient, e.getApplicationName()), packageVersionMap);
            compList = ClientPackageDeployment.getOlderDomainsInPackage(packageVersionMap, rtVersionMap);
            sameVersList = ClientPackageDeployment.getSameVersionDomainsInPackage(packageVersionMap, rtVersionMap);
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: Finished version comparison of client package " + packFolder);
         } catch (Exception var12) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: " + Util.exceptionToString(var12));
            throw new RuntimeException("Could not deploy client package because: Could not check versions of package and runtime: " + var12.getMessage());
         }

         try {
            String e1 = ClientPackageDeployment.deployPackage(struct.updDirClient, struct.rtDirClient, packFolder, overrideVersionCheck != null && "true".equals(overrideVersionCheck), compList, sameVersList);
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: Result of client package deployment (" + packFolder + ")\n" + e1);
            if(e1 != null) {
               olderVersionResultMapClient.put(packFolder, e1);
            }
         } catch (Exception var13) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: " + Util.exceptionToString(var13));
            throw new RuntimeException("Could not deploy client package because: " + var13.getMessage());
         }
      }

      return olderVersionResultMapClient;
   }

   private Map deployJavaPackages(String overrideVersionCheck, ParamStruct struct, Map javaPackageList) {
      HashMap olderVersionResultMap = new HashMap();
      Iterator it = javaPackageList.keySet().iterator();

      while(it.hasNext()) {
         String packFolder = (String)it.next();
         packFolder = packFolder.substring(packFolder.indexOf(46) + 1);
         Map compList = null;
         List sameVersList = null;

         try {
            PackageMeta e = PackageMeta.loadPackageMeta(struct.updDirServer + File.separator + packFolder);
            Map packageVersionMap = JavaPackageDeployment.getPackageFileVersions(packFolder, struct.updDirServer, struct.tempDir.getAbsolutePath());
            Map rtVersionMap = JavaPackageDeployment.getRuntimeFileVersions(new File(struct.rtDirServer, e.getApplicationName()), packageVersionMap, struct.tempDir.getAbsolutePath());
            compList = JavaPackageDeployment.getOlderFilesInPackage(packageVersionMap, rtVersionMap);
            sameVersList = JavaPackageDeployment.getSameVersionFilesInPackage(packageVersionMap, rtVersionMap);
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: Finished version comparison of java package " + packFolder);
         } catch (Exception var12) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: " + Util.exceptionToString(var12));
            throw new RuntimeException("Could not deploy package because: Could not check versions of package and runtime: " + var12.getMessage());
         }

         try {
            String e1 = JavaPackageDeployment.deployPackage(struct.updDirServer, struct.rtDirServer, packFolder, overrideVersionCheck != null && "true".equals(overrideVersionCheck), compList, sameVersList);
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: Result of pacakge deployment (" + packFolder + ")\n" + e1);
            if(e1 != null) {
               olderVersionResultMap.put(packFolder, e1);
            }
         } catch (Exception var13) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetPackDeploy: " + Util.exceptionToString(var13));
            throw new RuntimeException("Could not deploy package because: " + var13.getMessage());
         }
      }

      return olderVersionResultMap;
   }

   private void movePackagesToRightPlace(Map clientPackageList, Map javaPackageList, ParamStruct struct) throws FileNotFoundException, IOException {
      Iterator it = clientPackageList.keySet().iterator();

      String packFolder;
      while(it.hasNext()) {
         packFolder = (String)it.next();
         (new File(struct.updDirClient, packFolder.substring(packFolder.indexOf(46) + 1))).getParentFile().mkdirs();
         ((File)clientPackageList.get(packFolder)).renameTo(new File(struct.updDirClient, packFolder.substring(packFolder.indexOf(46) + 1)));
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: Finished move of java package " + packFolder);
      }

      it = javaPackageList.keySet().iterator();

      while(it.hasNext()) {
         packFolder = (String)it.next();
         (new File(struct.updDirServer, packFolder.substring(packFolder.indexOf(46) + 1))).getParentFile().mkdirs();
         ((File)javaPackageList.get(packFolder)).renameTo(new File(struct.updDirServer, packFolder.substring(packFolder.indexOf(46) + 1)));
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: Finished move of client package " + packFolder);
      }

   }

   private void unzipAndGetPackageContents(File packTempFolder, Map clientPackageList, Map javaPackageList, List hydraPackageList) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException, ParseException {
      File javaPackFolder = new File(packTempFolder, "java");
      File clientPackFolder = new File(packTempFolder, "client");
      File serverPackFolder = new File(packTempFolder, "server");
      File[] packs;
      int i;
      File f;
      File packageFolder;
      if(javaPackFolder.exists()) {
         packs = javaPackFolder.listFiles();

         for(i = 0; i < packs.length; ++i) {
            f = packs[i];
            if(f.getName().toLowerCase().endsWith(".upd")) {
               packageFolder = new File(f.getParentFile(), f.getName().replace(".upd", ""));
               ZipUtil.unzip(f.getAbsolutePath(), packageFolder.getAbsolutePath(), false);
               PackageMeta meta = PackageMeta.loadPackageMeta(packageFolder.getAbsolutePath());
               javaPackageList.put(meta.getCreationDate().getTimeInMillis() + "." + DateTimeUtil.calendarUtcToIsoDateString(meta.getCreationDate()) + "-" + meta.getName(), packageFolder);
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: Finished unzip and loading contents of java package " + packageFolder.getName());
            }
         }
      }

      if(clientPackFolder.exists()) {
         packs = clientPackFolder.listFiles();

         for(i = 0; i < packs.length; ++i) {
            f = packs[i];
            if(f.getName().toLowerCase().endsWith(".upd")) {
               packageFolder = new File(f.getParentFile(), f.getName().replace(".upd", ""));
               ZipUtil.unzip(f.getAbsolutePath(), packageFolder.getAbsolutePath(), false);
               ClientPackageMeta var13 = ClientPackageMeta.loadPackageMeta(packageFolder.getAbsolutePath());
               clientPackageList.put(var13.getCreationDate().getTimeInMillis() + "." + DateTimeUtil.calendarUtcToIsoDateString(var13.getCreationDate()) + "-" + var13.getName(), packageFolder);
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - SysResetPackDeploy: Finished unzip and loading contents of client package " + packageFolder.getName());
            }
         }
      }

      if(serverPackFolder.exists()) {
         packs = serverPackFolder.listFiles();

         for(i = 0; i < packs.length; ++i) {
            f = packs[i];
            if(f.getName().toLowerCase().endsWith(".upd")) {
               hydraPackageList.add(f);
            }
         }
      }

   }

   private File saveAndUnzip(FileItem packageFile, ParamStruct struct) throws IOException {
      String tempName = UUID.randomUUID().toString();
      File unzipFolder = new File(struct.tempDir, tempName);
      if(!unzipFolder.mkdirs()) {
         throw new RuntimeException("Could not create unzip folder " + unzipFolder.getAbsolutePath());
      } else {
         ZipUtil.unzip(packageFile, unzipFolder.getAbsolutePath(), false);
         return unzipFolder;
      }
   }

   private ParamStruct checkParams(Configuration config, FileItem packageFile) {
      if(config == null) {
         throw new IllegalArgumentException("Could not get configuration");
      } else {
         String updDirPath = config.getUpdateDirServer();
         if(updDirPath != null && updDirPath.length() != 0) {
            File updDir = new File(updDirPath);
            if(!updDir.exists()) {
               throw new IllegalStateException("The configured update dir does not exist: " + updDir.getAbsolutePath());
            } else {
               String rtDirPath = config.getRuntimeDirServer();
               if(rtDirPath != null && rtDirPath.length() != 0) {
                  File rtDir = new File(rtDirPath);
                  if(!rtDir.exists()) {
                     throw new IllegalStateException("The configured runtime dir does not exist: " + rtDir.getAbsolutePath());
                  } else {
                     String updDirPathCl = config.getUpdateDirClient();
                     if(updDirPathCl != null && updDirPathCl.length() != 0) {
                        File updDirCl = new File(updDirPathCl);
                        if(!updDirCl.exists()) {
                           throw new IllegalStateException("The configured update dir does not exist: " + updDirCl.getAbsolutePath());
                        } else {
                           String rtDirPathCl = config.getRuntimeDirClient();
                           if(rtDirPathCl != null && rtDirPathCl.length() != 0) {
                              File rtDirCl = new File(rtDirPathCl);
                              if(!rtDirCl.exists()) {
                                 throw new IllegalStateException("The configured runtime dir does not exist: " + rtDirCl.getAbsolutePath());
                              } else {
                                 String tempDirPath = config.getTempDir();
                                 if(tempDirPath != null && tempDirPath.length() != 0) {
                                    File tempDir = new File(tempDirPath);
                                    if(!tempDir.exists()) {
                                       throw new IllegalStateException("The configured temp dir does not exist: " + tempDir.getAbsolutePath());
                                    } else {
                                       String tomcatHostPort = config.getTomcatHostPort();
                                       if(tomcatHostPort != null && tomcatHostPort.length() != 0) {
                                          if(packageFile == null) {
                                             throw new IllegalStateException("The package file is missing");
                                          } else if(!packageFile.getName().toLowerCase().endsWith(".upd")) {
                                             throw new IllegalStateException("The package file is not of type .upd");
                                          } else {
                                             String jHydradirPath = config.getjHydraDir();
                                             if(jHydradirPath != null && jHydradirPath.length() != 0) {
                                                File jHydradir = new File(jHydradirPath);
                                                if(!jHydradir.exists()) {
                                                   throw new IllegalStateException("The configured JHYDRADIR dir does not exist: " + jHydradir.getAbsolutePath());
                                                } else {
                                                   String tcDirPath = config.getTomcatDir();
                                                   if(tcDirPath != null && tcDirPath.length() != 0) {
                                                      File tcDir = new File(tcDirPath);
                                                      if(!tcDir.exists()) {
                                                         throw new IllegalStateException("The configured tomcat dir does not exist: " + tcDir.getAbsolutePath());
                                                      } else {
                                                         ParamStruct struct = new ParamStruct();
                                                         struct.updDirServer = updDir;
                                                         struct.rtDirServer = rtDir;
                                                         struct.updDirClient = updDirCl;
                                                         struct.rtDirClient = rtDirCl;
                                                         struct.tempDir = tempDir;
                                                         struct.jhydraDir = jHydradir;
                                                         struct.tomcatDir = tcDir;
                                                         return struct;
                                                      }
                                                   } else {
                                                      throw new IllegalStateException("The tomcat dir is not configured");
                                                   }
                                                }
                                             } else {
                                                throw new IllegalStateException("The JHYDRADIR dir is not configured");
                                             }
                                          }
                                       } else {
                                          throw new IllegalStateException("The tomcat host/port is not configured");
                                       }
                                    }
                                 } else {
                                    throw new IllegalStateException("The temp dir is not configured");
                                 }
                              }
                           } else {
                              throw new IllegalStateException("The runtime dir is not configured");
                           }
                        }
                     } else {
                        throw new IllegalStateException("The update dir is not configured");
                     }
                  }
               } else {
                  throw new IllegalStateException("The runtime dir is not configured");
               }
            }
         } else {
            throw new IllegalStateException("The update dir is not configured");
         }
      }
   }

   private DiskFileItemFactory newDiskFileItemFactory(ServletContext context, File repository) {
      FileCleaningTracker fileCleaningTracker = FileCleanerCleanup.getFileCleaningTracker(context);
      DiskFileItemFactory factory = new DiskFileItemFactory();
      factory.setFileCleaningTracker(fileCleaningTracker);
      factory.setRepository(repository);
      factory.setSizeThreshold(10240);
      return factory;
   }
}