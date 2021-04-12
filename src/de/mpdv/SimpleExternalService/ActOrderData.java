package de.mpdv.SimpleExternalService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.nio.file.Files;
//import java.nio.file.Paths;

public class ActOrderData implements ISimpleExternalService {
	
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory)
    {
    	final ISdiLoggerProvider loggerProvider = factory.fetchUtil("LoggerProvider");
    	final ISdiLogger logger = loggerProvider.fetchLogger(this.getClass());
    	
    	final IDbConnectionProvider conProvider = factory.fetchUtil("DbConnectionProvider");
   
    	IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
    	
	builder.addCol("operation.operation", DataType.STRING);
	builder.addCol("order.articledesignation", DataType.STRING);
	builder.addCol("order.article", DataType.STRING);
	builder.addCol("order.costcenter", DataType.STRING);
	builder.addCol("order.id", DataType.STRING);
	builder.addCol("order.tool", DataType.STRING  );
	builder.addCol("order.userfield66", DataType.STRING);
        builder.addCol("person.name", DataType.STRING);
        builder.addCol("person.email_company", DataType.STRING);
        builder.addCol("person.telephone_number.public", DataType.STRING);
        builder.addCol("note.data", DataType.STRING);
        builder.addCol("batch.technical_information", DataType.STRING);
        builder.addCol("person.infotext20", DataType.STRING);
        builder.addCol("operation.latest_end_ts", DataType.STRING);
        
        String strorder = "";
        String stroperation  = "";
        String strdate = "!";
        SpecialParam u_order = request.getSpecialParam("order.id");
       	SpecialParam u_operation = request.getSpecialParam("operation.operation");
        SpecialParam u_date = request.getSpecialParam("operation.latest_end_ts");
        
        boolean TestData = false;
        
        if (u_order != null) strorder= (String) u_order.getValue();
        if (u_operation != null) stroperation= (String) u_operation.getValue();
        GregorianCalendar OrderDate = null;
        String DateData = "";
        if (u_date != null) 
            OrderDate = (GregorianCalendar) u_date.getValue();
        else
            strdate = "u_date == null";        
        if (OrderDate != null)
        {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            fmt.setCalendar(OrderDate);
            strdate = fmt.format(OrderDate.getTime());
        }
        
	Connection conn = conProvider.fetchDbConnection();

        Statement stmt = null;
	ResultSet rs = null;

        String sql= String.format("SELECT * FROM DataForAct ('%s', '%s', '%s')", strorder.trim(), stroperation.trim(), strdate.trim());
        
        
            if (TestData)
            {
                builder.addRow();
                builder.value("");
                builder.value("");
                builder.value(strorder);
                builder.value(stroperation);
                builder.value(DateData);
                builder.value("");
                builder.value("");
                builder.value("");
                builder.value("");
                builder.value("");
                builder.value(sql);
                builder.value("");
                builder.value("");
                builder.value("");
            }
        
        
        try 
        {
            if (!TestData)
            {
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);
                while (rs.next()) 
                {
                    builder.addRow();
                    builder.value(rs.getString("agnr") != null ? rs.getString("agnr").trim() : "");
                    builder.value(rs.getString("artikel_bez") != null ? rs.getString("artikel_bez").trim() : "");
                    builder.value(rs.getString("artikel") != null ? rs.getString("artikel").trim() : "");
                    builder.value(rs.getString("kostentraeger") != null ? rs.getString("kostentraeger").trim() : "");
                    builder.value(rs.getString("auftrag_nr") != null ? rs.getString("auftrag_nr").trim() : "");
		
                    String Detector = "";
                    String Indicator = "";
                    
                    String strRGK = rs.getString("rgk") != null ? rs.getString("rgk") : "|";
                    

                    
                    if (strRGK != null)
                    {
                        String[] RgkData = strRGK.split("[|]"); 
                        if (null != RgkData)
                        {
                            if (RgkData.length != 0)
                            {
                                for(String TechData:RgkData)
                                {
                                    if (TechData.contains("[D]"))
                                    {
                                        Detector = TechData.replace("[D]", "");
                                    }
                        
                                    if (TechData.contains("[I]"))
                                    {
                                        Indicator = TechData.replace("[I]", "");
                                    }
                                }
                            }
                        }
                        else
                        {
                            Detector = "null != RgkData";
                        }
                    }
                
                    builder.value(Detector + "|" + Indicator);
                    String TU_Data = rs.getString("user_c_66") != null ? rs.getString("user_c_66").trim() : "";
                    if (TU_Data.contains("ТУ"))
                    {
                        TU_Data = TU_Data.substring(TU_Data.indexOf("ТУ"));
                    }
                    builder.value(TU_Data);
                    builder.value(rs.getString("person_name") != null ? rs.getString("person_name").trim() : "");
                    builder.value(rs.getString("email_firma") != null ? rs.getString("email_firma").trim() : "");
                    builder.value(rs.getString("telefon_privat") != null ? rs.getString("telefon_privat").trim() : "");
                    builder.value(rs.getString("longname") != null ? rs.getString("longname").trim() : "");
                    builder.value(rs.getString("tech_info") != null ? rs.getString("tech_info").trim() : "");
                    builder.value(rs.getString("code") != null ? rs.getString("code").trim() : "");
                    builder.value(rs.getString("opdate") != null ? rs.getString("opdate").trim() : "");
                }
            }
	} 
        catch (SQLException e) 
        {
            builder.addRow();
            builder.value(sql);
            builder.value("");
            builder.value(e.getLocalizedMessage());
            builder.value(e.getErrorCode());
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");
            
            //e.printStackTrace();
            logger.error("Exception while accessing database", e);
            throw new SesException("lkDbError",	"Exception while accessing database", e);
                
	} 
        finally 
        {
            try 
            {
                if (rs != null) rs.close();
            } 
            catch (SQLException e) {logger.error("Exception while closing SQL-ResultSet", e);}
		
            try 
            {
                if (stmt != null) stmt.close();
            } 
            catch (SQLException e) {logger.error("Exception while closing SQL-Statement", e);}
		
            try 
            {
                if (conn != null) {conn.close();}
            } 
            catch (SQLException e) {logger.error("Exception while closing SQL-Connection", e);}
	}
	return new SesResultBuilder().addDataTable(builder.build()).build();
    }
}



