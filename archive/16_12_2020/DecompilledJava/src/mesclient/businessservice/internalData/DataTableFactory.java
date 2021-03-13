package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.DataTable;
import de.mpdv.mesclient.businessservice.internalData.DataTableColumnMeta;
import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DataTableFactory {

   public static IDataTable createDataTable(List tableStructure, List tabularData) {
      return new DataTable(tableStructure, tabularData);
   }

   public static IDataTable createDataTable(List tableStructure, List tabularData, String dataTableId) {
      return new DataTable(tableStructure, tabularData, dataTableId);
   }

   public static IDataTable copyDataTable(IDataTable original) {
      List tableStructure = original.dumpMeta(true);
      List tabularData = original.dumpData(true);
      int metaCount = tableStructure.size();
      ArrayList newMeta = new ArrayList(metaCount);

      int dataCount;
      for(dataCount = 0; dataCount < metaCount; ++dataCount) {
         newMeta.add(new DataTableColumnMeta((DataTableColumnMeta)tableStructure.get(dataCount)));
      }

      dataCount = tabularData.size();
      ArrayList newData = new ArrayList(dataCount);

      for(int i = 0; i < dataCount; ++i) {
         List row = (List)tabularData.get(i);
         int colCount = row.size();
         ArrayList newRow = new ArrayList(colCount);

         for(int j = 0; j < colCount; ++j) {
            Object val = row.get(j);
            Object newVal = null;
            if(val != null) {
               Class valType = val.getClass();
               if(!valType.equals(String.class) && !valType.equals(Integer.class) && !valType.equals(BigDecimal.class) && !valType.equals(Boolean.class)) {
                  if(!Calendar.class.isAssignableFrom(valType)) {
                     int len;
                     int k;
                     if(valType.equals(byte[].class)) {
                        byte[] var28 = (byte[])((byte[])val);
                        len = var28.length;
                        byte[] var34 = new byte[len];

                        for(k = 0; k < len; ++k) {
                           var34[k] = var28[k];
                        }

                        newVal = var34;
                     } else if(valType.equals(String[].class)) {
                        String[] var27 = (String[])((String[])val);
                        len = var27.length;
                        String[] var33 = new String[len];

                        for(k = 0; k < len; ++k) {
                           var33[k] = var27[k];
                        }

                        newVal = var33;
                     } else if(valType.equals(Integer[].class)) {
                        Integer[] var26 = (Integer[])((Integer[])val);
                        len = var26.length;
                        Integer[] var32 = new Integer[len];

                        for(k = 0; k < len; ++k) {
                           var32[k] = var26[k];
                        }

                        newVal = var32;
                     } else if(valType.equals(BigDecimal[].class)) {
                        BigDecimal[] var25 = (BigDecimal[])((BigDecimal[])val);
                        len = var25.length;
                        BigDecimal[] var31 = new BigDecimal[len];

                        for(k = 0; k < len; ++k) {
                           var31[k] = var25[k];
                        }

                        newVal = var31;
                     } else if(valType.equals(Boolean[].class)) {
                        Boolean[] var24 = (Boolean[])((Boolean[])val);
                        len = var24.length;
                        Boolean[] var30 = new Boolean[len];

                        for(k = 0; k < len; ++k) {
                           var30[k] = var24[k];
                        }

                        newVal = var30;
                     } else if(Calendar[].class.isAssignableFrom(valType)) {
                        Calendar[] var23 = (Calendar[])((Calendar[])val);
                        len = var23.length;
                        Calendar[] var29 = new Calendar[len];

                        for(k = 0; k < len; ++k) {
                           var29[k] = var23[k] == null?null:(Calendar)var23[k].clone();
                        }

                        newVal = var29;
                     } else if(valType.equals(byte[][].class)) {
                        byte[][] arr = (byte[][])((byte[][])val);
                        len = arr.length;
                        byte[][] newArr = new byte[len][];
                        k = 0;

                        while(true) {
                           if(k >= len) {
                              newVal = newArr;
                              break;
                           }

                           byte[] subArr = arr[k];
                           byte[] newSubArr = null;
                           if(subArr != null) {
                              int subLen = subArr.length;
                              newSubArr = new byte[subLen];

                              for(int l = 0; l < subLen; ++l) {
                                 newSubArr[l] = subArr[l];
                              }
                           }

                           newArr[k] = newSubArr;
                           ++k;
                        }
                     }
                  } else {
                     newVal = ((Calendar)val).clone();
                  }
               } else {
                  newVal = val;
               }
            }

            newRow.add(newVal);
         }

         newData.add(newRow);
      }

      return createDataTable(newMeta, newData);
   }

   public static IDataTable copyDataTable(IDataTable original, String dataTableId) {
      IDataTable table = copyDataTable(original);
      table.setDataTableId(dataTableId);
      return table;
   }
}