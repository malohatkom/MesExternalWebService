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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class U_ShiftOrdersRelease implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private boolean debugEnabled = true;
    private final boolean debugMode = true;
 
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory) {
        Connection con = null;
        Statement stmt = null;
        try {
            ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
            this.logger = loggerProvider.fetchLogger(U_ShiftOrdersRelease.class);
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
                con = connProv.fetchDbConnection();
                stmt = con.createStatement();

                String sql = 
                " Update " +
                "   u_shiftorders " +
                " Set " +
                String.format("   s_status = 'R' Where shiftorder_nr = '%s' and id = %s", shiftorder_id.getValue(), shiftorder_key.getValue());

                if (this.debugEnabled)
                {
                    this.logger.debug(sql);
                }
                if (debugMode) System.out.println(sql);
                int affectedRows = stmt.executeUpdate(sql);
                if (debugMode) System.out.println("Update shift orders count = " + affectedRows);
                
                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Released " + affectedRows + " orders");
                }
                
                IHydraCaller caller = (IHydraCaller)factory.fetchUtil("HydraCaller");
                
                sql =
                " Select " +
                "   Distinct oi.[ab_auftrag_nr], " +
                "   uso.[shiftorder_nr], " +
                "   oi.[kostenstelle], " +
                "   IsNull((Select top 1 IsNull(auftrag_nr, '') From hydra1.hydadm.auftrags_bestand Where ag_bez = N'Упаковка' and Left(auftrag_nr, 20) = Left(oi.ab_auftrag_nr, 20)), '') auftrag_nr2, " +
                "   IsNull(oi.[masch_nr], '') masch_nr, " +
                "   oi.[mgruppe], " +
                "   (Select [a_status] From hydra1.hydadm.auftrag_status Where auftrag_nr = oi.[ab_auftrag_nr]) a_status, " +
                "   IsNull(uso.[term_anf_dat], IsNull((Select Max(term_anf_dat) From hydra1.hydadm.u_shiftoperations Where shiftorder_nr = sh.shiftorder_nr), Format(CURRENT_TIMESTAMP,'yyyy-MM-dd 00:00:00.000'))) term_anf_dat, " +
                "   IsNull(uso.[term_anf_zeit],	Case When sh.shift_nr = 'A' Then 28800 When sh.shift_nr = 'B' Then 72000 End) term_anf_zeit, " +
                "   IsNull(uso.[term_end_dat], IsNull((Select Max(term_end_dat) From hydra1.hydadm.u_shiftoperations Where shiftorder_nr = sh.shiftorder_nr), Format(CURRENT_TIMESTAMP,'yyyy-MM-dd 00:00:00.000'))) term_end_dat, " +
                "   IsNull(uso.[term_end_zeit],	Case When sh.shift_nr = 'A' Then 72000 When sh.shift_nr = 'B' Then 28800 End) term_end_zeit " +
                " From " +
                "   hydra1.hydadm.U_OperationInfo oi " +
                String.format("   Left Join hydra1.hydadm.u_shiftorders sh on sh.shiftorder_nr = N'%s' ", shiftorder_id.getValue()) +
                "   Left Outer Join hydra1.hydadm.u_shiftoperations uso on uso.auftrag_nr = oi.ab_auftrag_nr and uso.shiftorder_nr = sh.shiftorder_nr " +
                " Where " +
                "   ak_auftrag_nr in (Select Distinct Left(auftrag_nr, 20) From hydra1.hydadm.u_shiftoperations Where shiftorder_nr = sh.shiftorder_nr) "; 
                
                System.out.println(sql);
                
                if (operation_costcenter != null ? !"".equals(operation_costcenter.getValue()) : false)
                {
                    sql += String.format(" and oi.kostenstelle = N'%s' ", operation_costcenter.getValue());
                }
                        
                /*
                " Select " +
                "      [id], " +
                "      [shiftorder_nr], " +
                "      [kostenstelle], " +
                "      sho.[auftrag_nr], " +
                "      IsNull((Select top 1 IsNull(auftrag_nr, '') From hydra1.hydadm.auftrags_bestand Where ag_bez = N'" + (debugEnabled ? "Окончательная сдача" : "Формирование поддона") + "' and Left(auftrag_nr, 20) = Left(sho.auftrag_nr, 20)), '') auftrag_nr2, " +
                "      IsNull([masch_nr], '') masch_nr," +
                "      [mgruppe], " +
                "      [res_wnr], " +
                "      (Select [a_status] From hydra1.hydadm.auftrag_status Where auftrag_nr = sho.[auftrag_nr]) a_status, " +
                "      [term_anf_dat], " +
                "      [term_anf_zeit], " +
                "      [term_end_dat], " +
                "      [term_end_zeit], " +
                "      [gut_bas], " +
                "      [aus_bas], " +
                "      [bearb], " +
                "      [bearb_date], " +
                "      [bearb_time] " +
                " From " +
                "       [hydra1].[hydadm].[u_shiftoperations] sho " +
                " Where " +
                "   shiftorder_nr = '" + (String)shiftorder_id.getValue() + "' ";
                */
                        
                        
                if (debugMode) System.out.println(sql);
                
                ResultSet DataSet = stmt.executeQuery(sql);

                Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>();
                
                Map<String, List<String>> uv = new HashMap<String, List<String>>();
                Map<String, List<String>> up = new HashMap<String, List<String>>();
                while (DataSet.next())
                {
                    Map<String, String> rows = new HashMap<String, String>();
                    ResultSetMetaData md = DataSet.getMetaData();
                    for (int cInd = 1; cInd <= md.getColumnCount(); cInd++)
                    {
                        rows.put(md.getColumnName(cInd), DataSet.getString(cInd));
                    }
                    data.put(DataSet.getString("ab_auftrag_nr"), rows);
                }
                try {
                    DataSet.close();
                }
                catch (Exception e) {
                    this.logger.error("Could not close ResultSet", e);
                }
                
                if (!data.isEmpty())
                {
                    for(String key: data.keySet())
                    {
                        Map<String, String> rData = data.get(key);
                        caller.addString("DLG", "ANR.ENTSPERREN");
                        caller.addString("ANR.ANR", key);
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
                            if (debugMode) System.out.println(String.format("RET=%s KT=%s LT=%s ANR %s", retVal.getReturnCode(), retVal.getLongText(), retVal.getShortText(), key));
                        }
                        else
                        {
                            if (debugMode) System.out.println(String.format("RET=%s unblock OP %s", retVal.getReturnCode(), key));
                            sql = 
                            " Update " +
                            "	hydra1.hydadm.auftrags_bestand " +
                            " Set " +
                            "	term_anf_dat = '" + rData.get("term_anf_dat") + "', " +
                            "	term_anf_zeit = " + rData.get("term_anf_zeit") + ", " +
                            "	term_end_dat = '" + rData.get("term_end_dat") + "', " +
                            "	term_end_zeit = " + rData.get("term_end_zeit") + " " +
                            " Where " +
                            "	auftrag_nr = '" + key + "' ";
                            //if (debugMode) System.out.println(sql);
                            int r = stmt.executeUpdate(sql);
                            //if (debugMode) System.out.println(" update auftrags_bestand rows count = " + r);
                        }
                        //if (debugMode) System.out.println("a_status = " + rData.get("a_status"));
                        if ("E".equals(rData.get("a_status")))
                        {
                            caller.addString("DLG", "ANR.SETSTATUS");
                            caller.addString("ANR.ANR", key);
                            caller.addString("ANR.TABLE", "A");
                            caller.addString("ANR.ATYP", "AG");
                            caller.addString("MOD", "R");
                            caller.addString("PNR", request.getLangId());
                            caller.addString("ZEI", "now");
                            caller.addString("DAT", "today");
                            caller.addString("LANG", "18");
                            result = caller.callHydra();
                            retVal = result.getReturnValue();
                            if (retVal.getReturnCode() != 0) 
                            {
                                if (debugMode) System.out.println(String.format("RET=%s KT=%s LT=%s ANR %s", retVal.getReturnCode(), retVal.getShortText(), retVal.getLongText(), key));
                            }
                            else
                            {
                                if (debugMode) System.out.println(String.format("RET=%s Reactivate OP %s", retVal.getReturnCode(), key));
                            }
                        }
                        //if (debugMode) System.out.println("<<1>>");                    
                        //if (debugMode) System.out.println("masch_nr = " + rData.get("masch_nr")); 
                        //if (debugMode) System.out.println("<<2>>");   
                        if (rData.get("masch_nr").contains((debugMode ? "TIP_UP_" : "LM")))
                        {
                            //if (debugMode) System.out.println("auftrag_nr2 = [" + rData.get("auftrag_nr2") + "]");
                            if (!"".equals(rData.get("auftrag_nr2")))
                            {
                                //if (debugMode) System.out.println("<<3>>");                    
                                if (!uv.containsKey(rData.get("auftrag_nr2")))
                                {
                                    List<String> lm = new ArrayList<String>();
                                    uv.put(rData.get("auftrag_nr2"), lm);
                                }
                                //if (debugMode) System.out.println("<<<4>>");
                                if (uv.containsKey(rData.get("auftrag_nr2")))
                                {
                                    List<String> lm = uv.get(rData.get("auftrag_nr2"));
                                    if (lm.indexOf(rData.get("masch_nr")) == -1) 
                                        lm.add(rData.get("masch_nr"));
                                    //if (debugMode) System.out.println("<<<5>>");
                                    uv.put(rData.get("auftrag_nr2"), lm);
                                }
                                //if (debugMode) System.out.println("<<<6>>");
                            }
                        }
                        
                        if (rData.get("masch_nr") == .contains((debugMode ? "UCHUP" : "LM")))
                        {
                            //if (debugMode) System.out.println("auftrag_nr2 = [" + rData.get("auftrag_nr2") + "]");
                            if (!"".equals(rData.get("auftrag_nr2")))
                            {
                                //if (debugMode) System.out.println("<<3>>");                    
                                if (!uv.containsKey(rData.get("auftrag_nr2")))
                                {
                                    List<String> lm = new ArrayList<String>();
                                    uv.put(rData.get("auftrag_nr2"), lm);
                                }
                                //if (debugMode) System.out.println("<<<4>>");
                                if (uv.containsKey(rData.get("auftrag_nr2")))
                                {
                                    List<String> lm = uv.get(rData.get("auftrag_nr2"));
                                    if (lm.indexOf(rData.get("masch_nr")) == -1) 
                                        lm.add(rData.get("masch_nr"));
                                    //if (debugMode) System.out.println("<<<5>>");
                                    uv.put(rData.get("auftrag_nr2"), lm);
                                }
                                //if (debugMode) System.out.println("<<<6>>");
                            }
                        }
                    }
                }
                //if (debugMode) System.out.println(uv.toString());
                if (!uv.isEmpty())
                {
                    for (String key: uv.keySet())
                    {
                        sql =
                        " Delete " +
                        "   hydra1.hydadm.mlst_hy " +
                        " Where " +
                        "   auftrag_nr = '" + key + "'";
                        //if (debugMode) System.out.println(sql);
                        stmt.executeUpdate(sql);
                        for (String mnr: uv.get(key))
                        {
                            sql = 
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
                            "       '" + key + "'," +               //auftrag_nr 
                            "       '00000000812', " +              //artikel
                            "       '" + mnr + "', " +                  //bez_1
                            "       '', " +                         //bez_2
                            "       0.000000, " +                   //soll_menge
                            "       NULL, " +                       //soll_einh
                            "       N'M', " +                       //kennz
                            "       0.000000, " +                   //ist_menge
                            "       " +  (debugEnabled ? mnr.replace("TIP_UP_", "") : mnr.replace("LM", "")) + ", " +   //pos
                            "       N'GP_Колесо', " +               //hz_typ
                            "       N'Mat" + Integer.toString(Integer.parseInt((debugEnabled ? mnr.replace("TIP_UP_", "") : mnr.replace("LM", "")))) + "', " +  //artikel_bez
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
                            "       N'J', " +                          //ersetzbar
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
                            "       N'" + request.getUserId() + "', " + //bearb
                            "       Format(CURRENT_TIMESTAMP, 'yyyy-MM-dd 00:00:00.000'), " +   //bearb_date
                            "       DATEDIFF(SECOND, 0,CONVERT(time, CURRENT_TIMESTAMP)), " +   //bearb_time
                            "       NULL, " +                       //ist_menge_einh
                            "       16.000000, " +                  //ist_menge_sek
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
                            "       N'N'; " +                        //verb_zaehler
                            " Set IDENTITY_INSERT hydadm.mlst_hy OFF;";
                            //if (debugMode) System.out.println(sql);
                            int i = stmt.executeUpdate(sql);
                            if (debugMode) System.out.println("insert components count = " + i + " anr " + key);
                        }
                    }
                }
            }
        }
        catch (SQLException e) {
            if (debugMode) System.out.println("error = " + e.getMessage());
            throw new SesException("lkDbError", "Error at DB access: " + e.getMessage(), e, new String[0]);
        } 
        catch (NumberFormatException e) {
            if (debugMode) System.out.println("error = " + e.getMessage());
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
        return null;
    }
}