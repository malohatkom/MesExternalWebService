package de.mpdv.mesclient.businessservice.util;

import de.mpdv.mesclient.businessservice.InfoData;
import de.mpdv.mesclient.businessservice.internalData.BusinessException;
import de.mpdv.mesclient.businessservice.util.InfoDataConverter;

public class BusinessExceptionConverter {

   public static BusinessException convert(de.mpdv.mesclient.businessservice.BusinessException exc) {
      String message = exc.getMessage();
      InfoData data = exc.getFaultInfo();
      Object newInfoData = null;
      if(data != null) {
         InfoDataConverter.convert(data);
      }

      return new BusinessException(message, (de.mpdv.mesclient.businessservice.internalData.InfoData)newInfoData);
   }
}