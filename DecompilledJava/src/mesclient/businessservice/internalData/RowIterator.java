package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.mesclient.businessservice.internalData.IDataTable;
import de.mpdv.mesclient.businessservice.internalData.IRowIterator;

class RowIterator implements IRowIterator {

   private final IDataTable dataTable;
   private int position;


   public RowIterator(IDataTable dataTable, int position) {
      this.dataTable = dataTable;
      this.position = position;
   }

   public void addRow() {
      this.checkPosition();
      this.dataTable.addEmptyRow(this.position);
   }

   public int currentRowPos() {
      return this.position;
   }

   public Object getCellValue(String colName, Class type) {
      this.checkPosition();
      return this.dataTable.getCellValue(this.position, colName, type);
   }

   public Object getCellValue(int colIdx, Class type) {
      this.checkPosition();
      return this.dataTable.getCellValue(this.position, colIdx, type);
   }

   public boolean next() {
      if(this.position < this.dataTable.getRowCount() - 1) {
         ++this.position;
         return true;
      } else {
         return false;
      }
   }

   public boolean previous() {
      this.checkPosition();
      if(this.position > 0) {
         --this.position;
         return true;
      } else {
         return false;
      }
   }

   public void removeRow() {
      this.checkPosition();
      this.dataTable.removeRow(this.position);
   }

   public void setCellValue(String colName, Class type, Object value) {
      this.checkPosition();
      this.dataTable.setCellValue(this.position, colName, type, value);
   }

   public void setCellValue(int colIdx, Class type, Object value) {
      this.checkPosition();
      this.dataTable.setCellValue(this.position, colIdx, type, value);
   }

   private void checkPosition() {
      if(this.position == -1) {
         throw new IllegalStateException("Rowiterator can not be used before calling next");
      }
   }
}