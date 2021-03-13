package de.mpdv.maintenanceManager.servlet.eterno;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.gui.CommonResponseFrame;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.JarUtil;
import de.mpdv.maintenanceManager.util.Util;
import de.mpdv.mesclient.businessservice.internalData.DataTableSortSpec;
import de.mpdv.mesclient.businessservice.internalData.DataTableSortSpec.OrderDirection;
import de.mpdv.mesclient.businessservice.internalData.DataTableUtil;
import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.ServiceCallData;
import de.mpdv.mesclient.businessservice.internalData.ServiceInputFilterParam;
import de.mpdv.mesclient.businessservice.util.ServiceCaller;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CheckDBDataServlet extends HttpServlet {

   private static final long serialVersionUID = 3614040670713151430L;
   private static final String MODE_LIST_STATUS = "LIST_STATUS";
   private static final String MODE_EXPORT_STATUS = "EXPORT_STATUS";
   
   
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
   {
      if(SessionManager.checkLogin(request, response)) 
      {
         Configuration config = null;
         try 
         {
            config = Configuration.getConfiguration();
         } 
         catch (Exception var16) 
         {
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var16));
            CommonResponseFrame.printToResponse(this.getErrorResponse("Could not load application for software status because: Could not get configuration: " + var16.getMessage()), response);
            return;
         }

         String mode = request.getParameter("mode");
         if(mode != null && !mode.equals("")) 
         {
            try 
            {
               this.checkParams(config, mode);
            } 
            catch (Exception var15) 
            {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var15));
               CommonResponseFrame.printToResponse(this.getErrorResponse("Could not execute the software status application because: " + var15.getMessage()), response);
               return;
            }

            if(mode.equals("LIST_STATUS")) 
            {
               try 
               {
                  LinkedList e = new LinkedList();
                  e.add("inspectionpoint.ud_code");
                  e.add("operation.designation");
                  e.add("operation.latest_end_ts");
                  e.add("operation.operation");
                  e.add("order.id");
                  e.add("person.function");
                  e.add("person.name");
                  e.add("qmcharacteristic.designation");
                  e.add("qmcharacteristic.lower_tolerance_limit");
                  e.add("qmcharacteristic.target_value");
                  e.add("qmcharacteristic.upper_tolerance_limit");
                  e.add("qmsinglevalue.measured_value.value");
                  String filterOrder = request.getParameter("order.id");
                  LinkedList filterList = new LinkedList();
                  if(!Util.stringNullOrEmpty(filterOrder)) 
                  {
                     filterList.add(new ServiceInputFilterParam("order.id", "LIKE", String.class, filterOrder));
                  }

                  IDataTable OrderDataTable = SessionManager.callWebService(request, "BOOrder.listNotes", e, filterList, (List)null, (Map)null, 1);
                  String sortColumn = request.getParameter("sortcolumn");
                  if(!Util.stringNullOrEmpty(sortColumn)) 
                  {
                     DataTableSortSpec sortSpec = new DataTableSortSpec(OrderDataTable);
                     sortSpec.column(sortColumn, OrderDirection.ASC);
                     sortSpec.freeze();
                     OrderDataTable = DataTableUtil.sort(sortSpec);
                  }

                  CommonResponseFrame.printToResponse(this.getSwStatusResponse(OrderDataTable, filterOrder, config), response);
               } 
               catch (Exception var13) 
               {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var13));
                  CommonResponseFrame.printToResponse(this.getErrorResponse("Could not execute the software status application because: " + var13.getMessage()), response);
               }
            } 
         } 
         else 
         {
            CommonResponseFrame.printToResponse(this.getPageEntryResponse(config), response);
         }
      }
   }

   private void checkParams(Configuration config, String mode) {
      if(mode != null && !mode.equals("")) 
      {
         if(!mode.equals("LIST_STATUS")) 
         {
            throw new IllegalArgumentException("Unknown mode specified: " + mode + ". Allowed modes are: " + "LIST_STATUS" + ", " + "EXPORT_STATUS");
         } 
         else 
         {
            String tomcatHostPort = config.getTomcatHostPort();
            if(tomcatHostPort != null && !tomcatHostPort.equals("")) 
            {

            } 
            else 
            {
               throw new IllegalStateException("The tomcat host/port is not configured");
            }
         }
      } 
      else 
      {
         throw new IllegalArgumentException("The mode is not specified");
      }
   }

   private String getErrorResponse(String message) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Software status</h1>\n");
      builder.append("<br />\n");
      builder.append("<span style=\"color:red;\">An error has occured: " + message + "</span><br />");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getSwStatusResponse(IDataTable swStatusTable, String filterOrder, Configuration config) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;height:667px;overflow-y:scroll;overflow-x:scroll;\">\n");
      builder.append("  <h1>Software status</h1>\n");
      builder.append("  <form action=\"SoftwareStatus\" method=\"post\">\n");
      builder.append("      <div style=\"width:100%;\">");
      builder.append("      <div style=\"width:32%;float:left;text-align:left;\">&nbsp;</div>");
      builder.append("      <div style=\"width:17%;float:left;text-align:left;\">Filter by order:</div>");
      builder.append("      <div style=\"width:4%;float:left;\">&nbsp;</div>");
      builder.append("      <div style=\"width:47%;float:left;text-align:left;\"><input type=\"text\" name=\"filterorder\" value=\"" + (filterOrder == null?"":filterOrder) + "\"/></div>");
      builder.append("</div>");
      builder.append("<br /><br /><br /><br />");
      builder.append("  <input type=\"hidden\" name=\"mode\" value=\"LIST_STATUS\"/>\n");
      builder.append("  <input type=\"submit\" name=\"Submit\" value=\"Show order data\"/>\n");
      builder.append("</form>\n");
      builder.append("<script type=\"text/javascript\">\n");
      builder.append("function showstatusinnewwindow()");
      builder.append("{");
      builder.append("windowopts = \"toolbar=no,scrollbars=yes,location=0,statusbar=no,menubar=no,resizable=yes,outerWidth=1024,outerHeight=768,width=1024,height=768\";");
      builder.append("imgwindow = window.open(\"\", \"\", windowopts);");
      builder.append("imgwindow.focus();");
      builder.append("imgwindow.document.open();");
      builder.append("var tablecontent=document.getElementById(\"statustable\").innerHTML;");
      builder.append("with(imgwindow)");
      builder.append("{");
      builder.append("    document.write(\"<!DOCTYPE HTML PUBLIC \\\"-//W3C//DTD HTML 4.01 Transitional//EN\\\" \\\"http://www.w3.org/TR/html4/loose.dtd\\\">\");");
      builder.append("    document.write(\"<html>\");");
      builder.append("    document.write(\" <head>\");");
      builder.append("    document.write(\"  <title>MPDV Maintenance Manager - Software status</title>\");");
      builder.append("    document.write(\" </head>\");");
      builder.append("    document.write(\" <body>\");");
      builder.append("    document.write(\" <div style=\\\"text-align:center;width:100%;\\\">\");");
      builder.append("    document.write(\" <h1>Software status</h1>\");");
      builder.append("    document.write(\" <table border=\\\"2\\\" style=\\\"margin-left:auto;margin-right:auto;text-align:left;\\\">\");");
      builder.append("    document.write(\" <tr>\");");
      builder.append("    document.write(\" <td style=\\\"background-color:grey;\\\"><b>Filter by name</b></td>\");");
      builder.append(String.format("    document.write(\" <td>%1$s</td>\");", filterOrder == null?"&nbsp;":filterOrder));
      builder.append("    document.write(\" </tr>\");");
      builder.append("    document.write(\" </table>\");");
      builder.append("    document.write(\"<br /><br />\");");
      builder.append("    document.write(\" <table border=\\\"2\\\" style=\\\"margin-left:auto;margin-right:auto;font-size:70%;text-align:left;\\\">\");");
      builder.append("    document.write(tablecontent);");
      builder.append("    document.getElementById(\"thname\").innerHTML=\"Name\";");
      builder.append("    document.getElementById(\"thtype\").innerHTML=\"Type\";");
      builder.append("    document.getElementById(\"thprogver\").innerHTML=\"Program Version\";");
      builder.append("    document.getElementById(\"thprogdate\").innerHTML=\"Program Date\";");
      builder.append("    document.getElementById(\"thcompdate\").innerHTML=\"Compilation Date\";");
      builder.append("    document.getElementById(\"thfname\").innerHTML=\"File name\";");
      builder.append("    document.getElementById(\"thfct\").innerHTML=\"Function\";");
      builder.append("    document.getElementById(\"thlogtime\").innerHTML=\"Log Time\";");
      builder.append("    document.getElementById(\"thnostarts\").innerHTML=\"Number of starts\";");
      builder.append("    document.getElementById(\"therrtime\").innerHTML=\"Error Time\";");
      builder.append("    document.getElementById(\"therrcode\").innerHTML=\"Error Code\";");
      builder.append("    document.getElementById(\"therrmsg\").innerHTML=\"Error Message\";");
      builder.append("    document.write(\" </table>\");");
      builder.append("    document.write(\" </div>\");");
      builder.append("    document.write(\" </body>\");");
      builder.append("    document.write(\"</html>\");");
      builder.append("}");
      builder.append("}");
      builder.append("</script>\n");
      builder.append("<input type=\"button\" name=\"newWindow\" onClick=\"showstatusinnewwindow()\" value=\"Show table in new Window\" /><br /><br />\n");
      builder.append("<table border=\"2\" id=\"statustable\" name=\"statustable\" style=\"margin-left:auto;margin-right:auto;font-size:70%;text-align:left;\">\n");
      builder.append("<tr style=\"background-color:grey;\">\n");
      builder.append("<th colspan=\"7\">Program</th>\n");
      builder.append("<th colspan=\"2\">Log</th>\n");
      builder.append("<th colspan=\"3\">Error</th>\n");
      builder.append("</tr>\n");
      builder.append("<tr style=\"background-color:grey;\">\n");
      builder.append("<th id=\"thname\">" + this.getSortHeaderHtml(filterOrder, "ud_code", "inspectionpoint.ud_code") + "</th>\n");
      builder.append("<th id=\"thtype\">" + this.getSortHeaderHtml(filterOrder, "designation", "operation.designation") + "</th>\n");
      builder.append("<th id=\"thprogver\">" + this.getSortHeaderHtml(filterOrder, "latest_end_ts", "operation.latest_end_ts") + "</th>\n");
      builder.append("<th id=\"thprogdate\">" + this.getSortHeaderHtml(filterOrder, "operation", "operation.operation") + "</th>\n");
      builder.append("<th id=\"thcompdate\">" + this.getSortHeaderHtml(filterOrder, "id", "order.id") + "</th>\n");
      builder.append("<th id=\"thfname\">" + this.getSortHeaderHtml(filterOrder, "function", "person.function") + "</th>\n");
      builder.append("<th id=\"thfct\">" + this.getSortHeaderHtml(filterOrder, "name", "person.name") + "</th>\n");
      builder.append("<th id=\"thlogtime\">" + this.getSortHeaderHtml(filterOrder, "designation", "qmcharacteristic.designation") + "</th>\n");
      builder.append("<th id=\"thnostarts\">" + this.getSortHeaderHtml(filterOrder, "Number of starts", "softwarestatus.logged_count") + "</th>\n");
      builder.append("<th id=\"therrtime\">" + this.getSortHeaderHtml(filterOrder, "lower_tolerance_limit", "qmcharacteristic.lower_tolerance_limit") + "</th>\n");
      builder.append("<th id=\"therrcode\">" + this.getSortHeaderHtml(filterOrder, "target_value", "qmcharacteristic.target_value") + "</th>\n");
      builder.append("<th id=\"therrmsg\">" + this.getSortHeaderHtml(filterOrder, "upper_tolerance_limit", "qmcharacteristic.upper_tolerance_limit") + "</th>\n");
      builder.append("</tr>\n");
      int count = swStatusTable.getRowCount();

        for(int i = 0; i < count; ++i) 
        {
            builder.append("<tr>\n");
            builder.append("    <td>" + (swStatusTable.getCellValue(i, "inspectionpoint.ud_code",                   String.class) == null   ?   "":(String)swStatusTable.getCellValue(i, "inspectionpoint.ud_code", String.class)) + "</td>\n");
            builder.append("    <td>" + (swStatusTable.getCellValue(i, "operation.designation",                     String.class) == null   ?   "":(String)swStatusTable.getCellValue(i, "inspectionpoint.ud_code", String.class)) + "</td>\n");
            builder.append("    <td>" + (swStatusTable.getCellValue(i, "operation.latest_end_ts",                   Calendar.class) == null ?   "":DateTimeUtil.calendarToPrintString((Calendar)swStatusTable.getCellValue(i, "operation.latest_end_ts", Calendar.class))) + "</td>\n");
            builder.append("    <td>" + (swStatusTable.getCellValue(i, "operation.operation",                       String.class) == null   ?   "":(String)swStatusTable.getCellValue(i, "operation.operation", String.class)) + "</td>\n");
            builder.append("    <td>" + (swStatusTable.getCellValue(i, "order.id",                                  String.class) == null   ?   "":(String)swStatusTable.getCellValue(i, "order.id", String.class)) + "</td>\n");
            builder.append("    <td>" + (swStatusTable.getCellValue(i, "person.function",                           String.class) == null   ?   "":(String)swStatusTable.getCellValue(i, "person.function", String.class)) + "</td>\n");
            builder.append("    <td>" + (swStatusTable.getCellValue(i, "person.name",                               String.class) == null   ?   "":(String)swStatusTable.getCellValue(i, "person.name", String.class)) + "</td>\n");
            builder.append("    <td>" + (swStatusTable.getCellValue(i, "qmcharacteristic.designation",              String.class) == null   ?   "":(String)swStatusTable.getCellValue(i, "qmcharacteristic.designation", String.class)) + "</td>\n");
            builder.append("    <td>" + (swStatusTable.getCellValue(i, "qmcharacteristic.lower_tolerance_limit",    Float.class) == null    ?   "":(Float)swStatusTable.getCellValue(i, "qmcharacteristic.lower_tolerance_limit", Float.class)) + "</td>\n");
            builder.append("    <td>" + (swStatusTable.getCellValue(i, "qmcharacteristic.target_value",             Float.class) == null    ?   "":(Float)swStatusTable.getCellValue(i, "qmcharacteristic.target_value", Float.class)) + "</td>\n");
            builder.append("    <td>" + (swStatusTable.getCellValue(i, "qmcharacteristic.upper_tolerance_limit",    Float.class) == null    ?   "":(Float)swStatusTable.getCellValue(i, "qmcharacteristic.upper_tolerance_limit", Float.class)) + "</td>\n");
            builder.append("    <td>" + (swStatusTable.getCellValue(i, "qmsinglevalue.measured_value.value",        Float.class) == null    ?   "":(Float)swStatusTable.getCellValue(i, "qmsinglevalue.measured_value.value", Float.class)) + "</td>\n");
            builder.append("</tr>\n");
      }
      builder.append("</table>\n");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getSortHeaderHtml(String filterOrder, String columnName, String sortKey) {
      StringBuilder builder = new StringBuilder();
      builder.append("<form action=\"SoftwareStatus\" method=\"post\">\n");
      builder.append("<input type=\"hidden\" name=\"mode\" value=\"LIST_STATUS\"/>\n");
      if(filterOrder != null) {
         builder.append("<input type=\"hidden\" name=\"filterorder\" value=\"" + filterOrder + "\"/>\n");
      }
      builder.append("<input type=\"hidden\" name=\"sortcolumn\" value=\"" + sortKey + "\"/>\n");
      builder.append("<input type=\"submit\" name=\"Submit\" value=\"" + columnName + "\"/>\n");
      builder.append("</form>\n");
      return builder.toString();
   }

   private String getPageEntryResponse(Configuration config) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("  <h1>Order information</h1>\n");
      builder.append("  <br />\n");
      builder.append("  <form action=\"eterno\" method=\"post\">\n");
      builder.append("      <div style=\"width:100%;\">");
      builder.append("          <div style=\"width:32%;float:left;text-align:left;\">&nbsp;</div>");
      builder.append("          <div style=\"width:17%;float:left;text-align:left;\">Filter by order:</div>");
      builder.append("          <div style=\"width:4%;float:left;\">&nbsp;</div>");
      builder.append("          <div style=\"width:47%;float:left;text-align:left;\"><input type=\"text\" name=\"filterorder\" /></div>");
      builder.append("      </div>");
      builder.append("      <br /><br /><br /><br />");
      builder.append("      <input type=\"hidden\" name=\"mode\" value=\"LIST_STATUS\"/>\n");
      builder.append("      <input type=\"submit\" name=\"Submit\" value=\"Show software status\"/>\n");
      builder.append("  </form>\n");
      builder.append("</div>\n");
      return builder.toString();
   }
}