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

	 public class U_PZ_Person implements ISimpleExternalService {

	    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory)
	    {

	    	final ISdiLoggerProvider loggerProvider = factory.fetchUtil("LoggerProvider");
	    	final ISdiLogger logger = loggerProvider.fetchLogger(this.getClass());
	    	
	    	final IDbConnectionProvider conProvider = factory.fetchUtil("DbConnectionProvider");
	   
	    	IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
	    	
			builder.addCol("order.id", DataType.STRING);
			builder.addCol("person.name", DataType.STRING);
			builder.addCol("person.function", DataType.STRING);
			builder.addCol("operation.latest_end_ts", DataType.STRING);
			
			Connection conn = conProvider.fetchDbConnection();
			
			Statement stmt = null;
			ResultSet rs = null;
			
			SpecialParam u_order = request.getSpecialParam("order.id");
			String strorder= (String) u_order.getValue();
			
			String sql="select auftrag_nr,name, taetigkeit from pz_personen ('"+ strorder+"')";

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
					builder.value(rs.getString("name"));
					builder.value(rs.getString("taetigkeit"));
					builder.value("");
				
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
	 }