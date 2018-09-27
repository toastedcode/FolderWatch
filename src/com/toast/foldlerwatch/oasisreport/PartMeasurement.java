package com.toast.foldlerwatch.oasisreport;

import java.util.HashMap;
import java.util.Map;

public class PartMeasurement
{
   
   public boolean isValid()
   {
      return (isValid);
   }
   
   public String getName()
   {
      return (name);
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   public void addValue(MeasurementType type, Double value)
   {
      data.put(type, value);
   }
   
   public Double getValue(MeasurementType type)
   {
      Double value = 0.0;
      
      if (data.containsKey(type))
      {
         value = data.get(type);
      }
      
      return (value);
   }
   
   public MeasurementResult getResult()
   {
      return (result);
   }
   
   public void setResult(MeasurementResult result)
   {
      this.result = result;
   }
   
   public String toHtml()
   {
      String html = "<tr>";
      
      // Name
      html += "<td>" + name + "</td>\n";
      
      // Measurements
      for (MeasurementType measurmentType : MeasurementType.values())
      {
         html += "<td>";
         
         if (data.containsKey(measurmentType))
         {
            html += Double.toString(data.get(measurmentType));
         }
         
         html += "</td>\n";
      }
      
      // Result
      String htmlClass = "";
      if (result.isPassed())
      {
         htmlClass =  "measurement-result-pass";
      }
      else if (result.isWarning())
      {
         htmlClass =  "measurement-result-warning";
      }
      else
      {
         htmlClass =  "measurement-result-fail";         
      }
      html += "<td class=\"" + htmlClass + "\">" + result.getToken() + "</td>\n";
      
      html += "</tr>";
      
      return (html);
   }
   
   private boolean isValid = false;
   
   private String name = "";
   
   private Map<MeasurementType, Double> data = new HashMap<MeasurementType, Double>();
   
   private MeasurementResult result = MeasurementResult.FAIL_HIGH;
}
