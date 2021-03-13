package de.mpdv.maintenanceManager.servlet.systemReset;

import java.io.File;

public class SysResetCleanUpServlet$ParamStruct {

   File jHydraDir;
   File tempDir;
   String tomcatHostPort;
   File tomcatDir;
   File updDirClient;
   File updDirServer;
   File rtDirClient;
   File rtDirServer;


   public String toString() {
      return "ParamStruct [jHydraDir=" + this.jHydraDir + ", tempDir=" + this.tempDir + ", tomcatHostPort=" + this.tomcatHostPort + ", tomcatDir=" + this.tomcatDir + ", updDirClient=" + this.updDirClient + ", updDirServer=" + this.updDirServer + ", rtDirClient=" + this.rtDirClient + ", rtDirServer=" + this.rtDirServer + "]";
   }
}