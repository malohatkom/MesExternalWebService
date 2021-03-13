package de.mpdv.maintenanceManager.logic.javaServer;

import de.mpdv.maintenanceManager.data.javaServer.VersionComparisonInfo;
import de.mpdv.maintenanceManager.data.javaServer.VersionInfo;
import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.ZipUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class VersionInformationRetriever {

   public static List getVersionInformationWarFile(String warFilePath, String tempDir) throws IOException {
      if(warFilePath != null && !warFilePath.equals("")) {
         File f = new File(warFilePath);
         if(!f.exists()) {
            throw new FileNotFoundException("File " + warFilePath + " does not exist");
         } else {
            String folder = UUID.randomUUID().toString();
            File tmp = new File(tempDir, folder);

            List var5;
            try {
               tmp.mkdirs();
               ZipUtil.unzip(warFilePath, tmp.getAbsolutePath(), false);
               var5 = getVersionInformationFolder(tmp.getAbsolutePath(), tempDir);
            } finally {
               FileSystemUtil.deleteDir(tmp);
            }

            return var5;
         }
      } else {
         throw new IllegalArgumentException("Parameter warFilePath is null or empty");
      }
   }

   public static List getVersionInformationFolder(String folderPath, String tempDir) throws IOException {
      if(folderPath != null && !folderPath.equals("")) {
         File folder = new File(folderPath);
         if(!folder.exists()) {
            throw new FileNotFoundException("Folder " + folderPath + " does not exist");
         } else {
            List fullFileList = FileSystemUtil.getFileListing(folder);
            LinkedList jarFileList = new LinkedList();
            Iterator it = fullFileList.iterator();

            while(it.hasNext()) {
               File f = (File)it.next();
               if(f.getName().toLowerCase().endsWith(".jar")) {
                  jarFileList.add(f);
               }
            }

            return getVersionInformationJarFiles(jarFileList, tempDir);
         }
      } else {
         throw new IllegalArgumentException("Parameter folderPath is null or empty");
      }
   }

   public static List getVersionInformationJarFiles(List jarFiles, String tempDir) throws FileNotFoundException, IOException {
      String tmpFolder = UUID.randomUUID().toString();
      File tmp = new File(tempDir, tmpFolder);
      FileInputStream inStr = null;

      try {
         TreeMap infoMap = new TreeMap();
         Iterator it = jarFiles.iterator();
         int folderCount = 0;

         while(it.hasNext()) {
            File info = new File(tmp, "" + folderCount);
            info.mkdirs();
            File treeIt = (File)it.next();
            ZipUtil.unzip(treeIt.getAbsolutePath(), info.getAbsolutePath(), true);
            File fName = null;
            List contents = FileSystemUtil.getFileListing(info);
            int count = contents.size();

            for(int p = 0; p < count; ++p) {
               File vendor = (File)contents.get(p);
               if(vendor.getName().toUpperCase().equals("MANIFEST.MF") && vendor.getParentFile().getName().toUpperCase().equals("META-INF")) {
                  fName = vendor;
                  break;
               }
            }

            if(fName != null) {
               Properties var29 = new Properties();

               try {
                  inStr = new FileInputStream(fName);
               } catch (Exception var23) {
                  continue;
               }

               var29.load(inStr);
               String var30 = var29.getProperty("Implementation-Vendor");
               String version = var29.getProperty("Implementation-Version");
               String title = var29.getProperty("Implementation-Title");
               String changeDate = var29.getProperty("Implementation-Time");
               String fName1 = treeIt.getName();
               infoMap.put(fName1.toLowerCase(), getVersionInfo(var30, title, changeDate, fName1, version));
               inStr.close();
               ++folderCount;
            }
         }

         LinkedList var25 = new LinkedList();
         Iterator var26 = infoMap.keySet().iterator();

         while(var26.hasNext()) {
            String var27 = (String)var26.next();
            var25.add(infoMap.get(var27));
         }

         LinkedList var28 = var25;
         return var28;
      } finally {
         if(inStr != null) {
            inStr.close();
         }

         FileSystemUtil.deleteDir(tmp);
      }
   }

   public static List compareVersions(List leftInfo, List rightInfo) {
      TreeMap map = new TreeMap();
      Iterator leftIt = leftInfo.iterator();

      while(leftIt.hasNext()) {
         VersionInfo rightIt = (VersionInfo)leftIt.next();
         map.put(rightIt.getFileName().toLowerCase(), new VersionComparisonInfo(rightIt.getFileName(), rightIt.getVendor(), rightIt.getTitle(), rightIt.getVersionString(), rightIt.getMajor(), rightIt.getMinor(), rightIt.getRevision(), rightIt.getChangeDate(), (String)null, (Integer)null, (Integer)null, (Integer)null, (String)null));
      }

      Iterator var11 = rightInfo.iterator();

      while(var11.hasNext()) {
         VersionInfo compList = (VersionInfo)var11.next();
         VersionComparisonInfo keySet = (VersionComparisonInfo)map.get(compList.getFileName().toLowerCase());
         if(keySet != null) {
            keySet.setRightVersionString(compList.getVersionString());
            keySet.setRightMajor(compList.getMajor());
            keySet.setRightMinor(compList.getMinor());
            keySet.setRightRevision(compList.getRevision());
            keySet.setRightChangeDate(compList.getChangeDate());
         } else {
            keySet = new VersionComparisonInfo(compList.getFileName(), compList.getVendor(), compList.getTitle(), (String)null, (Integer)null, (Integer)null, (Integer)null, (String)null, compList.getVersionString(), compList.getMajor(), compList.getMinor(), compList.getRevision(), compList.getChangeDate());
            map.put(compList.getFileName().toLowerCase(), keySet);
         }
      }

      LinkedList var12 = new LinkedList();
      Set var13 = map.keySet();
      LinkedList keyList = new LinkedList(var13);
      int keyCount = keyList.size();

      for(int i = 0; i < keyCount; ++i) {
         String key = (String)keyList.get(i);
         var12.add(map.get(key));
      }

      return var12;
   }

   private static VersionInfo getVersionInfo(String vendor, String title, String changeDate, String fname, String versionStr) {
      if(versionStr != null && !versionStr.equals("")) {
         int major;
         int minor;
         int revision;
         String[] parts;
         if(versionStr.contains("svn-rev")) {
            parts = versionStr.split("\\.");
            if(parts.length != 3) {
               return new VersionInfo(fname, vendor, title, versionStr, changeDate, (Integer)null, (Integer)null, (Integer)null);
            }

            if("trunk".equals(parts[0])) {
               major = 0;
               minor = 0;
            } else {
               major = 1;
               minor = 0;
            }

            try {
               revision = Integer.valueOf(parts[2]).intValue();
            } catch (NumberFormatException var13) {
               return new VersionInfo(fname, vendor, title, versionStr, changeDate, (Integer)null, (Integer)null, (Integer)null);
            }
         } else {
            parts = versionStr.split("\\.");
            if(parts.length != 4) {
               return new VersionInfo(fname, vendor, title, versionStr, changeDate, (Integer)null, (Integer)null, (Integer)null);
            }

            try {
               major = Integer.valueOf(parts[0]).intValue();
            } catch (NumberFormatException var12) {
               return new VersionInfo(fname, vendor, title, versionStr, changeDate, (Integer)null, (Integer)null, (Integer)null);
            }

            try {
               minor = Integer.valueOf(parts[1]).intValue();
            } catch (NumberFormatException var11) {
               return new VersionInfo(fname, vendor, title, versionStr, changeDate, (Integer)null, (Integer)null, (Integer)null);
            }

            try {
               revision = Integer.valueOf(parts[3]).intValue();
            } catch (NumberFormatException var10) {
               return new VersionInfo(fname, vendor, title, versionStr, changeDate, (Integer)null, (Integer)null, (Integer)null);
            }
         }

         return new VersionInfo(fname, vendor, title, versionStr, changeDate, Integer.valueOf(major), Integer.valueOf(minor), Integer.valueOf(revision));
      } else {
         return new VersionInfo(fname, vendor, title, versionStr, changeDate, (Integer)null, (Integer)null, (Integer)null);
      }
   }
}