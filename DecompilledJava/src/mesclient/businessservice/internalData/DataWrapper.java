package de.mpdv.mesclient.businessservice.internalData;

import de.mpdv.maintenanceManager.util.DateTimeUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.XMLGregorianCalendar;

public class DataWrapper implements Comparable, Serializable {

   private static final long serialVersionUID = -5700051529195931857L;
   private String dataType;
   private String str;
   private XMLGregorianCalendar dt;
   private BigDecimal dec;
   private Integer num;
   private Boolean bool;
   private byte[] bin;
   private String[] strAr;
   private XMLGregorianCalendar[] dtAr;
   private BigDecimal[] decAr;
   private Integer[] numAr;
   private Boolean[] boolAr;
   private byte[][] binAr;
   private Calendar[] calAr;
   private Calendar cal;


   public DataWrapper() {}

   public DataWrapper(DataWrapper dw) {
      this.bin = dw.getBin();
      this.binAr = dw.getBinAr();
      this.bool = dw.getBool();
      this.boolAr = dw.getBoolAr();
      this.dataType = dw.getDataType();
      this.dec = dw.getDec();
      this.decAr = dw.getDecAr();
      this.dt = dw.getDt();
      this.dtAr = dw.getDtAr();
      this.num = dw.getNum();
      this.numAr = dw.getNumAr();
      this.str = dw.getStr();
      this.strAr = dw.getStrAr();
      this.cal = dw.getCal();
      this.calAr = dw.getCalAr();
   }

   public DataWrapper(String data) {
      this.str = data;
      this.dataType = "string";
   }

   public DataWrapper(Calendar data) {
      this.cal = data;
      this.dataType = "datetime";
   }

   public DataWrapper(XMLGregorianCalendar data) {
      this.dt = data;
      this.dataType = "datetime";
   }

   public DataWrapper(BigDecimal data) {
      this.dec = data;
      this.dataType = "decimal";
   }

   public DataWrapper(Integer data) {
      this.num = data;
      this.dataType = "integer";
   }

   public DataWrapper(Boolean data) {
      this.bool = data;
      this.dataType = "boolean";
   }

   public DataWrapper(byte[] data) {
      this.bin = data;
      this.dataType = "binary";
   }

   public DataWrapper(String[] data) {
      this.strAr = data;
      this.dataType = "string[]";
   }

   public DataWrapper(Calendar[] data) {
      this.calAr = data;
      this.dataType = "datetime[]";
   }

   public DataWrapper(XMLGregorianCalendar[] data) {
      this.dtAr = data;
      this.dataType = "datetime[]";
   }

   public DataWrapper(BigDecimal[] data) {
      this.decAr = data;
      this.dataType = "decimal[]";
   }

   public DataWrapper(Integer[] data) {
      this.numAr = data;
      this.dataType = "integer[]";
   }

   public DataWrapper(Boolean[] data) {
      this.boolAr = data;
      this.dataType = "boolean[]";
   }

   public DataWrapper(byte[][] data) {
      this.binAr = data;
      this.dataType = "binary[]";
   }

   @XmlElement(
      name = "t"
   )
   public String getDataType() {
      return this.dataType;
   }

   public void setDataType(String dataType) {
      this.dataType = dataType;
   }

   @XmlElement(
      name = "s"
   )
   public String getStr() {
      return this.str;
   }

   public void setStr(String str) {
      this.str = str;
      this.dataType = "string";
   }

   @XmlSchemaType(
      name = "dateTime"
   )
   @XmlElement(
      name = "d"
   )
   public XMLGregorianCalendar getDt() {
      return this.dt;
   }

   public void setDt(XMLGregorianCalendar dt) {
      this.dt = dt;
      this.dataType = "datetime";
   }

   @XmlElement(
      name = "f"
   )
   public BigDecimal getDec() {
      return this.dec;
   }

   public void setDec(BigDecimal dec) {
      this.dec = dec;
      this.dataType = "decimal";
   }

   @XmlElement(
      name = "n"
   )
   public Integer getNum() {
      return this.num;
   }

   public void setNum(Integer num) {
      this.num = num;
      this.dataType = "integer";
   }

   @XmlElement(
      name = "b"
   )
   public Boolean getBool() {
      return this.bool;
   }

   public void setBool(Boolean bool) {
      this.bool = bool;
      this.dataType = "boolean";
   }

   @XmlElement(
      name = "bn"
   )
   public byte[] getBin() {
      return this.bin;
   }

   public void setBin(byte[] bin) {
      this.bin = bin;
      this.dataType = "binary";
   }

   @XmlElement(
      name = "sa"
   )
   public String[] getStrAr() {
      return this.strAr;
   }

   public void setStrAr(String[] strAr) {
      this.strAr = strAr;
      this.dataType = "string[]";
   }

   @XmlSchemaType(
      name = "dateTime"
   )
   @XmlElement(
      name = "da"
   )
   public XMLGregorianCalendar[] getDtAr() {
      return this.dtAr;
   }

   public void setDtAr(XMLGregorianCalendar[] dtAr) {
      this.dtAr = dtAr;
      this.dataType = "datetime[]";
   }

   @XmlElement(
      name = "fa"
   )
   public BigDecimal[] getDecAr() {
      return this.decAr;
   }

   public void setDecAr(BigDecimal[] decAr) {
      this.decAr = decAr;
      this.dataType = "decimal[]";
   }

   @XmlElement(
      name = "na"
   )
   public Integer[] getNumAr() {
      return this.numAr;
   }

   public void setNumAr(Integer[] numAr) {
      this.numAr = numAr;
      this.dataType = "integer[]";
   }

   @XmlElement(
      name = "ba"
   )
   public Boolean[] getBoolAr() {
      return this.boolAr;
   }

   public void setBoolAr(Boolean[] boolAr) {
      this.boolAr = boolAr;
      this.dataType = "boolean[]";
   }

   @XmlElement(
      name = "bna"
   )
   public byte[][] getBinAr() {
      return this.binAr;
   }

   public void setBinAr(byte[][] binAr) {
      this.binAr = binAr;
      this.dataType = "binary[]";
   }

   @XmlTransient
   public Calendar[] getCalAr() {
      return this.calAr;
   }

   public void setCalAr(Calendar[] calAr) {
      this.calAr = calAr;
   }

   @XmlTransient
   public Calendar getCal() {
      return this.cal;
   }

   public void setCal(Calendar cal) {
      this.cal = cal;
   }

   public String toString() {
      StringBuilder builder;
      int count;
      int i;
      if(this.dataType != null) {
         if(this.bin != null) {
            return this.bin.toString() + " | Datatype = " + this.dataType;
         } else if(this.binAr != null) {
            return Arrays.deepToString(this.binAr) + " | Datatype = " + this.dataType;
         } else if(this.bool != null) {
            return this.bool.toString() + " | Datatype = " + this.dataType;
         } else if(this.boolAr != null) {
            return Arrays.deepToString(this.boolAr) + " | Datatype = " + this.dataType;
         } else if(this.dec != null) {
            return this.dec.toString() + " | Datatype = " + this.dataType;
         } else if(this.decAr != null) {
            return Arrays.deepToString(this.decAr) + " | Datatype = " + this.dataType;
         } else if(this.cal != null) {
            return DateTimeUtil.calendarToPrintString(this.cal) + " | Datatype = " + this.dataType;
         } else if(this.calAr != null) {
            builder = new StringBuilder("[");
            count = this.calAr.length;

            for(i = 0; i < count; ++i) {
               if(i > 0) {
                  builder.append(",");
               }

               builder.append(DateTimeUtil.calendarToPrintString(this.calAr[i]));
            }

            builder.append("]");
            return builder.toString() + " | Datatype = " + this.dataType;
         } else if(this.dt != null) {
            return DateTimeUtil.xmlCalendarToLocalString(this.dt) + " | Datatype = " + this.dataType;
         } else if(this.dtAr != null) {
            builder = new StringBuilder("[");
            count = this.dtAr.length;

            for(i = 0; i < count; ++i) {
               if(i > 0) {
                  builder.append(",");
               }

               builder.append(DateTimeUtil.xmlCalendarToLocalString(this.dtAr[i]));
            }

            builder.append("]");
            return builder.toString() + " | Datatype = " + this.dataType;
         } else {
            return this.num != null?this.num.toString() + " | Datatype = " + this.dataType:(this.numAr != null?Arrays.deepToString(this.numAr) + " | Datatype = " + this.dataType:(this.str != null?this.str + " | Datatype = " + this.dataType:(this.strAr != null?Arrays.deepToString(this.strAr) + " | Datatype = " + this.dataType:"NULL | Datatype = " + this.dataType)));
         }
      } else if(this.bin != null) {
         return this.bin.toString();
      } else if(this.binAr != null) {
         return Arrays.deepToString(this.binAr);
      } else if(this.bool != null) {
         return this.bool.toString();
      } else if(this.boolAr != null) {
         return Arrays.deepToString(this.boolAr);
      } else if(this.dec != null) {
         return this.dec.toString();
      } else if(this.decAr != null) {
         return Arrays.deepToString(this.decAr);
      } else if(this.cal != null) {
         return DateTimeUtil.calendarToPrintString(this.cal);
      } else if(this.calAr != null) {
         builder = new StringBuilder("[");
         count = this.calAr.length;

         for(i = 0; i < count; ++i) {
            if(i > 0) {
               builder.append(",");
            }

            builder.append(DateTimeUtil.calendarToPrintString(this.calAr[i]));
         }

         builder.append("]");
         return builder.toString();
      } else if(this.dt != null) {
         return DateTimeUtil.xmlCalendarToLocalString(this.dt);
      } else if(this.dtAr != null) {
         builder = new StringBuilder("[");
         count = this.dtAr.length;

         for(i = 0; i < count; ++i) {
            if(i > 0) {
               builder.append(",");
            }

            builder.append(DateTimeUtil.xmlCalendarToLocalString(this.dtAr[i]));
         }

         builder.append("]");
         return builder.toString();
      } else {
         return this.num != null?this.num.toString():(this.numAr != null?Arrays.deepToString(this.numAr):(this.str != null?this.str:(this.strAr != null?Arrays.deepToString(this.strAr):"NULL")));
      }
   }

   public int hashCode() {
      boolean prime = true;
      byte result = 1;
      int result1 = 31 * result + Arrays.hashCode(this.bin);
      result1 = 31 * result1 + Arrays.hashCode(this.binAr);
      result1 = 31 * result1 + (this.bool == null?0:this.bool.hashCode());
      result1 = 31 * result1 + Arrays.hashCode(this.boolAr);
      result1 = 31 * result1 + (this.cal == null?0:this.cal.hashCode());
      result1 = 31 * result1 + Arrays.hashCode(this.calAr);
      result1 = 31 * result1 + (this.dataType == null?0:this.dataType.hashCode());
      result1 = 31 * result1 + (this.dec == null?0:this.dec.hashCode());
      result1 = 31 * result1 + Arrays.hashCode(this.decAr);
      result1 = 31 * result1 + (this.dt == null?0:this.dt.hashCode());
      result1 = 31 * result1 + Arrays.hashCode(this.dtAr);
      result1 = 31 * result1 + (this.num == null?0:this.num.hashCode());
      result1 = 31 * result1 + Arrays.hashCode(this.numAr);
      result1 = 31 * result1 + (this.str == null?0:this.str.hashCode());
      result1 = 31 * result1 + Arrays.hashCode(this.strAr);
      return result1;
   }

   public boolean equals(Object obj) {
      if(this == obj) {
         return true;
      } else if(obj == null) {
         return false;
      } else if(this.getClass() != obj.getClass()) {
         return false;
      } else {
         DataWrapper other = (DataWrapper)obj;
         if(!Arrays.equals(this.bin, other.bin)) {
            return false;
         } else if(!Arrays.equals(this.binAr, other.binAr)) {
            return false;
         } else {
            if(this.bool == null) {
               if(other.bool != null) {
                  return false;
               }
            } else if(!this.bool.equals(other.bool)) {
               return false;
            }

            if(!Arrays.equals(this.boolAr, other.boolAr)) {
               return false;
            } else {
               if(this.cal == null) {
                  if(other.cal != null) {
                     return false;
                  }
               } else if(!this.cal.equals(other.cal)) {
                  return false;
               }

               if(!Arrays.equals(this.calAr, other.calAr)) {
                  return false;
               } else {
                  if(this.dataType == null) {
                     if(other.dataType != null) {
                        return false;
                     }
                  } else if(!this.dataType.equals(other.dataType)) {
                     return false;
                  }

                  if(this.dec == null) {
                     if(other.dec != null) {
                        return false;
                     }
                  } else if(!this.dec.equals(other.dec)) {
                     return false;
                  }

                  if(!Arrays.equals(this.decAr, other.decAr)) {
                     return false;
                  } else {
                     if(this.dt == null) {
                        if(other.dt != null) {
                           return false;
                        }
                     } else if(!this.dt.equals(other.dt)) {
                        return false;
                     }

                     if(!Arrays.equals(this.dtAr, other.dtAr)) {
                        return false;
                     } else {
                        if(this.num == null) {
                           if(other.num != null) {
                              return false;
                           }
                        } else if(!this.num.equals(other.num)) {
                           return false;
                        }

                        if(!Arrays.equals(this.numAr, other.numAr)) {
                           return false;
                        } else {
                           if(this.str == null) {
                              if(other.str != null) {
                                 return false;
                              }
                           } else if(!this.str.equals(other.str)) {
                              return false;
                           }

                           return Arrays.equals(this.strAr, other.strAr);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public int compareTo(DataWrapper o) {
      return this.dataType.equals("binary[]")?0:(this.dataType.equals("binary")?0:(this.dataType.equals("boolean[]")?0:(this.dataType.equals("boolean")?this.bool.compareTo(o.getBool()):(this.dataType.equals("datetime[]")?0:(this.dataType.equals("datetime")?this.cal.compareTo(o.getCal()):(this.dataType.equals("decimal[]")?0:(this.dataType.equals("decimal")?this.dec.compareTo(o.getDec()):(this.dataType.equals("integer[]")?0:(this.dataType.equals("integer")?this.num.compareTo(o.getNum()):(this.dataType.equals("string[]")?0:(this.dataType.equals("string")?this.str.compareTo(o.getStr()):0)))))))))));
   }

   public void transformDtInput() {
      if(this.dataType.equals("datetime")) {
         if(this.dt != null) {
            this.cal = this.dt.toGregorianCalendar();
            this.dt = null;
         }
      } else if(this.dataType.equals("datetime[]") && this.dtAr != null) {
         int dtCount = this.dtAr.length;
         this.calAr = new Calendar[dtCount];

         for(int i = 0; i < dtCount; ++i) {
            XMLGregorianCalendar currDt = this.dtAr[i];
            if(currDt != null) {
               this.calAr[i] = currDt.toGregorianCalendar();
            }
         }

         this.dtAr = null;
      }

   }

   public void prepareForTransmission() {
      if(this.dataType.equals("datetime")) {
         if(this.cal != null) {
            this.dt = DateTimeUtil.calendarToXMLCalendar(this.cal);
            this.cal = null;
         }
      } else if(this.dataType.equals("datetime[]") && this.calAr != null) {
         int dtCount = this.calAr.length;
         this.dtAr = new XMLGregorianCalendar[dtCount];

         for(int i = 0; i < dtCount; ++i) {
            Calendar currDt = this.calAr[i];
            if(currDt != null) {
               this.dtAr[i] = DateTimeUtil.calendarToXMLCalendar(currDt);
            }
         }

         this.calAr = null;
      }

   }
}