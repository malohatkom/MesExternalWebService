package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.DataTableComparator.IColumnComparator;
import de.mpdv.mesclient.businessservice.internalData.SingletonHolder;
import java.util.Calendar;

class SimpleColTypeComparator implements IColumnComparator {

   public static SimpleColTypeComparator getInstance() {
      return SingletonHolder.INSTANCE;
   }

   public int compare(Object left, Object right, Class classType) {
      if(classType == null) {
         throw new NullPointerException("Parameter classType is null");
      } else if(left == null && right == null) {
         return 0;
      } else if(left == null) {
         return -1;
      } else if(right == null) {
         return 1;
      } else {
         int result;
         if(classType.equals(Calendar.class)) {
            long leftMillis = ((Calendar)left).getTimeInMillis();
            long rightMillis = ((Calendar)right).getTimeInMillis();
            if(leftMillis < rightMillis) {
               result = -1;
            } else if(leftMillis > rightMillis) {
               result = 1;
            } else {
               result = 0;
            }
         } else {
            result = this.compareByComparable(left, right);
         }

         return result;
      }
   }

   private int compareByComparable(Object left, Object right) {
      Comparable leftVal = (Comparable)left;
      Comparable rightVal = (Comparable)right;
      int result = leftVal.compareTo(rightVal);
      return result;
   }
}