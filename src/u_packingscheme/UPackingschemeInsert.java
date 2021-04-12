/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package u_packingscheme;

import de.mpdv.customization.userExit.IUserExitParam;
import de.mpdv.sdi.data.SpecialParam;
import de.mpdv.sdi.data.ue.SdiAfterPerformActionParam;
import de.mpdv.sdi.systemutility.IDbConnectionProvider;
import de.mpdv.sdi.systemutility.ISdiLogger;
import de.mpdv.sdi.systemutility.ISdiLoggerProvider;
import de.mpdv.sdi.systemutility.ISystemUtilFactory;
import de.mpdv.sdi.systemutility.IToNativeSqlConverter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author mikhail.malokhatko
 */
public class UPackingschemeInsert {
    
    private final boolean DebugMessages = true;
    
    private ISystemUtilFactory factory;
  
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
    
    public void sdiAfterPerformAction(IUserExitParam param) {
        if (DebugMessages) write("UPackingschemeInsert", "UPackingschemeInsert");
        
        SdiAfterPerformActionParam aParam = (SdiAfterPerformActionParam)param.get("param");
        this.factory = (ISystemUtilFactory)param.get("factory");
        ISdiLoggerProvider logerProvider = (ISdiLoggerProvider)this.factory.fetchUtil("LoggerProvider");
        ISdiLogger logger = logerProvider.fetchLogger(UPackingschemeInsert.class);
        logger.debug("Testing UPackingschemeInsert");
        SpecialParam PackingSchemeCopyMaterials = (SpecialParam)aParam.getSpecialParameters().get("PackingScheme.copy_materials");
        SpecialParam PackingSchemeCopyLabels = (SpecialParam)aParam.getSpecialParameters().get("PackingScheme.copy_labels");
        if (DebugMessages) write("UPackingschemeInsert", "PackingSchemeCopyMaterials = " + PackingSchemeCopyMaterials.getValue().toString());
        SpecialParam PackingSchemeOldKey = (SpecialParam)aParam.getSpecialParameters().get("PackingScheme.old_key");
        
        if (PackingSchemeOldKey != null) 
        {
            try 
            {
                if (DebugMessages) write("UPackingschemeInsert", "PackingSchemeOldKey = " + PackingSchemeOldKey.getValue().toString());
                if (DebugMessages) write("UPackingschemeInsert", "PackingSchemeKey = " + aParam.getCreatedSerial().toString());
            
                IToNativeSqlConverter sqlConverter = (IToNativeSqlConverter)this.factory.fetchUtil("ToNativeSqlConverter");
                IDbConnectionProvider dbConnectionProvider = (IDbConnectionProvider)this.factory.fetchUtil("DbConnectionProvider");
                Connection connection = dbConnectionProvider.fetchDbConnection();
                
                int old = Integer.parseInt(PackingSchemeOldKey.getValue().toString());
                int id = Integer.parseInt(aParam.getCreatedSerial().toString());
                    
                String copyResources = "";
                    
                if (PackingSchemeCopyMaterials != null ? "true".equals(PackingSchemeCopyMaterials.getValue().toString()) : false)
                {
                    if (DebugMessages) write("UPackingschemeInsert", "PackingSchemeCopyMaterials = " + PackingSchemeCopyMaterials.getValue().toString());
                    copyResources = 
                        " Set IDENTITY_INSERT U_PackingMaterial On " +
                        " Insert Into " +
                        "   U_PackingMaterial " +
                        "       ( " +
                        "           id, " +
                        "           U_PackingScheme_id, " +
                        "           artikel, " +
                        "           artikel_bez, " +
                        "           bez_1, " +
                        "           bez_2, " +
                        "           hz_typ, " +
                        "           soll_menge, " +
                        "           soll_einh, " +
                        "           bearb, " +
                        "           bearb_date, " +
                        "           bearb_time " +
                        "       ) " +
                        " Select " +
                        "   (Select Max(id) From U_PackingMaterial) + ROW_NUMBER() OVER(ORDER BY id ASC), " +
                        "   ?, " +
                        "   artikel, " +
                        "   artikel_bez, " +
                        "   bez_1, " +
                        "   bez_2, " +
                        "   hz_typ, " +
                        "   soll_menge, " +
                        "   soll_einh, " +
                        "   bearb, " +
                        "   bearb_date, " +
                        "   bearb_time " +
                        " From " +
                        "   U_PackingMaterial " +
                        " Where " +
                        "   U_PackingScheme_id = ? " +
                        " Set IDENTITY_INSERT U_PackingMaterial Off ";
                    if (DebugMessages) write("UPackingschemeInsert", "copyResources = " + copyResources);
                    CopyResources(logger, sqlConverter, connection, id, old, copyResources);
                }
                    
                if (PackingSchemeCopyLabels != null ? "true".equals(PackingSchemeCopyLabels.getValue().toString()) : false)
                {
                    if (DebugMessages) write("UPackingschemeInsert", "PackingSchemeCopyLabels = " + PackingSchemeCopyLabels.getValue().toString());
                    copyResources = 
                        " Set IDENTITY_INSERT U_PackingLabel On " +
                        " Insert Into " +
                        "   U_PackingLabel " +
                        "   ( " +
                        "       id, " +
                        "       U_PackingScheme_id, " +
                        "       report, " +
                        "       labelcount, " +
                        "       kommentar, " +
                        "       aktiviert, " +
                        "       bearb, " +
                        "       bearb_date, " +
                        "       bearb_time " +
                        "   ) " +
                        "   Select " +
                        "   (Select Max(id) From U_PackingLabel) + ROW_NUMBER() OVER(ORDER BY id ASC), " +
                        "   ?, " +
                        "   report, " +
                        "   labelcount, " +
                        "   kommentar, " +
                        "   aktiviert, " +
                        "   bearb, " +
                        "   bearb_date, " +
                        "   bearb_time " +
                        " From " +
                        "   U_PackingLabel " +
                        " Where " +
                        "   U_PackingScheme_id = ? " +
                        " Set IDENTITY_INSERT U_PackingLabel Off ";
                    if (DebugMessages) write("UPackingschemeInsert", "copyResources = " + copyResources);
                    CopyResources(logger, sqlConverter, connection, id, old, copyResources);
                }
                try 
                {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException exc) {
                    logger.error("Error when closing DB connection", exc);
                } 
            }
            catch (ClassCastException e) {
                logger.error("Error", e);
                logger.error(e.getMessage());
            }
        }
    }
    
    private void CopyResources(ISdiLogger logger, IToNativeSqlConverter sqlConverter, Connection connection, int id, int old, String sql) {
        java.sql.PreparedStatement stmt = null;
    
        try {
            stmt = connection.prepareStatement(sqlConverter.toNativeSql(sql));
            stmt.setInt(1, id);
            stmt.setInt(2, old);
            if (DebugMessages) write("UPackingschemeInsert", "id = " + id + " old = " + old);
            int res = stmt.executeUpdate();
            if (DebugMessages) write("UPackingschemeInsert", "res = " + res);
        } catch (SQLException e) {
            logger.error("Error executing sql.", e);
            if (DebugMessages) write("UPackingschemeInsert", "Error: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException exc) {
                logger.error("Error closing statement", exc);
            } 
        } 
    }
    
    /*
    private void deleteDocuments(ISdiLogger logger, IToNativeSqlConverter sqlConverter, Connection connection, int fmeaId) {
        java.sql.PreparedStatement stmt = null;
        try {
            String deleteTeamData = "DELETE FROM hyd_document WHERE object = 'FMEA' AND key1='fmea.id'  AND value1 = ?";
            stmt = connection.prepareStatement(sqlConverter.toNativeSql("DELETE FROM hyd_document WHERE object = 'FMEA' AND key1='fmea.id'  AND value1 = ?"));
            stmt.setInt(1, fmeaId);
            stmt.execute();
        } catch (SQLException e) {
            logger.error("Error while executing sql.", e);
        } finally {
            //JdbcUtil jdbcUtil = JdbcUtilFactory.createJdbcUtil(getClass(), this.factory);
            //jdbcUtil.closeQuietly(stmt);
            //jdbcUtil.closeQuietly(connection);
            try
            {
                if (stmt != null) stmt.close();
            }
            catch (SQLException ex) {
                Logger.getLogger(UPackingschemeDelete.class.getName()).log(Level.SEVERE, null, ex);
            }            finally
            {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(UPackingschemeDelete.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } 
    }*/
}