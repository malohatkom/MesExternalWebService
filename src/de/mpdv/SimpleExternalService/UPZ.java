package de.mpdv.SimpleExternalService;
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

public class UPZ implements ISimpleExternalService 
{
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
        builder.addCol("inspectionpoint.ud_code", DataType.DATETIME  );
                
	Connection conn = conProvider.fetchDbConnection();
		
	Statement stmt = null;
	ResultSet rs = null;
		
	SpecialParam u_order = request.getSpecialParam("order.id");
	String strorder= (String) u_order.getValue();
          
        
        String sql = "EXECUTE [dbo].[FromPDMDataFull] '"+ strorder +"'";
                
        
        
        
	/*
        String sql = "SELECT [auftrag_nr]" +
        " ,[agnr]" +
        " ,[ag_bez]" +
        " ,[name]" +
        " ,[taetigkeit]" +
        " ,[taetigkeit_2]" +
        " ,[datum]" +
        " ,[zeit]" +
        " ,[merkmal_bez_18]" +
        " ,[otg]" +
        " ,[sw]" +
        " ,[utg]" +
        " ,[status]" +
        " ,[status_info]" +
        " ,[ppunkt_ve_code]" +
        " ,[messwert]" +
        " FROM [hydra1].[hydadm].[NewPZ] where auftrag_nr = '" + strorder + "'";
        sql += " order by agnr";
        */

        try 
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) 
            {
                
//auftrag_nr                    agnr	a_status	ag_bez                                  masch_nr	datatime                nmkp                    znkp                    mizn	nozn	mazn	name                            taetigkeit              F_DOCID	F_PARENTKEY
//TK4475.530.530.22.190005	0005	E               Термическая резка листа на заготовки	SUPRARX         2019-03-02 06:07:24.000	Длина заготовки, мм	1920 | 1928 | 1930	1920	1928	1930	Гончаренко, Максим Владимирович	Газорезчик 5 разряда	9317	5434118                
                
                builder.addRow();
		builder.value(rs.getString("auftrag_nr"));
                builder.value(rs.getString("agnr"));
		builder.value(rs.getString("ag_bez"));
                
                //String OPName = rs.getString("ag_bez");
                String Result = rs.getString("znkp");  //"messwert");
                String LValue = rs.getString("mizn");  //"otg");
                String NValue = rs.getString("nozn");  //sw");
                String UValue = rs.getString("mazn");  //"utg");
                
                //if ("Сварка продольного шва".equals(OPName))
                //{
                    if ("".equals(Result))
                    {
                        if (!"".equals(NValue)) 
                            Result = NValue;
                        else
                        {
                            if (!"".equals(LValue) && !"".equals(UValue))
                            {
                                Result = ((Float)((Float.parseFloat(LValue) + Float.parseFloat(UValue)) / 2)).toString();
                            }
                            else
                            {
                                if (!"".equals(LValue) && "".equals(UValue)) Result = LValue;
                                if ("".equals(LValue) && !"".equals(UValue)) Result = UValue;
                            }
                        }
                    }
                //}
//Result = "1111";                                
		builder.value(rs.getString("name"));
		builder.value(rs.getString("taetigkeit")); //taetigkeit_2
		builder.value(rs.getString("datatime"));  //"end_ts"));
		builder.value(rs.getString("nmkp"));  //"merkmal_bez_18"));
		builder.value(rs.getString("mizn"));  //"otg"));
		builder.value(rs.getString("nozn"));  //"sw"));
		builder.value(rs.getString("mazn"));  //"utg"));
		builder.value(Result);
		builder.value(""); //rs.getString("ppunkt_ve_code"));
            }
	} 
        catch (SQLException e) 
        {
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
            catch (SQLException e) 
            {
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
}