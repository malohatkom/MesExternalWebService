package de.mpdv.maintenanceManager.servlet.maintenance;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.data.client.ClientPackageMeta;
import de.mpdv.maintenanceManager.data.javaServer.PackageMeta;
import de.mpdv.maintenanceManager.gui.CommonResponseFrame;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.servlet.maintenance.PackageCleanupServlet.s;
import de.mpdv.maintenanceManager.servlet.maintenance.PackageCleanupServlet.DiskSize;
import de.mpdv.maintenanceManager.servlet.maintenance.PackageCleanupServlet.ParamStruct;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class PackageCleanupServlet extends HttpServlet {

    
class DiskSize {

   long clientBackup;
   long clientUpdate;
   long javaBackup;
   long javaUpdate;


   public String toString() {
      return "DiskSize [clientBackup=" + this.clientBackup + ", clientUpdate=" + this.clientUpdate + ", javaBackup=" + this.javaBackup + ", javaUpdate=" + this.javaUpdate + "]";
   }
}    


class ParamStruct {

   String dateStr;
   Calendar date;
   boolean clientBackup;
   boolean clientUpdate;
   boolean javaBackup;
   boolean javaUpdate;
   boolean includeUndeployed;
   File updateDirJava;
   File updateDirClient;


   public String toString() {
      return "ParamStruct [dateStr=" + this.dateStr + ", date=" + this.date.getTime() + ", clientBackup=" + this.clientBackup + ", clientUpdate=" + this.clientUpdate + ", javaBackup=" + this.javaBackup + ", javaUpdate=" + this.javaUpdate + ", includeUndeployed=" + this.includeUndeployed + ", updateDirJava=" + this.updateDirJava + ", updateDirClient=" + this.updateDirClient + "]";
   }
}
    


class s implements Runnable {

   // $FF: synthetic field
   final List val$fileList;
   // $FF: synthetic field
   final Map val$sizeMap;
   // $FF: synthetic field
   final int val$sizeId;
   // $FF: synthetic field
   final PackageCleanupServlet this$0;


   s(PackageCleanupServlet var1, List var2, Map var3, int var4) {
      this.this$0 = var1;
      this.val$fileList = var2;
      this.val$sizeMap = var3;
      this.val$sizeId = var4;
   }

   public void run() {
      long delSize = 0L;

      for(int i = this.val$fileList.size() - 1; i >= 0; --i) {
         File f = (File)this.val$fileList.get(i);
         delSize += f.length();
         f.delete();
      }

      this.val$sizeMap.put(Integer.valueOf(this.val$sizeId), Long.valueOf(delSize));
   }
}


    
   private static final long serialVersionUID = 3913655023259615450L;
   private static final String ACT_CALC = "CALC";
   private static final String ACT_REMOVE = "REMOVE";


   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      if(SessionManager.checkLogin(request, response)) {
         File mm2ConfigFile = Configuration.getMM2ConfigFile();
         if(mm2ConfigFile.exists()) {
            CommonResponseFrame.printToResponse("Maintananace Manager 2.0 is installed! Please use the new version!", response);
         } else {
            Configuration config = null;

            try {
               config = Configuration.getConfiguration();
            } catch (Exception var20) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var20));
               CommonResponseFrame.printToResponse(this.getPageEntryResponse((ParamStruct)null, "Could not load application for clean up because: Could not get configuration: " + var20.getMessage(), (String)null, (DiskSize)null), response);
               return;
            }

            String action = request.getParameter("action");
            if(action != null && !action.equals("")) {
               String dateStr = request.getParameter("date");
               String cliBackupStr = request.getParameter("clientBackups");
               String cliUpdStr = request.getParameter("clientUpdates");
               String javaBackupStr = request.getParameter("javaBackups");
               String javaUpdStr = request.getParameter("javaUpdates");
               String includeUndeployedStr = request.getParameter("includeUndeployed");
               ParamStruct struct = null;

               try {
                  struct = this.checkParams(config, action, dateStr, cliBackupStr, cliUpdStr, javaBackupStr, javaUpdStr, includeUndeployedStr);
               } catch (Exception var19) {
                  struct = new ParamStruct();
                  struct.dateStr = dateStr;
                  struct.clientBackup = "true".equals(cliBackupStr);
                  struct.clientUpdate = "true".equals(cliUpdStr);
                  struct.javaBackup = "true".equals(javaBackupStr);
                  struct.javaUpdate = "true".equals(javaUpdStr);
                  struct.includeUndeployed = "true".equals(includeUndeployedStr);
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var19));
                  CommonResponseFrame.printToResponse(this.getPageEntryResponse(struct, "Could not execute the cleanup application with action " + action + " because: " + var19.getMessage(), (String)null, (DiskSize)null), response);
                  return;
               }

               if(action.equals("CALC")) {
                  try {
                     DiskSize e1 = this.calcDiskUsage(struct);
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Calculated disk size of client/java updates " + e1);
                     CommonResponseFrame.printToResponse(this.getPageEntryResponse((ParamStruct)null, (String)null, "Successfully calculated disk size", e1), response);
                  } catch (Exception var18) {
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var18));
                     CommonResponseFrame.printToResponse(this.getPageEntryResponse(struct, "Could not calculate disk usage because: " + var18.getMessage(), (String)null, (DiskSize)null), response);
                  }
               } else if(action.equals("REMOVE")) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Start data cleanup for the following parameters: " + struct);

                  try {
                     LinkedList e = new LinkedList();
                     LinkedList clientPackList = new LinkedList();
                     if(struct.javaBackup || struct.javaUpdate) {
                        this.getJavaPacksToRemove(struct, e);
                     }

                     if(struct.clientBackup || struct.clientUpdate) {
                        this.getClientPacksToRemove(struct, clientPackList);
                     }

                     if(e.size() == 0 && clientPackList.size() == 0) {
                        DiskSize delSize1 = this.calcDiskUsage(struct);
                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished clean up (nothing to do - no packages selected) - Disk usage after clean up: " + delSize1);
                        CommonResponseFrame.printToResponse(this.getPageEntryResponse((ParamStruct)null, (String)null, "Clean up completed successfully. 0 GB of data have been removed.", delSize1), response);
                     } else {
                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Found the following packages for clean up - Client: " + clientPackList + " Java: " + e);
                        long delSize = this.removeData(struct, e, clientPackList);
                        DiskSize s = this.calcDiskUsage(struct);
                        System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished clean up - Deleted " + this.toGbStr(delSize) + " GB of data - Disk usage after clean up: " + s);
                        CommonResponseFrame.printToResponse(this.getPageEntryResponse((ParamStruct)null, (String)null, "Clean up completed successfully. " + this.toGbStr(delSize) + " GB of data have been removed.", s), response);
                     }

                  } catch (Exception var21) {
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var21));
                     CommonResponseFrame.printToResponse(this.getPageEntryResponse(struct, "Could not complete clean up (perhaps already partial done) because: " + var21.getMessage(), (String)null, (DiskSize)null), response);
                  }
               } else {
                  CommonResponseFrame.printToResponse(this.getPageEntryResponse((ParamStruct)null, (String)null, (String)null, (DiskSize)null), response);
               }
            } else {
               CommonResponseFrame.printToResponse(this.getPageEntryResponse((ParamStruct)null, (String)null, (String)null, (DiskSize)null), response);
            }
         }
      }
   }

   private long removeData(ParamStruct struct, List javaPackages, List clientPackages) throws FileNotFoundException {
      long delSize = 0L;

      int i;
      String pack;
      for(i = 0; i < javaPackages.size(); ++i) {
         pack = (String)javaPackages.get(i);
         if(struct.javaBackup) {
            delSize += this.delJavaBackup(pack, struct.updateDirJava);
         }

         if(struct.javaUpdate) {
            delSize += this.delJavaUpdate(pack, struct.updateDirJava);
         }
      }

      for(i = 0; i < clientPackages.size(); ++i) {
         pack = (String)clientPackages.get(i);
         if(struct.clientBackup) {
            delSize += this.delClientBackup(pack, struct.updateDirClient);
         }

         if(struct.clientUpdate) {
            delSize += this.delClientUpdate(pack, struct.updateDirClient);
         }
      }

      return delSize;
   }

   private long delListOfFiles(List delList) {
      LinkedList folders = new LinkedList();

      for(int subLists = delList.size() - 1; subLists >= 0; --subLists) {
         File subListSize = (File)delList.get(subLists);
         if(!subListSize.isFile()) {
            folders.add(0, subListSize);
            delList.remove(subListSize);
         }
      }

      LinkedList var10 = new LinkedList();
      int var11 = delList.size() / 10;
      if(var11 == 0) {
         var10.add(delList);
      } else {
         for(int sizeMap = 0; sizeMap < 10; ++sizeMap) {
            if(sizeMap == 9) {
               var10.add(delList.subList(0 + sizeMap * var11, delList.size()));
            } else {
               var10.add(delList.subList(0 + sizeMap * var11, 0 + (sizeMap + 1) * var11));
            }
         }
      }

      ConcurrentHashMap var12 = new ConcurrentHashMap();

      int delSize;
      for(delSize = 0; delSize < var10.size(); ++delSize) {
         List it = (List)var10.get(delSize);
         (new Thread(new s(this, it, var12, delSize))).start();
      }

      while(var12.size() != var10.size()) {
         try {
            Thread.sleep(100L);
         } catch (InterruptedException var9) {
            ;
         }
      }

      for(delSize = folders.size() - 1; delSize >= 0; --delSize) {
         File f = (File)folders.get(delSize);
         f.delete();
      }

      long var13 = 0L;

      for(Iterator var14 = var12.keySet().iterator(); var14.hasNext(); var13 += ((Long)var12.get(var14.next())).longValue()) {
         ;
      }

      return var13;
   }

   private long delClientBackup(String pack, File updDir) throws FileNotFoundException {
      File f1 = new File(updDir, "Backups/" + pack);
      File f2 = new File(updDir, "Backups/" + pack + "-SERVER");
      LinkedList delList = new LinkedList();
      delList.addAll(getFullFileListing(f1));
      delList.addAll(getFullFileListing(f2));
      return this.delListOfFiles(delList);
   }

   private long delClientUpdate(String pack, File updDir) throws FileNotFoundException {
      File f1 = new File(updDir, pack);
      List delList = getFullFileListing(f1);
      delList.remove(new File(f1, "clientPackageMeta.xml"));
      delList.remove(f1);
      return this.delListOfFiles(delList);
   }

   private long delJavaBackup(String pack, File updDir) throws FileNotFoundException {
      File f1 = new File(updDir, "Backups/" + pack);
      File f2 = new File(updDir, "Backups/" + pack + "-CLIENT");
      LinkedList delList = new LinkedList();
      delList.addAll(getFullFileListing(f1));
      delList.addAll(getFullFileListing(f2));
      return this.delListOfFiles(delList);
   }

   private long delJavaUpdate(String pack, File updDir) throws FileNotFoundException {
      File f1 = new File(updDir, pack);
      List delList = getFullFileListing(f1);
      delList.remove(new File(f1, "packageMeta.xml"));
      delList.remove(f1);
      return this.delListOfFiles(delList);
   }

   private static List getFullFileListing(File dir) throws FileNotFoundException {
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
               List deeperList = getFullFileListing(file);
               result.addAll(deeperList);
            }
         }

         result.add(dir);
         Collections.sort(result);
         return result;
      }
   }

   private void getJavaPacksToRemove(ParamStruct struct, List packList) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, ParseException {
      File[] packages = struct.updateDirJava.listFiles();
      int packCount = packages.length;

      for(int i = 0; i < packCount; ++i) {
         if(!packages[i].getName().equals("Backups") && packages[i].isDirectory()) {
            PackageMeta meta = PackageMeta.loadPackageMeta(packages[i].getAbsolutePath());
            if(meta.getDeploymentDate() != null) {
               if(meta.getDeploymentDate().compareTo(struct.date) < 0) {
                  packList.add(packages[i].getName());
               }
            } else if(struct.includeUndeployed && meta.getCreationDate().compareTo(struct.date) < 0) {
               packList.add(packages[i].getName());
            }
         }
      }

   }

   private void getClientPacksToRemove(ParamStruct struct, List packList) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, ParseException {
      File[] packages = struct.updateDirClient.listFiles();
      int packCount = packages.length;

      for(int i = 0; i < packCount; ++i) {
         if(!packages[i].getName().equals("Backups") && packages[i].isDirectory()) {
            ClientPackageMeta meta = ClientPackageMeta.loadPackageMeta(packages[i].getAbsolutePath());
            if(meta.getDeploymentDate() != null) {
               if(meta.getDeploymentDate().compareTo(struct.date) < 0) {
                  packList.add(packages[i].getName());
               }
            } else if(struct.includeUndeployed && meta.getCreationDate().compareTo(struct.date) < 0) {
               packList.add(packages[i].getName());
            }
         }
      }

   }

   private DiskSize calcDiskUsage(ParamStruct struct) throws FileNotFoundException {
      DiskSize s = new DiskSize();
      if(struct.updateDirClient.exists()) {
         this.getSizeFromUpdDir(struct.updateDirClient, s, "client");
      }

      if(struct.updateDirJava.exists()) {
         this.getSizeFromUpdDir(struct.updateDirJava, s, "java");
      }

      return s;
   }

   private void getSizeFromUpdDir(File updDir, DiskSize s, String type) throws FileNotFoundException {
      File backupDir = new File(updDir, "Backups");
      String backupDirPath = backupDir.getAbsolutePath();
      long size = 0L;
      long backupSize = 0L;
      List files = FileSystemUtil.getFileListing(updDir);

      for(int i = 0; i < files.size(); ++i) {
         File f = (File)files.get(i);
         if(f.isFile()) {
            if(f.getAbsolutePath().startsWith(backupDirPath)) {
               backupSize += f.length();
            } else {
               size += f.length();
            }
         }
      }

      if("java".equals(type)) {
         s.javaBackup = backupSize;
         s.javaUpdate = size;
      } else {
         s.clientBackup = backupSize;
         s.clientUpdate = size;
      }

   }

   private String getPageEntryResponse(ParamStruct struct, String errorMessage, String successMessage, DiskSize size) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Clean up</h1>\n");
      builder.append("<h2>Remove backup and update data</h2>\n");
      builder.append("<br />\n");
      builder.append("<script type=\"text/javascript\">\n");
      builder.append("function del()\n");
      builder.append("{\n");
      builder.append("document.getElementById(\"progress\").innerHTML=\"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Please wait, removing data ...<br />\";\n");
      builder.append("document.getElementById(\'Remove\').style.visibility=\"hidden\";\n");
      builder.append("document.getElementById(\'Calc\').style.visibility=\"hidden\";\n");
      builder.append("document.getElementById(\'action\').value=\"REMOVE\";\n");
      builder.append("document.getElementById(\"cleanupform\").submit();\n");
      builder.append("}\n");
      builder.append("\n");
      builder.append("function calc()\n");
      builder.append("{\n");
      builder.append("document.getElementById(\"progress\").innerHTML=\"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Please wait, calculating disc usage ...<br />\";\n");
      builder.append("document.getElementById(\'Remove\').style.visibility=\"hidden\";\n");
      builder.append("document.getElementById(\'Calc\').style.visibility=\"hidden\";\n");
      builder.append("document.getElementById(\'action\').value=\"CALC\";\n");
      builder.append("document.getElementById(\"cleanupform\").submit();\n");
      builder.append("}\n");
      builder.append("</script>\n");
      if(errorMessage != null) {
         builder.append("<span style=\"color:red;\">An error has occured: " + errorMessage + "</span><br /><br />\n");
      }

      if(successMessage != null) {
         builder.append("<span style=\"color:green;\">" + successMessage + "</span><br /><br />\n");
      }

      builder.append("<form action=\"PackageCleanup\" method=\"post\" id=\"cleanupform\" >\n");
      builder.append("<div style=\"width:100%;\">\n");
      builder.append("<div style=\"width:25%;float:left;text-align:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:29%;float:left;text-align:left;\"><b>Client data</b></div>\n");
      builder.append("<div style=\"width:4%;float:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:42%;float:left;text-align:left;\"><b>Total</b></div><br /><br />\n");
      builder.append("<div style=\"width:25%;float:left;text-align:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:29%;float:left;text-align:left;\"><input style=\"float:left;\" type=\"checkbox\" name=\"clientBackups\" value=\"true\" " + (struct != null?(struct.clientBackup?"checked=\"checked\"":""):"") + "/> Remove backups</div>\n");
      builder.append("<div style=\"width:4%;float:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:42%;float:left;text-align:left;\">" + (size != null?this.toGbStr(size.clientBackup):"???") + " GB</div><br /><br style=\"line-height:5px;\"/>\n");
      builder.append("<div style=\"width:25%;float:left;text-align:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:29%;float:left;text-align:left;\"><input style=\"float:left;\" type=\"checkbox\" name=\"clientUpdates\" value=\"true\" " + (struct != null?(struct.clientUpdate?"checked=\"checked\"":""):"") + "/> Remove update packages</div>\n");
      builder.append("<div style=\"width:4%;float:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:42%;float:left;text-align:left;\">" + (size != null?this.toGbStr(size.clientUpdate):"???") + " GB</div>\n");
      builder.append("<div style=\"width:100%;\">&nbsp;<br /><br /></div>\n");
      builder.append("<div style=\"width:25%;float:left;text-align:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:29%;float:left;text-align:left;\"><b>Server data</b></div>\n");
      builder.append("<div style=\"width:4%;float:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:42%;float:left;text-align:left;\"><b>Total</b></div><br /><br />\n");
      builder.append("<div style=\"width:25%;float:left;text-align:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:29%;float:left;text-align:left;\"><input style=\"float:left;\" type=\"checkbox\" name=\"javaBackups\" value=\"true\" " + (struct != null?(struct.javaBackup?"checked=\"checked\"":""):"") + "/> Remove backups</div>\n");
      builder.append("<div style=\"width:4%;float:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:42%;float:left;text-align:left;\">" + (size != null?this.toGbStr(size.javaBackup):"???") + " GB</div><br /><br style=\"line-height:5px;\"/>\n");
      builder.append("<div style=\"width:25%;float:left;text-align:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:29%;float:left;text-align:left;\"><input style=\"float:left;\" type=\"checkbox\" name=\"javaUpdates\" value=\"true\" " + (struct != null?(struct.javaUpdate?"checked=\"checked\"":""):"") + "/> Remove update packages</div>\n");
      builder.append("<div style=\"width:4%;float:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:42%;float:left;text-align:left;\">" + (size != null?this.toGbStr(size.javaUpdate):"???") + " GB</div>\n");
      builder.append("<div style=\"width:100%;\">&nbsp;<br /><br /><br /></div>\n");
      builder.append("<div style=\"width:25%;float:left;text-align:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;\">Remove data deployed before (MM/DD/YYYY):&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"text\" name=\"date\" " + (struct != null && struct.dateStr != null?"value=\"" + struct.dateStr + "\"":"") + "/></div><br /><br />\n");
      builder.append("<div style=\"width:25%;float:left;text-align:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;\"><input style=\"float:left;\" type=\"checkbox\" name=\"includeUndeployed\" value=\"true\" " + (struct != null?(struct.includeUndeployed?"checked=\"checked\"":""):"") + "/> Include data of undeployed packages (created before date)</div>\n");
      builder.append("<div style=\"width:100%;\">&nbsp;<br /><br /></div>\n");
      builder.append("</div>\n");
      builder.append("<input type=\"hidden\" name=\"action\" id=\"action\" value=\"\"/>\n");
      builder.append("<div style=\"width:47%;float:left;text-align:right;\"><input style=\"visibility:hidden;\" type=\"button\" name=\"Remove\" id=\"Remove\" value=\"Remove data\" onclick=\"del();\" /></div>\n");
      builder.append("<div style=\"width:6%;float:left;text-align:left;\">&nbsp;</div>\n");
      builder.append("<div style=\"width:47%;float:left;text-align:left;\"><input style=\"visibility:hidden;\" type=\"button\" name=\"Calc\" id=\"Calc\" value=\"Calculate disc usage\" onclick=\"calc();\" /></div>\n");
      builder.append("<span id=\"javascriptwarning\" style=\"color:red;\"><b>ATTENTION:<br /> Javascript is disabled in your browser, but you need javascript to use package deployment!</b></span>\n");
      builder.append("<span id=\"progress\" style=\"color:blue;\"></span>\n");
      builder.append("</form>\n");
      builder.append("</div>\n");
      builder.append("<script type=\"text/javascript\">\n");
      builder.append(" document.getElementById(\'Remove\').style.visibility=\"visible\";\n");
      builder.append(" document.getElementById(\'Calc\').style.visibility=\"visible\";\n");
      builder.append(" document.getElementById(\'javascriptwarning\').style.visibility=\"hidden\";\n");
      builder.append(" document.getElementById(\'javascriptwarning\').innerHTML=\"\";\n");
      builder.append("</script>\n");
      return builder.toString();
   }

   private String toGbStr(long val) {
      double gbVal = (double)val / 1.073741824E9D;
      NumberFormat nf = NumberFormat.getInstance();
      nf.setMaximumFractionDigits(2);
      return nf.format(new BigDecimal(gbVal));
   }

   private ParamStruct checkParams(Configuration config, String action, String dateStr, String cliBackupStr, String cliUpdStr, String javaBackupStr, String javaUpdStr, String includeUndeployedStr) {
      if(action != null && !action.equals("")) {
         if(!action.equals("CALC") && !action.equals("REMOVE")) {
            throw new IllegalArgumentException("Unknown action specified: " + action + ". Allowed actions are: " + "CALC" + ", " + "REMOVE");
         } else {
            String updDirPath = config.getUpdateDirServer();
            if(updDirPath != null && !updDirPath.equals("")) {
               File updDir = new File(updDirPath);
               if(!updDir.exists()) {
                  throw new IllegalStateException("The JAVA update dir does not exist: " + updDir.getAbsolutePath());
               } else {
                  String clUpdDirPath = config.getUpdateDirClient();
                  if(clUpdDirPath != null && !clUpdDirPath.equals("")) {
                     File clUpdDir = new File(clUpdDirPath);
                     if(!clUpdDir.exists()) {
                        throw new IllegalStateException("The client update dir does not exist: " + clUpdDir.getAbsolutePath());
                     } else {
                        Calendar date = null;
                        boolean cliBackup = false;
                        boolean cliUpd = false;
                        boolean javaBackup = false;
                        boolean javaUpd = false;
                        boolean includeUndeployed = false;
                        if(action.equals("REMOVE")) {
                           cliBackup = "true".equals(cliBackupStr);
                           cliUpd = "true".equals(cliUpdStr);
                           javaBackup = "true".equals(javaBackupStr);
                           javaUpd = "true".equals(javaUpdStr);
                           includeUndeployed = "true".equals(includeUndeployedStr);
                           if(dateStr == null || "".equals(dateStr)) {
                              throw new IllegalStateException("Please specify a date for removal");
                           }

                           try {
                              date = DateTimeUtil.stringToCalendar(dateStr);
                           } catch (Exception var20) {
                              throw new IllegalStateException("Date for removal has wrong format: " + dateStr);
                           }
                        }

                        ParamStruct struct = new ParamStruct();
                        struct.dateStr = dateStr;
                        struct.updateDirClient = clUpdDir;
                        struct.updateDirJava = updDir;
                        struct.date = date;
                        struct.clientBackup = cliBackup;
                        struct.clientUpdate = cliUpd;
                        struct.javaBackup = javaBackup;
                        struct.javaUpdate = javaUpd;
                        struct.includeUndeployed = includeUndeployed;
                        return struct;
                     }
                  } else {
                     throw new IllegalStateException("The client update dir is not configured");
                  }
               }
            } else {
               throw new IllegalStateException("The JAVA update dir is not configured");
            }
         }
      } else {
         throw new IllegalArgumentException("The action is not specified");
      }
   }
}