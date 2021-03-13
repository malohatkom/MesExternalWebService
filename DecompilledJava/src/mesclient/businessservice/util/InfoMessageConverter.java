package de.mpdv.mesclient.businessservice.util;

import de.mpdv.mesclient.businessservice.internalData.DataWrapper;
import de.mpdv.mesclient.businessservice.internalData.InfoMessage;
import de.mpdv.mesclient.businessservice.util.DataWrapperConverter;
import java.util.List;

public class InfoMessageConverter {

   public static InfoMessage convert(de.mpdv.mesclient.businessservice.InfoMessage msg) {
      String langKey = msg.getLangKey();
      List params = msg.getParams();
      DataWrapper[] newParams = null;
      if(params != null) {
         newParams = new DataWrapper[params.size()];
         int count = params.size();

         for(int i = 0; i < count; ++i) {
            de.mpdv.mesclient.businessservice.DataWrapper wrapper = (de.mpdv.mesclient.businessservice.DataWrapper)params.get(i);
            DataWrapper newWrapper = null;
            if(wrapper != null) {
               newWrapper = DataWrapperConverter.convert(wrapper);
            }

            newParams[i] = newWrapper;
         }
      }

      return new InfoMessage(langKey, newParams);
   }
}