package de.mpdv.maintenanceManager.servlet;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.data.client.ClientPackageMeta;
import de.mpdv.maintenanceManager.data.javaServer.PackageMeta;
import de.mpdv.maintenanceManager.data.javaServer.VersionInfo;
import de.mpdv.maintenanceManager.gui.CommonResponseFrame;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.logic.client.ClientPackageDeployment;
import de.mpdv.maintenanceManager.logic.hydra.HydraPackageDeployment;
import de.mpdv.maintenanceManager.logic.javaServer.JavaPackageDeployment;
import de.mpdv.maintenanceManager.logic.javaServer.VersionInformationRetriever;
import de.mpdv.maintenanceManager.servlet.UniversalPackageDeploymentServlet.ParamStruct;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.Util;
import de.mpdv.maintenanceManager.util.ZipUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;
import org.xml.sax.SAXException;

public class UniversalPackageDeploymentServlet extends HttpServlet {

   private static final long serialVersionUID = -4462895427927514662L;
   private static final String ACTION_DEPLOY = "DEPLOY";
   private static final String ACTION_DEPLOY2 = "DEPLOY2";
   private static final String ACTION_DEPLOY3 = "DEPLOY3";
   private static final String ACTION_DEPLOY4 = "DEPLOY4";
   private static final String PACKAGE_EXTENSION = ".upd";

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
      if(SessionManager.checkLogin(request, response)) {
         FileItem packageFile = null;

         try {
            File mm2ConfigFile = Configuration.getMM2ConfigFile();
            if(!mm2ConfigFile.exists()) {
               Configuration config = null;

               try {
                  config = Configuration.getConfiguration();
               } catch (Exception var38) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var38));
                  CommonResponseFrame.printToResponse(this.getErrorResponse("Could not get configuration: " + var38.getMessage()), response);
                  return;
               }

               String tempDirPath = config.getTempDir();
               if(tempDirPath != null && !tempDirPath.equals("")) {
                  File tempDir = new File(tempDirPath);
                  if(!tempDir.exists()) {
                     CommonResponseFrame.printToResponse(this.getErrorResponse("The configured temp dir does not exist: " + tempDir.getAbsolutePath()), response);
                     return;
                  }

                  String action = null;
                  String overrideVersionCheck = null;
                  String serialData = null;
                  DiskFileItemFactory struct;
                  ServletFileUpload fileName;
                  if(ServletFileUpload.isMultipartContent(request)) {
                     struct = this.newDiskFileItemFactory(this.getServletContext(), tempDir);
                     fileName = new ServletFileUpload(struct);

                     try {
                        List clientPackageList = fileName.parseRequest(request);
                        Iterator javaPackageList = clientPackageList.iterator();

                        while(javaPackageList.hasNext()) {
                           FileItem hydraPackageList = (FileItem)javaPackageList.next();
                           if(!hydraPackageList.isFormField() && hydraPackageList.getFieldName().equals("package")) {
                              packageFile = hydraPackageList;
                           } else if(hydraPackageList.isFormField()) {
                              if(hydraPackageList.getFieldName().equals("action")) {
                                 action = hydraPackageList.getString();
                              } else if(hydraPackageList.getFieldName().equals("overrideVersionCheck")) {
                                 overrideVersionCheck = hydraPackageList.getString();
                              } else if(hydraPackageList.getFieldName().equals("serialData")) {
                                 serialData = hydraPackageList.getString();
                              }
                           }
                        }
                     } catch (FileUploadException var40) {
                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var40));
                        CommonResponseFrame.printToResponse(this.getErrorResponse("A file upload exception has occured: " + var40.getMessage()), response);
                        return;
                     }
                  } else {
                     action = request.getParameter("action");
                     overrideVersionCheck = request.getParameter("overrideVersionCheck");
                     serialData = request.getParameter("serialData");
                  }

                  if(action != null && !action.equals("")) {
                     struct = null;

                     ParamStruct struct1;
                     try {
                        struct1 = this.checkParams(config, action, packageFile, serialData);
                     } catch (Exception var39) {
                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var39));
                        CommonResponseFrame.printToResponse(this.getErrorResponse(var39.getMessage()), response);
                        return;
                     }

                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Received request action=" + action + " overrideVersionCheck=" + overrideVersionCheck + (packageFile == null?" serial data supplied":" package file supplied"));
                     fileName = null;
                     TreeMap clientPackageList1 = new TreeMap();
                     TreeMap javaPackageList1 = new TreeMap();
                     LinkedList hydraPackageList1 = new LinkedList();
                     Map olderVersionResultMap = null;
                     Map olderVersionResultMapClient = null;
                     List hydraInstallLogs = null;
                     File tempPackFolder = null;

                     try {
                        String fileName1;
                        if(!action.equals("DEPLOY")) {
                           Map clientPackageList2;
                           Map javaPackageList2;
                           List hydraPackageList2;
                           Map e2;
                           if(action.equals("DEPLOY2")) {
                              e2 = this.deserializeData(serialData);
                              clientPackageList2 = (Map)e2.get("clientPackageList");
                              javaPackageList2 = (Map)e2.get("javaPackageList");
                              hydraPackageList2 = (List)e2.get("hydraPackageList");
                              olderVersionResultMap = (Map)e2.get("olderVersionResultMap");
                              olderVersionResultMapClient = (Map)e2.get("olderVersionResultMapClient");
                              hydraInstallLogs = (List)e2.get("hydraInstallLogs");
                              tempPackFolder = (File)e2.get("tempPackFolder");
                              fileName1 = (String)e2.get("fileName");
                              olderVersionResultMap = this.deployJavaPackages(overrideVersionCheck, struct1, javaPackageList2);
                              CommonResponseFrame.printToResponse(this.getPageStepResponse("DEPLOY3", overrideVersionCheck, this.serializeData(javaPackageList2, olderVersionResultMap, clientPackageList2, olderVersionResultMapClient, hydraPackageList2, hydraInstallLogs, tempPackFolder, fileName1), fileName1), response);
                              return;
                           } else {
                              if(action.equals("DEPLOY3")) {
                                 e2 = this.deserializeData(serialData);
                                 clientPackageList2 = (Map)e2.get("clientPackageList");
                                 javaPackageList2 = (Map)e2.get("javaPackageList");
                                 hydraPackageList2 = (List)e2.get("hydraPackageList");
                                 olderVersionResultMap = (Map)e2.get("olderVersionResultMap");
                                 olderVersionResultMapClient = (Map)e2.get("olderVersionResultMapClient");
                                 hydraInstallLogs = (List)e2.get("hydraInstallLogs");
                                 tempPackFolder = (File)e2.get("tempPackFolder");
                                 fileName1 = (String)e2.get("fileName");
                                 olderVersionResultMapClient = this.deployClientPackages(overrideVersionCheck, struct1, clientPackageList2);
                                 CommonResponseFrame.printToResponse(this.getPageStepResponse("DEPLOY4", overrideVersionCheck, this.serializeData(javaPackageList2, olderVersionResultMap, clientPackageList2, olderVersionResultMapClient, hydraPackageList2, hydraInstallLogs, tempPackFolder, fileName1), fileName1), response);
                              } else if(action.equals("DEPLOY4")) {
                                 e2 = this.deserializeData(serialData);
                                 clientPackageList2 = (Map)e2.get("clientPackageList");
                                 javaPackageList2 = (Map)e2.get("javaPackageList");
                                 hydraPackageList2 = (List)e2.get("hydraPackageList");
                                 olderVersionResultMap = (Map)e2.get("olderVersionResultMap");
                                 olderVersionResultMapClient = (Map)e2.get("olderVersionResultMapClient");
                                 hydraInstallLogs = (List)e2.get("hydraInstallLogs");
                                 tempPackFolder = (File)e2.get("tempPackFolder");
                                 fileName1 = (String)e2.get("fileName");
                                 hydraInstallLogs = this.deployHydraPackages(struct1, hydraPackageList2, request);
                                 if(tempPackFolder != null && tempPackFolder.exists()) {
                                    FileSystemUtil.deleteDir(tempPackFolder);
                                 }

                                 this.printResult(javaPackageList2, olderVersionResultMap, clientPackageList2, olderVersionResultMapClient, hydraInstallLogs, response);
                                 return;
                              }

                              return;
                           }
                        }

                        if(packageFile == null) {
                           throw new IllegalStateException("The package file to deploy is missing");
                        }

                        fileName1 = packageFile.getName();
                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Start deployment of package " + packageFile.getName());

                        try {
                           tempPackFolder = this.saveAndUnzip(packageFile, struct1);
                           System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished save and unzip of package " + packageFile.getName());
                        } catch (Exception var36) {
                           System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var36));
                           CommonResponseFrame.printToResponse(this.getErrorResponse("Error saving/unzipping package: " + var36.getMessage()), response);
                           return;
                        }

                        try {
                           List e = this.getMissingPrerequisites(tempPackFolder, config);
                           if(e.size() > 0) {
                              System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - The following prerequisites are not fulfilled: " + e);
                              CommonResponseFrame.printToResponse(this.getErrorResponse("Can not deploy package because one or more of its prerequisites are not fulfilled.\n\n\nMissing prerequisites:\n\n" + e), response);
                              return;
                           }
                        } catch (Exception var35) {
                           System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Could not check prerequisites\n" + Util.exceptionToString(var35));
                           CommonResponseFrame.printToResponse(this.getErrorResponse("Could not check prerequisites: " + var35.getMessage()), response);
                           return;
                        }

                        this.unzipAndGetPackageContents(tempPackFolder, clientPackageList1, javaPackageList1, hydraPackageList1);
                        if(clientPackageList1.size() == 0 && javaPackageList1.size() == 0 && hydraPackageList1.size() == 0) {
                           CommonResponseFrame.printToResponse(this.getErrorResponse("No usable content in package found!"), response);
                           return;
                        }

                        LinkedList e1 = new LinkedList();
                        Iterator containedExistingJavaPackages = clientPackageList1.keySet().iterator();

                        while(containedExistingJavaPackages.hasNext()) {
                           String e1 = (String)containedExistingJavaPackages.next();
                           if((new File(struct1.updDirClient, e1.substring(e1.indexOf(".") + 1))).exists()) {
                              e1.add(e1.substring(e1.indexOf(".") + 1));
                           }
                        }

                        LinkedList containedExistingJavaPackages1 = new LinkedList();
                        Iterator e11 = javaPackageList1.keySet().iterator();

                        while(e11.hasNext()) {
                           String packFolder = (String)e11.next();
                           (new File(struct1.updDirServer, packFolder.substring(packFolder.indexOf(".") + 1))).getParentFile().mkdirs();
                           if((new File(struct1.updDirServer, packFolder.substring(packFolder.indexOf(".") + 1))).exists()) {
                              containedExistingJavaPackages1.add(packFolder.substring(packFolder.indexOf(".") + 1));
                           }
                        }

                        if(e1.size() <= 0 && containedExistingJavaPackages1.size() <= 0) {
                           this.movePackagesToRightPlace(clientPackageList1, javaPackageList1, struct1);
                           CommonResponseFrame.printToResponse(this.getPageStepResponse("DEPLOY2", overrideVersionCheck, this.serializeData(javaPackageList1, olderVersionResultMap, clientPackageList1, olderVersionResultMapClient, hydraPackageList1, hydraInstallLogs, tempPackFolder, fileName1), fileName1), response);
                           return;
                        }

                        if(tempPackFolder != null && tempPackFolder.exists()) {
                           FileSystemUtil.deleteDir(tempPackFolder);
                        }

                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Could not deploy package because it contains packages that already exist. Client Packages: " + e1 + " Java packages: " + containedExistingJavaPackages1);

                        try {
                           CommonResponseFrame.printToResponse(this.getErrorResponse("Error at deployment of packages: Could not deploy package because it contains packages that already exist. <br />Client Packages: " + e1 + " <br />Java packages: " + containedExistingJavaPackages1), response);
                        } catch (IOException var34) {
                           ;
                        }

                        return;
                     } catch (Exception var37) {
                        this.handleError(struct1, javaPackageList1, clientPackageList1, tempPackFolder, var37, response);
                        return;
                     }
                  }

                  CommonResponseFrame.printToResponse(this.getPageEntryResponse(), response);
                  return;
               }

               CommonResponseFrame.printToResponse(this.getErrorResponse("The temp dir is not configured"), response);
               return;
            }

            CommonResponseFrame.printToResponse("Maintananace Manager 2.0 is installed! Please use the new version!", response);
         } finally {
            if(packageFile != null) {
               packageFile.delete();
            }

         }

      }
   }

   private List getMissingPrerequisites(File tempPackFolder, Configuration config) throws IOException {
      ArrayList missingPreReqList = new ArrayList();
      File rtDir = new File(config.getRuntimeDirServer());
      File preReqFile = new File(tempPackFolder, "prerequisites.txt");
      if(preReqFile.exists()) {
         FileReader reader = null;
         BufferedReader breader = null;

         try {
            reader = new FileReader(preReqFile);
            breader = new BufferedReader(reader);

            for(String line = breader.readLine(); line != null; line = breader.readLine()) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Package has prerequisite " + line);
               if(!(new File(rtDir, line.replace("/", "/SpMarker/") + ".txt")).exists()) {
                  missingPreReqList.add(line);
               }
            }
         } finally {
            if(breader != null) {
               try {
                  breader.close();
               } catch (IOException var18) {
                  ;
               }
            }

            if(reader != null) {
               try {
                  reader.close();
               } catch (IOException var17) {
                  ;
               }
            }

         }
      }

      return missingPreReqList;
   }

   private Map deserializeData(String serializedData) {
      ObjectInputStream inStr = null;
      BufferedInputStream bufInStr = null;
      ByteArrayInputStream byteInStr = null;

      Map var7;
      try {
         byte[] t = Base64.decodeBase64(serializedData.getBytes("UTF-8"));
         byteInStr = new ByteArrayInputStream(t);
         bufInStr = new BufferedInputStream(byteInStr);
         inStr = new ObjectInputStream(bufInStr);
         Map map = (Map)inStr.readObject();
         var7 = map;
      } catch (Throwable var22) {
         throw new RuntimeException("Error at deserialization of data for step", var22);
      } finally {
         if(inStr != null) {
            try {
               inStr.close();
            } catch (IOException var21) {
               ;
            }
         }

         if(bufInStr != null) {
            try {
               bufInStr.close();
            } catch (IOException var20) {
               ;
            }
         }

         if(byteInStr != null) {
            try {
               byteInStr.close();
            } catch (IOException var19) {
               ;
            }
         }

      }

      return var7;
   }

   private String serializeData(Map javaPackageList, Map olderVersionResultMap, Map clientPackageList, Map olderVersionResultMapClient, List hydraPackageList, List hydraInstallLogs, File tempPackFolder, String fileName) {
      HashMap map = new HashMap();
      if(javaPackageList != null) {
         map.put("javaPackageList", javaPackageList);
      }

      if(olderVersionResultMap != null) {
         map.put("olderVersionResultMap", olderVersionResultMap);
      }

      if(clientPackageList != null) {
         map.put("clientPackageList", clientPackageList);
      }

      if(olderVersionResultMapClient != null) {
         map.put("olderVersionResultMapClient", olderVersionResultMapClient);
      }

      if(hydraPackageList != null) {
         map.put("hydraPackageList", hydraPackageList);
      }

      if(hydraInstallLogs != null) {
         map.put("hydraInstallLogs", hydraInstallLogs);
      }

      if(tempPackFolder != null) {
         map.put("tempPackFolder", tempPackFolder);
      }

      if(fileName != null) {
         map.put("fileName", fileName);
      }

      ObjectOutputStream outStr = null;
      BufferedOutputStream bufOutStr = null;
      ByteArrayOutputStream byteOutStr = null;

      String t;
      try {
         byteOutStr = new ByteArrayOutputStream();
         bufOutStr = new BufferedOutputStream(byteOutStr);
         outStr = new ObjectOutputStream(bufOutStr);
         outStr.writeObject(map);
         outStr.flush();
         bufOutStr.flush();
         byteOutStr.flush();
         t = new String(Base64.encodeBase64(byteOutStr.toByteArray()), "UTF-8");
      } catch (Throwable var28) {
         throw new RuntimeException("Error at serialization of data for next step", var28);
      } finally {
         if(outStr != null) {
            try {
               outStr.close();
            } catch (IOException var27) {
               ;
            }
         }

         if(bufOutStr != null) {
            try {
               bufOutStr.close();
            } catch (IOException var26) {
               ;
            }
         }

         if(byteOutStr != null) {
            try {
               byteOutStr.close();
            } catch (IOException var25) {
               ;
            }
         }

      }

      return t;
   }

   private void handleError(ParamStruct struct, Map javaPackageList, Map clientPackageList, File tempPackFolder, Exception e, HttpServletResponse response) {
      Iterator e1;
      String packFolder;
      if(clientPackageList != null) {
         e1 = clientPackageList.keySet().iterator();

         while(e1.hasNext()) {
            packFolder = (String)e1.next();
            if((new File(struct.updDirClient, packFolder.substring(packFolder.indexOf(".") + 1))).exists()) {
               try {
                  ClientPackageMeta exc = ClientPackageMeta.loadPackageMeta((new File(struct.updDirClient, packFolder.substring(packFolder.indexOf(".") + 1))).getAbsolutePath());
                  if(exc.getDeploymentDate() != null) {
                     ClientPackageDeployment.undeployPackage(struct.updDirClient, struct.rtDirClient, packFolder.substring(packFolder.indexOf(".") + 1));
                  }

                  FileSystemUtil.deleteDir(new File(struct.updDirClient, packFolder.substring(packFolder.indexOf(".") + 1)));
               } catch (Exception var12) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Error at undeploy package for error case " + Util.exceptionToString(var12));
               }
            }
         }
      }

      if(javaPackageList != null) {
         e1 = javaPackageList.keySet().iterator();

         while(e1.hasNext()) {
            packFolder = (String)e1.next();
            if((new File(struct.updDirServer, packFolder.substring(packFolder.indexOf(".") + 1))).exists()) {
               try {
                  PackageMeta exc1 = PackageMeta.loadPackageMeta((new File(struct.updDirServer, packFolder.substring(packFolder.indexOf(".") + 1))).getAbsolutePath());
                  if(exc1.getDeploymentDate() != null) {
                     JavaPackageDeployment.undeployPackage(struct.updDirServer, struct.rtDirServer, packFolder.substring(packFolder.indexOf(".") + 1));
                  }

                  FileSystemUtil.deleteDir(new File(struct.updDirServer, packFolder.substring(packFolder.indexOf(".") + 1)));
               } catch (Exception var11) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Error at undeploy package for error case " + Util.exceptionToString(var11));
               }
            }
         }
      }

      if(tempPackFolder != null && tempPackFolder.exists()) {
         FileSystemUtil.deleteDir(tempPackFolder);
      }

      System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(e));

      try {
         CommonResponseFrame.printToResponse(this.getErrorResponse("Error at deployment of packages: " + e.getMessage()), response);
      } catch (IOException var10) {
         ;
      }

   }

   private void printResult(Map javaPackageList, Map olderVersionResultMap, Map clientPackageList, Map olderVersionResultMapClient, List hydraInstallLogs, HttpServletResponse response) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">");
      builder.append("<h1>Deployment successfully finished</h1>\n");
      builder.append("<br />\n");
      builder.append("<span style=\"color:red;\">PLEASE REMEMBER: After the deployment of JAVA packages you need to activate the software to enable the changes!</span><br />");
      builder.append("<h2>Java packages:</h2>\n");
      builder.append("<ul>\n");
      Iterator it = javaPackageList.keySet().iterator();

      String e;
      while(it.hasNext()) {
         e = (String)it.next();
         builder.append("<li>" + e.substring(e.indexOf("-") + 1) + "</li>\n");
         if(olderVersionResultMap.containsKey(e.substring(e.indexOf(".") + 1))) {
            builder.append("<textarea rows=\"9\" style=\"width:70%;\" readonly=\"readonly\">\n");
            builder.append((String)olderVersionResultMap.get(e.substring(e.indexOf(".") + 1)));
            builder.append("</textarea><br /><br />\n");
         }
      }

      builder.append("</ul>\n");
      builder.append("<br />\n");
      builder.append("<h2>Client packages:</h2>\n");
      builder.append("<ul>\n");
      it = clientPackageList.keySet().iterator();

      while(it.hasNext()) {
         e = (String)it.next();
         builder.append("<li>" + e.substring(e.indexOf("-") + 1) + "</li>\n");
         if(olderVersionResultMapClient.containsKey(e.substring(e.indexOf(".") + 1))) {
            builder.append("<textarea rows=\"9\" style=\"width:70%;\" readonly=\"readonly\">\n");
            builder.append((String)olderVersionResultMapClient.get(e.substring(e.indexOf(".") + 1)));
            builder.append("</textarea><br /><br />\n");
         }
      }

      builder.append("</ul>\n");
      builder.append("<br />\n");
      builder.append("<h2>Server packages:</h2>\n");
      it = hydraInstallLogs.iterator();

      while(it.hasNext()) {
         builder.append("<textarea rows=\"29\" style=\"width:90%;\" readonly=\"readonly\">\n");
         builder.append((String)it.next());
         builder.append("</textarea>\n");
         builder.append("<br /><br />\n");
      }

      try {
         CommonResponseFrame.printToResponse(builder.toString(), response);
      } catch (IOException var10) {
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
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - HYDRA install: Domain SvcSystem is in version 2014 or higher. Don\'t put data in service call.");
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
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished HYDRA install for package " + e1.getName());
            } catch (Exception var10) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var10));
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
         packFolder = packFolder.substring(packFolder.indexOf(".") + 1);
         Map compList = null;

         try {
            ClientPackageMeta e = ClientPackageMeta.loadPackageMeta(struct.updDirClient + File.separator + packFolder);
            Map packageVersionMap = ClientPackageDeployment.getPackageDomainVersions(e);
            Map rtVersionMap = ClientPackageDeployment.getRuntimeDomainsVersions(new File(struct.rtDirClient, e.getApplicationName()), packageVersionMap);
            compList = ClientPackageDeployment.getOlderDomainsInPackage(packageVersionMap, rtVersionMap);
            sameVersList = ClientPackageDeployment.getSameVersionDomainsInPackage(packageVersionMap, rtVersionMap);
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished version comparison of client package " + packFolder);
         } catch (Exception var12) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var12));
            throw new RuntimeException("Could not deploy client package because: Could not check versions of package and runtime: " + var12.getMessage());
         }

         try {
            String e1 = ClientPackageDeployment.deployPackage(struct.updDirClient, struct.rtDirClient, packFolder, overrideVersionCheck != null && "true".equals(overrideVersionCheck), compList, sameVersList);
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Result of client package deployment (" + packFolder + ")\n" + e1);
            if(e1 != null) {
               olderVersionResultMapClient.put(packFolder, e1);
            }
         } catch (Exception var13) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var13));
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
         packFolder = packFolder.substring(packFolder.indexOf(".") + 1);
         Map compList = null;
         List sameVersList = null;

         try {
            PackageMeta e = PackageMeta.loadPackageMeta(struct.updDirServer + File.separator + packFolder);
            Map packageVersionMap = JavaPackageDeployment.getPackageFileVersions(packFolder, struct.updDirServer, struct.tempDir.getAbsolutePath());
            Map rtVersionMap = JavaPackageDeployment.getRuntimeFileVersions(new File(struct.rtDirServer, e.getApplicationName()), packageVersionMap, struct.tempDir.getAbsolutePath());
            compList = JavaPackageDeployment.getOlderFilesInPackage(packageVersionMap, rtVersionMap);
            sameVersList = JavaPackageDeployment.getSameVersionFilesInPackage(packageVersionMap, rtVersionMap);
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished version comparison of java package " + packFolder);
         } catch (Exception var12) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var12));
            throw new RuntimeException("Could not deploy package because: Could not check versions of package and runtime: " + var12.getMessage());
         }

         try {
            String e1 = JavaPackageDeployment.deployPackage(struct.updDirServer, struct.rtDirServer, packFolder, overrideVersionCheck != null && "true".equals(overrideVersionCheck), compList, sameVersList);
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Result of pacakge deployment (" + packFolder + ")\n" + e1);
            if(e1 != null) {
               olderVersionResultMap.put(packFolder, e1);
            }
         } catch (Exception var13) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var13));
            throw new RuntimeException("Could not deploy package because: " + var13.getMessage());
         }
      }

      return olderVersionResultMap;
   }

   private void movePackagesToRightPlace(Map clientPackageList, Map javaPackageList, ParamStruct struct) throws FileNotFoundException, IOException {
      Iterator it = clientPackageList.keySet().iterator();

      String packFolder;
      boolean moveSucceeded;
      int i;
      while(it.hasNext()) {
         packFolder = (String)it.next();
         (new File(struct.updDirClient, packFolder.substring(packFolder.indexOf(".") + 1))).getParentFile().mkdirs();
         moveSucceeded = false;
         i = 0;

         while(true) {
            if(i < 20) {
               moveSucceeded = ((File)clientPackageList.get(packFolder)).renameTo(new File(struct.updDirClient, packFolder.substring(packFolder.indexOf(".") + 1)));
               if(!moveSucceeded) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Error moving client package " + packFolder + " in iteration " + i);

                  try {
                     Thread.sleep(100L);
                  } catch (InterruptedException var10) {
                     ;
                  }

                  ++i;
                  continue;
               }
            }

            if(!moveSucceeded) {
               throw new RuntimeException("Error moving client package from temp to destination " + packFolder.substring(packFolder.indexOf(".") + 1) + " after 20 tries");
            }

            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished move of client package " + packFolder);
            break;
         }
      }

      it = javaPackageList.keySet().iterator();

      while(it.hasNext()) {
         packFolder = (String)it.next();
         (new File(struct.updDirServer, packFolder.substring(packFolder.indexOf(".") + 1))).getParentFile().mkdirs();
         moveSucceeded = false;
         i = 0;

         while(true) {
            if(i < 20) {
               moveSucceeded = ((File)javaPackageList.get(packFolder)).renameTo(new File(struct.updDirServer, packFolder.substring(packFolder.indexOf(".") + 1)));
               if(!moveSucceeded) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Error moving java package " + packFolder + " in iteration " + i);

                  try {
                     Thread.sleep(100L);
                  } catch (InterruptedException var9) {
                     ;
                  }

                  ++i;
                  continue;
               }
            }

            if(!moveSucceeded) {
               throw new RuntimeException("Error moving java package from temp to destination " + packFolder.substring(packFolder.indexOf(".") + 1) + " after 20 tries");
            }

            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished move of java package " + packFolder);
            break;
         }
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
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished unzip and loading contents of java package " + packageFolder.getName());
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
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished unzip and loading contents of client package " + packageFolder.getName());
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

   private String getPageStepResponse(String nextAction, String overrideVersionCheck, String serialData, String fileName) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">");
      builder.append("<h1>Package deployment</h1>\n");
      builder.append("<br />\n");
      builder.append("<span style=\"color:blue;text-align:center;\"><b>Deploy " + fileName + "</b><br /><br />Overwrite newer versions = " + ("true".equals(overrideVersionCheck)?"true":"false") + "</span>\n");
      builder.append("<form action=\"UniPackDeployment\" method=\"post\" id=\"deplform\" >\n");
      builder.append("<input type=\"hidden\" name=\"overrideVersionCheck\" value=\"" + overrideVersionCheck + "\"/><br />\n");
      builder.append("<input type=\"hidden\" name=\"action\" value=\"" + nextAction + "\"/><br />\n");
      builder.append("<input type=\"hidden\" name=\"serialData\" value=\"" + serialData + "\"/><br />\n");
      builder.append("</form>\n");
      builder.append("</div>\n");
      builder.append("<script type=\"text/javascript\">\n");
      builder.append("window.setTimeout(\"deploy()\", 500);");
      builder.append("");
      builder.append("function deploy()");
      builder.append("{");
      builder.append("document.getElementById(\"deplform\").submit();");
      builder.append("}");
      builder.append("</script>\n");
      if(nextAction.equals("DEPLOY2")) {
         builder.append("<span id=\"progress\" style=\"color:blue;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(1/4) Upload and unzip package ... FINISHED<br /><br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(2/4) Deploy JAVA contents ...<br /></span><br />");
      } else if(nextAction.equals("DEPLOY3")) {
         builder.append("<span id=\"progress\" style=\"color:blue;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(1/4) Upload and unzip package ... FINISHED<br /><br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(2/4) Deploy JAVA contents ... FINISHED<br /><br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(3/4) Deploy Client contents ...<br /></span><br />");
      } else if(nextAction.equals("DEPLOY4")) {
         builder.append("<span id=\"progress\" style=\"color:blue;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(1/4) Upload and unzip package ... FINISHED<br /><br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(2/4) Deploy JAVA contents ... FINISHED<br /><br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(3/4) Deploy Client contents ... FINISHED<br /><br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(4/4) Deploy server contents ...<br /></span><br />");
      }

      return builder.toString();
   }

   private String getPageEntryResponse() {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">");
      builder.append("<h1>Package deployment</h1>\n");
      builder.append("<br />\n");
      builder.append("<script type=\"text/javascript\">\n");
      builder.append("function deploy()");
      builder.append("{");
      builder.append("document.getElementById(\"progress\").innerHTML=\"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(1/4) Upload and unzip package ...<br />\";");
      builder.append("document.getElementById(\"fileform\").submit();");
      builder.append("}");
      builder.append("</script>\n");
      builder.append("<form action=\"UniPackDeployment\" method=\"post\" enctype=\"multipart/form-data\" id=\"fileform\">\n");
      builder.append("Select update package (*.upd) <input type=\"file\" name=\"package\"/><br /><br />\n");
      builder.append("Overwrite newer versions <input type=\"checkbox\" name=\"overrideVersionCheck\" value=\"true\"/><br />\n");
      builder.append("<input type=\"hidden\" name=\"action\" value=\"DEPLOY\"/><br />\n");
      builder.append("<input style=\"visibility:hidden;\" type=\"button\" name=\"Submit\" id=\"Submit\" value=\"Deploy package\" onclick=\"deploy();\" /><br />\n");
      builder.append("<span id=\"javascriptwarning\" style=\"color:red;\"><b>ATTENTION:<br /> Javascript is disabled in your browser, but you need javascript to use package deployment!</b></span><br />\n");
      builder.append("</form><br /><br />\n");
      builder.append("</div>\n");
      builder.append("<script type=\"text/javascript\">\n");
      builder.append(" document.getElementById(\'Submit\').style.visibility=\"visible\";\n");
      builder.append(" document.getElementById(\'javascriptwarning\').style.visibility=\"hidden\";\n");
      builder.append("</script>\n");
      builder.append("<span id=\"progress\" style=\"color:blue;\"></span><br />");
      return builder.toString();
   }

   private String getErrorResponse(String message) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">");
      builder.append("<h1>Package deployment</h1>\n");
      builder.append("<br />\n");
      builder.append("<span style=\"color:red;\">An error has occured: " + message + "</span><br />");
      builder.append("</div>\n");
      return builder.toString();
   }

   private ParamStruct checkParams(Configuration config, String action, FileItem packageFile, String serialData) {
      if(config == null) {
         throw new IllegalArgumentException("Could not get configuration");
      } else if(action != null && !action.equals("")) {
         if(!action.equals("DEPLOY") && !action.equals("DEPLOY2") && !action.equals("DEPLOY3") && !action.equals("DEPLOY4")) {
            throw new IllegalArgumentException("Unknown action specified: " + action + ". Allowed actions are: " + "DEPLOY" + ", " + "DEPLOY2" + ", " + "DEPLOY3" + ", " + "DEPLOY4");
         } else {
            String updDirPath = config.getUpdateDirServer();
            if(updDirPath != null && !updDirPath.equals("")) {
               File updDir = new File(updDirPath);
               if(!updDir.exists()) {
                  throw new IllegalStateException("The configured update dir does not exist: " + updDir.getAbsolutePath());
               } else {
                  String rtDirPath = config.getRuntimeDirServer();
                  if(rtDirPath != null && !rtDirPath.equals("")) {
                     File rtDir = new File(rtDirPath);
                     if(!rtDir.exists()) {
                        throw new IllegalStateException("The configured runtime dir does not exist: " + rtDir.getAbsolutePath());
                     } else {
                        String updDirPathCl = config.getUpdateDirClient();
                        if(updDirPathCl != null && !updDirPathCl.equals("")) {
                           File updDirCl = new File(updDirPathCl);
                           if(!updDirCl.exists()) {
                              throw new IllegalStateException("The configured update dir does not exist: " + updDirCl.getAbsolutePath());
                           } else {
                              String rtDirPathCl = config.getRuntimeDirClient();
                              if(rtDirPathCl != null && !rtDirPathCl.equals("")) {
                                 File rtDirCl = new File(rtDirPathCl);
                                 if(!rtDirCl.exists()) {
                                    throw new IllegalStateException("The configured runtime dir does not exist: " + rtDirCl.getAbsolutePath());
                                 } else {
                                    String tempDirPath = config.getTempDir();
                                    if(tempDirPath != null && !tempDirPath.equals("")) {
                                       File tempDir = new File(tempDirPath);
                                       if(!tempDir.exists()) {
                                          throw new IllegalStateException("The configured temp dir does not exist: " + tempDir.getAbsolutePath());
                                       } else {
                                          String tomcatHostPort = config.getTomcatHostPort();
                                          if(tomcatHostPort != null && !tomcatHostPort.equals("")) {
                                             if(packageFile == null && Util.stringNullOrEmpty(serialData)) {
                                                throw new IllegalStateException("The package file and / or serialized data is missing");
                                             } else if(packageFile != null && !packageFile.getName().toLowerCase().endsWith(".upd")) {
                                                throw new IllegalStateException("The package file is not of type .upd");
                                             } else {
                                                String jHydradirPath = config.getjHydraDir();
                                                if(jHydradirPath != null && !jHydradirPath.equals("")) {
                                                   File jHydradir = new File(jHydradirPath);
                                                   if(!jHydradir.exists()) {
                                                      throw new IllegalStateException("The configured JHYDRADIR dir does not exist: " + jHydradir.getAbsolutePath());
                                                   } else {
                                                      String tcDirPath = config.getTomcatDir();
                                                      if(tcDirPath != null && !tcDirPath.equals("")) {
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
      } else {
         throw new IllegalArgumentException("The desired action is not specified");
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