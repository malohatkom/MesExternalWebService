package de.mpdv.mesclient.businessservice.util;

import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.mesclient.businessservice.ColumnMeta;
import de.mpdv.mesclient.businessservice.DataWrapper;
import de.mpdv.mesclient.businessservice.GenericResultSet;
import de.mpdv.mesclient.businessservice.Row;
import de.mpdv.mesclient.businessservice.internalData.DataTableColumnMeta;
import de.mpdv.mesclient.businessservice.internalData.DataTableFactory;
import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.DataTypes.WebServiceType;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

public class GenericResultSetConverter {

   public static IDataTable convert(GenericResultSet rs) {
      List metaList = getColumnMeta(rs);
      List rows = rs.getR();
      ArrayList dataTableRows = new ArrayList();

      for(int table = 0; table < rows.size(); ++table) {
         Row r = (Row)rows.get(table);
         List dataTableRow = getTableRow(metaList, r);
         dataTableRows.add(dataTableRow);
      }

      IDataTable var7 = DataTableFactory.createDataTable(metaList, dataTableRows, rs.getResultSetId());
      return var7;
   }

   private static List getTableRow(List metaList, Row r) {
      List cols = r.getC();
      ArrayList dataTableRow = new ArrayList();

      for(int k = 0; k < cols.size(); ++k) {
         String type = ((DataTableColumnMeta)metaList.get(k)).getColWsType().toString().toLowerCase();
         DataWrapper col = (DataWrapper)cols.get(k);
         if(type.equals("string")) {
            dataTableRow.add(col.getS());
         } else if(type.equals("integer")) {
            dataTableRow.add(col.getN());
         } else if(type.equals("decimal")) {
            dataTableRow.add(col.getF());
         } else if(type.equals("boolean")) {
            dataTableRow.add(col.isB());
         } else if(type.equals("binary")) {
            dataTableRow.add(col.getBn());
         } else if(type.equals("datetime")) {
            dataTableRow.add(DateTimeUtil.timestampToCalendar(DateTimeUtil.xmlCalendarToTimestamp(col.getD())));
         } else if(type.equals("string[]")) {
            dataTableRow.add(listToArrayAllowNull(col.getSa(), String.class));
         } else if(type.equals("integer")) {
            dataTableRow.add(listToArrayAllowNull(col.getNa(), Integer.class));
         } else if(type.equals("decimal")) {
            dataTableRow.add(listToArrayAllowNull(col.getDa(), BigDecimal.class));
         } else if(type.equals("boolean")) {
            dataTableRow.add(listToArrayAllowNull(col.getBa(), Boolean.class));
         } else if(type.equals("binary")) {
            dataTableRow.add(listToArrayAllowNull(col.getBna(), byte[].class));
         } else if(type.equals("datetime")) {
            List list = col.getDa();
            if(list == null) {
               dataTableRow.add((Object)null);
            } else {
               Calendar[] arr = new Calendar[list.size()];

               for(int l = 0; l < list.size(); ++l) {
                  XMLGregorianCalendar val = (XMLGregorianCalendar)list.get(l);
                  if(val == null) {
                     arr[l] = null;
                  } else {
                     arr[l] = DateTimeUtil.timestampToCalendar(DateTimeUtil.xmlCalendarToTimestamp((XMLGregorianCalendar)list.get(l)));
                  }
               }

               dataTableRow.add(arr);
            }
         } else {
            dataTableRow.add((Object)null);
         }
      }

      return dataTableRow;
   }

   private static Object[] listToArrayAllowNull(List list, Class type) {
      if(list == null) {
         return null;
      } else {
         int count = list.size();
         Object[] arr = (Object[])((Object[])Array.newInstance(type, count));

         for(int i = 0; i < count; ++i) {
            Object val = list.get(i);
            arr[i] = val;
         }

         return arr;
      }
   }

   private static List getColumnMeta(GenericResultSet rs) {
      List meta = rs.getM();
      ArrayList colMeta = new ArrayList();

      for(int j = 0; j < meta.size(); ++j) {
         ColumnMeta curMeta = (ColumnMeta)meta.get(j);
         DataTableColumnMeta dataTableMeta = new DataTableColumnMeta(curMeta.getK(), WebServiceType.valueOf(curMeta.getT().toUpperCase()));
         colMeta.add(dataTableMeta);
      }

      return colMeta;
   }
}