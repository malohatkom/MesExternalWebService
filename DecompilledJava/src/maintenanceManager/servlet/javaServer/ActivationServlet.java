package de.mpdv.maintenanceManager.servlet.javaServer;

import de.mpdv.maintenanceManager.data.Configuration;
import de.mpdv.maintenanceManager.data.XmlWriter;
import de.mpdv.maintenanceManager.data.javaServer.DeploymentMeta;
import de.mpdv.maintenanceManager.data.javaServer.ProjectMeta;
import de.mpdv.maintenanceManager.gui.CommonResponseFrame;
import de.mpdv.maintenanceManager.logic.SessionManager;
import de.mpdv.maintenanceManager.servlet.javaServer.ActivationServlet.HttpCallResult;
import de.mpdv.maintenanceManager.servlet.javaServer.ActivationServlet.ParamStruct;
import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.FileSystemUtil;
import de.mpdv.maintenanceManager.util.JarUtil;
import de.mpdv.maintenanceManager.util.MemUtil;
import de.mpdv.maintenanceManager.util.Util;
import de.mpdv.maintenanceManager.util.XmlUtil;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.xml.sax.SAXException;

public class ActivationServlet extends HttpServlet {

   private static final long serialVersionUID = -5161154699342453891L;
   private static final String ACTION_ACTIVATE = "ACTIVATE";
   private static final String TC_USER = "ad";
   private static final String TC_PASS = "Mosbach74821";
   private static final Pattern COMPILE = Pattern.compile(".xml", 16);

   
   class HttpCallResult {

   String response;
   int retCode;
 

}
   
   
public class ParamStruct {

   File rtAppDir;
   DeploymentMeta deploymentMeta;
   File jHydraDir;
   File tempDir;
   String tomcatHostPort;


}   
   

   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      if(SessionManager.checkLogin(request, response)) {
         File mm2ConfigFile = Configuration.getMM2ConfigFile();
         if(mm2ConfigFile.exists()) {
            CommonResponseFrame.printToResponse("Maintananace Manager 2.0 is installed! Please use the new version!", response);
         } else {
            Configuration config = null;

            try {
               config = Configuration.getConfiguration();
            } catch (Exception var13) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var13));
               CommonResponseFrame.printToResponse(this.getErrorResponse("Could not load application for software activation because: Could not get configuration: " + var13.getMessage()), response);
               return;
            }

            File rtDir;
            try {
               rtDir = this.checkEntryPageParams(config);
            } catch (Exception var12) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var12));
               CommonResponseFrame.printToResponse(this.getErrorResponse("Could not load application for software activation because: " + var12.getMessage()), response);
               return;
            }

            String action = request.getParameter("action");
            if(action != null && !"".equals(action)) {
               String deploymentType = request.getParameter("deploymentType");
               ParamStruct struct = null;

               try {
                  struct = this.checkParams(config, deploymentType, action, rtDir);
               } catch (Exception var11) {
                  System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var11));
                  CommonResponseFrame.printToResponse(this.getErrorResponse("Could not execute the software activation because: " + var11.getMessage()), response);
                  return;
               }

               if(action.equals("ACTIVATE")) {
                  try {
                     this.activate(struct, config);
                     CommonResponseFrame.printToResponse(this.getPageEntryResponse(config, rtDir, "Software activation successfully completed"), response);
                  } catch (Exception var10) {
                     System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - ERROR - " + Util.exceptionToString(var10));
                     CommonResponseFrame.printToResponse(this.getErrorResponse("Could not execute the software activation because: " + var10.getMessage()), response);
                     return;
                  }
               }

            } else {
               CommonResponseFrame.printToResponse(this.getPageEntryResponse(config, rtDir, (String)null), response);
            }
         }
      }
   }

   public void activate(ParamStruct struct, Configuration config) throws IOException, XMLStreamException, ParserConfigurationException, SAXException {
      String[] tcHostPortParts = struct.tomcatHostPort.split(":");
      if(tcHostPortParts.length != 2) {
         throw new IllegalArgumentException("Could not split tomcat host and port " + struct.tomcatHostPort + " to host and port");
      } else {
         String hostname = tcHostPortParts[0];
         Integer port = null;

         try {
            port = Integer.valueOf(tcHostPortParts[1]);
         } catch (NumberFormatException var11) {
            throw new IllegalArgumentException("Could not split tomcat host and port " + struct.tomcatHostPort + " to host and port because the port part is not numeric: " + tcHostPortParts[1]);
         }

         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Starting to create war");
         File warFile = null;

         try {
            warFile = this.createWarFile(struct);
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished to create war");
            if(!MemUtil.isEnoughPermGenMemoryAvailable(warFile.length())) {
               throw new IllegalStateException("There is not enough permgen memory for activation available. PLEASE RESTART TOMCAT AND RETRY THE ACTIVATION!");
            }

            if(this.checkUndeploymentNeeded(struct.deploymentMeta.getWarName(), hostname, port.intValue(), config.getTomcatVersion())) {
               System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Undeploy from TC");
               this.undeployFromTomcat(struct.deploymentMeta.getWarName(), hostname, port.intValue(), config.getTomcatVersion());
            }

            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Deploy conf files");
            this.deployConfigFiles(struct, config);
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Deploy to TC");
            this.deployToTomcat(struct.deploymentMeta.getWarName(), hostname, port.intValue(), warFile, config.getTomcatVersion());
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - Finished deployment to TC");
         } finally {
            if(warFile != null && warFile.exists()) {
               warFile.delete();
            }

         }

      }
   }

   private void deployConfigFiles(ParamStruct struct, Configuration config) throws IOException {
      File jHydraBaseDir;
      if(Util.stringNullOrEmpty(struct.deploymentMeta.getConfContextName())) {
         jHydraBaseDir = struct.jHydraDir;
      } else {
         jHydraBaseDir = new File(struct.jHydraDir, struct.deploymentMeta.getConfContextName());
      }

      FileSystemUtil.copyDir(new File(struct.rtAppDir, "jhydra"), jHydraBaseDir, true);
      File jHydraInstBaseDir;
      if(Util.stringNullOrEmpty(struct.deploymentMeta.getConfContextName())) {
         jHydraInstBaseDir = new File(struct.jHydraDir, "1");
      } else {
         jHydraInstBaseDir = new File(new File(struct.jHydraDir, struct.deploymentMeta.getConfContextName()), "1");
      }

      FileSystemUtil.copyDir(new File(struct.rtAppDir, "jhydra-inst"), jHydraInstBaseDir, true);
   }

   private File createWarFile(ParamStruct struct) throws IOException, XMLStreamException, ParserConfigurationException, SAXException {
      File codeDir = new File(struct.rtAppDir, "code");
      File libDir = new File(struct.rtAppDir, "ThirdPartyLibs");
      File warTemp = new File(struct.tempDir, struct.deploymentMeta.getWarName());

      File var31;
      try {
         warTemp.mkdirs();
         File metaWarTemp = new File(warTemp, "META-INF");
         metaWarTemp.mkdirs();
         File webWarTemp = new File(warTemp, "WEB-INF");
         webWarTemp.mkdirs();
         FileSystemUtil.writeTextFile(metaWarTemp, "MANIFEST.MF", "Manifest-Version: 1.0\nCreated-By: MPDV Maintenance Manager");
         this.writeContextFile(metaWarTemp);
         File libDirTemp = new File(webWarTemp, "lib");
         File[] codeFiles = codeDir.listFiles();
         if(codeFiles != null) {
            File[] major = codeFiles;
            int webClassesDir = codeFiles.length;

            for(int confContextName = 0; confContextName < webClassesDir; ++confContextName) {
               File metaList = major[confContextName];
               FileSystemUtil.copyFile(metaList, new File(libDirTemp, metaList.getName()), true);
            }
         }

         FileSystemUtil.copyDir(libDir, libDirTemp, true);
         int var26 = Integer.parseInt(System.getProperty("java.version").split("\\.")[1]);
         File var27;
         if(var26 >= 8) {
            (new File(libDirTemp, "ojdbc5.jar")).delete();
            var27 = new File(this.getServletContext().getRealPath("/jdbc/ojdbc7.jar"));
            FileSystemUtil.copyFile(var27, new File(libDirTemp, var27.getName()), true);
         }

         var27 = new File(webWarTemp, "classes");
         var27.mkdirs();
         String var28 = struct.deploymentMeta.getConfContextName();
         if(!Util.stringNullOrEmpty(var28)) {
            FileSystemUtil.writeTextFile(var27, "config.properties", "configuration.context=" + var28 + "\n");
         }

         List var29 = this.loadMeta(new File(struct.rtAppDir, "domainMeta"));
         FileSystemUtil.writeTextFile(var27, "pluginConfig.conf", this.getPluginConfig(var29));
         ProjectMeta directMeta = this.getDirectServiceMeta(var29);
         File var30;
         if(directMeta == null) {
            XmlWriter warFile = null;
            XmlWriter jaxWsWriter = null;

            try {
               warFile = this.initWebFile(webWarTemp);
               this.appendWebFile(var29, warFile);
               this.finishWebFile(warFile);
               warFile.getWriter().flush();
               jaxWsWriter = this.initSunJaxWsFile(webWarTemp);
               this.appendSunJaxWsFile(var29, jaxWsWriter);
               this.finishSunJaxWsFile(jaxWsWriter);
               jaxWsWriter.getWriter().flush();
            } finally {
               if(warFile != null) {
                  warFile.closeCompletely();
               }

               if(jaxWsWriter != null) {
                  jaxWsWriter.closeCompletely();
               }

            }
         } else {
            var30 = new File(struct.rtAppDir, "conf");
            if(!var30.exists()) {
               throw new FileNotFoundException("Can not activate direct service " + directMeta.getProjectName() + " because the conf dir " + var30.getAbsolutePath() + " is missing");
            }

            FileSystemUtil.copyDir(var30, warTemp, true);
         }

         var30 = new File(struct.tempDir, struct.deploymentMeta.getWarName() + ".war");
         JarUtil.createJar(var30.getAbsolutePath(), warTemp, warTemp.getAbsolutePath() + File.separator);
         var31 = var30;
      } finally {
         FileSystemUtil.deleteDir(warTemp);
      }

      return var31;
   }

   private XmlWriter initSunJaxWsFile(File webInfDir) throws XMLStreamException, IOException {
      XMLOutputFactory2 xmlOutFact = (XMLOutputFactory2)XMLOutputFactory.newInstance();
      FileWriter fw = new FileWriter(new File(webInfDir, "sun-jaxws.xml"));
      XMLStreamWriter2 xmlWriter = xmlOutFact.createXMLStreamWriter(fw, "UTF-8");
      xmlWriter.writeStartDocument("1.0");
      XmlUtil.addLinebreak(xmlWriter);
      xmlWriter.writeStartElement("endpoints");
      xmlWriter.writeAttribute("xmlns", "http://java.sun.com/xml/ns/jax-ws/ri/runtime");
      xmlWriter.writeAttribute("version", "2.0");
      XmlUtil.addLinebreak(xmlWriter);
      return new XmlWriter(fw, xmlWriter);
   }

   private void appendSunJaxWsFile(List metaList, XmlWriter xmlWriter) throws XMLStreamException {
      Iterator i$ = metaList.iterator();

      while(i$.hasNext()) {
         ProjectMeta meta = (ProjectMeta)i$.next();
         if("metroservice".equals(meta.getProjectType())) {
            XMLStreamWriter2 writer = xmlWriter.getWriter();
            XmlUtil.addIndentation(writer, 1);
            writer.writeEmptyElement("endpoint");
            writer.writeAttribute("name", meta.getServiceName());
            writer.writeAttribute("implementation", meta.getServiceClass());
            writer.writeAttribute("url-pattern", "/" + meta.getServiceUrl());
            XmlUtil.addLinebreak(writer);
            XmlUtil.addLinebreak(writer);
         }
      }

   }

   private void finishSunJaxWsFile(XmlWriter xmlWriter) throws XMLStreamException {
      if(xmlWriter == null) {
         throw new NullPointerException("Parameter xmlWriter is null");
      } else {
         XMLStreamWriter2 writer = xmlWriter.getWriter();
         writer.writeEndElement();
         writer.writeEndDocument();
         writer.flush();
      }
   }

   private XmlWriter initWebFile(File webInfDir) throws IOException, XMLStreamException {
      XMLOutputFactory2 xmlOutFact = (XMLOutputFactory2)XMLOutputFactory.newInstance();
      FileWriter fw = new FileWriter(new File(webInfDir, "web.xml"));
      XMLStreamWriter2 xmlWriter = xmlOutFact.createXMLStreamWriter(fw, "UTF-8");
      xmlWriter.writeStartDocument("1.0");
      XmlUtil.addLinebreak(xmlWriter);
      xmlWriter.writeRaw("<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/j2ee/dtds/web-app_2_3.dtd\">");
      XmlUtil.addLinebreak(xmlWriter);
      XmlUtil.addLinebreak(xmlWriter);
      xmlWriter.writeStartElement("web-app");
      XmlUtil.addLinebreak(xmlWriter);
      XmlUtil.addIndentation(xmlWriter, 1);
      xmlWriter.writeStartElement("listener");
      XmlUtil.addLinebreak(xmlWriter);
      XmlUtil.addIndentation(xmlWriter, 2);
      xmlWriter.writeStartElement("listener-class");
      xmlWriter.writeCharacters("com.sun.xml.ws.transport.http.servlet.WSServletContextListener");
      xmlWriter.writeEndElement();
      XmlUtil.addLinebreak(xmlWriter);
      XmlUtil.addIndentation(xmlWriter, 1);
      xmlWriter.writeEndElement();
      XmlUtil.addLinebreak(xmlWriter);
      return new XmlWriter(fw, xmlWriter);
   }

   private void appendWebFile(List metaList, XmlWriter xmlWriter) throws XMLStreamException {
      Iterator i$ = metaList.iterator();

      while(i$.hasNext()) {
         ProjectMeta meta = (ProjectMeta)i$.next();
         XMLStreamWriter2 writer;
         if("metroservice".equals(meta.getProjectType())) {
            writer = xmlWriter.getWriter();
            XmlUtil.addIndentation(writer, 1);
            writer.writeStartElement("servlet");
            XmlUtil.addLinebreak(writer);
            XmlUtil.addIndentation(writer, 2);
            writer.writeStartElement("servlet-name");
            writer.writeCharacters(meta.getServiceName() + "Servlet");
            writer.writeEndElement();
            XmlUtil.addLinebreak(writer);
            XmlUtil.addIndentation(writer, 2);
            writer.writeStartElement("servlet-class");
            writer.writeCharacters("com.sun.xml.ws.transport.http.servlet.WSServlet");
            writer.writeEndElement();
            XmlUtil.addLinebreak(writer);
            XmlUtil.addIndentation(writer, 2);
            writer.writeStartElement("load-on-startup");
            writer.writeCharacters("1");
            writer.writeEndElement();
            XmlUtil.addLinebreak(writer);
            XmlUtil.addIndentation(writer, 1);
            writer.writeEndElement();
            XmlUtil.addLinebreak(writer);
            XmlUtil.addIndentation(writer, 1);
            writer.writeStartElement("servlet-mapping");
            XmlUtil.addLinebreak(writer);
            XmlUtil.addIndentation(writer, 2);
            writer.writeStartElement("servlet-name");
            writer.writeCharacters(meta.getServiceName() + "Servlet");
            writer.writeEndElement();
            XmlUtil.addLinebreak(writer);
            XmlUtil.addIndentation(writer, 2);
            writer.writeStartElement("url-pattern");
            writer.writeCharacters("/" + meta.getServiceUrl());
            writer.writeEndElement();
            XmlUtil.addLinebreak(writer);
            XmlUtil.addIndentation(writer, 1);
            writer.writeEndElement();
            XmlUtil.addLinebreak(writer);
            XmlUtil.addLinebreak(writer);
         } else if("servletlibrary".equals(meta.getProjectType())) {
            writer = xmlWriter.getWriter();
            XmlUtil.addIndentation(writer, 1);
            writer.writeStartElement("servlet");
            XmlUtil.addLinebreak(writer);
            XmlUtil.addIndentation(writer, 2);
            writer.writeStartElement("servlet-name");
            writer.writeCharacters(meta.getServletName());
            writer.writeEndElement();
            XmlUtil.addLinebreak(writer);
            XmlUtil.addIndentation(writer, 2);
            writer.writeStartElement("servlet-class");
            writer.writeCharacters(meta.getServletClass());
            writer.writeEndElement();
            XmlUtil.addLinebreak(writer);
            XmlUtil.addIndentation(writer, 2);
            writer.writeStartElement("load-on-startup");
            writer.writeCharacters("1");
            writer.writeEndElement();
            XmlUtil.addLinebreak(writer);
            XmlUtil.addIndentation(writer, 1);
            writer.writeEndElement();
            XmlUtil.addLinebreak(writer);
            XmlUtil.addLinebreak(writer);
         }
      }

   }

   private void finishWebFile(XmlWriter xmlWriter) throws XMLStreamException {
      if(xmlWriter == null) {
         throw new NullPointerException("Parameter xmlWriter is null");
      } else {
         XMLStreamWriter2 writer = xmlWriter.getWriter();
         XmlUtil.addIndentation(writer, 1);
         writer.writeStartElement("session-config");
         XmlUtil.addLinebreak(writer);
         XmlUtil.addIndentation(writer, 2);
         writer.writeStartElement("session-timeout");
         writer.writeCharacters("60");
         writer.writeEndElement();
         XmlUtil.addLinebreak(writer);
         XmlUtil.addIndentation(writer, 1);
         writer.writeEndElement();
         XmlUtil.addLinebreak(writer);
         writer.writeEndElement();
         writer.writeEndDocument();
         writer.flush();
      }
   }

   private ProjectMeta getDirectServiceMeta(List metaList) {
      Iterator i$ = metaList.iterator();

      ProjectMeta meta;
      do {
         if(!i$.hasNext()) {
            return null;
         }

         meta = (ProjectMeta)i$.next();
      } while(!"directservice".equals(meta.getProjectType()));

      return meta;
   }

   private List loadMeta(File metaFileDir) throws ParserConfigurationException, SAXException, IOException {
      LinkedList metaList = new LinkedList();
      File[] metaFiles = metaFileDir.listFiles();
      if(metaFiles != null) {
         File[] arr$ = metaFiles;
         int len$ = metaFiles.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            File metaFile = arr$[i$];
            ProjectMeta meta = ProjectMeta.loadProjectMeta(metaFile.getParentFile().getAbsolutePath(), COMPILE.matcher(metaFile.getName()).replaceAll(""));
            metaList.add(meta);
         }
      }

      return metaList;
   }

   private String getPluginConfig(List metaList) {
      StringBuilder builder = new StringBuilder();
      Iterator it = metaList.iterator();

      while(it.hasNext()) {
         ProjectMeta meta = (ProjectMeta)it.next();
         List plugClasses = meta.getProvidedPluginClasses();
         int count = plugClasses.size();

         for(int j = 0; j < count; ++j) {
            String plugClass = (String)plugClasses.get(j);
            builder.append(plugClass);
            builder.append("\n");
         }
      }

      return builder.toString();
   }

   private void writeContextFile(File metaFolder) throws IOException, XMLStreamException {
      XMLOutputFactory2 xmlOutFact = (XMLOutputFactory2)XMLOutputFactory.newInstance();
      FileWriter fw = null;
      XMLStreamWriter2 xmlWriter = null;

      try {
         fw = new FileWriter(new File(metaFolder, "context.xml"));
         xmlWriter = xmlOutFact.createXMLStreamWriter(fw, "UTF-8");
         xmlWriter.writeStartDocument("1.0");
         XmlUtil.addLinebreak(xmlWriter);
         xmlWriter.writeStartElement("Context");
         xmlWriter.writeAttribute("antiJARLocking", "true");
         xmlWriter.writeAttribute("antiResourceLocking", "true");
         XmlUtil.addLinebreak(xmlWriter);
         XmlUtil.addIndentation(xmlWriter, 1);
         xmlWriter.writeStartElement("WatchedResource");
         xmlWriter.writeCharacters("WEB-INF/web.xml");
         xmlWriter.writeEndElement();
         XmlUtil.addLinebreak(xmlWriter);
         xmlWriter.writeEndElement();
         xmlWriter.writeEndDocument();
         xmlWriter.flush();
         fw.flush();
      } finally {
         if(xmlWriter != null) {
            try {
               xmlWriter.close();
            } catch (XMLStreamException var14) {
               ;
            }
         }

         if(fw != null) {
            try {
               fw.close();
            } catch (IOException var13) {
               ;
            }
         }

      }

   }

   public File checkEntryPageParams(Configuration config) {
      if(config == null) {
         throw new IllegalArgumentException("Could not get configuration");
      } else {
         String rtDir = config.getRuntimeDirServer();
         if(rtDir != null && !"".equals(rtDir)) {
            File rtDirFile = new File(rtDir);
            if(!rtDirFile.exists()) {
               throw new IllegalStateException("The runtime dir does not exist: " + rtDirFile.getAbsolutePath());
            } else {
               return rtDirFile;
            }
         } else {
            throw new IllegalStateException("The runtime dir is not configured");
         }
      }
   }

   private String getErrorResponse(String message) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Software activation</h1>\n");
      builder.append("<br />\n");
      builder.append("<span style=\"color:red;\">An error has occured: ").append(message).append("</span><br />");
      builder.append("</div>\n");
      return builder.toString();
   }

   private String getPageEntryResponse(Configuration config, File rtDir, String message) {
      StringBuilder builder = new StringBuilder();
      builder.append("<div style=\"text-align:center;width:100%;\">\n");
      builder.append("<h1>Software activation</h1>\n");
      if(message != null) {
         builder.append("<br />\n");
         builder.append("<span style=\"color:#009600;\">").append(message).append("</span><br />");
      }

      builder.append("<br />\n");
      builder.append("<script type=\"text/javascript\">\n");
      builder.append("function activate()");
      builder.append("{");
      builder.append("document.getElementById(\"progress\").innerHTML=\"Activation in progress! Please wait...<br />\";");
      builder.append("document.getElementById(\"activationform\").submit();");
      builder.append("}");
      builder.append("</script>\n");
      builder.append("<span id=\"progress\" style=\"color:blue;\"></span><br />");
      builder.append("<form action=\"Activation\" id=\"activationform\" method=\"post\">\n");
      builder.append("<input type=\"hidden\" id=\"action\" name=\"action\" value=\"ACTIVATE\" />\n");
      builder.append("Select deployment type: <select name=\"deploymentType\" size=\"1\">\n");
      Set availableDeploymentTypes = this.getAvailableDeploymentTypes(rtDir);
      Iterator it = availableDeploymentTypes.iterator();

      while(it.hasNext()) {
         String deploymentType = (String)it.next();
         builder.append("<option value=\"").append(deploymentType).append("\">").append(deploymentType).append("</option>\n");
      }

      builder.append("</select><br />\n");
      builder.append("<br />\n");
      builder.append("<input type=\"button\" name=\"Submit\" value=\"Activate\" onClick=\"activate();\"/><br />\n");
      builder.append("</form>\n");
      builder.append("</div>\n");
      return builder.toString();
   }

   private Set getAvailableDeploymentTypes(File rtDir) {
      HashSet deploymentTypesSet = new HashSet();
      File[] subFolders = rtDir.listFiles();
      int count = subFolders.length;

      for(int i = 0; i < count; ++i) {
         File currFolder = subFolders[i];
         if(currFolder.isDirectory()) {
            deploymentTypesSet.add(currFolder.getName());
         }
      }

      return deploymentTypesSet;
   }

   public ParamStruct checkParams(Configuration config, String deploymentType, String action, File rtDir) {
      if(deploymentType != null && !"".equals(deploymentType)) {
         if(action != null && !"".equals(action)) {
            if(!action.equals("ACTIVATE")) {
               throw new IllegalArgumentException("Unknown action specified: " + action + ". Allowed actions are: " + "ACTIVATE");
            } else {
               Set availableDeploymentTypes = this.getAvailableDeploymentTypes(rtDir);
               if(!availableDeploymentTypes.contains(deploymentType)) {
                  throw new IllegalArgumentException("Unknown deployment type specified: " + deploymentType + ". Allowed deployment types are: " + availableDeploymentTypes);
               } else {
                  DeploymentMeta deplMeta = null;

                  try {
                     deplMeta = DeploymentMeta.loadDeploymentMeta(new File(rtDir, deploymentType));
                  } catch (Exception var16) {
                     throw new RuntimeException("Deployment meta data for deployment type " + deploymentType + " could not be loaded", var16);
                  }

                  String warName = deplMeta.getWarName();
                  if(warName != null && !"".equals(warName)) {
                     ParamStruct struct = new ParamStruct();
                     struct.deploymentMeta = deplMeta;
                     String rtDirPrefix = File.separator + deploymentType + File.separator;
                     File rtAppDir = new File(rtDir, rtDirPrefix);
                     if(!rtAppDir.exists()) {
                        throw new IllegalStateException("The runtime dir for deployment type " + deploymentType + " does not exist: " + rtAppDir.getAbsolutePath());
                     } else {
                        struct.rtAppDir = rtAppDir;
                        String jHydraDir = config.getjHydraDir();
                        if(jHydraDir != null && !"".equals(jHydraDir)) {
                           File jHydraDirFile = new File(jHydraDir);
                           if(!jHydraDirFile.exists()) {
                              throw new IllegalStateException("The JHYDRADIR does not exist: " + jHydraDirFile.getAbsolutePath());
                           } else {
                              struct.jHydraDir = jHydraDirFile;
                              String tempDir = config.getTempDir();
                              if(tempDir != null && !"".equals(tempDir)) {
                                 File tempDirFile = new File(tempDir);
                                 if(!tempDirFile.exists()) {
                                    throw new IllegalStateException("The temp dir does not exist: " + tempDirFile.getAbsolutePath());
                                 } else {
                                    struct.tempDir = tempDirFile;
                                    String tcHostPort = config.getTomcatHostPort();
                                    if(tcHostPort != null && !"".equals(tcHostPort)) {
                                       struct.tomcatHostPort = tcHostPort;
                                       return struct;
                                    } else {
                                       throw new IllegalStateException("The tomcat host and port is not configured");
                                    }
                                 }
                              } else {
                                 throw new IllegalStateException("The temp dir is not configured");
                              }
                           }
                        } else {
                           throw new IllegalStateException("The JHYDRADIR is not configured");
                        }
                     }
                  } else {
                     throw new IllegalArgumentException("The name of the war file for the deployment type " + deploymentType + " is missing");
                  }
               }
            }
         } else {
            throw new IllegalArgumentException("The action is not specified");
         }
      } else {
         throw new IllegalArgumentException("The deployment type is not specified");
      }
   }

   private void deployToTomcat(String warName, String host, int port, File warFile, int tomcatVersion) {
      HttpCallResult result = null;

      try {
         if(tomcatVersion < 7) {
            result = this.callUrl(host, port, "ad", "Mosbach74821", "/manager/deploy?path=/" + warName, warFile);
         } else {
            result = this.callUrl(host, port, "ad", "Mosbach74821", "/manager/text/deploy?path=/" + warName, warFile);
         }
      } catch (Exception var8) {
         throw new RuntimeException("Could not deploy " + warName + ".", var8);
      }

      if(result.retCode != 200) {
         throw new RuntimeException("Could not deploy " + warName + ". Return code was: " + result.retCode + ", response was: " + result.response);
      } else if(result.response.startsWith("FAIL")) {
         throw new RuntimeException("Error at deployment of " + warName + ". Response from server was: " + result.response);
      }
   }

   private void undeployFromTomcat(String warName, String host, int port, int tomcatVersion) {
      HttpCallResult result = null;

      try {
         if(tomcatVersion < 7) {
            result = this.callUrl(host, port, "ad", "Mosbach74821", "/manager/undeploy?path=/" + warName, (File)null);
         } else {
            result = this.callUrl(host, port, "ad", "Mosbach74821", "/manager/text/undeploy?path=/" + warName, (File)null);
         }
      } catch (Exception var7) {
         throw new RuntimeException("Could not undeploy " + warName + ".", var7);
      }

      if(result.retCode != 200) {
         throw new RuntimeException("Could not undeploy " + warName + ". Return code was: " + result.retCode + ", response was: " + result.response);
      } else if(result.response.startsWith("FAIL")) {
         throw new RuntimeException("Error at undeployment of " + warName + ". Response from server was: " + result.response);
      }
   }

   private boolean checkUndeploymentNeeded(String warName, String host, int port, int tomcatVersion) {
      HttpCallResult result = null;

      try {
         if(tomcatVersion < 7) {
            result = this.callUrl(host, port, "ad", "Mosbach74821", "/manager/list", (File)null);
         } else {
            result = this.callUrl(host, port, "ad", "Mosbach74821", "/manager/text/list", (File)null);
         }
      } catch (Exception var12) {
         throw new RuntimeException("Could not determine if Undeployment of " + warName + " is needed.", var12);
      }

      if(result.retCode != 200) {
         throw new RuntimeException("Could not determine if Undeployment of " + warName + " is needed. Return code was: " + result.retCode + ", response was: " + result.response);
      } else {
         String[] lines = result.response.split("\\n");
         int len = lines.length;
         String[] arr$ = lines;
         int len$ = lines.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String line = arr$[i$];
            if(line.startsWith("/" + warName + ":")) {
               return true;
            }
         }

         return false;
      }
   }

   private HttpCallResult callUrl(String host, int port, String user, String pass, String url, File warFile) throws IOException {
      String realUrl = "http://" + host + ":" + port + url;
      HttpClient client = new HttpClient();
      Object method = null;
      if(warFile == null) {
         method = new GetMethod(realUrl);
      } else {
         method = new PutMethod(realUrl);
      }

      ((HttpMethod)method).getParams().setParameter("http.method.retry-handler", new DefaultHttpMethodRetryHandler(3, false));
      if(!Util.stringNullOrEmpty(user) && !Util.stringNullOrEmpty(pass)) {
         client.getParams().setAuthenticationPreemptive(true);
         UsernamePasswordCredentials result = new UsernamePasswordCredentials(user, pass);
         client.getState().setCredentials(new AuthScope(host, port, AuthScope.ANY_REALM), result);
      }

      if(warFile != null) {
         ((EntityEnclosingMethod)method).setRequestEntity(new FileRequestEntity(warFile, "application/jar"));
      }

      try {
         HttpCallResult result1 = new HttpCallResult();
         result1.retCode = client.executeMethod((HttpMethod)method);
         String respCharSet = ((HttpMethodBase)method).getResponseCharSet();
         InputStream respStream = ((HttpMethod)method).getResponseBodyAsStream();
         ByteArrayOutputStream outStr = new ByteArrayOutputStream();
         boolean read = true;

         int read1;
         while((read1 = respStream.read()) != -1) {
            outStr.write(read1);
         }

         outStr.flush();
         byte[] responseBody = outStr.toByteArray();
         result1.response = new String(responseBody, respCharSet);
         HttpCallResult var16 = result1;
         return var16;
      } finally {
         ((HttpMethod)method).releaseConnection();
      }
   }

}