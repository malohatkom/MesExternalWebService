package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.IRowIterator;
import de.mpdv.mesclient.businessservice.internalData.DataTypes.WebServiceType;
import java.util.List;

public interface IDataTable {

   IRowIterator getRowIterator();

   IRowIterator getRowIterator(int var1);

   int getRowCount();

   int getColumnCount();

   int addEmptyColumn(String var1, WebServiceType var2);

   void addEmptyColumnAt(String var1, WebServiceType var2, int var3);

   int addEmptyRow();

   void addEmptyRow(int var1);

   int copyRow(int var1);

   void copyRow(int var1, int var2);

   void removeColumn(int var1);

   void removeColumn(String var1);

   void probeRemoveColumn(String var1);

   void removeRow(int var1);

   void addAllRows(IDataTable var1);

   Object getCellValue(int var1, int var2, Class var3);

   Object getCellValue(int var1, String var2, Class var3);

   void setCellValue(int var1, int var2, Class var3, Object var4);

   void setCellValue(int var1, String var2, Class var3, Object var4);

   List dumpData(boolean var1);

   List dumpMeta(boolean var1);

   String getColName(int var1);

   int getColIdx(String var1);

   int probeColIdx(String var1);

   WebServiceType getColType(int var1);

   WebServiceType getColType(String var1);

   void renameColumn(String var1, String var2);

   void probeRenameColumn(String var1, String var2);

   void renameColumn(int var1, String var2);

   String getDataTableId();

   void setDataTableId(String var1);

   String toTabularString();
}