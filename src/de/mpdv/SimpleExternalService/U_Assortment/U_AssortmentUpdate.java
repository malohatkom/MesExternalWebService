package de.mpdv.SimpleExternalService.U_Assortment;

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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class U_AssortmentUpdate implements ISimpleExternalService 
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
        this.logger = loggerProvider.fetchLogger(U_AssortmentUpdate.class);
        this.debugEnabled = this.logger.isDebugEnabled();
        
        if (debugMode) write("U_AssortmentUpdate", "U_AssortmentUpdate");
        try
        {
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            con = connProv.fetchDbConnection();
            stmt = con.createStatement();
            String sql = "Select COLUMN_NAME, DATA_TYPE From INFORMATION_SCHEMA.COLUMNS Where TABLE_NAME = 'u_assortment'";
            Map<String, String> colType = new HashMap<String, String>();
                
            ResultSet DataSet = stmt.executeQuery(sql);
            if (DataSet != null) {
                try {
                    while(DataSet.next()) colType.put(DataSet.getString("COLUMN_NAME"), DataSet.getString("DATA_TYPE"));
                    DataSet.close();
                }
                catch (Exception e) {
                    this.logger.error("Could not close DataSet", e);
                } 
            }

            if (debugMode)
            {
                String s = "";
                for (String key: request.getSpecialParamMap().keySet())
                {
                    s += " " + key + " = " + (request.getSpecialParam(key) != null ? request.getSpecialParam(key).toString() : "null");
                }
                write("U_AssortmentUpdate", s);
            }
            StringBuilder newval = new StringBuilder();
            String id = "";
            for (String key: request.getSpecialParamMap().keySet())
            {
                if ("Assortment.AST_WHEEL_KOD".equals(key))
                    id = request.getSpecialParam(key).getValue() != null ? request.getSpecialParam(key).getValue().toString() : "null";
                else
                {
                    if (newval.length() != 0) 
                    {
                        newval.append(", ");
                    } 
                    newval.append(String.format("%s = %s", key.replace("Assortment.", ""), GetValStr(request.getSpecialParam(key), colType)));
                }
            }
            
            if (debugMode) write("U_AssortmentUpdate", " newval.length() = " + Integer.toString(newval.length()) + " id " + id);
            if (newval.length() != 0 && !"".equals(id)) 
            {
                sql = "Update u_assortment Set " + newval.toString() + String.format(", bearb = '%s', bearb_date = Format(CURRENT_TIMESTAMP, 'yyyy-MM-dd 00:00:00.000'), bearb_time = DATEDIFF(SECOND, 0,CONVERT(time, CURRENT_TIMESTAMP)) Where ast_wheel_kod = N'%s'", request.getUserId(), id);
                if (debugMode) write("U_AssortmentUpdate", sql);
                int i = stmt.executeUpdate(sql);
                if (debugMode) write("U_AssortmentUpdate", "rows updated " + i);

                if (i == 0)
                {
                    StringBuilder fld = new StringBuilder();
                    StringBuilder val = new StringBuilder();
                    for (String key: colType.keySet())
                    {
                        if (!key.contains("bearb") && !"AST_ID".equals(key))
                        {
                            if (fld.length() > 0 && val.length() > 0); 
                            {
                                fld.append(", ");
                                val.append(", ");
                            } 
                    
                            if (!"".equals(key.trim()))
                            {
                                fld.append(key.replace("Assortment.", ""));
                                val.append(GetValStr(request.getSpecialParam("Assortment." + key), colType));
                            }
                        }
                    }
            
                    if (debugMode) write("U_AssortmentUpdate", " fld.length() = " + Integer.toString(fld.length()) + " val.length() " + Integer.toString(val.length()) + " id " + id);
                    if (fld.length() != 0 && val.length() != 0 && !"".equals(id)) 
                    {
                        sql = 
                            " If (Not Exists(Select * From u_assortment Where ast_wheel_kod = N'"+id+"')) " + 
                            " Begin " +
                            "   Insert Into " +
                            "       u_assortment " +
                            "       ( " +
                            "           bearb, " +
                            "           bearb_date, " +
                            "           bearb_time " + 
                                        fld.toString() + 
                            "       ) " +
                            "   Values " +
                            "       ( " +
                            "           '"+request.getUserId()+"', " +
                            "           Format(CURRENT_TIMESTAMP, 'yyyy-MM-dd 00:00:00.000'), " +
                            "           DATEDIFF(SECOND, 0,CONVERT(time, CURRENT_TIMESTAMP)) " + 
                                        val.toString() + 
                            "       ) " +
                            " End ".replace("(, ", "");
                        if (debugMode) write("U_AssortmentUpdate", sql);
                        i = stmt.executeUpdate(sql);
                        if (debugMode) write("U_AssortmentUpdate", "rows inserted " + i);
                    }
                }
               
                IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
                builder.addCol("Assortment.AST_WHEEL_KOD", DataType.STRING);
                if (i > 0)
                {
                    builder.addRow();
                    builder.value(id);
                    res = new SesResultBuilder().addDataTable(builder.build()).build();
                }
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(U_AssortmentUpdate.class.getName()).log(Level.SEVERE, null, ex);
            if (debugMode) write("U_AssortmentUpdate", ex.getMessage());
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

    private String GetValStr(SpecialParam sParam, Map<String, String> cTypes) {
        String res = "null ";
        try
        {
            if (sParam != null)
            {
                Object v = sParam.getValue();
                if (v != null ? !"".equals(v) : false)
                {
                    String t = cTypes.get(sParam.getAcronym().replace("Assortment.", ""));
                    //if (debugMode) write("U_AssortmentUpdate", sParam.getAcronym().replace("Assortment.", "") + " : " + t);
                    if ("int".equals(t)) res = String.format("%s ", sParam.getValue());
                    if ("nvarchar".equals(t)) res = String.format("N'%s' ", sParam.getValue());
                    if ("decimal".equals(t)) res = String.format("%s ", sParam.getValue().toString()).replace(",", ".");
                    if ("datetime".equals(t)) 
                    {
                        res = String.format("'%s'", new SimpleDateFormat("yyyy-MM-dd 00:00:00.0").format(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(sParam.getValue().toString())));
                    }
                }
            }
        }
        catch (Exception e)
        {
            if (debugMode) write("U_AssortmentUpdate", "GetValStr error : " + e.getMessage());
        }
        return res;
    }
}
