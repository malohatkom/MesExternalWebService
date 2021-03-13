package de.mpdv.mesclient.businessservice.util;

import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.mesclient.businessservice.DataWrapper;
import de.mpdv.mesclient.businessservice.GenericParam;
import de.mpdv.mesclient.businessservice.Segment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

public class GenericParamBuilder {

   private final String functionId;
   private final List segList = new ArrayList();


   public GenericParamBuilder(String functionId) {
      this.functionId = functionId;
   }

   public GenericParamBuilder segment(String key, String value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      dw.setS(value);
      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, String value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      dw2.setS(value);
      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParamBuilder segment(String key, Integer value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      dw.setN(value);
      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, Integer value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      dw2.setN(value);
      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParamBuilder segment(String key, byte[] value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      dw.setBn(value);
      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, byte[] value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      dw2.setBn(value);
      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParamBuilder segment(String key, BigDecimal value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      dw.setF(value);
      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, BigDecimal value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      dw2.setF(value);
      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParamBuilder segment(String key, Boolean value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      dw.setB(value);
      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, Boolean value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      dw2.setB(value);
      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParamBuilder segment(String key, XMLGregorianCalendar value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      dw.setD(value);
      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, XMLGregorianCalendar value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      dw2.setD(value);
      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParamBuilder segment(String key, Calendar value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      dw.setD(DateTimeUtil.calendarToXMLCalendar(value));
      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, Calendar value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      dw2.setD(DateTimeUtil.calendarToXMLCalendar(value));
      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParamBuilder segment(String key, String[] value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      List list = dw.getSa();

      for(int i = 0; i < value.length; ++i) {
         list.add(value[i]);
      }

      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, String[] value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      List list = dw2.getSa();

      for(int i = 0; i < value.length; ++i) {
         list.add(value[i]);
      }

      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParamBuilder segment(String key, Integer[] value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      List list = dw.getNa();

      for(int i = 0; i < value.length; ++i) {
         list.add(value[i]);
      }

      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, Integer[] value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      List list = dw2.getNa();

      for(int i = 0; i < value.length; ++i) {
         list.add(value[i]);
      }

      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParamBuilder segment(String key, BigDecimal[] value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      List list = dw.getFa();

      for(int i = 0; i < value.length; ++i) {
         list.add(value[i]);
      }

      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, BigDecimal[] value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      List list = dw2.getFa();

      for(int i = 0; i < value.length; ++i) {
         list.add(value[i]);
      }

      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParamBuilder segment(String key, Boolean[] value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      List list = dw.getBa();

      for(int i = 0; i < value.length; ++i) {
         list.add(value[i]);
      }

      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, Boolean[] value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      List list = dw2.getBa();

      for(int i = 0; i < value.length; ++i) {
         list.add(value[i]);
      }

      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParamBuilder segment(String key, XMLGregorianCalendar[] value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      List list = dw.getDa();

      for(int i = 0; i < value.length; ++i) {
         list.add(value[i]);
      }

      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, XMLGregorianCalendar[] value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      List list = dw2.getDa();

      for(int i = 0; i < value.length; ++i) {
         list.add(value[i]);
      }

      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParamBuilder segment(String key, Calendar[] value) {
      Segment seg = new Segment();
      seg.setKey(key);
      DataWrapper dw = new DataWrapper();
      List list = dw.getDa();

      for(int i = 0; i < value.length; ++i) {
         list.add(DateTimeUtil.calendarToXMLCalendar(value[i]));
      }

      seg.setValue(dw);
      this.segList.add(seg);
      return this;
   }

   public GenericParamBuilder segment(String key, String operator, Calendar[] value) {
      Segment seg1 = new Segment();
      seg1.setKey(key + ".op");
      DataWrapper dw1 = new DataWrapper();
      dw1.setS(operator);
      seg1.setValue(dw1);
      this.segList.add(seg1);
      Segment seg2 = new Segment();
      seg2.setKey(key + ".param");
      DataWrapper dw2 = new DataWrapper();
      List list = dw2.getDa();

      for(int i = 0; i < value.length; ++i) {
         list.add(DateTimeUtil.calendarToXMLCalendar(value[i]));
      }

      seg2.setValue(dw2);
      this.segList.add(seg2);
      return this;
   }

   public GenericParam build() {
      int segCount = this.segList.size();
      Segment[] segArray = new Segment[segCount];
      this.segList.toArray(segArray);
      GenericParam param = new GenericParam();
      param.setFunctionId(this.functionId);
      List list = param.getSegments();

      for(int i = 0; i < segArray.length; ++i) {
         list.add(segArray[i]);
      }

      return param;
   }
}