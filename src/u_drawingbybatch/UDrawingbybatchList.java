/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package u_drawingbybatch;

import de.mpdv.customization.userExit.IUserExitParam;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.data.ue.SdiAugmentSqlParam;
import de.mpdv.sdi.data.ue.SdiAugmentSqlResult;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author mikhail.malokhatko
 */
public class UDrawingbybatchList {
    private final boolean debugPrint = true;
   
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
        if (debugPrint) write("UDrawingbybatchList", " Select " + ((sqlParams.getSelect() != null) ? sqlParams.getSelect() : "") + " From " + ((sqlParams.getFrom() != null) ? sqlParams.getFrom() : "") + " Where " + ((sqlParams.getWhere() != null) ? sqlParams.getWhere() : "") + " Group By " + ((sqlParams.getGroupBy() != null) ? sqlParams.getGroupBy() : ""));
        String suffixGroupBy = "";
        if (sqlParams.getSelect() != null ? sqlParams.getSelect().contains("mat_puf") : false) {
            if (debugPrint) write("UDrawingbybatchList", "add group mat_puf");
            suffixGroupBy = "lb.mat_puf";
        }
        if (debugPrint) write("UDrawingbybatchList", "add group by ast.ast_drawing_num");
        suffixGroupBy += (!"".equals(suffixGroupBy) ? ", " : "") + "ast.ast_drawing_num";
        if (sqlParams.getSelect() != null ? sqlParams.getSelect().contains("ast_performance") : false) {
            if (debugPrint) write("UDrawingbybatchList", "add group by ast.ast_performance");
            suffixGroupBy += ", ast.ast_performance";
        }
        param.set("result", new SdiAugmentSqlResult(null, null, suffixGroupBy, null));
    }
}
