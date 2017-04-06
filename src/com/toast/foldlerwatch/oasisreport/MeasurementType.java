package com.toast.foldlerwatch.oasisreport;

public enum MeasurementType
{
   LOW_LIMIT("Low Limit"),
   LOW_WARN("Low Warning"),
   MEASURED("Measured"),
   HIGH_WARN("High Warning"),
   HIGH_LIMIT("High Limit");
   
   MeasurementType(String label)
   {
      this.label = label;
   }
   
   public String getLabel()
   {
      return (label);
   }
   
   private String label;
}
