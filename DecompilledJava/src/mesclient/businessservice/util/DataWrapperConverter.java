package de.mpdv.mesclient.businessservice.util;

import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.mesclient.businessservice.internalData.DataWrapper;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

public class DataWrapperConverter {

   public static DataWrapper convert(de.mpdv.mesclient.businessservice.DataWrapper dw) {
      DataWrapper newDw = new DataWrapper();
      String type = dw.getT();
      if(type.equals("string")) {
         newDw.setStr(dw.getS());
      } else if(type.equals("integer")) {
         newDw.setNum(dw.getN());
      } else if(type.equals("decimal")) {
         newDw.setDec(dw.getF());
      } else if(type.equals("boolean")) {
         newDw.setBool(dw.isB());
      } else if(type.equals("binary")) {
         newDw.setBin(dw.getBn());
      } else if(type.equals("datetime")) {
         XMLGregorianCalendar list = dw.getD();
         if(list != null) {
            newDw.setCal(DateTimeUtil.timestampToCalendar(DateTimeUtil.xmlCalendarToTimestamp(list)));
         } else {
            newDw.setCal((Calendar)null);
         }
      } else if(type.equals("string[]")) {
         newDw.setStrAr((String[])listToArrayAllowNull(dw.getSa(), String.class));
      } else if(type.equals("integer")) {
         newDw.setNumAr((Integer[])listToArrayAllowNull(dw.getNa(), Integer.class));
      } else if(type.equals("decimal")) {
         newDw.setDecAr((BigDecimal[])listToArrayAllowNull(dw.getFa(), BigDecimal.class));
      } else if(type.equals("boolean")) {
         newDw.setBoolAr((Boolean[])listToArrayAllowNull(dw.getBa(), Boolean.class));
      } else if(type.equals("binary")) {
         newDw.setBinAr((byte[][])listToArrayAllowNull(dw.getBna(), byte[].class));
      } else if(type.equals("datetime")) {
         List var7 = dw.getDa();
         if(var7 == null) {
            newDw.setCalAr((Calendar[])null);
         } else {
            Calendar[] arr = new Calendar[var7.size()];

            for(int l = 0; l < var7.size(); ++l) {
               XMLGregorianCalendar val = (XMLGregorianCalendar)var7.get(l);
               if(val == null) {
                  arr[l] = null;
               } else {
                  arr[l] = DateTimeUtil.timestampToCalendar(DateTimeUtil.xmlCalendarToTimestamp((XMLGregorianCalendar)var7.get(l)));
               }
            }

            newDw.setCalAr((Calendar[])null);
         }
      }

      return newDw;
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
}