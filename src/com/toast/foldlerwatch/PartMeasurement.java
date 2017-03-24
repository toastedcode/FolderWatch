package com.toast.foldlerwatch;

import java.util.HashMap;
import java.util.Map;

public class PartMeasurement
{
   public boolean parse(String string)
   {
      boolean success = false;
      
      // Oasis measurement format:
      // DATA|thd lgh|0.6600|0.6620|0.6626|0.6980|0.7000|PASS
      
      String[] tokens = string.split("\\|");
      
      if (tokens[DATA_INDEX].equals("DATA"))
      {
         for (int i = NAME_INDEX; i <= RESULT_INDEX; i++)
         {
            if (i == NAME_INDEX)
            {
               name = tokens[i];
            }
            else if (i == RESULT_INDEX)
            {
               result = MeasurementResult.valueOfToken(tokens[i]);
            }
            else
            {
               MeasurementType type = MeasurementType.values()[(i - FIRST_MEASUREMENT_INDEX)];
               data.put(type, Double.valueOf(tokens[i]));
            }
         }
         
         success = true;
      }
      
      return (success);
   }
   
   public boolean isValid()
   {
      return (isValid);
   }
   
   public String getName()
   {
      return (name);
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
   
   private final int DATA_INDEX = 0;
   
   private final int NAME_INDEX = 1;
   
   private final int FIRST_MEASUREMENT_INDEX = 2;
   
   private final int RESULT_INDEX = 7;
   
   private boolean isValid = false;
   
   private String name = "";
   
   private Map<MeasurementType, Double> data = new HashMap<MeasurementType, Double>();
   
   private MeasurementResult result = MeasurementResult.FAIL_HIGH;
}
