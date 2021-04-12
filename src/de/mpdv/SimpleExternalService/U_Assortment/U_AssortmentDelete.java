package de.mpdv.SimpleExternalService.U_Assortment;

import de.mpdv.sdi.data.SesContext;
import de.mpdv.sdi.data.SesRequest;
import de.mpdv.sdi.data.SesResult;
import de.mpdv.sdi.simpleExternalService.ISimpleExternalService;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class U_AssortmentDelete implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private boolean debugEnabled = true;
    private final boolean debugMode = true;

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
    
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory) {
        SesResult res = null;
        Connection con = null;
        Statement stmt = null;

        ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        this.logger = loggerProvider.fetchLogger(U_AssortmentDelete.class);
        this.debugEnabled = this.logger.isDebugEnabled();
        
        if (debugMode) write("U_AssortmentDelete", "U_AssortmentDelete");
        try
        {
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            con = connProv.fetchDbConnection();
            stmt = con.createStatement();
            if (debugMode)
            {
                String s = "";
                for (String key: request.getSpecialParamMap().keySet())
                {
                    s += " " + key + " = " + (request.getSpecialParam(key) != null ? request.getSpecialParam(key).toString() : "null");
                }
                write("U_AssortmentDelete", s);
            }
            String id = "";
            for (String key: request.getSpecialParamMap().keySet())
            {
                if ("Assortment.AST_WHEEL_KOD".equals(key))
                    id = request.getSpecialParam(key).getValue() != null ? request.getSpecialParam(key).getValue().toString() : "null";
            }
            
            if (debugMode) write("U_AssortmentDelete", " id " + id);
            if (!"".equals(id)) 
            {
                String sql = String.format("Delete u_assortment Where ast_wheel_kod = N'%s'", id);
                if (debugMode) write("U_AssortmentDelete", sql);
                int i = stmt.executeUpdate(sql);
                if (debugMode) write("U_AssortmentDelete", "rows deleted " + i);
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(U_AssortmentDelete.class.getName()).log(Level.SEVERE, null, ex);
            if (debugMode) write("U_AssortmentDelete", ex.getMessage());
        }        
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (Exception e) {
                    this.logger.error("Could not close statement", e);
                } 
            }
            if (con != null) {
                try {
                    con.close();
                }
                catch (Exception e) {
                    this.logger.error("Could not close connection", e);
                } 
            }
        }
        return res;
    }
}