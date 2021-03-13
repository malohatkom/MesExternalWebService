package de.mpdv.SimpleExternalService;
import de.mpdv.sdi.data.HydraCallResult;
import de.mpdv.sdi.data.HydraReturnValue;
import de.mpdv.sdi.data.SesContext;
import de.mpdv.sdi.data.SesException;
import de.mpdv.sdi.data.SesRequest;
import de.mpdv.sdi.data.SesResult;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.simpleExternalService.ISimpleExternalService;
import de.mpdv.sdi.systemutility.IDbConnectionProvider;
import de.mpdv.sdi.systemutility.IHydraCaller;
import de.mpdv.sdi.systemutility.ISdiLogger;
import de.mpdv.sdi.systemutility.ISdiLoggerProvider;
import de.mpdv.sdi.systemutility.ISystemUtilFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class U_ShiftOrdersRemove implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private boolean debugEnabled = true;
    private final boolean debugMode = true;
 
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory) {
        ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        this.logger = loggerProvider.fetchLogger(U_ShiftOrdersRemove.class);
        this.debugEnabled = this.logger.isDebugEnabled();
        if (this.debugEnabled)
        {
            this.logger.debug("Release shift order");
        }

        SpecialParam shiftorder_key = request.getSpecialParam("shiftorder.key");
        SpecialParam shiftorder_id = request.getSpecialParam("shiftorder.id");
        SpecialParam operation_costcenter = request.getSpecialParam("operation.costcenter");

        if (debugMode)
        {
            String s = "";
            for (String key: request.getSpecialParamMap().keySet())
            {
                s += " " + key + " = " + request.getSpecialParam(key).getValue().toString();
            }
            System.out.println(s);
        }
        
        if (shiftorder_key != null && shiftorder_id != null)
        {
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            Connection con = null;
            Statement stmt = null;
            try {
                con = connProv.fetchDbConnection();
                stmt = con.createStatement();
                String sql = 
                " Update " +
                "   u_shiftorders " +
                " Set " +
                "   s_status = 'M' " +
                " Where " + 
                String.format("   shiftorder_nr = '%s' and id = %s", shiftorder_id.getValue(), shiftorder_key.getValue());
                
                if (this.debugEnabled)
                {
                    this.logger.debug(sql);
                }
                if (debugMode) System.out.println(sql);
                int affectedRows = stmt.executeUpdate(sql);
                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Remove " + affectedRows + " orders");
                }
                if (debugMode) System.out.println("Remove " + affectedRows + " orders");
                IHydraCaller caller = (IHydraCaller)factory.fetchUtil("HydraCaller");
                
                sql =
                " Select " +
                "   Distinct oi.[auftrag_nr], " +
                "   oi.[kostenstelle], " +
                "   (Select [a_status] From hydra1.hydadm.auftrag_status Where auftrag_nr = oi.[auftrag_nr]) a_status, " +
                "   IsNull(hb.subkey1, '') Workplace, " +
                "   IsNull(hb.subkey2, '') MESOrder, " +
                String.format("   IsNull(hb2.subkey4, N'%s') UserCode, ", request.getUserId()) +
                "   IsNull(hb.hyuser, '') HydraUser " +
                " From " +
                "   hydra1.hydadm.U_OperationInfo oi " +
                String.format("   Left Join hydra1.hydadm.u_shiftorders sh on sh.shiftorder_nr = N'%s' ", shiftorder_id.getValue()) +
                "   Left Join [hydra1].[hydadm].hybuch hb  on hb.subkey2 = oi.auftrag_nr and hb.typ = 'A' " +
                "   Left Join [hydra1].[hydadm].hybuch hb2 on hb2.subkey2 = hb.subkey2 and hb2.subkey4 is not null " +
                " Where " +
                "   aunr in (Select Distinct Left(auftrag_nr, 20) From hydra1.hydadm.u_shiftoperations Where shiftorder_nr = sh.shiftorder_nr) "; 
                
                if (operation_costcenter != null ? !"".equals(operation_costcenter.getValue()) : false)
                {
                    sql += String.format(" and oi.kostenstelle = N'%s' ", operation_costcenter.getValue());
                }
                        
                /*
                " Select " +
                "   [shiftorder_nr] " +
                "   ,sho.[kostenstelle] " +
                "   ,[auftrag_nr] " +
                "   ,[masch_nr] " +
                "   ,[mgruppe] " +
                "   ,[res_wnr] " +
                "   ,sho.[a_status] " +
                "   ,[term_anf_dat] " +
                "   ,[term_anf_zeit] " +
                "   ,[term_end_dat] " +
                "   ,[term_end_zeit] " +
                "   ,sho.[gut_bas] " +
                "   ,sho.[aus_bas] " +
                "   ,[bearb] " +
                "   ,[bearb_date] " +
                "   ,[bearb_time] " +
                "   ,IsNull(hb.subkey1, '') Workplace " +
                "   ,IsNull(hb.subkey2, '') MESOrder " +
                "   ,IsNull(hb2.subkey4, N'" + request.getUserId() + "') UserCode " +
                "   ,IsNull(hb.hyuser, '') HydraUser " +
                " From " +
                "   [hydra1].[hydadm].[u_shiftoperations] sho " +
                "   Left Join [hydra1].[hydadm].hybuch hb  on hb.subkey2 = sho.auftrag_nr and hb.typ = 'A' " +
                "   Left Join [hydra1].[hydadm].hybuch hb2 on hb2.subkey2 = hb.subkey2 and hb2.subkey4 is not null " +
                " Where " +
                "   shiftorder_nr = '" + (String)shiftorder_id.getValue() + "' ";
                */        
                if (debugMode) System.out.println(sql);
                ResultSet DataSet = stmt.executeQuery(sql);
                while (DataSet.next())
                {
                    if (!"".equals(DataSet.getString("MESOrder")) && !"".equals(DataSet.getString("Workplace")))
                    {
                        caller.addString("DLG", "A_AB");
                        caller.addString("USR", DataSet.getString("HydraUser"));
                        caller.addString("PNR", DataSet.getString("UserCode"));
                        caller.addString("ANR", DataSet.getString("MESOrder"));
                        caller.addString("MNR", DataSet.getString("Workplace"));
                        caller.addString("MST", "9;M");
                        caller.addString("ZEI", "now");
                        caller.addString("DAT", "today");
                        caller.addString("LANG", "18");
                        HydraCallResult result = caller.callHydra();
                        HydraReturnValue retVal = result.getReturnValue();
                        if (retVal.getReturnCode() != 0) 
                        {
                            if (debugMode) System.out.println(String.format("RET=%s KT=%s LT=%s ANR %s", retVal.getReturnCode(), retVal.getShortText(), retVal.getLongText(), DataSet.getString("MESOrder")));
                        }
                        else
                        {
                            if (debugMode) System.out.println(String.format("RET=%s OP %s Finished", retVal.getReturnCode(), DataSet.getString("MESOrder")));
                        }
                    }

                    caller.addString("DLG", "ANR.SPERREN");
                    caller.addString("ANR.ANR", DataSet.getString("auftrag_nr"));
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
                        if (debugMode) System.out.println(String.format("RET=%s KT=%s LT=%s anr %s", retVal.getReturnCode(), retVal.getShortText(), retVal.getLongText(), DataSet.getString("auftrag_nr")));
                    }
                    else
                    {
                        if (debugMode) System.out.println(String.format("RET=%s anr %s bloked", retVal.getReturnCode(), DataSet.getString("auftrag_nr")));
                    }
                }
                try
                {
                    DataSet.close();
                }
                catch (Exception e)
                {
                    if (debugMode) System.out.println("DataSet.close error " + e.getMessage());
                    throw new SesException("lkDbError", "Error at DB access: " + e.getMessage(), e, new String[0]);
                }

                /*
                sql =
                " Update " +
                "   [hydra1].[hydadm].[u_shiftoperations] " +
                " Set " +
                "   [a_status] = (Select a_status From hydra1.hydadm.auftrag_status Where auftrag_nr = '" + DataSet.getString("auftrag_nr") + "') " +
                " Where " +
                "   [auftrag_nr] = '" + DataSet.getString("auftrag_nr") + "'";
                int r = stmt.executeUpdate(sql);
                */
            }
            catch (SQLException e) {
                throw new SesException("lkDbError", "Error at DB access: " + e.getMessage(), e, new String[0]);
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
        }
        else
            this.logger.error("SpecialParam not found");
        return null;
    }
}