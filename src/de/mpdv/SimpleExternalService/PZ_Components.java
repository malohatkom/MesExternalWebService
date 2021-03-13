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

 public class PZ_Components implements ISimpleExternalService {

    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory)
    {

    	final ISdiLoggerProvider loggerProvider = factory.fetchUtil("LoggerProvider");
    	final ISdiLogger logger = loggerProvider.fetchLogger(this.getClass());
    	
    	final IDbConnectionProvider conProvider = factory.fetchUtil("DbConnectionProvider");
   
    	IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
    	
		builder.addCol("batch.id", DataType.STRING);
		builder.addCol("batch.material_type", DataType.STRING);
		builder.addCol("u_batch.certificate", DataType.STRING);
		builder.addCol("u_batch.manufacturer", DataType.STRING);
		builder.addCol("u_batch.listnumber", DataType.STRING);
		builder.addCol("u_batch.meltnumber", DataType.STRING  );
		builder.addCol("u_batch.batchnumber", DataType.STRING);
		builder.addCol("order.id", DataType.STRING);
		builder.addCol("batch.articledesignation", DataType.STRING  );
		builder.addCol("batch.article", DataType.STRING);
		
		Connection conn = conProvider.fetchDbConnection();
		
		
		
		Statement stmt = null;
		ResultSet rs = null;
		
		
		SpecialParam u_order = request.getSpecialParam("order.id");
		String strorder= (String) u_order.getValue();
		
		String sql="select edll_nr,el_hztyp, attrib_120,attrib_107, lot,attrib_103 ,attrib_102,auftrag_nr,artikel_bez,artikel from pz_components ('"+ strorder+"') Order by Right(auftrag_nr, 4), el_hztyp";

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
				builder.value(rs.getString("edll_nr"));
				builder.value(rs.getString("el_hztyp"));
				builder.value(rs.getString("attrib_120"));
				builder.value(rs.getString("attrib_107"));
				builder.value(rs.getString("lot"));
				builder.value(rs.getString("attrib_103"));
				builder.value(rs.getString("attrib_102"));
				builder.value(rs.getString("auftrag_nr"));
				builder.value(rs.getString("artikel_bez"));
				builder.value(rs.getString("artikel"));
			
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



