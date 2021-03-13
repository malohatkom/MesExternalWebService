package de.mpdv.maintenanceManager.data;

import java.io.FileWriter;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.XMLStreamWriter2;

public class XmlWriter {

   private final XMLStreamWriter2 writer;
   private final FileWriter fileWriter;


   public XmlWriter(FileWriter fileWriter, XMLStreamWriter2 writer) {
      if(fileWriter == null) {
         throw new IllegalArgumentException("Parameter fileWriter is null");
      } else if(writer == null) {
         throw new IllegalArgumentException("Parameter writer is null");
      } else {
         this.fileWriter = fileWriter;
         this.writer = writer;
      }
   }

   public XMLStreamWriter2 getWriter() {
      return this.writer;
   }

   public void closeCompletely() throws XMLStreamException, IOException {
      this.writer.close();
      this.fileWriter.close();
   }
}