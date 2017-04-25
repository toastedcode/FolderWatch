package com.toast.foldlerwatch.summaryreport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.toast.foldlerwatch.oasisreport.OasisReport;

public class SummaryReport
{
   
   public SummaryReport()
   {
   }
   
   public void addReport(OasisReport report)
   {
      String employeeNumber = report.getEmployeeNumber();
      
      if (!operatorSummaries.containsKey(employeeNumber))
      {
         operatorSummaries.put(employeeNumber, new OperatorSummary());
      }
      
      operatorSummaries.get(employeeNumber).addReport(report);
   }
   
   public int size()
   {
      return (operatorSummaries.size());
   }
   
   public String toText()
   {
      String text = "";
      
      return (text);
   }
   
   public String toHtml()
   {
      final String OPERATOR_SUMMARIES_DIV = "<div id=\"operator-summaries-container\">";
      
      String html = "";
      
      try
      {
         // 
         // Compile operator summaries
         //
         
         String operatorSummariesHtml = "";
         
         int index = 0;
         for (OperatorSummary summary : operatorSummaries.values())
         {
            operatorSummariesHtml += summary.toHtml();
            
            if (index < (operatorSummaries.size() - 1))
            {
               operatorSummariesHtml += "<br/><hr><br/>";
            }
            
            index++;
         }
         
         // 
         // Insert into template
         //
         
         html = getReportTemplate();
         
         StringBuilder stringBuilder = new StringBuilder(html);
         
         int pos = stringBuilder.indexOf(OPERATOR_SUMMARIES_DIV) + OPERATOR_SUMMARIES_DIV.length() + 1;  // +1 for \n
         
         stringBuilder.insert(pos, operatorSummariesHtml);
         
         html = stringBuilder.toString();
      }
      catch (IOException e)
      {
         
      }
      
      return (html);
   }
   
   private String getReportTemplate() throws IOException
   {
      final String TEMPLATE_FILENAME = "summaryReportTemplate.html"; 
      
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
   
   private Map<String, OperatorSummary> operatorSummaries =  new HashMap<String, OperatorSummary>();
}
