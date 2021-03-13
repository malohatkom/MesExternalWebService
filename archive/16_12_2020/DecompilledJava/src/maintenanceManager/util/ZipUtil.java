package de.mpdv.maintenanceManager.util;

import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.Util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.fileupload.FileItem;

public class ZipUtil {

   public static void unzip(FileItem srcFile, String destPath, boolean onlyManifest) throws IOException, FileNotFoundException {
      if(srcFile == null) {
         throw new IllegalArgumentException("Parameter srcFile is null");
      } else if(destPath != null && !destPath.equals("")) {
         File destinationPath = new File(destPath);
         if(!destinationPath.exists()) {
            destinationPath.mkdirs();
         }

         InputStream in = null;
         ArchiveInputStream arcIn = null;

         try {
            in = srcFile.getInputStream();
            arcIn = (new ArchiveStreamFactory()).createArchiveInputStream("zip", in);
            ZipArchiveEntry e = null;
            HashMap realFolderNameMap = new HashMap();

            while((e = (ZipArchiveEntry)arcIn.getNextEntry()) != null) {
               if(!onlyManifest || e.getName().toUpperCase().equals("META-INF/") || e.getName().toUpperCase().equals("META-INF/MANIFEST.MF")) {
                  storeZipArchiveEntry(e, destPath, realFolderNameMap, arcIn);
               }
            }
         } catch (ArchiveException var19) {
            throw new IOException("Error at creation of archive input stream\n" + Util.exceptionToString(var19));
         } finally {
            if(arcIn != null) {
               try {
                  arcIn.close();
               } catch (IOException var18) {
                  ;
               }
            }

            if(in != null) {
               try {
                  in.close();
               } catch (IOException var17) {
                  ;
               }
            }

         }

      } else {
         throw new IllegalArgumentException("Parameter destPath is null or empty");
      }
   }

   public static void unzip(String srcFile, String destPath, boolean onlyManifest) throws IOException, FileNotFoundException {
      if(srcFile != null && !srcFile.equals("")) {
         if(destPath != null && !destPath.equals("")) {
            File inFile = new File(srcFile);
            if(!inFile.exists()) {
               throw new FileNotFoundException("The file " + inFile + " could not be found");
            } else {
               File destinationPath = new File(destPath);
               if(!destinationPath.exists()) {
                  destinationPath.mkdirs();
               }

               FileInputStream in = null;
               ArchiveInputStream arcIn = null;

               try {
                  in = new FileInputStream(inFile);
                  arcIn = (new ArchiveStreamFactory()).createArchiveInputStream("zip", in);
                  ZipArchiveEntry e = null;
                  HashMap realFolderNameMap = new HashMap();

                  while((e = (ZipArchiveEntry)arcIn.getNextEntry()) != null) {
                     if(!onlyManifest || e.getName().toUpperCase().equals("META-INF/") || e.getName().toUpperCase().equals("META-INF/MANIFEST.MF")) {
                        storeZipArchiveEntry(e, destPath, realFolderNameMap, arcIn);
                     }
                  }
               } catch (ArchiveException var20) {
                  throw new IOException("Error at creation of archive input stream\n" + Util.exceptionToString(var20));
               } finally {
                  if(arcIn != null) {
                     try {
                        arcIn.close();
                     } catch (IOException var19) {
                        ;
                     }
                  }

                  if(in != null) {
                     try {
                        in.close();
                     } catch (IOException var18) {
                        ;
                     }
                  }

               }

            }
         } else {
            throw new IllegalArgumentException("Parameter destPath is null or empty");
         }
      } else {
         throw new IllegalArgumentException("Parameter srcFile is null or empty");
      }
   }

   private static void storeZipArchiveEntry(ZipArchiveEntry entry, String destPath, Map realFolderNameMap, ArchiveInputStream arcIn) throws IOException {
      FileOutputStream out = null;

      try {
         if(entry.isDirectory()) {
            (new File(destPath, entry.getName())).mkdirs();
            realFolderNameMap.put(entry.getName().toLowerCase(), entry.getName());
         } else {
            String realEntryName;
            if(entry.getName().contains("/")) {
               String destFile = entry.getName().substring(0, entry.getName().lastIndexOf("/") + 1);
               if(realFolderNameMap.containsKey(destFile.toLowerCase())) {
                  realEntryName = (String)realFolderNameMap.get(destFile.toLowerCase()) + entry.getName().substring(entry.getName().lastIndexOf("/") + 1);
               } else {
                  realEntryName = entry.getName();
               }
            } else {
               realEntryName = entry.getName();
            }

            File destFile1 = new File(destPath, realEntryName);
            destFile1.getParentFile().mkdirs();
            destFile1.createNewFile();
            out = new FileOutputStream(destFile1);
            IOUtils.copy(arcIn, out);
         }
      } finally {
         if(out != null) {
            try {
               out.close();
            } catch (IOException var13) {
               ;
            }
         }

      }

   }

   public static void unzip(byte[] srcData, String destPath) throws IOException {
      if(srcData == null) {
         throw new IllegalArgumentException("Parameter srcData is null or empty");
      } else if(destPath != null && !destPath.equals("")) {
         File destinationPath = new File(destPath);
         if(!destinationPath.exists()) {
            destinationPath.mkdirs();
         }

         ByteArrayInputStream in = null;
         ArchiveInputStream arcIn = null;

         try {
            in = new ByteArrayInputStream(srcData);
            arcIn = (new ArchiveStreamFactory()).createArchiveInputStream("zip", in);
            ZipArchiveEntry e = null;
            HashMap realFolderNameMap = new HashMap();

            while((e = (ZipArchiveEntry)arcIn.getNextEntry()) != null) {
               storeZipArchiveEntry(e, destPath, realFolderNameMap, arcIn);
            }
         } catch (ArchiveException var18) {
            throw new IOException("Error at creation of archive input stream\n" + Util.exceptionToString(var18));
         } finally {
            if(arcIn != null) {
               try {
                  arcIn.close();
               } catch (IOException var17) {
                  ;
               }
            }

            if(in != null) {
               try {
                  in.close();
               } catch (IOException var16) {
                  ;
               }
            }

         }

      } else {
         throw new IllegalArgumentException("Parameter destPath is null or empty");
      }
   }

   public static List getRootFilesFromZip(byte[] srcData) throws IOException {
      if(srcData == null) {
         throw new IllegalArgumentException("Parameter srcData is null or empty");
      } else {
         ByteArrayInputStream in = null;
         ArchiveInputStream arcIn = null;

         ArrayList entryName1;
         try {
            in = new ByteArrayInputStream(srcData);
            arcIn = (new ArchiveStreamFactory()).createArchiveInputStream("zip", in);
            ArrayList e = new ArrayList();
            ZipArchiveEntry entry = null;

            while((entry = (ZipArchiveEntry)arcIn.getNextEntry()) != null) {
               String entryName = entry.getName();
               if(!entryName.contains("/")) {
                  e.add(entryName);
               }
            }

            entryName1 = e;
         } catch (ArchiveException var17) {
            throw new IOException("Error at creation of archive input stream\n" + Util.exceptionToString(var17));
         } finally {
            if(arcIn != null) {
               try {
                  arcIn.close();
               } catch (IOException var16) {
                  ;
               }
            }

            if(in != null) {
               try {
                  in.close();
               } catch (IOException var15) {
                  ;
               }
            }

         }

         return entryName1;
      }
   }

   public static void zip(String zipName, File srcDir, String dirPrefixRemove) throws IOException, FileNotFoundException {
      if(Util.stringNullOrEmpty(zipName)) {
         throw new IllegalArgumentException("Parameter zipName is null or empty");
      } else if(Util.stringNullOrEmpty(dirPrefixRemove)) {
         throw new IllegalArgumentException("Parameter dirPrefixRemove is null or empty");
      } else if(srcDir == null) {
         throw new NullPointerException("Parameter srcDir is null");
      } else if(!srcDir.exists()) {
         throw new FileNotFoundException("Source path " + srcDir.getPath() + " not found");
      } else {
         FileOutputStream outStr = null;
         ZipArchiveOutputStream zOut = null;

         try {
            outStr = new FileOutputStream(zipName);
            zOut = new ZipArchiveOutputStream(outStr);
            List files = FileSystemUtil.getFileListing(srcDir);

            for(int i = 0; i < files.size(); ++i) {
               FileInputStream inStr = null;

               try {
                  inStr = new FileInputStream(((File)files.get(i)).getPath());
                  String entryName = ((File)files.get(i)).getPath().replace(dirPrefixRemove, "");
                  entryName = FileSystemUtil.seperatorsToSlash(entryName);
                  ZipArchiveEntry ze = new ZipArchiveEntry(entryName);
                  ze.setTime(((File)files.get(i)).lastModified());
                  zOut.putArchiveEntry(ze);
                  IOUtils.copy(inStr, zOut);
                  zOut.closeArchiveEntry();
               } finally {
                  if(inStr != null) {
                     try {
                        inStr.close();
                     } catch (IOException var32) {
                        ;
                     }
                  }

               }
            }

            zOut.flush();
            outStr.flush();
         } finally {
            if(zOut != null) {
               try {
                  zOut.close();
               } catch (IOException var31) {
                  ;
               }
            }

            if(outStr != null) {
               try {
                  outStr.close();
               } catch (IOException var30) {
                  ;
               }
            }

         }
      }
   }

   public static byte[] zip(File srcDir, String dirPrefixRemove) throws IOException, FileNotFoundException {
      if(Util.stringNullOrEmpty(dirPrefixRemove)) {
         throw new IllegalArgumentException("Parameter dirPrefixRemove is null or empty");
      } else if(srcDir == null) {
         throw new NullPointerException("Parameter srcDir is null");
      } else if(!srcDir.exists()) {
         throw new FileNotFoundException("Source path " + srcDir.getPath() + " not found");
      } else {
         ByteArrayOutputStream outStr = null;
         ZipArchiveOutputStream zOut = null;

         byte[] var34;
         try {
            outStr = new ByteArrayOutputStream();
            zOut = new ZipArchiveOutputStream(outStr);
            List files = FileSystemUtil.getFileListing(srcDir);

            for(int i = 0; i < files.size(); ++i) {
               FileInputStream fIn = null;

               try {
                  fIn = new FileInputStream(((File)files.get(i)).getPath());
                  String entryName = ((File)files.get(i)).getPath().replace(dirPrefixRemove, "");
                  entryName = FileSystemUtil.seperatorsToSlash(entryName);
                  ZipArchiveEntry ze = new ZipArchiveEntry(entryName);
                  ze.setTime(((File)files.get(i)).lastModified());
                  zOut.putArchiveEntry(ze);
                  IOUtils.copy(fIn, zOut);
                  zOut.closeArchiveEntry();
               } finally {
                  if(fIn != null) {
                     try {
                        fIn.close();
                     } catch (IOException var31) {
                        ;
                     }
                  }

               }
            }

            zOut.flush();
            zOut.finish();
            outStr.flush();
            var34 = outStr.toByteArray();
         } finally {
            if(zOut != null) {
               try {
                  zOut.close();
               } catch (IOException var30) {
                  ;
               }
            }

            if(outStr != null) {
               try {
                  outStr.close();
               } catch (IOException var29) {
                  ;
               }
            }

         }

         return var34;
      }
   }

   public static void zip(File srcDir, String dirPrefixRemove, OutputStream outStr) throws IOException, FileNotFoundException {
      if(Util.stringNullOrEmpty(dirPrefixRemove)) {
         throw new IllegalArgumentException("Parameter dirPrefixRemove is null or empty");
      } else if(srcDir == null) {
         throw new NullPointerException("Parameter srcDir is null");
      } else if(!srcDir.exists()) {
         throw new FileNotFoundException("Source path " + srcDir.getPath() + " not found");
      } else if(outStr == null) {
         throw new NullPointerException("Parameter outStr is null");
      } else {
         ZipArchiveOutputStream zOut = null;

         try {
            zOut = new ZipArchiveOutputStream(outStr);
            List files = FileSystemUtil.getFileListing(srcDir);

            for(int i = 0; i < files.size(); ++i) {
               FileInputStream fIn = null;

               try {
                  fIn = new FileInputStream(((File)files.get(i)).getPath());
                  String entryName = ((File)files.get(i)).getPath().replace(dirPrefixRemove, "");
                  entryName = FileSystemUtil.seperatorsToSlash(entryName);
                  ZipArchiveEntry ze = new ZipArchiveEntry(entryName);
                  ze.setTime(((File)files.get(i)).lastModified());
                  zOut.putArchiveEntry(ze);
                  IOUtils.copy(fIn, zOut);
                  zOut.closeArchiveEntry();
               } finally {
                  if(fIn != null) {
                     try {
                        fIn.close();
                     } catch (IOException var27) {
                        ;
                     }
                  }

               }
            }

            zOut.flush();
            zOut.finish();
            outStr.flush();
         } finally {
            if(zOut != null) {
               try {
                  zOut.close();
               } catch (IOException var26) {
                  ;
               }
            }

         }

      }
   }
}