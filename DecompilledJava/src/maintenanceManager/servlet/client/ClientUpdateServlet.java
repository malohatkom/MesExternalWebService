package de.mpdv.maintenanceManager.servlet.client;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.servlet.client.ClientUpdateServlet.ParamStruct;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.FileSystemUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class ClientUpdateServlet extends HttpServlet {

   private static final long serialVersionUID = -2678243403051750983L;
   private static final String ACTION_UPDATE = "UPDATE";
   private static final String ACTION_UPDATES_AVAILABLE = "UPDATES_AVAILABLE";


   class ParamStruct {

   File rtDir;
   File tempDir;
}
   
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      Configuration config = null;

      try {
         config = Configuration.getConfiguration();
      } catch (Exception var19) {
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Client update: Could not get configuration: " + var19.getMessage());
         return;
      }

      String action = request.getParameter("action");
      String application = request.getParameter("application");
      String includeDocStr = request.getParameter("includeDocumentation");
      boolean includeDoc;
      if(!"MOC".equals(application)) {
         includeDoc = true;
      } else if("1".equals(includeDocStr)) {
         includeDoc = true;
      } else {
         includeDoc = false;
      }

      String lastUpdateTsStr = request.getParameter("lastupdatets");
      System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Client update action: " + action);
      System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Client update lastupdatets: " + lastUpdateTsStr);
      System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Client update application: " + application);
      System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Client update include documentation from call: " + includeDocStr + " results in include documentation flag: " + includeDoc);
      Calendar lastUpdateTs = null;

      try {
         lastUpdateTs = DateTimeUtil.isoStringToCalendarTimeStampUtc(lastUpdateTsStr);
      } catch (Exception var18) {
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Client update: Could not parse last update ts string " + var18.getMessage());
         return;
      }

      ParamStruct struct = null;

      try {
         struct = this.checkParams(config, action, application);
      } catch (Exception var17) {
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + var17.getMessage());
         return;
      }

      File sourceDir;
      if(action.equals("UPDATE")) {
         sourceDir = new File(struct.rtDir, application);
         if(!sourceDir.exists()) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Client update: No files for update found");
            return;
         }

         if(FileSystemUtil.getFileListing(sourceDir).size() == 0) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Client update: No files for update found");
            return;
         }

         if(lastUpdateTs == null) {
            try {
               this.returnFullUpdate(response, sourceDir, includeDoc);
            } catch (Exception var16) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Client update: Error writing update zip to response: " + var16.getMessage());
               return;
            }
         } else {
            try {
               this.returnOnlyNewerFiles(response, sourceDir, lastUpdateTs, struct.tempDir, includeDoc);
            } catch (Exception var15) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Client update: Error writing update zip to response: " + var15.getMessage());
               return;
            }
         }
      } else if(action.equals("UPDATES_AVAILABLE")) {
         sourceDir = new File(struct.rtDir, application);
         if(!sourceDir.exists()) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Client update: No files for update found");
            return;
         }

         if(FileSystemUtil.getFileListing(sourceDir).size() == 0) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Client update: No files for update found");
            return;
         }

         try {
            List e = this.getFilesToUse(sourceDir, lastUpdateTs, includeDoc);
            PrintWriter out;
            if(e == null) {
               response.setContentType("text/html");
               out = response.getWriter();
               out.write("0");
            } else {
               response.setContentType("text/html");
               out = response.getWriter();
               out.write("" + e.size());
            }
         } catch (Exception var14) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Client update: Error getting files to use / available updates: " + var14.getMessage());
            return;
         }
      }

   }

   private void writeDataZippedToResponse(List sourceFiles, File srcFolder, HttpServletResponse response) throws IOException {
      response.setContentType("application/octet-stream");
      response.setHeader("Content-Disposition", "attachment; filename=\"Clientupdate.zip\"");
      ServletOutputStream outStr = null;
      ZipArchiveOutputStream zOut = null;

      try {
         outStr = response.getOutputStream();
         zOut = new ZipArchiveOutputStream(outStr);

         for(int i = 0; i < sourceFiles.size(); ++i) {
            FileInputStream fIn = null;

            try {
               fIn = new FileInputStream(((File)sourceFiles.get(i)).getPath());
               String entryName = ((File)sourceFiles.get(i)).getPath().replace(srcFolder + File.separator, "");
               entryName = FileSystemUtil.seperatorsToSlash(entryName);
               ZipArchiveEntry ze = new ZipArchiveEntry(entryName);
               ze.setTime(((File)sourceFiles.get(i)).lastModified());
               zOut.putArchiveEntry(ze);
               IOUtils.copy(fIn, zOut);
               zOut.closeArchiveEntry();
            } finally {
               if(fIn != null) {
                  try {
                     fIn.close();
                  } catch (IOException var32) {
                     ;
                  }
               }

            }
         }

         zOut.flush();
         zOut.finish();
         outStr.flush();
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished writing response");
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

   private List getFilesToUse(File sourceDir, Calendar lastUpdateTs, boolean includeDoc) throws FileNotFoundException {
      System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Get files to use for last update ts " + DateTimeUtil.calendarToPrintString(lastUpdateTs));
      List filesToUse = null;
      if(lastUpdateTs == null) {
         filesToUse = FileSystemUtil.getFileListing(sourceDir);
      } else {
         long filesBefore = lastUpdateTs.getTimeInMillis();
         List entryName = FileSystemUtil.getFileListing(sourceDir);

         for(int i1 = entryName.size() - 1; i1 >= 0; --i1) {
            File f = (File)entryName.get(i1);
            if(f.lastModified() <= filesBefore) {
               entryName.remove(i1);
            }
         }

         filesToUse = entryName;
      }

      if(!includeDoc) {
         int var10 = filesToUse.size();

         for(int i = filesToUse.size() - 1; i >= 0; --i) {
            String var11 = ((File)filesToUse.get(i)).getPath().replace(sourceDir + File.separator, "");
            if(var11 != null && !"".equals(var11) && var11.startsWith("resources" + File.separator + "help")) {
               filesToUse.remove(i);
            }
         }

         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Help documents were not included (" + (var10 - filesToUse.size()) + " files)");
      }

      if(filesToUse.size() == 0) {
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Client update: No files for update found");
      } else {
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Found " + filesToUse.size() + " newer files for application");
      }

      return filesToUse;
   }

   private void returnFullUpdate(HttpServletResponse response, File sourceDir, boolean includeDoc) throws IOException {
      List files = this.getFilesToUse(sourceDir, (Calendar)null, includeDoc);
      this.writeDataZippedToResponse(files, sourceDir, response);
   }

   private void returnOnlyNewerFiles(HttpServletResponse response, File sourceDir, Calendar lastUpdateTs, File tempDirGlobal, boolean includeDoc) throws FileNotFoundException, IOException {
      List rtFiles = this.getFilesToUse(sourceDir, lastUpdateTs, includeDoc);
      if(rtFiles.size() != 0) {
         this.writeDataZippedToResponse(rtFiles, sourceDir, response);
      }
   }

   private ParamStruct checkParams(Configuration config, String action, String application) {
      if(config == null) {
         throw new IllegalArgumentException("Maint Mgr - Client update: Could not get configuration");
      } else if(action != null && !action.equals("")) {
         if(!action.equals("UPDATE") && !action.equals("UPDATES_AVAILABLE")) {
            throw new IllegalArgumentException("Maint Mgr - Client update: Unknown action specified: " + action + ". Allowed actions are: " + "UPDATE" + ", " + "UPDATES_AVAILABLE");
         } else {
            ParamStruct struct = new ParamStruct();
            if(action.equals("UPDATE") || action.equals("UPDATES_AVAILABLE")) {
               String rtDirPath = config.getRuntimeDirClient();
               if(rtDirPath == null || rtDirPath.equals("")) {
                  throw new IllegalStateException("Maint Mgr - Client update: Runtime dir is not configured");
               }

               File rtDir = new File(rtDirPath);
               if(!rtDir.exists()) {
                  throw new IllegalStateException("Maint Mgr - Client update: Runtime dir does not exist: " + rtDir.getAbsolutePath());
               }

               struct.rtDir = rtDir;
               String tempDirPath = config.getTempDir();
               if(tempDirPath == null || tempDirPath.equals("")) {
                  throw new IllegalStateException("Maint Mgr - Client update: Temp dir is not configured");
               }

               File tempDir = new File(tempDirPath);
               if(!tempDir.exists()) {
                  throw new IllegalStateException("Maint Mgr - Client update: Temp dir does not exist: " + tempDir.getAbsolutePath());
               }

               struct.tempDir = tempDir;
            }

            if(application != null && !application.equals("")) {
               return struct;
            } else {
               throw new IllegalArgumentException("Maint Mgr - Client update: No applicaton specified");
            }
         }
      } else {
         throw new IllegalArgumentException("Maint Mgr - Client update: No action specified");
      }
   }
}