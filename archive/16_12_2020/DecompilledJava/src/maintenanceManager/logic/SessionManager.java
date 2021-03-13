package de.mpdv.maintenanceManager.logic;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.gui.CommonResponseFrame;
import de.mpdv.maintenanceManager.util.CryptUtil;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.ServiceCallData;
import de.mpdv.mesclient.businessservice.internalData.ServiceInputSpecialParam;
import de.mpdv.mesclient.businessservice.util.ServiceCaller;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SessionManager {

   private static final String MAINT_MGR_PASSWD_HASH = "f23e393ca852a8aef12e73a10de221e4";


   public static boolean isLoggedIn(HttpServletRequest servletRequest) {
      HttpSession session = servletRequest.getSession(true);
      Boolean isLoggedIn = (Boolean)session.getAttribute("isLoggedIn");
      return isLoggedIn == null?false:isLoggedIn.booleanValue();
   }

   private static void login(HttpServletRequest servletRequest, String password) {
      HttpSession session = servletRequest.getSession(true);
      session.setMaxInactiveInterval(7200);

      String md5InputPassword;
      try {
         md5InputPassword = CryptUtil.getMd5HashString(password);
      } catch (NoSuchAlgorithmException var5) {
         throw new RuntimeException("Could not check password", var5);
      }

      if(md5InputPassword.equals("f23e393ca852a8aef12e73a10de221e4")) {
         session.setAttribute("isLoggedIn", Boolean.TRUE);
      } else {
         throw new IllegalArgumentException("The specified password is wrong");
      }
   }

   public static void internalLogin(HttpServletRequest servletRequest) {
      HttpSession session = servletRequest.getSession(true);
      session.setMaxInactiveInterval(7200);
      session.setAttribute("isLoggedIn", Boolean.TRUE);
   }

   public static void logout(HttpServletRequest servletRequest) {
      HttpSession session = servletRequest.getSession(true);
      session.removeAttribute("isLoggedIn");
   }

   public static void printLoginForm(HttpServletRequest servletRequest, HttpServletResponse response, String message) throws IOException {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Login</h1>\n");
      builder.append("<br />\n");
      builder.append("<br />\n");
      if(message != null && !"".equals(message)) {
         builder.append("<span style=\"color:red;\">An error has occured: " + message + "</span><br />");
         builder.append("<br />\n");
      }

      builder.append("<form name=\"login\" method=\"post\" action=\"" + servletRequest.getRequestURL().toString() + "\">");
      builder.append("<input type=\"hidden\" name=\"doLogin\" value=\"true\" />");
      builder.append("Password: <input type=\"password\" name=\"password\" />");
      builder.append("<input type=\"submit\" value=\"Login\" />");
      builder.append("</form>");
      builder.append("</div>\n");
      CommonResponseFrame.printToResponse(builder.toString(), response);
   }

   public static boolean checkLogin(HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {
      String doLogin = servletRequest.getParameter("doLogin");
      if(doLogin != null && doLogin.equals("true")) {
         String password = servletRequest.getParameter("password");
         if(password != null && !password.equals("")) {
            try {
               login(servletRequest, password);
               return true;
            } catch (Exception var5) {
               printLoginForm(servletRequest, response, var5.getMessage());
               return false;
            }
         } else {
            printLoginForm(servletRequest, response, "Please enter a password");
            return false;
         }
      } else if(isLoggedIn(servletRequest)) {
         return true;
      } else {
         printLoginForm(servletRequest, response, (String)null);
         return false;
      }
   }

   public static IDataTable callWebService(HttpServletRequest servletRequest, String functionId, List requestedColumns, List filterCriterias, List specialParams, Map directParams, Integer instance) {
      Configuration config = null;

      try {
         config = Configuration.getConfiguration();
      } catch (Exception var26) {
         throw new RuntimeException("Could not call webservice " + functionId + " because: Could not get configuration: " + var26.getMessage());
      }

      String tomcatHostPort = config.getTomcatHostPort();
      if(tomcatHostPort != null && !tomcatHostPort.equals("")) {
         String licenseToken = UUID.randomUUID().toString();
         String sessionId = UUID.randomUUID().toString();

         try {
            LinkedList data = new LinkedList();
            data.add(new ServiceInputSpecialParam("license.token.id", "EQUAL", licenseToken));
            data.add(new ServiceInputSpecialParam("session.id", "EQUAL", sessionId));
            ServiceCallData loginData = new ServiceCallData("SYSSessionLifeCycle.login", (List)null, (List)null, data, (Map)null, licenseToken, sessionId, instance, tomcatHostPort);
            ServiceCaller.callService(loginData);
         } catch (Exception var25) {
            throw new RuntimeException("Could not call webservice " + functionId + " because: Service login has failed because: " + var25.getMessage());
         }

         IDataTable loginData1;
         try {
            ServiceCallData data2 = new ServiceCallData(functionId, requestedColumns, filterCriterias, specialParams, directParams, licenseToken, sessionId, instance, tomcatHostPort);
            loginData1 = ServiceCaller.callService(data2);
         } finally {
            LinkedList logoutSpecialParams = new LinkedList();
            logoutSpecialParams.add(new ServiceInputSpecialParam("license.token.id", "EQUAL", licenseToken));
            logoutSpecialParams.add(new ServiceInputSpecialParam("session.id", "EQUAL", sessionId));
            ServiceCallData data1 = new ServiceCallData("SYSSessionLifeCycle.logout", (List)null, (List)null, logoutSpecialParams, (Map)null, licenseToken, sessionId, instance, tomcatHostPort);

            try {
               ServiceCaller.callService(data1);
            } catch (Exception var23) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - Error calling logout webservice for service logged in client " + instance + " because:\n" + var23.getMessage());
            }

         }

         return loginData1;
      } else {
         throw new RuntimeException("Could not call webservice " + functionId + " because: Tomcat host and port not set");
      }
   }
}