package de.mpdv.SimpleExternalService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

// OLD: import de.mpdv.sdi.data.DataTableBuilder;
import de.mpdv.sdi.data.DataType;
import de.mpdv.sdi.data.SesContext;
import de.mpdv.sdi.data.SesException;
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
import java.io.*;
import java.text.SimpleDateFormat;



public class ShiftPlanOrder implements ISimpleExternalService {
    
    public static void write(String fileName, String text) {
        //Определяем файл
        File file = new File(fileName);

        try {
            //проверяем, что если файл не существует то создаем его
            if(!file.exists()){
                file.createNewFile();
            }

            //PrintWriter обеспечит возможности записи в файл
            PrintWriter out = new PrintWriter(file.getAbsoluteFile());

            try {
                //Записываем текст у файл
                out.print(text);
            } finally {
                //После чего мы должны закрыть файл
                //Иначе файл не запишется
                out.close();
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory)
    {
        final ISdiLoggerProvider loggerProvider = factory.fetchUtil("LoggerProvider");
    	final ISdiLogger logger = loggerProvider.fetchLogger(this.getClass());
    	
    	final IDbConnectionProvider conProvider = factory.fetchUtil("DbConnectionProvider");
   
        
        
    	IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
    	
		builder.addCol("u_maschine.designation", DataType.STRING);
                builder.addCol("u_group", DataType.STRING);
                builder.addCol("u_group.designation", DataType.STRING);
                builder.addCol("u_operation.plan.workplace", DataType.STRING);
                builder.addCol("u_artikel.designation", DataType.STRING);
                builder.addCol("u_order.designation", DataType.STRING);
                builder.addCol("u_operation.number", DataType.STRING);
                builder.addCol("u_operation.designation", DataType.STRING);
                builder.addCol("u_operation.operation_start_ts", DataType.DATETIME);
                builder.addCol("u_operation.operation_end_ts", DataType.DATETIME);
                builder.addCol("u_operation.scheduled_start_ts", DataType.DATETIME);
                builder.addCol("u_operation.scheduled_end_ts", DataType.DATETIME);
                builder.addCol("u_shift.number", DataType.STRING);
                builder.addCol("u_operation.act.status", DataType.STRING);
                builder.addCol("u_operation.costcenter", DataType.STRING);
                builder.addCol("u_order.customer", DataType.STRING);

	        Connection conn = conProvider.fetchDbConnection();
		Statement stmt = null;
		ResultSet rs = null;
                String sql = null;
                String partSql = "SELECT "
                        + "mas.bez_lang_18, "
                        + "ab.mgruppe, "
                        + "grp.bezeichnung, "
                        + "ab.masch_nr, "
                        + "ab.artikel_bez, " //+ "oab.artikel, "
                        + "ab.aunr, "
                        + "ab.agnr, "
                        + "ab.ag_bez,  "
                        + "get_datetime(e_anmeld_dat, e_anmeld_zeit) LastLogOn,"
                        + "get_datetime(prot_dat, prot_zeit) LastLogOff," 
                        + "get_datetime(ab.term_anf_dat, ab.term_anf_zeit) tss, "
                        + "get_datetime(ab.term_end_dat, ab.term_end_zeit) tse, "
                        + "getShift(ab.term_anf_dat, ab.term_anf_zeit, ab.term_end_dat, ab.term_end_zeit) shiftNo, "
                        + "ast.a_status "
                        + ",mas.kostenstelle "
                        + ",ab.kunden_bez"
                        + " FROM auftrags_bestand ab "
                        + "LEFT OUTER JOIN auftrag_status ast ON ab.auftrag_nr = ast.auftrag_nr "
                        + "LEFT OUTER JOIN maschinen mas ON ast.masch_nr=mas.masch_nr "
                        + "LEFT OUTER JOIN auftrags_bestand oab ON left (ab.auftrag_nr,20)=oab.auftrag_nr "
                        + "LEFT OUTER JOIN hy_gruppen grp ON ab.mgruppe=grp.gruppe "
                        + " AND ab.masch_nr NOT IN (SELECT masch_name FROM u_masch_blacklist)";
                
                SpecialParam u_selStartDate = request.getSpecialParam("u_operation.scheduled_start_ts");
                SpecialParam u_selEndDate = request.getSpecialParam("u_operation.scheduled_end_ts");
		
                if (u_selStartDate != null && u_selEndDate != null)
                {
                    Calendar selStartDate= (Calendar) u_selStartDate.getValue();
                    Calendar selEndDate= (Calendar) u_selEndDate.getValue();
                    
                    String strSelStartDate = calendarToStringIso(selStartDate);
                    String strSelEndDate = calendarToStringIso(selEndDate);
                    
//                    write("C://testJavaCode.txt", strSelStartDate);
                    if (strSelStartDate != null)
                    {
                           //" AND mas.bez_lang_18 IS NOT NULL AND ab.agnr IS NOT NULL AND get_datetime(term_anf_dat, term_anf_zeit) >= '" + strSelStartDate + "'" + " AND get_datetime(term_end_dat, term_end_zeit) < '" + strSelEndDate + "'" + "order get_datetime(term_anf_dat, term_anf_zeit) ASC";
                        sql = partSql //+ " AND mas.bez_lang_18 IS NOT NULL AND ab.agnr IS NOT NULL AND get_datetime(term_anf_dat, term_anf_zeit) >= '" + strSelStartDate + "' AND get_datetime(term_end_dat, term_end_zeit) < '" + strSelEndDate + "' ORDER BY mas.bez_lang_18, get_datetime(term_anf_dat, term_anf_zeit) ASC"; 
+ " AND mas.bez_lang_18 IS NOT NULL AND ab.agnr IS NOT NULL AND get_datetime(ab.term_anf_dat, ab.term_anf_zeit) >= '" + strSelStartDate + "' AND get_datetime(ab.term_end_dat, ab.term_end_zeit) < '" + strSelEndDate + "' ORDER BY CASE WHEN ab.masch_nr = 'SUPRARX' THEN 1 WHEN ab.masch_nr = 'METALREZ' THEN 2 WHEN ab.masch_nr = 'VAL16-2' THEN 3 WHEN ab.masch_nr = 'VAL16-6' THEN 4 WHEN ab.masch_nr = 'VAL26-15' THEN 5 WHEN ab.masch_nr = 'ROBOT' THEN 6 WHEN ab.masch_nr = 'ROBOTREZ' THEN 7 WHEN ab.masch_nr = 'KOG' THEN 8 WHEN ab.masch_nr = 'PRES4000' THEN 9 WHEN ab.masch_nr = 'PRES7000' THEN 10 WHEN ab.masch_nr = 'SBORKA' THEN 11 WHEN ab.masch_nr = 'CAB_1'  THEN 12 WHEN ab.masch_nr = 'CAB_2'  THEN 13 WHEN ab.masch_nr = 'CAB_3'  THEN 14 WHEN ab.masch_nr = 'CAB_4'  THEN 15 WHEN ab.masch_nr = 'CAB_5' THEN 16 WHEN ab.masch_nr = 'STND_TRN' THEN 17 WHEN ab.masch_nr = 'STND_OTV' THEN 18 WHEN ab.masch_nr = 'STND_D_P' THEN 19 WHEN ab.masch_nr = 'STND_POL' THEN 20 WHEN ab.masch_nr = 'STENDREZ' THEN 21 WHEN ab.masch_nr = 'KALIBR15'  THEN 22 WHEN ab.masch_nr = 'KALIBR30' THEN 23 WHEN ab.masch_nr = 'UZK' THEN 24 WHEN ab.masch_nr = 'REMZONA1'  THEN 25 WHEN ab.masch_nr = 'REMZONA2'  THEN 26 WHEN ab.masch_nr = 'REMZONA3'  THEN 27 WHEN ab.masch_nr = 'REMZONA4' THEN 28 WHEN ab.masch_nr = 'RUCHSVAR' THEN 29 WHEN ab.masch_nr = 'TIP_UP_1'  THEN 30 WHEN ab.masch_nr = 'TIP_UP_2' THEN 31 WHEN ab.masch_nr = 'OTPUSK_1'  THEN 32 WHEN ab.masch_nr = 'OTPUSK_2' THEN 33 WHEN ab.masch_nr = 'MEX_1G_1'  THEN 34 WHEN ab.masch_nr = 'MEX_3G_1'  THEN 35 WHEN ab.masch_nr = 'MEX_3G_2' THEN 36 WHEN ab.masch_nr = 'FINSDACH' THEN 37 WHEN ab.masch_nr = 'UCHUP' THEN 38 ELSE 39 END, get_datetime(ab.term_anf_dat, ab.term_anf_zeit) ASC";
                    }
                }
                else
                    {
                           sql = partSql + " AND mas.bez_lang_18 IS NOT NULL AND ab.agnr IS NOT NULL ORDER BY mas.bez_lang_18, get_datetime(ab.term_anf_dat, ab.term_anf_zeit) ASC";  
                           
                           String T = " AND mas.kostenstelle IS NOT NULL AND ab.agnr IS NOT NULL "
                                + " ORDER BY"
                                + "CASE "
                        + "WHEN ab.masch_nr = 'SUPRARX' THEN 1 " 
			+ "WHEN ab.masch_nr = 'METALREZ' THEN 2 "
                        + "WHEN ab.masch_nr = 'ROBOTREZ' THEN 3 " 
                        + "WHEN ab.masch_nr = 'VAL16-2' THEN 4 " 
                        + "WHEN ab.masch_nr = 'VAL16-6' THEN 5 " 
                        + "WHEN ab.masch_nr = 'VAL26-15' THEN 6 " 
                        + "WHEN ab.masch_nr = 'ROBOT' THEN 7 " 
                        + "WHEN ab.masch_nr = 'KOG' THEN 8 " 
                        + "WHEN ab.masch_nr = 'PRES4000' THEN 9 " 
                        + "WHEN ab.masch_nr = 'PRES7000' THEN 10 "
                        + "WHEN ab.masch_nr = 'STND_POL' THEN 11 "
                        + "WHEN ab.masch_nr = 'SBORKA' THEN 12 "
                        + "WHEN ab.masch_nr = 'TELESKP2' THEN 13 "                                   
                        + "WHEN ab.masch_nr = 'CAB_1'  THEN 14 "
                        + "WHEN ab.masch_nr = 'CAB_2'  THEN 15  "
                        + "WHEN ab.masch_nr = 'CAB_3'  THEN 16  "
                        + "WHEN ab.masch_nr = 'CAB_4'  THEN 17  "
                        + "WHEN ab.masch_nr = 'CAB_5' THEN 18  "
                        + "WHEN ab.masch_nr = 'STND_TRN' THEN 19 " 
                        + "WHEN ab.masch_nr = 'STND_OTV' THEN 20  "
                        + "WHEN ab.masch_nr = 'STND_D_P' THEN 21  "
                        + "WHEN ab.masch_nr = 'STENDREZ' THEN 22  "                                   
                        + "WHEN ab.masch_nr = 'KALIBR15'  THEN 23  "
                        + "WHEN ab.masch_nr = 'KALIBR30' THEN 24  "
                        + "WHEN ab.masch_nr = 'VIK' THEN 25  "
                        + "WHEN ab.masch_nr = 'UZK' THEN 26  "
                        + "WHEN ab.masch_nr = 'RENTGEN1' THEN 27  "
                        + "WHEN ab.masch_nr = 'REMZONA1'  THEN 28  "
                        + "WHEN ab.masch_nr = 'REMZONA2'  THEN 29  "
                        + "WHEN ab.masch_nr = 'REMZONA3'  THEN 30  "
                        + "WHEN ab.masch_nr = 'REMZONA4' THEN 31  "
                        + "WHEN ab.masch_nr = 'RUCHSVAR' THEN 32  "
                        + "WHEN ab.masch_nr = 'TIP_UP_1'  THEN 33  "
                        + "WHEN ab.masch_nr = 'TIP_UP_2' THEN 34  "
                        + "WHEN ab.masch_nr = 'OTPUSK_1'  THEN 35  "
                        + "WHEN ab.masch_nr = 'OTPUSK_2' THEN 36  "
                        + "WHEN ab.masch_nr = 'MEX_1G_1'  THEN 37  "
                        + "WHEN ab.masch_nr = 'MEX_3G_1'  THEN 38  "
                        + "WHEN ab.masch_nr = 'MEX_3G_2' THEN 39  "
                        + "WHEN ab.masch_nr = 'FINSDACH' THEN 40  "
                        + "WHEN ab.masch_nr = 'FIN_VIK' THEN 41  "
                        + "WHEN ab.masch_nr = 'FIN_UZK' THEN 42  "
                        + "WHEN ab.masch_nr = 'FIN_MARK' THEN 43  "
                        + "WHEN ab.masch_nr = 'UCHUP' THEN 44  "
                        + "ELSE 39 END, "
                        + "get_datetime(ab.term_anf_dat, ab.term_anf_zeit) ASC";
                    }
                
                try {
			
                        stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
                        
			while (rs.next()) {
				builder.addRow();
				builder.value(rs.getString("bez_lang_18"));
                                builder.value(rs.getString("mgruppe"));
                                builder.value(rs.getString("bezeichnung"));
                                builder.value(rs.getString("masch_nr"));
                                builder.value(rs.getString("artikel_bez")); //builder.value(rs.getString("artikel"));
                                builder.value(rs.getString("aunr"));
                                builder.value(rs.getString("agnr"));
                                builder.value(rs.getString("ag_bez"));
                                
                                builder.value(timestampToCalendar(rs.getTimestamp("LastLogOn")));
 				builder.value(timestampToCalendar(rs.getTimestamp("LastLogOff")));
                                builder.value(timestampToCalendar(rs.getTimestamp("tss")));
 				builder.value(timestampToCalendar(rs.getTimestamp("tse")));
                                builder.value(rs.getString("shiftNo"));
                                //builder.value(rs.getString("a_status"));
                                if ("V".equals(rs.getString("a_status")))
                                {
                                    builder.value("Запланировано");
                                }
                                else if ("L".equals(rs.getString("a_status")))
                                {
                                    builder.value("В работе");
                                }
                                else if ("U".equals(rs.getString("a_status")))
                                {
                                    builder.value("Прервано");
                                }
                                else if ("F".equals(rs.getString("a_status")))
                                {
                                    builder.value("Автоматическое прерывание");
                                }
                                else if ("E".equals(rs.getString("a_status")))
                                {
                                    builder.value("Выполнено");
                                }
                                else if ("D".equals(rs.getString("a_status")))
                                {
                                    builder.value("Удалена");
                                }
                                else if ("S".equals(rs.getString("a_status")))
                                {
                                    builder.value("Неизвестное состояние");
                                }
                                builder.value(rs.getString("kostenstelle"));
                                builder.value(rs.getString("kunden_bez"));
                                
			}
	} catch (SQLException e) {
		logger.error("Exception while accessing database", e);
		throw new SesException("lkDbError",
				"Exception while accessing database", e);
	} finally {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			logger.error("Exception while closing SQL-ResultSet", e);
		}
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException e) {
			logger.error("Exception while closing SQL-Statement", e);
		}
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			logger.error("Exception while closing SQL-Connection", e);
		}
	}

		return new SesResultBuilder().addDataTable(builder.build()).build();

    }
    
        public static String calendarToStringIso(Calendar cal)
    {
        if (cal == null) {
          return "null";
        }
        else
        {
           SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
           return dateFormat.format(cal.getTime());
        }
        
    }
  
    public static Calendar timestampToCalendar(Timestamp ts)
    {
        if (ts == null) 
        {
          return null;
        }
        //Calendar cal = new GregorianCalendar(1970, 0, 1, 0, 0, 0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.setTimeInMillis(ts.getTime());
        return cal;
    }
}