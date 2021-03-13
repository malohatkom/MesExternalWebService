package de.mpdv.maintenanceManager.data;

import de.mpdv.maintenanceManager.util.Util;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CopyRule {

   private final String name;
   private final String source;
   private final String target;
   private final String filter;


   public CopyRule(String name, String source, String target, String filter) {
      this.name = name;
      this.source = source;
      this.target = target;
      this.filter = filter;
   }

   public String getName() {
      return this.name;
   }

   public String getSource() {
      return this.source;
   }

   public String getTarget() {
      return this.target;
   }

   public String getFilter() {
      return this.filter;
   }

   public int hashCode() {
      boolean prime = true;
      byte result = 1;
      int result1 = 31 * result + (this.filter == null?0:this.filter.hashCode());
      result1 = 31 * result1 + (this.name == null?0:this.name.hashCode());
      result1 = 31 * result1 + (this.source == null?0:this.source.hashCode());
      result1 = 31 * result1 + (this.target == null?0:this.target.hashCode());
      return result1;
   }

   public boolean equals(Object obj) {
      if(this == obj) {
         return true;
      } else if(obj == null) {
         return false;
      } else if(this.getClass() != obj.getClass()) {
         return false;
      } else {
         CopyRule other = (CopyRule)obj;
         if(this.filter == null) {
            if(other.filter != null) {
               return false;
            }
         } else if(!this.filter.equals(other.filter)) {
            return false;
         }

         if(this.name == null) {
            if(other.name != null) {
               return false;
            }
         } else if(!this.name.equals(other.name)) {
            return false;
         }

         if(this.source == null) {
            if(other.source != null) {
               return false;
            }
         } else if(!this.source.equals(other.source)) {
            return false;
         }

         if(this.target == null) {
            if(other.target != null) {
               return false;
            }
         } else if(!this.target.equals(other.target)) {
            return false;
         }

         return true;
      }
   }

   public String toString() {
      return "CopyRule [name=" + this.name + ", source=" + this.source + ", target=" + this.target + ", filter=" + this.filter + "]";
   }

   public static List loadRulesFromFile(String sourceFile) throws ParserConfigurationException, SAXException, IOException {
      DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = fact.newDocumentBuilder();
      FileInputStream inStr = null;

      LinkedList ruleList;
      try {
         inStr = new FileInputStream(sourceFile);
         Document document = builder.parse(new InputSource(inStr));
         NodeList ruleNodeList = document.getElementsByTagName("CopyRule");
         if(ruleNodeList.getLength() != 0) {
            ruleList = new LinkedList();
            int ruleCount = ruleNodeList.getLength();

            for(int j = 0; j < ruleCount; ++j) {
               String e = null;
               String src = null;
               String target = null;
               String filter = null;
               Node ruleNode = ruleNodeList.item(j);
               NodeList contents = ruleNode.getChildNodes();
               int count = contents.getLength();

               for(int i = 0; i < count; ++i) {
                  Node currValueNode = contents.item(i);
                  String nodeName = currValueNode.getNodeName();
                  if(nodeName.equals("Name")) {
                     e = currValueNode.getTextContent();
                  } else if(nodeName.equals("Source")) {
                     src = currValueNode.getTextContent();
                     src = src.replaceAll("\\\\", "/");
                  } else if(nodeName.equals("Target")) {
                     target = currValueNode.getTextContent();
                     target = target.replaceAll("\\\\", "/");
                  } else if(nodeName.equals("Filter")) {
                     filter = currValueNode.getTextContent();
                  }
               }

               if(Util.stringNullOrEmpty(e)) {
                  throw new IllegalArgumentException("The mandatory element Name is missing in copy rules file " + sourceFile);
               }

               if(Util.stringNullOrEmpty(src)) {
                  throw new IllegalArgumentException("The mandatory element Source is missing in copy rules file " + sourceFile);
               }

               if(Util.stringNullOrEmpty(target)) {
                  throw new IllegalArgumentException("The mandatory element Target is missing in copy rules file " + sourceFile);
               }

               ruleList.add(new CopyRule(e, src, target, filter != null && !"".equals(filter) && !"none".equals(filter)?filter.replace("*", ""):null));
            }

            LinkedList var27 = ruleList;
            return var27;
         }

         ruleList = new LinkedList();
      } finally {
         if(inStr != null) {
            try {
               inStr.close();
            } catch (Exception var25) {
               ;
            }
         }

      }

      return ruleList;
   }
}