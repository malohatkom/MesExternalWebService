package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.BusinessException;
import de.mpdv.mesclient.businessservice.GenericParam;
import de.mpdv.mesclient.businessservice.ObjectFactory;
import de.mpdv.mesclient.businessservice.ResultItem;
import de.mpdv.mesclient.businessservice.ResultItemMii;
import de.mpdv.mesclient.businessservice.ResultStruct;
import de.mpdv.mesclient.businessservice.ServiceEnvironment;
import de.mpdv.mesclient.businessservice.XmlResultItem;
import de.mpdv.mesclient.businessservice.XmlResultStruct;
import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(
   name = "MesBusinessService",
   targetNamespace = "http://businessService.mesClient.mpdv.de/"
)
@XmlSeeAlso({ObjectFactory.class})
public interface MesBusinessService {

   @WebMethod(
      action = "interactBatch"
   )
   @WebResult(
      name = "result",
      targetNamespace = ""
   )
   @RequestWrapper(
      localName = "interactBatch",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractBatch"
   )
   @ResponseWrapper(
      localName = "interactBatchResponse",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractBatchResponse"
   )
   ResultStruct interactBatch(
      @WebParam(
         name = "env",
         targetNamespace = ""
      ) ServiceEnvironment var1, 
      @WebParam(
         name = "param",
         targetNamespace = ""
      ) List var2) throws BusinessException;

   @WebMethod(
      action = "interact"
   )
   @WebResult(
      name = "result",
      targetNamespace = ""
   )
   @RequestWrapper(
      localName = "interact",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.Interact"
   )
   @ResponseWrapper(
      localName = "interactResponse",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractResponse"
   )
   ResultItem interact(
      @WebParam(
         name = "env",
         targetNamespace = ""
      ) ServiceEnvironment var1, 
      @WebParam(
         name = "param",
         targetNamespace = ""
      ) GenericParam var2) throws BusinessException;

   @WebMethod(
      action = "interactOptimizedBatch"
   )
   @WebResult(
      name = "result",
      targetNamespace = ""
   )
   @RequestWrapper(
      localName = "interactOptimizedBatch",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractOptimizedBatch"
   )
   @ResponseWrapper(
      localName = "interactOptimizedBatchResponse",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractOptimizedBatchResponse"
   )
   XmlResultStruct interactOptimizedBatch(
      @WebParam(
         name = "env",
         targetNamespace = ""
      ) ServiceEnvironment var1, 
      @WebParam(
         name = "param",
         targetNamespace = ""
      ) List var2) throws BusinessException;

   @WebMethod(
      action = "interactOptimized"
   )
   @WebResult(
      name = "result",
      targetNamespace = ""
   )
   @RequestWrapper(
      localName = "interactOptimized",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractOptimized"
   )
   @ResponseWrapper(
      localName = "interactOptimizedResponse",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractOptimizedResponse"
   )
   XmlResultItem interactOptimized(
      @WebParam(
         name = "env",
         targetNamespace = ""
      ) ServiceEnvironment var1, 
      @WebParam(
         name = "param",
         targetNamespace = ""
      ) GenericParam var2) throws BusinessException;

   @WebMethod(
      action = "interactMii"
   )
   @WebResult(
      name = "result",
      targetNamespace = ""
   )
   @RequestWrapper(
      localName = "interactMii",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractMii"
   )
   @ResponseWrapper(
      localName = "interactMiiResponse",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractMiiResponse"
   )
   ResultItemMii interactMii(
      @WebParam(
         name = "env",
         targetNamespace = ""
      ) ServiceEnvironment var1, 
      @WebParam(
         name = "param",
         targetNamespace = ""
      ) GenericParam var2);

   @WebMethod
   @WebResult(
      targetNamespace = ""
   )
   @RequestWrapper(
      localName = "interactOptimizedPin",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractOptimizedPin"
   )
   @ResponseWrapper(
      localName = "interactOptimizedPinResponse",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractOptimizedPinResponse"
   )
   XmlResultItem interactOptimizedPin(
      @WebParam(
         name = "arg0",
         targetNamespace = ""
      ) ServiceEnvironment var1, 
      @WebParam(
         name = "arg1",
         targetNamespace = ""
      ) GenericParam var2) throws BusinessException;

   @WebMethod
   @WebResult(
      targetNamespace = ""
   )
   @RequestWrapper(
      localName = "interactPin",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractPin"
   )
   @ResponseWrapper(
      localName = "interactPinResponse",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractPinResponse"
   )
   ResultItem interactPin(
      @WebParam(
         name = "arg0",
         targetNamespace = ""
      ) ServiceEnvironment var1, 
      @WebParam(
         name = "arg1",
         targetNamespace = ""
      ) GenericParam var2) throws BusinessException;

   @WebMethod(
      action = "interactMaintMgr"
   )
   @WebResult(
      name = "result",
      targetNamespace = ""
   )
   @RequestWrapper(
      localName = "interactMaintMgr",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractMaintMgr"
   )
   @ResponseWrapper(
      localName = "interactMaintMgrResponse",
      targetNamespace = "http://businessService.mesClient.mpdv.de/",
      className = "de.mpdv.mesclient.businessservice.InteractMaintMgrResponse"
   )
   ResultItem interactMaintMgr(
      @WebParam(
         name = "env",
         targetNamespace = ""
      ) ServiceEnvironment var1, 
      @WebParam(
         name = "param",
         targetNamespace = ""
      ) GenericParam var2) throws BusinessException;
}