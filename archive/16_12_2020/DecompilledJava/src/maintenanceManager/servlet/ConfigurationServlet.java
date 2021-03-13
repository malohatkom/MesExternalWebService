package de.mpdv.maintenanceManager.servlet;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.gui.CommonResponseFrame;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ConfigurationServlet extends HttpServlet {

   private static final long serialVersionUID = 5311181107687383940L;
   private static final String ACTION_SAVE = "SAVE";


   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      if(SessionManager.checkLogin(request, response)) {
         File mm2ConfigFile = Configuration.getMM2ConfigFile();
         if(mm2ConfigFile.exists()) {
            CommonResponseFrame.printToResponse("Maintananace Manager 2.0 is installed! Please use the new version!", response);
         } else {
            Configuration config = null;

            try {
               config = Configuration.getConfiguration();
            } catch (Exception var8) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var8));
               CommonResponseFrame.printToResponse(this.getErrorResponse("Could not get configuration: " + var8.getMessage()), response);
               return;
            }

            String action = request.getParameter("action");
            if(action != null && action.length() != 0) {
               if(action.equals("SAVE")) {
                  try {
                     this.saveConfig(request);
                  } catch (Exception var7) {
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var7));
                     CommonResponseFrame.printToResponse(this.getErrorResponse("Could not save configuration: " + var7.getMessage()), response);
                     return;
                  }

                  config = Configuration.getConfiguration();
                  CommonResponseFrame.printToResponse(this.getPageEntryResponse(config), response);
               } else {
                  CommonResponseFrame.printToResponse(this.getErrorResponse("Unknown action : " + action), response);
               }
            } else {
               CommonResponseFrame.printToResponse(this.getPageEntryResponse(config), response);
            }
         }
      }
   }

   private void saveConfig(HttpServletRequest request) throws IOException {
      Configuration newConfig = new Configuration((String)null, (String)null, (String)null, (String)null, (String)null, (String)null, 6);
      Map paramMap = request.getParameterMap();
      Iterator it = paramMap.keySet().iterator();

      while(it.hasNext()) {
         String paramName = (String)it.next();
         String[] arr = (String[])((String[])paramMap.get(paramName));
         String paramValue = null;
         if(arr != null && arr.length > 0) {
            paramValue = arr[0];
         }

         if("jhydradir".equals(paramName)) {
            if(paramValue != null && paramValue.length() != 0) {
               newConfig.setjHydraDir(paramValue);
            }
         } else if("tempdir".equals(paramName)) {
            if(paramValue != null && paramValue.length() != 0) {
               newConfig.setTempDir(paramValue);
            }
         } else if("tomcatdir".equals(paramName)) {
            if(paramValue != null && paramValue.length() != 0) {
               newConfig.setTomcatDir(paramValue);
            }
         } else if("updatedir".equals(paramName)) {
            if(paramValue != null && paramValue.length() != 0) {
               newConfig.setBaseUpdateDir(paramValue);
            }
         } else if("runtimedir".equals(paramName)) {
            if(paramValue != null && paramValue.length() != 0) {
               newConfig.setBaseRuntimeDir(paramValue);
            }
         } else if(paramName.startsWith("tomcathostport")) {
            if(paramValue != null && paramValue.length() != 0) {
               newConfig.setTomcatHostPort(paramValue);
            }
         } else if(paramName.startsWith("tomcatversion") && paramValue != null && paramValue.length() != 0) {
            try {
               int e = Integer.parseInt(paramValue);
               newConfig.setTomcatVersion(e);
            } catch (NumberFormatException var9) {
               var9.printStackTrace();
            }
         }
      }

      Configuration.setMaintMgrDirs(newConfig);
      Configuration.saveConfiguration(newConfig);
   }

   private String getErrorResponse(String message) {
      return "<div style=\"text-align:center;width:100%;\"><h1>Configuration</h1>\n<br />\n<span style=\"color:red;\">An error has occured: " + message + "</span><br />" + "</div>\n";
   }

   private String getPageEntryResponse(Configuration config) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">");
      builder.append("<h1>Configuration</h1>\n");
      builder.append("<br />\n");
      builder.append("<form action=\"Configuration\" method=\"post\">\n");
      builder.append("<b>General configuration</b><br /><br />");
      builder.append("<table border=\"2\" style=\"font-size:75%;margin-left:auto;margin-right:auto;\">\n");
      builder.append("<tr>");
      builder.append("<td>Java Hydra dir</td>");
      builder.append("<td><input type=\"text\" size=\"50\" name=\"jhydradir\" value=\"").append(config.getjHydraDir()).append("\" readonly=\"readonly\"/></td>");
      builder.append("</tr>");
      builder.append("<tr>");
      builder.append("<td>Temp dir</td>");
      builder.append("<td><input type=\"text\" size=\"50\" name=\"tempdir\" value=\"").append(config.getTempDir() == null?"":config.getTempDir()).append("\" readonly=\"readonly\"/></td>");
      builder.append("</tr>");
      builder.append("</table><br /><br /><br />");
      builder.append("<b>General instance configuration</b><br /><br />");
      builder.append("<table border=\"2\" style=\"font-size:75%;margin-left:auto;margin-right:auto;\">\n");
      builder.append("<tr>");
      builder.append("<td>Tomcat host and port<br />(HOSTNAME:PORT)</td>");
      builder.append("<td><input type=\"text\" size=\"50\" name=\"tomcathostport\" value=\"").append(config.getTomcatHostPort() == null?"":config.getTomcatHostPort()).append("\"/></td>");
      builder.append("</tr>");
      builder.append("<tr>");
      builder.append("<td>Tomcat versiont</td>");
      builder.append("<td><input type=\"text\" size=\"50\" name=\"tomcatversion\" value=\"").append(config.getTomcatVersion()).append("\"/></td>");
      builder.append("</tr>");
      builder.append("<tr>");
      builder.append("<td>Tomcat dir</td>");
      builder.append("<td><input type=\"text\" size=\"50\" name=\"tomcatdir\" value=\"").append(config.getTomcatDir() == null?"":config.getTomcatDir()).append("\"/></td>");
      builder.append("</tr>");
      builder.append("<tr>");
      builder.append("<td>Update dir</td>");
      builder.append("<td><input type=\"text\" size=\"50\" name=\"updatedir\" value=\"").append(config.getBaseUpdateDir() == null?"":config.getBaseUpdateDir()).append("\" readonly=\"readonly\"/></td>");
      builder.append("</tr>");
      builder.append("<tr>");
      builder.append("<td>Runtime dir</td>");
      builder.append("<td><input type=\"text\" size=\"50\" name=\"runtimedir\" value=\"").append(config.getBaseRuntimeDir() == null?"":config.getBaseRuntimeDir()).append("\" readonly=\"readonly\"/></td>");
      builder.append("</tr>");
      builder.append("<tr>");
      builder.append("<td>Java version</td>");
      builder.append("<td><input type=\"text\" size=\"50\" name=\"runtimedir\" value=\"").append(System.getProperty("java.version")).append("\" readonly=\"readonly\"/></td>");
      builder.append("</tr>");
      builder.append("</table><br /><br /><br />");
      builder.append("<input type=\"hidden\" name=\"action\" value=\"SAVE\"/><br />\n");
      builder.append("<input type=\"submit\" name=\"Submit\" value=\"Save\"/><br />\n");
      builder.append("</form>\n");
      builder.append("</div>\n");
      return builder.toString();
   }
}