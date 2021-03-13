package de.mpdv.SimpleExternalService;
import de.mpdv.sdi.data.DataType;
import de.mpdv.sdi.data.SesContext;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class U_AssortmentUpdate implements ISimpleExternalService 
{
    private ISdiLogger logger = null;
    private boolean debugEnabled = true;
    private final boolean debugMode = true;
 
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory) {
        SesResult res = null;
        Connection con = null;
        Statement stmt = null;

        ISdiLoggerProvider loggerProvider = (ISdiLoggerProvider)factory.fetchUtil("LoggerProvider");
        this.logger = loggerProvider.fetchLogger(U_AssortmentUpdate.class);
        this.debugEnabled = this.logger.isDebugEnabled();
        
        if (debugMode) System.out.println("U_AssortmentUpdate");
        try
        {
            IDbConnectionProvider connProv = (IDbConnectionProvider)factory.fetchUtil("DbConnectionProvider");
            con = connProv.fetchDbConnection();
            stmt = con.createStatement();
            String sql = "Select COLUMN_NAME, DATA_TYPE From hydra1.INFORMATION_SCHEMA.COLUMNS Where TABLE_NAME = 'u_assortment'";
            Map<String, String> colType = new HashMap<String, String>();
                
            ResultSet DataSet = stmt.executeQuery(sql);
            while(DataSet.next()) colType.put(DataSet.getString("COLUMN_NAME"), DataSet.getString("DATA_TYPE"));
            if (DataSet != null) {
                try {
                    DataSet.close();
                }
                catch (Exception e) {
                    this.logger.error("Could not close DataSet", e);
                } 
            }

            if (debugMode)
            {
                String s = "";
                for (String key: request.getSpecialParamMap().keySet())
                {
                    s += " " + key + " = " + (request.getSpecialParam(key) != null ? request.getSpecialParam(key).toString() : "null");
                }
                System.out.println(s);
            }
            StringBuilder newval = new StringBuilder();
            String id = "";
            for (String key: request.getSpecialParamMap().keySet())
            {
                if ("Assortment.AST_WHEEL_KOD".equals(key))
                    id = request.getSpecialParam(key).getValue() != null ? request.getSpecialParam(key).getValue().toString() : "null";
                else
                {
                    if (newval.length() != 0) 
                    {
                        newval.append(", ");
                    } 
                    newval.append(String.format("%s = %s", key.replace("Assortment.", ""), GetValStr(request.getSpecialParam(key), colType)));
                }
            }
            
            if (debugMode) System.out.println(" newval.length() = " + Integer.toString(newval.length()) + " id " + id);
            if (newval.length() != 0 && !"".equals(id)) 
            {
                sql = "Update u_assortment Set " + newval.toString() + String.format(", bearb = '%s', bearb_date = Format(CURRENT_TIMESTAMP, 'yyyy-MM-dd 00:00:00.000'), bearb_time = DATEDIFF(SECOND, 0,CONVERT(time, CURRENT_TIMESTAMP)) Where AST_WHEEL_KOD = N'%s'", request.getUserId(), id);
                if (debugMode) System.out.println(sql);
                int i = stmt.executeUpdate(sql);
                if (debugMode) System.out.println("rows updated " + i);

                if (i == 0)
                {
                    StringBuilder fld = new StringBuilder();
                    StringBuilder val = new StringBuilder();
                    for (String key: colType.keySet())
                    {
                        if (!key.contains("bearb") && !"AST_ID".equals(key))
                        {
                            if (fld.length() > 0 && val.length() > 0); 
                            {
                                fld.append(", ");
                                val.append(", ");
                            } 
                    
                            if (!"".equals(key.trim()))
                            {
                                fld.append(key.replace("Assortment.", ""));
                                val.append(GetValStr(request.getSpecialParam("Assortment." + key), colType));
                            }
                        }
                    }
            
                    if (debugMode) System.out.println(" fld.length() = " + Integer.toString(fld.length()) + " val.length() " + Integer.toString(val.length()) + " id " + id);
                    if (fld.length() != 0 && val.length() != 0 && !"".equals(id)) 
                    {
                        sql = "If (Not Exists(Select * From u_assortment Where AST_WHEEL_KOD = N'"+id+"')) Begin Insert Into u_assortment (bearb, bearb_date, bearb_time" + fld.toString() + ") Values ('"+request.getUserId()+"', Format(CURRENT_TIMESTAMP, 'yyyy-MM-dd 00:00:00.000'), DATEDIFF(SECOND, 0,CONVERT(time, CURRENT_TIMESTAMP)) " + val.toString() + ") End".replace("(, ", "");
                        if (debugMode) System.out.println(sql);
                        i = stmt.executeUpdate(sql);
                        if (debugMode) System.out.println("rows inserted " + i);
                    }
                }
               
                IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
                builder.addCol("Assortment.AST_WHEEL_KOD", DataType.STRING);
                if (i > 0)
                {
                    builder.addRow();
                    builder.value(id);
                    res = new SesResultBuilder().addDataTable(builder.build()).build();
                }
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(U_ShiftOrdersList.class.getName()).log(Level.SEVERE, null, ex);
            if (debugMode) System.out.println(ex.getMessage());
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

    private String GetValStr(SpecialParam sParam, Map<String, String> cTypes) {
        String res = "null ";
        try
        {
            if (sParam != null)
            {
                Object v = sParam.getValue();
                if (v != null ? !"".equals(v) : false)
                {
                    String t = cTypes.get(sParam.getAcronym().replace("Assortment.", ""));
                    //if (debugMode) System.out.println(sParam.getAcronym().replace("Assortment.", "") + " : " + t);
                    if ("int".equals(t)) res = String.format("%s ", sParam.getValue());
                    if ("nvarchar".equals(t)) res = String.format("N'%s' ", sParam.getValue());
                    if ("decimal".equals(t)) res = String.format("%s ", sParam.getValue().toString()).replace(",", ".");
                    if ("datetime".equals(t)) 
                    {
                        res = String.format("'%s'", new SimpleDateFormat("yyyy-MM-dd 00:00:00.0").format(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(sParam.getValue().toString())));
                    }
                }
            }
        }
        catch (Exception e)
        {
            if (debugMode) System.out.println("GetValStr error : " + e.getMessage());
        }
        return res;
    }
}