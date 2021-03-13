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

public class UCaqAde implements ISimpleExternalService {
	
    public SesResult execute(SesRequest request, SesContext context, ISystemUtilFactory factory)
    {

    	final ISdiLoggerProvider loggerProvider = factory.fetchUtil("LoggerProvider");
    	final ISdiLogger logger = loggerProvider.fetchLogger(this.getClass());
    	
    	final IDbConnectionProvider conProvider = factory.fetchUtil("DbConnectionProvider");
   
    	IDataTableBuilder builder = factory.fetchUtil("DataTableBuilder");
    	
	builder.addCol("id", DataType.STRING);
	builder.addCol("qmcharacteristic.id", DataType.STRING);
	builder.addCol("qmcharacteristic.designation", DataType.STRING);
	builder.addCol("qmcharacteristic.inspection_type.designation_short", DataType.STRING);
	builder.addCol("unit.id", DataType.STRING);
	builder.addCol("qmcharacteristic.upper_tolerance_limit", DataType.STRING  );
	builder.addCol("qmcharacteristic.target_value", DataType.STRING);
	builder.addCol("qmcharacteristic.lower_tolerance_limit", DataType.STRING);
	builder.addCol("qmsinglevalue.measured_value.value", DataType.STRING  );
	builder.addCol("qmsinglevalue.defect_units_count", DataType.STRING);
	builder.addCol("operation.id", DataType.STRING);
	builder.addCol("order.id", DataType.STRING);
	builder.addCol("operation.operation", DataType.STRING);
        builder.addCol("inspectionpoint.comment", DataType.STRING);
        builder.addCol("inspectionpoint.physical_sample", DataType.STRING);
        builder.addCol("inspectionpoint.sample", DataType.STRING);

        String strorder = "";
        String stroperation  = "";
        String strsample = "";
        SpecialParam u_order = request.getSpecialParam("order.id");
       	SpecialParam u_operation = request.getSpecialParam("operation.operation");
        SpecialParam u_sample = request.getSpecialParam("inspectionpoint.sample");
        
        if (u_order != null) strorder = (String) u_order.getValue();
        if (u_operation != null) stroperation = (String) u_operation.getValue();
        if (u_sample != null) strsample = u_sample.getValue().toString();
        
	Connection conn = conProvider.fetchDbConnection();
		
	Statement stmt = null;
	ResultSet rs = null;

        String sql=
            "SELECT " +
            " IsNull([id], '') id " +
            " ,IsNull([merkmal_nr], '') merkmal_nr " +
            " ,IsNull([merkmal_bez_18], '') merkmal_bez_18 " +
            " ,IsNull([pruefung_typ], '') pruefung_typ " +
            " ,IsNull([einheit], '') einheit " +
            " ,IsNull([otg], '') otg " +
            " ,IsNull([sw], '') sw " +
            " ,IsNull([utg], '') utg " +
            " ,IsNull([messwert], '') messwert " +
            " ,IsNull([anz_fehler], '') anz_fehler " +
            " ,IsNull([arbgang_nr], '') arbgang_nr " +
            " ,IsNull(pz.[auftrag_nr], '') auftrag_nr " +
            " ,IsNull([op], '') op " +
            " ,IsNull([stichpr_nr], '') stichpr_nr " +
            " ,IsNull(ab.ag_bez, '') ag_bez " +
            " FROM [hydra1].[dbo].[pz001] pz" +
            " left join hydra1.hydadm.auftrags_bestand ab on ab.auftrag_nr = pz.op" +
            " where pz.auftrag_nr = '" + strorder.trim() + "'";
            
        if (!"".equals(stroperation)) sql += " and arbgang_nr = '" + stroperation.trim() + "'";
        if (!"".equals(strsample)) sql += " and pz.stichpr_nr = " + strsample ;
                
        sql += " order by op, cast(id as decimal)";

        String QualityValue = ""; 
        
        /*builder.addRow();
        builder.value("");
        builder.value("");
        builder.value(sql);
        builder.value("");
        builder.value("");
        builder.value("");
        builder.value("");
        builder.value("");
        builder.value("");
        builder.value("");
        builder.value("");
        builder.value("");
        builder.value("");
        builder.value("");
        builder.value("");
        builder.value("");*/

        try 
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            while (rs.next()) 
            {
                if ("Фактическая чувствительность".equals(rs.getString("merkmal_bez_18")))
                {
                    QualityValue = rs.getString("messwert");
                }
                else
                {
                    builder.addRow();
                    builder.value(rs.getString("id").trim());
                    builder.value(rs.getString("merkmal_nr").trim());
                    builder.value(rs.getString("merkmal_bez_18").trim());
                    builder.value(rs.getString("pruefung_typ").trim());
                    builder.value(rs.getString("einheit"));
                    builder.value(rs.getString("otg"));
                    if ("Дефекты сварного шва".equals(rs.getString("merkmal_bez_18")))
                    {
                        builder.value(QualityValue);
                    }
                    else
                    {
                        builder.value(rs.getString("sw"));
                    }
                    builder.value(rs.getString("utg"));
		
                    String ResultValue = rs.getString("messwert");
                    String CommentData = "";
                    String SampleLocat = "";
                
                    if ("Дефекты сварного шва".equals(rs.getString("merkmal_bez_18")))
                    {
                        if (ResultValue.contains("("))
                        {
                            ResultValue = ResultValue.replace(" ", "/");
                            CommentData = ResultValue.substring(ResultValue.indexOf("(") + 1, ResultValue.indexOf(")"));
                            if (CommentData.contains("/") && CommentData.contains("-"))
                            {
                                SampleLocat = CommentData.substring(0, CommentData.indexOf("/"));
                                CommentData = CommentData.substring(CommentData.indexOf("/") + 1).replace("--", ",");
                            }
                            else
                            {
                                SampleLocat = CommentData;
                                CommentData = "-";
                            }   
                            ResultValue = ResultValue.substring(0, ResultValue.indexOf("(")).trim().replace("Соотв.", "Годен").replace("Не соотв.", "Не годен");
                        }
                    }
                
                    builder.value(ResultValue);
                    builder.value(rs.getString("anz_fehler"));
                    builder.value(rs.getString("op").trim());
                    builder.value(rs.getString("auftrag_nr").trim());
                    builder.value(rs.getString("arbgang_nr").trim());
                    builder.value(CommentData);
                    builder.value(SampleLocat);
                    
                    String Comm = "РГК в случае ремонта".equals(rs.getString("ag_bez").trim()) ? "R" + rs.getString("stichpr_nr") : rs.getString("stichpr_nr");
                    
                    builder.value(Comm);
                }
            }
	} 
        catch (SQLException e) 
        {
            builder.addRow();
            builder.value(sql);
            builder.value("");
            builder.value(e.getLocalizedMessage());
            builder.value(e.getErrorCode());
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");
            builder.value("");

            //e.printStackTrace();
            logger.error("Exception while accessing database", e);
            throw new SesException("lkDbError",	"Exception while accessing database", e);
                
	} 
        finally 
        {
            try 
            {
                if (rs != null) rs.close();
            } 
            catch (SQLException e) {logger.error("Exception while closing SQL-ResultSet", e);}
		
            try 
            {
                if (stmt != null) stmt.close();
            } 
            catch (SQLException e) {logger.error("Exception while closing SQL-Statement", e);}
		
            try 
            {
                if (conn != null) {conn.close();}
            } 
            catch (SQLException e) {logger.error("Exception while closing SQL-Connection", e);}
	}
	return new SesResultBuilder().addDataTable(builder.build()).build();
    }
}



