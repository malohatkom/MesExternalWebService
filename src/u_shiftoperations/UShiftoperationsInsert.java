/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package u_shiftoperations;

import de.mpdv.customization.userExit.IUserExitParam;
import de.mpdv.sdi.data.HydraCallResult;
import de.mpdv.sdi.data.HydraReturnValue;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.data.ue.BapiInterpreterUeContext;
import de.mpdv.sdi.data.ue.SdiAfterPerformActionParam;
import de.mpdv.sdi.systemutility.IDbConnectionProvider;
import de.mpdv.sdi.systemutility.IHydraCaller;
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
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Михаил
 */
public class UShiftoperationsInsert {
    private final boolean debugPrint = true;
    ISdiLogger logger = null;
    
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
        try
        {
            String s = "";
            if (debugPrint)
            {
                for (String key: param.keys()) s += " " + key + " = " + ((Object)param.get(key)).toString();
                write("UShiftoperationsInsert", s);
            }
            SdiAfterPerformActionParam aParam = (SdiAfterPerformActionParam)param.get("param");
            BapiInterpreterUeContext uContext = (BapiInterpreterUeContext)param.get("context");
            if (debugPrint)
            {
                for (String key: aParam.getSpecialParameters().keySet()) s += " " + key + " = " + aParam.getSpecialParameters().get(key).getValue().toString();
                write("UShiftoperationsInsert", s);
            }
        
            ISystemUtilFactory factory = (ISystemUtilFactory)param.get("factory");
            ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
            logger = loggerProvider.fetchLogger(UShiftoperationsInsert.class);
            logger.debug("UShiftoperationsInsert sdiAfterPerformAction");
        
            SpecialParam pOperation_id;
            SpecialParam pShiftorder_id;
            String sOperation_id = "";
            String sShiftorder_id = "";

            pOperation_id   = aParam.getSpecialParameters().get("operation.id");
            pShiftorder_id  = aParam.getSpecialParameters().get("shiftorder.id");

            if (pOperation_id != null && pShiftorder_id != null)
            {
                sOperation_id = pOperation_id.getValue().toString();        
                sShiftorder_id = pShiftorder_id.getValue().toString();        
            }    
        
            if (!"".equals(sOperation_id) && !"".equals(sShiftorder_id))
            {
                IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
                List<Map<String, String>> retData = new  ArrayList<Map<String, String>>();

                Connection con = connProv.fetchDbConnection();
                Statement stmt = con.createStatement();
                
                String sql = 
                " Select " +
                "   auftrag_nr, " +
                "   ast.a_status, " +
                "   IsNull(hb.subkey1, '') Workplace, " +
                "   IsNull(hb.subkey2, '') MESOrder, " +
                "   IsNull(hb2.subkey4, '"+uContext.getUserId()+"') UserCode, " +
                "   IsNull(hb.hyuser, '') HydraUser " +
                " From " +
                "   auftrag_status ast " +
                "   Left Join hybuch hb  on hb.subkey2 = ast.auftrag_nr and hb.typ = 'A' " +
                "   Left Join hybuch hb2 on hb2.subkey2 = hb.subkey2 and hb2.subkey4 is not null " +
                " Where " +
                "   ast.auftrag_nr in ('" + sOperation_id.replace(", ", "', '").replace("[", "").replace("]", "") + "') ";
                if (debugPrint) write("UShiftoperationsInsert", sql);
                ResultSet DataSet = stmt.executeQuery(sql);
                ResultSetMetaData retMetaData = DataSet.getMetaData();
                
                while (DataSet.next())
                {
                    Map<String, String> colData = new HashMap<String, String>();
                    for(int colInd = 1; colInd <= retMetaData.getColumnCount(); colInd++)
                    {
                        colData.put(retMetaData.getColumnName(colInd), DataSet.getString(colInd));
                    }
                    retData.add(colData);
                } 
                DataSet.close();

                IHydraCaller caller = (IHydraCaller)factory.fetchUtil("HydraCaller");
                for (Map<String, String> rData: retData)
                {
                    if (!"".equals(rData.get("MESOrder")) && !"".equals(rData.get("Workplace")))
                    {
                        caller.addString("DLG", "A_AB");
                        caller.addString("USR", rData.get("HydraUser"));
                        caller.addString("PNR", rData.get("UserCode"));
                        caller.addString("ANR", rData.get("MESOrder"));
                        caller.addString("MNR", rData.get("Workplace"));
                        caller.addString("MST", "9;M");
                        caller.addString("ZEI", "now");
                        caller.addString("DAT", "today");
                        caller.addString("LANG", "18");
                        HydraCallResult result = caller.callHydra();
                        HydraReturnValue retVal = result.getReturnValue();
                        if (retVal.getReturnCode() != 0) 
                        {
                            if (debugPrint) write("UShiftoperationsInsert", "RET=" + retVal.getReturnCode() + " KT= " + retVal.getShortText() + " LT= " + retVal.getLongText());
                        }
                        else
                        {   
                            if (debugPrint) write("UShiftoperationsInsert", "RET=" + retVal.getReturnCode() + " OP " + rData.get("MESOrder") + " Finished");
                        }
                    }
                    caller.addString("DLG", "ANR.SPERREN");
                    caller.addString("MOD", "S");
                    caller.addString("ANR.ANR", rData.get("auftrag_nr"));
                    caller.addString("ANR.TABLE", "A");
                    caller.addString("ANR.ATYP", "AG");
                    caller.addString("PNR", rData.get("UserCode"));
                    caller.addString("ZEI", "now");
                    caller.addString("DAT", "today");
                    caller.addString("LANG", "18");
                    HydraCallResult result = caller.callHydra();
                    HydraReturnValue retVal = result.getReturnValue();
                    if (retVal.getReturnCode() != 0) 
                    {
                        if (debugPrint) write("UShiftoperationsInsert", "RET=" + retVal.getReturnCode() + " KT= " + retVal.getShortText() + " LT= " + retVal.getLongText());
                    }
                    else
                    {
                        if (debugPrint) write("UShiftoperationsInsert", "RET=" + retVal.getReturnCode() + "  " + rData.get("auftrag_nr") + " update op status");
                    }
                }
                stmt.close();
                con.close();
            }
        }
        catch (Exception e) {
            logger.error("Could not close connection", e);
            write("UShiftoperationsInsert", "Error: " + e.getMessage());
        } 
    }
}
