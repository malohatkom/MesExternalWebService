package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.maintenanceManager.util.Util;
import de.mpdv.mesclient.businessservice.internalData.DataTableColumnMeta;
import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.IRowIterator;
import de.mpdv.mesclient.businessservice.internalData.RowIterator;
import de.mpdv.mesclient.businessservice.internalData.TableDataManager;
import de.mpdv.mesclient.businessservice.internalData.TableMetaManager;
import de.mpdv.mesclient.businessservice.internalData.DataTypes.WebServiceType;
import java.util.List;

class DataTable implements IDataTable {

   private final TableMetaManager metaMgr;
   private final TableDataManager dataMgr;
   private String dataTableId;


   DataTable(List tableStructure, List tabularData) {
      this.checkFundamentalParams(tableStructure, tabularData);
      this.metaMgr = new TableMetaManager(tableStructure);
      int colCount = this.metaMgr.colCount();
      Class[] colDataTypes = this.createExpectedColumnTypes(colCount);
      int tabDataSize = tabularData.size();
      this.dataMgr = new TableDataManager(colCount, tabDataSize);

      for(int rowIdx = 0; rowIdx < tabDataSize; ++rowIdx) {
         List cells = (List)tabularData.get(rowIdx);
         this.checkRow(colCount, colDataTypes, rowIdx, cells);
         this.dataMgr.addRow(cells);
      }

      this.dataTableId = "";
   }

   DataTable(List tableStructure, List tabularData, String dataTableId) {
      this.checkFundamentalParams(tableStructure, tabularData);
      this.metaMgr = new TableMetaManager(tableStructure);
      int colCount = this.metaMgr.colCount();
      Class[] colDataTypes = this.createExpectedColumnTypes(colCount);
      int tabDataSize = tabularData.size();
      this.dataMgr = new TableDataManager(colCount, tabDataSize);

      for(int rowIdx = 0; rowIdx < tabDataSize; ++rowIdx) {
         List cells = (List)tabularData.get(rowIdx);
         this.checkRow(colCount, colDataTypes, rowIdx, cells);
         this.dataMgr.addRow(cells);
      }

      this.dataTableId = dataTableId;
   }

   private void checkRow(int colCount, Class[] colDataTypes, int rowIdx, List row) {
      if(row == null) {
         throw new NullPointerException("The row at position " + rowIdx + " is null");
      } else if(colDataTypes == null) {
         throw new NullPointerException("Parameter colDataTypes is null");
      } else if(row.size() != colCount) {
         throw new IllegalArgumentException("The size of the row at position " + rowIdx + " has a cell count of " + row.size() + " but expected was " + colCount);
      } else {
         for(int colIdx = 0; colIdx < colCount; ++colIdx) {
            Object value = row.get(colIdx);
            if(value != null && !colDataTypes[colIdx].isInstance(value)) {
               throw new IllegalArgumentException("The type in row " + rowIdx + " at column " + colIdx + " is not compatible with the column type. The expected type is " + colDataTypes[colIdx] + " but actual it is " + value.getClass());
            }
         }

      }
   }

   private Class[] createExpectedColumnTypes(int colCount) {
      Class[] colDataTypes = new Class[colCount];

      for(int ii = 0; ii < colCount; ++ii) {
         colDataTypes[ii] = this.metaMgr.javaType(ii);
      }

      return colDataTypes;
   }

   private void checkFundamentalParams(List tableStructure, List tabularData) {
      if(tableStructure == null) {
         throw new NullPointerException("Table meta structure is null");
      } else if(tabularData == null) {
         throw new NullPointerException("Tabular data is null");
      } else if(tableStructure.size() == 0) {
         throw new IllegalArgumentException("Table meta structure contains no columns");
      }
   }

   public void addAllRows(IDataTable table) {
      if(table == null) {
         throw new NullPointerException("Parameter table is null");
      } else {
         int tableColCount = table.getColumnCount();
         int tableRowCount = table.getRowCount();
         if(this.metaMgr.colCount() != tableColCount) {
            throw new IllegalArgumentException("Column count differs. Given data table: " + tableColCount + " Current table: " + this.metaMgr.colCount());
         } else {
            this.dataMgr.ensureCapacity(this.getRowCount() + tableRowCount);
            Class[] javaDataTypes = new Class[tableColCount];

            int rowIdx;
            for(rowIdx = 0; rowIdx < tableColCount; ++rowIdx) {
               if(!table.getColName(rowIdx).equals(this.getColName(rowIdx))) {
                  throw new IllegalArgumentException("Column names differ at column index " + rowIdx + " left name: " + this.getColName(rowIdx) + " right name: " + table.getColName(rowIdx));
               }

               if(!table.getColType(rowIdx).equals(this.getColType(rowIdx))) {
                  throw new IllegalArgumentException("Column types differ at column " + this.getColName(rowIdx) + " left type: " + this.getColType(rowIdx) + " right type: " + table.getColType(rowIdx));
               }

               javaDataTypes[rowIdx] = this.metaMgr.javaType(rowIdx);
            }

            for(rowIdx = 0; rowIdx < tableRowCount; ++rowIdx) {
               this.dataMgr.addEmptyRow();
               int position = this.getRowCount() - 1;

               for(int colIdx = 0; colIdx < tableColCount; ++colIdx) {
                  Object value = table.getCellValue(rowIdx, colIdx, javaDataTypes[colIdx]);
                  this.dataMgr.setCellValue(position, colIdx, value);
               }
            }

         }
      }
   }

   public int addEmptyColumn(String colName, WebServiceType wsType) {
      if(wsType == null) {
         throw new NullPointerException("Parameter wsType is null");
      } else if(Util.stringNullOrEmpty(colName)) {
         throw new NullPointerException("Parameter colName is null or empty");
      } else {
         int colIdx = this.metaMgr.addColumn(new DataTableColumnMeta(colName, wsType));
         this.dataMgr.addEmptyColumn();
         return colIdx;
      }
   }

   public void addEmptyColumnAt(String colName, WebServiceType wsType, int idx) {
      if(wsType == null) {
         throw new NullPointerException("Parameter wsType is null");
      } else if(Util.stringNullOrEmpty(colName)) {
         throw new NullPointerException("Parameter colName is null or empty");
      } else if(this.metaMgr.colCount() < idx) {
         throw new IllegalArgumentException("The Column index " + idx + " is too high for a datatable with " + this.metaMgr.colCount() + " columns");
      } else {
         this.metaMgr.addColumnAt(new DataTableColumnMeta(colName, wsType), idx);
         this.dataMgr.addEmptyColumnAt(idx);
      }
   }

   public int addEmptyRow() {
      return this.dataMgr.addEmptyRow();
   }

   public void addEmptyRow(int rowIdx) {
      this.dataMgr.checkRowRange(rowIdx, 0, this.getRowCount());
      this.dataMgr.addEmptyRow(rowIdx);
   }

   public int copyRow(int srcIdx) {
      return this.dataMgr.copyRow(srcIdx);
   }

   public void copyRow(int srcIdx, int destIdx) {
      if(destIdx == this.getRowCount()) {
         this.dataMgr.copyRow(srcIdx);
      } else {
         this.dataMgr.checkRowRange(destIdx, 0, this.getRowCount());
         this.dataMgr.copyRow(srcIdx, destIdx);
      }

   }

   public List dumpData(boolean fixedSized) {
      return this.dataMgr.dump(fixedSized);
   }

   public List dumpMeta(boolean fixedSized) {
      return this.metaMgr.dump(fixedSized);
   }

   public Object getCellValue(int rowIdx, int colIdx, Class type) {
      if(type == null) {
         throw new NullPointerException("Parameter type is null");
      } else {
         this.dataMgr.checkRowRange(rowIdx, 0, this.getRowCount());
         this.metaMgr.checkColRange(colIdx);
         this.metaMgr.checkType(type, colIdx);
         return type.cast(this.dataMgr.getCellValue(rowIdx, colIdx));
      }
   }

   public Object getCellValue(int rowIdx, String colName, Class type) {
      if(type == null) {
         throw new NullPointerException("Parameter type is null");
      } else {
         this.dataMgr.checkRowRange(rowIdx, 0, this.getRowCount());
         this.metaMgr.checkColName(colName);
         int colIdx = this.metaMgr.colIdx(colName);
         this.metaMgr.checkType(type, colIdx);
         return type.cast(this.dataMgr.getCellValue(rowIdx, colIdx));
      }
   }

   public int getColumnCount() {
      return this.metaMgr.colCount();
   }

   public int getRowCount() {
      return this.dataMgr.rowCount();
   }

   public IRowIterator getRowIterator() {
      return new RowIterator(this, -1);
   }

   public IRowIterator getRowIterator(int rowIdx) {
      this.dataMgr.checkRowRange(rowIdx, 0, this.getRowCount());
      return new RowIterator(this, rowIdx);
   }

   public void removeColumn(int colIdx) {
      this.metaMgr.checkColRange(colIdx);
      this.metaMgr.removeColumn(colIdx);
      this.dataMgr.removeColumn(colIdx);
   }

   public void removeColumn(String colName) {
      this.metaMgr.checkColName(colName);
      int colIdx = this.metaMgr.colIdx(colName);
      this.metaMgr.removeColumn(colIdx);
      this.dataMgr.removeColumn(colIdx);
   }

   public void probeRemoveColumn(String colName) {
      this.metaMgr.checkColName(colName);
      int colIdx = this.metaMgr.probeColIdx(colName);
      if(colIdx != -1) {
         this.metaMgr.removeColumn(colIdx);
         this.dataMgr.removeColumn(colIdx);
      }

   }

   public void removeRow(int rowIdx) {
      this.dataMgr.checkRowRange(rowIdx, 0, this.getRowCount());
      this.dataMgr.removeRow(rowIdx);
   }

   public void setCellValue(int rowIdx, int colIdx, Class type, Object value) {
      if(type == null) {
         throw new NullPointerException("Parameter type is null");
      } else {
         this.dataMgr.checkRowRange(rowIdx, 0, this.getRowCount());
         this.metaMgr.checkColRange(colIdx);
         this.metaMgr.checkType(type, colIdx);
         this.dataMgr.setCellValue(rowIdx, colIdx, value);
      }
   }

   public void setCellValue(int rowIdx, String colName, Class type, Object value) {
      if(type == null) {
         throw new NullPointerException("Parameter type is null");
      } else {
         this.dataMgr.checkRowRange(rowIdx, 0, this.getRowCount());
         this.metaMgr.checkColName(colName);
         int colIdx = this.metaMgr.colIdx(colName);
         this.metaMgr.checkType(type, colIdx);
         this.dataMgr.setCellValue(rowIdx, colIdx, value);
      }
   }

   public WebServiceType getColType(int colIdx) {
      this.metaMgr.checkColRange(colIdx);
      return this.metaMgr.wsDataType(colIdx);
   }

   public WebServiceType getColType(String colName) {
      this.metaMgr.checkColName(colName);
      int colIdx = this.metaMgr.colIdx(colName);
      return this.metaMgr.wsDataType(colIdx);
   }

   public String getColName(int colIdx) {
      this.metaMgr.checkColRange(colIdx);
      return this.metaMgr.colName(colIdx);
   }

   public int getColIdx(String colName) {
      this.metaMgr.checkColName(colName);
      return this.metaMgr.colIdx(colName);
   }

   public int probeColIdx(String colName) {
      return this.metaMgr.probeColIdx(colName);
   }

   public void renameColumn(String colname, String newColName) {
      this.metaMgr.checkColName(colname);
      this.metaMgr.checkColName(newColName);
      int colIdx = this.getColIdx(colname);
      this.renameColumn(colIdx, newColName);
   }

   public void probeRenameColumn(String colname, String newColName) {
      this.metaMgr.checkColName(colname);
      this.metaMgr.checkColName(newColName);
      int colIdx = this.probeColIdx(colname);
      if(colIdx != -1) {
         this.renameColumn(colIdx, newColName);
      }

   }

   public void renameColumn(int colIdx, String newColName) {
      this.metaMgr.checkColName(newColName);
      this.metaMgr.renameColumn(colIdx, newColName);
   }

   public String toTabularString() {
      String newLineDelimiter = "\r\n";
      StringBuilder builder = new StringBuilder();
      builder.append(this.metaMgr.toTabularString());
      if(this.getRowCount() != 0) {
         builder.append("\r\n");
         builder.append(this.dataMgr.toTabularString());
      }

      return builder.toString();
   }

   public String getDataTableId() {
      return this.dataTableId;
   }

   public int hashCode() {
      boolean prime = true;
      byte result = 1;
      int result1 = 31 * result + (this.dataMgr == null?0:this.dataMgr.hashCode());
      result1 = 31 * result1 + (this.metaMgr == null?0:this.metaMgr.hashCode());
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
         DataTable other = (DataTable)obj;
         if(this.dataMgr == null) {
            if(other.dataMgr != null) {
               return false;
            }
         } else if(!this.dataMgr.equals(other.dataMgr)) {
            return false;
         }

         if(this.metaMgr == null) {
            if(other.metaMgr != null) {
               return false;
            }
         } else if(!this.metaMgr.equals(other.metaMgr)) {
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