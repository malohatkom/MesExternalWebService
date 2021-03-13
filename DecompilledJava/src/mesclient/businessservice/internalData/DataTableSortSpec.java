package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.maintenanceManager.util.Util;
import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.DataTableSortSpec.OrderDirection;
import java.util.ArrayList;
import java.util.List;

public class DataTableSortSpec {


public enum OrderDirection {

   ASC("ASC", 0),
   DESC("DESC", 1);
   // $FF: synthetic field
   private static final OrderDirection[] $VALUES = new OrderDirection[]{ASC, DESC};


   private OrderDirection(String var1, int var2) {}

}    
    
    private boolean frozen = false;
   private final IDataTable dataTable;
   private final List idxColList = new ArrayList();
   private final List orderDirectionList = new ArrayList();


   public DataTableSortSpec(IDataTable dataTable) {
      this.dataTable = dataTable;
      if(this.dataTable == null) {
         throw new NullPointerException("Parameter dataTable is null");
      }
   }

   public DataTableSortSpec freeze() {
      this.frozen = true;
      return this;
   }

   public DataTableSortSpec column(String colName, OrderDirection direction) {
      if(this.frozen) {
         throw new IllegalStateException("Can not add column because DataTableSortSpec is frozen");
      } else if(direction == null) {
         throw new NullPointerException("Parameter direction is null");
      } else if(Util.stringNullOrEmpty(colName)) {
         throw new IllegalArgumentException("Parameter colName is null or empty");
      } else {
         this.idxColList.add(Integer.valueOf(this.dataTable.getColIdx(colName)));
         this.orderDirectionList.add(direction);
         return this;
      }
   }

   public DataTableSortSpec column(int colIdx, OrderDirection direction) {
      if(this.frozen) {
         throw new IllegalStateException("Can not add column because DataTableSortSpec is frozen");
      } else if(direction == null) {
         throw new NullPointerException("Parameter direction is null");
      } else {
         this.idxColList.add(Integer.valueOf(colIdx));
         this.orderDirectionList.add(direction);
         return this;
      }
   }

   public int orderColCount() {
      return this.orderDirectionList.size();
   }

   public int orderColIdx(int idx) {
      Integer colIdx = (Integer)this.idxColList.get(idx);
      return colIdx.intValue();
   }

   public OrderDirection orderDirection(int idx) {
      return (OrderDirection)this.orderDirectionList.get(idx);
   }

   public IDataTable dataTable() {
      return this.dataTable;
   }
}