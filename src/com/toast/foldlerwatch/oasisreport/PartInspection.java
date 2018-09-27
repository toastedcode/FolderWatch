package com.toast.foldlerwatch.oasisreport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PartInspection
{
   
   public boolean isValid()
   {
      return (isValid);
   }
   
   public void setValid(boolean isValid)
   {
      this.isValid = isValid;
   }
   
   public String getDataFile()
   {
      return (dataFile);
   }
   
   public void setDataFile(String dataFile)
   {
      this.dataFile = dataFile;
   }
   
   public Date getDate()
   {
      return (date);
   }
   
   public void setDate(Date date)
   {
      this.date = date;
   }
   
   public List<PartMeasurement> getMeasurments()
   {
      return (measurements);
   }
   
   public void addMeasurement(PartMeasurement measurement)
   {
      measurements.add(measurement);
   }
   
   public int getFailureCount()
   {
      int failureCount = 0;
      
      for (PartMeasurement measurement : measurements)
      {
         if (measurement.getResult().isFailed())
         {
            failureCount++;
         }
      }
      
      return (failureCount);
   }
   
   public String toHtml()
   {
      String html = "<table class=\"part-inspection-table\">\n";
      
      // Table heading.
      html += "<tr class=\"part-inspection-table-header\">\n";
      html += "<th>Name</th>\n";
      for (MeasurementType measurmentType : MeasurementType.values())
      {
         html += "<th>" + measurmentType.getLabel() + "</th>\n";
      }
      html += "<th>Result</th>\n";
      html += "</tr>\n";
      
      for (PartMeasurement measurement : measurements)
      {
         html += measurement.toHtml();
      }
      
      html += "</table>\n";
      
      return (html);      
   }
      
   private boolean isValid = false;
 
   private String dataFile = "";
   
   private Date date;
   
   private List<PartMeasurement> measurements = new ArrayList<PartMeasurement>();
}
