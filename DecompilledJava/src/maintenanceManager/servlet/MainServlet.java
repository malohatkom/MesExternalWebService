package de.mpdv.maintenanceManager.servlet;

import de.mpdv.maintenanceManager.gui.CommonResponseFrame;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.util.JarUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CheckDBDataServlet extends HttpServlet {

   private static final long serialVersionUID = 9043387935087487104L;


   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      if(SessionManager.checkLogin(request, response)) {
         StringBuilder builder = new StringBuilder();
         builder.append("<div style=\"text-align:center;width:100%;\">\n");
         builder.append("<br />\n");
         builder.append("<br />\n");
         builder.append("<h1>\n");
         builder.append("MPDV<br />\n");
         builder.append("<br />\n");
         builder.append("Maintenance Manager<br />\n");
         builder.append("<br />\n");
         builder.append("</h1>\n");
         builder.append("<br />\n");
         String version = JarUtil.getImplementationVersionFromJarContainingClass(CheckDBDataServlet.class);
         if(version != null) {
            builder.append("<h2>Version " + version + "</h2>\n");
         } else {
            builder.append("<h2>Version unknown</h2>\n");
         }

         builder.append("</div>\n");
         CommonResponseFrame.printToResponse(builder.toString(), response);
      }
   }
}