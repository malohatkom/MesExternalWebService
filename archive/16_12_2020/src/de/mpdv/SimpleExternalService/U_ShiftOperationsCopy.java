package de.mpdv.SimpleExternalService;
import de.mpdv.sdi.data.DataType;
import de.mpdv.sdi.data.HydraCallResult;
import de.mpdv.sdi.data.HydraReturnValue;
import de.mpdv.sdi.data.SesContext;
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
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class U_ShiftOperationsCopy implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private boolean debugEnabled = true;
    private final boolean debugMode = true;
 
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory) {
        SesResult res = null;
        ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        this.logger = loggerProvider.fetchLogger(U_ShiftOperationsCopy.class);
        this.debugEnabled = this.logger.isDebugEnabled();
        
        SpecialParam pOperation_id = null;
        SpecialParam pShiftorder_id = null;
        SpecialParam pOperation_workplace = null;
        SpecialParam pOperation_tool = null;
        SpecialParam pOperation_start = null;
        SpecialParam pOperation_end = null;
        
        String sOperation_id = "";
        String sShiftorder_id = "";
        String sOperation_workplace = "";
        String sOperation_tool = "";
        String sOperation_start = "";
        String sOperation_end = "";
        
        try
        {
            pOperation_id = request.getSpecialParam("operation.id");
            pShiftorder_id = request.getSpecialParam("shiftorder.id");
            pOperation_workplace = request.getSpecialParam("operation.act.workplace");
            pOperation_tool = request.getSpecialParam("operation.tool");
            pOperation_start = request.getSpecialParam("operation.scheduled_start_ts");
            pOperation_end = request.getSpecialParam("operation.scheduled_end_ts");
        }
        catch (Exception e)
        {
            pOperation_id = null;
            pShiftorder_id = null;
            pOperation_workplace = null;
            pOperation_tool = null;
            pOperation_start = null;
            pOperation_end = null;
        }
        finally
        {
            if (pOperation_id != null ? pOperation_id.getValue() != null : false) sOperation_id = pOperation_id.getValue().toString();        
            if (pShiftorder_id != null ? pShiftorder_id.getValue() != null : false) sShiftorder_id = pShiftorder_id.getValue().toString();        
            if (pOperation_workplace != null ? pOperation_workplace.getValue() != null : false) sOperation_workplace = pOperation_workplace.getValue().toString();        
            if (pOperation_tool != null ? pOperation_tool.getValue() != null : false) sOperation_tool = pOperation_tool.getValue().toString();        
            if (pOperation_start != null ? pOperation_start.getValue() != null : false) sOperation_start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").format(((GregorianCalendar)pOperation_start.getValue()).getTime());        
            if (pOperation_end != null ? pOperation_end.getValue() != null : false) sOperation_end = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").format(((GregorianCalendar)pOperation_end.getValue()).getTime());        
        }
        
        if (debugMode) System.out.println("U_ShiftOperationsCopy");
        
        if (this.debugEnabled)
        {
            this.logger.debug("Copy shift order operation");
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
        
        /*
        operation.act.workplace = CAB_3_OT 
        operation.id = 000000000000000000010015 
        operation.scheduled_end_ts = java.util.GregorianCalendar[time=1605711600000,areFieldsSet=true,areAllFieldsSet=false,lenient=true,zone=sun.util.calendar.ZoneInfo[id="Asia/Yekaterinburg",offset=18000000,dstSavings=3600000,useDaylight=true,transitions=118,lastRule=java.util.SimpleTimeZone[id=Asia/Yekaterinburg,offset=18000000,dstSavings=3600000,useDaylight=true,startYear=0,startMode=2,startMonth=2,startDay=-1,startDayOfWeek=1,startTime=7200000,startTimeMode=1,endMode=2,endMonth=9,endDay=-1,endDayOfWeek=1,endTime=7200000,endTimeMode=1]],firstDayOfWeek=1,minimalDaysInFirstWeek=1,ERA=?,YEAR=2020,MONTH=10,WEEK_OF_YEAR=?,WEEK_OF_MONTH=?,DAY_OF_MONTH=18,DAY_OF_YEAR=?,DAY_OF_WEEK=?,DAY_OF_WEEK_IN_MONTH=?,AM_PM=?,HOUR=?,HOUR_OF_DAY=20,MINUTE=0,SECOND=0,MILLISECOND=0,ZONE_OFFSET=?,DST_OFFSET=?] 
        operation.scheduled_start_ts = java.util.GregorianCalendar[time=1605668400000,areFieldsSet=true,areAllFieldsSet=false,lenient=true,zone=sun.util.calendar.ZoneInfo[id="Asia/Yekaterinburg",offset=18000000,dstSavings=3600000,useDaylight=true,transitions=118,lastRule=java.util.SimpleTimeZone[id=Asia/Yekaterinburg,offset=18000000,dstSavings=3600000,useDaylight=true,startYear=0,startMode=2,startMonth=2,startDay=-1,startDayOfWeek=1,startTime=7200000,startTimeMode=1,endMode=2,endMonth=9,endDay=-1,endDayOfWeek=1,endTime=7200000,endTimeMode=1]],firstDayOfWeek=1,minimalDaysInFirstWeek=1,ERA=?,YEAR=2020,MONTH=10,WEEK_OF_YEAR=?,WEEK_OF_MONTH=?,DAY_OF_MONTH=18,DAY_OF_YEAR=?,DAY_OF_WEEK=?,DAY_OF_WEEK_IN_MONTH=?,AM_PM=?,HOUR=?,HOUR_OF_DAY=8,MINUTE=0,SECOND=0,MILLISECOND=0,ZONE_OFFSET=?,DST_OFFSET=?] 
        operation.tool = I6000BDA08608C00002E 
        shiftorder.id = 11 
        shiftorder.key = 1
        */
        
        if (!"".equals(sOperation_id) && !"".equals(sShiftorder_id))
        {
            this.logger.debug("U_ShiftOperationsCopy has params");
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
                    "   [shiftorder_nr] " +
                    "   ,[kostenstelle]" +
                    "   ,[auftrag_nr] " +
                    "   ," + (!"".equals(sOperation_workplace) ? "N'"+sOperation_workplace+"' " : "[masch_nr]" ) + 
                    "   ,IsNull([mgruppe], '') " +
                    "   ," + (!"".equals(sOperation_tool) ? "N'"+sOperation_tool+"' " : "[res_wnr]" ) +
                    "   ,[artikel] " +        
                    "   ,[a_status] " +
                    "   ," + (!"".equals(sOperation_start) ? "Format(Cast('"+sOperation_start+"' as datetime), 'yyyy-MM-dd 00:00:00.0')" : "[term_anf_dat]" ) + 
                    "   ," + (!"".equals(sOperation_start) ? "DATEDIFF(SECOND, 0,CONVERT(time, Cast('"+sOperation_start+"' as datetime)))" : "[term_anf_zeit]" ) +
                    "   ," + (!"".equals(sOperation_end) ? "Format(Cast('"+sOperation_end+"' as datetime), 'yyyy-MM-dd 00:00:00.0')" : "[term_end_dat]" ) + 
                    "   ," + (!"".equals(sOperation_end) ? "DATEDIFF(SECOND, 0,CONVERT(time, Cast('"+sOperation_end+"' as datetime)))" : "[term_end_zeit]" ) +
                    "   ,[gut_bas] " +
                    "   ,[aus_bas] " +
                    "   ,N'" + request.getUserId() + "' " +
                    "   ,Format(CURRENT_TIMESTAMP, 'yyyy-MM-dd 00:00:00.000') " +
                    "   ,DATEDIFF(SECOND, 0,CONVERT(time, CURRENT_TIMESTAMP))" +
                    " From " +
                    "   [hydra1].[hydadm].[u_shiftoperations] " +
                    " Where " +
                    "   [shiftorder_nr] = N'" + sShiftorder_id + "' and auftrag_nr = '" + sOperation_id + "' ";
                
                    if (debugMode) System.out.println(sql);
                    int affectedRows = stmt != null ? stmt.executeUpdate(sql) : 0;
                    if (debugMode) System.out.println(affectedRows);
                    if (this.debugEnabled)
                    {
                        this.logger.debug(sql);
                    }
                    if (this.logger.isDebugEnabled())
                    {
                        this.logger.debug("Copy " + affectedRows + " OPs");
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