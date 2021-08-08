package com.example;
 
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
public class TestServlet extends HttpServlet {
 
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String ParamData = "";
        
            Connection DBConnect = null;
            try 
            {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
            } 
            catch (ClassNotFoundException ex) 
            {
                Logger.getLogger(TestServlet.class.getName()).log(Level.SEVERE, null, ex);
                ParamData = ex.getMessage();
            }
            finally
            {
                try 
                {
                    DBConnect = DriverManager.getConnection("sa", "hydadm", "jdbc:jtds:sqlserver://10.58.16.18:57613/hydra1");
                } 
                catch (SQLException ex) 
                {
                    Logger.getLogger(TestServlet.class.getName()).log(Level.SEVERE, null, ex);
                    ParamData = ex.getMessage();
                }
                finally
                {
                    if (DBConnect != null)
                    {
                        ResultSet rs = null;
                        try 
                        {
                            rs = DBConnect.createStatement().executeQuery("SELECT isnull(DB_NAME(), '') AS dbname");
                            if (rs != null)
                            {
                                if (rs.next())
                                {
                                    ParamData = rs.getString("dbname");
                                }
                                rs.close();
                            }
                        } 
                        catch (SQLException ex) 
                        {
                            Logger.getLogger(TestServlet.class.getName()).log(Level.SEVERE, null, ex);
                            ParamData = ex.getMessage();
                        }
                        try 
                        {
                            DBConnect.close();
                        } catch (SQLException ex) {
                            Logger.getLogger(TestServlet.class.getName()).log(Level.SEVERE, null, ex);
                            ParamData = ex.getMessage();
                        }
                    }
                }
            }
            response.setContentType("text/html;charset=utf-8");
                    PrintWriter pw = response.getWriter();
                
        pw.println(String.format("<H1>%1$s</H1>", ParamData));

        }
   }    
    
