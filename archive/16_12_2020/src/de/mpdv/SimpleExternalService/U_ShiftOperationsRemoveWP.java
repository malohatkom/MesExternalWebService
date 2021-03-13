package de.mpdv.SimpleExternalService;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class U_ShiftOperationsRemoveWP implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private boolean debugEnabled = false;
 
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory) {
        SesResult res = null;
        ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        this.logger = loggerProvider.fetchLogger(U_ShiftOperationsRemoveWP.class);
        this.debugEnabled = this.logger.isDebugEnabled();
        SpecialParam operation_id = request.getSpecialParam("operation.id");
        SpecialParam shiftorder_id = request.getSpecialParam("shiftorder.id");
        SpecialParam shiftoperations_key = request.getSpecialParam("shiftoperations.key");
        System.out.println("Cell service U_ShiftOperationsAssignWP");
        
        if (this.debugEnabled)
        {
            this.logger.debug("Assign workplace to operation");
        }
		
        if (operation_id != null && shiftorder_id != null && shiftoperations_key != null)
        {
            this.logger.debug("U_ShiftOperationsRemoveWP has params");
            
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            Connection con = null;
            Statement   stmt = null;
            try {
                con = connProv.fetchDbConnection();
                String sql = 
                    " Update " +
                    "   [hydadm].[u_shiftoperations] " +
                    " Set " +
                    "   [masch_nr] = N'', " +
                    "   [bearb] = '" + request.getUserId() + "', " +
                    "   [bearb_date] = Format(CURRENT_TIMESTAMP, 'yyyy-MM-dd 00:00:00.000'), " +
                    "   [bearb_time] = DATEDIFF(SECOND, 0,CONVERT(time, CURRENT_TIMESTAMP)) " +
                    " Where " +
                    "   [shiftorder_nr] = N'" + shiftorder_id.getValue().toString() + "' and " +
                    "   [auftrag_nr] = N'" + operation_id.getValue().toString() + "' and " +
                    "   [id] = " + shiftoperations_key.getValue().toString();
                stmt = con.createStatement();
                
                System.out.println(sql);
                
            	IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
    		builder.addCol("shiftorder.id", DataType.STRING);
                builder.addCol("operation.id", DataType.STRING);
                builder.addRow();
                builder.value(shiftorder_id.getValue().toString());
                builder.value(operation_id.getValue().toString());
                res = new SesResultBuilder().addDataTable(builder.build()).build();
                
                if (this.debugEnabled)
                {
                    this.logger.debug(sql);
                }
                int affectedRows = stmt.executeUpdate(sql);
                System.out.println(affectedRows);
                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Update " + affectedRows + " operations");
                }
            }
            catch (SQLException e) {
                throw new SesException("lkDbError", "Error at DB access: " + e.getMessage(), e, new String[0]);
            }
            finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    }
                    catch (Exception e) {
                        this.logger.error("Could not close statement", e);
                    } 
                }
                if (con != null) {
                    try {
                        con.close();
                    }
                    catch (Exception e) {
                        this.logger.error("Could not close connection", e);
                    } 
                }
            }
        }
        else
            this.logger.error("U_ShiftOperationsRemoveWP SpecialParam not found");
        
        return res;
    }
}