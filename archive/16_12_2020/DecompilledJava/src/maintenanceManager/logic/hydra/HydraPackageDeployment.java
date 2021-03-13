package de.mpdv.maintenanceManager.logic.hydra;

import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.ServiceInputSpecialParam;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

public class HydraPackageDeployment {

   public static String deployPackage(File hyInstFile, HttpServletRequest request, boolean putDataInSvcCall, File jHydraDir) throws IOException {
      File jHydraDirMoc = new File(jHydraDir, "MOC");
      String filename = "hyinst-" + UUID.randomUUID().toString() + ".zip";

      String e;
      try {
         LinkedList columnList = new LinkedList();
         columnList.add("system.hydra_install_log");
         LinkedList specialParams = new LinkedList();
         if(putDataInSvcCall) {
            byte[] logBuilder = FileSystemUtil.fileToByteArray(hyInstFile);
            specialParams.add(new ServiceInputSpecialParam("system.hydra_install_data", "EQUAL", logBuilder));
         } else {
            (new File(jHydraDirMoc, filename)).getParentFile().mkdirs();
            hyInstFile.renameTo(new File(jHydraDirMoc, filename));
            specialParams.add(new ServiceInputSpecialParam("system.hydra_install_filename", "EQUAL", filename));
         }

         StringBuilder logBuilder1 = new StringBuilder();
         IDataTable installTable = SessionManager.callWebService(request, "System.hydraInstall", columnList, (List)null, specialParams, (Map)null, Integer.valueOf(1));
         String hydraInstallLog = null;
         if(installTable.getRowCount() == 1 && installTable.probeColIdx("system.hydra_install_log") != -1) {
            hydraInstallLog = (String)installTable.getCellValue(0, "system.hydra_install_log", String.class);
            logBuilder1.append(hydraInstallLog);
            logBuilder1.append("\n\n\n");
         }

         File maintMgrDir = new File(jHydraDir, "MaintenanceManager");
         File protDir = new File(maintMgrDir, "logs/hydra");
         protDir.mkdirs();
         String name = DateTimeUtil.calendarUtcToIsoString(DateTimeUtil.getCurrentUtcCalendar()) + "-DEPLOY-" + hyInstFile.getName() + ".txt";
         String content = "Deployed package " + hyInstFile.getName() + " at " + DateTimeUtil.calendarUtcToPrintString(DateTimeUtil.getCurrentUtcCalendar()) + " (UTC)\n";
         content = content + "\n" + logBuilder1.toString();

         try {
            FileSystemUtil.writeTextFile(protDir, name, content);
         } catch (Exception var20) {
            ;
         }

         e = logBuilder1.toString();
      } finally {
         if((new File(jHydraDirMoc, filename)).exists()) {
            (new File(jHydraDirMoc, filename)).delete();
         }

      }

      return e;
   }
}