package de.mpdv.mesclient.businessservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "authToken",
   propOrder = {"token"}
)
public class AuthToken {

   protected byte[] token;


   public byte[] getToken() {
      return this.token;
   }

   public void setToken(byte[] value) {
      this.token = (byte[])value;
   }
}