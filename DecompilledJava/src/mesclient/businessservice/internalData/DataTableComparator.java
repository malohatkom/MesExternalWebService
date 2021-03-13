package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.DataTableComparator.ArrayColTypeComparator;
import de.mpdv.mesclient.businessservice.internalData.DataTableComparator.IColumnComparator;
import de.mpdv.mesclient.businessservice.internalData.DataTableComparator.MultiArrayComparator;
import de.mpdv.mesclient.businessservice.internalData.DataTableComparator.SimpleColTypeComparator;
import de.mpdv.mesclient.businessservice.internalData.DataTableSortSpec.OrderDirection;
import java.util.Comparator;

class DataTableComparator implements Comparator {

   private final IDataTable dataTab;
   private final int[] colIdxes;
   private final OrderDirection[] directions;
   private final Class[] types;
   private final IColumnComparator[] comparatorArr;


   DataTableComparator(IDataTable dataTab, int[] colIdxes, OrderDirection[] directions, Class[] types) {
      this.dataTab = dataTab;
      this.colIdxes = colIdxes;
      this.directions = directions;
      this.types = types;
      if(this.dataTab == null) {
         throw new NullPointerException("Parameter dataTab is null");
      } else if(this.colIdxes != null && this.colIdxes.length != 0) {
         if(this.directions != null && this.directions.length != 0) {
            if(this.types != null && this.types.length != 0) {
               if(this.colIdxes.length == this.directions.length && this.colIdxes.length == this.types.length) {
                  this.comparatorArr = new IColumnComparator[this.types.length];

                  for(int ii = 0; ii < this.types.length; ++ii) {
                     if(this.types[ii].isArray()) {
                        if(byte[][].class.equals(this.types[ii])) {
                           this.comparatorArr[ii] = MultiArrayComparator.getInstance();
                        } else {
                           this.comparatorArr[ii] = ArrayColTypeComparator.getInstance();
                        }
                     } else {
                        this.comparatorArr[ii] = SimpleColTypeComparator.getInstance();
                     }
                  }

               } else {
                  throw new IllegalArgumentException("The length of the three array parameters differ. colIdxes has size: " + this.colIdxes.length + ", directions has size: " + this.directions.length + ", classTypes has size: " + this.types.length);
               }
            } else {
               throw new IllegalArgumentException("Parameter types is null or empty");
            }
         } else {
            throw new IllegalArgumentException("Parameter directions is null or empty");
         }
      } else {
         throw new IllegalArgumentException("Parameter colIdxes is null or empty");
      }
   }

   public int compare(Integer lhs, Integer rhs) {
      if(lhs == null) {
         throw new NullPointerException("Parameter lhs is null");
      } else if(rhs == null) {
         throw new NullPointerException("Parameter rhs is null");
      } else {
         int result = 0;

         for(int ii = 0; result == 0 && ii < this.comparatorArr.length; ++ii) {
            Object lhsObject = this.dataTab.getCellValue(lhs.intValue(), this.colIdxes[ii], this.types[ii]);
            Object rhsObject = this.dataTab.getCellValue(rhs.intValue(), this.colIdxes[ii], this.types[ii]);
            result = this.comparatorArr[ii].compare(lhsObject, rhsObject, this.types[ii]);
            if(result != 0 && this.directions[ii].equals(OrderDirection.DESC)) {
               result *= -1;
            }
         }

         return result;
      }
   }
}