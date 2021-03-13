package de.mpdv.maintenanceManager.servlet;

import de.mpdv.maintenanceManager.logic.SessionManager;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogoutServlet extends HttpServlet {

   private static final long serialVersionUID = -8182072124804577201L;


   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      SessionManager.logout(request);
      response.sendRedirect(request.getRequestURI().replace("Logout", ""));
   }
}