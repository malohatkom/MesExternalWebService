/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package u_operationinfo;

import de.mpdv.customization.userExit.IUserExitParam;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.data.sqlfilterexpression.ASqlFilterExpression;
import de.mpdv.sdi.data.sqlfilterexpression.SqlFilterComplexExpression;
import de.mpdv.sdi.data.sqlfilterexpression.SqlFilterSimpleExpression;
import de.mpdv.sdi.data.sqlfilterexpression.SqlFilterType;
import de.mpdv.sdi.data.ue.SdiAugmentSqlParam;
import de.mpdv.sdi.data.ue.SdiAugmentSqlResult;
import de.mpdv.sdi.data.ue.SdiGlobalModifyRequestParam;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author mikhail.malokhatko
 */
public class UOperationinfoList {
    private final boolean debugPrint = true;
    private final String ComputerName = System.getenv("COMPUTERNAME");
    private final boolean debugMode = "ETN-SRV-MES2".equals(ComputerName);
    
    private static void write(String FileName, String text) {
        String fileName = "C:\\Windows\\Temp\\" + FileName + ".log";
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
            out.println(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()) + " " + text);
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }         
    }    
    
    public void sdiAugmentSql(final IUserExitParam param) {
        SdiAugmentSqlParam sqlParams = (SdiAugmentSqlParam)param.get("param");
        String operation_site = "";
        if (sqlParams.getSpecialParameters().containsKey("operation.site")) {
            SpecialParam spOperationSite = sqlParams.getSpecialParameters().get("operation.site");
            if (spOperationSite != null)
            {
                operation_site = (spOperationSite.getValue() != null) ? spOperationSite.getValue().toString() : "";
            }
        } 
        if (debugPrint) write("UOperationInfoList", " Select " + ((sqlParams.getSelect() != null) ? sqlParams.getSelect() : "") + " From " + ((sqlParams.getFrom() != null) ? sqlParams.getFrom() : "") + " Where " + ((sqlParams.getWhere() != null) ? sqlParams.getWhere() : ""));
        if (!"".equals(operation_site)) {
            if (debugPrint) write("UShiftordersList", " and uoi.kostenstelle Like N'" + operation_site + "%' ");
            param.set("result", new SdiAugmentSqlResult(null, " uoi.kostenstelle Like N'" + operation_site + "%' ", null, null));
        }         
    }
    
    public void sdiGlobalModifyRequest(final IUserExitParam param) {
        if (debugPrint)
        {
            try
            {
                SdiGlobalModifyRequestParam struct = (SdiGlobalModifyRequestParam)param.get("param");
                SqlFilterComplexExpression sqlFilterComplexExpression = struct.getFilterParametersRootExpression();
                
                String params = "";
                for (ASqlFilterExpression fe: sqlFilterComplexExpression.getChildren())
                {
                    if (fe.getSqlFilterType() == SqlFilterType.SIMPLE)
                    {
                        SqlFilterSimpleExpression se = (SqlFilterSimpleExpression)fe;
                        params +=  ("".equals(params) ? "" : " | ") + se.getKey() + " " + se.getOperator().name() + " ";
                        String vals = "";
                        for (Object val: se.getValues())
                        {
                            vals += ("".equals(vals) ? " " : ", ");
                            switch (se.getValueType().ordinal())
                            {
                                case 0:
                                    vals += val;
                                    break;
                                case 1:
                                    vals += new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(((GregorianCalendar)val).getTime());
                                    break;
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                    vals += val.toString();
                                    break;
                            }
                        }
                        params += vals;
                    }
                }
                write("UOperationInfoList", params);
            }
            catch (Exception e)
            {
                write("UOperationInfoList", e.getMessage());
            }
        }
    }
}