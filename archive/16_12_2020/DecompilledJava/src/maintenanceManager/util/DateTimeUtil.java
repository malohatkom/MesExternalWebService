package de.mpdv.maintenanceManager.util;

import de.mpdv.maintenanceManager.util.Util;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class DateTimeUtil {

   private static final String EMPTY_STR = "";
   private static final String NULL_STR = "0";
   private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
   private static final String XSD_TS_FORMAT = "yyyy-MM-dd\'T\'HH:mm:ss";
   private static final String PRINT_FORMAT = "MM/dd/yyyy HH:mm:ss";
   private static final String ISO_DATE_FORMAT = "MMddyyyy";
   public static final String XML_DT_FORMAT = "yyyy-MM-dd HH:mm:ss";
   public static final String DEFAULT_FORMAT = "MM/dd/yyyy";
   public static final String ISO_DT_FORMAT = "yyyyMMddHHmmss";
   private static final SimpleDateFormat UTC_XSD_FORMATTER = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
   private static final SimpleDateFormat UTC_PRINTFORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
   private static final SimpleDateFormat UTC_ISO_DATE_FORMATTER = new SimpleDateFormat("MMddyyyy");
   private static final SimpleDateFormat LOCAL_PRINTFORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
   private static final SimpleDateFormat UTC_XML_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   private static final SimpleDateFormat UTC_FORMATTER = new SimpleDateFormat("MM/dd/yyyy");
   private static final SimpleDateFormat LOCAL_FORMATTER = new SimpleDateFormat("MM/dd/yyyy");
   private static final SimpleDateFormat UTC_ISO_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
   private static final TimeZone LOCAL_TIME_ZONE = TimeZone.getDefault();
   private static final DatatypeFactory DATAFACTORY;


   public static Calendar getBlankCalendar(TimeZone timeZone) {
      GregorianCalendar cal = new GregorianCalendar(1970, 0, 1, 0, 0, 0);
      cal.setTimeZone(timeZone);
      return cal;
   }

   public static Calendar xsdStringToCalendarUtc(String input) throws ParseException {
      if(input != null && !input.equals("")) {
         SimpleDateFormat formatterInstance = (SimpleDateFormat)UTC_XSD_FORMATTER.clone();
         formatterInstance.setLenient(false);
         Date d = formatterInstance.parse(input);
         Calendar cal = getBlankCalendar(UTC_TIME_ZONE);
         cal.setTime(d);
         return cal;
      } else {
         return null;
      }
   }

   public static String calendarUtcToXsdString(Calendar cal) {
      if(cal == null) {
         return "null";
      } else {
         TimeZone tz = cal.getTimeZone();
         if(UTC_TIME_ZONE.hasSameRules(tz)) {
            SimpleDateFormat formatter = (SimpleDateFormat)UTC_XSD_FORMATTER.clone();
            return formatter.format(cal.getTime());
         } else {
            throw new IllegalArgumentException("Calendar is not in UTC timezone");
         }
      }
   }

   public static String calendarUtcToPrintString(Calendar cal) {
      if(cal == null) {
         return "null";
      } else {
         TimeZone tz = cal.getTimeZone();
         if(UTC_TIME_ZONE.hasSameRules(tz)) {
            SimpleDateFormat formatter = (SimpleDateFormat)UTC_PRINTFORMATTER.clone();
            return formatter.format(cal.getTime());
         } else {
            throw new IllegalArgumentException("Calendar is not in UTC timezone");
         }
      }
   }

   public static String calendarUtcToIsoDateString(Calendar cal) {
      if(cal == null) {
         return "null";
      } else {
         TimeZone tz = cal.getTimeZone();
         if(UTC_TIME_ZONE.hasSameRules(tz)) {
            SimpleDateFormat formatter = (SimpleDateFormat)UTC_ISO_DATE_FORMATTER.clone();
            return formatter.format(cal.getTime());
         } else {
            throw new IllegalArgumentException("Calendar is not in UTC timezone");
         }
      }
   }

   public static Calendar getCurrentUtcCalendar() {
      Calendar cal = Calendar.getInstance();
      cal.setTimeZone(UTC_TIME_ZONE);
      return cal;
   }

   public static XMLGregorianCalendar calendarToXMLCalendar(Calendar date) {
      if(date == null) {
         return null;
      } else {
         XMLGregorianCalendar cal;
         if(date instanceof GregorianCalendar) {
            cal = DATAFACTORY.newXMLGregorianCalendar((GregorianCalendar)date);
         } else {
            cal = DATAFACTORY.newXMLGregorianCalendar(date.get(1), date.get(2) + 1, date.get(5), date.get(11), date.get(12), date.get(13), date.get(14), date.getTimeZone().getOffset(date.getTimeInMillis()));
         }

         return cal;
      }
   }

   public static String calendarToPrintString(Calendar cal) {
      if(cal == null) {
         return "null";
      } else {
         TimeZone tz = cal.getTimeZone();
         SimpleDateFormat formatter;
         if(UTC_TIME_ZONE.hasSameRules(tz)) {
            formatter = (SimpleDateFormat)UTC_PRINTFORMATTER.clone();
         } else if(LOCAL_TIME_ZONE.hasSameRules(tz)) {
            formatter = (SimpleDateFormat)LOCAL_PRINTFORMATTER.clone();
         } else {
            formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            formatter.setTimeZone(tz);
         }

         return formatter.format(cal.getTime());
      }
   }

   public static Timestamp xmlCalendarToTimestamp(XMLGregorianCalendar inCal) {
      if(inCal == null) {
         return null;
      } else {
         GregorianCalendar cal = inCal.toGregorianCalendar();
         return new Timestamp(cal.getTimeInMillis());
      }
   }

   public static Calendar timestampToCalendar(Timestamp ts) {
      if(ts == null) {
         return null;
      } else {
         Calendar cal = getBlankCalendar(LOCAL_TIME_ZONE);
         cal.setTimeInMillis(ts.getTime());
         return cal;
      }
   }

   public static String xmlCalendarToLocalString(XMLGregorianCalendar cal) {
      if(cal == null) {
         return "null";
      } else {
         GregorianCalendar gregCal = cal.toGregorianCalendar();
         return ((SimpleDateFormat)UTC_XML_FORMATTER.clone()).format(gregCal.getTime()) + "+" + TimeZone.getDefault().getOffset(gregCal.getTimeInMillis()) / 3600000;
      }
   }

   public static String calendarToString(Calendar cal) {
      if(cal == null) {
         return "null";
      } else {
         TimeZone tz = cal.getTimeZone();
         SimpleDateFormat formatter;
         if(UTC_TIME_ZONE.hasSameRules(tz)) {
            formatter = (SimpleDateFormat)UTC_FORMATTER.clone();
         } else if(LOCAL_TIME_ZONE.hasSameRules(tz)) {
            formatter = (SimpleDateFormat)LOCAL_FORMATTER.clone();
         } else {
            formatter = new SimpleDateFormat("MM/dd/yyyy");
            formatter.setTimeZone(tz);
         }

         return formatter.format(cal.getTime());
      }
   }

   public static Calendar isoStringToCalendarTimeStampUtc(String raw) {
      Date date = isoStringToDateTimeStampUtc(raw);
      if(date != null) {
         Calendar cal = getBlankCalendar(UTC_TIME_ZONE);
         cal.setTime(date);
         return cal;
      } else {
         return null;
      }
   }

   public static Date isoStringToDateTimeStampUtc(String input) {
      if(input == null) {
         return null;
      } else if("0".equals(input)) {
         return null;
      } else if("".equals(input)) {
         return null;
      } else {
         try {
            SimpleDateFormat exc = (SimpleDateFormat)UTC_ISO_FORMATTER.clone();
            exc.setLenient(false);
            return exc.parse(input);
         } catch (ParseException var2) {
            throw new IllegalArgumentException("Error parsing the date: " + input + "\n" + Util.exceptionToString(var2));
         }
      }
   }

   public static String calendarUtcToIsoString(Calendar cal) {
      if(cal == null) {
         return "null";
      } else {
         TimeZone tz = cal.getTimeZone();
         if(UTC_TIME_ZONE.hasSameRules(tz)) {
            SimpleDateFormat formatter = (SimpleDateFormat)UTC_ISO_FORMATTER.clone();
            return formatter.format(cal.getTime());
         } else {
            throw new IllegalArgumentException("Calendar is not in UTC timezone");
         }
      }
   }

   public static Calendar createCalendar(Calendar cal, int field, int amount) {
      if(cal == null) {
         throw new NullPointerException("Calendar is NULL");
      } else {
         Calendar result = (Calendar)cal.clone();
         int dstBefore = result.get(16);
         result.add(field, amount);
         int dstAfter = result.get(16);
         result.add(14, -(dstAfter - dstBefore));
         return result;
      }
   }

   public static Calendar stringToCalendar(String input) {
      Date date = stringToDate(input);
      if(date != null) {
         Calendar cal = getBlankCalendar(LOCAL_TIME_ZONE);
         cal.setTime(date);
         return cal;
      } else {
         return null;
      }
   }

   public static Date stringToDate(String input) {
      if(input == null) {
         return null;
      } else if("0".equals(input)) {
         return null;
      } else if("".equals(input)) {
         return null;
      } else {
         try {
            SimpleDateFormat exc = (SimpleDateFormat)LOCAL_FORMATTER.clone();
            return exc.parse(input);
         } catch (ParseException var2) {
            throw new IllegalArgumentException("Error parsing the date: " + input + "\n" + Util.exceptionToString(var2));
         }
      }
   }

   static {
      TimeZone gmt = TimeZone.getTimeZone("UTC");
      UTC_XSD_FORMATTER.setTimeZone(gmt);
      UTC_PRINTFORMATTER.setTimeZone(gmt);
      UTC_ISO_DATE_FORMATTER.setTimeZone(gmt);
      UTC_XML_FORMATTER.setTimeZone(gmt);
      UTC_FORMATTER.setTimeZone(gmt);
      UTC_ISO_FORMATTER.setTimeZone(gmt);
      DatatypeFactory factory = null;

      try {
         factory = DatatypeFactory.newInstance();
      } catch (DatatypeConfigurationException var3) {
         throw new RuntimeException("Error instantiating datatype factory: " + Util.exceptionToString(var3));
      }

      DATAFACTORY = factory;
   }
}