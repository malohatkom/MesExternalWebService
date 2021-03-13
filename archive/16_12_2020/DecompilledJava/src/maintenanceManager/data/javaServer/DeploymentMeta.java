package de.mpdv.maintenanceManager.data.javaServer;

import de.mpdv.maintenanceManager.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

public class DeploymentMeta {

   private static final String DEPLOYMENT_META_FILE = "deploymentMeta.xml";
   private final String warName;
   private final String confContextName;


   public DeploymentMeta(String warName, String confContextName) {
      this.warName = warName;
      this.confContextName = confContextName;
   }

   public String getWarName() {
      return this.warName;
   }

   public String getConfContextName() {
      return this.confContextName;
   }

   public String toString() {
      return "DeploymentMeta [confContextName=" + this.confContextName + ", warName=" + this.warName + "]";
   }

   public static DeploymentMeta loadDeploymentMeta(File metaPath) throws ParserConfigurationException, SAXException, IOException {
      DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = fact.newDocumentBuilder();
      FileInputStream inStr = null;

      DeploymentMeta var21;
      try {
         inStr = new FileInputStream(new File(metaPath, "deploymentMeta.xml"));
         Document document = builder.parse(new InputSource(inStr));
         NodeList metaRoot = document.getElementsByTagName("deploymentMeta");
         if(metaRoot.getLength() != 1) {
            throw new IllegalArgumentException("The tag deploymentMeta is not contained, or contained more than once");
         }

         Node rootNode = metaRoot.item(0);
         String warName = null;
         String confContextName = null;
         NodeList contents = rootNode.getChildNodes();
         int count = contents.getLength();

         for(int i = 0; i < count; ++i) {
            Node e = contents.item(i);
            String nodeName = e.getNodeName();
            if(nodeName.equals("warName")) {
               warName = e.getTextContent();
            } else if(nodeName.equals("confContextName")) {
               confContextName = e.getTextContent();
            }
         }

         if(Util.stringNullOrEmpty(warName)) {
            throw new IllegalArgumentException("The mandatory element warName is missing in deploymentMeta.xml of " + metaPath.getAbsolutePath());
         }

         var21 = new DeploymentMeta(warName, confContextName);
      } finally {
         if(inStr != null) {
            try {
               inStr.close();
            } catch (Exception var19) {
               ;
            }
         }

      }

      return var21;
   }

   public void saveDeploymentMeta(File metaPath) throws XMLStreamException, IOException {
      XMLOutputFactory2 xmlOutFact = (XMLOutputFactory2)XMLOutputFactory.newInstance();
      xmlOutFact.configureForXmlConformance();
      FileOutputStream os = null;
      OutputStreamWriter ow = null;
      XMLStreamWriter2 xmlWriter = null;

      try {
         os = new FileOutputStream(new File(metaPath, "deploymentMeta.xml"));
         ow = new OutputStreamWriter(os, "UTF-8");
         xmlWriter = xmlOutFact.createXMLStreamWriter(ow, "UTF-8");
         xmlWriter.writeStartDocument("1.0");
         xmlWriter.writeCharacters("\n");
         xmlWriter.writeStartElement("deploymentMeta");
         xmlWriter.writeCharacters("\n");
         xmlWriter.writeCharacters("\t");
         xmlWriter.writeStartElement("warName");
         xmlWriter.writeCharacters(this.warName);
         xmlWriter.writeEndElement();
         xmlWriter.writeCharacters("\n");
         if(!Util.stringNullOrEmpty(this.confContextName)) {
            xmlWriter.writeCharacters("\t");
            xmlWriter.writeStartElement("confContextName");
            xmlWriter.writeCharacters(this.confContextName);
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
            } catch (XMLStreamException var18) {
               ;
            }
         }

         if(ow != null) {
            try {
               ow.close();
            } catch (IOException var17) {
               ;
            }
         }

         if(os != null) {
            try {
               os.close();
            } catch (IOException var16) {
               ;
            }
         }

      }

   }
}