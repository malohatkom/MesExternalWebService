package u_shiftoperations;

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

public class UShiftoperationsList
{
    private final boolean debugPrint = true;
  
    private static void write(String FileName, String text) {
        String fileName = "C:\\Windows\\Temp\\" + FileName + ".log";
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
            out.println((new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")).format(new Date()) + " " + text);
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            if (out != null) {
                out.close();
            }
        } 
    }
  
    public void sdiAugmentSql(IUserExitParam param) {
        SdiAugmentSqlParam sqlParams = (SdiAugmentSqlParam)param.get("param");
        if (debugPrint) write("UShiftoperationsList", " Select " + ((sqlParams.getSelect() != null) ? sqlParams.getSelect() : "") + " From " + ((sqlParams.getFrom() != null) ? sqlParams.getFrom() : "") + " Where " + ((sqlParams.getWhere() != null) ? sqlParams.getWhere() : ""));
    }
  
    public void sdiGlobalModifyRequest(IUserExitParam param) {
        try {
            SdiGlobalModifyRequestParam struct = (SdiGlobalModifyRequestParam)param.get("param");
            SqlFilterComplexExpression sqlFilterComplexExpression = struct.getFilterParametersRootExpression();
            String params = "";
            for (ASqlFilterExpression fe : sqlFilterComplexExpression.getChildren()) {
                if (fe.getSqlFilterType() == SqlFilterType.SIMPLE) {
                    SqlFilterSimpleExpression se = (SqlFilterSimpleExpression)fe;
                    params = params + ("".equals(params) ? "" : " | ") + se.getKey() + " " + se.getOperator().name() + " ";
                    String vals = "";
                    for (Object val : se.getValues()) {
                        vals = vals + ("".equals(vals) ? " " : ", ");
                        switch (se.getValueType().ordinal()) {
                            case 0:
                                vals = vals + val;
                                break;
                            case 1:
                                vals = vals + (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")).format(((GregorianCalendar)val).getTime());
                                break;
                            case 2:
                            case 3:
                            case 4:
                            case 5:
                                vals = vals + val.toString();
                                break;
                        } 
                    } 
                    params = params + vals;
                } 
            } 
            if (debugPrint) write("UShiftoperationsList", params);
        }
        catch (Exception e) {
            write("UShiftoperationsList", e.getMessage());
        } 
    }
}