package de.mpdv.SimpleExternalService.UToolsUsing;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UToolsUsing implements ISimpleExternalService {

    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory)
    {
        final ISdiLoggerProvider loggerProvider = factory.fetchUtil("LoggerProvider");
    	final ISdiLogger logger = loggerProvider.fetchLogger(this.getClass());
    	
    	final IDbConnectionProvider conProvider = factory.fetchUtil("DbConnectionProvider");
        
    	IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
    	
        builder.addCol("u_tool.article", DataType.STRING);
        builder.addCol("u_tool.description", DataType.STRING);
        builder.addCol("u_tool.usingcount", DataType.INTEGER);
        builder.addCol("u_tool.startusedate", DataType.STRING);
        builder.addCol("u_tool.endusedate", DataType.STRING);

	Connection conn = conProvider.fetchDbConnection();
	Statement stmt = null;
	ResultSet rs = null;
         
        SpecialParam useStartDate = request.getSpecialParam("u_tool.startusedate");
        SpecialParam useEndDate = request.getSpecialParam("u_tool.endusedate");
        
        String strPeriodStartDate = java.util.Calendar.getInstance().getTime().toString();
        String strPeriodEndDate = java.util.Calendar.getInstance().getTime().toString();        
        
        if (useStartDate != null && useEndDate != null)
        {
            SimpleDateFormat MOCFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            SimpleDateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            String reqStartDate = java.lang.String.valueOf(useStartDate.getValue());
            String reqEndDate = java.lang.String.valueOf(useEndDate.getValue());
            
            Date datPeriodStartDate;
                try {
                    datPeriodStartDate = MOCFormat.parse(reqStartDate);
                    strPeriodStartDate =  sqlFormat.format(datPeriodStartDate);
                } catch (ParseException ex) {
                    Logger.getLogger(UToolsUsing.class.getName()).log(Level.SEVERE, null, ex);
                }
            Date datPeriodEndDate;
                try {
                    datPeriodEndDate = MOCFormat.parse(reqEndDate);
                    
                    strPeriodEndDate =  sqlFormat.format(datPeriodEndDate);
                } catch (ParseException ex) {
                    Logger.getLogger(UToolsUsing.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        
        
        String sql = 
            "SELECT artikel, artikel_bez, UsesCount "
            + "FROM ShowUsingWNR "
            + "( '" + strPeriodStartDate + "' , '" + strPeriodEndDate + "' )";
        
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                builder.addRow();
		builder.value(rs.getString("artikel"));
                builder.value(rs.getString("artikel_bez"));
                builder.value(Integer.valueOf(rs.getString("UsesCount")));
                builder.value(strPeriodStartDate);
                builder.value(strPeriodEndDate);
            }
	} catch (SQLException e) {
		logger.error("Exception while accessing database", e);
		throw new SesException("lkDbError", "Exception while accessing database", e);
	} finally {
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
	return new SesResultBuilder().addDataTable(builder.build()).build();
    }
    
    public static String calendarToStringIso(Calendar cal)
    {
        if (cal == null) {
          return "null";
        }
        else
        {
           SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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