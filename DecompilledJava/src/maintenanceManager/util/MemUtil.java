package de.mpdv.maintenanceManager.util;

import de.mpdv.maintenanceManager.util.DateTimeUtil;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.Calendar;
import java.util.Iterator;

public class MemUtil {

   public static boolean isEnoughPermGenMemoryAvailable(long warFileSize) {
      int major = Integer.parseInt(System.getProperty("java.version").split("\\.")[1]);
      if(major <= 7) {
         Iterator i$ = ManagementFactory.getMemoryPoolMXBeans().iterator();

         MemoryPoolMXBean item;
         do {
            if(!i$.hasNext()) {
               throw new IllegalStateException("Could not determine the permgen memory bean");
            }

            item = (MemoryPoolMXBean)i$.next();
         } while(!item.getName().toLowerCase().contains("perm gen"));

         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - War file size: " + warFileSize + " max perm gen space: " + item.getPeakUsage().getMax() + " used perm gen space: " + item.getPeakUsage().getUsed());
         long freePermGen = item.getPeakUsage().getMax() - item.getPeakUsage().getUsed();
         if((double)freePermGen <= (double)warFileSize * 1.1D) {
            return false;
         } else {
            return true;
         }
      } else {
         return true;
      }
   }
}