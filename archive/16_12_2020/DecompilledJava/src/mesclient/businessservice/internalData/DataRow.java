package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.maintenanceManager.util.DateTimeUtil;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.collections.list.TreeList;

class DataRow {

   private final List cells;


   DataRow(int cellCount) {
      this.cells = this.createCellList(cellCount, (Object)null);
   }

   DataRow(List cellData) {
      this.cells = this.createCellList(cellData);
   }

   private List createCellList(int cellCount, Object fillValue) {
      TreeList list = new TreeList();

      for(int ii = 0; ii < cellCount; ++ii) {
         list.add(fillValue);
      }

      return list;
   }

   private List createCellList(List cellData) {
      TreeList list = new TreeList(cellData);
      return list;
   }

   void addCell(Object cellContent) {
      this.cells.add(cellContent);
   }

   void addCellAt(Object cellContent, int idx) {
      this.cells.add(idx, cellContent);
   }

   void removeCell(int colIdx) {
      this.cells.remove(colIdx);
   }

   Object getCellValue(int colIdx) {
      return this.cells.get(colIdx);
   }

   void setCellValue(int colIdx, Object cellValue) {
      this.cells.set(colIdx, cellValue);
   }

   void ensureCapacity(int minimumCapacity) {}

   String toTabularString() {
      StringBuilder builder = new StringBuilder();
      String colDelimiter = "|";
      int cellCount = this.cells.size();
      builder.append("|");

      for(int ii = 0; ii < cellCount; ++ii) {
         Object value = this.cells.get(ii);
         if(value instanceof Calendar) {
            builder.append(DateTimeUtil.calendarToPrintString((Calendar)value));
         } else if(value != null && value.getClass().isArray() && !value.getClass().equals(byte[].class) && !value.getClass().equals(byte[][].class)) {
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

      return builder.toString();
   }

   public int hashCode() {
      boolean prime = true;
      byte result = 1;
      int result1 = 31 * result + (this.cells == null?0:this.cells.hashCode());
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
         DataRow other = (DataRow)obj;
         if(this.cells == null) {
            if(other.cells != null) {
               return false;
            }
         } else if(!this.cells.equals(other.cells)) {
            return false;
         }

         return true;
      }
   }

   List getCells() {
      return this.cells;
   }
}