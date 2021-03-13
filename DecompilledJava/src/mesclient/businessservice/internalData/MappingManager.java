package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.WebServiceType;
import de.mpdv.mesclient.businessservice.internalData.SingletonHolder;
import de.mpdv.mesclient.businessservice.internalData.WsTypeClassMappingStruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MappingManager {

    
   private final List wsTypeToClassMappings = this.initializeTypeClassMappings();
   private final Map wsTypeToClassMap = this.initializeTypeToClassMap();


   public static MappingManager getInstance() {
      return SingletonHolder.INSTANCE;
   }

   private List initializeTypeClassMappings() {
      ArrayList list = new ArrayList();
      list.add(new WsTypeClassMappingStruct(String.class, WebServiceType.STRING));
      list.add(new WsTypeClassMappingStruct(String[].class, WebServiceType.A_STRING));
      list.add(new WsTypeClassMappingStruct(BigDecimal.class, WebServiceType.DECIMAL));
      list.add(new WsTypeClassMappingStruct(BigDecimal[].class, WebServiceType.A_DECIMAL));
      list.add(new WsTypeClassMappingStruct(Calendar.class, WebServiceType.DATETIME));
      list.add(new WsTypeClassMappingStruct(Calendar[].class, WebServiceType.A_DATETIME));
      list.add(new WsTypeClassMappingStruct(Integer.class, WebServiceType.INTEGER));
      list.add(new WsTypeClassMappingStruct(Integer[].class, WebServiceType.A_INTEGER));
      list.add(new WsTypeClassMappingStruct(Boolean.class, WebServiceType.BOOLEAN));
      list.add(new WsTypeClassMappingStruct(Boolean[].class, WebServiceType.A_BOOLEAN));
      list.add(new WsTypeClassMappingStruct(byte[].class, WebServiceType.BINARY));
      list.add(new WsTypeClassMappingStruct(byte[][].class, WebServiceType.A_BINARY));
      return list;
   }

   private Map initializeTypeToClassMap() {
      HashMap map = new HashMap();
      Iterator i$ = this.wsTypeToClassMappings.iterator();

      while(i$.hasNext()) {
         WsTypeClassMappingStruct mapping = (WsTypeClassMappingStruct)i$.next();
         map.put(mapping.wsType, mapping.clsType);
      }

      return map;
   }

   public Class wsTypeToClass(WebServiceType wsType) {
      if(wsType == null) {
         throw new IllegalArgumentException("Web service type is NULL or empty");
      } else {
         Class type = (Class)this.wsTypeToClassMap.get(wsType);
         if(type == null) {
            throw new IllegalArgumentException("No valid web service type given: " + wsType);
         } else {
            return type;
         }
      }
   }
}