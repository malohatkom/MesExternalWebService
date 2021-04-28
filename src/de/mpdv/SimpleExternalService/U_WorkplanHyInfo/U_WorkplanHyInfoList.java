/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mpdv.SimpleExternalService.U_WorkplanHyInfo;

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
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mikhail.malokhatko
 */
public class U_WorkplanHyInfoList implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private final boolean printDebug = true;
    //private final boolean debugMode = true;
    private final String className = "U_WorkplanHyInfoList"; 
 
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
        ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        this.logger = loggerProvider.fetchLogger(U_WorkplanHyInfoList.class);
        
        SpecialParam pWorkplanTechParamOrderID = null;
        String sWorkplanTechParamOrderID = "";
        try
        {
            pWorkplanTechParamOrderID = request.getSpecialParam("workplantechparam.order.id");
            if (printDebug) write(className, String.format("pWorkplanTechParamOrderID = %s", pWorkplanTechParamOrderID));
        }
        catch (Exception e)
        {
            pWorkplanTechParamOrderID = null;
            if (printDebug) write(className, String.format("Error: %s", e.getMessage()));
        }
        finally
        {
            sWorkplanTechParamOrderID = pWorkplanTechParamOrderID != null ? (pWorkplanTechParamOrderID.getValue() != null ? pWorkplanTechParamOrderID.getValue().toString().toUpperCase() : "") : "";        
        }
        
        if ("".equals(sWorkplanTechParamOrderID))
        {
            if (printDebug) write(className, "Parameter [workplantechparam.order.id] not found");
            return null;
        }
        
        IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
        Connection con = null;
        Statement stmt = null;
        List<Map<String, Object>> retData = new  ArrayList<Map<String, Object>>();
        try {
            con = connProv.fetchDbConnection();
            stmt = con.createStatement();
                
            String sql = 
                " Select " +
                "   id, " +
                "   auftrag_nr, " +
                "   agnr, " +
                "   operation, " +
                "   f_order, " +
                "   f_key, " +
                "   forder, " +
                "   nmkp, " +
                "   znkp, " +
                "   f_docid, " +
                "   f_parentkey, " +
                "   type, " +
                "   u_date " +
                " From " +
                "   u_arbplan_hyinfo " +
                " Where " +
                "   auftrag_nr = '" + sWorkplanTechParamOrderID.toUpperCase() + "' " +
                " Order by " +
                "   forder" ;
            if (printDebug) write(className, sql);
            ResultSet DataSet = stmt.executeQuery(sql);
            ResultSetMetaData retMetaData = DataSet.getMetaData();
            while (DataSet.next())
            {
                Map<String, Object> colData = new HashMap<String, Object>();
                for(int colInd = 1; colInd <= retMetaData.getColumnCount(); colInd++)
                {
                    colData.put(retMetaData.getColumnName(colInd), DataSet.getObject(colInd));
                }
                retData.add(colData);
            } 
            DataSet.close();
        }
        catch (Exception e) {
            if (printDebug) write(className, "Error: " + e.getMessage());
            return null;
        }

        if (retData.isEmpty())
        {
            if (printDebug) write(className, "Нет данных");
            return null;
        }
        
        IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
        builder.addCol("workplantechparam.doc.id", DataType.STRING);
        builder.addCol("workplantechparam.key", DataType.INTEGER);
        builder.addCol("workplantechparam.operation.designation", DataType.STRING);
        builder.addCol("workplantechparam.operation.operation", DataType.STRING);
        builder.addCol("workplantechparam.order.id", DataType.STRING);
        builder.addCol("workplantechparam.pdm.key", DataType.INTEGER);
        builder.addCol("workplantechparam.pdm.parentkey", DataType.INTEGER);
        builder.addCol("workplantechparam.techparam.date", DataType.DATETIME);
        builder.addCol("workplantechparam.techparam.index", DataType.INTEGER);
        builder.addCol("workplantechparam.techparam.name", DataType.STRING);
        builder.addCol("workplantechparam.techparam.subindex", DataType.STRING);
        builder.addCol("workplantechparam.techparam.type", DataType.STRING);
        builder.addCol("workplantechparam.techparam.value", DataType.STRING);
        
        for (Map<String, Object> colValue : retData)
        {
            builder.addRow();
            builder.value(colValue.get("f_docid"));
            builder.value(colValue.get("id"));
            builder.value(colValue.get("operation"));
            builder.value(colValue.get("agnr"));
            builder.value(colValue.get("auftrag_nr"));
            builder.value(colValue.get("f_key"));
            builder.value(colValue.get("f_parentkey"));
            builder.value(Timestamp2Calendar(colValue.get("u_date")));
            builder.value(colValue.get("f_order"));
            builder.value(Clob2String(colValue.get("nmkp")));
            builder.value(colValue.get("forder").toString());
            builder.value(colValue.get("type"));
            builder.value(Clob2String(colValue.get("znkp")));
        }
        
        res = new SesResultBuilder().addDataTable(builder.build()).build();
        return res;
    }

    private Object Clob2String(Object val) {
        try {
            Clob clob = (Clob)val;
            Reader r = clob.getCharacterStream();
            StringBuilder buffer = new StringBuilder();
            int ch;
            while ((ch = r.read())!=-1) {
                buffer.append("").append((char)ch);
            }
            return buffer.toString();
        } catch (SQLException ex) {
            Logger.getLogger(U_WorkplanHyInfoList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(U_WorkplanHyInfoList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ""; 
    }

    private Calendar Timestamp2Calendar(Object val) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(((Timestamp)val).getTime());
        return calendar;
    }
}