/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package u_shiftorders;

import de.mpdv.customization.userExit.IUserExitParam;
import de.mpdv.sdi.data.SesException;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.data.ue.BapiInterpreterUeContext;
import de.mpdv.sdi.data.ue.SdiAfterPerformActionParam;
import de.mpdv.sdi.systemutility.IDbConnectionProvider;
import de.mpdv.sdi.systemutility.ISdiLogger;
import de.mpdv.sdi.systemutility.ISdiLoggerProvider;
import de.mpdv.sdi.systemutility.ISystemUtilFactory;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author mikhail.malokhatko
 */
public class UShiftordersDelete {
    private final boolean debugPrint = true;
    private final String ComputerName = System.getenv("COMPUTERNAME");
    private final boolean debugMode = "ETN-SRV-MES2".equals(ComputerName);
    
    private static void write(String FileName, String text) {
        String fileName = "C:\\Windows\\Temp\\" + FileName + ".log";
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
            out.println(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()) + " " + text);
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }         
    }    
  
    public void sdiAfterPerformAction(IUserExitParam param) {
        String s = "";
        if (debugPrint)
        {
            for (String key: param.keys()) s += " " + key + " = " + ((Object)param.get(key)).toString();
            write("UShiftordersDelete", s);
        }
        SdiAfterPerformActionParam aParam = (SdiAfterPerformActionParam)param.get("param");
        BapiInterpreterUeContext uContext = (BapiInterpreterUeContext)param.get("context");
        if (debugPrint)
        {
            for (String key: aParam.getSpecialParameters().keySet()) s += " " + key + " = " + aParam.getSpecialParameters().get(key).getValue().toString();
            write("UShiftordersDelete", s);
        }
        
        ISystemUtilFactory factory = (ISystemUtilFactory)param.get("factory");
        ISdiLoggerProvider logerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        ISdiLogger logger = logerProvider.fetchLogger(UShiftordersDelete.class);
        logger.debug("Testing UShiftordersDelete");
        SpecialParam shiftorder_key = aParam.getSpecialParameters().get("shiftorder.key");
        
        if (debugPrint) write("UShiftordersDelete", "shiftorder_key = " + shiftorder_key);
        
        if (shiftorder_key != null ? !"".equals(shiftorder_key.getValue()) : false)
        {
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            Connection con;
            Statement stmt;
            
            try
            {
                con = connProv.fetchDbConnection();
                stmt = con.createStatement();
                
                String sql = String.format(" Delete u_shiftoperations Where shiftorder_id = %s", shiftorder_key != null ? shiftorder_key.getValue() : "-1");
                if (debugPrint) write("UShiftordersDelete", sql);
                int cnt = stmt.executeUpdate(sql);
                if (debugPrint) write("UShiftordersDelete", String.format("cnt = %s", cnt));    
                
                stmt.close();
                con.close();
            }
            catch (SQLException e) {
                if (debugPrint) write("UShiftordersDelete", "error = " + e.getMessage());
                throw new SesException("lkDbError", "Error at DB access: " + e.getMessage(), e, new String[0]);
            } 
            catch (NumberFormatException e) {
                if (debugPrint) write("UShiftordersDelete", "error = " + e.getMessage());
                throw new SesException("lkDbError", "Error at DB access: " + e.getMessage(), e, new String[0]);
            }
        }
    }
}