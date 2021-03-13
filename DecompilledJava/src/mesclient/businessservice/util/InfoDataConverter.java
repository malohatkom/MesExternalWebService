package de.mpdv.mesclient.businessservice.util;

import de.mpdv.mesclient.businessservice.internalData.InfoData;
import de.mpdv.mesclient.businessservice.internalData.InfoMessage;
import de.mpdv.mesclient.businessservice.util.InfoMessageConverter;
import java.util.List;

public class InfoDataConverter {

   public static InfoData convert(de.mpdv.mesclient.businessservice.InfoData info) {
      de.mpdv.mesclient.businessservice.InfoData causeInfo = info.getCause();
      InfoData newCauseInfo = null;
      if(causeInfo != null) {
         newCauseInfo = convert(causeInfo);
      }

      String level = info.getLevel();
      List messageList = info.getMessages();
      InfoMessage[] newMessageList = null;
      if(messageList != null) {
         newMessageList = new InfoMessage[messageList.size()];
         int shortMessage = messageList.size();

         for(int stackTrace = 0; stackTrace < shortMessage; ++stackTrace) {
            de.mpdv.mesclient.businessservice.InfoMessage type = (de.mpdv.mesclient.businessservice.InfoMessage)messageList.get(stackTrace);
            InfoMessage newInfo = null;
            if(type != null) {
               newInfo = InfoMessageConverter.convert(type);
            }

            newMessageList[stackTrace] = newInfo;
         }
      }

      String var10 = info.getShortMsg();
      String var11 = info.getStackTrace();
      String var12 = info.getType();
      InfoData var13 = new InfoData();
      var13.setCause(newCauseInfo);
      var13.setLevel(level);
      var13.setMessages(newMessageList);
      var13.setShortMsg(var10);
      var13.setStackTrace(var11);
      var13.setType(var12);
      return var13;
   }
}