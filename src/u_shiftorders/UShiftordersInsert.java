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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mikhail.malokhatko
 */
public class UShiftordersInsert {
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
            write("UShiftordersInsert", s);
        }
        SdiAfterPerformActionParam aParam = (SdiAfterPerformActionParam)param.get("param");
        BapiInterpreterUeContext uContext = (BapiInterpreterUeContext)param.get("context");
        if (debugPrint)
        {
            for (String key: aParam.getSpecialParameters().keySet()) s += " " + key + " = " + aParam.getSpecialParameters().get(key).getValue().toString();
            write("UShiftordersInsert", s);
        }
        
        ISystemUtilFactory factory = (ISystemUtilFactory)param.get("factory");
        ISdiLoggerProvider logerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        ISdiLogger logger = logerProvider.fetchLogger(UShiftordersInsert.class);
        logger.debug("Testing UShiftordersInsert");
        String shiftorder_key = aParam.getCreatedSerial().toString();
        SpecialParam shiftorder_copy_all_data = aParam.getSpecialParameters().get("shiftorder.copy_all_data");
        SpecialParam shiftorder_old_key = aParam.getSpecialParameters().get("shiftorder.old_key");
        
        if (debugPrint) write("UShiftordersInsert", "shiftorder_key = " + shiftorder_key);
        
        if (shiftorder_copy_all_data != null && shiftorder_old_key != null && !"".equals(shiftorder_key) ? !"".equals(shiftorder_copy_all_data.getValue()) && !"".equals(shiftorder_old_key.getValue()) : false)
        {
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            Connection con;
            Statement stmt;
            
            try
            {
                con = connProv.fetchDbConnection();
                stmt = con.createStatement();
                
                String sql = String.format(" Select * From u_shiftoperations Where shiftorder_id = %s", shiftorder_old_key != null ? shiftorder_old_key.getValue() : "-1");
                if (debugPrint) write("UShiftordersInsert", sql);
                ResultSet DataSet = stmt.executeQuery(sql);
                List<Map<String, Map<String, String>>> Data = new ArrayList<Map<String, Map<String, String>>> ();
                while (DataSet.next())
                {
                    Map<String, Map<String, String>> columns = new HashMap<String, Map<String, String>>();
                    ResultSetMetaData md = DataSet.getMetaData();
                    for (int cInd = 1; cInd <= md.getColumnCount(); cInd++)
                    {
                        Map<String, String> colData = new HashMap<String, String>();
                        colData.put("ColumnTypeName", md.getColumnTypeName(cInd));
                        colData.put("ColumnValue", "shiftorder_id".equals(md.getColumnName(cInd)) ? shiftorder_key : DataSet.getString(cInd));
                        columns.put(md.getColumnName(cInd), colData);
                    }
                    Data.add(columns);
                }
                DataSet.close();
                        
                if (!Data.isEmpty())
                {
                    for (Map<String, Map<String, String>> row : Data)
                    {
                        String fields = "";
                        String values = "";
                        for (String key : row.keySet())
                        {
                            if (!key.toLowerCase().contains("bearb"))
                            {
                                fields = fields + ", " + key;
                                values = values + ", " + ("id".equals(key.toLowerCase()) ? " (Select Max(id) + 1 From u_shiftoperations)  " : GetValStr(row, key));
                            }
                        }
                         
                        sql = 
                        " Set IDENTITY_INSERT u_shiftoperations ON " +
                        " Insert Into " +
                        "   u_shiftoperations " +
                        "   ( " +
                        "           bearb, " +
                        "           bearb_date, " +
                        "           bearb_time" + 
                                    fields + 
                        "   ) " +
                        "   Values " +
                        "   ( " +
                        "       '" + uContext.getUserId() + "', " +
                        "       Format(CURRENT_TIMESTAMP, 'yyyy-MM-dd 00:00:00.000'), " +
                        "       DATEDIFF(SECOND, 0,CONVERT(time, CURRENT_TIMESTAMP)) " + 
                                values + 
                        "   ) " +
                        " Set IDENTITY_INSERT u_shiftoperations OFF ";
                        sql = sql.replace("(, ", "");
                        
                        if (debugPrint) write("UShiftordersInsert", sql);    
                        
                        int cnt = stmt.executeUpdate(sql);
                        if (debugPrint) write("UShiftordersInsert", String.format("cnt = %s", cnt));    
                    }
                }
                
                stmt.close();
                con.close();
            }
            catch (SQLException e) {
                if (debugPrint) write("UShiftordersInsert", "error = " + e.getMessage());
                throw new SesException("lkDbError", "Error at DB access: " + e.getMessage(), e, new String[0]);
            } 
            catch (NumberFormatException e) {
                if (debugPrint) write("UShiftordersInsert", "error = " + e.getMessage());
                throw new SesException("lkDbError", "Error at DB access: " + e.getMessage(), e, new String[0]);
            }
        }
    }

    private String GetValStr(Map<String, Map<String, String>> row, String key) {
        String res = "null ";
        try
        {
            if (!"".equals(key))
            {
                String v = row.get(key).get("ColumnValue");
                if (!"".equals(v))
                {
                    String t = row.get(key).get("ColumnTypeName");
                    if ("int".equals(t)) res = String.format("%s ", v);
                    if ("nvarchar".equals(t)) res = !"null".equals(v) ? String.format("N'%s' ", v) : v;
                    if ("decimal".equals(t)) res = String.format("%s ", v).replace(",", ".");
                    if ("datetime".equals(t)) 
                    {
                        res = String.format("'%s'", new SimpleDateFormat("yyyy-MM-dd 00:00:00.0").format(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(v)));
                    }
                }
                if ("id".equals(key.toLowerCase())) res = "0";
            }
        }
        catch (Exception e)
        {
            if (debugMode) write("U_AssortmentInsert", "GetValStr error : " + e.getMessage());
        }
        return res.replace("N'null'", "null");
    }
}