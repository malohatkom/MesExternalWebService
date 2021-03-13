package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.MesBusinessService;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

@WebServiceClient(
   name = "MesBusinessServiceService",
   targetNamespace = "http://businessService.mesClient.mpdv.de/",
   wsdlLocation = "http://moc-tomcat-01:8085/MocServices/MesBusinessService?wsdl"
)
public class MesBusinessServiceService extends Service {

   private static final URL MESBUSINESSSERVICESERVICE_WSDL_LOCATION;
   private static final Logger logger = Logger.getLogger(MesBusinessServiceService.class.getName());


   public MesBusinessServiceService(URL wsdlLocation, QName serviceName) {
      super(wsdlLocation, serviceName);
   }

   public MesBusinessServiceService() {
      super(MESBUSINESSSERVICESERVICE_WSDL_LOCATION, new QName("http://businessService.mesClient.mpdv.de/", "MesBusinessServiceService"));
   }

   @WebEndpoint(
      name = "MesBusinessServicePort"
   )
   public MesBusinessService getMesBusinessServicePort() {
      return (MesBusinessService)super.getPort(new QName("http://businessService.mesClient.mpdv.de/", "MesBusinessServicePort"), MesBusinessService.class);
   }

   @WebEndpoint(
      name = "MesBusinessServicePort"
   )
   public MesBusinessService getMesBusinessServicePort(WebServiceFeature ... features) {
      return (MesBusinessService)super.getPort(new QName("http://businessService.mesClient.mpdv.de/", "MesBusinessServicePort"), MesBusinessService.class, features);
   }

   static {
      URL url = null;

      try {
         URL e = MesBusinessServiceService.class.getResource(".");
         url = new URL(e, "http://moc-tomcat-01:8085/MocServices/MesBusinessService?wsdl");
      } catch (MalformedURLException var2) {
         logger.warning("Failed to create URL for the wsdl Location: \'http://moc-tomcat-01:8085/MocServices/MesBusinessService?wsdl\', retrying as a local file");
         logger.warning(var2.getMessage());
      }

      MESBUSINESSSERVICESERVICE_WSDL_LOCATION = url;
   }
}