package de.mpdv.SimpleExternalService.PZ;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Clob;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class U_PZData implements ISimpleExternalService 
{
    public static String clobStringConversion(Clob clb) throws IOException, SQLException
     {
       if (clb == null)
      return  "";
              
       StringBuilder str = new StringBuilder();
       String strng;
                
      
     BufferedReader bufferRead = new BufferedReader(clb.getCharacterStream());
     
       while ((strng=bufferRead .readLine())!=null)
        str.append(strng);
     
       return str.toString();
     }        

    
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
                out.println(text);
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
        builder.addCol("order.id", DataType.STRING);
	builder.addCol("operation.operation", DataType.STRING);
        builder.addCol("operation.designation", DataType.STRING);
        builder.addCol("person.name", DataType.STRING);
        builder.addCol("person.function", DataType.STRING);
	builder.addCol("operation.latest_end_ts", DataType.DATETIME  );
        builder.addCol("qmcharacteristic.designation", DataType.STRING);
        builder.addCol("qmcharacteristic.lower_tolerance_limit", DataType.STRING);
        builder.addCol("qmcharacteristic.target_value", DataType.STRING);
	builder.addCol("qmcharacteristic.upper_tolerance_limit", DataType.STRING  );
	builder.addCol("qmsinglevalue.measured_value.value", DataType.STRING  );
        builder.addCol("inspectionpoint.ud_code", DataType.STRING );
                
	Connection conn = conProvider.fetchDbConnection();
		
	Statement stmt = null;
	ResultSet rs = null;
		
	SpecialParam u_order = request.getSpecialParam("order.id");
	String strorder= (String) u_order.getValue();
                
	/*
        String sql = "SELECT auftrag_nr" +
        " ,agnr" +
        " ,ag_bez" +
        " ,name" +
        " ,taetigkeit" +
        " ,taetigkeit_2" +
        " ,end_ts" +
        " ,merkmal_bez_18" +
        " ,otg" +
        " ,sw" +
        " ,utg" +
        " ,status" +
        " ,status_info" +
        " ,ppunkt_ve_code" +
        " ,messwert" +
        " FROM NewPZ where auftrag_nr = '" + strorder + "' order by agnr";
        */
        
        String sql = "EXECUTE FromPDMDataFull '"+ strorder +"'";
        
        try 
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) 
            {
                String OPName = rs.getString("ag_bez");
                String MKName = rs.getString("nmkp"); //"merkmal_bez_18");

                String Result = null; //rs.getString(//"messwert");
                String LValue = rs.getString("mizn"); //"utg");
                String NValue = rs.getString("nozn"); //"sw");
                String UValue = rs.getString("mazn"); //"otg");
                
                //String FOrder = clobStringConversion(rs.getClob("FORDER"));
                
                if (null == Result)
                {
                    if (NValue != null) 
                        Result = NValue;
                    else
                    {
                        if (LValue != null && UValue != null)
                        {
                            Result = ((Float)((Float.parseFloat(LValue) + Float.parseFloat(UValue)) / 2)).toString();
                        }
                        else
                        {
                            if (LValue != null && UValue == null) Result = LValue;
                            if (LValue == null && UValue != null) Result = UValue;
                        }
                    }
                }

                if (OPName != null && MKName != null)
                {
                    if (OPName.contains("Сварка") && MKName.contains("Температура метал"))
                    {
                        Result = Integer.toString(12 + new Random().nextInt(3));
                    }
                }
                
                if (Result == null) Result = rs.getString("znkp");
                
                //if (Result != null && !"".equals(Result))
                //{
                    builder.addRow();
                    builder.value(rs.getString("auftrag_nr"));
                    builder.value(rs.getString("agnr"));
                    builder.value(rs.getString("ag_bez"));
                    builder.value(rs.getString("name"));
                    builder.value(rs.getString("taetigkeit")); //taetigkeit_2
                    builder.value(timestampToCalendar(rs.getTimestamp("datatime"))); //"end_ts")));
                    builder.value(rs.getString("nmkp")); //merkmal_bez_18"));
                    builder.value(rs.getString("mizn")); //"utg"));
                    builder.value(rs.getString("nozn")); //"sw"));
                    builder.value(rs.getString("mazn")); //"otg"));
                    //builder.value(rs.getString("messwert"));
                    builder.value(Result);
                    builder.value(""); //rs.getString("ppunkt_ve_code"));
                //}
            }
	} 
        catch (SQLException e) 
        {
            PrintWriter writer = null;    
            try {
                writer = new PrintWriter("C:\\Windows\\Temp\\U_PZData_java.log", "UTF-8");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(U_PZData.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(U_PZData.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally
            {
                if (writer != null)
                {
                    writer.println(sql);
                    writer.println(e.getLocalizedMessage());
                    //writer.println(e.getMessage());
                    //writer.println(e.getSQLState());
                    writer.close();    
                }
            }
                
            //write("C:\\U_PZData_java.log", sql);
            //write("C:\\U_PZData_java.log", e.getLocalizedMessage());
            //write("C:\\U_PZData_java.log", e.getMessage());
            //write("C:\\U_PZData_java.log", e.getSQLState());
            //e.printStackTrace();
            logger.error("Exception while accessing database", e);
            throw new SesException("lkDbError",	"Exception while accessing database", e);
	//} catch (IOException ex) { 
            //Logger.getLogger(U_PZData.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally 
        {
            try 
            {
                if (rs != null) rs.close();
            } 
            catch (SQLException e) 
            {
                PrintWriter writer = null;    
                try {
                    writer = new PrintWriter("C:\\Windows\\Temp\\U_PZData_java.log", "UTF-8");
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(U_PZData.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(U_PZData.class.getName()).log(Level.SEVERE, null, ex);
                }
                finally
                {
                    if (writer != null)
                    {
                        writer.println(sql);
                        writer.println(e.getLocalizedMessage());
                        //writer.println(e.getMessage());
                        //writer.println(e.getSQLState());
                        writer.close();    
                    }
                }
                logger.error("Exception while closing SQL-ResultSet", e);
            }
            
            try 
            {
                if (stmt != null) stmt.close();
            } 
            catch (SQLException e) 
            {
                logger.error("Exception while closing SQL-Statement", e);
            }
		
            try 
            {
                if (conn != null) conn.close();
            } 
            catch (SQLException e) 
            {
                logger.error("Exception while closing SQL-Connection", e);
            }
        }
        
	return new SesResultBuilder().addDataTable(builder.build()).build();
    }
    
    public static Calendar timestampToCalendar(Timestamp ts)
    {
        if (ts == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.setTimeInMillis(ts.getTime());
        return cal;
    }
}