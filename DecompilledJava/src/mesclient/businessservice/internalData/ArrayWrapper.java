package de.mpdv.mesclient.businessservice.internalData;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.RandomAccess;

public class ArrayWrapper extends AbstractList implements RandomAccess, Serializable {

   private static final long serialVersionUID = 1568117736099564550L;
   private final Object[] wrappedArray;


   public ArrayWrapper(Object[] wrappedArray) {
      this.wrappedArray = wrappedArray;
   }

   public Object get(int index) {
      return this.wrappedArray[index];
   }

   public int size() {
      return this.wrappedArray.length;
   }

   public Object[] toArray() {
      return this.wrappedArray;
   }

   public Object set(int index, Object element) {
      Object previous = this.wrappedArray[index];
      this.wrappedArray[index] = element;
      return previous;
   }

   public Object[] toArray(Object[] a) {
      return (Object[])this.wrappedArray;
   }
}