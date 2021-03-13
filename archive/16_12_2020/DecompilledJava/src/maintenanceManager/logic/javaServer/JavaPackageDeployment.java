package de.mpdv.maintenanceManager.logic.javaServer;

import de.mpdv.maintenanceManager.data.CopyRule;
import de.mpdv.maintenanceManager.data.javaServer.PackageElement;
import de.mpdv.maintenanceManager.data.javaServer.PackageMeta;
import de.mpdv.maintenanceManager.data.javaServer.ProjectMeta;
import de.mpdv.maintenanceManager.data.javaServer.VersionComparisonInfo;
import de.mpdv.maintenanceManager.data.javaServer.VersionInfo;
import de.mpdv.maintenanceManager.data.javaServer.PackageElement.PackageElementType;
import de.mpdv.maintenanceManager.logic.javaServer.VersionInformationRetriever;
import de.mpdv.maintenanceManager.logic.javaServer.JavaPackageDeployment.f;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public class JavaPackageDeployment {

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
    
   public static String deployPackage(File updateDir, File rtDir, String packageFolder, boolean overrideVersionCheck, Map olderVersionMap, List sameVersionList) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XMLStreamException, ParseException {
      StringBuilder resultBuilder = new StringBuilder();
      if(olderVersionMap.size() > 0) {
         if(!overrideVersionCheck) {
            resultBuilder.append("The following older domains were NOT deployed:\n\n");
         } else {
            resultBuilder.append("The following older domains were deployed:\n\n");
         }
      }

      File packFolder = new File(updateDir, packageFolder);
      PackageMeta meta = PackageMeta.loadPackageMeta(packFolder.getAbsolutePath());
      File backupFolder = new File(packFolder.getParentFile(), "Backups" + File.separator + packFolder.getName());
      backupFolder.mkdirs();
      File applicationRtDir = new File(rtDir, meta.getApplicationName());
      LinkedList affectedRtFiles = new LinkedList();

      try {
         File e = new File(packFolder, "deploymentMeta.xml");
         if(e.exists()) {
            if(!applicationRtDir.exists()) {
               applicationRtDir.mkdirs();
            }

            File replacedProjectsJarNames = new File(applicationRtDir, "deploymentMeta.xml");
            if(replacedProjectsJarNames.exists()) {
               if(!backupFolder.exists()) {
                  backupFolder.mkdirs();
               }

               FileSystemUtil.copyFile(replacedProjectsJarNames, new File(backupFolder, replacedProjectsJarNames.getName()), true, true);
            }

            if(!Util.isWindowsSystem()) {
               handleDifferentFileCasing(replacedProjectsJarNames, backupFolder, false);
            }

            FileSystemUtil.copyFile(e, replacedProjectsJarNames, true, true);
            affectedRtFiles.add(replacedProjectsJarNames);
         }

         Set replacedProjectsJarNames1 = getReplacedProjectJarNamesFromRt(applicationRtDir);
         List elems = meta.getElements();
         Iterator elemIt = elems.iterator();

         File maintMgrDir;
         File protDir;
         while(elemIt.hasNext()) {
            PackageElement returnVal = (PackageElement)elemIt.next();
            if(returnVal.getElementType().equals(PackageElementType.THIRD_PARTY_LIBS)) {
               deployThirdPartyLibs(packFolder, applicationRtDir, backupFolder, affectedRtFiles);
            } else {
               maintMgrDir = new File(packFolder, returnVal.getElementPathRelative());
               protDir = new File(maintMgrDir, "Mpdv" + returnVal.getElementName() + ".jar");
               if(!replacedProjectsJarNames1.contains("Mpdv" + returnVal.getElementName() + ".jar")) {
                  boolean name = false;
                  if(olderVersionMap.containsKey(protDir.getName())) {
                     VersionComparisonInfo content = (VersionComparisonInfo)olderVersionMap.get(protDir.getName());
                     resultBuilder.append(returnVal.getElementName() + (returnVal.getCustomerName() != null && !"".equals(returnVal.getCustomerName())?"(" + returnVal.getCustomerName() + ")":"") + "\nExisting version: " + content.getRightVersionString() + "\nVersion to deploy: " + content.getLeftVersionString() + "\n\n");
                     if(!overrideVersionCheck) {
                        name = true;
                     }
                  } else if(sameVersionList.contains(protDir.getName()) && !overrideVersionCheck) {
                     name = true;
                  }

                  ProjectMeta content1 = ProjectMeta.loadProjectMeta(maintMgrDir.getAbsolutePath(), "Mpdv" + returnVal.getElementName());
                  if(!Util.stringNullOrEmpty(content1.getReplaceProject())) {
                     handleReplace(backupFolder, applicationRtDir, maintMgrDir, content1, name, affectedRtFiles);
                  }

                  if(!content1.getProjectType().equals("directservice") && (new File(maintMgrDir.getAbsolutePath() + File.separator + "rules.xml")).exists()) {
                     deployDomainByRules(backupFolder, applicationRtDir, returnVal, maintMgrDir, name, affectedRtFiles);
                  } else {
                     deployContentOfDirectSvcOrNoRuleElem(backupFolder, applicationRtDir, returnVal, maintMgrDir, name, affectedRtFiles);
                  }
               }
            }
         }

         meta.setDeploymentDate(DateTimeUtil.getCurrentUtcCalendar());
         meta.savePackageMeta(packFolder.getAbsolutePath());
         String returnVal1 = null;
         if(olderVersionMap.size() > 0) {
            returnVal1 = resultBuilder.toString();
         }

         maintMgrDir = rtDir.getParentFile().getParentFile();
         protDir = new File(maintMgrDir, "logs/java");
         protDir.mkdirs();
         String name1 = DateTimeUtil.calendarUtcToIsoString(DateTimeUtil.getCurrentUtcCalendar()) + "-DEPLOY-" + meta.getName() + ".txt";
         String content2 = "Deployed package " + meta.getName() + " at " + DateTimeUtil.calendarUtcToPrintString(DateTimeUtil.getCurrentUtcCalendar()) + " (UTC)\n";
         if(returnVal1 != null) {
            content2 = content2 + "\n" + returnVal1;
         }

         try {
            FileSystemUtil.writeTextFile(protDir, name1, content2);
         } catch (Exception var22) {
            ;
         }

         return returnVal1;
      } catch (RuntimeException var23) {
         rollBackForError(backupFolder, applicationRtDir, affectedRtFiles);
         throw var23;
      } catch (FileNotFoundException var24) {
         rollBackForError(backupFolder, applicationRtDir, affectedRtFiles);
         throw var24;
      } catch (ParserConfigurationException var25) {
         rollBackForError(backupFolder, applicationRtDir, affectedRtFiles);
         throw var25;
      } catch (SAXException var26) {
         rollBackForError(backupFolder, applicationRtDir, affectedRtFiles);
         throw var26;
      } catch (IOException var27) {
         rollBackForError(backupFolder, applicationRtDir, affectedRtFiles);
         throw var27;
      } catch (XMLStreamException var28) {
         rollBackForError(backupFolder, applicationRtDir, affectedRtFiles);
         throw var28;
      }
   }

   private static void rollBackForError(File backupFolder, File applicationRtDir, List affectedFiles) {
      File backupFolderClient;
      for(int clientBackupPath = 0; clientBackupPath < affectedFiles.size(); ++clientBackupPath) {
         backupFolderClient = (File)affectedFiles.get(clientBackupPath);

         try {
            if(backupFolderClient.isDirectory()) {
               FileSystemUtil.deleteDir(backupFolderClient);
            } else {
               backupFolderClient.delete();
            }
         } catch (Exception var9) {
            ;
         }
      }

      try {
         FileSystemUtil.copyDir(backupFolder, applicationRtDir, true, true);
      } catch (Exception var8) {
         ;
      }

      String var10 = (backupFolder.getAbsolutePath().endsWith(File.separator)?backupFolder.getAbsolutePath().substring(0, backupFolder.getAbsolutePath().length() - 1):backupFolder.getAbsolutePath()) + "-CLIENT";
      backupFolderClient = new File(var10);
      File applicationRtDirClient = new File(applicationRtDir.getParentFile().getParentFile(), "client/" + applicationRtDir.getName());
      if(backupFolderClient.exists()) {
         try {
            FileSystemUtil.copyDir(backupFolderClient, applicationRtDirClient, true, true);
         } catch (Exception var7) {
            ;
         }
      }

      FileSystemUtil.deleteDir(backupFolder);
      if(backupFolderClient.exists()) {
         FileSystemUtil.deleteDir(backupFolderClient);
      }

   }

   private static Set getReplacedProjectJarNamesFromRt(File rtAppDir) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
      HashSet replacedSet = new HashSet();
      File metaDir = new File(rtAppDir, "domainMeta");
      File[] metaFiles = metaDir.listFiles();
      if(metaFiles != null) {
         for(int i = 0; i < metaFiles.length; ++i) {
            File metaFile = metaFiles[i];
            String metaName = metaFile.getName();
            if(metaName.toLowerCase().startsWith("mpdvcust")) {
               ProjectMeta prjMeta = ProjectMeta.loadProjectMeta(metaDir.getAbsolutePath(), metaFile.getName().replace(".xml", ""));
               String replacedProject = prjMeta.getReplaceProject();
               if(!Util.stringNullOrEmpty(replacedProject)) {
                  replacedSet.add("Mpdv" + replacedProject + ".jar");
               }
            }
         }
      }

      return replacedSet;
   }

   private static void deployDomainByRules(File backupFolder, File applicationRtDir, PackageElement elem, File domainPath, boolean onlyBackup, List affectedRtFiles) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {
      String clientBackupPath = (backupFolder.getAbsolutePath().endsWith(File.separator)?backupFolder.getAbsolutePath().substring(0, backupFolder.getAbsolutePath().length() - 1):backupFolder.getAbsolutePath()) + "-CLIENT";
      File backupFolderClient = new File(clientBackupPath);
      backupFolderClient.mkdirs();
      File applicationRtDirClient = new File(applicationRtDir.getParentFile().getParentFile(), "client/" + applicationRtDir.getName());
      File destDomainMetaFolder = new File(applicationRtDir, "domainMeta");
      File backupDomainMetaFolder = new File(backupFolder, "domainMeta");
      File metaFile = new File(domainPath, "Mpdv" + elem.getElementName() + ".xml");
      if(!destDomainMetaFolder.exists()) {
         destDomainMetaFolder.mkdirs();
      }

      File destMetaFile = new File(destDomainMetaFolder, "Mpdv" + elem.getElementName() + ".xml");
      if(destMetaFile.exists()) {
         if(!backupDomainMetaFolder.exists()) {
            backupDomainMetaFolder.mkdirs();
         }

         if(onlyBackup) {
            FileSystemUtil.copyFile(destMetaFile, new File(backupDomainMetaFolder, metaFile.getName()), true, true);
         } else {
            (new File(backupDomainMetaFolder, metaFile.getName())).getParentFile().mkdirs();
            destMetaFile.renameTo(new File(backupDomainMetaFolder, metaFile.getName()));
         }
      }

      if(!Util.isWindowsSystem()) {
         handleDifferentFileCasing(destMetaFile, backupDomainMetaFolder, onlyBackup);
      }

      if(!onlyBackup) {
         FileSystemUtil.copyFile(metaFile, destMetaFile, true, true);
         affectedRtFiles.add(destMetaFile);
      }

      List rules = CopyRule.loadRulesFromFile(domainPath.getAbsolutePath() + File.separator + "rules.xml");

      File pdaRegDestFolder;
      File pdaRegBackupFolder;
      for(int pdaCompRegFolder = 0; pdaCompRegFolder < rules.size(); ++pdaCompRegFolder) {
         CopyRule filesToProcess = (CopyRule)rules.get(pdaCompRegFolder);
         File pdaCompRegFiles = !".".equals(filesToProcess.getSource()) && !"".equals(filesToProcess.getSource())?new File(domainPath, filesToProcess.getSource()):domainPath;
         if(pdaCompRegFiles.exists()) {
            String i;
            if(filesToProcess.getTarget().startsWith("#CLIENT#/")) {
               i = filesToProcess.getTarget().replaceFirst("#CLIENT#/", "");
               pdaRegDestFolder = !".".equals(i) && !"".equals(i)?new File(applicationRtDirClient, i.replace("#SCOPE#", elem.getElementPathRelative().substring(0, elem.getElementPathRelative().indexOf("/")).toLowerCase())):applicationRtDirClient;
               pdaRegBackupFolder = !".".equals(i) && !"".equals(i)?new File(backupFolderClient, i.replace("#SCOPE#", elem.getElementPathRelative().substring(0, elem.getElementPathRelative().indexOf("/")).toLowerCase())):backupFolderClient;
            } else {
               pdaRegDestFolder = !".".equals(filesToProcess.getTarget()) && !"".equals(filesToProcess.getTarget())?new File(applicationRtDir, filesToProcess.getTarget().replace("#SCOPE#", elem.getElementPathRelative().substring(0, elem.getElementPathRelative().indexOf("/")).toLowerCase())):applicationRtDir;
               pdaRegBackupFolder = !".".equals(filesToProcess.getTarget()) && !"".equals(filesToProcess.getTarget())?new File(backupFolder, filesToProcess.getTarget().replace("#SCOPE#", elem.getElementPathRelative().substring(0, elem.getElementPathRelative().indexOf("/")).toLowerCase())):backupFolder;
            }

            i = filesToProcess.getFilter();
            int in;
            File e;
            if(i != null) {
               File[] f = pdaCompRegFiles.listFiles();

               for(in = 0; in < f.length; ++in) {
                  e = f[in];
                  if(!e.isDirectory() && e.getName().endsWith(i)) {
                     if(!pdaRegDestFolder.exists()) {
                        pdaRegDestFolder.mkdirs();
                     }

                     File it = new File(pdaRegDestFolder, e.getName());
                     if(it.exists()) {
                        if(!pdaRegBackupFolder.exists()) {
                           pdaRegBackupFolder.mkdirs();
                        }

                        if(onlyBackup) {
                           FileSystemUtil.copyFile(it, new File(pdaRegBackupFolder, e.getName()), true, true);
                        } else {
                           (new File(pdaRegBackupFolder, e.getName())).getParentFile().mkdirs();
                           it.renameTo(new File(pdaRegBackupFolder, e.getName()));
                        }
                     }

                     if(!Util.isWindowsSystem()) {
                        handleDifferentFileCasing(it, pdaRegBackupFolder, onlyBackup);
                     }

                     if(!onlyBackup) {
                        FileSystemUtil.copyFile(e, it, true, true);
                        affectedRtFiles.add(it);
                     }
                  }
               }
            } else {
               List var85 = FileSystemUtil.getFileListing(pdaCompRegFiles);

               for(in = 0; in < var85.size(); ++in) {
                  e = (File)var85.get(in);
                  String var89 = e.getAbsolutePath().replace(pdaCompRegFiles.getAbsolutePath() + File.separator, "");
                  if(!pdaRegDestFolder.exists()) {
                     pdaRegDestFolder.mkdirs();
                  }

                  File targetComponent = new File(pdaRegDestFolder, var89);
                  if(targetComponent.exists()) {
                     if(!pdaRegBackupFolder.exists()) {
                        pdaRegBackupFolder.mkdirs();
                     }

                     if(onlyBackup) {
                        FileSystemUtil.copyFile(targetComponent, new File(pdaRegBackupFolder, var89), true, true);
                     } else {
                        (new File(pdaRegBackupFolder, var89)).getParentFile().mkdirs();
                        targetComponent.renameTo(new File(pdaRegBackupFolder, var89));
                     }
                  }

                  if(!Util.isWindowsSystem()) {
                     handleDifferentFileCasing(targetComponent, pdaRegBackupFolder, onlyBackup);
                  }

                  if(!onlyBackup) {
                     FileSystemUtil.copyFile(e, targetComponent, true, true);
                     affectedRtFiles.add(targetComponent);
                  }
               }
            }
         }
      }

      File var80 = new File(domainPath, "PdaCompRegister");
      if(var80.exists()) {
         LinkedList var81 = new LinkedList();
         File[] var82 = var80.listFiles();
         if(var82 != null) {
            for(int var83 = 0; var83 < var82.length; ++var83) {
               pdaRegBackupFolder = var82[var83];
               if(pdaRegBackupFolder.isFile() && pdaRegBackupFolder.getName().endsWith(".properties")) {
                  var81.add(pdaRegBackupFolder);
               }
            }
         }

         if(var81.size() > 0) {
            pdaRegDestFolder = new File(applicationRtDir, "jhydra-inst/pdaConfig");
            if(!pdaRegDestFolder.exists()) {
               pdaRegDestFolder.mkdirs();
            }

            pdaRegBackupFolder = new File(backupFolder, "jhydra-inst/pdaConfig");
            if(!pdaRegBackupFolder.exists()) {
               FileSystemUtil.copyDir(pdaRegDestFolder, pdaRegBackupFolder, true, true);
            }

            if(!onlyBackup) {
               for(int var84 = 0; var84 < var81.size(); ++var84) {
                  File var86 = (File)var81.get(var84);
                  FileInputStream var87 = null;

                  try {
                     var87 = new FileInputStream(var86);
                     Properties var88 = new Properties();
                     var88.load(var87);
                     Iterator var90 = var88.keySet().iterator();

                     while(var90.hasNext()) {
                        String var91 = (String)var90.next();
                        Properties targetProps = new Properties();
                        File targetCompFile = new File(pdaRegDestFolder, var91 + ".conf");
                        if(targetCompFile.exists()) {
                           FileInputStream out = null;

                           try {
                              out = new FileInputStream(targetCompFile);
                              targetProps.load(out);
                           } catch (Exception var75) {
                              throw new RuntimeException("Error processing pda config of " + var86, var75);
                           } finally {
                              if(out != null) {
                                 try {
                                    out.close();
                                 } catch (IOException var73) {
                                    ;
                                 }
                              }

                           }
                        }

                        targetProps.setProperty(var88.getProperty(var91), var86.getName().substring(0, var86.getName().length() - 11));
                        FileOutputStream var92 = null;

                        try {
                           var92 = new FileOutputStream(targetCompFile);
                           targetProps.store(var92, (String)null);
                           var92.flush();
                           affectedRtFiles.add(targetCompFile);
                        } catch (Exception var74) {
                           throw new RuntimeException("Error processing pda config of " + var86, var74);
                        } finally {
                           if(var92 != null) {
                              try {
                                 var92.close();
                              } catch (IOException var72) {
                                 ;
                              }
                           }

                        }
                     }
                  } catch (Exception var78) {
                     throw new RuntimeException("Error processing pda config of " + var86, var78);
                  } finally {
                     if(var87 != null) {
                        try {
                           var87.close();
                        } catch (IOException var71) {
                           ;
                        }
                     }

                  }
               }
            }
         }
      }

   }

   private static void deployContentOfDirectSvcOrNoRuleElem(File backupFolder, File applicationRtDir, PackageElement elem, File domainPath, boolean onlyBackup, List affectedRtFiles) throws FileNotFoundException, IOException {
      File destDomainMetaFolder = new File(applicationRtDir, "domainMeta");
      File backupDomainMetaFolder = new File(backupFolder, "domainMeta");
      File metaFile = new File(domainPath, "Mpdv" + elem.getElementName() + ".xml");
      if(!destDomainMetaFolder.exists()) {
         destDomainMetaFolder.mkdirs();
      }

      File destMetaFile = new File(destDomainMetaFolder, "Mpdv" + elem.getElementName() + ".xml");
      if(destMetaFile.exists()) {
         if(!backupDomainMetaFolder.exists()) {
            backupDomainMetaFolder.mkdirs();
         }

         FileSystemUtil.copyFile(destMetaFile, new File(backupDomainMetaFolder, metaFile.getName()), true, true);
      }

      if(!Util.isWindowsSystem()) {
         handleDifferentFileCasing(destMetaFile, backupDomainMetaFolder, false);
      }

      if(!onlyBackup) {
         FileSystemUtil.copyFile(metaFile, destMetaFile, true, true);
         affectedRtFiles.add(destMetaFile);
      }

      File destCodeFolder = new File(applicationRtDir, "code");
      File backupCodeFolder = new File(backupFolder, "code");
      File codeFile = new File(domainPath, "Mpdv" + elem.getElementName() + ".jar");
      if(!destCodeFolder.exists()) {
         destCodeFolder.mkdirs();
      }

      File destCodeFile = new File(destCodeFolder, "Mpdv" + elem.getElementName() + ".jar");
      if(destCodeFile.exists()) {
         if(!backupCodeFolder.exists()) {
            backupCodeFolder.mkdirs();
         }

         FileSystemUtil.copyFile(destCodeFile, new File(backupCodeFolder, codeFile.getName()), true, true);
      }

      if(!Util.isWindowsSystem()) {
         handleDifferentFileCasing(destCodeFile, backupCodeFolder, onlyBackup);
      }

      if(!onlyBackup) {
         FileSystemUtil.copyFile(codeFile, destCodeFile, true, true);
         affectedRtFiles.add(destCodeFile);
      }

      File destConfFolder = new File(applicationRtDir, "conf");
      File backupConfFolder = new File(backupFolder, "conf");
      File confFolder = new File(domainPath, "conf");
      if(confFolder.exists()) {
         if(destConfFolder.exists()) {
            if(!backupConfFolder.exists()) {
               backupConfFolder.mkdirs();
            }

            FileSystemUtil.copyDir(destConfFolder, backupConfFolder, true, true);
         }

         if(!onlyBackup) {
            if(!destConfFolder.exists()) {
               destConfFolder.mkdirs();
            }

            FileSystemUtil.copyDir(confFolder, destConfFolder, true, true);
            affectedRtFiles.add(destConfFolder);
         }
      }

   }

   private static void handleReplace(File backupFolder, File applicationRtDir, File domainPath, ProjectMeta prjMeta, boolean onlyBackup, List affectedRtFiles) throws FileNotFoundException, IOException {
      String replaceBaseName = "Mpdv" + prjMeta.getReplaceProject();
      File destDomainMetaFolder = new File(applicationRtDir, "domainMeta");
      File backupDomainMetaFolder = new File(backupFolder, "domainMeta");
      File domainMetaFile = new File(destDomainMetaFolder, replaceBaseName + ".xml");
      if(domainMetaFile.exists()) {
         if(!backupDomainMetaFolder.exists()) {
            backupDomainMetaFolder.mkdirs();
         }

         FileSystemUtil.copyFile(domainMetaFile, new File(backupDomainMetaFolder, domainMetaFile.getName()), true, true);
         if(!onlyBackup) {
            domainMetaFile.delete();
         }
      }

      if(!Util.isWindowsSystem()) {
         handleDifferentFileCasing(domainMetaFile, backupDomainMetaFolder, false);
      }

      File destCodeFolder = new File(applicationRtDir, "code");
      File backupCodeFolder = new File(backupFolder, "code");
      File domainLibFile = new File(destCodeFolder, replaceBaseName + ".jar");
      if(domainLibFile.exists()) {
         if(!backupCodeFolder.exists()) {
            backupCodeFolder.mkdirs();
         }

         FileSystemUtil.copyFile(domainLibFile, new File(backupCodeFolder, domainLibFile.getName()), true, true);
         if(!onlyBackup) {
            domainLibFile.delete();
         }
      }

      if(!Util.isWindowsSystem()) {
         handleDifferentFileCasing(domainLibFile, backupCodeFolder, false);
      }

      if(prjMeta.getProjectType().equals("directservice") || !(new File(domainPath.getAbsolutePath() + File.separator + "rules.xml")).exists()) {
         File destConfFolder = new File(applicationRtDir, "conf");
         File backupConfFolder = new File(backupFolder, "conf");
         if(destConfFolder.exists()) {
            if(!backupConfFolder.exists()) {
               backupConfFolder.mkdirs();
            }

            FileSystemUtil.copyDir(destConfFolder, backupConfFolder, true, true);
            if(!onlyBackup) {
               FileSystemUtil.deleteDir(destConfFolder);
            }
         }
      }

   }

   private static void deployThirdPartyLibs(File packFolder, File applicationRtDir, File backupFolder, List affectedRtFiles) throws FileNotFoundException, IOException {
      File srcFolder = new File(packFolder, "ThirdPartyLibs");
      if(srcFolder.exists()) {
         File[] libFiles = srcFolder.listFiles();
         File destFolder = new File(applicationRtDir, "ThirdPartyLibs");
         File curBackupFolder = new File(backupFolder, "ThirdPartyLibs");

         for(int i = 0; i < libFiles.length; ++i) {
            File f = libFiles[i];
            if(!f.isDirectory()) {
               if(!destFolder.exists()) {
                  destFolder.mkdirs();
               }

               File destFile = new File(destFolder, f.getName());
               if(destFile.exists()) {
                  if(!curBackupFolder.exists()) {
                     curBackupFolder.mkdirs();
                  }

                  (new File(curBackupFolder, f.getName())).getParentFile().mkdirs();
                  destFile.renameTo(new File(curBackupFolder, f.getName()));
               }

               if(!Util.isWindowsSystem()) {
                  handleDifferentFileCasing(destFile, curBackupFolder, false);
               }

               FileSystemUtil.copyFile(f, destFile, true, true);
               affectedRtFiles.add(destFile);
            }
         }

      }
   }

   public static void undeployPackage(File updateDir, File rtDir, String packageFolder) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, ParseException, XMLStreamException {
      File packFolder = new File(updateDir, packageFolder);
      PackageMeta meta = PackageMeta.loadPackageMeta(packFolder.getAbsolutePath());
      File backupFolder = new File(packFolder.getParentFile(), "Backups" + File.separator + packFolder.getName());
      File backupFolderClient = new File(packFolder.getParentFile(), "Backups" + File.separator + packFolder.getName() + "-CLIENT");
      File applicationRtDir = new File(rtDir, meta.getApplicationName());
      File applicationRtDirClient = new File(rtDir.getParentFile(), "client/" + meta.getApplicationName());
      File destFile = new File(applicationRtDir, "deploymentMeta.xml");
      if(destFile.exists()) {
         destFile.delete();
      }

      Set replacedProjectsJarNames = getReplacedProjectJarNamesFromRt(applicationRtDir);
      List elems = meta.getElements();
      Iterator elemIt = elems.iterator();

      File maintMgrDir;
      while(elemIt.hasNext()) {
         PackageElement pdaRegBackupFolder = (PackageElement)elemIt.next();
         if(pdaRegBackupFolder.getElementType().equals(PackageElementType.THIRD_PARTY_LIBS)) {
            undeployThirdPartyLibs(packFolder, applicationRtDir, backupFolder);
         } else {
            maintMgrDir = new File(packFolder, pdaRegBackupFolder.getElementPathRelative());
            if(!replacedProjectsJarNames.contains("Mpdv" + pdaRegBackupFolder.getElementName() + ".jar")) {
               ProjectMeta protDir = ProjectMeta.loadProjectMeta(maintMgrDir.getAbsolutePath(), "Mpdv" + pdaRegBackupFolder.getElementName());
               if(!protDir.getProjectType().equals("directservice") && (new File(maintMgrDir.getAbsolutePath() + File.separator + "rules.xml")).exists()) {
                  undeployDomainByRules(backupFolder, applicationRtDir, pdaRegBackupFolder, maintMgrDir);
               } else {
                  undeployContentOfDirectSvcOrNoRuleElem(backupFolder, applicationRtDir, pdaRegBackupFolder, maintMgrDir);
               }
            }
         }
      }

      File pdaRegBackupFolder1 = new File(backupFolder, "jhydra-inst/pdaConfig");
      if(pdaRegBackupFolder1.exists()) {
         maintMgrDir = new File(applicationRtDir, "jhydra-inst/pdaConfig");
         FileSystemUtil.deleteDir(maintMgrDir);
      }

      FileSystemUtil.copyDir(backupFolder, applicationRtDir, true, true);
      if(backupFolderClient.exists()) {
         FileSystemUtil.copyDir(backupFolderClient, applicationRtDirClient, true, true);
      }

      FileSystemUtil.deleteDir(backupFolder);
      if(backupFolderClient.exists()) {
         FileSystemUtil.deleteDir(backupFolderClient);
      }

      meta.setDeploymentDate((Calendar)null);
      meta.savePackageMeta(packFolder.getAbsolutePath());
      maintMgrDir = rtDir.getParentFile().getParentFile();
      File protDir1 = new File(maintMgrDir, "logs/java");
      protDir1.mkdirs();
      String name = DateTimeUtil.calendarUtcToIsoString(DateTimeUtil.getCurrentUtcCalendar()) + "-UNDEPLOY-" + meta.getName() + ".txt";
      String content = "Undeployed package " + meta.getName() + " at " + DateTimeUtil.calendarUtcToPrintString(DateTimeUtil.getCurrentUtcCalendar()) + " (UTC)\n";

      try {
         FileSystemUtil.writeTextFile(protDir1, name, content);
      } catch (Exception var19) {
         ;
      }

   }

   private static void undeployThirdPartyLibs(File packFolder, File applicationRtDir, File backupFolder) throws FileNotFoundException, IOException {
      File srcFolder = new File(packFolder, "ThirdPartyLibs");
      if(srcFolder.exists()) {
         File[] libFiles = srcFolder.listFiles();
         File destFolder = new File(applicationRtDir, "ThirdPartyLibs");

         for(int currDelDir = 0; currDelDir < libFiles.length; ++currDelDir) {
            File f = libFiles[currDelDir];
            if(!f.isDirectory()) {
               File destFile = new File(destFolder, f.getName());
               if(destFile.exists()) {
                  destFile.delete();
               }
            }
         }

         for(File var9 = destFolder; !var9.equals(applicationRtDir) && var9.listFiles() != null && var9.listFiles().length == 0; var9 = var9.getParentFile()) {
            var9.delete();
         }

      }
   }

   private static void undeployDomainByRules(File backupFolder, File applicationRtDir, PackageElement elem, File domainPath) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {
      String clientBackupPath = (backupFolder.getAbsolutePath().endsWith(File.separator)?backupFolder.getAbsolutePath().substring(0, backupFolder.getAbsolutePath().length() - 1):backupFolder.getAbsolutePath()) + "-CLIENT";
      File backupFolderClient = new File(clientBackupPath);
      backupFolderClient.mkdirs();
      File applicationRtDirClient = new File(applicationRtDir.getParentFile().getParentFile(), "client/" + applicationRtDir.getName());
      File destDomainMetaFolder = new File(applicationRtDir, "domainMeta");
      File destMetaFile = new File(destDomainMetaFolder, "Mpdv" + elem.getElementName() + ".xml");
      if(destMetaFile.exists()) {
         destMetaFile.delete();
      }

      File currDelDir;
      for(currDelDir = destDomainMetaFolder; !currDelDir.equals(applicationRtDir) && currDelDir.listFiles() != null && currDelDir.listFiles().length == 0; currDelDir = currDelDir.getParentFile()) {
         currDelDir.delete();
      }

      List rules = CopyRule.loadRulesFromFile(domainPath.getAbsolutePath() + File.separator + "rules.xml");

      for(int j = 0; j < rules.size(); ++j) {
         CopyRule r = (CopyRule)rules.get(j);
         File curSrcFolder = !".".equals(r.getSource()) && !"".equals(r.getSource())?new File(domainPath, r.getSource()):domainPath;
         if(curSrcFolder.exists()) {
            File curDestFolder;
            String filter;
            if(r.getTarget().startsWith("#CLIENT#/")) {
               filter = r.getTarget().replaceFirst("#CLIENT#/", "");
               curDestFolder = !".".equals(filter) && !"".equals(filter)?new File(applicationRtDirClient, filter.replace("#SCOPE#", elem.getElementPathRelative().substring(0, elem.getElementPathRelative().indexOf("/")).toLowerCase())):applicationRtDirClient;
            } else {
               curDestFolder = !".".equals(r.getTarget()) && !"".equals(r.getTarget())?new File(applicationRtDir, r.getTarget().replace("#SCOPE#", elem.getElementPathRelative().substring(0, elem.getElementPathRelative().indexOf("/")).toLowerCase())):applicationRtDir;
            }

            filter = r.getFilter();
            int k;
            File srcFile;
            File destFile;
            if(filter != null) {
               File[] srcFiles = curSrcFolder.listFiles();

               for(k = 0; k < srcFiles.length; ++k) {
                  srcFile = srcFiles[k];
                  boolean relativePath = true;
                  if(srcFile.isDirectory()) {
                     relativePath = false;
                  }

                  if(relativePath) {
                     if(!srcFile.getName().endsWith(filter)) {
                        relativePath = false;
                     }

                     if(relativePath) {
                        destFile = new File(curDestFolder, srcFile.getName());
                        if(destFile.exists()) {
                           destFile.delete();
                        }
                     }
                  }

                  for(currDelDir = curDestFolder; !currDelDir.equals(applicationRtDir) && currDelDir.listFiles() != null && currDelDir.listFiles().length == 0; currDelDir = currDelDir.getParentFile()) {
                     currDelDir.delete();
                  }
               }
            } else {
               List var21 = FileSystemUtil.getFileListing(curSrcFolder);

               for(k = 0; k < var21.size(); ++k) {
                  srcFile = (File)var21.get(k);
                  String var22 = srcFile.getAbsolutePath().replace(curSrcFolder.getAbsolutePath() + File.separator, "");
                  destFile = new File(curDestFolder, var22);
                  if(destFile.exists()) {
                     destFile.delete();
                  }

                  for(currDelDir = destFile.getParentFile(); !currDelDir.equals(applicationRtDir) && currDelDir.listFiles() != null && currDelDir.listFiles().length == 0; currDelDir = currDelDir.getParentFile()) {
                     currDelDir.delete();
                  }
               }
            }
         }
      }

   }

   private static void undeployContentOfDirectSvcOrNoRuleElem(File backupFolder, File applicationRtDir, PackageElement elem, File domainPath) throws FileNotFoundException, IOException {
      File destDomainMetaFolder = new File(applicationRtDir, "domainMeta");
      File destMetaFile = new File(destDomainMetaFolder, "Mpdv" + elem.getElementName() + ".xml");
      if(destMetaFile.exists()) {
         destMetaFile.delete();
      }

      File currDelDir;
      for(currDelDir = destDomainMetaFolder; !currDelDir.equals(applicationRtDir) && currDelDir.listFiles() != null && currDelDir.listFiles().length == 0; currDelDir = currDelDir.getParentFile()) {
         currDelDir.delete();
      }

      File destCodeFolder = new File(applicationRtDir, "code");
      File destCodeFile = new File(destCodeFolder, "Mpdv" + elem.getElementName() + ".jar");
      if(destCodeFile.exists()) {
         destCodeFile.delete();
      }

      for(currDelDir = destCodeFolder; !currDelDir.equals(applicationRtDir) && currDelDir.listFiles() != null && currDelDir.listFiles().length == 0; currDelDir = currDelDir.getParentFile()) {
         currDelDir.delete();
      }

      File destConfFolder = new File(applicationRtDir, "conf");
      File backupConfFolder = new File(backupFolder, "conf");
      if(backupConfFolder.exists() && destConfFolder.exists()) {
         FileSystemUtil.deleteDir(destConfFolder);
      }

   }

   public static Map getOlderFilesInPackage(Map packageVersions, Map rtVersions) {
      HashMap compList = new HashMap();
      Iterator packFileIt = packageVersions.keySet().iterator();

      while(packFileIt.hasNext()) {
         String fileName = (String)packFileIt.next();
         VersionInfo packVersInfo = (VersionInfo)packageVersions.get(fileName);
         VersionInfo rtVersInfo = (VersionInfo)rtVersions.get(fileName);
         if(packVersInfo != null && rtVersInfo != null) {
            boolean packVersionSet = packVersInfo.getMajor() != null && packVersInfo.getMinor() != null && packVersInfo.getRevision() != null;
            boolean rtVersionSet = rtVersInfo.getMajor() != null && rtVersInfo.getMinor() != null && rtVersInfo.getRevision() != null;
            if(packVersionSet && rtVersionSet && (packVersInfo.getMajor().intValue() != rtVersInfo.getMajor().intValue() || packVersInfo.getMinor().intValue() != rtVersInfo.getMinor().intValue() || packVersInfo.getRevision().intValue() != rtVersInfo.getRevision().intValue()) && packVersInfo.getMajor().intValue() <= rtVersInfo.getMajor().intValue() && (packVersInfo.getMajor().intValue() != rtVersInfo.getMajor().intValue() || packVersInfo.getMinor().intValue() <= rtVersInfo.getMinor().intValue()) && (packVersInfo.getMajor().intValue() != rtVersInfo.getMajor().intValue() || packVersInfo.getMinor().intValue() != rtVersInfo.getMinor().intValue() || packVersInfo.getRevision().intValue() < rtVersInfo.getRevision().intValue())) {
               compList.put(fileName, new VersionComparisonInfo(fileName, packVersInfo.getVendor(), packVersInfo.getTitle(), packVersInfo.getVersionString(), packVersInfo.getMajor(), packVersInfo.getMinor(), packVersInfo.getRevision(), packVersInfo.getChangeDate(), rtVersInfo.getVersionString(), rtVersInfo.getMajor(), rtVersInfo.getMinor(), rtVersInfo.getRevision(), rtVersInfo.getChangeDate()));
            }
         }
      }

      return compList;
   }

   public static List getSameVersionFilesInPackage(Map packageVersions, Map rtVersions) {
      ArrayList list = new ArrayList();
      Iterator packFileIt = packageVersions.keySet().iterator();

      while(packFileIt.hasNext()) {
         String fileName = (String)packFileIt.next();
         VersionInfo packVersInfo = (VersionInfo)packageVersions.get(fileName);
         VersionInfo rtVersInfo = (VersionInfo)rtVersions.get(fileName);
         if(packVersInfo != null && rtVersInfo != null) {
            boolean packVersionSet = packVersInfo.getMajor() != null && packVersInfo.getMinor() != null && packVersInfo.getRevision() != null;
            boolean rtVersionSet = rtVersInfo.getMajor() != null && rtVersInfo.getMinor() != null && rtVersInfo.getRevision() != null;
            if(packVersionSet && rtVersionSet && packVersInfo.getMajor().intValue() == rtVersInfo.getMajor().intValue() && packVersInfo.getMinor().intValue() == rtVersInfo.getMinor().intValue() && packVersInfo.getRevision().intValue() == rtVersInfo.getRevision().intValue()) {
               list.add(fileName);
            }
         }
      }

      return list;
   }

   public static Map getRuntimeFileVersions(File runtimeDir, Map packageVersions, String tempDir) throws IOException {
      List rtFiles = FileSystemUtil.getFileListing(runtimeDir);
      LinkedList relevantRtFiles = new LinkedList();
      int fileCount = rtFiles.size();

      for(int rtVersionMap = 0; rtVersionMap < fileCount; ++rtVersionMap) {
         File rtVersions = (File)rtFiles.get(rtVersionMap);
         if(packageVersions.containsKey(rtVersions.getName())) {
            relevantRtFiles.add(rtVersions);
         }
      }

      HashMap var11 = new HashMap();
      List var12 = VersionInformationRetriever.getVersionInformationJarFiles(relevantRtFiles, tempDir);
      int count = var12.size();

      for(int i = 0; i < count; ++i) {
         VersionInfo info = (VersionInfo)var12.get(i);
         var11.put(info.getFileName(), info);
      }

      return var11;
   }

   public static Map getPackageFileVersions(String packageFolder, File updateDir, String tempDir) throws IOException {
      LinkedHashMap map = new LinkedHashMap();
      List versionList = VersionInformationRetriever.getVersionInformationFolder((new File(updateDir.getAbsolutePath() + File.separator + packageFolder)).getAbsolutePath(), tempDir);
      int count = versionList.size();

      for(int i = 0; i < count; ++i) {
         VersionInfo info = (VersionInfo)versionList.get(i);
         map.put(info.getFileName(), info);
      }

      return map;
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