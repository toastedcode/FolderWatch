package com.toast.foldlerwatch.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.toast.foldlerwatch.oasisreport.MeasurementResult;
import com.toast.foldlerwatch.oasisreport.MeasurementType;
import com.toast.foldlerwatch.oasisreport.OasisReport;
import com.toast.foldlerwatch.oasisreport.PartInspection;
import com.toast.foldlerwatch.oasisreport.PartMeasurement;
import com.toast.foldlerwatch.oasisreport.ReportLineType;
import com.toast.foldlerwatch.oasisreport.UserField;

public class RptParser implements Parser
{
   
   @Override
   public void parse(File file, OasisReport report) throws IOException, ParseException
   {
      this.report = report;
      
      // Open the file
      FileInputStream fileInputStream = new FileInputStream(file);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

      String line;

      // Read file, line by line.
      while ((line = bufferedReader.readLine()) != null)
      {
         try
         {
            parse(line);
         }
         catch (Exception e)
         {
            System.out.format("Failed to parse line in file [%s]: \"%s\"", file.toString(), line);
         }
      }
      
      bufferedReader.close();
   }
   
   private boolean parse(String line) throws ParseException
   {
      boolean success = true;
      
      String[] tokens = line.split("\\|");
      
      if (tokens.length > 0)
      {
         ReportLineType lineType = ReportLineType.valueOfToken(tokens[0]);
         
         if (tokens[0].isEmpty())
         {
            // Ignore blank lines.
         }
         else
         {
            switch (lineType)
            {
               case PART_INSPECTION_START:
               {
                  if (partInspection != null)
                  {
                     throw new ParseException();
                  }
                  else
                  {
                     partInspection = new PartInspection();
                     parsePartInspection(line, partInspection);
                  }
                  break;
               }
               
               case PART_INSPECTION_DATA:
               {
                  if (partInspection == null)
                  {
                     throw new ParseException();
                  }
                  else
                  {
                     parsePartInspection(line, partInspection);
                  }
                  break;
               }
               
               case PART_INSPECTION_END:
               {
                  if (partInspection == null)
                  {
                     throw new ParseException();
                  }
                  else
                  {
                     parsePartInspection(line, partInspection);
                     report.addInspection(partInspection);
                     partInspection = null;
                  }
                  break;
               }
               
               case USER_FIELD_LABEL:
               {
                  int index = Integer.valueOf(tokens[0].replaceAll("[^0-9]", "")) - 1;
                  
                  UserField field = report.getUserField(index);
                  
                  if (field != null)
                  {
                     throw new ParseException();
                  }
                  else
                  {
                     field = new UserField();
                     
                     parseUserField(line,  field);
                     
                     report.addUserField(index, field);
                  }
                  break;
               }
               
               case USER_FIELD_VALUE:
               {
                  int index = Integer.valueOf(tokens[0].replaceAll("[^0-9]", "")) - 1;
                  
                  UserField field = report.getUserField(index);
                  
                  if (field == null)
                  {
                     throw new ParseException();
                  }
                  else
                  {
                     parseUserField(line,  field);
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
      else
      {
         // Ignore empty lines.
      }
      
      return (success);      
   }
   
   void parsePartInspection(String line, PartInspection inspection) throws ParseException
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
      
      String[] tokens = line.split("\\|");
      
      if (tokens.length > 0)
      {
         ReportLineType lineType = ReportLineType.valueOfToken(tokens[0]);
         
         if (tokens.length != lineType.getTokenCount())
         {
            throw new ParseException();
         }
         else
         {
            switch (lineType)
            {
               case PART_INSPECTION_START:
               {
                  if (inspection.isValid() == false)
                  {
                     inspection.setDataFile(tokens[1]);
                  }
                  else
                  {
                     throw new ParseException();
                  }
                  break;
               }
               
               case PART_INSPECTION_DATA:
               {
                  if (inspection.isValid() == false)
                  {
                     PartMeasurement measurement = new PartMeasurement();
                     parsePartMeasurement(line, measurement);
                     inspection.addMeasurement(measurement);
                  }
                  else
                  {
                     throw new ParseException();
                  }
                  break;
               }
               
               case PART_INSPECTION_END:
               {
                  if (inspection.isValid() == false)
                  {
                     String dateString = tokens[1] + " " + tokens[2];
                     
                     try
                     {
                        // To match: Mar 21 2017|11:05:43 PM
                        DateFormat format = new SimpleDateFormat("MMM dd yyyy hh:mm:ss a", Locale.ENGLISH);
                        inspection.setDate(format.parse(dateString));
                        inspection.setValid(true);
                     }
                     catch (java.text.ParseException e)
                     {
                        throw new ParseException();
                     }
                  }
                  else
                  {
                     throw new ParseException();
                  }
                  break;
               }
               
               default:
               {
                  throw new ParseException();
               }
            }
         }
      }
   }
   
   void parsePartMeasurement(String line, PartMeasurement measurement) throws ParseException
   {
      // Oasis measurement format:
      // DATA|thd lgh|0.6600|0.6620|0.6626|0.6980|0.7000|PASS
      
      String[] tokens = line.split("\\|");
      
      if (tokens[DATA_INDEX].equals("DATA"))
      {
         for (int i = NAME_INDEX; i <= RESULT_INDEX; i++)
         {
            if (i == NAME_INDEX)
            {
               measurement.setName(tokens[i]);
            }
            else if (i == RESULT_INDEX)
            {
               measurement.setResult(MeasurementResult.valueOfToken(tokens[i]));
            }
            else
            {
               MeasurementType type = MeasurementType.values()[(i - FIRST_MEASUREMENT_INDEX)];
               
               try
               {
                  Double value = Double.parseDouble(tokens[i]);
                  measurement.addValue(type, value);
               }
               catch (NumberFormatException e)
               {
                  measurement.addValue(type,  Double.NaN);
                  throw new ParseException();
               }
            }
         }
      }
   }
   
   void parseUserField(String line, UserField field) throws ParseException
   {
      String[] tokens = line.split("\\|");
      
      if (tokens.length > 0)
      {
         ReportLineType lineType = ReportLineType.valueOfToken(tokens[0]);
         
         switch (lineType)
         {
            case USER_FIELD_LABEL:
            {
               if (tokens.length == 1)
               {
                  // Allow for empty values.
                  field.setLabel("");
               }
               else
               {
                  field.setLabel(tokens[1]);
               }
               break;
            }
            
            case USER_FIELD_VALUE:
            {
               if (tokens.length == 1)
               {
                  // Allow for empty values.
                  field.setValue("");
               }
               else
               {
                  field.setValue(tokens[1]);
               }
               break;
            }
            
            default:
            {
               throw new ParseException();
            }
         }
      }
   }
   
   private final int DATA_INDEX = 0;
   
   private final int NAME_INDEX = 1;
   
   private final int FIRST_MEASUREMENT_INDEX = 2;
   
   private final int RESULT_INDEX = 7;
   
   private PartInspection partInspection = null;
   
   private OasisReport report = null;
}
