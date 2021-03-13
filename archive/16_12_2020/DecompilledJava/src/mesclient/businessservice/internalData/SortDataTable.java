package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.Util;
import de.mpdv.mesclient.businessservice.internalData.DataTableColumnMeta;
import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.IRowIterator;
import de.mpdv.mesclient.businessservice.internalData.RowIterator;
import de.mpdv.mesclient.businessservice.internalData.DataTypes.WebServiceType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.collections.list.TreeList;

class SortDataTable implements IDataTable {

   private final IDataTable originalTab;
   private final List sortedMapping;
   private String dataTableId;


   SortDataTable(IDataTable originalTab, List sortedMapping) {
      if(originalTab == null) {
         throw new NullPointerException("Parameter originalTab is null");
      } else if(sortedMapping == null) {
         throw new NullPointerException("Parameter sortedMapping is null");
      } else {
         this.originalTab = originalTab;
         this.sortedMapping = this.createMappingList(sortedMapping);
         this.dataTableId = "";
      }
   }

   SortDataTable(IDataTable originalTab, List sortedMapping, String dataTableId) {
      if(originalTab == null) {
         throw new NullPointerException("Parameter originalTab is null");
      } else if(sortedMapping == null) {
         throw new NullPointerException("Parameter sortedMapping is null");
      } else {
         this.originalTab = originalTab;
         this.sortedMapping = this.createMappingList(sortedMapping);
         this.dataTableId = dataTableId;
      }
   }

   public void addAllRows(IDataTable table) {
      if(table == null) {
         throw new NullPointerException("Parameter table is null");
      } else {
         this.originalTab.addAllRows(table);
         int addCount = table.getRowCount();
         int addIdx = this.sortedMapping.size();

         for(int i = 0; i < addCount; ++i) {
            this.sortedMapping.add(Integer.valueOf(addIdx));
            ++addIdx;
         }

      }
   }

   public int addEmptyColumn(String colName, WebServiceType wsType) {
      if(wsType == null) {
         throw new NullPointerException("Parameter wsType is null");
      } else if(Util.stringNullOrEmpty(colName)) {
         throw new NullPointerException("Parameter colName is null or empty");
      } else {
         return this.originalTab.addEmptyColumn(colName, wsType);
      }
   }

   public void addEmptyColumnAt(String colName, WebServiceType wsType, int idx) {
      if(wsType == null) {
         throw new NullPointerException("Parameter wsType is null");
      } else if(Util.stringNullOrEmpty(colName)) {
         throw new NullPointerException("Parameter colName is null or empty");
      } else {
         this.originalTab.addEmptyColumnAt(colName, wsType, idx);
      }
   }

   public int addEmptyRow() {
      int addIdx = this.originalTab.addEmptyRow();
      this.sortedMapping.add(Integer.valueOf(addIdx));
      return addIdx;
   }

   public void addEmptyRow(int rowIdx) {
      this.checkRowRange(rowIdx);
      this.originalTab.addEmptyRow();
      int addIdx = this.sortedMapping.size();
      this.sortedMapping.add(rowIdx, Integer.valueOf(addIdx));
   }

   public int copyRow(int srcIdx) {
      int addIdx = this.originalTab.copyRow(((Integer)this.sortedMapping.get(srcIdx)).intValue());
      this.sortedMapping.add(Integer.valueOf(addIdx));
      return addIdx;
   }

   public void copyRow(int srcIdx, int destIdx) {
      this.checkRowRange(srcIdx);
      this.checkRowRange(destIdx);
      this.originalTab.copyRow(((Integer)this.sortedMapping.get(srcIdx)).intValue());
      int addIdx = this.sortedMapping.size();
      this.sortedMapping.add(destIdx, Integer.valueOf(addIdx));
   }

   public List dumpData(boolean fixedSized) {
      List unsortedDump = this.originalTab.dumpData(fixedSized);
      ArrayList sortedDump = new ArrayList();
      int rowCount = this.sortedMapping.size();

      for(int i = 0; i < rowCount; ++i) {
         sortedDump.add(unsortedDump.get(((Integer)this.sortedMapping.get(i)).intValue()));
      }

      return sortedDump;
   }

   public List dumpMeta(boolean fixedSized) {
      return this.originalTab.dumpMeta(fixedSized);
   }

   public Object getCellValue(int rowIdx, int colIdx, Class type) {
      if(type == null) {
         throw new NullPointerException("Parameter type is null");
      } else {
         this.checkRowRange(rowIdx);
         return this.originalTab.getCellValue(((Integer)this.sortedMapping.get(rowIdx)).intValue(), colIdx, type);
      }
   }

   public Object getCellValue(int rowIdx, String colName, Class type) {
      if(type == null) {
         throw new NullPointerException("Parameter type is null");
      } else if(Util.stringNullOrEmpty(colName)) {
         throw new NullPointerException("Parameter colName is null or empty");
      } else {
         this.checkRowRange(rowIdx);
         return this.originalTab.getCellValue(((Integer)this.sortedMapping.get(rowIdx)).intValue(), colName, type);
      }
   }

   public int getColIdx(String colName) {
      if(Util.stringNullOrEmpty(colName)) {
         throw new NullPointerException("Parameter colName is null or empty");
      } else {
         return this.originalTab.getColIdx(colName);
      }
   }

   public int probeColIdx(String colName) {
      if(Util.stringNullOrEmpty(colName)) {
         throw new NullPointerException("Parameter colName is null or empty");
      } else {
         return this.originalTab.probeColIdx(colName);
      }
   }

   public String getColName(int colIdx) {
      return this.originalTab.getColName(colIdx);
   }

   public WebServiceType getColType(int colIdx) {
      return this.originalTab.getColType(colIdx);
   }

   public WebServiceType getColType(String colName) {
      if(Util.stringNullOrEmpty(colName)) {
         throw new NullPointerException("Parameter colName is null or empty");
      } else {
         return this.originalTab.getColType(colName);
      }
   }

   public int getColumnCount() {
      return this.originalTab.getColumnCount();
   }

   public int getRowCount() {
      return this.sortedMapping.size();
   }

   public IRowIterator getRowIterator() {
      return new RowIterator(this, -1);
   }

   public IRowIterator getRowIterator(int rowIdx) {
      this.checkRowRange(rowIdx);
      return new RowIterator(this, rowIdx);
   }

   public void removeColumn(int colIdx) {
      this.originalTab.removeColumn(colIdx);
   }

   public void removeColumn(String colName) {
      this.originalTab.removeColumn(colName);
   }

   public void probeRemoveColumn(String colName) {
      this.originalTab.probeRemoveColumn(colName);
   }

   public void removeRow(int rowIdx) {
      this.checkRowRange(rowIdx);
      this.sortedMapping.remove(rowIdx);
   }

   public void setCellValue(int rowIdx, int colIdx, Class type, Object value) {
      if(type == null) {
         throw new NullPointerException("Parameter type is null");
      } else {
         this.checkRowRange(rowIdx);
         this.originalTab.setCellValue(((Integer)this.sortedMapping.get(rowIdx)).intValue(), colIdx, type, value);
      }
   }

   public void setCellValue(int rowIdx, String colName, Class type, Object value) {
      if(type == null) {
         throw new NullPointerException("Parameter type is null");
      } else if(Util.stringNullOrEmpty(colName)) {
         throw new NullPointerException("Parameter colName is null or empty");
      } else {
         this.checkRowRange(rowIdx);
         this.originalTab.setCellValue(((Integer)this.sortedMapping.get(rowIdx)).intValue(), colName, type, value);
      }
   }

   private List createMappingList(List mappingList) {
      if(mappingList == null) {
         throw new NullPointerException("Parameter mappingList is null");
      } else {
         return new TreeList(mappingList);
      }
   }

   private void checkRowRange(int rowIdx) {
      if(rowIdx < 0 || rowIdx >= this.sortedMapping.size()) {
         throw new IndexOutOfBoundsException("Illegal Index: " + rowIdx + ". Index must be between " + 0 + " and " + (this.sortedMapping.size() - 1));
      }
   }

   public void renameColumn(String colname, String newColName) {
      if(Util.stringNullOrEmpty(colname)) {
         throw new IllegalArgumentException("Parameter colname is null or empty");
      } else if(Util.stringNullOrEmpty(newColName)) {
         throw new IllegalArgumentException("Parameter newColName is null or empty");
      } else {
         this.originalTab.renameColumn(colname, newColName);
      }
   }

   public void probeRenameColumn(String colname, String newColName) {
      if(Util.stringNullOrEmpty(colname)) {
         throw new IllegalArgumentException("Parameter colname is null or empty");
      } else if(Util.stringNullOrEmpty(newColName)) {
         throw new IllegalArgumentException("Parameter newColName is null or empty");
      } else {
         this.originalTab.probeRenameColumn(colname, newColName);
      }
   }

   public void renameColumn(int colIdx, String newColName) {
      if(Util.stringNullOrEmpty(newColName)) {
         throw new IllegalArgumentException("Parameter newColName is null or empty");
      } else {
         this.originalTab.renameColumn(colIdx, newColName);
      }
   }

   public String toTabularString() {
      String newLineDelimiter = "\r\n";
      String colDelimiter = "|";
      StringBuilder builder = new StringBuilder();
      int colCount = this.getColumnCount();
      List colMetaList = this.dumpMeta(true);
      builder.append("|");

      for(int dataList = 0; dataList < colCount; ++dataList) {
         builder.append(((DataTableColumnMeta)colMetaList.get(dataList)).toTabularString());
      }

      List var15 = this.dumpData(true);
      int rowCount = this.getRowCount();

      for(int rowIdx = 0; rowIdx < rowCount; ++rowIdx) {
         builder.append("\r\n");
         builder.append("|");
         List row = (List)var15.get(rowIdx);

         for(int colIdx = 0; colIdx < colCount; ++colIdx) {
            Object value = row.get(colIdx);
            if(value instanceof Calendar) {
               builder.append(DateTimeUtil.calendarToPrintString((Calendar)value));
            } else if(value != null && value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive()) {
               if(value.getClass().getComponentType().equals(Calendar.class)) {
                  Calendar[] calArr = (Calendar[])((Calendar[])value);
                  int len = calArr.length;
                  builder.append("[");

                  for(int k = 0; k < len; ++k) {
                     if(k > 0) {
                        builder.append(", ");
                     }

                     builder.append(DateTimeUtil.calendarToPrintString(calArr[k]));
                  }

                  builder.append("]");
               } else {
                  builder.append(Arrays.deepToString((Object[])((Object[])value)));
               }
            } else {
               builder.append(value);
            }

            builder.append("|");
         }
      }

      return builder.toString();
   }

   public String getDataTableId() {
      return this.dataTableId;
   }

   public int hashCode() {
      boolean prime = true;
      byte result = 1;
      int result1 = 31 * result + (this.originalTab == null?0:this.originalTab.hashCode());
      result1 = 31 * result1 + (this.sortedMapping == null?0:this.sortedMapping.hashCode());
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
         SortDataTable other = (SortDataTable)obj;
         if(this.originalTab == null) {
            if(other.originalTab != null) {
               return false;
            }
         } else if(!this.originalTab.equals(other.originalTab)) {
            return false;
         }

         if(this.sortedMapping == null) {
            if(other.sortedMapping != null) {
               return false;
            }
         } else if(!this.sortedMapping.equals(other.sortedMapping)) {
            return false;
         }

         return true;
      }
   }

   public void setDataTableId(String dataTableId) {
      if(dataTableId == null) {
         this.dataTableId = "";
      } else {
         this.dataTableId = dataTableId;
      }

   }
}