package de.mpdv.SimpleExternalService.PZ;

 
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

 public class U_PZ_Order implements ISimpleExternalService {

    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory)
    {

    	final ISdiLoggerProvider loggerProvider = factory.fetchUtil("LoggerProvider");
    	final ISdiLogger logger = loggerProvider.fetchLogger(this.getClass());
    	
    	final IDbConnectionProvider conProvider = factory.fetchUtil("DbConnectionProvider");
   
    	IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
    	
		builder.addCol("order.id", DataType.STRING);
		builder.addCol("order.material_type", DataType.STRING);
		builder.addCol("order.weight", DataType.STRING);
		builder.addCol("order.ty", DataType.STRING);
		builder.addCol("order.customerdesignation", DataType.STRING);
		builder.addCol("article.drawing_number", DataType.STRING  );
		builder.addCol("article.mtp", DataType.STRING);
                builder.addCol("order.workplan.actnumber", DataType.STRING);
		
		
		Connection conn = conProvider.fetchDbConnection();
		
		
		
		Statement stmt = null;
		ResultSet rs = null;
		
		
		SpecialParam u_order = request.getSpecialParam("order.id");
		String strorder= (String) u_order.getValue();
		
		String sql="select auftrag_nr,matetyp,weight,ty,customerdesignation,drawing_number,mtp,actnumber,extinfo from pz_order ('"+ strorder+"')";

		//SpecialParam u_order = request.getSpecialParam("order.id");
		//if (u_order != null)
		//{
		//	String strorder= (String) u_order.getValue();
		//	if (strorder != null)
			//	{
			//		sql += " and  auftrag_nr= '" + strorder + "'";
		//	}
	//	}

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				builder.addRow();
				builder.value(rs.getString("auftrag_nr"));
				builder.value(rs.getString("matetyp"));
				builder.value(rs.getString("weight"));
				builder.value(rs.getString("ty"));
				builder.value(rs.getString("customerdesignation"));
				builder.value(rs.getString("drawing_number"));
				builder.value(rs.getString("mtp"));
                                builder.value(rs.getString("actnumber"));
			}
	} catch (SQLException e) {
		e.printStackTrace();
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
}



