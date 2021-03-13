package de.mpdv.maintenanceManager.logic.client;

import de.mpdv.maintenanceManager.data.CopyRule;
import de.mpdv.maintenanceManager.data.client.ClientPackageMeta;
import de.mpdv.maintenanceManager.data.client.VersionComparisonInfo;
import de.mpdv.maintenanceManager.data.client.VersionInfo;
import de.mpdv.maintenanceManager.logic.client.ClientPackageDeployment.f;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ClientPackageDeployment {

class f implements FilenameFilter {

   // $FF: synthetic field
   final File val$destFile;


   f(File var1) {
      this.val$destFile = var1;
   }

   public boolean accept(File dir, String name) {
      return name.equalsIgnoreCase(this.val$destFile.getName());
   }
}    
    
   public static String deployPackage(File updateDir, File rtDir, String packageFolder, boolean overrideVersionCheck, Map olderVersionMap, List sameVersionList) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, ParseException, XMLStreamException {
      StringBuilder resultBuilder = new StringBuilder();
      if(olderVersionMap.size() > 0) {
         if(!overrideVersionCheck) {
            resultBuilder.append("The following older domains were NOT deployed:\n\n");
         } else {
            resultBuilder.append("The following older domains were deployed:\n\n");
         }
      }

      ClientPackageMeta meta = ClientPackageMeta.loadPackageMeta(updateDir + File.separator + packageFolder);
      File packFolder = new File(updateDir, DateTimeUtil.calendarUtcToIsoDateString(meta.getCreationDate()) + "-" + meta.getName());
      File backupFolder = new File(packFolder.getParentFile(), "Backups" + File.separator + packFolder.getName());
      backupFolder.mkdirs();
      File backupFolderServer = new File(packFolder.getParentFile(), "Backups" + File.separator + packFolder.getName() + "-SERVER");
      backupFolderServer.mkdirs();
      File rtAppDir = new File(rtDir, meta.getApplicationName());
      File rtAppDirServer = new File(rtDir.getParentFile(), "server/" + meta.getApplicationName());
      LinkedList affectedRtFiles = new LinkedList();

      try {
         File[] e = packFolder.listFiles();

         File maintMgrDir;
         for(int returnVal = 0; returnVal < e.length; ++returnVal) {
            maintMgrDir = e[returnVal];
            if(!maintMgrDir.isFile()) {
               boolean protDir = false;
               if(olderVersionMap.containsKey(maintMgrDir.getName().toLowerCase())) {
                  VersionComparisonInfo name = (VersionComparisonInfo)olderVersionMap.get(maintMgrDir.getName().toLowerCase());
                  resultBuilder.append(name.getDomain() + "\nExisting version: " + name.getRightVersionString() + "\nVersion to deploy: " + name.getLeftVersionString() + "\n\n");
                  if(!overrideVersionCheck) {
                     protDir = true;
                  }
               } else if(sameVersionList.contains(maintMgrDir.getName().toLowerCase()) && !overrideVersionCheck) {
                  protDir = true;
               }

               List var38 = CopyRule.loadRulesFromFile(maintMgrDir.getAbsolutePath() + File.separator + "rules.xml");

               for(int content = 0; content < var38.size(); ++content) {
                  CopyRule e1 = (CopyRule)var38.get(content);
                  File curSrcFolder = !".".equals(e1.getSource()) && !"".equals(e1.getSource())?new File(maintMgrDir, e1.getSource()):maintMgrDir;
                  if(curSrcFolder.exists()) {
                     File curDestFolder;
                     File curBackupFolder;
                     String filter;
                     if(e1.getTarget().startsWith("#SERVER#/")) {
                        filter = e1.getTarget().replaceFirst("#SERVER#/", "");
                        curDestFolder = !".".equals(filter) && !"".equals(filter)?new File(rtAppDirServer, filter):rtAppDirServer;
                        curBackupFolder = !".".equals(filter) && !"".equals(filter)?new File(backupFolderServer, filter):backupFolderServer;
                     } else {
                        curDestFolder = !".".equals(e1.getTarget()) && !"".equals(e1.getTarget())?new File(rtAppDir, e1.getTarget()):rtAppDir;
                        curBackupFolder = !".".equals(e1.getTarget()) && !"".equals(e1.getTarget())?new File(backupFolder, e1.getTarget()):backupFolder;
                     }

                     filter = e1.getFilter();
                     int k;
                     File srcFile;
                     if(filter != null) {
                        File[] srcFiles = curSrcFolder.listFiles();

                        for(k = 0; k < srcFiles.length; ++k) {
                           srcFile = srcFiles[k];
                           if(!srcFile.isDirectory() && srcFile.getName().endsWith(filter)) {
                              if(!curDestFolder.exists()) {
                                 curDestFolder.mkdirs();
                              }

                              File relativePath = new File(curDestFolder, srcFile.getName());
                              if(relativePath.exists()) {
                                 if(!curBackupFolder.exists()) {
                                    curBackupFolder.mkdirs();
                                 }

                                 if(protDir) {
                                    FileSystemUtil.copyFile(relativePath, new File(curBackupFolder, srcFile.getName()), true, true);
                                 } else {
                                    (new File(curBackupFolder, srcFile.getName())).getParentFile().mkdirs();
                                    relativePath.renameTo(new File(curBackupFolder, srcFile.getName()));
                                 }
                              }

                              if(!Util.isWindowsSystem()) {
                                 handleDifferentFileCasing(relativePath, curBackupFolder, protDir);
                              }

                              if(!protDir) {
                                 FileSystemUtil.copyFile(srcFile, relativePath, true, true);
                                 affectedRtFiles.add(relativePath);
                              }
                           }
                        }
                     } else {
                        List var42 = FileSystemUtil.getFileListing(curSrcFolder);

                        for(k = 0; k < var42.size(); ++k) {
                           srcFile = (File)var42.get(k);
                           String var43 = srcFile.getAbsolutePath().replace(curSrcFolder.getAbsolutePath() + File.separator, "");
                           if(!curDestFolder.exists()) {
                              curDestFolder.mkdirs();
                           }

                           File destFile = new File(curDestFolder, var43);
                           if(destFile.exists()) {
                              if(!curBackupFolder.exists()) {
                                 curBackupFolder.mkdirs();
                              }

                              if(protDir) {
                                 FileSystemUtil.copyFile(destFile, new File(curBackupFolder, var43), true, true);
                              } else {
                                 (new File(curBackupFolder, var43)).getParentFile().mkdirs();
                                 destFile.renameTo(new File(curBackupFolder, var43));
                              }
                           }

                           if(!Util.isWindowsSystem()) {
                              handleDifferentFileCasing(destFile, curBackupFolder, protDir);
                           }

                           if(!protDir) {
                              FileSystemUtil.copyFile(srcFile, destFile, true, true);
                              affectedRtFiles.add(destFile);
                           }
                        }
                     }
                  }
               }
            }
         }

         meta.setDeploymentDate(DateTimeUtil.getCurrentUtcCalendar());
         meta.savePackageMeta(packFolder.getAbsolutePath());
         String var37 = null;
         if(olderVersionMap.size() > 0) {
            var37 = resultBuilder.toString();
         }

         maintMgrDir = rtDir.getParentFile().getParentFile();
         File var41 = new File(maintMgrDir, "logs/client");
         var41.mkdirs();
         String var39 = DateTimeUtil.calendarUtcToIsoString(DateTimeUtil.getCurrentUtcCalendar()) + "-DEPLOY-" + meta.getName() + ".txt";
         String var40 = "Deployed package " + meta.getName() + " at " + DateTimeUtil.calendarUtcToPrintString(DateTimeUtil.getCurrentUtcCalendar()) + " (UTC)\n";
         if(var37 != null) {
            var40 = var40 + "\n" + var37;
         }

         try {
            FileSystemUtil.writeTextFile(var41, var39, var40);
         } catch (Exception var30) {
            ;
         }

         return var37;
      } catch (RuntimeException var31) {
         rollBackForError(backupFolder, rtAppDir, backupFolderServer, rtAppDirServer, affectedRtFiles);
         throw var31;
      } catch (FileNotFoundException var32) {
         rollBackForError(backupFolder, rtAppDir, backupFolderServer, rtAppDirServer, affectedRtFiles);
         throw var32;
      } catch (ParserConfigurationException var33) {
         rollBackForError(backupFolder, rtAppDir, backupFolderServer, rtAppDirServer, affectedRtFiles);
         throw var33;
      } catch (SAXException var34) {
         rollBackForError(backupFolder, rtAppDir, backupFolderServer, rtAppDirServer, affectedRtFiles);
         throw var34;
      } catch (IOException var35) {
         rollBackForError(backupFolder, rtAppDir, backupFolderServer, rtAppDirServer, affectedRtFiles);
         throw var35;
      } catch (XMLStreamException var36) {
         rollBackForError(backupFolder, rtAppDir, backupFolderServer, rtAppDirServer, affectedRtFiles);
         throw var36;
      }
   }

   private static void rollBackForError(File backupFolder, File applicationRtDir, File backupFolderServer, File applicationRtDirServer, List affectedFiles) {
      for(int e = 0; e < affectedFiles.size(); ++e) {
         File f = (File)affectedFiles.get(e);

         try {
            if(f.isDirectory()) {
               FileSystemUtil.deleteDir(f);
            } else {
               f.delete();
            }
         } catch (Exception var10) {
            ;
         }
      }

      try {
         FileSystemUtil.copyDir(backupFolder, applicationRtDir, true, true);
      } catch (Exception var9) {
         ;
      }

      if(backupFolderServer.exists()) {
         try {
            FileSystemUtil.copyDir(backupFolderServer, applicationRtDirServer, true, true);
         } catch (Exception var8) {
            ;
         }
      }

      FileSystemUtil.deleteDir(backupFolder);
      if(backupFolderServer.exists()) {
         FileSystemUtil.deleteDir(backupFolderServer);
      }

   }

   public static void undeployPackage(File updateDir, File rtDir, String packageFolder) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, ParseException, XMLStreamException {
      ClientPackageMeta meta = ClientPackageMeta.loadPackageMeta(updateDir.getAbsolutePath() + File.separator + packageFolder);
      File packFolder = new File(updateDir, DateTimeUtil.calendarUtcToIsoDateString(meta.getCreationDate()) + "-" + meta.getName());
      File backupFolder = new File(packFolder.getParentFile(), "Backups" + File.separator + packFolder.getName());
      File backupFolderServer = new File(packFolder.getParentFile(), "Backups" + File.separator + packFolder.getName() + "-SERVER");
      File rtAppDir = new File(rtDir, meta.getApplicationName());
      File rtAppDirServer = new File(rtDir.getParentFile(), "server/" + meta.getApplicationName());
      File[] packFiles = packFolder.listFiles();

      File protDir;
      for(int maintMgrDir = 0; maintMgrDir < packFiles.length; ++maintMgrDir) {
         protDir = packFiles[maintMgrDir];
         if(!protDir.isFile()) {
            List name = CopyRule.loadRulesFromFile(protDir.getAbsolutePath() + File.separator + "rules.xml");

            for(int content = 0; content < name.size(); ++content) {
               CopyRule e = (CopyRule)name.get(content);
               File curSrcFolder = !".".equals(e.getSource()) && !"".equals(e.getSource())?new File(protDir, e.getSource()):protDir;
               if(curSrcFolder.exists()) {
                  File curDestFolder;
                  String filter;
                  if(e.getTarget().startsWith("#SERVER#/")) {
                     filter = e.getTarget().replaceFirst("#SERVER#/", "");
                     curDestFolder = !".".equals(filter) && !"".equals(filter)?new File(rtAppDirServer, filter):rtAppDirServer;
                  } else {
                     curDestFolder = !".".equals(e.getTarget()) && !"".equals(e.getTarget())?new File(rtAppDir, e.getTarget()):rtAppDir;
                  }

                  filter = e.getFilter();
                  int k;
                  File srcFile;
                  File destFile;
                  if(filter != null) {
                     File[] srcFiles = curSrcFolder.listFiles();

                     for(k = 0; k < srcFiles.length; ++k) {
                        srcFile = srcFiles[k];
                        if(!srcFile.isDirectory() && srcFile.getName().endsWith(filter)) {
                           File relativePath = new File(curDestFolder, srcFile.getName());
                           if(relativePath.exists()) {
                              relativePath.delete();

                              for(destFile = relativePath.getParentFile(); !destFile.equals(rtAppDir) && destFile.listFiles().length == 0; destFile = destFile.getParentFile()) {
                                 destFile.delete();
                              }
                           }
                        }
                     }
                  } else {
                     List var28 = FileSystemUtil.getFileListing(curSrcFolder);

                     for(k = 0; k < var28.size(); ++k) {
                        srcFile = (File)var28.get(k);
                        String var29 = srcFile.getAbsolutePath().replace(curSrcFolder.getAbsolutePath() + File.separator, "");
                        destFile = new File(curDestFolder, var29);
                        if(destFile.exists()) {
                           destFile.delete();

                           for(File currDelDir = destFile.getParentFile(); !currDelDir.equals(rtAppDir) && currDelDir.listFiles().length == 0; currDelDir = currDelDir.getParentFile()) {
                              currDelDir.delete();
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      FileSystemUtil.copyDir(backupFolder, rtAppDir, true, true);
      if(backupFolderServer.exists()) {
         FileSystemUtil.copyDir(backupFolderServer, rtAppDirServer, true, true);
      }

      FileSystemUtil.deleteDir(backupFolder);
      if(backupFolderServer.exists()) {
         FileSystemUtil.deleteDir(backupFolderServer);
      }

      meta.setDeploymentDate((Calendar)null);
      meta.savePackageMeta(packFolder.getAbsolutePath());
      File var25 = rtDir.getParentFile().getParentFile();
      protDir = new File(var25, "logs/client");
      protDir.mkdirs();
      String var26 = DateTimeUtil.calendarUtcToIsoString(DateTimeUtil.getCurrentUtcCalendar()) + "-UNDEPLOY-" + meta.getName() + ".txt";
      String var27 = "Undeployed package " + meta.getName() + " at " + DateTimeUtil.calendarUtcToPrintString(DateTimeUtil.getCurrentUtcCalendar()) + " (UTC)\n";

      try {
         FileSystemUtil.writeTextFile(protDir, var26, var27);
      } catch (Exception var24) {
         ;
      }

   }

   public static Map getRuntimeDomainsVersions(File runtimeDir, Map packageVersions) throws IOException, SAXException, ParserConfigurationException {
      HashMap rtVersionMap = new HashMap();
      List versionFiles = FileSystemUtil.getFileListing(new File(runtimeDir, "versions"));
      int fileCount = versionFiles.size();

      for(int packVersIt = 0; packVersIt < fileCount; ++packVersIt) {
         File domain = (File)versionFiles.get(packVersIt);
         String fileName = domain.getName();
         if(fileName.endsWith(".versioninfo.xml")) {
            String domain1 = fileName.substring(0, fileName.length() - 16).toLowerCase();
            if(packageVersions.containsKey(domain1)) {
               rtVersionMap.put(domain1, getVersionInfo(domain, domain1));
            }
         }
      }

      if(packageVersions.size() != rtVersionMap.size()) {
         Iterator var9 = packageVersions.keySet().iterator();

         while(var9.hasNext()) {
            String var10 = (String)var9.next();
            if(!rtVersionMap.containsKey(var10)) {
               rtVersionMap.put(var10, new VersionInfo(var10, "0.0.STD.0", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), "STD"));
            }
         }
      }

      return rtVersionMap;
   }

   private static VersionInfo getVersionInfo(File file, String domain) throws SAXException, IOException, ParserConfigurationException {
      DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = fact.newDocumentBuilder();
      FileInputStream inStr = null;

      VersionInfo var33;
      try {
         inStr = new FileInputStream(file);
         Document document = builder.parse(new InputSource(inStr));
         NodeList root = document.getElementsByTagName("mpdvVersion");
         if(root.getLength() != 1) {
            throw new IllegalArgumentException("The tag mpdvVersion is not contained, or contained more than once in file " + file);
         }

         Node rootNode = root.item(0);
         Integer major = null;
         Integer minor = null;
         Integer revision = null;
         String customerFlag = null;
         NodeList contents = rootNode.getChildNodes();
         int count = contents.getLength();

         for(int i = 0; i < count; ++i) {
            Node currValueNode = contents.item(i);
            String nodeName = currValueNode.getNodeName();
            String revisionStr;
            if(nodeName.equals("MajorVersion")) {
               revisionStr = currValueNode.getTextContent();

               try {
                  major = Integer.valueOf(revisionStr);
               } catch (NumberFormatException var31) {
                  throw new IllegalArgumentException("Major version " + revisionStr + " is not numeric in file " + file);
               }
            } else if(nodeName.equals("MinorVersion")) {
               revisionStr = currValueNode.getTextContent();

               try {
                  minor = Integer.valueOf(revisionStr);
               } catch (NumberFormatException var30) {
                  throw new IllegalArgumentException("Minor version " + revisionStr + " is not numeric in file " + file);
               }
            } else if(nodeName.equals("Revision")) {
               revisionStr = currValueNode.getTextContent();

               try {
                  revision = Integer.valueOf(revisionStr);
               } catch (NumberFormatException var29) {
                  throw new IllegalArgumentException("Revision version " + revisionStr + " is not numeric in file " + file);
               }
            } else if(nodeName.equals("CustomerId")) {
               customerFlag = currValueNode.getTextContent();
            }
         }

         if(Util.stringNullOrEmpty(customerFlag)) {
            throw new IllegalArgumentException("The mandatory element CustomerId is missing in " + file);
         }

         if(major == null) {
            throw new IllegalArgumentException("The mandatory element MajorVersion is missing in " + file);
         }

         if(minor == null) {
            throw new IllegalArgumentException("The mandatory element MinorVersion is missing in " + file);
         }

         if(revision == null) {
            throw new IllegalArgumentException("The mandatory element Revision is missing in " + file);
         }

         var33 = new VersionInfo(domain, major + "." + minor + "." + customerFlag + "." + revision, major, minor, revision, customerFlag);
      } finally {
         if(inStr != null) {
            try {
               inStr.close();
            } catch (Exception var28) {
               ;
            }
         }

      }

      return var33;
   }

   public static Map getPackageDomainVersions(ClientPackageMeta meta) {
      HashMap packVersionMap = new HashMap();
      List domains = meta.getDomains();
      Map versionMap = meta.getDomainVersions();

      for(int i = 0; i < domains.size(); ++i) {
         String domain = ((String)domains.get(i)).toLowerCase();
         String versionStr = (String)versionMap.get(domains.get(i));
         if(Util.stringNullOrEmpty(versionStr)) {
            throw new IllegalArgumentException("Client package meta does not contain a version for domain " + domain);
         }

         String[] parts = versionStr.split("\\.");
         if(parts.length != 4) {
            throw new IllegalArgumentException("Client package meta does contain a version for domain " + domain + " in wrong format");
         }

         Integer major;
         try {
            major = Integer.valueOf(parts[0]);
         } catch (NumberFormatException var15) {
            throw new IllegalArgumentException("Client package meta does contain a version for domain " + domain + " with non numeric major version");
         }

         Integer minor;
         try {
            minor = Integer.valueOf(parts[1]);
         } catch (NumberFormatException var14) {
            throw new IllegalArgumentException("Client package meta does contain a version for domain " + domain + " with non numeric minor version");
         }

         Integer revision;
         try {
            revision = Integer.valueOf(parts[3]);
         } catch (NumberFormatException var13) {
            throw new IllegalArgumentException("Client package meta does contain a version for domain " + domain + " with non numeric revision version");
         }

         String customerFlag = parts[2];
         packVersionMap.put(domain, new VersionInfo(domain, major + "." + minor + "." + customerFlag + "." + revision, major, minor, revision, customerFlag));
      }

      return packVersionMap;
   }

   public static Map getOlderDomainsInPackage(Map packageVersions, Map rtVersions) {
      HashMap compList = new HashMap();
      Iterator packIt = packageVersions.keySet().iterator();

      while(packIt.hasNext()) {
         String domain = (String)packIt.next();
         VersionInfo packVersInfo = (VersionInfo)packageVersions.get(domain);
         VersionInfo rtVersInfo = (VersionInfo)rtVersions.get(domain);
         if(packVersInfo.getCustomerFlag().equals("STD") && !rtVersInfo.getCustomerFlag().equals("STD")) {
            compList.put(domain, new VersionComparisonInfo(domain, packVersInfo.getVersionString(), packVersInfo.getMajor(), packVersInfo.getMinor(), packVersInfo.getRevision(), packVersInfo.getCustomerFlag(), rtVersInfo.getVersionString(), rtVersInfo.getMajor(), rtVersInfo.getMinor(), rtVersInfo.getRevision(), rtVersInfo.getCustomerFlag()));
         } else if((packVersInfo.getCustomerFlag().equals("STD") || !rtVersInfo.getCustomerFlag().equals("STD")) && (packVersInfo.getMajor().intValue() != rtVersInfo.getMajor().intValue() || packVersInfo.getMinor().intValue() != rtVersInfo.getMinor().intValue() || packVersInfo.getRevision().intValue() != rtVersInfo.getRevision().intValue()) && packVersInfo.getMajor().intValue() <= rtVersInfo.getMajor().intValue() && (packVersInfo.getMajor().intValue() != rtVersInfo.getMajor().intValue() || packVersInfo.getMinor().intValue() <= rtVersInfo.getMinor().intValue()) && (packVersInfo.getMajor().intValue() != rtVersInfo.getMajor().intValue() || packVersInfo.getMinor().intValue() != rtVersInfo.getMinor().intValue() || packVersInfo.getRevision().intValue() < rtVersInfo.getRevision().intValue())) {
            compList.put(domain, new VersionComparisonInfo(domain, packVersInfo.getVersionString(), packVersInfo.getMajor(), packVersInfo.getMinor(), packVersInfo.getRevision(), packVersInfo.getCustomerFlag(), rtVersInfo.getVersionString(), rtVersInfo.getMajor(), rtVersInfo.getMinor(), rtVersInfo.getRevision(), rtVersInfo.getCustomerFlag()));
         }
      }

      return compList;
   }

   public static List getSameVersionDomainsInPackage(Map packageVersions, Map rtVersions) {
      ArrayList list = new ArrayList();
      Iterator packIt = packageVersions.keySet().iterator();

      while(packIt.hasNext()) {
         String domain = (String)packIt.next();
         VersionInfo packVersInfo = (VersionInfo)packageVersions.get(domain);
         VersionInfo rtVersInfo = (VersionInfo)rtVersions.get(domain);
         if(packVersInfo.getCustomerFlag().equals(rtVersInfo.getCustomerFlag()) && packVersInfo.getMajor().intValue() == rtVersInfo.getMajor().intValue() && packVersInfo.getMinor().intValue() == rtVersInfo.getMinor().intValue() && packVersInfo.getRevision().intValue() == rtVersInfo.getRevision().intValue()) {
            list.add(domain);
         }
      }

      return list;
   }

   private static void handleDifferentFileCasing(File destFile, File curBackupFolder, boolean onlyBackup) throws FileNotFoundException, IOException {
      File[] filesOtherCasing = destFile.getParentFile().listFiles(new f(destFile));
      if(filesOtherCasing != null) {
         if(!curBackupFolder.exists()) {
            curBackupFolder.mkdirs();
         }

         for(int fileIdx = 0; fileIdx < filesOtherCasing.length; ++fileIdx) {
            File curFile = filesOtherCasing[fileIdx];
            if(onlyBackup) {
               FileSystemUtil.copyFile(curFile, new File(curBackupFolder, curFile.getName()), true, true);
            } else {
               (new File(curBackupFolder, curFile.getName())).getParentFile().mkdirs();
               curFile.renameTo(new File(curBackupFolder, curFile.getName()));
            }
         }
      }

   }
}