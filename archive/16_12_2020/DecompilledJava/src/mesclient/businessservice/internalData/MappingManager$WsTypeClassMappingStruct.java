package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.DataTypes.WebServiceType;

class WsTypeClassMappingStruct {

   public final WebServiceType wsType;
   public final Class clsType;


   public WsTypeClassMappingStruct(Class clsType, WebServiceType wsType) {
      this.clsType = clsType;
      this.wsType = wsType;
   }
}