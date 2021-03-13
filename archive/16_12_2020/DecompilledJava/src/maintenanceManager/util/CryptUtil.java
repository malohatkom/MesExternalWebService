package de.mpdv.maintenanceManager.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptUtil {

   public static String getMd5HashString(String input) throws NoSuchAlgorithmException {
      MessageDigest m = MessageDigest.getInstance("MD5");
      m.reset();
      m.update(input.getBytes());
      byte[] digest = m.digest();
      BigInteger bigInt = new BigInteger(1, digest);

      String hashtext;
      for(hashtext = bigInt.toString(16); hashtext.length() < 32; hashtext = "0" + hashtext) {
         ;
      }

      return hashtext;
   }
}