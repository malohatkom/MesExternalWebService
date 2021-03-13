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
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author mikhail.malokhatko
 */
public class UPackingschemeInsert {
    
    private final boolean DebugMessages = true;
    
    private ISystemUtilFactory factory;
  
    public void sdiAfterPerformAction(IUserExitParam param) {
        if (DebugMessages) System.out.println("UPackingschemeInsert");
        
        SdiAfterPerformActionParam aParam = (SdiAfterPerformActionParam)param.get("param");
        this.factory = (ISystemUtilFactory)param.get("factory");
        ISdiLoggerProvider logerProvider = (ISdiLoggerProvider)this.factory.fetchUtil("LoggerProvider");
        ISdiLogger logger = logerProvider.fetchLogger(UPackingschemeInsert.class);
        logger.debug("Testing UPackingschemeInsert");
        SpecialParam PackingSchemeCopyMaterials = (SpecialParam)aParam.getSpecialParameters().get("PackingScheme.copy_materials");
        if (DebugMessages) System.out.println("PackingSchemeCopyMaterials = " + PackingSchemeCopyMaterials.getValue().toString());
        SpecialParam PackingSchemeOldKey = (SpecialParam)aParam.getSpecialParameters().get("PackingScheme.old_key");
        
        if (PackingSchemeCopyMaterials != null ? "true".equals(PackingSchemeCopyMaterials.getValue().toString()) : false)
        {
            if (PackingSchemeOldKey != null) {
            try {
                    if (DebugMessages) System.out.println("PackingSchemeOldKey = " + PackingSchemeOldKey.getValue().toString());
                    if (DebugMessages) System.out.println("PackingSchemeKey = " + aParam.getCreatedSerial().toString());
            
                    IToNativeSqlConverter sqlConverter = (IToNativeSqlConverter)this.factory.fetchUtil("ToNativeSqlConverter");
                    IDbConnectionProvider dbConnectionProvider = (IDbConnectionProvider)this.factory.fetchUtil("DbConnectionProvider");
                    Connection connection = dbConnectionProvider.fetchDbConnection();
                
                    int old = Integer.parseInt(PackingSchemeOldKey.getValue().toString());
                    int id = Integer.parseInt(aParam.getCreatedSerial().toString());
                    String copyMaterials = 
                        " Set IDENTITY_INSERT [hydra1].[hydadm].[U_PackingMaterial] On " +
                        " Insert Into [hydra1].[hydadm].[U_PackingMaterial] (id, U_PackingScheme_id, artikel, artikel_bez, bez_1, bez_2, hz_typ, soll_menge, soll_einh, bearb, bearb_date,bearb_time) " +
                        " Select (Select Max(id) From [hydra1].[hydadm].[U_PackingMaterial]) + ROW_NUMBER() OVER(ORDER BY id ASC), ?, [artikel], [artikel_bez], [bez_1], [bez_2], [hz_typ], [soll_menge], [soll_einh], [bearb], [bearb_date], [bearb_time] From [hydra1].[hydadm].[U_PackingMaterial] Where U_PackingScheme_id = ? " +
                        " Set IDENTITY_INSERT [hydra1].[hydadm].[U_PackingMaterial] Off ";
                    CopyMaterials(logger, sqlConverter, connection, id, old, copyMaterials);
                    try {
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (SQLException exc) {
                        logger.error("Unknown exception when closing DB connection", exc);
                    } 
                }
                catch (ClassCastException e) {
                    logger.error("Error when converting special parameters. Team members, documents and structure elements of FMEA could not be deleted.");
                    logger.error(e.getMessage());
                } 
            }
        }
    }
    
    private void CopyMaterials(ISdiLogger logger, IToNativeSqlConverter sqlConverter, Connection connection, int id, int old, String sql) {
        java.sql.PreparedStatement stmt = null;
    
        try {
            stmt = connection.prepareStatement(sqlConverter.toNativeSql(sql));
            stmt.setInt(1, id);
            stmt.setInt(2, old);
            stmt.execute();
        } catch (SQLException e) {
            logger.error("Error while executing sql.", e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException exc) {
                logger.error("Unknown exception when closing statement", exc);
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