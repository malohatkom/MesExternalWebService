package de.mpdv.maintenanceManager.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbUtil {

   public static Connection getDbConnection(String dbUser, String dbPass, String dbType, String dbConnString) throws ClassNotFoundException, SQLException {
      if(dbType.equals("ORACLE")) {
         Class.forName("oracle.jdbc.driver.OracleDriver");
         return DriverManager.getConnection(dbConnString, dbUser, dbPass);
      } else if(dbType.equals("SQLSERVER")) {
         Class.forName("net.sourceforge.jtds.jdbc.Driver");
         return DriverManager.getConnection(dbConnString, dbUser, dbPass);
      } else {
         throw new IllegalArgumentException("Unknown DB type " + dbType);
      }
   }
}