package de.mpdv.maintenanceManager.servlet.maintenance;

import de.mpdv.maintenanceManager.servlet.maintenance.PackageCleanupServlet;
import java.io.File;
import java.util.List;
import java.util.Map;

class PackageCleanupServlet$1 implements Runnable {

   // $FF: synthetic field
   final List val$fileList;
   // $FF: synthetic field
   final Map val$sizeMap;
   // $FF: synthetic field
   final int val$sizeId;
   // $FF: synthetic field
   final PackageCleanupServlet this$0;


   PackageCleanupServlet$1(PackageCleanupServlet var1, List var2, Map var3, int var4) {
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