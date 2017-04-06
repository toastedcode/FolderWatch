package com.toast.foldlerwatch.summaryreport;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.toast.foldlerwatch.oasisreport.OasisReport;

public class OperatorSummary
{

   public OperatorSummary()
   {
   }
   
   Set<String> getEmployeeNumbers()
   {
      return (employeeNumbers);
   }
   
   Set<Date> getReportDates()
   {
      return (reportDates);
   }
   
   void addReport(OasisReport report)
   {
      String employeeNumber = report.getEmployeeNumber();
      String machineNumber = report.getMachineNumber();
      Date reportDate = report.getDate();
      
      employeeNumbers.add(employeeNumber);
      
      reportDates.add(trim(reportDate));
      
      if (!machineSummaries.containsKey(machineNumber))
      {
         machineSummaries.put(machineNumber,  new MachineSummary(machineNumber));
      }
      
      machineSummaries.get(machineNumber).addReport(report);
   }
   
   public String toHtml()
   {
      String html = "";
      
      // Construct employee numbers string.
      String employeeNumbersString = "";
      int index = 0;
      for (String employeeNumber : employeeNumbers)
      {
         employeeNumbersString += employeeNumber;
         if (index < (employeeNumbers.size() - 1))
         {
            employeeNumbersString += ", ";
            index++;
         }
      }
      
      // Construct employee names string.
      String employeeNamesString = "";
      
      // Construct report dates string.
      String reportDatesString = "";
      SimpleDateFormat formatter = new SimpleDateFormat("MM/dd");  
      index = 0;
      for (Date reportDate : reportDates)
      {
         reportDatesString += formatter.format(reportDate);
         
         if (index < (reportDates.size() - 1))
         {
            reportDatesString += ", ";
            index++;
         }
      }
         
      html += "<table class=\"operator-summary-table\">\n"
            + "<tr>\n"
            + "<td><label>Operator #:</label></td>\n"
            + "<td><input type=\"text\" disabled value=\"" + employeeNumbersString + "\"/></td>\n"
            + "</tr>\n"
            + "<tr>\n"
            + "<td><label>Operator name:</label></td>\n"
            + "<td><input type=\"text\" disabled value=\"" + employeeNamesString + "\"/></td>\n"
            + "</tr>\n"
            + "<tr>\n"
            + "<td><label>Report date(s):</label></td>\n"
            + "<td><input type=\"text\" disabled value=\"" + reportDatesString + "\"/></td>\n"
            + "</tr>\n"
            + "</table>\n";
      
      html += "<table>\n";
      
      for (MachineSummary summary : machineSummaries.values())
      {
         html += "<tr>\n";
         
         html += summary.toHtml();
         
         html += "</tr>\n";
      }
      
      html += "</table>\n";
      
      return (html);
   }
   
   // http://stackoverflow.com/questions/2775411/how-to-trim-minutes-and-hours-and-seconds-from-date-object
   private static Date trim(Date date)
   {
      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.setTime(date);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      
      return (cal.getTime());
 }
   
   private Set<String> employeeNumbers = new HashSet<String>();
   
   private Set<String> employeeNames = new HashSet<String>();
   
   private Set<Date> reportDates = new HashSet<Date>();
   
   private Map<String, MachineSummary> machineSummaries =  new HashMap<String, MachineSummary>();   
}
