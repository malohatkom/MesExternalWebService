package de.mpdv.maintenanceManager.servlet.maintenance;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.gui.CommonResponseFrame;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.servlet.maintenance.HydraPathMaintenanceServlet.ParamStruct;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.DbUtil;
import de.mpdv.maintenanceManager.util.Util;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HydraPathMaintenanceServlet extends HttpServlet {

   private static final long serialVersionUID = -1609819323476693946L;

class ParamStruct {

   String dbType;
   String dbConnString;
   String dbUser;
   String dbPass;
   String pathId;
   String pathProtocol;
   String pathUser;
   String pathPass;
   String pathHost;
   String pathPort;
   String pathUrl;
   String pathDesignation;


}
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      if(SessionManager.checkLogin(request, response)) {
         File mm2ConfigFile = Configuration.getMM2ConfigFile();
         if(mm2ConfigFile.exists()) {
            CommonResponseFrame.printToResponse("Maintananace Manager 2.0 is installed! Please use the new version!", response);
         } else {
            String action = request.getParameter("action");
            ParamStruct struct = new ParamStruct();
            struct.dbConnString = request.getParameter("dbConnString");
            struct.dbType = request.getParameter("dbType");
            struct.dbPass = request.getParameter("dbPass");
            struct.dbUser = request.getParameter("dbUser");
            struct.pathId = request.getParameter("pathId");
            struct.pathDesignation = request.getParameter("pathDesignation");
            struct.pathHost = request.getParameter("pathHost");
            struct.pathPass = request.getParameter("pathPass");
            struct.pathPort = request.getParameter("pathPort");
            struct.pathProtocol = request.getParameter("pathProtocol");
            struct.pathUrl = request.getParameter("pathUrl");
            struct.pathUser = request.getParameter("pathUser");

            try {
               this.checkCommonParams(action, struct);
            } catch (Exception var11) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var11));
               CommonResponseFrame.printToResponse(this.getPageEntryResponse(struct, "Could not execute the action " + action + " of Path maintenance application because: " + var11.getMessage(), (String)null, false), response);
               return;
            }

            if(action != null && !action.equals("")) {
               try {
                  this.checkParams(action, struct);
               } catch (Exception var10) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var10));
                  CommonResponseFrame.printToResponse(this.getListResponse(struct, "Could not execute the action " + action + " of Path maintenance application because: " + var10.getMessage(), (String)null), response);
                  return;
               }

               if(action.equals("list")) {
                  CommonResponseFrame.printToResponse(this.getListResponse(struct, (String)null, (String)null), response);
               } else if(action.equals("editView")) {
                  CommonResponseFrame.printToResponse(this.getInsertResponse(struct, true, false), response);
               } else if(action.equals("insertView")) {
                  CommonResponseFrame.printToResponse(this.getInsertResponse(struct, false, false), response);
               } else if(action.equals("deleteView")) {
                  CommonResponseFrame.printToResponse(this.getDeleteResponse(struct), response);
               } else if(action.equals("copyView")) {
                  CommonResponseFrame.printToResponse(this.getInsertResponse(struct, false, true), response);
               } else if(action.equals("edit")) {
                  try {
                     this.updateEntry(struct);
                     CommonResponseFrame.printToResponse(this.getListResponse(struct, (String)null, "Successfully updated entry"), response);
                  } catch (Exception var7) {
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var7));
                     CommonResponseFrame.printToResponse(this.getListResponse(struct, "Could not execute the action " + action + " of Path maintenance application because: " + var7.getMessage(), (String)null), response);
                  }
               } else if(action.equals("insert")) {
                  try {
                     this.insertEntry(struct);
                     CommonResponseFrame.printToResponse(this.getListResponse(struct, (String)null, "Successfully inserted new entry"), response);
                  } catch (Exception var8) {
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var8));
                     CommonResponseFrame.printToResponse(this.getListResponse(struct, "Could not execute the action " + action + " of Path maintenance application because: " + var8.getMessage(), (String)null), response);
                  }
               } else if(action.equals("delete")) {
                  try {
                     this.deleteEntry(struct);
                     CommonResponseFrame.printToResponse(this.getListResponse(struct, (String)null, "Successfully deleted the entry"), response);
                  } catch (Exception var9) {
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var9));
                     CommonResponseFrame.printToResponse(this.getListResponse(struct, "Could not execute the action " + action + " of Path maintenance application because: " + var9.getMessage(), (String)null), response);
                  }
               }
            } else {
               CommonResponseFrame.printToResponse(this.getPageEntryResponse(struct, (String)null, (String)null, false), response);
            }
         }
      }
   }

   private void deleteEntry(ParamStruct struct) {
      Connection con = null;
      PreparedStatement stmt = null;

      try {
         con = DbUtil.getDbConnection(struct.dbUser, struct.dbPass, struct.dbType, struct.dbConnString);
         stmt = con.prepareStatement("delete from hy_path where path=?");
         stmt.setString(1, struct.pathId);
         stmt.executeUpdate();
      } catch (Exception var16) {
         throw new RuntimeException("Could not delete: " + var16.getMessage());
      } finally {
         if(stmt != null) {
            try {
               stmt.close();
            } catch (SQLException var15) {
               ;
            }
         }

         if(con != null) {
            try {
               con.close();
            } catch (SQLException var14) {
               ;
            }
         }

      }

   }

   private void updateEntry(ParamStruct struct) {
      Connection con = null;
      PreparedStatement stmt = null;

      try {
         con = DbUtil.getDbConnection(struct.dbUser, struct.dbPass, struct.dbType, struct.dbConnString);
         StringBuilder e = new StringBuilder();
         e.append(" p_scheme=?");
         e.append(", p_user=?");
         e.append(", p_host=?");
         e.append(", p_port=?");
         e.append(", p_url_path=?");
         e.append(", bem=?");
         if(struct.pathPass != null && !struct.pathPass.equals("")) {
            e.append(", p_password=?");
         }

         stmt = con.prepareStatement("update hy_path set " + e.toString() + " where path=?");
         stmt.setString(1, struct.pathProtocol);
         stmt.setString(2, struct.pathUser);
         stmt.setString(3, struct.pathHost);

         try {
            Integer e1 = Integer.valueOf(struct.pathPort);
            stmt.setInt(4, e1.intValue());
         } catch (NumberFormatException var18) {
            throw new IllegalArgumentException("The specified port is not numeric");
         }

         stmt.setString(5, struct.pathUrl);
         stmt.setString(6, struct.pathDesignation);
         if(struct.pathPass != null && !struct.pathPass.equals("")) {
            stmt.setString(7, struct.pathPass);
            stmt.setString(8, struct.pathId);
         } else {
            stmt.setString(7, struct.pathId);
         }

         stmt.executeUpdate();
      } catch (Exception var19) {
         throw new RuntimeException("Could not update: " + var19.getMessage());
      } finally {
         if(stmt != null) {
            try {
               stmt.close();
            } catch (SQLException var17) {
               ;
            }
         }

         if(con != null) {
            try {
               con.close();
            } catch (SQLException var16) {
               ;
            }
         }

      }

   }

   private void insertEntry(ParamStruct struct) {
      Connection con = null;
      PreparedStatement stmt = null;

      try {
         con = DbUtil.getDbConnection(struct.dbUser, struct.dbPass, struct.dbType, struct.dbConnString);
         StringBuilder e = new StringBuilder();
         StringBuilder valueBuilder = new StringBuilder();
         e.append("path");
         valueBuilder.append("?");
         e.append(", p_scheme");
         valueBuilder.append(", ?");
         if(struct.pathUser != null && !struct.pathUser.equals("")) {
            e.append(", p_user");
            valueBuilder.append(", ?");
         }

         if(struct.pathPass != null && !struct.pathPass.equals("")) {
            e.append(", p_password");
            valueBuilder.append(", ?");
         }

         if(struct.pathHost != null && !struct.pathHost.equals("")) {
            e.append(", p_host");
            valueBuilder.append(", ?");
         }

         if(struct.pathPort != null && !struct.pathPort.equals("")) {
            e.append(", p_port");
            valueBuilder.append(", ?");
         }

         if(struct.pathUrl != null && !struct.pathUrl.equals("")) {
            e.append(", p_url_path");
            valueBuilder.append(", ?");
         }

         if(struct.pathDesignation != null && !struct.pathDesignation.equals("")) {
            e.append(", bem");
            valueBuilder.append(", ?");
         }

         stmt = con.prepareStatement("insert into hy_path (" + e.toString() + ") values (" + valueBuilder.toString() + ")");
         stmt.setString(1, struct.pathId);
         stmt.setString(2, struct.pathProtocol);
         int idx = 3;
         if(struct.pathUser != null && !struct.pathUser.equals("")) {
            stmt.setString(idx, struct.pathUser);
            ++idx;
         }

         if(struct.pathPass != null && !struct.pathPass.equals("")) {
            stmt.setString(idx, struct.pathPass);
            ++idx;
         }

         if(struct.pathHost != null && !struct.pathHost.equals("")) {
            stmt.setString(idx, struct.pathHost);
            ++idx;
         }

         if(struct.pathPort != null && !struct.pathPort.equals("")) {
            try {
               Integer e1 = Integer.valueOf(struct.pathPort);
               stmt.setInt(idx, e1.intValue());
               ++idx;
            } catch (NumberFormatException var20) {
               throw new IllegalArgumentException("The specified port is not numeric");
            }
         }

         if(struct.pathUrl != null && !struct.pathUrl.equals("")) {
            stmt.setString(idx, struct.pathUrl);
            ++idx;
         }

         if(struct.pathDesignation != null && !struct.pathDesignation.equals("")) {
            stmt.setString(idx, struct.pathDesignation);
            ++idx;
         }

         stmt.executeUpdate();
      } catch (Exception var21) {
         throw new RuntimeException("Could not insert: " + var21.getMessage());
      } finally {
         if(stmt != null) {
            try {
               stmt.close();
            } catch (SQLException var19) {
               ;
            }
         }

         if(con != null) {
            try {
               con.close();
            } catch (SQLException var18) {
               ;
            }
         }

      }

   }

   private void checkCommonParams(String action, ParamStruct struct) {
      if(action != null) {
         if(!"edit".equals(action) && !"insert".equals(action) && !"delete".equals(action) && !"list".equals(action) && !"editView".equals(action) && !"insertView".equals(action) && !"deleteView".equals(action) && !"copyView".equals(action)) {
            throw new IllegalArgumentException("Unknown action " + action);
         } else if(struct.dbUser != null && !struct.dbUser.equals("")) {
            if(struct.dbPass != null && !struct.dbPass.equals("")) {
               if(struct.dbType != null && !struct.dbType.equals("")) {
                  if(struct.dbConnString == null || struct.dbConnString.equals("")) {
                     throw new IllegalArgumentException("The database connection string is not specified");
                  }
               } else {
                  throw new IllegalArgumentException("The database type is not specified");
               }
            } else {
               throw new IllegalArgumentException("The database password is not specified");
            }
         } else {
            throw new IllegalArgumentException("The database user is not specified");
         }
      }
   }

   private void checkParams(String action, ParamStruct struct) {
      if(action != null) {
         if("delete".equals(action) && (struct.pathId == null || struct.pathId.equals(""))) {
            throw new IllegalArgumentException("The path id is not specified");
         } else {
            if("edit".equals(action)) {
               if(struct.pathId == null || struct.pathId.equals("")) {
                  throw new IllegalArgumentException("The path id is not specified");
               }

               if(struct.pathProtocol == null || struct.pathProtocol.equals("")) {
                  throw new IllegalArgumentException("The path protocol is not specified");
               }
            }

            if("insert".equals(action)) {
               if(struct.pathId == null || struct.pathId.equals("")) {
                  throw new IllegalArgumentException("The path id is not specified");
               }

               if(struct.pathProtocol == null || struct.pathProtocol.equals("")) {
                  throw new IllegalArgumentException("The path protocol is not specified");
               }
            }

         }
      }
   }

   private String getInsertResponse(ParamStruct struct, boolean editMode, boolean copyMode) {
      StringBuilder builder = new StringBuilder();
      builder.append(this.getPageEntryResponse(struct, (String)null, (String)null, true));
      builder.append("<br />\n");
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      if(editMode) {
         builder.append("<h2>Edit entry</h2>\n");
      } else {
         builder.append("<h2>New entry</h2>\n");
      }

      builder.append("<br />\n");
      if(editMode) {
         builder.append("<form action=\"HydraPathMaintenance\" method=\"post\">\n");
         builder.append("<input type=\"hidden\" name=\"action\" value=\"edit\" />\n");
      } else if(copyMode) {
         builder.append("<form action=\"HydraPathMaintenance\" method=\"post\">\n");
         builder.append("<input type=\"hidden\" name=\"action\" value=\"insert\" />\n");
      } else {
         builder.append("<form action=\"HydraPathMaintenance\" method=\"post\">\n");
         builder.append("<input type=\"hidden\" name=\"action\" value=\"insert\" />\n");
      }

      String name = null;
      String protocol = null;
      String user = null;
      String host = null;
      Integer port = null;
      String url = null;
      String designation = null;
      if(editMode || copyMode) {
         Connection con = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;

         String var16;
         try {
            con = DbUtil.getDbConnection(struct.dbUser, struct.dbPass, struct.dbType, struct.dbConnString);
            stmt = con.prepareStatement("select path, p_scheme, p_user, p_host, p_port, p_url_path, bem from hy_path where path=?");
            stmt.setString(1, struct.pathId);
            rs = stmt.executeQuery();
            if(rs.next()) {
               name = rs.getString(1);
               protocol = rs.getString(2);
               user = rs.getString(3);
               host = rs.getString(4);
               int e1 = rs.getInt(5);
               if(!rs.wasNull()) {
                  port = Integer.valueOf(e1);
               }

               url = rs.getString(6);
               designation = rs.getString(7);
               break label178;
            }

            String e = this.getListResponse(struct, "Could not find the entry to copy/edit", (String)null);
            return e;
         } catch (Exception var37) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var37));
            var16 = this.getListResponse(struct, "Could not load the entry to copy/edit: " + var37.getMessage(), (String)null);
         } finally {
            if(rs != null) {
               try {
                  rs.close();
               } catch (SQLException var36) {
                  ;
               }
            }

            if(stmt != null) {
               try {
                  stmt.close();
               } catch (SQLException var35) {
                  ;
               }
            }

            if(con != null) {
               try {
                  con.close();
               } catch (SQLException var34) {
                  ;
               }
            }

         }

         return var16;
      }

      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      builder.append("Name:\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      if(editMode) {
         builder.append("<input type=\"text\" name=\"pathId\" size=\"50\" length=\"8\" readonly=\"readonly\" value=\"" + (name == null?"":name) + "\" />\n");
      } else if(copyMode) {
         builder.append("<input type=\"text\" name=\"pathId\" size=\"50\" length=\"8\" value=\"" + (name == null?"":name) + "\" />\n");
      } else {
         builder.append("<input type=\"text\" name=\"pathId\" size=\"50\" length=\"8\" />\n");
      }

      builder.append("</div>\n");
      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      builder.append("Protocol:\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      builder.append("<select name=\"pathProtocol\" size=\"1\" >\n");
      if(!editMode && !copyMode) {
         builder.append("<option>file</option>\n");
         builder.append("<option>localfile</option>\n");
         builder.append("<option>ftp</option>\n");
         builder.append("<option>hydra</option>\n");
         builder.append("<option>http</option>\n");
         builder.append("<option>ftps</option>\n");
         builder.append("<option>smtp</option>\n");
      } else {
         builder.append("<option " + (protocol != null && protocol.equals("file")?"selected=\"selected\"":"") + ">file</option>\n");
         builder.append("<option " + (protocol != null && protocol.equals("localfile")?"selected=\"selected\"":"") + ">localfile</option>\n");
         builder.append("<option " + (protocol != null && protocol.equals("ftp")?"selected=\"selected\"":"") + ">ftp</option>\n");
         builder.append("<option " + (protocol != null && protocol.equals("hydra")?"selected=\"selected\"":"") + ">hydra</option>\n");
         builder.append("<option " + (protocol != null && protocol.equals("http")?"selected=\"selected\"":"") + ">http</option>\n");
         builder.append("<option " + (protocol != null && protocol.equals("ftps")?"selected=\"selected\"":"") + ">ftps</option>\n");
         builder.append("<option " + (protocol != null && protocol.equals("smtp")?"selected=\"selected\"":"") + ">smtp</option>\n");
      }

      builder.append("</select><br />\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      builder.append("User:\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      if(!editMode && !copyMode) {
         builder.append("<input type=\"text\" name=\"pathUser\" size=\"50\" length=\"20\" />\n");
      } else {
         builder.append("<input type=\"text\" name=\"pathUser\" size=\"50\" length=\"20\" value=\"" + (user == null?"":user) + "\" />\n");
      }

      builder.append("</div>\n");
      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      if(editMode) {
         builder.append("Password:<br />(<b>empty to keep</b>)\n");
      } else {
         builder.append("Password:\n");
      }

      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      if(editMode) {
         builder.append("<input type=\"password\" name=\"pathPass\" size=\"50\" length=\"20\" /><br />&nbsp;\n");
      } else {
         builder.append("<input type=\"password\" name=\"pathPass\" size=\"50\" length=\"20\" />\n");
      }

      builder.append("</div>\n");
      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      builder.append("Host:\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      if(!editMode && !copyMode) {
         builder.append("<input type=\"text\" name=\"pathHost\" size=\"50\" length=\"40\" />\n");
      } else {
         builder.append("<input type=\"text\" name=\"pathHost\" size=\"50\" length=\"40\" value=\"" + (host == null?"":host) + "\" />\n");
      }

      builder.append("</div>\n");
      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      builder.append("Port:\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      if(!editMode && !copyMode) {
         builder.append("<input type=\"text\" name=\"pathPort\" size=\"50\" length=\"8\" />\n");
      } else {
         builder.append("<input type=\"text\" name=\"pathPort\" size=\"50\" length=\"8\" value=\"" + (port == null?"":port) + "\" />\n");
      }

      builder.append("</div>\n");
      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      builder.append("URL:\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      if(!editMode && !copyMode) {
         builder.append("<input type=\"text\" name=\"pathUrl\" size=\"50\" length=\"80\" />\n");
      } else {
         builder.append("<input type=\"text\" name=\"pathUrl\" size=\"50\" length=\"80\" value=\"" + (url == null?"":url) + "\" />\n");
      }

      builder.append("</div>\n");
      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      builder.append("Designation:\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      if(!editMode && !copyMode) {
         builder.append("<input type=\"text\" name=\"pathDesignation\" size=\"50\" length=\"80\" />\n");
      } else {
         builder.append("<input type=\"text\" name=\"pathDesignation\" size=\"50\" length=\"80\" value=\"" + (designation == null?"":designation) + "\" />\n");
      }

      builder.append("</div>\n");
      builder.append("<br /><br />\n");
      builder.append("<div style=\"width:50%;float:left;text-align:right;\">\n");
      builder.append("<input type=\"hidden\" name=\"dbUser\" value=\"" + struct.dbUser + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbPass\" value=\"" + struct.dbPass + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbConnString\" value=\"" + struct.dbConnString + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbType\" value=\"" + struct.dbType + "\" />\n");
      builder.append("<input type=\"image\" src=\"img/button-ok.png\" style=\"width:20px;height:20px;\" alt=\"OK\" />&nbsp;&nbsp;&nbsp;\n");
      builder.append("</form>\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:50%;float:left;text-align:left;\">\n");
      builder.append("<form action=\"HydraPathMaintenance\" method=\"post\">\n");
      builder.append("<input type=\"hidden\" name=\"action\" value=\"list\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbUser\" value=\"" + struct.dbUser + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbPass\" value=\"" + struct.dbPass + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbConnString\" value=\"" + struct.dbConnString + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbType\" value=\"" + struct.dbType + "\" />\n");
      builder.append("&nbsp;&nbsp;&nbsp;<input type=\"image\" src=\"img/button-cancel.png\" style=\"width:20px;height:20px;\" alt=\"Cancel\" />\n");
      builder.append("</form>\n");
      builder.append("</div>\n");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getDeleteResponse(ParamStruct struct) {
      StringBuilder builder = new StringBuilder();
      builder.append(this.getPageEntryResponse(struct, (String)null, (String)null, true));
      builder.append("<br />\n");
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<b>Are you sure to delete the following entry?</b><br /><br />\n");
      builder.append("<table border=\"2\" style=\"font-size:75%;margin-left:auto;margin-right:auto;width:90%;\">\n");
      builder.append("<tr>\n");
      builder.append("<th>Name</th>\n");
      builder.append("<th>Protocol</th>\n");
      builder.append("<th>User</th>\n");
      builder.append("<th>Host</th>\n");
      builder.append("<th>Port</th>\n");
      builder.append("<th>Path</th>\n");
      builder.append("<th>Designation</th>\n");
      builder.append("</tr>\n");
      Connection con = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;

      label222: {
         String var7;
         try {
            con = DbUtil.getDbConnection(struct.dbUser, struct.dbPass, struct.dbType, struct.dbConnString);
            stmt = con.prepareStatement("select path, p_scheme, p_user, p_host, p_port, p_url_path, bem from hy_path where path=?");
            stmt.setString(1, struct.pathId);
            rs = stmt.executeQuery();

            while(true) {
               if(!rs.next()) {
                  break label222;
               }

               builder.append("<tr>");
               builder.append("<td>" + (rs.getString(1) == null?"":rs.getString(1)) + "</td>\n");
               builder.append("<td>" + (rs.getString(2) == null?"":rs.getString(2)) + "</td>\n");
               builder.append("<td>" + (rs.getString(3) == null?"":rs.getString(3)) + "</td>\n");
               builder.append("<td>" + (rs.getString(4) == null?"":rs.getString(4)) + "</td>\n");
               int e = rs.getInt(5);
               builder.append("<td>" + (rs.wasNull()?"":Integer.valueOf(e)) + "</td>\n");
               builder.append("<td>" + (rs.getString(6) == null?"":rs.getString(6)) + "</td>\n");
               builder.append("<td>" + (rs.getString(7) == null?"":rs.getString(7)) + "</td>\n");
               builder.append("</tr>\n");
            }
         } catch (Exception var25) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var25));
            var7 = this.getListResponse(struct, "Could not load the entry to delete: " + var25.getMessage(), (String)null);
         } finally {
            if(rs != null) {
               try {
                  rs.close();
               } catch (SQLException var24) {
                  ;
               }
            }

            if(stmt != null) {
               try {
                  stmt.close();
               } catch (SQLException var23) {
                  ;
               }
            }

            if(con != null) {
               try {
                  con.close();
               } catch (SQLException var22) {
                  ;
               }
            }

         }

         return var7;
      }

      builder.append("</table>\n");
      builder.append("<br /><br />\n");
      builder.append("<div style=\"width:50%;float:left;text-align:right;\">\n");
      builder.append("<form action=\"HydraPathMaintenance\" method=\"post\">\n");
      builder.append("<input type=\"hidden\" name=\"action\" value=\"delete\" />\n");
      builder.append("<input type=\"hidden\" name=\"pathId\" value=\"" + struct.pathId + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbUser\" value=\"" + struct.dbUser + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbPass\" value=\"" + struct.dbPass + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbConnString\" value=\"" + struct.dbConnString + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbType\" value=\"" + struct.dbType + "\" />\n");
      builder.append("<input type=\"image\" src=\"img/button-ok.png\" style=\"width:20px;height:20px;\" alt=\"OK\" />&nbsp;&nbsp;&nbsp;\n");
      builder.append("</form>\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:50%;float:left;text-align:left;\">\n");
      builder.append("<form action=\"HydraPathMaintenance\" method=\"post\">\n");
      builder.append("<input type=\"hidden\" name=\"action\" value=\"list\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbUser\" value=\"" + struct.dbUser + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbPass\" value=\"" + struct.dbPass + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbConnString\" value=\"" + struct.dbConnString + "\" />\n");
      builder.append("<input type=\"hidden\" name=\"dbType\" value=\"" + struct.dbType + "\" />\n");
      builder.append("&nbsp;&nbsp;&nbsp;<input type=\"image\" src=\"img/button-cancel.png\" style=\"width:20px;height:20px;\" alt=\"Cancel\" />\n");
      builder.append("</form>\n");
      builder.append("</div>\n");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getListResponse(ParamStruct struct, String errorMessage, String successMessage) {
      StringBuilder builder = new StringBuilder();
      builder.append(this.getPageEntryResponse(struct, errorMessage, successMessage, false));
      builder.append("<br />\n");
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h2>Existing entries</h2>\n");
      builder.append("<form action=\"HydraPathMaintenance\" method=\"post\">\n<input type=\"hidden\" name=\"action\" value=\"insertView\" />\n<input type=\"hidden\" name=\"dbUser\" value=\"" + struct.dbUser + "\" />\n" + "<input type=\"hidden\" name=\"dbPass\" value=\"" + struct.dbPass + "\" />\n" + "<input type=\"hidden\" name=\"dbConnString\" value=\"" + struct.dbConnString + "\" />\n" + "<input type=\"hidden\" name=\"dbType\" value=\"" + struct.dbType + "\" />\n" + "<input type=\"submit\" value=\"New entry\" />\n" + "</form>\n");
      builder.append("</tr>\n");
      builder.append("<br />\n");
      builder.append("<table border=\"2\" style=\"font-size:75%;margin-left:auto;margin-right:auto;width:90%;\">\n");
      builder.append("<tr>\n");
      builder.append("<th>Name</th>\n");
      builder.append("<th>Protocol</th>\n");
      builder.append("<th>User</th>\n");
      builder.append("<th>Host</th>\n");
      builder.append("<th>Port</th>\n");
      builder.append("<th>Path</th>\n");
      builder.append("<th colspan=\"4\">Designation</th>\n");
      builder.append("</tr>\n");
      Connection con = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;

      label252: {
         String var9;
         try {
            con = DbUtil.getDbConnection(struct.dbUser, struct.dbPass, struct.dbType, struct.dbConnString);
            stmt = con.prepareStatement("select path, p_scheme, p_user, p_host, p_port, p_url_path, bem from hy_path order by path");
            rs = stmt.executeQuery();

            while(true) {
               if(!rs.next()) {
                  break label252;
               }

               builder.append("<tr>\n");
               builder.append("<td>" + (rs.getString(1) == null?"":rs.getString(1)) + "</td>\n");
               builder.append("<td>" + (rs.getString(2) == null?"":rs.getString(2)) + "</td>\n");
               builder.append("<td>" + (rs.getString(3) == null?"":rs.getString(3)) + "</td>\n");
               builder.append("<td>" + (rs.getString(4) == null?"":rs.getString(4)) + "</td>\n");
               int e = rs.getInt(5);
               builder.append("<td>" + (rs.wasNull()?"":Integer.valueOf(e)) + "</td>\n");
               builder.append("<td>" + (rs.getString(6) == null?"":rs.getString(6)) + "</td>\n");
               builder.append("<td>" + (rs.getString(7) == null?"":rs.getString(7)) + "</td>\n");
               builder.append("<td>\n<form action=\"HydraPathMaintenance\" method=\"post\">\n<input type=\"hidden\" name=\"action\" value=\"editView\" />\n<input type=\"hidden\" name=\"pathId\" value=\"" + (rs.getString(1) == null?"":rs.getString(1)) + "\" />\n" + "<input type=\"hidden\" name=\"dbUser\" value=\"" + struct.dbUser + "\" />\n" + "<input type=\"hidden\" name=\"dbPass\" value=\"" + struct.dbPass + "\" />\n" + "<input type=\"hidden\" name=\"dbConnString\" value=\"" + struct.dbConnString + "\" />\n" + "<input type=\"hidden\" name=\"dbType\" value=\"" + struct.dbType + "\" />\n" + "<input type=\"image\" src=\"img/button-edit.png\" style=\"width:16px;height:16px;\" alt=\"Edit\" />\n" + "</form>\n" + "</td>\n");
               builder.append("<td>\n<form action=\"HydraPathMaintenance\" method=\"post\">\n<input type=\"hidden\" name=\"action\" value=\"copyView\" />\n<input type=\"hidden\" name=\"pathId\" value=\"" + (rs.getString(1) == null?"":rs.getString(1)) + "\" />\n" + "<input type=\"hidden\" name=\"dbUser\" value=\"" + struct.dbUser + "\" />\n" + "<input type=\"hidden\" name=\"dbPass\" value=\"" + struct.dbPass + "\" />\n" + "<input type=\"hidden\" name=\"dbConnString\" value=\"" + struct.dbConnString + "\" />\n" + "<input type=\"hidden\" name=\"dbType\" value=\"" + struct.dbType + "\" />\n" + "<input type=\"image\" src=\"img/button-copy.png\" style=\"width:16px;height:16px;\" alt=\"Copy\" />\n" + "</form>\n" + "</td>\n");
               builder.append("<td>\n<form action=\"HydraPathMaintenance\" method=\"post\">\n<input type=\"hidden\" name=\"action\" value=\"deleteView\" />\n<input type=\"hidden\" name=\"pathId\" value=\"" + (rs.getString(1) == null?"":rs.getString(1)) + "\" />\n" + "<input type=\"hidden\" name=\"dbUser\" value=\"" + struct.dbUser + "\" />\n" + "<input type=\"hidden\" name=\"dbPass\" value=\"" + struct.dbPass + "\" />\n" + "<input type=\"hidden\" name=\"dbConnString\" value=\"" + struct.dbConnString + "\" />\n" + "<input type=\"hidden\" name=\"dbType\" value=\"" + struct.dbType + "\" />\n" + "<input type=\"image\" src=\"img/button-delete.png\" style=\"width:16px;height:16px;\" alt=\"Delete\" />\n" + "</form>\n" + "</td>\n");
               builder.append("</tr>\n");
            }
         } catch (Exception var27) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var27));
            var9 = this.getPageEntryResponse(struct, "Could not load existing entries: " + var27.getMessage(), (String)null, false);
         } finally {
            if(rs != null) {
               try {
                  rs.close();
               } catch (SQLException var26) {
                  ;
               }
            }

            if(stmt != null) {
               try {
                  stmt.close();
               } catch (SQLException var25) {
                  ;
               }
            }

            if(con != null) {
               try {
                  con.close();
               } catch (SQLException var24) {
                  ;
               }
            }

         }

         return var9;
      }

      builder.append("</table>\n");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getPageEntryResponse(ParamStruct struct, String errorMessage, String successMessage, boolean readOnly) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Path maintenance</h1>\n");
      builder.append("<br />\n");
      if(errorMessage != null) {
         builder.append("<span style=\"color:red;\">An error has occured: " + errorMessage + "</span><br /><br />\n");
      }

      if(successMessage != null) {
         builder.append("<span style=\"color:green;\">" + successMessage + "</span><br /><br />\n");
      }

      String readOnlyParam = readOnly?"disabled=\"disabled\"":"";
      builder.append("<form action=\"HydraPathMaintenance\" method=\"post\">\n");
      builder.append("<input type=\"hidden\" name=\"action\" value=\"list\" />\n");
      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      builder.append("DB user:\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      builder.append("<input type=\"text\" size=\"20\" name=\"dbUser\" value=\"" + (struct != null && struct.dbUser != null?struct.dbUser:"") + "\" " + readOnlyParam + "/>\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      builder.append("DB password:\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      builder.append("<input type=\"password\" size=\"20\" name=\"dbPass\" value=\"" + (struct != null && struct.dbPass != null?struct.dbPass:"") + "\" " + readOnlyParam + "/>\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      builder.append("DB URL:\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      builder.append("<input type=\"text\" size=\"60\" name=\"dbConnString\" value=\"" + (struct != null && struct.dbConnString != null?struct.dbConnString:"") + "\" " + readOnlyParam + "/>\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      builder.append("&nbsp;\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      builder.append("ORACLE (SID)<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;jdbc:oracle:thin:@&lt;HOSTNAME&gt;:&lt;PORT&gt;:&lt;ORACLE_SID&gt;<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. jdbc:oracle:thin:@192.168.20.112:1521:HYD1)<br /><br />\n");
      builder.append("ORACLE (SERVICE_NAME)<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;jdbc:oracle:thin:@&lt;HOSTNAME&gt;:&lt;PORT&gt;/&lt;SERVICE_NAME&gt;<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. jdbc:oracle:thin:@192.168.20.112:1521/hydra1)<br /><br />\n");
      builder.append("SQL Server<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;jdbc:jtds:sqlserver://&lt;HOSTNAME&gt;/&lt;DATABASE&gt;;instance=&lt;INSTANCE&gt;<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(e.g. jdbc:jtds:sqlserver://scc5/hydra1;instance=hydms1)<br /><br />\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:20%;float:left;text-align:right;padding-bottom:10px;padding-right:10px;\">\n");
      builder.append("DB type:\n");
      builder.append("</div>\n");
      builder.append("<div style=\"width:75%;float:left;text-align:left;padding-bottom:10px;padding-left:10px;\">\n");
      builder.append("<select name=\"dbType\" size=\"1\" " + readOnlyParam + ">\n");
      if(struct != null && struct.dbType != null && "ORACLE".equals(struct.dbType)) {
         builder.append("<option selected=\"selected\">ORACLE</option>\n");
         builder.append("<option>SQLSERVER</option>\n");
      } else if(struct != null && struct.dbType != null && "SQLSERVER".equals(struct.dbType)) {
         builder.append("<option>ORACLE</option>\n");
         builder.append("<option selected=\"selected\">SQLSERVER</option>\n");
      } else {
         builder.append("<option>ORACLE</option>\n");
         builder.append("<option>SQLSERVER</option>\n");
      }

      builder.append("</select>\n");
      builder.append("</div>\n");
      builder.append("<br />\n");
      if(!readOnly) {
         builder.append("<input type=\"submit\" name=\"Submit\" value=\"Get existing entries\"/><br />\n");
      }

      builder.append("</form>\n");
      builder.append("</div>\n");
      return builder.toString();
   }
}