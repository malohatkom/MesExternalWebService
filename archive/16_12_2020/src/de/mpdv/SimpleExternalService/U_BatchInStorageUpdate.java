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

public class U_BatchInStorageUpdate implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private boolean debugEnabled = false;
 
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory) {
        SesResult res = null;
        ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        this.logger = loggerProvider.fetchLogger(U_BatchInStorageUpdate.class);
        this.debugEnabled = this.logger.isDebugEnabled();
        SpecialParam batch_id = request.getSpecialParam("batch.id");
        SpecialParam batch_userfield08 = request.getSpecialParam("batch.userfield08");
        System.out.println("Cell service public U_BatchInStorageUpdate");
        
        if (this.debugEnabled)
        {
            this.logger.debug("Add batch to painting order");
        }
		
        if (batch_id != null && batch_userfield08 != null)
        {
            this.logger.debug("U_BatchInStorageUpdate has params");
            
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            Connection con = null;
            Statement   stmt = null;
            try {
                con = connProv.fetchDbConnection();
                String sql = 
                    " Update " +
                    "   [hydadm].[los_bestand] " +
                    " Set " +
                    "   [user_n_08] = '" + batch_userfield08.getValue().toString() + "', " +
                    "   [bearb] = '" + request.getUserId() + "', " +
                    "   [bearb_date] = Format(CURRENT_TIMESTAMP, 'yyyy-MM-dd 00:00:00.000'), " +
                    "   [bearb_time] = DATEDIFF(SECOND, 0,CONVERT(time, CURRENT_TIMESTAMP)) " +
                    " Where " +
                    "   [losnr] = '" + batch_id.getValue().toString() + "' ";
                stmt = con.createStatement();
                
                System.out.println(sql);

                IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
    		builder.addCol("batch.id", DataType.STRING);
                builder.addCol("batch.userfield08", DataType.INTEGER);
                builder.addRow();
                builder.value(batch_id.getValue().toString());
                builder.value(batch_userfield08.getValue());
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
            this.logger.error("U_ShiftOperationsAssignWP SpecialParam not found");
        
        return res;
    }
}