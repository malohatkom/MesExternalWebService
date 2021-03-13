package de.mpdv.test;

import de.mpdv.maintenanceManager.util.ZipUtil;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ZipTest {

   public static void main(String[] args) throws FileNotFoundException, IOException {
      ZipUtil.unzip("d:\\temp\\2012-07-25_#81025-client.upd", "d:\\temp\\test", false);
   }
}