package de.mpdv.maintenanceManager.data.javaServer;

import de.mpdv.maintenanceManager.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ProjectMeta {

   private final String projectName;
   private final String projectType;
   private final String replaceProject;
   private final Boolean useInWar;
   private final List providedPluginClasses;
   private final String serviceName;
   private final String serviceClass;
   private final String serviceUrl;
   private final String servletName;
   private final String servletClass;


   public ProjectMeta(String projectType, String replaceProject, Boolean useInWar, List providedPluginClasses, String serviceName, String serviceClass, String serviceUrl, String servletName, String servletClass, String projectName) {
      this.projectType = projectType;
      this.replaceProject = replaceProject;
      this.useInWar = useInWar;
      this.providedPluginClasses = providedPluginClasses;
      this.serviceName = serviceName;
      this.serviceClass = serviceClass;
      this.serviceUrl = serviceUrl;
      this.servletName = servletName;
      this.servletClass = servletClass;
      this.projectName = projectName;
   }

   public String getProjectType() {
      return this.projectType;
   }

   public String getReplaceProject() {
      return this.replaceProject;
   }

   public Boolean getUseInWar() {
      return this.useInWar;
   }

   public List getProvidedPluginClasses() {
      return this.providedPluginClasses;
   }

   public String getServiceName() {
      return this.serviceName;
   }

   public String getServiceClass() {
      return this.serviceClass;
   }

   public String getServiceUrl() {
      return this.serviceUrl;
   }

   public String getServletName() {
      return this.servletName;
   }

   public String getServletClass() {
      return this.servletClass;
   }

   public String getProjectName() {
      return this.projectName;
   }

   public String toString() {
      return "ProjectMeta [projectType=" + this.projectType + ", providedPluginClasses=" + this.providedPluginClasses + ", replaceProject=" + this.replaceProject + ", serviceClass=" + this.serviceClass + ", serviceName=" + this.serviceName + ", serviceUrl=" + this.serviceUrl + ", servletClass=" + this.servletClass + ", servletName=" + this.servletName + ", useInWar=" + this.useInWar + ", projectName=" + this.projectName + "]";
   }

   public static ProjectMeta loadProjectMeta(String projectPath, String projectName) throws ParserConfigurationException, SAXException, FileNotFoundException, IOException {
      DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = fact.newDocumentBuilder();
      FileInputStream inStr = null;

      ProjectMeta var30;
      try {
         inStr = new FileInputStream(new File(projectPath, projectName + ".xml"));
         Document document = builder.parse(new InputSource(inStr));
         NodeList metaRoot = document.getElementsByTagName("projectMeta");
         if(metaRoot.getLength() != 1) {
            throw new IllegalArgumentException("The tag projectMeta is not contained, or contained more than once");
         }

         Node rootNode = metaRoot.item(0);
         String type = null;
         String replaceProject = null;
         Boolean useInWar = null;
         String serviceName = null;
         String serviceClass = null;
         String serviceUrl = null;
         String servletName = null;
         String servletClass = null;
         LinkedList providedPluginClasses = new LinkedList();
         NodeList contents = rootNode.getChildNodes();
         int count = contents.getLength();

         for(int i = 0; i < count; ++i) {
            Node e = contents.item(i);
            String nodeName = e.getNodeName();
            if(nodeName.equals("projectType")) {
               type = e.getTextContent();
            } else if(nodeName.equals("replaceProject")) {
               replaceProject = e.getTextContent();
            } else if(nodeName.equals("useInWar")) {
               String useInWarStr = e.getTextContent();
               if(!useInWarStr.toLowerCase().equals("false") && !useInWarStr.toLowerCase().equals("true")) {
                  throw new IllegalArgumentException("The xml element useInWar does not contain a valid boolean value: " + useInWarStr);
               }

               useInWar = Boolean.valueOf(useInWarStr);
            } else if(nodeName.equals("serviceName")) {
               serviceName = e.getTextContent();
            } else if(nodeName.equals("serviceClass")) {
               serviceClass = e.getTextContent();
            } else if(nodeName.equals("serviceUrl")) {
               serviceUrl = e.getTextContent();
            } else if(nodeName.equals("servletName")) {
               servletName = e.getTextContent();
            } else if(nodeName.equals("servletClass")) {
               servletClass = e.getTextContent();
            } else if(nodeName.equals("pluginClass")) {
               providedPluginClasses.add(e.getTextContent());
            }
         }

         if(Util.stringNullOrEmpty(type)) {
            throw new IllegalArgumentException("The mandatory element projectType is missing in " + projectName + " of " + projectPath);
         }

         if(useInWar == null) {
            useInWar = Boolean.FALSE;
         }

         if("servletlibrary".equals(type)) {
            if(Util.stringNullOrEmpty(servletName)) {
               throw new IllegalArgumentException("The mandatory element servletName for project type servletlibrary is missing in " + projectName + " of " + projectPath);
            }

            if(Util.stringNullOrEmpty(servletClass)) {
               throw new IllegalArgumentException("The mandatory element servletClass for project type servletlibrary is missing in " + projectName + " of " + projectPath);
            }
         } else if("metroservice".equals(type)) {
            if(Util.stringNullOrEmpty(serviceName)) {
               throw new IllegalArgumentException("The mandatory element serviceName for project type metroservice is missing in " + projectName + " of " + projectPath);
            }

            if(Util.stringNullOrEmpty(serviceClass)) {
               throw new IllegalArgumentException("The mandatory element serviceClass for project type metroservice is missing in " + projectName + " of " + projectPath);
            }

            if(Util.stringNullOrEmpty(serviceUrl)) {
               throw new IllegalArgumentException("The mandatory element serviceUrl for project type metroservice is missing in " + projectName + " of " + projectPath);
            }
         }

         var30 = new ProjectMeta(type, replaceProject, useInWar, providedPluginClasses, serviceName, serviceClass, serviceUrl, servletName, servletClass, projectName);
      } finally {
         if(inStr != null) {
            try {
               inStr.close();
            } catch (Exception var28) {
               ;
            }
         }

      }

      return var30;
   }

   public void saveProjectMeta(String projectPath, String saveName) throws XMLStreamException, IOException {
      XMLOutputFactory2 xmlOutFact = (XMLOutputFactory2)XMLOutputFactory.newInstance();
      xmlOutFact.configureForXmlConformance();
      FileOutputStream os = null;
      OutputStreamWriter ow = null;
      XMLStreamWriter2 xmlWriter = null;

      try {
         os = new FileOutputStream(new File(projectPath, saveName + ".xml"));
         ow = new OutputStreamWriter(os, "UTF-8");
         xmlWriter = xmlOutFact.createXMLStreamWriter(ow, "UTF-8");
         xmlWriter.writeStartDocument("1.0");
         xmlWriter.writeCharacters("\n");
         xmlWriter.writeStartElement("projectMeta");
         xmlWriter.writeCharacters("\n");
         xmlWriter.writeCharacters("\t");
         xmlWriter.writeStartElement("projectType");
         xmlWriter.writeCharacters(this.projectType);
         xmlWriter.writeEndElement();
         xmlWriter.writeCharacters("\n");
         if(!Util.stringNullOrEmpty(this.replaceProject)) {
            xmlWriter.writeCharacters("\t");
            xmlWriter.writeStartElement("replaceProject");
            xmlWriter.writeCharacters(this.replaceProject);
            xmlWriter.writeEndElement();
            xmlWriter.writeCharacters("\n");
         }

         xmlWriter.writeCharacters("\t");
         xmlWriter.writeStartElement("useInWar");
         xmlWriter.writeCharacters(this.useInWar.toString());
         xmlWriter.writeEndElement();
         xmlWriter.writeCharacters("\n");
         if(!Util.stringNullOrEmpty(this.servletName)) {
            xmlWriter.writeCharacters("\t");
            xmlWriter.writeStartElement("servletName");
            xmlWriter.writeCharacters(this.servletName);
            xmlWriter.writeEndElement();
            xmlWriter.writeCharacters("\n");
         }

         if(!Util.stringNullOrEmpty(this.servletClass)) {
            xmlWriter.writeCharacters("\t");
            xmlWriter.writeStartElement("servletClass");
            xmlWriter.writeCharacters(this.servletClass);
            xmlWriter.writeEndElement();
            xmlWriter.writeCharacters("\n");
         }

         if(!Util.stringNullOrEmpty(this.serviceName)) {
            xmlWriter.writeCharacters("\t");
            xmlWriter.writeStartElement("serviceName");
            xmlWriter.writeCharacters(this.serviceName);
            xmlWriter.writeEndElement();
            xmlWriter.writeCharacters("\n");
         }

         if(!Util.stringNullOrEmpty(this.serviceClass)) {
            xmlWriter.writeCharacters("\t");
            xmlWriter.writeStartElement("serviceClass");
            xmlWriter.writeCharacters(this.serviceClass);
            xmlWriter.writeEndElement();
            xmlWriter.writeCharacters("\n");
         }

         if(!Util.stringNullOrEmpty(this.serviceUrl)) {
            xmlWriter.writeCharacters("\t");
            xmlWriter.writeStartElement("serviceUrl");
            xmlWriter.writeCharacters(this.serviceUrl);
            xmlWriter.writeEndElement();
            xmlWriter.writeCharacters("\n");
         }

         int e = this.providedPluginClasses.size();

         for(int i = 0; i < e; ++i) {
            String currClass = (String)this.providedPluginClasses.get(i);
            xmlWriter.writeCharacters("\t");
            xmlWriter.writeStartElement("pluginClass");
            xmlWriter.writeCharacters(currClass);
            xmlWriter.writeEndElement();
            xmlWriter.writeCharacters("\n");
         }

         xmlWriter.writeEndElement();
         xmlWriter.writeCharacters("\n");
         xmlWriter.writeEndDocument();
         xmlWriter.flush();
         ow.flush();
         os.flush();
      } finally {
         if(xmlWriter != null) {
            try {
               xmlWriter.close();
            } catch (XMLStreamException var21) {
               ;
            }
         }

         if(ow != null) {
            try {
               ow.close();
            } catch (IOException var20) {
               ;
            }
         }

         if(os != null) {
            try {
               os.close();
            } catch (IOException var19) {
               ;
            }
         }

      }
   }
}