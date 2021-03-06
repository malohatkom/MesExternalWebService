/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package u_pz_order;

import de.mpdv.customization.userExit.IUserExitParam;
import de.mpdv.sdi.data.sqlfilterexpression.ASqlFilterExpression;
import de.mpdv.sdi.data.sqlfilterexpression.SqlFilterComplexExpression;
import de.mpdv.sdi.data.sqlfilterexpression.SqlFilterSimpleExpression;
import de.mpdv.sdi.data.sqlfilterexpression.SqlFilterType;
import de.mpdv.sdi.data.ue.SdiAugmentSqlParam;
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
public class UPzorderList {
    
    private final boolean debugPrint = true;
    
    private static void write(String FileName, String text) {
        String fileName = "D:\\" + FileName + ".log";
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
            String data = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()) + " " + text;
            out.println(data);
            System.out.println(data);
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
        if (debugPrint) write("UPzOrderList", " Select " + ((sqlParams.getSelect() != null) ? sqlParams.getSelect() : "") + " From " + ((sqlParams.getFrom() != null) ? sqlParams.getFrom() : "") + " Where " + ((sqlParams.getWhere() != null) ? sqlParams.getWhere() : ""));
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
                write("UPzOrderList", params);
            }
            catch (Exception e)
            {
                write("UPzOrderList", e.getMessage());
            }
        }
    }
}
