package de.mpdv.SimpleExternalService;
import de.mpdv.sdi.data.DataType;
import de.mpdv.sdi.data.SesContext;
import de.mpdv.sdi.data.SesRequest;
import de.mpdv.sdi.data.SesResult;
import de.mpdv.sdi.data.SesResultBuilder;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.simpleExternalService.ISimpleExternalService;
import de.mpdv.sdi.systemutility.IDataTableBuilder;
import de.mpdv.sdi.systemutility.IDbConnectionProvider;
import de.mpdv.sdi.systemutility.ISdiLogger;
import de.mpdv.sdi.systemutility.ISdiLoggerProvider;
import de.mpdv.sdi.systemutility.ISystemUtilFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class U_AssortmentDelete implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private boolean debugEnabled = true;
    private final boolean debugMode = true;
 
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory) {
        SesResult res = null;
        Connection con = null;
        Statement stmt = null;

        ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        this.logger = loggerProvider.fetchLogger(U_AssortmentDelete.class);
        this.debugEnabled = this.logger.isDebugEnabled();
        
        if (debugMode) System.out.println("U_AssortmentDelete");
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
                System.out.println(s);
            }
            String id = "";
            for (String key: request.getSpecialParamMap().keySet())
            {
                if ("Assortment.AST_WHEEL_KOD".equals(key))
                    id = request.getSpecialParam(key).getValue() != null ? request.getSpecialParam(key).getValue().toString() : "null";
            }
            
            if (debugMode) System.out.println(" id " + id);
            if (!"".equals(id)) 
            {
                String sql = String.format("Delete u_assortment Where AST_WHEEL_KOD = N'%s'", id);
                if (debugMode) System.out.println(sql);
                int i = stmt.executeUpdate(sql);
                if (debugMode) System.out.println("rows deleted " + i);
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(U_ShiftOrdersList.class.getName()).log(Level.SEVERE, null, ex);
            if (debugMode) System.out.println(ex.getMessage());
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