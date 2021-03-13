package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.DataTableComparator.IColumnComparator;
import de.mpdv.mesclient.businessservice.internalData.DataTableComparator.SimpleColTypeComparator;
import de.mpdv.mesclient.businessservice.internalData.DataTableComparator.ArrayColTypeComparator.SingletonHolder;

class ArrayColTypeComparator implements IColumnComparator {
    
   public static ArrayColTypeComparator getInstance() {
      return SingletonHolder.INSTANCE;
   }

   public int compare(Object left, Object right, Class classType) {
      if(classType == null) {
         throw new NullPointerException("Parameter classType is null");
      } else {
         return classType.equals(byte[].class)?this.compareByteArray(left, right):this.compareNormalArray(left, right, classType);
      }
   }

   private int compareNormalArray(Object left, Object right, Class classType) {
      Object[] arrLeft = (Object[])((Object[])left);
      Object[] arrRight = (Object[])((Object[])right);
      if(arrLeft != null && arrRight != null) {
         if(arrLeft.length == 0 && arrRight.length == 0) {
            return 0;
         } else {
            int leftCount = arrLeft.length;
            int rightCount = arrRight.length;
            int elemCount = leftCount <= rightCount?leftCount:rightCount;

            for(int i = 0; i < elemCount; ++i) {
               int result = SimpleColTypeComparator.getInstance().compare(arrLeft[i], arrRight[i], classType.getComponentType());
               if(result != 0) {
                  return result;
               }

               if(i == elemCount - 1 && leftCount != rightCount) {
                  return leftCount < rightCount?-1:1;
               }
            }

            return 0;
         }
      } else {
         return arrRight != null?-1:(arrLeft != null?1:0);
      }
   }

   private int compareByteArray(Object left, Object right) {
      byte[] arrLeft = (byte[])((byte[])left);
      byte[] arrRight = (byte[])((byte[])right);
      if(arrLeft != null && arrRight != null) {
         if(arrLeft.length == 0 && arrRight.length == 0) {
            return 0;
         } else {
            int leftCount = arrLeft.length;
            int rightCount = arrRight.length;
            int elemCount = leftCount <= rightCount?leftCount:rightCount;

            for(int i = 0; i < elemCount; ++i) {
               byte result;
               if(arrLeft[i] < arrRight[i]) {
                  result = -1;
               } else if(arrLeft[i] > arrRight[i]) {
                  result = 1;
               } else {
                  result = 0;
               }

               if(result != 0) {
                  return result;
               }

               if(i == elemCount - 1 && leftCount != rightCount) {
                  return leftCount < rightCount?-1:1;
               }
            }

            return 0;
         }
      } else {
         return arrRight != null?-1:(arrLeft != null?1:0);
      }
   }
}