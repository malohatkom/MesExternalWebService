package de.mpdv.maintenanceManager.servlet.systemReset;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.servlet.javaServer.ActivationServlet;
import de.mpdv.maintenanceManager.servlet.javaServer.ActivationServlet.ParamStruct;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.Util;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SysResetActivationServlet extends HttpServlet {

   private static final long serialVersionUID = 7938645886320420817L;
   private static final String SYS_RESET_TOOL_HASH = "c5f40773a0660abf0e9782485f1e35d1";


   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      boolean alreadyLoggedIn = true;
      if(!SessionManager.isLoggedIn(request)) {
         alreadyLoggedIn = false;
         SessionManager.internalLogin(request);
      }

      try {
         ActivationServlet srvlet = new ActivationServlet();
         srvlet.init(this.getServletConfig());
         Configuration config = null;

         try {
            config = Configuration.getConfiguration();
         } catch (Exception var17) {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetSwActivation: " + Util.exceptionToString(var17));
            this.printToResponse(response, "ERROR:\n\n" + Util.exceptionToString(var17));
            return;
         }

         String deploymentType = "MOC";
         String internalPassword = request.getParameter("internal_password");
         if("c5f40773a0660abf0e9782485f1e35d1".equals(internalPassword)) {
            ParamStruct struct;
            try {
               File rtDir = srvlet.checkEntryPageParams(config);
               struct = srvlet.checkParams(config, "MOC", "ACTIVATE", rtDir);
            } catch (Exception var19) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetSwActivation: " + Util.exceptionToString(var19));
               this.printToResponse(response, "ERROR:\n\n" + Util.exceptionToString(var19));
               return;
            }

            try {
               srvlet.activate(struct, config);
               this.printToResponse(response, "Software activation successfully finished!");
               return;
            } catch (Exception var18) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetSwActivation: " + Util.exceptionToString(var18));
               this.printToResponse(response, "ERROR:\n\n" + Util.exceptionToString(var18));
               return;
            }
         }

         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - SysResetSwActivation: Internal password not specified or wrong!");
         this.printToResponse(response, "ERROR:\n\nInternal password not specified or wrong!");
      } finally {
         if(!alreadyLoggedIn) {
            SessionManager.logout(request);
         }

      }

   }

   private void printToResponse(HttpServletResponse response, String content) {
      try {
         response.setContentType("text/html");
         response.setCharacterEncoding("UTF-8");
         PrintWriter e = response.getWriter();
         e.println(content);
      } catch (IOException var4) {
         ;
      }

   }
}