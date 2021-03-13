package de.mpdv.maintenanceManager.data;

import de.mpdv.maintenanceManager.util.DateTimeUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

public class Configuration {

   private static final String CONFIG_DIR = "MaintenanceManager";
   private static Configuration config = null;
   private String jHydraDir;
   private String tempDir;
   private String tomcatDir;
   private String baseUpdateDir;
   private String baseRuntimeDir;
   private String tomcatHostPort;
   private int tomcatVersion = 6;


   public Configuration(String jHydraDir, String tempDir, String tomcatDir, String baseUpdateDir, String baseRuntimeDir, String tomcatHostPort, int tomcatVersion) {
      this.jHydraDir = jHydraDir;
      this.tempDir = tempDir;
      this.tomcatDir = tomcatDir;
      this.baseUpdateDir = baseUpdateDir;
      this.baseRuntimeDir = baseRuntimeDir;
      this.tomcatHostPort = tomcatHostPort;
      this.tomcatVersion = tomcatVersion;
   }

   public int getTomcatVersion() {
      return this.tomcatVersion;
   }

   public void setTomcatVersion(int tomcatVersion) {
      this.tomcatVersion = tomcatVersion;
   }

   public String getjHydraDir() {
      return this.jHydraDir;
   }

   public void setjHydraDir(String jHydraDir) {
      this.jHydraDir = jHydraDir;
   }

   public String getTempDir() {
      return this.tempDir;
   }

   public void setTempDir(String tempDir) {
      this.tempDir = tempDir;
   }

   public String getTomcatDir() {
      return this.tomcatDir;
   }

   public String getTomcatHostPort() {
      return this.tomcatHostPort;
   }

   public void setTomcatDir(String tomcatDir) {
      this.tomcatDir = tomcatDir;
   }

   public void setTomcatHostPort(String tomcatHostPort) {
      this.tomcatHostPort = tomcatHostPort;
   }

   public String getBaseUpdateDir() {
      return this.baseUpdateDir;
   }

   public String getBaseRuntimeDir() {
      return this.baseRuntimeDir;
   }

   public void setBaseUpdateDir(String baseUpdateDir) {
      this.baseUpdateDir = baseUpdateDir;
   }

   public void setBaseRuntimeDir(String baseRuntimeDir) {
      this.baseRuntimeDir = baseRuntimeDir;
   }

   public String getUpdateDirClient() {
      return this.baseUpdateDir + File.separator + "client";
   }

   public String getRuntimeDirClient() {
      return this.baseRuntimeDir + File.separator + "client";
   }

   public String getUpdateDirServer() {
      return this.baseUpdateDir + File.separator + "server";
   }

   public String getRuntimeDirServer() {
      return this.baseRuntimeDir + File.separator + "server";
   }

   public String toString() {
      return "Configuration [jHydraDir=" + this.jHydraDir + ", tempDir=" + this.tempDir + ", tomcatDir=" + this.tomcatDir + ", baseUpdateDir=" + this.baseUpdateDir + ", baseRuntimeDir=" + this.baseRuntimeDir + ", tomcatHostPort=" + this.tomcatHostPort + "]";
   }

   public static Configuration getConfiguration() {
      if(config == null) {
         config = loadConfiguration();
      }

      return config;
   }

   private static Configuration loadConfiguration() {
      Configuration conf = new Configuration(getJHydraDir().getAbsolutePath(), (String)null, (String)null, (String)null, (String)null, (String)null, 6);
      File configDir = getConfigDir();
      Properties p = new Properties();
      FileInputStream stream = null;

      label71: {
         Configuration var5;
         try {
            stream = new FileInputStream(new File(configDir, "config.properties"));
            p.load(stream);
            break label71;
         } catch (IOException var15) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - IO exception occured. Use empty configuration.");
            setMaintMgrDirs(conf);
            var5 = conf;
         } finally {
            if(stream != null) {
               try {
                  stream.close();
               } catch (IOException var14) {
                  ;
               }
            }

         }

         return var5;
      }

      conf.tomcatDir = p.getProperty("tomcat.dir");
      conf.tomcatHostPort = p.getProperty("tomcat.host_port");
      conf.tomcatVersion = Integer.parseInt(p.getProperty("tomcat.version", "6"));
      setMaintMgrDirs(conf);
      return conf;
   }

   public static void setMaintMgrDirs(Configuration conf) {
      File configDir = getConfigDir();
      File tempDir = new File(configDir, "temp");
      tempDir.mkdirs();
      conf.tempDir = tempDir.getAbsolutePath();
      File updDir = new File(configDir, "upd");
      updDir.mkdirs();
      conf.baseUpdateDir = updDir.getAbsolutePath();
      File rtDir = new File(configDir, "rt");
      rtDir.mkdirs();
      conf.baseRuntimeDir = rtDir.getAbsolutePath();
      (new File(updDir, "client")).mkdirs();
      (new File(rtDir, "client")).mkdirs();
      (new File(updDir, "server")).mkdirs();
      (new File(rtDir, "server")).mkdirs();
   }

   private static File getConfigDir() {
      File jHydraDir = getJHydraDir();
      File configDir = new File(jHydraDir, "MaintenanceManager");
      if(!configDir.exists()) {
         configDir.mkdirs();
      }

      if(!configDir.exists()) {
         throw new RuntimeException("The configuration dir for maintenance manager does not exist and could not be created: " + configDir.getAbsolutePath());
      } else {
         return configDir;
      }
   }

   private static File getJHydraDir() {
      String dir = System.getenv("JHYDRADIR");
      if(dir != null && dir.length() != 0) {
         File jHydraDir = new File(dir);
         if(!jHydraDir.exists()) {
            jHydraDir.mkdirs();
         }

         if(!jHydraDir.exists()) {
            throw new RuntimeException("Configured JHYDRADIR does not exist and could not be created: " + dir);
         } else {
            return jHydraDir;
         }
      } else {
         throw new RuntimeException("Environment variable JHYDRADIR not set");
      }
   }

   public static File getMM2ConfigFile() {
      return new File(getJHydraDir(), "MaintenanceManager" + File.separator + "config.json");
   }

   public static void saveConfiguration(Configuration conf) throws IOException {
      config = conf;
      Properties p = new Properties();
      if(config.tomcatDir != null) {
         p.setProperty("tomcat.dir", config.tomcatDir);
      }

      if(config.tomcatHostPort != null) {
         p.setProperty("tomcat.host_port", config.tomcatHostPort);
      }

      p.setProperty("tomcat.version", String.valueOf(config.tomcatVersion));
      File configDir = getConfigDir();
      FileOutputStream stream = null;

      try {
         stream = new FileOutputStream(new File(configDir, "config.properties"));
         p.store(stream, (String)null);
         stream.flush();
      } finally {
         if(stream != null) {
            try {
               stream.close();
            } catch (IOException var10) {
               ;
            }
         }

      }

   }

}