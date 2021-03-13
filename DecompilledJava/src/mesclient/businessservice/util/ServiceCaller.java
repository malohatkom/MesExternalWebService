package de.mpdv.mesclient.businessservice.util;

import de.mpdv.maintenanceManager.util.DateTimeUtil;
import de.mpdv.maintenanceManager.util.Util;
import de.mpdv.mesclient.businessservice.AuthToken;
import de.mpdv.mesclient.businessservice.BusinessException;
import de.mpdv.mesclient.businessservice.GenericResultSet;
import de.mpdv.mesclient.businessservice.MesBusinessService;
import de.mpdv.mesclient.businessservice.MesBusinessServiceService;
import de.mpdv.mesclient.businessservice.ResultItem;
import de.mpdv.mesclient.businessservice.ServiceEnvironment;
import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.ServiceCallData;
import de.mpdv.mesclient.businessservice.internalData.ServiceInputFilterParam;
import de.mpdv.mesclient.businessservice.internalData.ServiceInputSpecialParam;
import de.mpdv.mesclient.businessservice.util.BusinessExceptionConverter;
import de.mpdv.mesclient.businessservice.util.GenericParamBuilder;
import de.mpdv.mesclient.businessservice.util.GenericResultSetConverter;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

public class ServiceCaller {

   private static final String DEFAULT_WSDL_LOCATION = "http://%1$s/MocServices/MesBusinessService?wsdl";
   private static final AtomicInteger REQUEST_ID = new AtomicInteger(1);


   public static IDataTable callService(ServiceCallData data) {
      try {
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - \n\nReceived service call: " + data + ":\n" + DateTimeUtil.calendarToPrintString(Calendar.getInstance()));
         long e = 0L;
         long endPerf = 0L;
         double durationPerf = 0.0D;
         e = System.nanoTime();
         String wsdlLocation = String.format("http://%1$s/MocServices/MesBusinessService?wsdl", new Object[]{data.getTomcatHostPort()});

         URL serviceUrl;
         try {
            serviceUrl = new URL(wsdlLocation);
         } catch (MalformedURLException var17) {
            throw new IllegalArgumentException("Could not generate wsdl url from " + wsdlLocation);
         }

         MesBusinessServiceService serviceStub = new MesBusinessServiceService(serviceUrl, new QName("http://businessService.mesClient.mpdv.de/", "MesBusinessServiceService"));
         MesBusinessService service = serviceStub.getMesBusinessServicePort();
         ServiceEnvironment env = getServiceEnvironment(data.getLicenseToken(), data.getSessionId(), data.getClientId());
         GenericParamBuilder builder = new GenericParamBuilder(data.getFunctionId());
         appendColumnConfigurator(data.getRequestedColumns(), builder);
         appendFilters(data.getFilterCriterias(), builder);
         appendSpecialParams(data.getSpecialParams(), builder);
         appendDirectParams(data.getDirectParams(), builder);
         ResultItem item = service.interactMaintMgr(env, builder.build());
         endPerf = System.nanoTime();
         durationPerf = (double)(endPerf - e) / 1.0E9D;
         System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - \nDuration service call:" + data.getFunctionId() + ":\n" + durationPerf + ". Timestamp: " + DateTimeUtil.calendarToPrintString(Calendar.getInstance()));
         e = System.nanoTime();
         List resultList = item.getResultSetArray();
         if(resultList.size() > 1) {
            throw new IllegalStateException("Backend does not support services with more than one resultset");
         } else {
            IDataTable table = null;
            if(resultList.size() == 1) {
               GenericResultSet rs = (GenericResultSet)resultList.get(0);
               if(rs != null) {
                  table = GenericResultSetConverter.convert(rs);
               }
            }

            endPerf = System.nanoTime();
            durationPerf = (double)(endPerf - e) / 1.0E9D;
            System.out.println(DateTimeUtil.calendarToPrintString(Calendar.getInstance()) + ": Maint Mgr - \nDuration result conversion:" + data.getFunctionId() + ":\n" + durationPerf + ". Timestamp: " + DateTimeUtil.calendarToPrintString(Calendar.getInstance()));
            return table;
         }
      } catch (Exception var18) {
         Object exc = null;
         if(var18 instanceof BusinessException) {
            exc = BusinessExceptionConverter.convert((BusinessException)var18);
         } else {
            exc = var18;
         }

         throw new RuntimeException("Error calling webservice\n" + Util.recursiveExceptionMessageToString((Throwable)exc));
      }
   }

   private static void appendColumnConfigurator(List requestedColumns, GenericParamBuilder builder) {
      if(requestedColumns != null && requestedColumns.size() > 0) {
         int colCount = requestedColumns.size();
         String[] colArr = new String[colCount];
         colArr = (String[])requestedColumns.toArray(colArr);
         Integer[] aggFcts = new Integer[colCount];

         for(int i = 0; i < colCount; ++i) {
            aggFcts[i] = Integer.valueOf(5);
         }

         builder.segment("select.colconf", colArr);
         builder.segment("select.aggfuncts", aggFcts);
      }

   }

   private static void appendFilters(List filterCriterias, GenericParamBuilder builder) {
      if(filterCriterias != null && filterCriterias.size() > 0) {
         ArrayList expList = new ArrayList();
         int count = filterCriterias.size();

         for(int filterArr = 0; filterArr < count; ++filterArr) {
            expList.add("filter" + filterArr);
            ServiceInputFilterParam param = (ServiceInputFilterParam)filterCriterias.get(filterArr);
            builder.segment("filter" + filterArr + ".id", param.getKey());
            builder.segment("filter" + filterArr + ".operator", param.getOperator());
            List list = param.getValues();
            int valCount = list.size();
            Class clz = param.getValueType();
            int j;
            if(clz.equals(String.class)) {
               String[] arr = new String[valCount];

               for(j = 0; j < valCount; ++j) {
                  arr[j] = (String)list.get(j);
               }

               builder.segment("filter" + filterArr + ".param", arr);
            } else if(clz.equals(Integer.class)) {
               Integer[] var12 = new Integer[valCount];

               for(j = 0; j < valCount; ++j) {
                  var12[j] = (Integer)list.get(j);
               }

               builder.segment("filter" + filterArr + ".param", var12);
            } else if(clz.equals(BigDecimal.class)) {
               BigDecimal[] var13 = new BigDecimal[valCount];

               for(j = 0; j < valCount; ++j) {
                  var13[j] = (BigDecimal)list.get(j);
               }

               builder.segment("filter" + filterArr + ".param", var13);
            } else if(clz.equals(Boolean.class)) {
               Boolean[] var14 = new Boolean[valCount];

               for(j = 0; j < valCount; ++j) {
                  var14[j] = (Boolean)list.get(j);
               }

               builder.segment("filter" + filterArr + ".param", var14);
            } else {
               XMLGregorianCalendar[] var15;
               if(XMLGregorianCalendar.class.isAssignableFrom(clz)) {
                  var15 = new XMLGregorianCalendar[valCount];

                  for(j = 0; j < valCount; ++j) {
                     var15[j] = (XMLGregorianCalendar)list.get(j);
                  }

                  builder.segment("filter" + filterArr + ".param", var15);
               } else if(Calendar.class.isAssignableFrom(clz)) {
                  var15 = new XMLGregorianCalendar[valCount];

                  for(j = 0; j < valCount; ++j) {
                     var15[j] = DateTimeUtil.calendarToXMLCalendar((Calendar)list.get(j));
                  }

                  builder.segment("filter" + filterArr + ".param", var15);
               }
            }
         }

         String[] var11 = new String[expList.size()];
         var11 = (String[])expList.toArray(var11);
         builder.segment("root.expressions", var11);
         builder.segment("root.conjunction", "AND");
      }

   }

   private static void appendDirectParams(Map directParams, GenericParamBuilder builder) {
      if(directParams != null && directParams.size() > 0) {
         Iterator it = directParams.keySet().iterator();

         while(it.hasNext()) {
            String key = (String)it.next();
            Object value = directParams.get(key);
            Class clz = value.getClass();
            if(clz.equals(String.class)) {
               builder.segment(key, (String)value);
            } else if(clz.equals(String[].class)) {
               builder.segment(key, (String[])((String[])value));
            } else if(clz.equals(Integer.class)) {
               builder.segment(key, (Integer)value);
            } else if(clz.equals(Integer[].class)) {
               builder.segment(key, (Integer[])((Integer[])value));
            } else if(clz.equals(BigDecimal.class)) {
               builder.segment(key, (BigDecimal)value);
            } else if(clz.equals(BigDecimal[].class)) {
               builder.segment(key, (BigDecimal[])((BigDecimal[])value));
            } else if(clz.equals(Boolean.class)) {
               builder.segment(key, (Boolean)value);
            } else if(clz.equals(Boolean[].class)) {
               builder.segment(key, (Boolean[])((Boolean[])value));
            } else if(clz.equals(XMLGregorianCalendar.class)) {
               builder.segment(key, (XMLGregorianCalendar)value);
            } else if(clz.equals(XMLGregorianCalendar[].class)) {
               builder.segment(key, (XMLGregorianCalendar[])((XMLGregorianCalendar[])value));
            } else if(clz.equals(Calendar.class)) {
               builder.segment(key, (Calendar)value);
            } else if(clz.equals(Calendar[].class)) {
               builder.segment(key, (Calendar[])((Calendar[])value));
            } else if(clz.equals(byte[].class)) {
               builder.segment(key, (byte[])((byte[])value));
            }
         }
      }

   }

   private static void appendSpecialParams(List specialParams, GenericParamBuilder builder) {
      if(specialParams != null && specialParams.size() > 0) {
         for(int i = 0; i < specialParams.size(); ++i) {
            ServiceInputSpecialParam param = (ServiceInputSpecialParam)specialParams.get(i);
            String key = param.getKey();
            String op = param.getOperator();
            Object value = param.getValue();
            Class clz = value.getClass();
            if(clz.equals(String.class)) {
               builder.segment(key, op, (String)value);
            } else if(clz.equals(String[].class)) {
               builder.segment(key, op, (String[])((String[])value));
            } else if(clz.equals(Integer.class)) {
               builder.segment(key, op, (Integer)value);
            } else if(clz.equals(Integer[].class)) {
               builder.segment(key, op, (Integer[])((Integer[])value));
            } else if(clz.equals(BigDecimal.class)) {
               builder.segment(key, op, (BigDecimal)value);
            } else if(clz.equals(BigDecimal[].class)) {
               builder.segment(key, op, (BigDecimal[])((BigDecimal[])value));
            } else if(clz.equals(Boolean.class)) {
               builder.segment(key, op, (Boolean)value);
            } else if(clz.equals(Boolean[].class)) {
               builder.segment(key, op, (Boolean[])((Boolean[])value));
            } else if(clz.equals(XMLGregorianCalendar.class)) {
               builder.segment(key, op, (XMLGregorianCalendar)value);
            } else if(clz.equals(XMLGregorianCalendar[].class)) {
               builder.segment(key, op, (XMLGregorianCalendar[])((XMLGregorianCalendar[])value));
            } else if(clz.equals(Calendar.class)) {
               builder.segment(key, op, (Calendar)value);
            } else if(clz.equals(Calendar[].class)) {
               builder.segment(key, op, (Calendar[])((Calendar[])value));
            } else if(clz.equals(byte[].class)) {
               builder.segment(key, op, (byte[])((byte[])value));
            }
         }
      }

   }

   private static ServiceEnvironment getServiceEnvironment(String licenseToken, String sessionId, Integer clientId) {
      ServiceEnvironment env = new ServiceEnvironment();
      env.setAbortOnError(Boolean.FALSE);
      AuthToken token = new AuthToken();
      token.setToken((byte[])null);
      env.setAuth(token);
      env.setBatch(Boolean.FALSE);

      try {
         env.setDeviceId(InetAddress.getLocalHost().getHostName());
      } catch (UnknownHostException var6) {
         env.setDeviceId("DUMMY");
      }

      env.setLangId("de");
      env.setRequestId("" + REQUEST_ID.getAndIncrement());
      env.setTx(Boolean.FALSE);
      env.setUserId("Maint");
      env.setLicenseTokenId(licenseToken);
      env.setSessionId(sessionId);
      env.setClientId(clientId.toString());
      return env;
   }

}