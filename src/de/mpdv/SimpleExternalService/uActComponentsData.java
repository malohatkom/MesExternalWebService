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

public class uActComponentsData implements ISimpleExternalService {

    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory)
    {

    	final ISdiLoggerProvider loggerProvider = factory.fetchUtil("LoggerProvider");
    	final ISdiLogger logger = loggerProvider.fetchLogger(this.getClass());
    	final IDbConnectionProvider conProvider = factory.fetchUtil("DbConnectionProvider");
   
    	IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
    
	builder.addCol("uActComponentCeqv", DataType.STRING);
	builder.addCol("uActComponentPcm", DataType.STRING);
	builder.addCol("uActComponentMass", DataType.STRING);
        builder.addCol("order.id", DataType.STRING);
    
		
	Connection conn = conProvider.fetchDbConnection();
		
	Statement stmt = null;
	ResultSet rs = null;
		
	SpecialParam u_ActOrder = request.getSpecialParam("order.id");
	String strActOrder = (String) u_ActOrder.getValue();
		
	String sql = "SELECT auftrag_nr, c_eqv, p_sm, soll_menge, ONum FROM GetActData ('" + strActOrder + "')";

	float Ceqv = 0;
        float Pcm = 0;
        float M = 0;
        
        String C_eqv = "";
        String P_cm = "";
        
        try 
        {
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            
            while (rs.next()) 
            {
                Ceqv = rs.getFloat("c_eqv"); 
                Pcm = rs.getFloat("p_sm"); 
                M += rs.getFloat("soll_menge"); 
                
                if (Ceqv == 0)
                {
                    if (C_eqv.equals(""))
                    {
                        C_eqv = Float.toString(Ceqv);
                    }
                    else
                    {   
                        C_eqv += "/" + Float.toString(Ceqv);
                    }
                }
                
                if (Pcm == 0)
                {
                    if (P_cm.equals(""))
                    {
                        P_cm = Float.toString(Pcm);
                    }
                    else
                    {   
                        P_cm += "/" + Float.toString(Pcm);
                    }
                }
            }
            
            
            builder.addRow();
            builder.value(C_eqv);
            builder.value(P_cm);
            builder.value(Float.toString(M));
            builder.value(strActOrder);
	} 
        catch (SQLException e) 
        {
            logger.error("Exception while accessing database", e);
            throw new SesException("lkDbError", "Exception while accessing database", e);
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



