package de.mpdv.SimpleExternalService;
import de.mpdv.sdi.data.DataType;
import de.mpdv.sdi.data.HydraCallResult;
import de.mpdv.sdi.data.HydraReturnValue;
import de.mpdv.sdi.data.SesContext;
import de.mpdv.sdi.data.SesException;
import de.mpdv.sdi.data.SesRequest;
import de.mpdv.sdi.data.SesResult;
import de.mpdv.sdi.data.SesResultBuilder;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.simpleExternalService.ISimpleExternalService;
import de.mpdv.sdi.systemutility.IDataTableBuilder;
import de.mpdv.sdi.systemutility.IDbConnectionProvider;
import de.mpdv.sdi.systemutility.IHydraCaller;
import de.mpdv.sdi.systemutility.ISdiLogger;
import de.mpdv.sdi.systemutility.ISdiLoggerProvider;
import de.mpdv.sdi.systemutility.ISystemUtilFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class U_ShiftOperationsAdd implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private boolean debugEnabled = true;
    private final boolean debugMode = true;
 
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory) {
        SesResult res = null;
        ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        this.logger = loggerProvider.fetchLogger(U_ShiftOperationsAdd.class);
        this.debugEnabled = this.logger.isDebugEnabled();
        
        SpecialParam pOperation_id = null;
        SpecialParam pShiftorder_id = null;
        String sOperation_id = "";
        String sShiftorder_id = "";
        try
        {
            pOperation_id = request.getSpecialParam("operation.id");
            pShiftorder_id = request.getSpecialParam("shiftorder.id");
        }
        catch (Exception e)
        {
            pOperation_id = null;
            pShiftorder_id = null;
        }
        finally
        {
            if (pOperation_id != null ? pOperation_id.getValue() != null : false) sOperation_id = pOperation_id.getValue().toString();        
            if (pShiftorder_id != null ? pShiftorder_id.getValue() != null : false) sShiftorder_id = pShiftorder_id.getValue().toString();        
        }
        
        if (debugMode) System.out.println("U_ShiftOperationsAdd");
        
        if (this.debugEnabled)
        {
            this.logger.debug("Release shift order");
        }
        
        if (debugMode)
        {
            String s = "";
            for (String key: request.getSpecialParamMap().keySet())
            {
                s += " " + key + " = " + request.getSpecialParam(key).getValue().toString();
            }
            System.out.println(s);
        }
        
        if (!"".equals(sOperation_id) && !"".equals(sShiftorder_id))
        {
            this.logger.debug("U_ShiftOperationsAdd has params");
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            Connection con = null;
            Statement stmt = null;
            List<Map<String, String>> retData = new  ArrayList<Map<String, String>>();
            try {
                con = connProv.fetchDbConnection();
                stmt = con.createStatement();
                
                String sql = 
                " Select " +
                "   [auftrag_nr], " +
                "   ast.[a_status], " +
                "   IsNull(hb.subkey1, '') Workplace, " +
                "   IsNull(hb.subkey2, '') MESOrder, " +
                "   IsNull(hb2.subkey4, N'') UserCode, " +
                "   IsNull(hb.hyuser, '') HydraUser " +
                " From " +
                "   [hydra1].[hydadm].[auftrag_status] ast " +
                "   Left Join [hydra1].[hydadm].hybuch hb  on hb.subkey2 = ast.auftrag_nr and hb.typ = 'A' " +
                "   Left Join [hydra1].[hydadm].hybuch hb2 on hb2.subkey2 = hb.subkey2 and hb2.subkey4 is not null " +
                " Where " +
                "   ast.auftrag_nr in ('" + sOperation_id.replace(", ", "', '").replace("[", "").replace("]", "") + "') ";
                if (debugMode) System.out.println(sql);
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
            }
            catch (Exception e) {
                if (debugMode) System.out.println("DataSet.close Error " + e.getMessage());
                this.logger.error("Could not close ResultSet", e);
            }

            try
            {
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
                            if (debugMode) System.out.println("RET=" + retVal.getReturnCode() + " KT= " + retVal.getShortText() + " LT= " + retVal.getLongText());
                        }
                        else
                        {   
                            if (debugMode) System.out.println("RET=" + retVal.getReturnCode() + " OP " + rData.get("MESOrder") + " Finished");
                        }
                    }
                    caller.addString("DLG", "ANR.SPERREN");
                    caller.addString("MOD", "S");
                    caller.addString("ANR.ANR", rData.get("auftrag_nr"));
                    caller.addString("ANR.TABLE", "A");
                    caller.addString("ANR.ATYP", "AG");
                    caller.addString("PNR", request.getLangId());
                    caller.addString("ZEI", "now");
                    caller.addString("DAT", "today");
                    caller.addString("LANG", "18");
                    HydraCallResult result = caller.callHydra();
                    HydraReturnValue retVal = result.getReturnValue();
                    if (retVal.getReturnCode() != 0) 
                    {
                        if (debugMode) System.out.println("RET=" + retVal.getReturnCode() + " KT= " + retVal.getShortText() + " LT= " + retVal.getLongText());
                    }
                    else
                    {
                        if (debugMode) System.out.println("RET=" + retVal.getReturnCode() + "  " + rData.get("auftrag_nr") + " update op status");
                    }
                }
            }
            catch (Exception e)
            {
                if (debugMode) System.out.println("DataSet.close Error " + e.getMessage());
                this.logger.error("Could not close ResultSet", e);
            }
           
            try
            {
                if (!"".equals(sShiftorder_id))
                {
                    String sql = 
                    " INSERT INTO [hydadm].[u_shiftoperations] " +
                    "   ([shiftorder_nr] " +
                    "   ,[kostenstelle] " +
                    "   ,[auftrag_nr] " +
                    "   ,[masch_nr] " +
                    "   ,[mgruppe] " +
                    "   ,[res_wnr] " +
                    "   ,[artikel] " +
                    "   ,[a_status] " +
                    "   ,[term_anf_dat] " +
                    "   ,[term_anf_zeit] " +
                    "   ,[term_end_dat] " +
                    "   ,[term_end_zeit] " +
                    "   ,[gut_bas] " +
                    "   ,[aus_bas] " +
                    "   ,[bearb] " +
                    "   ,[bearb_date] " +
                    "   ,[bearb_time]) " +
                    " Select " +
                    "   N'" + sShiftorder_id + "' " +
                    "   ,[kostenstelle]" +
                    "   ,[ab_auftrag_nr] " +
                    "   ,IsNull([masch_nr], '') " +
                    "   ,IsNull([mgruppe], '') " +
                    "   ,[res_wnr] " +
                    "   ,[artikel] " +        
                    "   ,[a_status] " +
                    "   ,Format([term_anf_dat], 'yyyy-MM-dd 00:00:00.000') " +
                    "   ,DATEDIFF(SECOND, 0,CONVERT(time, [term_anf_dat])) " +
                    "   ,Format([term_end_dat], 'yyyy-MM-dd 00:00:00.000') " +
                    "   ,DATEDIFF(SECOND, 0,CONVERT(time, [term_end_dat])) " +
                    "   ,[gut_bas] " +
                    "   ,[aus_bas] " +
                    "   ,N'" + request.getUserId() + "' " +
                    "   ,Format(CURRENT_TIMESTAMP, 'yyyy-MM-dd 00:00:00.000') " +
                    "   ,DATEDIFF(SECOND, 0,CONVERT(time, CURRENT_TIMESTAMP))" +
                    " From " +
                    "   [hydra1].[hydadm].[U_OperationInfo] " +
                    " Where " +
                    "   [ab_auftrag_nr] in ('" + sOperation_id.replace(", ", "', '").replace("[", "").replace("]", "") + "') ";
                
                    if (debugMode) System.out.println(sql);
                    int affectedRows = stmt != null ? stmt.executeUpdate(sql) : 0;
                    if (debugMode) System.out.println(affectedRows);
                    if (this.debugEnabled)
                    {
                        this.logger.debug(sql);
                    }
                    if (this.logger.isDebugEnabled())
                    {
                        this.logger.debug("Add " + affectedRows + " OPs");
                    }
                }
            }
            catch (Exception e)
            {
                if (debugMode) System.out.println("DataSet.close Error " + e.getMessage());
                this.logger.error("Could not close ResultSet", e);
            }
            
            IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
            builder.addCol("shiftorder.id", DataType.STRING);
            builder.addCol("operation.id", DataType.STRING);
            builder.addRow();
            builder.value(sShiftorder_id);
            builder.value(sOperation_id);
            res = new SesResultBuilder().addDataTable(builder.build()).build();
            
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
        else
            System.out.println("U_ShiftOperationsAdd SpecialParam not found");
        return res;
    }
}