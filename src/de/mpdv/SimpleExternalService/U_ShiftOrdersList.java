package de.mpdv.SimpleExternalService;

import de.mpdv.sdi.data.DataType;
import de.mpdv.sdi.data.SesContext;
import de.mpdv.sdi.data.SesRequest;
import de.mpdv.sdi.data.SesResult;
import de.mpdv.sdi.data.SesResultBuilder;
import de.mpdv.sdi.simpleExternalService.ISimpleExternalService;
import de.mpdv.sdi.systemutility.IDataTableBuilder;
import de.mpdv.sdi.systemutility.IDbConnectionProvider;
import de.mpdv.sdi.systemutility.ISdiLogger;
import de.mpdv.sdi.systemutility.ISdiLoggerProvider;
import de.mpdv.sdi.systemutility.ISystemUtilFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class U_ShiftOrdersList implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private boolean debugEnabled = true;
    private final boolean debugMode = true;
 
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory) {
        SesResult res = null;
        Connection con = null;
        Statement stmt = null;
        
        Map<String, String> StatusText = new HashMap<String, String>();
        StatusText.put("C", "Создан");
        StatusText.put("R", "Выпущен");
        StatusText.put("M", "Снят");

        Map<String, String> DepartmentText = new HashMap<String, String>();
        DepartmentText.put("1", "ПК1");
        DepartmentText.put("2", "ПК2");        

        Map<String, String> ShiftText = new HashMap<String, String>();
        ShiftText.put("A", "День");
        ShiftText.put("B", "Ночь");        
        
        System.out.println("Execute Service U_ShiftOrdersList");
        //System.out.println(request.getSpecialParams().size());
        try {
            ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
            this.logger = loggerProvider.fetchLogger(U_ShiftOrdersList.class);
            this.debugEnabled = this.logger.isDebugEnabled();
            if (this.debugEnabled)
            {
                this.logger.debug("Shift order list");
            }
            
            String sql =
            "Select " +
            "   [id], " +
            "   [shiftorder_nr], " +
            "   [department], " +
            "   [create_date], " +
            "   [create_time], " +
            "   [shift_nr], " +
            "   [s_status], " +
            "   [bearb], " +
            "   [bearb_date], " +
            "   [bearb_time]" +
            "  From " +
            "   [u_shiftorders] " +
            " Where " +
            "   id Is Not Null ";
            
            String s = "";
            for (String key: request.getSpecialParamMap().keySet())
            {
                Object oVal = request.getSpecialParam(key).getValue(); 
                String val = oVal != null ? oVal.toString() : "";
                if (!"".equals(val))
                {
                    if ("shiftorder.create_ts".equals(key))
                    {
                        if (oVal != null)
                        {
                            System.out.println("oVal.getClass().getCanonicalName() = " + oVal.getClass().getCanonicalName() + "getOperator() = " + request.getSpecialParam(key).getOperator());
                            if (oVal.getClass().getCanonicalName().contains("ArrayList"))
                            {
                                List<GregorianCalendar> dateArray = null;
                                try
                                {
                                    dateArray = (List<GregorianCalendar>)oVal;
                                }
                                catch (Exception e)
                                {
                                    System.out.println("Cast Error" + e.getMessage());
                                }
                                finally
                                {
                                    if (dateArray != null)
                                    {
                                        if (dateArray.size() > 1)
                                        {
                                            sql += String.format(" and create_date BETWEEN Cast('%s' as datetime) and Cast('%s' as datetime)", new SimpleDateFormat("yyyy-MM-dd 00:00:00.0").format(dateArray.get(0).getTime()), new SimpleDateFormat("yyyy-MM-dd 00:00:00.0").format(dateArray.get(1).getTime()));
                                        }
                                        else
                                        {
                                            sql += String.format(" and create_date = Cast('%s' as datetime)", new SimpleDateFormat("yyyy-MM-dd 00:00:00.0").format(dateArray.get(0).getTime()));
                                        }
                                    }
                                }
                            }
                            else
                            {
                                sql += String.format(" and create_date = Cast('%s' as datetime)", new SimpleDateFormat("yyyy-MM-dd 00:00:00.0").format(((GregorianCalendar)request.getSpecialParam(key).getValue()).getTime()));
                            }
                        }
                    }
                    if ("shiftorder.department".equals(key))
                    {
                        sql += String.format(" and department = N'%s'", request.getSpecialParam(key).getValue());
                    }
                    if ("shiftorder.shift".equals(key))
                    {
                        sql += String.format(" and shift_nr = N'%s'", request.getSpecialParam(key).getValue());
                    }
                    if ("shiftorder.key".equals(key))
                    {
                        sql += String.format(" and id = %s", "java.util.Arrays$ArrayList".equals(request.getSpecialParam(key).getValue().getClass().getName()) ? ((List)request.getSpecialParam(key).getValue()).get(0) :  request.getSpecialParam(key).getValue());
                    }

                }
                s += " " + key + " = " + (request.getSpecialParam(key).getValue() != null ? request.getSpecialParam(key).getValue().getClass().getName() : "null" ) + " " + (request.getSpecialParam(key).getValue() != null ? request.getSpecialParam(key).getValue().toString() : "null");
            }
            
            if (debugMode)
            {
                System.out.println(s);
                System.out.println(sql);
            }
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            con = connProv.fetchDbConnection();
            stmt = con.createStatement();
            ResultSet DataSet = stmt.executeQuery(sql);
            
            IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
            builder.addCol("shiftorder.create_ts", DataType.DATETIME);
            builder.addCol("shiftorder.department", DataType.STRING);
            builder.addCol("shiftorder.id", DataType.STRING);
            builder.addCol("shiftorder.key", DataType.INTEGER);
            builder.addCol("shiftorder.shift", DataType.STRING);
            builder.addCol("shiftorder.status", DataType.STRING);

            while (DataSet.next())
            {
                builder.addRow();
                Calendar cld = new GregorianCalendar();
                cld.setTime(DataSet.getDate("create_date"));
                builder.value(cld);
                builder.value(DepartmentText.containsKey(DataSet.getString("department")) ? DepartmentText.get(DataSet.getString("department")) : "");
                builder.value(DataSet.getString("shiftorder_nr"));
                builder.value(DataSet.getInt("id"));
                builder.value(ShiftText.containsKey(DataSet.getString("shift_nr")) ? ShiftText.get(DataSet.getString("shift_nr")) : "");
                builder.value(StatusText.containsKey(DataSet.getString("s_status")) ? StatusText.get(DataSet.getString("s_status")) : "");
            }
            res = new SesResultBuilder().addDataTable(builder.build()).build();
            try {
                DataSet.close();
            }
            catch (Exception e) {
                this.logger.error("Could not close ResultSet", e);
            } 
        }
        catch (SQLException ex) {
            Logger.getLogger(U_ShiftOrdersList.class.getName()).log(Level.SEVERE, null, ex);
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
        return res;
    }
}