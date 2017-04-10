package com.toast.foldlerwatch.oasisreport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OasisReport implements Comparable<OasisReport>
{
   public OasisReport()
   {
      
   }
   
   public boolean parse(File file) throws IOException
   {
      boolean success = true;
      
      // Open the file
      FileInputStream fileInputStream = new FileInputStream(file);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

      String line;

      // Read file, line by line.
      while ((line = bufferedReader.readLine()) != null)
      {
         try
         {
            success &= parse(line);
         }
         catch (Exception e)
         {
            System.out.format("Failed to parse line in file [%s]: \"%s\"", file.toString(), line);
         }
         
         if (!success)
         {
            System.out.format("Failed to parse line in file [%s]: \"%s\"", file.toString(), line);
            break;
         }
      }
      
      bufferedReader.close();
      
      return (success);
   }
   
   public String toText()
   {
      String text = "";
      
      return (text);
   }
   
   public String toHtml()
   {
      final String USER_FIELD_DIV = "<div id=\"user-field-container\">";
      final String PART_INSPECTION_DIV = "<div id=\"part-inspection-container\">";
      
      String html = "";
      
      try
      {
         html = getReportTemplate();
         
         StringBuilder stringBuilder = new StringBuilder(html);
         
         int pos = stringBuilder.indexOf(USER_FIELD_DIV) + USER_FIELD_DIV.length() + 1;  // +1 for \n
         
         stringBuilder.insert(pos, userFieldsToHtml());
         
         pos = stringBuilder.indexOf(PART_INSPECTION_DIV) + PART_INSPECTION_DIV.length() + 1;  // +1 for \n
         
         stringBuilder.insert(pos, partInspectionsToHtml());
         
         html = stringBuilder.toString();
      }
      catch (IOException e)
      {
         
      }
      
      return (html);
   }
   
   public UserField getUserField(UserFieldType fieldType)
   {
      UserField field = null;
      
      if (userFields.containsKey(fieldType.ordinal()))
      {
         field = userFields.get(fieldType.ordinal());
      }
      
      return (field);
   }
         
   public String getEmployeeNumber()
   {
      String value = "";
      
      if (userFields.containsKey(UserFieldType.EMPLOYEE_NUMBER.ordinal()))
      {
         value = userFields.get(UserFieldType.EMPLOYEE_NUMBER.ordinal()).getValue();
      }
      
      return (value);
   }
   
   public int getPartCount()
   {
      int value = 0;
      
      if (userFields.containsKey(UserFieldType.PART_COUNT.ordinal()))
      {
         try
         {
            value = Integer.parseInt(userFields.get(UserFieldType.PART_COUNT.ordinal()).getValue());
         }
         catch (NumberFormatException e)
         {
            value = 0;
         }
      }
      
      return (value);
   }
   
   public int getSampleSize()
   {
      int value = 0;
      
      if (userFields.containsKey(UserFieldType.SAMPLE_SIZE.ordinal()))
      {
         try
         {
            value = Integer.parseInt(userFields.get(UserFieldType.SAMPLE_SIZE.ordinal()).getValue());
         }
         catch (NumberFormatException e)
         {
            value = 0;
         }
      }
      
      return (value);
   }
   
   public String getMachineNumber()
   {
      String value = "";
      
      if (userFields.containsKey(UserFieldType.MACHINE_NUMBER.ordinal()))
      {
         value = userFields.get(UserFieldType.MACHINE_NUMBER.ordinal()).getValue();
      }
      
      return (value);
   }
   
   public Date getDate()
   {
      Date date = null;
      
      PartInspection inspection = inspections.get(0);
      
      // Our most precise date can be pulled out of the first PartInspection object.
      if (inspection != null)
      {
         date = inspection.getDate();
      }
      // After that, use the date in the user fields.
      else
      {
         try
         {
            if (userFields.containsKey(UserFieldType.DATE.ordinal()))
            {
               String value = userFields.get(UserFieldType.DATE.ordinal()).getValue();
               
               DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
               date = format.parse(value);
            }
         }
         catch (ParseException e)
         {
            date = null;
         }      
      }
      
      return (date);
   }
   
   public String getPartNumber()
   {
      String value = "";
      
      if (userFields.containsKey(UserFieldType.PART_NUMBER.ordinal()))
      {
         value = userFields.get(UserFieldType.PART_NUMBER.ordinal()).getValue();
         
         // String generally looks like this: 
         // M8206 Rev 10
         value = value.substring(0, value.indexOf(" "));
      }
      
      return (value);
   }
   
   public Double getEfficiency()
   {
      Double value = 0.0;
      
      if (userFields.containsKey(UserFieldType.EFFICIENCY.ordinal()))
      {
         String string = userFields.get(UserFieldType.EFFICIENCY.ordinal()).getValue();
         
         // String generally looks like this:
         // 77%
         string = string.replaceAll("[^\\d.]", "");
         
         // Convert to Double.
         try
         {
            value = Double.parseDouble(string);
         }
         catch (NumberFormatException e)
         {
            value = 0.0;
         }
      }
      
      return (value);
   }
   
   public int getFailureCount()
   {
      int failureCount = 0;
      
      for (PartInspection inspection : inspections)
      {
         failureCount += inspection.getFailureCount();
      }
      
      return (failureCount);
   }
   
   public String getComments()
   {
      String value = "";
      
      if (userFields.containsKey(UserFieldType.COMMENTS.ordinal()))
      {
         value = userFields.get(UserFieldType.COMMENTS.ordinal()).getValue();
      }
      
      return (value);      
   }
   
   private boolean parse(String line)
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
                     // Parse error!
                     success = false;
                  }
                  else
                  {
                     partInspection = new PartInspection();
                     success = partInspection.parse(line);
                  }
                  break;
               }
               
               case PART_INSPECTION_DATA:
               {
                  if (partInspection == null)
                  {
                     // Parse error!
                     success = false;
                  }
                  else
                  {
                     success = partInspection.parse(line);
                  }
                  break;
               }
               
               case PART_INSPECTION_END:
               {
                  if (partInspection == null)
                  {
                     // Parse error!
                     success = false;
                  }
                  else if (!partInspection.parse(line))
                  {
                     // Parse error!
                     success = false;                     
                  }
                  else
                  {
                     inspections.add(partInspection);
                     partInspection = null;
                  }
                  break;
               }
               
               case USER_FIELD_LABEL:
               {
                  int userFieldIndex = Integer.valueOf(tokens[0].replaceAll("[^0-9]", "")) - 1;
                  
                  if (!userFields.containsKey(userFieldIndex))
                  {
                     userFields.put(userFieldIndex,  new UserField());
                  }
                  
                  success = userFields.get(userFieldIndex).parse(line);
                  break;
               }
               
               case USER_FIELD_VALUE:
               {
                  int userFieldIndex = Integer.valueOf(tokens[0].replaceAll("[^0-9]", "")) - 1;
                  
                  if (!userFields.containsKey(userFieldIndex))
                  {
                     userFields.put(userFieldIndex,  new UserField());
                  }
                  
                  success = userFields.get(userFieldIndex).parse(line);
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
   
   private String userFieldsToHtml()
   {
      final UserFieldType[] USER_FIELDS = 
      {
         UserFieldType.DATE,            UserFieldType.MACHINE_NUMBER, UserFieldType.PART_COUNT,
         null,                          UserFieldType.PART_NUMBER,    null,
         UserFieldType.EMPLOYEE_NUMBER, UserFieldType.SAMPLE_SIZE,    UserFieldType.EFFICIENCY
      };

      final int TIME_INDEX = 3;

      final int FAILURE_COUNT_INDEX = 5;
      
      String html = "";
      
      // ------------------------------------------------
      // | Date           | Machine #      | Part Count | 
      // -----------------------------------------------|
      // | Time           | Part #         | Failure Ct |
      // -----------------------------------------------|
      // | Employee #     | Sample Size    | Efficiency |
      // -----------------------------------------------|
      //
      // Comments:
      
      html += "<h2>Summary</h2>\n";
      
      html += "<table class=\"user-field-table\">\n";
      
      int index = 0;
      for (UserFieldType fieldType : USER_FIELDS)
      {
         if ((index % 3) == 0)
         {
            html += "<tr>\n";
         }
         
         if (index == FAILURE_COUNT_INDEX)
         {
            int failureCount = getFailureCount();
            String htmlClass = (failureCount > 0) ? "measurement-result-fail" : "measurement-result-pass"; 
            html += "<td><label>Failure count</label></td>\n";
            html += "<td><input type=\"text\" class=\"" + htmlClass + "\" value=\"" + failureCount + "\" disabled/></td>\n";
         }
         else if (index == TIME_INDEX)
         {
            Date date = getDate();
            String dateString = "Unknown";
            if (date != null)
            {
               SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
               dateString = formatter.format(date);
            }

            html += "<td><label>Time</label></td>\n";
            html += "<td><input type=\"text\" value=\"" + dateString + "\" disabled/></td>\n";
         }
         else
         {
            if (fieldType != null)
            {
               UserField field = getUserField(fieldType);
   
               String value = "";
               if (field != null)
               {
                  value = field.getValue();
               }
               
               html += "<td><label>" + fieldType.getLabel() + "</label></td>\n";
               html += "<td><input type=\"text\" disabled value=\"" + value + "\"/></td>\n";
            }
            else
            {
               html += "<td></td><td></td>\n";
            }
         }
         
         if (((index + 1) % 3) == 0)
         {
            html += "</tr>\n";
         }
         
         index++;
      }
      
      html += "</table>\n";
      
      html += "<div>\n" +
              "<div><label>Comments</label></div>\n" +
              "<div><textarea disabled>" + getComments() + "</textarea></div>\n" +
              "</div>\n";
      
      html += "</table>\n";
      
      return (html);
   }
   
   private String partInspectionsToHtml()
   {
      String html = "";
      
      int index = 1;
      for (PartInspection inspection : inspections)
      {
         html += "<h2>Sample " + index + "</h2>\n";
         
         html += inspection.toHtml();
         
         html += "<br/>";
         
         index++;
      }
      
      return (html);
   }
   
   private String getReportTemplate() throws IOException
   {
      final String TEMPLATE_FILENAME = "template.html"; 
      
      String html = "";
      
      // Open a report file.
      File file = new File(TEMPLATE_FILENAME);
      
      // Create a BufferedReader object.
      FileInputStream fileInputStream = new FileInputStream(file);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
      
      // Read all lines.
      String line = bufferedReader.readLine(); 
      StringBuilder stringBuilder = new StringBuilder(); 
      while(line != null)
      {
         stringBuilder.append(line).append("\n"); 
         line = bufferedReader.readLine(); 
      }
      
      bufferedReader.close();
      
      html = stringBuilder.toString();
      
      return (html);
   }
   
   private Map<Integer, UserField> userFields = new HashMap<Integer, UserField>();
   
   private List<PartInspection> inspections = new ArrayList<PartInspection>();
   
   private PartInspection partInspection;

   @Override
   public int compareTo(OasisReport rhs)
   {
      long lhsDate = getDate().getTime();
      long rhsDate = rhs.getDate().getTime();
      
      return ((lhsDate > rhsDate) ? 1 : (lhsDate < rhsDate) ? -1 : 0);
   }
}
