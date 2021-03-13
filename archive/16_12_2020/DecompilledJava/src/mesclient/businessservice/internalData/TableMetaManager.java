package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.maintenanceManager.util.Util;
import de.mpdv.mesclient.businessservice.internalData.ArrayWrapper;
import de.mpdv.mesclient.businessservice.internalData.DataTableColumnMeta;
import de.mpdv.mesclient.businessservice.internalData.MappingManager;
import de.mpdv.mesclient.businessservice.internalData.DataTypes.WebServiceType;
import de.mpdv.mesclient.businessservice.internalData.MutableInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.list.TreeList;

class TableMetaManager {
   
   private final Map columnNameToIdx = new LinkedHashMap();
   private final List columnMetaList;
   private final MappingManager mappingMgr = MappingManager.getInstance();


   TableMetaManager(List tableHeadInfo) {
      int colCount = tableHeadInfo != null?tableHeadInfo.size():0;
      if(tableHeadInfo != null && colCount != 0) {
         this.columnMetaList = this.createColMetaList(tableHeadInfo);
         this.checkColumnMetaInfo();
      } else {
         throw new IllegalArgumentException("No columns specified");
      }
   }

   private void checkColumnMetaInfo() {
      int colCount = this.columnMetaList.size();
      Iterator iter = this.columnMetaList.iterator();

      for(int ii = 0; ii < colCount; ++ii) {
         DataTableColumnMeta meta = (DataTableColumnMeta)iter.next();
         if(Util.stringNullOrEmpty(meta.getColName())) {
            throw new IllegalArgumentException("Column name is null or empty at position " + ii);
         }

         if(meta.getColWsType() == null) {
            throw new NullPointerException("Web service enumeration data type is null at position " + ii);
         }

         this.checkForDuplicateColName(meta.getColName());
         this.columnNameToIdx.put(meta.getColName(), new MutableInteger(ii));
      }

   }

   private List createColMetaList(List tableHeadInfo) {
      return new TreeList(tableHeadInfo);
   }

   int addColumn(DataTableColumnMeta column) {
      if(column == null) {
         throw new NullPointerException("Column is null");
      } else {
         this.checkForDuplicateColName(column.getColName());
         DataTableColumnMeta colMeta = new DataTableColumnMeta(column);
         int position = this.columnMetaList.size();
         this.columnMetaList.add(colMeta);
         this.columnNameToIdx.put(colMeta.getColName(), new MutableInteger(position));
         return position;
      }
   }

   void addColumnAt(DataTableColumnMeta column, int idx) {
      if(column == null) {
         throw new NullPointerException("Column is null");
      } else {
         this.checkForDuplicateColName(column.getColName());
         DataTableColumnMeta colMeta = new DataTableColumnMeta(column);
         this.columnMetaList.add(idx, colMeta);
         Iterator it = this.columnNameToIdx.keySet().iterator();

         while(it.hasNext()) {
            String currKey = (String)it.next();
            MutableInteger currIdx = (MutableInteger)this.columnNameToIdx.get(currKey);
            if(currIdx.intValue >= idx) {
               this.columnNameToIdx.put(currKey, new MutableInteger(currIdx.intValue + 1));
            }
         }

         this.columnNameToIdx.put(colMeta.getColName(), new MutableInteger(idx));
      }
   }

   int colCount() {
      return this.columnMetaList.size();
   }

   void removeColumn(int colIdx) {
      String colName = ((DataTableColumnMeta)this.columnMetaList.remove(colIdx)).getColName();
      this.columnNameToIdx.remove(colName);
      Iterator it = this.columnNameToIdx.keySet().iterator();

      while(it.hasNext()) {
         String currColName = (String)it.next();
         MutableInteger currIdx = (MutableInteger)this.columnNameToIdx.get(currColName);
         if(currIdx.intValue > colIdx) {
            --currIdx.intValue;
         }
      }

   }

   void checkColRange(int colIdx) {
      if(colIdx < 0 || colIdx >= this.columnMetaList.size()) {
         throw new IndexOutOfBoundsException("Index must be between: 0 and " + this.columnMetaList.size());
      }
   }

   int colIdx(String colName) {
      MutableInteger position = (MutableInteger)this.columnNameToIdx.get(colName);
      if(position == null) {
         throw new IllegalArgumentException("Unknown column name specified: " + colName);
      } else {
         return position.intValue;
      }
   }

   int probeColIdx(String colName) {
      MutableInteger position = (MutableInteger)this.columnNameToIdx.get(colName);
      return position == null?-1:position.intValue;
   }

   private void checkForDuplicateColName(String colName) {
      if(Util.stringNullOrEmpty(colName)) {
         throw new IllegalArgumentException("Column name is NULL or empty");
      } else if(this.columnNameToIdx.containsKey(colName)) {
         throw new IllegalArgumentException("A column with the specified name already exists: " + colName);
      }
   }

   void checkColName(String colName) {
      if(Util.stringNullOrEmpty(colName)) {
         throw new IllegalArgumentException("Column name is NULL or empty");
      }
   }

   Class javaType(int colIdx) {
      DataTableColumnMeta meta = (DataTableColumnMeta)this.columnMetaList.get(colIdx);
      this.checkMetaForNull(colIdx, meta);
      WebServiceType dataType = meta.getColWsType();
      if(dataType == null) {
         throw new IllegalStateException("No data type specified in column meta for index: " + colIdx + " that is column: " + meta.getColName());
      } else {
         return this.mappingMgr.wsTypeToClass(dataType);
      }
   }

   void checkType(Class type, int colIdx) {
      Class colType = this.javaType(colIdx);
      if(type != colType) {
         throw new IllegalArgumentException("Illegal type detected: " + type + ". It should be: " + colType + ". Column index is: " + colIdx + ". Column name is: " + this.colName(colIdx));
      }
   }

   String colName(int colIdx) {
      DataTableColumnMeta meta = (DataTableColumnMeta)this.columnMetaList.get(colIdx);
      this.checkMetaForNull(colIdx, meta);
      String colName = meta.getColName();
      if(Util.stringNullOrEmpty(colName)) {
         throw new IllegalStateException("Column name for valid index is null or empty");
      } else {
         return colName;
      }
   }

   WebServiceType wsDataType(int colIdx) {
      DataTableColumnMeta meta = (DataTableColumnMeta)this.columnMetaList.get(colIdx);
      this.checkMetaForNull(colIdx, meta);
      WebServiceType dataType = meta.getColWsType();
      if(dataType == null) {
         throw new IllegalStateException("No data type specified in column meta for index: " + colIdx + " that is column: " + meta.getColName());
      } else {
         return dataType;
      }
   }

   List dump(boolean fixedSized) {
      if(!fixedSized) {
         return new ArrayList(this.columnMetaList);
      } else {
         int size = this.columnMetaList.size();
         DataTableColumnMeta[] array = new DataTableColumnMeta[size];

         for(int ii = 0; ii < size; ++ii) {
            array[ii] = (DataTableColumnMeta)this.columnMetaList.get(ii);
         }

         return new ArrayWrapper(array);
      }
   }

   private void checkMetaForNull(int colIdx, DataTableColumnMeta meta) {
      if(meta == null) {
         throw new IllegalStateException("No meta found at valid index: " + colIdx);
      }
   }

   void renameColumn(int colIdx, String newColName) {
      this.checkColRange(colIdx);
      this.checkColName(newColName);
      this.checkForDuplicateColName(newColName);
      DataTableColumnMeta colMeta = (DataTableColumnMeta)this.columnMetaList.get(colIdx);
      String oldName = colMeta.getColName();
      colMeta.setColName(newColName);
      this.columnNameToIdx.remove(oldName);
      this.columnNameToIdx.put(newColName, new MutableInteger(colIdx));
   }

   String toTabularString() {
      int colCount = this.columnMetaList.size();
      StringBuilder result = new StringBuilder();
      String colDelimiter = "|";
      result.append("|");

      for(int ii = 0; ii < colCount; ++ii) {
         result.append(((DataTableColumnMeta)this.columnMetaList.get(ii)).toTabularString());
      }

      return result.toString();
   }

   public int hashCode() {
      boolean prime = true;
      byte result = 1;
      int result1 = 31 * result + (this.columnMetaList == null?0:this.columnMetaList.hashCode());
      result1 = 31 * result1 + (this.columnNameToIdx == null?0:this.columnNameToIdx.hashCode());
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
         TableMetaManager other = (TableMetaManager)obj;
         if(this.columnMetaList == null) {
            if(other.columnMetaList != null) {
               return false;
            }
         } else if(!this.columnMetaList.equals(other.columnMetaList)) {
            return false;
         }

         if(this.columnNameToIdx == null) {
            if(other.columnNameToIdx != null) {
               return false;
            }
         } else if(!this.columnNameToIdx.equals(other.columnNameToIdx)) {
            return false;
         }

         return true;
      }
   }
}