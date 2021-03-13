package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.DataTableComparator.ArrayColTypeComparator;
import de.mpdv.mesclient.businessservice.internalData.DataTableComparator.IColumnComparator;
import de.mpdv.mesclient.businessservice.internalData.DataTableComparator.MultiArrayComparator.SingletonHolder;

class DataTableComparator$MultiArrayComparator implements IColumnComparator {

   public static DataTableComparator$MultiArrayComparator getInstance() {
      return SingletonHolder.INSTANCE;
   }

   public int compare(Object left, Object right, Class classType) {
      if(classType == null) {
         throw new NullPointerException("Parameter localType is null");
      } else if(!classType.equals(byte[][].class)) {
         throw new IllegalArgumentException("Unsupported multiarray type: " + classType.toString());
      } else {
         byte[][] arrLeft = (byte[][])((byte[][])left);
         byte[][] arrRight = (byte[][])((byte[][])right);
         if(arrLeft != null && arrRight != null) {
            if(arrLeft.length == 0 && arrRight.length == 0) {
               return 0;
            } else {
               int leftCount = arrLeft.length;
               int rightCount = arrRight.length;
               int elemCount = leftCount <= rightCount?leftCount:rightCount;

               for(int i = 0; i < elemCount; ++i) {
                  int result = ArrayColTypeComparator.getInstance().compare(arrLeft[i], arrRight[i], byte[].class);
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
}