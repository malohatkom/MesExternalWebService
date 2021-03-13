package de.mpdv.maintenanceManager.data.client;

import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ClientPackageMeta {

   private static final String PACKAGE_META_FILE = "clientPackageMeta.xml";
   private final String name;
   private final Calendar creationDate;
   private final String description;
   private Calendar deploymentDate;
   private final List domains;
   private final Map domainVersions;
   private final String applicationName;


   public ClientPackageMeta(String name, Calendar creationDate, Calendar deploymentDate, String description, List domains, Map domainVersions, String applicationName) {
      this.name = name;
      this.creationDate = creationDate;
      this.deploymentDate = deploymentDate;
      this.description = description;
      this.domains = domains;
      this.domainVersions = domainVersions;
      this.applicationName = applicationName;
   }

   public String getName() {
      return this.name;
   }

   public Calendar getCreationDate() {
      return this.creationDate;
   }

   public String getDescription() {
      return this.description;
   }

   public Calendar getDeploymentDate() {
      return this.deploymentDate;
   }

   public void setDeploymentDate(Calendar deploymentDate) {
      this.deploymentDate = deploymentDate;
   }

   public List getDomains() {
      return this.domains;
   }

   public String getApplicationName() {
      return this.applicationName;
   }

   public Map getDomainVersions() {
      return this.domainVersions;
   }

   public String toString() {
      return "ClientPackageMeta [name=" + this.name + ", creationDate=" + this.creationDate + ", description=" + this.description + ", deploymentDate=" + this.deploymentDate + ", domains=" + this.domains + ", domainVersions=" + this.domainVersions + ", applicationName=" + this.applicationName + "]";
   }

   public static ClientPackageMeta loadPackageMeta(String packagePath) throws ParserConfigurationException, SAXException, FileNotFoundException, IOException, ParseException {
      DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = fact.newDocumentBuilder();
      FileInputStream inStr = null;

      ClientPackageMeta var33;
      try {
         inStr = new FileInputStream(new File(packagePath, "clientPackageMeta.xml"));
         Document document = builder.parse(new InputSource(inStr));
         NodeList metaRoot = document.getElementsByTagName("packageMeta");
         if(metaRoot.getLength() != 1) {
            throw new IllegalArgumentException("The tag packageMeta is not contained, or contained more than once");
         }

         Node rootNode = metaRoot.item(0);
         String name = null;
         Calendar creationDate = null;
         String description = null;
         Calendar deploymentDate = null;
         LinkedList domainList = new LinkedList();
         HashMap domainVersions = new HashMap();
         String appName = null;
         NodeList contents = rootNode.getChildNodes();
         int count = contents.getLength();

         for(int i = 0; i < count; ++i) {
            Node e = contents.item(i);
            String nodeName = e.getNodeName();
            if(nodeName.equals("name")) {
               name = e.getTextContent();
            } else if(nodeName.equals("applicationName")) {
               appName = e.getTextContent();
            } else {
               String domain;
               if(nodeName.equals("creationDate")) {
                  domain = e.getTextContent();

                  try {
                     creationDate = DateTimeUtil.xsdStringToCalendarUtc(domain);
                  } catch (Exception var31) {
                     throw new IllegalArgumentException("Could not parse creation date to calender: " + domain);
                  }
               } else if(nodeName.equals("description")) {
                  description = e.getTextContent();
               } else if(nodeName.equals("deploymentDate")) {
                  domain = e.getTextContent();

                  try {
                     deploymentDate = DateTimeUtil.xsdStringToCalendarUtc(domain);
                  } catch (Exception var30) {
                     throw new IllegalArgumentException("Could not parse deployment date to calender: " + domain);
                  }
               } else if(nodeName.equals("domain")) {
                  domain = e.getTextContent();
                  if(!Util.stringNullOrEmpty(domain)) {
                     domainList.add(domain);
                     if(e.getNodeType() == 1) {
                        NamedNodeMap attribMap = e.getAttributes();
                        if(attribMap != null) {
                           Node versionNode = attribMap.getNamedItem("version");
                           if(versionNode != null) {
                              domainVersions.put(domain, versionNode.getTextContent());
                           }
                        }
                     }
                  }
               }
            }
         }

         if(Util.stringNullOrEmpty(name)) {
            throw new IllegalArgumentException("The mandatory element name is missing in clientPackageMeta.xml of " + packagePath);
         }

         if(Util.stringNullOrEmpty(description)) {
            throw new IllegalArgumentException("The mandatory element description is missing in clientPackageMeta.xml of " + packagePath);
         }

         if(creationDate == null) {
            throw new IllegalArgumentException("The mandatory element creationDate is missing in clientPackageMeta.xml of " + packagePath);
         }

         if(domainList.size() == 0) {
            throw new IllegalArgumentException("No domains contained in file clientPackageMeta.xml of " + packagePath);
         }

         if(Util.stringNullOrEmpty(appName)) {
            throw new IllegalArgumentException("The mandatory element applicationName is missing in clientPackageMeta.xml of " + packagePath);
         }

         var33 = new ClientPackageMeta(name, creationDate, deploymentDate, description, domainList, domainVersions, appName);
      } finally {
         if(inStr != null) {
            try {
               inStr.close();
            } catch (Exception var29) {
               ;
            }
         }

      }

      return var33;
   }

   public void savePackageMeta(String packagePath) throws XMLStreamException, IOException {
      XMLOutputFactory2 xmlOutFact = (XMLOutputFactory2)XMLOutputFactory.newInstance();
      xmlOutFact.configureForXmlConformance();
      FileOutputStream os = null;
      OutputStreamWriter ow = null;
      XMLStreamWriter2 xmlWriter = null;

      try {
         os = new FileOutputStream(new File(packagePath, "clientPackageMeta.xml"));
         ow = new OutputStreamWriter(os, "UTF-8");
         xmlWriter = xmlOutFact.createXMLStreamWriter(ow, "UTF-8");
         xmlWriter.writeStartDocument("1.0");
         xmlWriter.writeCharacters("\n");
         xmlWriter.writeStartElement("packageMeta");
         xmlWriter.writeCharacters("\n");
         xmlWriter.writeCharacters("\t");
         xmlWriter.writeStartElement("name");
         xmlWriter.writeCharacters(this.name);
         xmlWriter.writeEndElement();
         xmlWriter.writeCharacters("\n");
         xmlWriter.writeCharacters("\t");
         xmlWriter.writeStartElement("description");
         xmlWriter.writeCharacters(this.description);
         xmlWriter.writeEndElement();
         xmlWriter.writeCharacters("\n");
         xmlWriter.writeCharacters("\t");
         xmlWriter.writeStartElement("creationDate");
         xmlWriter.writeCharacters(DateTimeUtil.calendarUtcToXsdString(this.creationDate));
         xmlWriter.writeEndElement();
         xmlWriter.writeCharacters("\n");
         if(this.deploymentDate != null) {
            xmlWriter.writeCharacters("\t");
            xmlWriter.writeStartElement("deploymentDate");
            xmlWriter.writeCharacters(DateTimeUtil.calendarUtcToXsdString(this.deploymentDate));
            xmlWriter.writeEndElement();
            xmlWriter.writeCharacters("\n");
         }

         int e = this.domains.size();

         for(int i = 0; i < e; ++i) {
            String domain = (String)this.domains.get(i);
            xmlWriter.writeCharacters("\t");
            xmlWriter.writeStartElement("domain");
            if(this.domainVersions.containsKey(domain)) {
               String version = (String)this.domainVersions.get(domain);
               if(version != null && !"".equals(version)) {
                  xmlWriter.writeAttribute("version", version);
               }
            }

            xmlWriter.writeCharacters(domain);
            xmlWriter.writeEndElement();
            xmlWriter.writeCharacters("\n");
         }

         xmlWriter.writeCharacters("\t");
         xmlWriter.writeStartElement("applicationName");
         xmlWriter.writeCharacters(this.applicationName);
         xmlWriter.writeEndElement();
         xmlWriter.writeCharacters("\n");
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