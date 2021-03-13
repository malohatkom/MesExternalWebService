package de.mpdv.mesclient.businessservice.internalData;


public interface IRowIterator {

   boolean next();

   boolean previous();

   int currentRowPos();

   Object getCellValue(String var1, Class var2);

   Object getCellValue(int var1, Class var2);

   void setCellValue(String var1, Class var2, Object var3);

   void setCellValue(int var1, Class var2, Object var3);

   void addRow();

   void removeRow();
}