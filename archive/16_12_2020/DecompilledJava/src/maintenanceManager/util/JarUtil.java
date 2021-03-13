package de.mpdv.maintenanceManager.util;

import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class JarUtil {

   public static void createJar(String jarName, File srcDir, String dirPrefixRemove) throws IOException, FileNotFoundException {
      if(Util.stringNullOrEmpty(jarName)) {
         throw new IllegalArgumentException("Parameter jarName is null or empty");
      } else if(Util.stringNullOrEmpty(dirPrefixRemove)) {
         throw new IllegalArgumentException("Parameter dirPrefixRemove is null or empty");
      } else if(srcDir == null) {
         throw new NullPointerException("Parameter srcDir is null");
      } else if(!srcDir.exists()) {
         throw new FileNotFoundException("Source path " + srcDir.getPath() + " not found");
      } else {
         FileOutputStream outStr = null;
         JarArchiveOutputStream jout = null;

         try {
            outStr = new FileOutputStream(jarName);
            jout = new JarArchiveOutputStream(outStr);
            JarArchiveEntry manifestFile = new JarArchiveEntry("META-INF/MANIFEST.MF");
            jout.putArchiveEntry(manifestFile);
            OutputStreamWriter outW = new OutputStreamWriter(jout);
            outW.write("Manifest-Version: 1.0\n");
            outW.write("Created-By: MPDV Maintenance Manager\n");
            outW.flush();
            jout.closeArchiveEntry();
            List files = FileSystemUtil.getFileListing(srcDir);

            for(int i = 0; i < files.size(); ++i) {
               FileInputStream fIn = null;

               try {
                  fIn = new FileInputStream(((File)files.get(i)).getPath());
                  String entryName = ((File)files.get(i)).getPath().replace(dirPrefixRemove, "");
                  entryName = FileSystemUtil.seperatorsToSlash(entryName);
                  if(entryName.equals("META-INF/MANIFEST.MF")) {
                     fIn.close();
                  } else {
                     JarArchiveEntry je = new JarArchiveEntry(entryName);
                     jout.putArchiveEntry(je);
                     IOUtils.copy(fIn, jout);
                     jout.closeArchiveEntry();
                  }
               } finally {
                  if(fIn != null) {
                     try {
                        fIn.close();
                     } catch (IOException var36) {
                        ;
                     }
                  }

               }
            }

            jout.flush();
            outStr.flush();
         } finally {
            if(jout != null) {
               try {
                  jout.close();
               } catch (IOException var35) {
                  ;
               }
            }

            if(outStr != null) {
               try {
                  outStr.close();
               } catch (IOException var34) {
                  ;
               }
            }

         }
      }
   }

   public static String getImplementationVersionFromJarContainingClass(Class clazz) {
      File jarFile = null;

      try {
         jarFile = getJarFileContainingClass(clazz);
      } catch (Exception var5) {
         var5.printStackTrace();
         return null;
      }

      if(jarFile != null && jarFile.exists()) {
         Properties p = null;

         try {
            p = readManifestFromJar(jarFile);
         } catch (Exception var4) {
            var4.printStackTrace();
            return null;
         }

         if(p != null && p.containsKey("Implementation-Version")) {
            String version = p.getProperty("Implementation-Version");
            if(version != null) {
               version = version.replace("svn-rev.", "");
            }

            return version;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static File getJarFileContainingClass(Class clazz) {
      URL location = clazz.getResource('/' + clazz.getName().replace(".", "/") + ".class");
      String jarPath = location.getPath();
      return new File(jarPath.substring(5, jarPath.lastIndexOf("!")).replace("%20", " "));
   }

   public static Properties readManifestFromJar(File jarFile) throws IOException {
      FileInputStream fIn = null;
      ZipArchiveInputStream zipIn = null;

      Properties var5;
      try {
         fIn = new FileInputStream(jarFile);
         zipIn = new ZipArchiveInputStream(fIn);
         ZipArchiveEntry entry = null;

         Properties p;
         do {
            if((entry = (ZipArchiveEntry)zipIn.getNextEntry()) == null) {
               p = null;
               return p;
            }
         } while(!entry.getName().equals("META-INF/MANIFEST.MF") || entry.isDirectory());

         p = new Properties();
         p.load(zipIn);
         var5 = p;
      } finally {
         if(zipIn != null) {
            try {
               zipIn.close();
            } catch (IOException var17) {
               ;
            }
         }

         if(fIn != null) {
            try {
               fIn.close();
            } catch (IOException var16) {
               ;
            }
         }

      }

      return var5;
   }
}