/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package u_shiftorders;

import de.mpdv.customization.userExit.IUserExitParam;
import de.mpdv.sdi.data.HydraCallResult;
import de.mpdv.sdi.data.HydraReturnValue;
import de.mpdv.sdi.data.SesException;
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
public class UShiftordersUpdate {
    private final boolean debugPrint = true;
    private final String ComputerName = System.getenv("COMPUTERNAME");
    private final boolean debugMode = "ETN-SRV-MES3".equals(ComputerName);
    
    private final String VP_OP_DEBUGMODE = "%Неразрушающий контроль (УЗК)";
    private final String UP_OP_DEBUGMODE = "Упаковка";
    private final String VP_OP_WORKMODE = "%Формирование поддона";
    private final String UP_OP_WORKMODE = "Упаковка и комплектация";
    
    private static void write(String text) {
        String fileName = "C:\\Windows\\Temp\\UShiftordersUpdate.log";
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
            write(s);
        }
        SdiAfterPerformActionParam aParam = (SdiAfterPerformActionParam)param.get("param");
        BapiInterpreterUeContext uContext = (BapiInterpreterUeContext)param.get("context");
        if (debugPrint)
        {
            for (String key: aParam.getSpecialParameters().keySet()) s += " " + key + " = " + aParam.getSpecialParameters().get(key).getValue().toString();
            write(s);
        }
        
        ISystemUtilFactory factory = (ISystemUtilFactory)param.get("factory");
        ISdiLoggerProvider logerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        ISdiLogger logger = logerProvider.fetchLogger(UShiftordersUpdate.class);
        logger.debug("Testing UShiftordersUpdate");
        SpecialParam shiftorder_key = aParam.getSpecialParameters().get("shiftorder.key");
        SpecialParam shiftorder_status = aParam.getSpecialParameters().get("shiftorder.status");
        SpecialParam operation_costcenter = aParam.getSpecialParameters().get("operation.costcenter");
        
        if (shiftorder_status != null && shiftorder_key != null ? !"".equals(shiftorder_status.getValue()) && !"".equals(shiftorder_key.getValue()) : false)
        {
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            Connection con;
            Statement stmt;
            
            try
            {
                con = connProv.fetchDbConnection();
                stmt = con.createStatement();
                
                String sql = 
                " Select " +
                "   oi.[ab_auftrag_nr], " +
                "   IsNull(oi.[ag_bez], '') ag_bez, " +
                "   IsNull(oi.[artikel], '') artikel, " +
                "   uso.[shiftorder_nr], " +
                "   IsNull(oi.[kostenstelle], '') kostenstelle, " +
                String.format("   IsNull((Select top 1 IsNull(auftrag_nr, '') From hydra1.hydadm.auftrags_bestand Where ag_bez like N'%s' and Left(auftrag_nr, 20) = Left(oi.ab_auftrag_nr, 20)), '') auftrag_nr2, ", debugMode ? VP_OP_DEBUGMODE : VP_OP_WORKMODE) +
                "   IsNull(uso.[masch_nr], '') masch_nr, " +
                "   oi.[mgruppe], " +
                "   (Select [a_status] From hydra1.hydadm.auftrag_status Where auftrag_nr = oi.[ab_auftrag_nr]) a_status, " +
                "   IsNull(uso.[term_anf_dat], IsNull((Select Max(term_anf_dat) From hydra1.hydadm.u_shiftoperations Where shiftorder_nr = sh.shiftorder_nr), Format(CURRENT_TIMESTAMP,'yyyy-MM-dd 00:00:00.000'))) term_anf_dat, " +
                "   IsNull(uso.[term_anf_zeit],	Case When sh.shift_nr = 'A' Then 28800 When sh.shift_nr = 'B' Then 72000 End) term_anf_zeit, " +
                "   IsNull(uso.[term_end_dat], IsNull((Select Max(term_end_dat) From hydra1.hydadm.u_shiftoperations Where shiftorder_nr = sh.shiftorder_nr), Format(CURRENT_TIMESTAMP,'yyyy-MM-dd 00:00:00.000'))) term_end_dat, " +
                "   IsNull(uso.[term_end_zeit],	Case When sh.shift_nr = 'A' Then 72000 When sh.shift_nr = 'B' Then 28800 End) term_end_zeit, " +
                "   IsNull(hb.subkey1, '') Workplace, " +
                "   IsNull(hb.subkey2, '') MESOrder, " +
                String.format("   IsNull(hb2.subkey4, N'%s') UserCode, ", uContext.getUserId()) +
                "   IsNull(hb.hyuser, '') HydraUser " +
                " From " +
                "   hydra1.hydadm.U_OperationInfo oi " +
                String.format("   Left Join hydra1.hydadm.u_shiftorders sh on sh.id = %s ", shiftorder_key.getValue()) +
                "   Left Outer Join hydra1.hydadm.u_shiftoperations uso on uso.auftrag_nr = oi.ab_auftrag_nr and uso.shiftorder_nr = sh.shiftorder_nr " +
                "   Left Join [hydra1].[hydadm].hybuch hb  on hb.subkey2 = oi.ab_auftrag_nr and hb.typ = 'A' " +
                "   Left Join [hydra1].[hydadm].hybuch hb2 on hb2.subkey2 = hb.subkey2 and hb2.subkey4 is not null " +
                " Where " +
                "   ak_auftrag_nr in (Select Distinct Left(auftrag_nr, 20) From hydra1.hydadm.u_shiftoperations Where shiftorder_nr = sh.shiftorder_nr) "; 
                
                if (operation_costcenter != null ? (operation_costcenter.getValue() != null ? !"".equals(operation_costcenter.getValue()) : false) : false)
                {
                    sql += String.format(" and oi.kostenstelle = N'%s' ", operation_costcenter.getValue());
                }
                       
                if (debugPrint) write(sql);

                Map<String, Map<String, String>> ab = new HashMap<String, Map<String, String>>();
                
                Map<String, List<String>> fp = new HashMap<String, List<String>>();
                List<String> lm; 
                
                Map<String, String> up = new HashMap<String, String>();
                        
                if (debugPrint) write(sql);
                
                ResultSet DataSet = stmt.executeQuery(sql);
                while (DataSet.next())
                {
                    Map<String, String> columns = new HashMap<String, String>();
                    ResultSetMetaData md = DataSet.getMetaData();
                    for (int cInd = 1; cInd <= md.getColumnCount(); cInd++)
                    {
                        columns.put(md.getColumnName(cInd), DataSet.getString(cInd));
                    }
                    if (!ab.containsKey(DataSet.getString("ab_auftrag_nr"))) ab.put(DataSet.getString("ab_auftrag_nr"), columns);
                    
                    //&& !"".equals(DataSet.getString("shiftorder_nr")) 
                    if (!"".equals(DataSet.getString("auftrag_nr2")) && DataSet.getString("masch_nr").contains(debugMode ? "RENTGEN" : "LM"))
                    {
                        if (debugPrint) write(String.format("auftrag_nr2 = [%s] masch_nr = [%s]", DataSet.getString("auftrag_nr2"), DataSet.getString("masch_nr")));
                        if (fp.containsKey(DataSet.getString("auftrag_nr2")))
                        {
                            lm = fp.get(DataSet.getString("auftrag_nr2"));
                        }
                        else
                            lm = new ArrayList<String>();
                            
                        lm.add(DataSet.getString("masch_nr"));
                        fp.put(DataSet.getString("auftrag_nr2"), lm);
                    }
                    
                    if (DataSet.getString("ag_bez").equals(debugMode ? UP_OP_DEBUGMODE : UP_OP_WORKMODE))
                    {
                        if (!up.containsKey(DataSet.getString("ab_auftrag_nr")))
                        {
                            up.put(DataSet.getString("ab_auftrag_nr"), DataSet.getString("artikel"));
                        }
                    }
                }
                DataSet.close();
                       
                sql =
                " Update " +
                "   u_shiftorders " +
                " Set " +
                String.format("   s_status = '%s' ", shiftorder_status.getValue()) +
                " Where " + 
                String.format(" id = %s", shiftorder_key.getValue());

                if (debugPrint) write(sql);
                int affectedRows = stmt.executeUpdate(sql);
                if (debugPrint) write("Update " + affectedRows + " orders");
                
                IHydraCaller caller = (IHydraCaller)factory.fetchUtil("HydraCaller");
            
                if ("M".equals(shiftorder_status.getValue()))
                {
                    if (debugPrint) write("Remove shift order");

                    if (!ab.isEmpty())
                    {
                        for (String key: ab.keySet())
                        {
                            if (!"".equals(ab.get(key).get("MESOrder")) && !"".equals(ab.get(key).get("Workplace")))
                            {
                                caller.addString("DLG", "A_AB");
                                caller.addString("USR", ab.get(key).get("HydraUser"));
                                caller.addString("PNR", ab.get(key).get("UserCode"));
                                caller.addString("ANR", ab.get(key).get("MESOrder"));
                                caller.addString("MNR", ab.get(key).get("Workplace"));
                                caller.addString("MST", "9;M");
                                caller.addString("ZEI", "now");
                                caller.addString("DAT", "today");
                                caller.addString("LANG", "18");
                                HydraCallResult result = caller.callHydra();
                                HydraReturnValue retVal = result.getReturnValue();
                                if (retVal.getReturnCode() != 0) 
                                {
                                    if (debugPrint) write(String.format("RET=%s KT=%s LT=%s ANR %s", retVal.getReturnCode(), retVal.getShortText(), retVal.getLongText(), ab.get(key).get("MESOrder")));
                                }
                                else
                                {
                                    if (debugPrint) write(String.format("RET=%s OP %s Finished", retVal.getReturnCode(), ab.get(key).get("MESOrder")));
                                }
                            }

                            caller.addString("DLG", "ANR.SPERREN");
                            caller.addString("ANR.ANR", ab.get(key).get("ab_auftrag_nr"));
                            caller.addString("ANR.TABLE", "A");
                            caller.addString("ANR.ATYP", "AG");
                            caller.addString("PNR", uContext.getUserId());
                            caller.addString("ZEI", "now");
                            caller.addString("DAT", "today");
                            caller.addString("LANG", "18");
                            HydraCallResult result = caller.callHydra();
                            HydraReturnValue retVal = result.getReturnValue();
                            if (retVal.getReturnCode() != 0) 
                            {
                                if (debugPrint) write(String.format("RET=%s KT=%s LT=%s anr %s", retVal.getReturnCode(), retVal.getShortText(), retVal.getLongText(), ab.get(key).get("auftrag_nr")));
                            }
                            else
                            {
                                if (debugPrint) write(String.format("RET=%s anr %s bloked", retVal.getReturnCode(), ab.get(key).get("ab_auftrag_nr")));
                            }
                        }
                    }
                }
            
            
                if ("R".equals(shiftorder_status.getValue()))
                {
                    if (debugPrint) write("Release shift order");
                    if (debugPrint) write("serverName = " + ComputerName);
                
                    if (!ab.isEmpty())
                    {
                        for(String key: ab.keySet())
                        {
                            Map<String, String> rData = ab.get(key);
                            caller.addString("DLG", "ANR.ENTSPERREN");
                            caller.addString("ANR.ANR", key);
                            caller.addString("ANR.TABLE", "A");
                            caller.addString("ANR.ATYP", "AG");
                            caller.addString("PNR", uContext.getUserId());
                            caller.addString("ZEI", "now");
                            caller.addString("DAT", "today");
                            caller.addString("LANG", "18");
                            HydraCallResult result = caller.callHydra();
                            HydraReturnValue retVal = result.getReturnValue();
                            if (retVal.getReturnCode() != 0) 
                            {
                                if (debugPrint) write(String.format("RET=%s KT=%s LT=%s ANR %s", retVal.getReturnCode(), retVal.getLongText(), retVal.getShortText(), key));
                            }
                            else
                            {
                                if (debugPrint) write(String.format("RET=%s unblock OP %s", retVal.getReturnCode(), key));
                                sql = 
                                " Update " +
                                "   hydra1.hydadm.auftrags_bestand " +
                                " Set " +
                                "   term_anf_dat = '" + rData.get("term_anf_dat") + "', " +
                                "   term_anf_zeit = " + rData.get("term_anf_zeit") + ", " +
                                "   term_end_dat = '" + rData.get("term_end_dat") + "', " +
                                "   term_end_zeit = " + rData.get("term_end_zeit") + " " +
                                " Where " +
                                "   auftrag_nr = '" + key + "' ";
                                if (debugPrint) write(sql);
                                int r = stmt.executeUpdate(sql);
                                if (debugPrint) write(" update auftrags_bestand rows count = " + r);
                            }
                            
                            if ("E".equals(rData.get("a_status")))
                            {
                                caller.addString("DLG", "ANR.SETSTATUS");
                                caller.addString("ANR.ANR", key);
                                caller.addString("ANR.TABLE", "A");
                                caller.addString("ANR.ATYP", "AG");
                                caller.addString("MOD", "R");
                                caller.addString("PNR", uContext.getUserId());
                                caller.addString("ZEI", "now");
                                caller.addString("DAT", "today");
                                caller.addString("LANG", "18");
                                result = caller.callHydra();
                                retVal = result.getReturnValue();
                                if (retVal.getReturnCode() != 0) 
                                {
                                    if (debugPrint) write(String.format("RET=%s KT=%s LT=%s ANR %s", retVal.getReturnCode(), retVal.getShortText(), retVal.getLongText(), key));
                                }
                                else
                                {
                                    if (debugPrint) write(String.format("RET=%s Reactivate OP %s", retVal.getReturnCode(), key));
                                }
                            }
                        }
                    }

                    if (!fp.isEmpty())
                    {
                        if (debugPrint) write(String.format("fp = [%s]", fp));
                        for (String key: fp.keySet())
                        {
                            sql =
                            " Delete " +
                            "   hydra1.hydadm.mlst_hy " +
                            " Where " +
                            "   auftrag_nr = '" + key + "'";
                            stmt.executeUpdate(sql);
                            for (String mnr: fp.get(key))
                            {
                                String mnrInd = debugMode ? mnr.replace("RENTGEN", "") : mnr.replace("LM", "");
                                sql = 
                                CreateComponentSql(
                                    key,                                //auftrag_nr,
                                    "00000000812",                      //artikel,
                                    mnr,                                //bez_1,
                                    "",                                 //bez_2,
                                    0,                                  //soll_menge,
                                    "кг",                               //soll_einh,
                                    mnrInd,                             //pos,
                                    "PF_Плавка",                        //hz_typ,
                                    "MAT" + mnrInd,                     //artikel_bez,
                                    uContext.getUserId()                //bearb
                                );
                                if (debugPrint) write(sql);
                                int i = stmt.executeUpdate(sql);
                                if (debugPrint) write("insert components count = " + i + " anr " + key);
                            }
                        }
                    }
                    if (debugPrint) write(up.toString());
                    if (!up.isEmpty())
                    {
                        List<String> InsertSQL = new ArrayList<String>();
                        for (String key: up.keySet())
                        {
                            sql =
                            " Delete " +
                            "   hydra1.hydadm.mlst_hy " +
                            " Where " +
                            "   auftrag_nr = '" + key + "' and kennz = 'M' and pos != 1";
                            
                            //if (debugPrint) System.out.println(sql);
                            
                            stmt.executeUpdate(sql);
                        
                            sql = 

                            " Select " +
                            "   IsNull(artikel, '') artikel, " +
                            "   IsNull(artikel_bez, '') artikel_bez, " +
                            "   IsNull(bez_1, '') bez_1, " +
                            "   IsNull(bez_2, '') bez_2, " +
                            "   IsNull(hz_typ, '') hz_typ, " +
                            "   IsNull(soll_menge, 0) soll_menge, " +
                            "   IsNull(soll_einh, '') soll_einh, " +
                            " From " +
                            "   hydra1.hydadm.U_PackingMaterial " +
                            " Where " +
                            "   U_PackingScheme_id = " +
                            "   ( " +
                            "       Select top 1 " +
                            "           id " +
                            "       From " +
                            "           hydra1.hydadm.U_PackingScheme " +
                            "       Where " +
                            "           name = " +
                            "           ( " +
                            "               Select top 1 " +
                            "                   AST_STOARAGE_TYPE " +
                            "               From " +
                            "                   hydra1.hydadm.u_assortment " +
                            "               Where " +
                            String.format("      AST_WHEEL_KOD = '%s' ", debugMode ? "4702" : up.get(key)) +
                            "           ) " +
                            "   ) " +
                            "   and artikel != 'LABEL' " +
                            " Union All " +
                            " Select " +
                            "   N'LABEL' artikel, " +
                            "   N'Упаковочная этикетка' artikel_bez, " +
                            "   N'100х100' bez_1, " +
                            "   Sum(IsNull(upl.labelcount, 1)) bez_2, " +
                            "   N'Этикетка' hz_typ, " +
                            "   Sum(IsNull(upl.labelcount, 1)) soll_menge, " +
                            "   N'шт' soll_einh, " +
                            " From " +
                            "   hydra1.hydadm.U_PackingLabel upl " +
                            " Where " +
                            "   U_PackingScheme_id = " +
                            "   ( " +
                            "       Select top 1 " +
                            "           id " +
                            "       From " +
                            "           hydra1.hydadm.U_PackingScheme " +
                            "       Where " +
                            "           name = " +
                            "		( " +
                            "               Select top 1 " +
                            "                   AST_STOARAGE_TYPE " +
                            "               From " +
                            "                   hydra1.hydadm.u_assortment " +
                            "               Where " +
                            String.format("      AST_WHEEL_KOD = '%s' ", debugMode ? "4702" : up.get(key)) +
                            "           ) " +
                            "   ) " +
                            " Group By " +
                            "   U_PackingScheme_id ";
                            
                            if (debugPrint) write(sql);
                            DataSet = stmt.executeQuery(sql);
                            
                            int pos = 2;
                            while (DataSet.next())
                            {
                                InsertSQL.add(CreateComponentSql(
                                    key,                                //auftrag_nr,
                                    DataSet.getString("artikel"),       //artikel,
                                    DataSet.getString("bez_1"),         //bez_1,
                                    DataSet.getString("bez_2"),         //bez_2,
                                    DataSet.getDouble("soll_menge"),    //soll_menge,
                                    DataSet.getString("soll_einh"),     //soll_einh,
                                    Integer.toString(pos++),            //pos,
                                    DataSet.getString("hz_typ"),        //hz_typ,
                                    DataSet.getString("artikel_bez"),   //artikel_bez,
                                    uContext.getUserId()                //bearb
                                ));
                                //if (debugMode) System.out.println(sql);
                            }
                            DataSet.close();
                        }
                    
                        if (debugPrint) write(InsertSQL.toString());
                        for (String inssql: InsertSQL)
                        {
                            if (debugPrint) write(inssql);
                            int i = stmt.executeUpdate(inssql);
                            if (debugPrint) write("insert rows " + i);
                        }
                    }
                }
                stmt.close();
                con.close();
            }
            catch (SQLException e) {
                if (debugPrint) write("error = " + e.getMessage());
                throw new SesException("lkDbError", "Error at DB access: " + e.getMessage(), e, new String[0]);
            } 
            catch (NumberFormatException e) {
                if (debugPrint) write("error = " + e.getMessage());
                throw new SesException("lkDbError", "Error at DB access: " + e.getMessage(), e, new String[0]);
            }
        }
    }
    
    private String CreateComponentSql(
        String auftrag_nr,
        String artikel,
        String bez_1,
        String bez_2,
        double soll_menge,
        String soll_einh,
        String pos,
        String hz_typ,
        String artikel_bez,
        String bearb
    ) 
    {
        String res =
            " Set IDENTITY_INSERT hydadm.mlst_hy ON; " +
            " Insert Into " +
            "   hydra1.hydadm.mlst_hy " +
            "   ( " +
            "       auftrag_nr, " +
            "       artikel, " +
            "       bez_1, " +
            "       bez_2, " +
            "       soll_menge, " +
            "       soll_einh, " +
            "       kennz, " +
            "       ist_menge, " +
            "       pos, " +
            "       hz_typ, " +
            "       artikel_bez, " +
            "       verweis, " +
            "       bereistell_menge, " +
            "       we_menge, " +
            "       manuell, " +
            "       mlst_pshort_01, " +
            "       mlst_pshort_02, " +
            "       mlst_plong_01, " +
            "       mlst_plong_02, " +
            "       mlst_pdouble_01, " +
            "       mlst_pdouble_02, " +
            "       mlst_param_str01, " +
            "       mlst_param_str02, " +
            "       verbrauch, " +
            "       sap_vornr, " +
            "       ersetzbar, " +
            "       ca_wl_pflicht, " +
            "       delete_kz, " +
            "       rf_sa_mg, " +
            "       rf_sa_mg_einh, " +
            "       plan_artikel, " +
            "       res_typ, " +
            "       res_familie, " +
            "       res_version, " +
            "       anzahl_von_pps, " +
            "       anzahl_gesamt, " +
            "       menge_von_pps, " +
            "       meta_res, " +
            "       bearb, " +
            "       bearb_date, " +
            "       bearb_time, " +
            "       ist_menge_einh, " +
            "       ist_menge_sek, " + 
            "       ist_einh_sek, " +
            "       ist_menge_ter, " +
            "       ist_einh_ter, " +
            "       soll_menge_sek, " +
            "       soll_einh_sek, " +
            "       soll_menge_ter, " +
            "       soll_einh_ter, " +
            "       bereist_menge_einh, " +
            "       we_menge_einh, " +
            "       verbrauch_einh, " +
            "       res_verweis, " +
            "       res_verweis_m, " +
            "       path, " +
            "       datei, " +
            "       stueli_stufe_m, " +
            "       pos_m, " +
            "       stueli_stufe, " +
            "       kz_fmenge, " +
            "       verbrauch_art, " +
            "       rgekz_pps, " +
            "       firma, " +
            "       soll_menge_proz, " +
            "       obere_toleranz, " +
            "       untere_toleranz, " +
            "       hauptressource, " +
            "       user_code, " +
            "       user_d_01, " +
            "       user_d_02, " +
            "       user_d_03, " +
            "       user_d_04, " +
            "       user_d_05, " +
            "       user_d_06, " +
            "       user_n_07, " +
            "       user_n_08, " +
            "       user_n_09, " +
            "       user_n_10, " +
            "       user_n_11, " +
            "       user_n_12, " +
            "       user_n_13, " +
            "       user_n_14, " +
            "       user_n_15, " +
            "       user_n_16, " +
            "       user_n_17, " +
            "       user_n_18, " +
            "       user_n_19, " +
            "       user_n_20, " +
            "       user_n_21, " + 
            "       user_n_22, " +
            "       user_f_23, " +
            "       user_f_24, " +
            "       user_f_25, " +
            "       user_f_26, " +
            "       user_f_27, " +
            "       user_f_28, " + 
            "       user_c_29, " +
            "       user_c_30, " +
            "       user_c_31, " +
            "       user_c_32, " +
            "       user_c_33, " +
            "       user_c_34, " +
            "       user_c_35, " +
            "       user_c_36, " +
            "       user_c_37, " +
            "       user_c_38, " +
            "       user_c_39, " +
            "       user_c_40, " +
            "       user_c_41, " +
            "       user_c_42, " + 
            "       user_c_43, " +
            "       user_c_44, " +
            "       user_c_45, " +
            "       user_c_46, " +
            "       user_c_47, " +
            "       user_c_48, " +
            "       user_c_49, " +
            "       user_c_50, " +
            "       user_c_51, " +
            "       user_c_52, " +
            "       user_c_53, " +
            "       user_c_54, " +
            "       user_c_55, " +
            "       user_c_56, " +
            "       user_c_57, " +
            "       user_c_58, " +
            "       user_c_59, " +
            "       user_c_60, " +
            "       user_c_61, " +
            "       user_c_62, " +
            "       user_c_63, " +
            "       user_c_64, " +
            "       user_c_65, " +
            "       user_c_66, " +
            "       mengen_tol, " +
            "       mengen_abweichung, " +
            "       chargen_nr, " +
            "       sta_mengenanp, " +
            "       sta_erfasst, " +
            "       tranr, " +
            "       kennz_slos, " +
            "       verb_zaehler " +
            "   ) " +
            "   Select " + 
            "       '" + auftrag_nr + "'," +        //auftrag_nr 
            "       N'" + artikel + "', " +         //artikel
            "       N'" + bez_1 + "', " +           //bez_1
            "       N'" + bez_2 + "', " +           //bez_2
            "       " + soll_menge + ", " +         //soll_menge
            "       N'" + soll_einh + "', " +       //soll_einh
            "       N'M', " +                       //kennz
            "       0.000000, " +                   //ist_menge
            "       " + pos + ", " +   //(debugEnabled ? mnr.replace("TIP_UP_", "") : mnr.replace("LM", "")) + ", " +   //pos
            "       N'" + hz_typ +"', " +               //hz_typ
            "       N'" + artikel_bez + "', " +  //artikel_bez 
            "       (Select Max(verweis) + 1 From hydra1.hydadm.mlst_hy), " +   //verweis
            "       0.000000, " +                   //bereistell_menge
            "       0.000000, " +                   //we_menge
            "       N'N', " +                       //manuell
            "       NULL, " +                       //mlst_pshort_01
            "       NULL, " +                       //mlst_pshort_02
            "       NULL, " +                       //mlst_plong_01
            "       NULL, " +                       //mlst_plong_02
            "       0.000000, " +                   //mlst_pdouble_01
            "       NULL, " +                       //mlst_pdouble_02
            "       NULL, " +                       //mlst_param_str01
            "       NULL, " +                       //mlst_param_str02
            "       2.000000, " +                   //verbrauch
            "       NULL, " +                       //sap_vornr
            "       N'J', " +                       //ersetzbar
            "       N'N', " +                       //ca_wl_pflicht
            "       NULL, " +                       //delete_kz
            "       NULL, " +                       //rf_sa_mg
            "       NULL, " +                       //rf_sa_mg_einh
            "       NULL, " +                       //plan_artikel
            "       N'MAT', " +                     //res_typ
            "       0, " +                          //res_familie
            "       NULL, " +                       //res_version
            "       0, " +                          //anzahl_von_pps
            "       1, " +                          //anzahl_gesamt
            "       0.000000, " +                   //menge_von_pps
            "       NULL, " +                       //meta_res
            "       N'" + bearb + "', " + //bearb request.getUserId()
            "       Format(CURRENT_TIMESTAMP, 'yyyy-MM-dd 00:00:00.000'), " +   //bearb_date
            "       DATEDIFF(SECOND, 0,CONVERT(time, CURRENT_TIMESTAMP)), " +   //bearb_time
            "       NULL, " +                       //ist_menge_einh
            "       0.000000, " +                   //ist_menge_sek
            "       NULL, " +                       //ist_einh_sek
            "       0.000000, " +                   //ist_menge_ter
            "       NULL, " +                       //ist_einh_ter
            "       0.000000, " +                   //soll_menge_sek
            "       NULL, " +                       //soll_einh_sek
            "       0.000000, " +                   //soll_menge_ter
            "       NULL, " +                       //soll_einh_ter
            "       NULL, " +                       //bereist_menge_einh
            "       NULL, " +                       //we_menge_einh
            "       N'шт', " +                      //verbrauch_einh
            "       NULL, " +                       //res_verweis
            "       0, " +                          //res_verweis_m
            "       N'HYDRA', " +                   //path
            "       NULL, " +                       //datei
            "       0, " +                          //stueli_stufe_m
            "       NULL, " +                       //pos_m
            "       0, " +                          //stueli_stufe
            "       NULL, " +                       //kz_fmenge
            "       N'L', " +                       //verbrauch_art
            "       NULL, " +                       //rgekz_pps
            "       NULL, " +                       //firma
            "       0.000000, " +                   //soll_menge_proz
            "       0.000000, " +                   //obere_toleranz
            "       0.000000, " +                   //untere_toleranz
            "       NULL, " +                       //hauptressource
            "       NULL, " +                       //user_code
            "       NULL, " +                       //user_d_01
            "       NULL, " +                       //user_d_02
            "       NULL, " +                       //user_d_03
            "       NULL, " +                       //user_d_04
            "       NULL, " +                       //user_d_05
            "       NULL, " +                       //user_d_06
            "       0, " +                          //user_n_07
            "       0, " +                          //user_n_08
            "       0, " +                          //user_n_09
            "       0, " +                          //user_n_10
            "       0, " +                          //user_n_11
            "       0, " +                          //user_n_12
            "       0, " +                          //user_n_13
            "       0, " +                          //user_n_14
            "       0, " +                          //user_n_15
            "       0, " +                          //user_n_16
            "       0, " +                          //user_n_17
            "       0, " +                          //user_n_18
            "       0, " +                          //user_n_19
            "       0, " +                          //user_n_20
            "       0, " +                          //user_n_21
            "       0, " +                          //user_n_22
            "       0.000000, " +                   //user_f_23
            "       0.000000, " +                   //user_f_24
            "       0.000000, " +                   //user_f_25
            "       0.000000, " +                   //user_f_26
            "       0.000000, " +                   //user_f_27
            "       0.000000, " +                   //user_f_28
            "       NULL, " +                       //user_c_29
            "       NULL, " +                       //user_c_30
            "       NULL, " +                       //user_c_31
            "       NULL, " +                       //user_c_32
            "       NULL, " +                       //user_c_33
            "       NULL, " +                       //user_c_34
            "       NULL, " +                       //user_c_35
            "       NULL, " +                       //user_c_36
            "       NULL, " +                       //user_c_37
            "       NULL, " +                       //user_c_38
            "       NULL, " +                       //user_c_39
            "       NULL, " +                       //user_c_40
            "       NULL, " +                       //user_c_41
            "       NULL, " +                       //user_c_42
            "       NULL, " +                       //user_c_43
            "       NULL, " +                       //user_c_44
            "       NULL, " +                       //user_c_45
            "       NULL, " +                       //user_c_46
            "       NULL, " +                       //user_c_47
            "       NULL, " +                       //user_c_48
            "       NULL, " +                       //user_c_49
            "       NULL, " +                       //user_c_50
            "       NULL, " +                       //user_c_51
            "       NULL, " +                       //user_c_52
            "       NULL, " +                       //user_c_53
            "       NULL, " +                       //user_c_54
            "       NULL, " +                       //user_c_55
            "       NULL, " +                       //user_c_56
            "       NULL, " +                       //user_c_57
            "       NULL, " +                       //user_c_58
            "       NULL, " +                       //user_c_59
            "       NULL, " +                       //user_c_60
            "       NULL, " +                       //user_c_61
            "       NULL, " +                       //user_c_62
            "       NULL, " +                       //user_c_63
            "       NULL, " +                       //user_c_64
            "       NULL, " +                       //user_c_65
            "       NULL, " +                       //user_c_66
            "       0.000000, " +                   //mengen_tol
            "       0.000000, " +                   //mengen_abweichung
            "       NULL, " +                       //chargen_nr
            "       N'N', " +                       //sta_mengenanp
            "       N'N', " +                       //sta_erfasst
            "       NULL, " +                       //tranr
            "       NULL, " +                       //kennz_slos
            "       N'N' " +                        //verb_zaehler
            " Set IDENTITY_INSERT hydadm.mlst_hy OFF;";
        return res;
    }
}