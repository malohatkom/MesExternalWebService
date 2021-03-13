package de.mpdv.maintenanceManager.data.javaServer;

import de.mpdv.maintenanceManager.data.javaServer.PackageElement;
import de.mpdv.maintenanceManager.data.javaServer.PackageElement.PackageElementType;
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PackageMeta {

   private static final String PACKAGE_META_FILE = "packageMeta.xml";
   private final String name;
   private final Calendar creationDate;
   private final String description;
   private Calendar deploymentDate;
   private final List elements;
   private final String applicationName;


   public PackageMeta(String name, Calendar creationDate, Calendar deploymentDate, String description, List elements, String applicationName) {
      this.name = name;
      this.creationDate = creationDate;
      this.deploymentDate = deploymentDate;
      this.description = description;
      this.elements = elements;
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

   public List getElements() {
      return this.elements;
   }

   public String getApplicationName() {
      return this.applicationName;
   }

   public String toString() {
      return "PackageMeta [name=" + this.name + ", creationDate=" + this.creationDate + ", description=" + this.description + ", deploymentDate=" + this.deploymentDate + ", elements=" + this.elements + ", applicationName=" + this.applicationName + "]";
   }

   public static PackageMeta loadPackageMeta(String packagePath) throws ParserConfigurationException, SAXException, FileNotFoundException, IOException, ParseException {
      DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = fact.newDocumentBuilder();
      FileInputStream inStr = null;

      PackageMeta var43;
      try {
         inStr = new FileInputStream(new File(packagePath, "packageMeta.xml"));
         Document document = builder.parse(new InputSource(inStr));
         NodeList metaRoot = document.getElementsByTagName("packageMeta");
         if(metaRoot.getLength() != 1) {
            throw new IllegalArgumentException("The tag packageMeta is not contained, or contained more than once");
         }

         Node rootNode = metaRoot.item(0);
         String name = null;
         Calendar creationDate = null;
         String description = null;
         String appName = null;
         Calendar deploymentDate = null;
         LinkedList elemList = new LinkedList();
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
               String attribs;
               if(nodeName.equals("creationDate")) {
                  attribs = e.getTextContent();

                  try {
                     creationDate = DateTimeUtil.xsdStringToCalendarUtc(attribs);
                  } catch (Exception var41) {
                     throw new IllegalArgumentException("Could not parse creation date to calender: " + attribs);
                  }
               } else if(nodeName.equals("description")) {
                  description = e.getTextContent();
               } else if(nodeName.equals("deploymentDate")) {
                  attribs = e.getTextContent();

                  try {
                     deploymentDate = DateTimeUtil.xsdStringToCalendarUtc(attribs);
                  } catch (Exception var40) {
                     throw new IllegalArgumentException("Could not parse deployment date to calender: " + attribs);
                  }
               } else if(nodeName.equals("packageElement")) {
                  NamedNodeMap var44 = e.getAttributes();
                  Node nameNode = var44.getNamedItem("name");
                  if(nameNode == null) {
                     throw new IllegalArgumentException("Found a package element with missing name attribute");
                  }

                  String elemName = nameNode.getTextContent();
                  Node typeNode = var44.getNamedItem("type");
                  if(typeNode == null) {
                     throw new IllegalArgumentException("Found a package element with missing type attribute");
                  }

                  String elemTypeStr = typeNode.getTextContent();
                  if(Util.stringNullOrEmpty(elemName)) {
                     throw new IllegalArgumentException("Found a package element with missing name attribute");
                  }

                  if(Util.stringNullOrEmpty(elemTypeStr)) {
                     throw new IllegalArgumentException("Found a package element with missing type attribute");
                  }

                  PackageElementType elemType;
                  try {
                     elemType = PackageElementType.valueOf(elemTypeStr);
                  } catch (Exception var39) {
                     throw new IllegalArgumentException("Found a package element with invalid type attribute: " + elemTypeStr);
                  }

                  Node customerNode = var44.getNamedItem("customer");
                  String elemCustomer = null;
                  if(customerNode != null) {
                     elemCustomer = customerNode.getTextContent();
                  }

                  Node relativePathNode = var44.getNamedItem("path");
                  String relativePath = null;
                  if(relativePathNode != null) {
                     relativePath = relativePathNode.getTextContent();
                  }

                  if(relativePath == null) {
                     throw new IllegalArgumentException("Found a package element with missing path attribute");
                  }

                  String version = null;
                  Node versionNode = var44.getNamedItem("version");
                  if(versionNode != null) {
                     version = versionNode.getTextContent();
                  }

                  elemList.add(new PackageElement(elemName, elemType, elemCustomer, relativePath, version));
               }
            }
         }

         if(Util.stringNullOrEmpty(name)) {
            throw new IllegalArgumentException("The mandatory element name is missing in packageMeta.xml of " + packagePath);
         }

         if(Util.stringNullOrEmpty(appName)) {
            throw new IllegalArgumentException("The mandatory element applicationName is missing in packageMeta.xml of " + packagePath);
         }

         if(Util.stringNullOrEmpty(description)) {
            throw new IllegalArgumentException("The mandatory element description is missing in packageMeta.xml of " + packagePath);
         }

         if(creationDate == null) {
            throw new IllegalArgumentException("The mandatory element creationDate is missing in packageMeta.xml of " + packagePath);
         }

         var43 = new PackageMeta(name, creationDate, deploymentDate, description, elemList, appName);
      } finally {
         if(inStr != null) {
            try {
               inStr.close();
            } catch (Exception var38) {
               ;
            }
         }

      }

      return var43;
   }

   public void savePackageMeta(String packagePath) throws XMLStreamException, IOException {
      XMLOutputFactory2 xmlOutFact = (XMLOutputFactory2)XMLOutputFactory.newInstance();
      xmlOutFact.configureForXmlConformance();
      FileOutputStream os = null;
      OutputStreamWriter ow = null;
      XMLStreamWriter2 xmlWriter = null;

      try {
         os = new FileOutputStream(new File(packagePath, "packageMeta.xml"));
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
         xmlWriter.writeStartElement("applicationName");
         xmlWriter.writeCharacters(this.applicationName);
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

         int e = this.elements.size();

         for(int i = 0; i < e; ++i) {
            PackageElement currElem = (PackageElement)this.elements.get(i);
            xmlWriter.writeCharacters("\t");
            xmlWriter.writeStartElement("packageElement");
            xmlWriter.writeAttribute("name", currElem.getElementName());
            xmlWriter.writeAttribute("path", currElem.getElementPathRelative());
            xmlWriter.writeAttribute("type", currElem.getElementType().toString());
            if(!Util.stringNullOrEmpty(currElem.getCustomerName())) {
               xmlWriter.writeAttribute("customer", currElem.getCustomerName());
            }

            if(!Util.stringNullOrEmpty(currElem.getVersion())) {
               xmlWriter.writeAttribute("version", currElem.getVersion());
            }

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
            } catch (XMLStreamException var20) {
               ;
            }
         }

         if(ow != null) {
            try {
               ow.close();
            } catch (IOException var19) {
               ;
            }
         }

         if(os != null) {
            try {
               os.close();
            } catch (IOException var18) {
               ;
            }
         }

      }

   }
}