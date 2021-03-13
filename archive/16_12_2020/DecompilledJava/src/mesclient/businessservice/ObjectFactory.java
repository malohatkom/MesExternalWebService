package de.mpdv.mesclient.businessservice;

import de.mpdv.mesclient.businessservice.AuthToken;
import de.mpdv.mesclient.businessservice.BinaryColumn;
import de.mpdv.mesclient.businessservice.BooleanColumn;
import de.mpdv.mesclient.businessservice.ColumnMeta;
import de.mpdv.mesclient.businessservice.DataWrapper;
import de.mpdv.mesclient.businessservice.DateTimeColumn;
import de.mpdv.mesclient.businessservice.DecimalColumn;
import de.mpdv.mesclient.businessservice.GenericParam;
import de.mpdv.mesclient.businessservice.GenericResultSet;
import de.mpdv.mesclient.businessservice.InfoData;
import de.mpdv.mesclient.businessservice.InfoMessage;
import de.mpdv.mesclient.businessservice.InputColumnMeta;
import de.mpdv.mesclient.businessservice.InputRow;
import de.mpdv.mesclient.businessservice.IntegerColumn;
import de.mpdv.mesclient.businessservice.Interact;
import de.mpdv.mesclient.businessservice.InteractBatch;
import de.mpdv.mesclient.businessservice.InteractBatchResponse;
import de.mpdv.mesclient.businessservice.InteractMaintMgr;
import de.mpdv.mesclient.businessservice.InteractMaintMgrResponse;
import de.mpdv.mesclient.businessservice.InteractMii;
import de.mpdv.mesclient.businessservice.InteractMiiResponse;
import de.mpdv.mesclient.businessservice.InteractOptimized;
import de.mpdv.mesclient.businessservice.InteractOptimizedBatch;
import de.mpdv.mesclient.businessservice.InteractOptimizedBatchResponse;
import de.mpdv.mesclient.businessservice.InteractOptimizedPin;
import de.mpdv.mesclient.businessservice.InteractOptimizedPinResponse;
import de.mpdv.mesclient.businessservice.InteractOptimizedResponse;
import de.mpdv.mesclient.businessservice.InteractPin;
import de.mpdv.mesclient.businessservice.InteractPinResponse;
import de.mpdv.mesclient.businessservice.InteractResponse;
import de.mpdv.mesclient.businessservice.NullColumn;
import de.mpdv.mesclient.businessservice.ResultItem;
import de.mpdv.mesclient.businessservice.ResultItemMii;
import de.mpdv.mesclient.businessservice.ResultStruct;
import de.mpdv.mesclient.businessservice.Row;
import de.mpdv.mesclient.businessservice.Segment;
import de.mpdv.mesclient.businessservice.ServiceEnvironment;
import de.mpdv.mesclient.businessservice.StringColumn;
import de.mpdv.mesclient.businessservice.XmlResultItem;
import de.mpdv.mesclient.businessservice.XmlResultSet;
import de.mpdv.mesclient.businessservice.XmlResultStruct;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

   private static final QName _InteractOptimizedResponse_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactOptimizedResponse");
   private static final QName _InteractBatchResponse_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactBatchResponse");
   private static final QName _InteractResponse_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactResponse");
   private static final QName _InteractMaintMgr_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactMaintMgr");
   private static final QName _InteractOptimizedBatchResponse_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactOptimizedBatchResponse");
   private static final QName _InteractMaintMgrResponse_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactMaintMgrResponse");
   private static final QName _InteractMiiResponse_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactMiiResponse");
   private static final QName _InteractOptimizedPinResponse_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactOptimizedPinResponse");
   private static final QName _InteractMii_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactMii");
   private static final QName _InteractBatch_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactBatch");
   private static final QName _InteractPin_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactPin");
   private static final QName _InteractPinResponse_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactPinResponse");
   private static final QName _InteractOptimized_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactOptimized");
   private static final QName _Interact_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interact");
   private static final QName _InteractOptimizedPin_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactOptimizedPin");
   private static final QName _BusinessException_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "BusinessException");
   private static final QName _InteractOptimizedBatch_QNAME = new QName("http://businessService.mesClient.mpdv.de/", "interactOptimizedBatch");


   public InteractMiiResponse createInteractMiiResponse() {
      return new InteractMiiResponse();
   }

   public GenericParam createGenericParam() {
      return new GenericParam();
   }

   public InteractPin createInteractPin() {
      return new InteractPin();
   }

   public InteractMaintMgrResponse createInteractMaintMgrResponse() {
      return new InteractMaintMgrResponse();
   }

   public InfoMessage createInfoMessage() {
      return new InfoMessage();
   }

   public NullColumn createNullColumn() {
      return new NullColumn();
   }

   public InteractMii createInteractMii() {
      return new InteractMii();
   }

   public InteractBatchResponse createInteractBatchResponse() {
      return new InteractBatchResponse();
   }

   public DateTimeColumn createDateTimeColumn() {
      return new DateTimeColumn();
   }

   public ResultItemMii createResultItemMii() {
      return new ResultItemMii();
   }

   public XmlResultStruct createXmlResultStruct() {
      return new XmlResultStruct();
   }

   public InteractOptimizedBatch createInteractOptimizedBatch() {
      return new InteractOptimizedBatch();
   }

   public GenericResultSet createGenericResultSet() {
      return new GenericResultSet();
   }

   public ResultStruct createResultStruct() {
      return new ResultStruct();
   }

   public XmlResultItem createXmlResultItem() {
      return new XmlResultItem();
   }

   public InteractBatch createInteractBatch() {
      return new InteractBatch();
   }

   public InteractOptimizedPinResponse createInteractOptimizedPinResponse() {
      return new InteractOptimizedPinResponse();
   }

   public Interact createInteract() {
      return new Interact();
   }

   public InteractOptimized createInteractOptimized() {
      return new InteractOptimized();
   }

   public IntegerColumn createIntegerColumn() {
      return new IntegerColumn();
   }

   public InteractOptimizedBatchResponse createInteractOptimizedBatchResponse() {
      return new InteractOptimizedBatchResponse();
   }

   public Segment createSegment() {
      return new Segment();
   }

   public ResultItem createResultItem() {
      return new ResultItem();
   }

   public Row createRow() {
      return new Row();
   }

   public InteractOptimizedPin createInteractOptimizedPin() {
      return new InteractOptimizedPin();
   }

   public BooleanColumn createBooleanColumn() {
      return new BooleanColumn();
   }

   public StringColumn createStringColumn() {
      return new StringColumn();
   }

   public InputColumnMeta createInputColumnMeta() {
      return new InputColumnMeta();
   }

   public AuthToken createAuthToken() {
      return new AuthToken();
   }

   public InputRow createInputRow() {
      return new InputRow();
   }

   public XmlResultSet createXmlResultSet() {
      return new XmlResultSet();
   }

   public ColumnMeta createColumnMeta() {
      return new ColumnMeta();
   }

   public ServiceEnvironment createServiceEnvironment() {
      return new ServiceEnvironment();
   }

   public DecimalColumn createDecimalColumn() {
      return new DecimalColumn();
   }

   public InteractPinResponse createInteractPinResponse() {
      return new InteractPinResponse();
   }

   public InfoData createInfoData() {
      return new InfoData();
   }

   public InteractResponse createInteractResponse() {
      return new InteractResponse();
   }

   public InteractOptimizedResponse createInteractOptimizedResponse() {
      return new InteractOptimizedResponse();
   }

   public DataWrapper createDataWrapper() {
      return new DataWrapper();
   }

   public InteractMaintMgr createInteractMaintMgr() {
      return new InteractMaintMgr();
   }

   public BinaryColumn createBinaryColumn() {
      return new BinaryColumn();
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactOptimizedResponse"
   )
   public JAXBElement createInteractOptimizedResponse(InteractOptimizedResponse value) {
      return new JAXBElement(_InteractOptimizedResponse_QNAME, InteractOptimizedResponse.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactBatchResponse"
   )
   public JAXBElement createInteractBatchResponse(InteractBatchResponse value) {
      return new JAXBElement(_InteractBatchResponse_QNAME, InteractBatchResponse.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactResponse"
   )
   public JAXBElement createInteractResponse(InteractResponse value) {
      return new JAXBElement(_InteractResponse_QNAME, InteractResponse.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactMaintMgr"
   )
   public JAXBElement createInteractMaintMgr(InteractMaintMgr value) {
      return new JAXBElement(_InteractMaintMgr_QNAME, InteractMaintMgr.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactOptimizedBatchResponse"
   )
   public JAXBElement createInteractOptimizedBatchResponse(InteractOptimizedBatchResponse value) {
      return new JAXBElement(_InteractOptimizedBatchResponse_QNAME, InteractOptimizedBatchResponse.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactMaintMgrResponse"
   )
   public JAXBElement createInteractMaintMgrResponse(InteractMaintMgrResponse value) {
      return new JAXBElement(_InteractMaintMgrResponse_QNAME, InteractMaintMgrResponse.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactMiiResponse"
   )
   public JAXBElement createInteractMiiResponse(InteractMiiResponse value) {
      return new JAXBElement(_InteractMiiResponse_QNAME, InteractMiiResponse.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactOptimizedPinResponse"
   )
   public JAXBElement createInteractOptimizedPinResponse(InteractOptimizedPinResponse value) {
      return new JAXBElement(_InteractOptimizedPinResponse_QNAME, InteractOptimizedPinResponse.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactMii"
   )
   public JAXBElement createInteractMii(InteractMii value) {
      return new JAXBElement(_InteractMii_QNAME, InteractMii.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactBatch"
   )
   public JAXBElement createInteractBatch(InteractBatch value) {
      return new JAXBElement(_InteractBatch_QNAME, InteractBatch.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactPin"
   )
   public JAXBElement createInteractPin(InteractPin value) {
      return new JAXBElement(_InteractPin_QNAME, InteractPin.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactPinResponse"
   )
   public JAXBElement createInteractPinResponse(InteractPinResponse value) {
      return new JAXBElement(_InteractPinResponse_QNAME, InteractPinResponse.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactOptimized"
   )
   public JAXBElement createInteractOptimized(InteractOptimized value) {
      return new JAXBElement(_InteractOptimized_QNAME, InteractOptimized.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interact"
   )
   public JAXBElement createInteract(Interact value) {
      return new JAXBElement(_Interact_QNAME, Interact.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactOptimizedPin"
   )
   public JAXBElement createInteractOptimizedPin(InteractOptimizedPin value) {
      return new JAXBElement(_InteractOptimizedPin_QNAME, InteractOptimizedPin.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "BusinessException"
   )
   public JAXBElement createBusinessException(InfoData value) {
      return new JAXBElement(_BusinessException_QNAME, InfoData.class, (Class)null, value);
   }

   @XmlElementDecl(
      namespace = "http://businessService.mesClient.mpdv.de/",
      name = "interactOptimizedBatch"
   )
   public JAXBElement createInteractOptimizedBatch(InteractOptimizedBatch value) {
      return new JAXBElement(_InteractOptimizedBatch_QNAME, InteractOptimizedBatch.class, (Class)null, value);
   }

}