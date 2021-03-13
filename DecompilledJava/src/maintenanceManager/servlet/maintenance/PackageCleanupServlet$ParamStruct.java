package de.mpdv.maintenanceManager.servlet.maintenance;

import java.io.File;
import java.util.Calendar;

class PackageCleanupServlet$ParamStruct {

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