/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package u_sqldialogfield;

import de.mpdv.customization.userExit.IUserExitParam;
import de.mpdv.sdi.data.OperatorType;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.data.ue.SdiAfterInitParam;
import de.mpdv.sdi.data.ue.SdiAfterInitResult;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author mikhail.malokhatko
 */
public class USqldialogfieldUpdate {
    
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
    
    public void sdiAfterInit(IUserExitParam param) {
        SdiAfterInitParam sdiParam = (SdiAfterInitParam)param.get("param");
        Map<String, SpecialParam> specialParameters = sdiParam.getSpecialParameters();
        String visible = "";
        String blocked = "";
        for (String key: specialParameters.keySet())
        {
            SpecialParam sp = specialParameters.get(key);
            if (sp != null)
            {
                if ("".equals(sp.getValue() != null ? sp.getValue().toString() : "null"))
                {
                    specialParameters.put(key, new SpecialParam(key, sp.getOperator(), null));
                }
                if ("udlgfld.visible".equals(key)) visible = "true".equals(sp.getValue().toString()) ? "J" : "N";
                if ("udlgfld.blocked".equals(key)) blocked = "true".equals(sp.getValue().toString()) ? "J" : "N";
                if ("udlgfld.activation".equals(key)) specialParameters.put(key, new SpecialParam(key, sp.getOperator(), "A".equals(sp.getValue()) ? "" : sp.getValue()));
            }
        }
        if (!"".equals(visible) && !"".equals(blocked)) specialParameters.put("udlgfld.blocked_visible", new SpecialParam("udlgfld.blocked_visible", OperatorType.EQUAL, blocked + visible));
        //for (String key: specialParameters.keySet()) write("USqldialogfieldUpdate", key + " = " + (specialParameters.get(key).getValue() != null ? specialParameters.get(key).getValue().toString() : "null"));
        SdiAfterInitResult result = new SdiAfterInitResult(specialParameters);
        param.set("result", result);
        //write("USqldialogfieldUpdate", );
    }
}