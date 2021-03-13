package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.ArrayWrapper;
import de.mpdv.mesclient.businessservice.internalData.DataRow;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.collections.list.TreeList;

class TableDataManager {

   private int colCount;
   private final List rows;


   TableDataManager(int colCount, int rowCount) {
      this.colCount = colCount;
      this.rows = this.createRowList(rowCount);
   }

   private List createRowList(int rowCount) {
      TreeList list = new TreeList();
      return list;
   }

   void addRow(List row) {
      if(row.size() != this.colCount) {
         throw new IllegalArgumentException("The row has a column count of " + row.size() + " but valid is " + this.colCount);
      } else {
         this.rows.add(new DataRow(row));
      }
   }

   void addRow(int destIdx, List row) {
      if(row.size() != this.colCount) {
         throw new IllegalArgumentException("The row has a column count of " + row.size() + " but valid is " + this.colCount);
      } else {
         this.rows.add(destIdx, new DataRow(row));
      }
   }

   int copyRow(int rowIdx) {
      this.addRow(((DataRow)this.rows.get(rowIdx)).getCells());
      return this.rows.size() - 1;
   }

   void copyRow(int rowIdx, int destIdx) {
      this.addRow(destIdx, ((DataRow)this.rows.get(rowIdx)).getCells());
   }

   int addEmptyRow() {
      this.rows.add(new DataRow(this.colCount));
      return this.rows.size() - 1;
   }

   void addEmptyRow(int rowIdx) {
      this.rows.add(rowIdx, new DataRow(this.colCount));
   }

   void addEmptyColumn() {
      int rowCounter = this.rowCount();
      ListIterator iter = this.rows.listIterator();

      for(int ii = 0; ii < rowCounter; ++ii) {
         DataRow row = (DataRow)iter.next();
         row.addCell((Object)null);
      }

      ++this.colCount;
   }

   void addEmptyColumnAt(int idx) {
      int rowCounter = this.rowCount();
      ListIterator iter = this.rows.listIterator();

      for(int ii = 0; ii < rowCounter; ++ii) {
         DataRow row = (DataRow)iter.next();
         row.addCellAt((Object)null, idx);
      }

      ++this.colCount;
   }

   void removeColumn(int colIdx) {
      int rowCounter = this.rowCount();
      ListIterator iter = this.rows.listIterator();

      for(int ii = 0; ii < rowCounter; ++ii) {
         DataRow row = (DataRow)iter.next();
         row.removeCell(colIdx);
      }

      --this.colCount;
   }

   void setCellValue(int rowIdx, int colIdx, Object content) {
      DataRow row = (DataRow)this.rows.get(rowIdx);
      row.setCellValue(colIdx, content);
   }

   Object getCellValue(int rowIdx, int colIdx) {
      DataRow row = (DataRow)this.rows.get(rowIdx);
      return row.getCellValue(colIdx);
   }

   void removeRow(int rowIdx) {
      this.rows.remove(rowIdx);
   }

   void checkRowRange(int rowIdx, int startIdx, int endIdx) {
      if(rowIdx < startIdx || rowIdx >= endIdx) {
         throw new IndexOutOfBoundsException("Illegal Index: " + rowIdx + ". Index must be between " + startIdx + " and " + endIdx);
      }
   }

   void ensureCapacity(int minimumCapacity) {}

   int rowCount() {
      return this.rows.size();
   }

   List dump(boolean fixedSized) {
      int rowCount = this.rowCount();
      ArrayList allRowsDump;
      if(fixedSized) {
         allRowsDump = new ArrayList(rowCount);
      } else {
         allRowsDump = new ArrayList();
      }

      for(int rowIdx = 0; rowIdx < rowCount; ++rowIdx) {
         Object[] rowDump = new Object[this.colCount];
         DataRow row = (DataRow)this.rows.get(rowIdx);

         for(int rowList = 0; rowList < this.colCount; ++rowList) {
            rowDump[rowList] = row.getCellValue(rowList);
         }

         ArrayWrapper var8 = new ArrayWrapper(rowDump);
         if(fixedSized) {
            allRowsDump.add(var8);
         } else {
            allRowsDump.add(new ArrayList(var8));
         }
      }

      return allRowsDump;
   }

   String toTabularString() {
      String rowDelimiter = "\r\n";
      StringBuilder builder = new StringBuilder();
      int rowCount = this.rowCount();

      for(int ii = 0; ii < rowCount; ++ii) {
         if(ii != 0) {
            builder.append("\r\n");
         }

         builder.append(((DataRow)this.rows.get(ii)).toTabularString());
      }

      return builder.toString();
   }

   public int hashCode() {
      boolean prime = true;
      byte result = 1;
      int result1 = 31 * result + this.colCount;
      result1 = 31 * result1 + (this.rows == null?0:this.rows.hashCode());
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
         TableDataManager other = (TableDataManager)obj;
         if(this.colCount != other.colCount) {
            return false;
         } else {
            if(this.rows == null) {
               if(other.rows != null) {
                  return false;
               }
            } else if(!this.rows.equals(other.rows)) {
               return false;
            }

            return true;
         }
      }
   }
}