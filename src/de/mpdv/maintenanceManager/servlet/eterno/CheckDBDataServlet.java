package de.mpdv.maintenanceManager.servlet.eterno;

import de.mpdv.maintenanceManager.gui.CommonResponseFrame;
import de.mpdv.maintenanceManager.logic.SessionManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CheckDBDataServlet extends HttpServlet {

   private static final long serialVersionUID = 3614040670713151430L;
   
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
   {
      String ParamData = "";
      if(SessionManager.checkLogin(request, response)) 
      {
          Connection DBConnect = null;
          try 
          {
              Class.forName("net.sourceforge.jtds.jdbc.Driver");
              DBConnect = DriverManager.getConnection("sa", "hydadm", "jdbc:jtds:sqlserver://etn-srv-mes2/hydra1;instance=hydms1");
          } catch (ClassNotFoundException ex) {
              Logger.getLogger(CheckDBDataServlet.class.getName()).log(Level.SEVERE, null, ex);
              ParamData = ex.getMessage();
          } catch (SQLException ex) {
              Logger.getLogger(CheckDBDataServlet.class.getName()).log(Level.SEVERE, null, ex);
              ParamData = ex.getMessage();
          }
          finally
          {
              if (DBConnect != null)
              {
                  ParamData = DBConnect.toString();
                  Statement SqlStm = null;
                  try 
                  {
                      SqlStm = DBConnect.createStatement();
                  } 
                  catch (SQLException ex) 
                  {
                      Logger.getLogger(CheckDBDataServlet.class.getName()).log(Level.SEVERE, null, ex);
                      ParamData = ex.getMessage();
                  }
                  finally
                  {
                      if (SqlStm != null)
                      {
                          ParamData = SqlStm.toString();
                          ResultSet rs = null;
                          String sql = "SELECT isnull(DB_NAME(), '') AS dbname";
                          
                          try 
                          {
                              rs = SqlStm.executeQuery(sql);
                          } 
                          catch (SQLException ex) 
                          {
                              Logger.getLogger(CheckDBDataServlet.class.getName()).log(Level.SEVERE, null, ex);
                              ParamData = ex.getMessage();
                          }
                          finally
                          {
                              if (rs != null)
                              {
                                  try {
                                      if (rs.next())
                                      {
                                          try {
                                              ParamData = rs.getString("dbname");
                                          } catch (SQLException ex) {
                                              Logger.getLogger(CheckDBDataServlet.class.getName()).log(Level.SEVERE, null, ex);
                                              ParamData = ex.getMessage();
                                          }
                                      }
                                      else
                                      {
                                          ParamData = rs.toString();
                                      }
                                  } catch (SQLException ex) {
                                      Logger.getLogger(CheckDBDataServlet.class.getName()).log(Level.SEVERE, null, ex);
                                      ParamData = ex.getMessage();
                                  }
                                  
                                  try {
                                      rs.close();
                                  } catch (SQLException ex) {
                                      Logger.getLogger(CheckDBDataServlet.class.getName()).log(Level.SEVERE, null, ex);
                                      ParamData = ex.getMessage();
                                  }
                              }
                              try {
                                  SqlStm.close();
                              } catch (SQLException ex) {
                                  Logger.getLogger(CheckDBDataServlet.class.getName()).log(Level.SEVERE, null, ex);
                                  ParamData = ex.getMessage();
                              }
                          }
                      }
                      try {
                          DBConnect.close();
                      } catch (SQLException ex) {
                          Logger.getLogger(CheckDBDataServlet.class.getName()).log(Level.SEVERE, null, ex);
                          ParamData = ex.getMessage();
                      }
                  }
                  
                  CommonResponseFrame.printToResponse(this.getPageEntryResponse(ParamData), response);
              }
          }
      }
   }

   private String getPageEntryResponse(String ParamData) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("  <h1>DBConnect information</h1>\n");
      builder.append(String.format("%1$s <br />\n", ParamData));
      builder.append("</div>\n");
      return builder.toString();
   }
}