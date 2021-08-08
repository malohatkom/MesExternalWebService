package de.mpdv.SimpleExternalService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
import java.util.*;



public class U_FinOP implements ISimpleExternalService {
    
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
       
	builder.addCol("operation.act.status.led", DataType.STRING);
        builder.addCol("order.id", DataType.STRING);
        builder.addCol("operation.operation", DataType.STRING);
        builder.addCol("order.article", DataType.STRING);
        builder.addCol("operation.designation", DataType.STRING);
        builder.addCol("machine.id", DataType.STRING);
        builder.addCol("machine.designation", DataType.STRING);
        builder.addCol("operation.act.first_logon_ts", DataType.DATETIME);
        builder.addCol("operation.act.last_logoff_ts", DataType.DATETIME);
        builder.addCol("person.id", DataType.INTEGER);
        builder.addCol("person.name", DataType.STRING);
        builder.addCol("operation.act.execution_time", DataType.INTEGER);
        builder.addCol("operation.act.processing_time", DataType.DATETIME);
        builder.addCol("operation.plan.yield.base", DataType.DECIMAL);
        
        SpecialParam u_selStartDate = request.getSpecialParam("operation.act.first_logon_ts");
        SpecialParam u_selEndDate = request.getSpecialParam("operation.act.last_logoff_ts");
        SpecialParam u_datatype = request.getSpecialParam("u_fnop.datatype");
                
        String strSelStartDate;
        String strSelEndDate;
        String strDataType;
        
        strDataType = u_datatype.getValue().toString();
        
        int DataType = 0;
        
        if ("Period".equals(strDataType)) DataType = 0;
        if ("DateList".equals(strDataType)) DataType = 1;
        if ("Mashines".equals(strDataType)) DataType = 2;
        if ("Orders".equals(strDataType)) DataType = 3;
        
        Calendar selStartDate;
        Calendar selEndDate;
        
        if (u_selStartDate != null && u_selEndDate != null)
        {
            selStartDate = (Calendar) u_selStartDate.getValue();
            selEndDate   = (Calendar) u_selEndDate.getValue();
        }
        else
        {
            selStartDate = new GregorianCalendar();
            selEndDate = new GregorianCalendar();
        }
        
        strSelStartDate = calendarToStringIso(selStartDate);
        strSelEndDate = calendarToStringIso(selEndDate);
        
        SpecialParam u_selWorkplace = request.getSpecialParam("machine.id");
        
        ArrayList<String> SelWorkplaceList = new ArrayList<String>();
        String strSelWorkplace;
        if (u_selWorkplace != null)
        {
            SelWorkplaceList.addAll((Collection<String>)u_selWorkplace.getValue()); 
            strSelWorkplace = SelWorkplaceList.toString().trim();
        }
        else
        {
            strSelWorkplace = "CAB_1, CAB_2, CAB_3, CAB_4, CAB_5, SBORKA, ROBOT, ROBOTREZ, SUPRARX, PRES4000, PRES7000, KOG, DROBLIST, TELESK2, VAL16_2, VAL16_6, VAL26_15";
        }
        
        switch (DataType)
        {
            case 0:
                builder.addRow();
                builder.value("");
                builder.value("");
                builder.value("");
                builder.value("");
                builder.value("");
                builder.value("");
                builder.value("");
                builder.value(strSelStartDate);
                builder.value(strSelEndDate);
                builder.value("");
                builder.value("");
                builder.value("");
                builder.value("");
                builder.value("");
                break;
            default:
                Connection conn = conProvider.fetchDbConnection();
                Statement stmt = null;
                ResultSet rs = null;
                String sql = "SELECT * FROM finishedOperationsData ('" + strSelStartDate + "','" + strSelEndDate + "','" + strSelWorkplace + "' , '" + strDataType + "')";
                //write("C:\\java_tst.txt", sql); 
                try {
                    stmt = conn.createStatement();
                    rs = stmt.executeQuery(sql);
                    
                    switch (DataType)
                    {
                        case 1:
                            while (rs.next()) {
                                builder.addRow();
                                builder.value("");
                                builder.value("");
                                builder.value("");
                                builder.value("");
                                builder.value("");
                                builder.value("");
                                builder.value("");
                                builder.value(timestampToCalendar(rs.getTimestamp("op_start")));
                                builder.value(timestampToCalendar(rs.getTimestamp("op_end")));
                                builder.value("");
                                builder.value("");
                                builder.value("");
                                builder.value("");
                                builder.value("");
                            }
                            break;
                            
                        case 2:
                            while (rs.next()) {
                                builder.addRow();
                                builder.value("");
                                builder.value("");
                                builder.value("");
                                builder.value("");
                                builder.value("");
                                builder.value(rs.getString("masch_nr"));
                                builder.value(rs.getString("bez_lang_18"));
                                builder.value(timestampToCalendar(rs.getTimestamp("op_start")));
                                builder.value(timestampToCalendar(rs.getTimestamp("op_end")));
                                builder.value("");
                                builder.value("");
                                builder.value("");
                                builder.value("");
                                builder.value("");
                            }
                            break;
                            
                        case 3:
                            while (rs.next()) {
                                builder.addRow();
                                builder.value(rs.getString("a_status"));
                                builder.value(rs.getString("auftrag_nr"));
                                builder.value(rs.getString("agnr"));
                                builder.value(rs.getString("artikel"));
                                builder.value(rs.getString("ag_bez"));
                                builder.value(rs.getString("masch_nr"));
                                builder.value(rs.getString("bez_lang_18"));
                                builder.value(timestampToCalendar(rs.getTimestamp("op_start")));
                                builder.value(timestampToCalendar(rs.getTimestamp("op_end")));
                                builder.value(rs.getInt("person_nr"));
                                builder.value(rs.getString("name"));
                                builder.value(rs.getInt("work_time"));
                                builder.value(timestampToCalendar(rs.getTimestamp("op_time")));
                                builder.value(rs.getBigDecimal("orders_count"));
                            }
                            break;
                    }
                } catch (SQLException e) {
                    logger.error("Exception while accessing database", e);
                    throw new SesException("lkDbError",
				"Exception while accessing database", e);
                } 
                finally {
                    try {
                        if (rs != null) rs.close();
                    } catch (SQLException e) {
                        logger.error("Exception while closing SQL-ResultSet", e);
                    }
                    try {
                        if (stmt != null) stmt.close();
                    } catch (SQLException e) {
                        logger.error("Exception while closing SQL-Statement", e);
                    }
                    try {
                        if (conn != null) conn.close();
                    } catch (SQLException e) {
                        logger.error("Exception while closing SQL-Connection", e);
                    }
                }
                break;
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
