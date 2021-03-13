package de.mpdv.maintenanceManager.gui;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

public class CommonResponseFrame {

   public static void printToResponse(String responseData, HttpServletResponse response) throws IOException {
      response.setContentType("text/html");
      response.setCharacterEncoding("UTF-8");
      PrintWriter out = response.getWriter();
      printHeader(out);
      printMenu(out);
      out.println("<div style=\"z-index:-5;position:absolute;left:230px;top:101px;width:795px;overflow:hidden;min-height:667px;height:auto;padding-bottom:40px;\">\n");
      out.println(responseData);
      out.println("</div>\n");
      printFooter(out);
   }

   private static void printHeader(PrintWriter out) {
      out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
      out.println("<html>\n");
      out.println("<head>\n");
      out.println("   <title>MPDV Maintenance Manager</title>\n");
      out.println("   <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
      out.println("</head>\n");
      out.println("<body style=\"font-family:arial,sans-serif;\">\n");
      out.println("\n");
      out.println("<div style=\"z-index:1;width:1024px;height:100px;position:fixed;top:0px;left:0px;border-bottom:1px solid black;border-right:1px solid black;overflow:hidden;\">\n");
      out.println("   <img src=\"img/header-back.png\" style=\"width:100%;height:100px;\" />\n");
      out.println("</div>\n");
      out.println("\n");
      out.println("<div style=\"z-index:2;width:1024px;height:100px;position:fixed;top:0px;left:0px;overflow:hidden;\">\n");
      out.println("   <div style=\"z-index:3;width:80%;height:100px;line-height:100px;text-align:left;float:left;overflow:hidden;\">\n");
      out.println("       <font style=\"font-size:30pt;font-weight:bold;margin-left:260px;\">Maintenance Manager</font>\n");
      out.println("   </div>\n");
      out.println("   <div style=\"z-index:3;width:20%;height:100px;float:right;text-align:right;overflow:hidden;\">\n");
      out.println("       <img src=\"img/mpdv-logo.png\" style=\"height:40px;position:relative;top:30px;\" />\n");
      out.println("   </div>\n");
      out.println("</div>\n");
      out.println("\n");
   }

   private static void printMenu(PrintWriter out) {
      out.println("<div style=\"z-index:1;width:230px;height:668px;position:fixed;top:100px;left:0px;overflow:hidden;\">\n");
      out.println("   <div style=\"z-index:2;width:229px;height:667px;position:absolute;left:0px;top=100px;overflow:hidden;border-bottom:1px solid black;border-right:1px solid black;\">\n");
      out.println("       <img src=\"img/menu-back.png\" style=\"width:229px;height:100%;\" />\n");
      out.println("   </div>\n");
      out.println("   <div style=\"z-index:3;width:229px;height:667px;position:absolute;left:0px;top=100px;overflow:hidden;\">\n");
      out.println("       <ul style=\"padding-left:25px;margin-top:0px;padding-top:15px;\">\n");
      out.println("           <li style=\"padding-bottom:15px;\"><a href=\"UniPackDeployment\" style=\"color:black;text-decoration:underline;font-weight:bold;\">Package deployment</a></li>\n");
      out.println("           <li style=\"color:black;font-weight:bold;\">Server\n");
      out.println("               <ul>\n");
      out.println("                   <li style=\"padding-bottom:15px;\"><a href=\"PackageAdmin\" style=\"color:black;text-decoration:underline;font-weight:bold;\">Package administration</a></li>\n");
      out.println("               </ul>\n");
      out.println("               <ul>\n");
      out.println("                   <li style=\"padding-bottom:15px;\"><a href=\"Activation\" style=\"color:black;text-decoration:underline;font-weight:bold;\">Software activation</a></li>\n");
      out.println("               </ul>\n");
      out.println("           </li>\n");
      out.println("           <li style=\"color:black;font-weight:bold;\">Client\n");
      out.println("               <ul>\n");
      out.println("                   <li style=\"padding-bottom:15px;\"><a href=\"ClientPackageAdmin\" style=\"color:black;text-decoration:underline;font-weight:bold;\">Package administration</a></li>\n");
      out.println("               </ul>\n");
      out.println("           </li>\n");
      out.println("           <li style=\"color:black;font-weight:bold;\">System maintenance\n");
      out.println("               <ul>\n");
      out.println("                   <li style=\"padding-bottom:15px;\"><a href=\"VersionInformation\" style=\"color:black;text-decoration:underline;font-weight:bold;\">Version information</a></li>\n");
      out.println("               </ul>\n");
      out.println("               <ul>\n");
      out.println("                   <li style=\"padding-bottom:15px;\"><a href=\"SoftwareStatus\" style=\"color:black;text-decoration:underline;font-weight:bold;\">Software status</a></li>\n");
      out.println("               </ul>\n");
      out.println("               <ul>\n");
      out.println("                   <li style=\"padding-bottom:15px;\"><a href=\"HydraPathMaintenance\" style=\"color:black;text-decoration:underline;font-weight:bold;\">Path maintenance</a></li>\n");
      out.println("               </ul>\n");
      out.println("               <ul>\n");
      out.println("                   <li style=\"padding-bottom:15px;\"><a href=\"PackageCleanup\" style=\"color:black;text-decoration:underline;font-weight:bold;\">Clean up</a></li>\n");
      out.println("               </ul>\n");
      out.println("           </li>\n");
      out.println("           <li style=\"padding-bottom:15px;\"><a href=\"Configuration\" style=\"color:black;text-decoration:underline;font-weight:bold;\">Configuration</a></li>\n");
      out.println("           <li style=\"padding-bottom:15px;\"><a href=\"Logout\" style=\"color:black;text-decoration:underline;font-weight:bold;\">Logout</a></li>\n");
      out.println("       </ul>\n");
      out.println("   </div>\n");
      out.println("</div>\n");
      out.println("\n");
   }

   private static void printFooter(PrintWriter out) {
      out.println("</body>\n");
      out.println("</html>\n");
   }
}