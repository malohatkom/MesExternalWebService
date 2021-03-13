package de.mpdv.maintenanceManager.logic.javaServer;

import java.io.File;
import java.io.FilenameFilter;

class JavaPackageDeployment$1 implements FilenameFilter {

   // $FF: synthetic field
   final File val$destFile;


   JavaPackageDeployment$1(File var1) {
      this.val$destFile = var1;
   }

   public boolean accept(File dir, String name) {
      return name.equalsIgnoreCase(this.val$destFile.getName());
   }
}