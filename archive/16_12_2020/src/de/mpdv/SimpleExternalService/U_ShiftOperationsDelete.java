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

public class U_ShiftOperationsDelete implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private boolean debugEnabled = false;
    private final boolean debugMode = false;
 
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory) {
        SesResult res = null;
        ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        this.logger = loggerProvider.fetchLogger(U_ShiftOperationsDelete.class);
        this.debugEnabled = this.logger.isDebugEnabled();
        SpecialParam operation_id = request.getSpecialParam("operation.id");
        SpecialParam shiftorder_id = request.getSpecialParam("shiftorder.id");
        SpecialParam shiftoperations_key = request.getSpecialParam("shiftoperations.key");
        System.out.println("U_ShiftOperationsDelete");
        
        if (this.debugEnabled)
        {
            this.logger.debug("Delete OP from shift order");
        }
		
        if (operation_id != null && shiftorder_id != null && shiftoperations_key != null ? !"".equals(operation_id.getValue()) && !"".equals(shiftorder_id.getValue()) && !"".equals(shiftoperations_key.getValue()) : false)
        {
            this.logger.debug("U_ShiftOperationsDelete has params");
            
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            Connection con = null;
            Statement   stmt = null;
            try {
                con = connProv.fetchDbConnection();
                stmt = con.createStatement();
                String sql = 
                    " Delete " +
                    "    [hydadm].[u_shiftoperations] " +
                    " Where " +
                    "   [shiftorder_nr] = N'" + shiftorder_id.getValue().toString() + "' and " +
                    "   [auftrag_nr] = N'" + operation_id.getValue().toString() + "' and " +
                    "   [id] = " + shiftoperations_key.getValue().toString();
                if (debugMode) System.out.println(sql);
                
            	IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
    		builder.addCol("shiftorder.id", DataType.STRING);
                //builder.addCol("operation.id", DataType.STRING);
                builder.addRow();
                builder.value(shiftorder_id.getValue().toString());
                //builder.value(operation_id.getValue().toString());
                res = new SesResultBuilder().addDataTable(builder.build()).build();
                
                if (this.debugEnabled)
                {
                    this.logger.debug(sql);
                }
                int affectedRows = stmt.executeUpdate(sql);
                System.out.println(affectedRows);
                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Delete " + affectedRows + " operations");
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
            this.logger.error("U_ShiftOperationsDelete SpecialParam not found");
        
        return res;
    }
}