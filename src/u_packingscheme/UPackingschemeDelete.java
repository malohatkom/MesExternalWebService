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
public class UPackingschemeDelete {
    private ISystemUtilFactory factory;
  
    public void sdiAfterPerformAction(IUserExitParam param) {
        SdiAfterPerformActionParam aParam = (SdiAfterPerformActionParam)param.get("param");
        this.factory = (ISystemUtilFactory)param.get("factory");
        ISdiLoggerProvider logerProvider = (ISdiLoggerProvider)this.factory.fetchUtil("LoggerProvider");
        ISdiLogger logger = logerProvider.fetchLogger(UPackingschemeDelete.class);
        IToNativeSqlConverter sqlConverter = (IToNativeSqlConverter)this.factory.fetchUtil("ToNativeSqlConverter");
        IDbConnectionProvider dbConnectionProvider = (IDbConnectionProvider)this.factory.fetchUtil("DbConnectionProvider");
        Connection connection = dbConnectionProvider.fetchDbConnection();
    
        SpecialParam PackingSchemeKey = (SpecialParam)aParam.getSpecialParameters().get("PackingScheme.key");
        if (PackingSchemeKey != null) {
            try {
                System.out.println("UPackingschemeDelete");
                System.out.println("PackingSchemeKey = " +PackingSchemeKey.getValue().toString());
                
                int id = Integer.parseInt(PackingSchemeKey.getValue().toString());
                String deleteMaterials = "Delete [hydra1].[hydadm].[U_PackingMaterial] Where U_PackingScheme_id = ?";
                DeleteMaterials(logger, sqlConverter, connection, id, deleteMaterials);
            }
            catch (ClassCastException e) {
                logger.error("Error", e);
                logger.error(e.getMessage());
            } 
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException exc) {
            logger.error("Error", exc);
        } 
    }
    
    private void DeleteMaterials(ISdiLogger logger, IToNativeSqlConverter sqlConverter, Connection connection, int id, String sql) {
        java.sql.PreparedStatement stmt = null;
    
        try {
            stmt = connection.prepareStatement(sqlConverter.toNativeSql(sql));
            stmt.setInt(1, id);
            stmt.execute();
        } catch (SQLException e) {
            logger.error("Error executing sql.", e);
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