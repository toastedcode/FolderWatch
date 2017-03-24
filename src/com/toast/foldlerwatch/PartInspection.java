package com.toast.foldlerwatch;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PartInspection
{
   public boolean parse(String line)
   {
      // Oasis part inspection format:
      /*
      START|C:\gpc\Oasis\Data\M8206 Rev 10.txt
      DATA|thd lgh|0.6600|0.6620|0.6667|0.6980|0.7000|PASS
      DATA|.75|0.7400|0.7410|0.7485|0.7590|0.7600|PASS
      DATA|.641|0.6310|0.6320|0.6419|0.6500|0.6510|PASS
      DATA|und cut|0.6310|0.6320|0.6426|0.6500|0.6510|PASS
      DATA|major|0.7385|0.7389|0.7454|0.7461|0.7465|PASS
      DATA|pitch|0.7030|0.7032|0.7055|0.7063|0.7065|PASS
      DATA|nose|0.4280|0.4290|0.4414|0.4470|0.4480|PASS
      DATA|45 DEG| 44.50| 44.55| 44.64| 45.45| 45.50|PASS
      DATA|.25 LGH|0.2400|0.2410|0.2476|0.2590|0.2600|PASS
      DATA|OAL|1.5180|1.5185|1.5236|1.5275|1.5280|PASS
      END|Mar 21 2017|11:05:43 PM
       */
      
      boolean success = true;
      
      String[] tokens = line.split("\\|");
      
      if (tokens.length > 0)
      {
         ReportLineType lineType = ReportLineType.valueOfToken(tokens[0]);
         
         if (tokens.length != lineType.getTokenCount())
         {
            // Parse error!
            success = false;
         }
         else
         {
            switch (lineType)
            {
               case PART_INSPECTION_START:
               {
                  if (isValid == false)
                  {
                     dataFile = tokens[1];
                  }
                  else
                  {
                     // Parse error!
                     success = false;
                  }
                  break;
               }
               
               case PART_INSPECTION_DATA:
               {
                  if (isValid == false)
                  {
                     PartMeasurement measurement = new PartMeasurement();
                     if (measurement.parse(line))
                     {
                        measurements.add(measurement);
                     }
                     else
                     {
                        // Parse error!
                        success = false;
                     }
                  }
                  else
                  {
                     // Parse error!
                     success = false;
                  }
                  break;
               }
               
               case PART_INSPECTION_END:
               {
                  if (isValid == false)
                  {
                     String dateString = tokens[1] + " " + tokens[2];
                     
                     try
                     {
                        // To match: Mar 21 2017|11:05:43 PM
                        DateFormat format = new SimpleDateFormat("MMM dd yyyy hh:mm:ss a", Locale.ENGLISH);
                        date = format.parse(dateString);
                        
                        isValid = true;
                     }
                     catch (ParseException e)
                     {
                        // Parse error!
                        success = false;
                     }
                  }
                  else
                  {
                     // Parse error!
                     success = false;
                  }
                  break;
               }
               
               default:
               {
                  // Parse error.
                  success = false;
               }
            }
         }
      }
      
      return (success);
   }
   
   public boolean isValid()
   {
      return (isValid);
   }
   
   public String getDataFile()
   {
      return (dataFile);
   }
   
   public Date getDate()
   {
      return (date);
   }
   
   public List<PartMeasurement> getMeasurments()
   {
      return (measurements);
   }
   
   public int getFailureCount()
   {
      int failureCount = 0;
      
      for (PartMeasurement measurement : measurements)
      {
         if (measurement.getResult() == MeasurementResult.FAIL)
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
