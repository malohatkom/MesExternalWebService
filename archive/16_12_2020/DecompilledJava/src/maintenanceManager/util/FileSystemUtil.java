package de.mpdv.maintenanceManager.util;

import de.mpdv.maintenanceManager.util.Util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class FileSystemUtil {

   public static boolean deleteDir(File dir) {
      if(dir == null) {
         throw new NullPointerException("Parameter dir is null");
      } else if(!dir.exists()) {
         return true;
      } else {
         if(dir.isDirectory()) {
            String[] children = dir.list();

            for(int i = 0; i < children.length; ++i) {
               boolean success = deleteDir(new File(dir, children[i]));
               if(!success) {
                  return false;
               }
            }
         }

         return dir.delete();
      }
   }

   public static List getFileListing(File dir) throws FileNotFoundException {
      if(dir == null) {
         throw new NullPointerException("Parameter dir is null");
      } else if(!dir.exists()) {
         return new ArrayList();
      } else {
         ArrayList result = new ArrayList();
         File[] filesAndDirs = dir.listFiles();
         List filesDirs = Arrays.asList(filesAndDirs);
         int fileCount = filesDirs.size();

         for(int i = 0; i < fileCount; ++i) {
            File file = (File)filesDirs.get(i);
            if(file.isFile()) {
               result.add(file);
            }

            if(!file.isFile()) {
               List deeperList = getFileListing(file);
               result.addAll(deeperList);
            }
         }

         Collections.sort(result);
         return result;
      }
   }

   public static void copyFile(File src, File dest, boolean overWrite, boolean setFileChangeDateNow) throws FileNotFoundException, IOException {
      if(src == null) {
         throw new NullPointerException("Parameter src is null");
      } else if(dest == null) {
         throw new NullPointerException("Parameter dest is null");
      } else if(!src.exists()) {
         throw new FileNotFoundException("The file " + src.getPath() + " does not exist");
      } else {
         FileInputStream inStr = null;
         BufferedInputStream in = null;
         FileOutputStream outStr = null;
         BufferedOutputStream out = null;

         try {
            dest.getParentFile().mkdirs();
            inStr = new FileInputStream(src);
            in = new BufferedInputStream(inStr);
            outStr = new FileOutputStream(dest, !overWrite);
            out = new BufferedOutputStream(outStr);
            byte[] tmp = new byte[16384];
            boolean bytes = false;

            while(true) {
               int bytes1;
               if((bytes1 = in.read(tmp)) < 0) {
                  out.flush();
                  break;
               }

               if(bytes1 > 0) {
                  out.write(tmp, 0, bytes1);
               }
            }
         } finally {
            if(in != null) {
               try {
                  in.close();
               } catch (IOException var25) {
                  ;
               }
            }

            if(inStr != null) {
               try {
                  inStr.close();
               } catch (IOException var24) {
                  ;
               }
            }

            if(out != null) {
               try {
                  out.close();
               } catch (IOException var23) {
                  ;
               }
            }

            if(outStr != null) {
               try {
                  outStr.close();
               } catch (IOException var22) {
                  ;
               }
            }

         }

         if(setFileChangeDateNow) {
            dest.setLastModified(Calendar.getInstance().getTimeInMillis());
         } else {
            dest.setLastModified(src.lastModified());
         }

      }
   }

   public static void copyFile(File src, File dest, boolean overWrite) throws FileNotFoundException, IOException {
      copyFile(src, dest, overWrite, false);
   }

   public static void writeTextFile(File folder, String name, String content) throws IOException {
      FileWriter fWriter = null;

      try {
         fWriter = new FileWriter(new File(folder, name));
         fWriter.write(content);
         fWriter.flush();
      } finally {
         if(fWriter != null) {
            try {
               fWriter.close();
            } catch (IOException var10) {
               ;
            }
         }

      }

   }

   public static void copyDir(File src, File dest, boolean overWrite) throws FileNotFoundException, IOException {
      copyDir(src, dest, overWrite, false);
   }

   public static void copyDir(File src, File dest, boolean overWrite, boolean setFileChangeDateNow) throws FileNotFoundException, IOException {
      if(src == null) {
         throw new NullPointerException("Parameter src is null");
      } else if(dest == null) {
         throw new NullPointerException("Parameter dest is null");
      } else if(src.exists()) {
         if(!dest.exists()) {
            dest.mkdirs();
         }

         File[] files = src.listFiles();
         File[] arr$ = files;
         int len$ = files.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            File file = arr$[i$];
            if(file.isDirectory()) {
               (new File(dest.getAbsolutePath() + File.separator + file.getName())).mkdirs();
               copyDir(file, new File(dest.getAbsolutePath() + File.separator + file.getName()), overWrite, setFileChangeDateNow);
            } else {
               copyFile(file, new File(dest.getAbsolutePath() + File.separator + file.getName()), overWrite, setFileChangeDateNow);
            }
         }

      }
   }

   public static byte[] fileToByteArray(File inFile) throws IOException {
      byte[] data = new byte[(int)inFile.length()];
      FileInputStream fIn = null;

      try {
         fIn = new FileInputStream(inFile);
         int offset = 0;

         int numRead1;
         for(boolean numRead = false; offset < data.length && (numRead1 = fIn.read(data, offset, data.length - offset)) >= 0; offset += numRead1) {
            ;
         }
      } finally {
         try {
            if(fIn != null) {
               fIn.close();
            }
         } catch (IOException var11) {
            ;
         }

      }

      return data;
   }

   public static byte[] inStreamToByteArray(InputStream inStr) throws IOException {
      ByteArrayOutputStream outStr = null;

      byte[] var4;
      try {
         outStr = new ByteArrayOutputStream();
         byte[] buffer = new byte['\u8000'];
         boolean bytes = false;

         int bytes1;
         while((bytes1 = inStr.read(buffer)) >= 0) {
            if(bytes1 > 0) {
               outStr.write(buffer, 0, bytes1);
            }
         }

         outStr.flush();
         var4 = outStr.toByteArray();
      } finally {
         if(outStr != null) {
            try {
               outStr.close();
            } catch (Exception var11) {
               ;
            }
         }

      }

      return var4;
   }

   public static void inStreamToFile(InputStream inStr, File destFile) throws IOException {
      FileOutputStream outStr = null;

      try {
         outStr = new FileOutputStream(destFile);
         byte[] buffer = new byte['\u8000'];
         boolean bytes = false;

         int bytes1;
         while((bytes1 = inStr.read(buffer)) >= 0) {
            if(bytes1 > 0) {
               outStr.write(buffer, 0, bytes1);
            }
         }

         outStr.flush();
      } finally {
         if(outStr != null) {
            try {
               outStr.close();
            } catch (Exception var11) {
               ;
            }
         }

      }

   }

   public static String seperatorsToSlash(String path) {
      return Util.stringNullOrEmpty(path)?path:path.replace("\\", "/");
   }
}