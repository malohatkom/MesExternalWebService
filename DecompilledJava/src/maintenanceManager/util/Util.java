package de.mpdv.maintenanceManager.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Util {

   public static String escapeHTML(String str) {
      if(str != null && str.length() != 0) {
         StringBuilder buf = new StringBuilder();
         int len = str.length();

         for(int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            switch(c) {
            case 34:
               buf.append("&quot;");
               break;
            case 38:
               buf.append("&amp;");
               break;
            case 39:
               buf.append("&apos;");
               break;
            case 60:
               buf.append("&lt;");
               break;
            case 62:
               buf.append("&gt;");
               break;
            default:
               buf.append(c);
            }
         }

         return buf.toString();
      } else {
         return "";
      }
   }

   public static String newlinesToXHTMLBreaks(String str) {
      if(str != null && str.length() != 0) {
         StringBuilder buf = new StringBuilder();
         int len = str.length();

         for(int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            switch(c) {
            case 10:
               buf.append("\n<br />");
               break;
            case 13:
               if(i + 1 < len && str.charAt(i + 1) == 10) {
                  ++i;
               }

               buf.append("\n<br />");
               break;
            default:
               buf.append(c);
            }
         }

         return buf.toString();
      } else {
         return "";
      }
   }

   public static boolean stringNullOrEmpty(String input) {
      return input == null || input.length() == 0;
   }

   public static String seperatorsToSlash(String path) {
      if(stringNullOrEmpty(path)) {
         throw new IllegalArgumentException("Parameter path is null or empty");
      } else {
         return path.replace("\\", "/");
      }
   }

   public static StringWriter exceptionToStringWriter(Throwable exc) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exc.printStackTrace(pw);
      return sw;
   }

   public static String exceptionToString(Throwable exc) {
      return exceptionToStringWriter(exc).toString();
   }

   public static String recursiveExceptionMessageToString(Throwable exc) {
      StringBuilder builder = new StringBuilder();
      Throwable current = exc;

      do {
         if(current != exc) {
            builder.append(" because of: ");
         }

         StackTraceElement[] elements = exc.getStackTrace();
         builder.append(current.getClass().getSimpleName());
         if(elements != null && elements.length != 0) {
            StackTraceElement element = elements[0];
            if(element.getClassName() != null && element.getClassName().length() != 0) {
               builder.append(" at class: " + fetchSimpleClassName(element.getClassName()));
            }

            if(element.getMethodName() != null && element.getMethodName().length() != 0) {
               builder.append(" at method: " + element.getMethodName());
            }

            if(element.getLineNumber() > 0) {
               builder.append(" at line: " + element.getLineNumber());
            }
         }

         if(!stringNullOrEmpty(current.getMessage())) {
            builder.append(": \"" + current.getMessage() + "\"");
         }
      } while((current = current.getCause()) != null);

      return builder.toString();
   }

   private static String fetchSimpleClassName(String className) {
      int pos = className.lastIndexOf(46);
      return pos != -1?className.substring(pos + 1):className;
   }

   public static String appendNewlineEachGivenChars(int nlCount, String str) {
      StringBuilder result = new StringBuilder(str);

      for(int breakIdx = nlCount; result.length() > breakIdx; breakIdx += nlCount + 1) {
         result.insert(breakIdx, "\n");
      }

      return result.toString();
   }

   public static boolean isWindowsSystem() {
      return System.getProperty("os.name").startsWith("Windows");
   }
}