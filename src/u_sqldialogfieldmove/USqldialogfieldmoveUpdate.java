/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package u_sqldialogfieldmove;

import de.mpdv.customization.userExit.IUserExitParam;
import de.mpdv.sdi.data.OperatorType;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.data.ue.BapiInterpreterUeContext;
import de.mpdv.sdi.data.ue.SdiAfterInitParam;
import de.mpdv.sdi.data.ue.SdiAfterInitResult;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import de.mpdv.sdi.data.SdiException;
import de.mpdv.sdi.systemutility.IDbConnectionProvider;
import de.mpdv.sdi.systemutility.ISdiLogger;
import de.mpdv.sdi.systemutility.ISdiLoggerProvider;
import de.mpdv.sdi.systemutility.ISystemUtilFactory;
import de.mpdv.sdi.systemutility.IToNativeSqlConverter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;


/**
 *
 * @author mikhail.malokhatko
 */
public class USqldialogfieldmoveUpdate {
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
    
    public void sdiAfterInit(IUserExitParam param) {
        SdiAfterInitParam sdiParam = (SdiAfterInitParam)param.get("param");
        //BapiInterpreterUeContext context = (BapiInterpreterUeContext)param.get("context");
        ISystemUtilFactory factory = (ISystemUtilFactory)param.get("factory");
        Map<String, SpecialParam> specialParameters = new HashMap<String, SpecialParam>();  //sdiParam.getSpecialParameters();
        SpecialParam udlgfld_id = sdiParam.getSpecialParameters().get("udlgfld.id");
        SpecialParam udlgcfg_id = sdiParam.getSpecialParameters().get("udlgcfg.id");
        SpecialParam udlgfld_move = sdiParam.getSpecialParameters().get("udlgfld.move");
        ISdiLoggerProvider loggerProvider = factory.fetchUtil("LoggerProvider");
        ISdiLogger logger = loggerProvider.fetchLogger(USqldialogfieldmoveUpdate.class);
        IToNativeSqlConverter sqlConverter = factory.fetchUtil("ToNativeSqlConverter");
        IDbConnectionProvider dbConnectionProvider = factory.fetchUtil("DbConnectionProvider");
        Connection connection = null;
        Statement stmt = null;
        
        try {
            connection = dbConnectionProvider.fetchDbConnection();
            stmt = connection.createStatement();
            
            String sqlUpdate = 
                " Declare " +
                "   @DPos int       = " + udlgcfg_id.getValue().toString() + ", " +
                "   @FPos int       = " + udlgfld_id.getValue().toString() + ", " +
                "   @Move nvarchar  = '" + udlgfld_move.getValue().toString() + "', " +
                "   @NPos int       = 1 " +
                " " +
                " If @Move = 'U' " +
                " Begin " +
                "   If @FPos > 1 " +
                "   Begin " +
                "       Set @NPos = @FPos - 1 " +
                "       Update hydialogfields Set f_nr = 999 Where dlg_verweis = @DPos and f_nr = @FPos " +
                "       If Exists(Select f_nr From hydialogfields Where dlg_verweis = @DPos and f_nr = @NPos) " +
                "       Begin " +
                "           Update hydialogfields Set f_nr = @FPos Where dlg_verweis = @DPos and f_nr = @NPos " +
                "       End " +
                //"       Update hydialogbuttons Set f_nr = @NPos Where dlg_verweis = @DPos and f_nr = 999 " +
                "   End " +
                " End " +
                " " +
                " If @Move = 'D' " +
                " Begin " +
                "   Set @NPos = (Select Count(f_nr) From hydialogfields Where dlg_verweis = @DPos) " +
                "   If @FPos < @NPos " +
                "   Begin " +
                "       Set @NPos = @FPos + 1 " +
                "       Update hydialogfields Set f_nr = 999 Where dlg_verweis = @DPos and f_nr = @FPos " +
                "       If Exists(Select f_nr From hydialogfields Where dlg_verweis = @DPos and f_nr = @NPos) " +
                "       Begin " +
                "           Update hydialogfields Set f_nr = @FPos Where dlg_verweis = @DPos and f_nr = @NPos " +
                "       End " +
                //"       Update hydialogbuttons Set f_nr = @NPos Where dlg_verweis = @DPos and f_nr = 999 " +
                "   End " +
                " End " +
                " Set @FPos = 999" +
                " Select @DPos as dlg_verweis, @FPos as f_nr, @NPos as nf_nr";
            //stmt = connection.prepareStatement(sqlConverter.toNativeSql(sqlUpdate));
            //write("USqldialogfieldmoveUpdate", sqlConverter.toNativeSql(sqlUpdate));
            //stmt.setInt(1, Integer.parseInt(usqldlgbut_usqldlgcfg_id.getValue().toString()));
            //stmt.setInt(2, Integer.parseInt(usqldlgbut_id.getValue().toString()));
            //stmt.setString(3, usqldlgbut_move.toString());
            ResultSet res = stmt.executeQuery(sqlUpdate);
            if (res != null ? res.next() : false)
            {
                specialParameters.put("udlgfld.move", new SpecialParam("udlgfld.move", OperatorType.EQUAL, udlgfld_move.getValue() != null ? udlgfld_move.getValue().toString() : ""));
                specialParameters.put("udlgcfg.id", new SpecialParam("udlgcfg.id", OperatorType.EQUAL, res != null ? res.getInt("dlg_verweis") : -1));
                //write("USqldialogfieldmoveUpdate", "udlgcfg.id = " + (res != null ? Integer.toString(res.getInt("dlg_verweis")) : -1));
                specialParameters.put("udlgfld.id", new SpecialParam("udlgfld.id", OperatorType.EQUAL, res != null ? res.getInt("f_nr") : -1));
                //write("USqldialogfieldmoveUpdate", "udlgfld.id = " + Integer.toString(res != null ? res.getInt("f_nr") : -1));
                specialParameters.put("udlgfld.newid", new SpecialParam("udlgfld.newid", OperatorType.EQUAL, res != null ? res.getInt("nf_nr") : -1));
                //write("USqldialogfieldmoveUpdate", "udlgfld.newid = " + Integer.toString(res != null ? res.getInt("nf_nr") : -1));
            }
        } catch (SQLException e) {
            logger.error("Error while executing sql.", e);
            write("USqldialogfieldmoveUpdate", e.getMessage());
            throw new SdiException("lkDbError", "Error while executing sql.", new String[0]);
        } finally {
            try
            {
                if (stmt != null) stmt.close();
                if (connection != null) connection.close();
            }
            catch(Exception e)
            {
                logger.error("Error while executing sql.", e);
            }
        } 
        param.set("result", new SdiAfterInitResult(specialParameters));
    }
}