/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package u_sqldialogbuttonmove;

import static com.sun.org.apache.xpath.internal.objects.XMLStringFactoryImpl.getFactory;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 *
 * @author mikhail.malokhatko
 */
public class USqldialogbuttonmoveUpdate {
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
        BapiInterpreterUeContext context = (BapiInterpreterUeContext)param.get("context");
        ISystemUtilFactory factory = (ISystemUtilFactory)param.get("factory");
        Map<String, SpecialParam> specialParameters = sdiParam.getSpecialParameters();
        SpecialParam usqldlgbut_id = sdiParam.getSpecialParameters().get("usqldlgbut.id");
        SpecialParam usqldlgbut_usqldlgcfg_id = sdiParam.getSpecialParameters().get("usqldlgbut.usqldlgcfg.id");
        SpecialParam usqldlgbut_move = sdiParam.getSpecialParameters().get("usqldlgbut.move");
        ISdiLoggerProvider loggerProvider = factory.fetchUtil("LoggerProvider");
        ISdiLogger logger = loggerProvider.fetchLogger(USqldialogbuttonmoveUpdate.class);
        IToNativeSqlConverter sqlConverter = factory.fetchUtil("ToNativeSqlConverter");
        IDbConnectionProvider dbConnectionProvider = factory.fetchUtil("DbConnectionProvider");
        Connection connection = null;
        Statement stmt = null;
        
        try {
            connection = dbConnectionProvider.fetchDbConnection();
            stmt = connection.createStatement();
            
            String sqlUpdate = 
                " Declare " +
                "   @DPos int       = " + usqldlgbut_usqldlgcfg_id.getValue().toString() + ", " +
                "   @BPos int       = " + usqldlgbut_id.getValue().toString() + ", " +
                "   @Move nvarchar  = '" + usqldlgbut_move.getValue().toString() + "', " +
                "   @NPos int       = 1 " +
                " " +
                " If @Move = 'U' " +
                " Begin " +
                "   If @BPos > 1 " +
                "   Begin " +
                "       Set @NPos = @BPos - 1 " +
                "       Update hydialogbuttons Set b_nr = 999 Where dlg_verweis = @DPos and b_nr = @BPos " +
                "       If Exists(Select b_nr From hydialogbuttons Where dlg_verweis = @DPos and b_nr = @NPos) " +
                "       Begin " +
                "           Update hydialogbuttons Set b_nr = @BPos Where dlg_verweis = @DPos and b_nr = @NPos " +
                "       End " +
                //"       Update hydialogbuttons Set b_nr = @NPos Where dlg_verweis = @DPos and b_nr = 999 " +
                "   End " +
                " End " +
                " " +
                " If @Move = 'D' " +
                " Begin " +
                "   Set @NPos = (Select Count(b_nr) From hydialogbuttons Where dlg_verweis = @DPos) " +
                "   If @BPos < @NPos " +
                "   Begin " +
                "       Set @NPos = @BPos + 1 " +
                "       Update hydialogbuttons Set b_nr = 999 Where dlg_verweis = @DPos and b_nr = @BPos " +
                "       If Exists(Select b_nr From hydialogbuttons Where dlg_verweis = @DPos and b_nr = @NPos) " +
                "       Begin " +
                "           Update hydialogbuttons Set b_nr = @BPos Where dlg_verweis = @DPos and b_nr = @NPos " +
                "       End " +
                //"       Update hydialogbuttons Set b_nr = @NPos Where dlg_verweis = @DPos and b_nr = 999 " +
                "   End " +
                " End " +
                " Set @BPos = 999" +
                " Select @DPos as dlg_verweis, @BPos as b_nr, @NPos as nb_nr";
            //stmt = connection.prepareStatement(sqlConverter.toNativeSql(sqlUpdate));
            write("USqldialogbuttonmoveUpdate", sqlConverter.toNativeSql(sqlUpdate));
            //stmt.setInt(1, Integer.parseInt(usqldlgbut_usqldlgcfg_id.getValue().toString()));
            //stmt.setInt(2, Integer.parseInt(usqldlgbut_id.getValue().toString()));
            //stmt.setString(3, usqldlgbut_move.toString());
            ResultSet res = stmt.executeQuery(sqlUpdate);
            if (res != null ? res.next() : false)
            {
                specialParameters.put("usqldlgbut.usqldlgcfg.id", new SpecialParam("usqldlgbut.usqldlgcfg.id", OperatorType.EQUAL, res.getInt("dlg_verweis")));
                specialParameters.put("usqldlgbut.id", new SpecialParam("usqldlgbut.id", OperatorType.EQUAL, res.getInt("b_nr")));
                specialParameters.put("usqldlgbut.newid", new SpecialParam("usqldlgbut.newid", OperatorType.EQUAL, res.getInt("nb_nr")));
            }
        } catch (SQLException e) {
            logger.error("Error while executing sql.", e);
            write("USqldialogbuttonmoveUpdate", e.getMessage());
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
        SdiAfterInitResult result = new SdiAfterInitResult(specialParameters);
        param.set("result", result);
    }
}
