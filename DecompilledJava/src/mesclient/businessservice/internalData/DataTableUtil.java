package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.ArrayWrapper;
import de.mpdv.mesclient.businessservice.internalData.DataTableComparator;
import de.mpdv.mesclient.businessservice.internalData.DataTableSortSpec;
import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.MappingManager;
import de.mpdv.mesclient.businessservice.internalData.SortDataTable;
import de.mpdv.mesclient.businessservice.internalData.DataTableSortSpec.OrderDirection;
import java.util.Arrays;

public class DataTableUtil {

   public static IDataTable sort(DataTableSortSpec sortSpec) {
      if(sortSpec == null) {
         throw new NullPointerException("Parameter sortSpec is null");
      } else {
         IDataTable dataTab = sortSpec.dataTable();
         int rowCount = dataTab.getRowCount();
         Integer[] rowIdxes = new Integer[rowCount];

         int orderColCount;
         for(orderColCount = 0; orderColCount < rowCount; ++orderColCount) {
            rowIdxes[orderColCount] = Integer.valueOf(orderColCount);
         }

         orderColCount = sortSpec.orderColCount();
         int[] colIdxes = new int[orderColCount];
         OrderDirection[] directions = new OrderDirection[orderColCount];
         Class[] types = new Class[orderColCount];

         for(int comp = 0; comp < orderColCount; ++comp) {
            colIdxes[comp] = sortSpec.orderColIdx(comp);
            directions[comp] = sortSpec.orderDirection(comp);
            types[comp] = MappingManager.getInstance().wsTypeToClass(dataTab.getColType(colIdxes[comp]));
         }

         DataTableComparator var9 = new DataTableComparator(dataTab, colIdxes, directions, types);
         Arrays.sort(rowIdxes, var9);
         return new SortDataTable(dataTab, new ArrayWrapper(rowIdxes));
      }
   }
}