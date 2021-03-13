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
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class U_ShiftOperationsPlaneOP implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private boolean debugEnabled = false;
 
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory) {
        System.out.println("Cell service U_ShiftOperationsPlaneOP");
        SesResult res = null;
        ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        this.logger = loggerProvider.fetchLogger(U_ShiftOperationsPlaneOP.class);
        this.debugEnabled = this.logger.isDebugEnabled();
        SpecialParam operation_id = request.getSpecialParam("operation.id");
        SpecialParam shiftorder_id = request.getSpecialParam("shiftorder.id");
        SpecialParam operation_scheduled_start_ts = request.getSpecialParam("operation.scheduled_start_ts");
        SpecialParam operation_scheduled_end_ts = request.getSpecialParam("operation.scheduled_end_ts");
        SpecialParam shiftoperations_key = request.getSpecialParam("shiftoperations.key");
        
        
        if (this.debugEnabled)
        {
            this.logger.debug("Plane operation");
        }
		
        if (operation_id != null && shiftorder_id != null && operation_scheduled_start_ts != null && operation_scheduled_end_ts != null && shiftoperations_key != null)
        {
            this.logger.debug("U_ShiftOperationsPlaneOP has params");
            
            GregorianCalendar cStart = (GregorianCalendar)operation_scheduled_start_ts.getValue();
            GregorianCalendar cEnd = (GregorianCalendar)operation_scheduled_end_ts.getValue();
            
            System.out.println("cStart = " + cStart.toString() + " || cEnd = " + cEnd.toString());
            System.out.println(String.format(" cStart.get(Calendar.HOUR)=%d cStart.get(Calendar.AM_PM)=%s cStart.get(Calendar.MINUTE)=%d cStart.get(Calendar.SECOND=%d", new Object[] { cStart.get(Calendar.HOUR), cStart.get(Calendar.AM_PM), cStart.get(Calendar.MINUTE), cStart.get(Calendar.SECOND)}));
            System.out.println(String.format(" cEnd.get(Calendar.HOUR)=%d cEnd.get(Calendar.AM_PM)=%s cEnd.get(Calendar.MINUTE)=%d cEnd.get(Calendar.SECOND=%d", new Object[] { cEnd.get(Calendar.HOUR), cEnd.get(Calendar.AM_PM), cEnd.get(Calendar.MINUTE), cEnd.get(Calendar.SECOND)}));
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            Connection con = null;
            Statement   stmt = null;
            try {
                con = connProv.fetchDbConnection();
                String sql = 
                    " Update " +
                    "   [hydadm].[u_shiftoperations] " +
                    " Set " +
                    "   [term_anf_dat] = '" + new SimpleDateFormat("yyyy-MM-dd 00:00:00.000").format(cStart.getTime()) + "', " +
                    "   [term_anf_zeit] = " + Integer.toString((cStart.get(Calendar.HOUR) + cStart.get(Calendar.AM_PM) * 12) * 3600 + cStart.get(Calendar.MINUTE) * 60 + cStart.get(Calendar.SECOND)) + ", " +
                    "   [term_end_dat] = '" + new SimpleDateFormat("yyyy-MM-dd 00:00:00.000").format(cEnd.getTime()) + "', " +
                    "   [term_end_zeit] = " + Integer.toString((cEnd.get(Calendar.HOUR) + cEnd.get(Calendar.AM_PM) * 12 ) * 3600 + cEnd.get(Calendar.MINUTE) * 60 + cEnd.get(Calendar.SECOND)) + ", " +
                    "   [bearb] = '" + request.getUserId() + "', " +
                    "   [bearb_date] = Format(CURRENT_TIMESTAMP, 'yyyy-MM-dd 00:00:00.000'), " +
                    "   [bearb_time] = DATEDIFF(SECOND, 0,CONVERT(time, CURRENT_TIMESTAMP)) " +
                    " Where " +
                    "   [shiftorder_nr] = N'" + shiftorder_id.getValue().toString() + "' and " +
                    "   [auftrag_nr] = N'" + operation_id.getValue().toString() + "' and " +
                    "   [id] = " + shiftoperations_key.getValue().toString();
                System.out.println(sql);
                stmt = con.createStatement();
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
            catch (Exception e) {
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
            this.logger.error("U_ShiftOperationsPlaneOP SpecialParam not found");
        
        return res;
    }
}