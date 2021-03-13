package de.mpdv.mesclient.businessservice.internalData;


class MutableInteger {

   int intValue;


   public MutableInteger(int intValue) {
      this.intValue = intValue;
   }

   public String toString() {
      return "intValue=" + this.intValue;
   }
}