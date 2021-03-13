package de.mpdv.maintenanceManager.util;

import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLStreamWriter2;

public class XmlUtil {

   public static final String XML_ENCODING = "UTF-8";


   public static void addLinebreak(XMLStreamWriter2 writer) throws XMLStreamException {
      if(writer == null) {
         throw new NullPointerException("Parameter writer is null");
      } else {
         writer.writeCharacters("\n");
      }
   }

   public static void addIndentation(XMLStreamWriter2 writer, int tabCount) throws XMLStreamException, IllegalArgumentException {
      if(writer == null) {
         throw new NullPointerException("Parameter writer is null");
      } else {
         for(int i = 0; i < tabCount; ++i) {
            writer.writeCharacters("\t");
         }

      }
   }
}