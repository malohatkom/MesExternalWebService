package de.mpdv.test;

import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.ServiceCallData;
import de.mpdv.mesclient.businessservice.util.ServiceCaller;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServiceCallTest {

   public static void main(String[] args) {
      ServiceCallData data = new ServiceCallData("Softwarestatus.list", (List)null, (List)null, (List)null, (Map)null, UUID.randomUUID().toString(), UUID.randomUUID().toString(), Integer.valueOf(1), "moc-tomcat-01:8085");
      IDataTable table = ServiceCaller.callService(data);
      System.out.println(table.toTabularString());
   }
}