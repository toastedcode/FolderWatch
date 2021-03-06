package com.toast.foldlerwatch.summaryreport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.toast.foldlerwatch.oasisreport.OasisReport;

class MachineSummary
{
   public final int SMALL_PART_EXPECTED_REPORTS = 4;
   public final int LARGE_PART_EXPECTED_REPORTS = 8;
   
   public MachineSummary(String machineNumber)
   {
      this.machineNumber = machineNumber;
   }
   
   public String getMachineNumber()
   {
      return (machineNumber);
   }
   
   public String getPartNumber()
   {
      Set<String> partNumbers = new HashSet<String>();
      
      String partNumber = "";
      
      for (OasisReport report : oasisReports)
      {
         partNumbers.add(report.getPartNumber());
      }
      
      int index = 0;
      for (String partNum : partNumbers)
      {
         partNumber += partNum;
         
         if ((partNumbers.size() > 1) &&
             (index < (partNumbers.size() - 1)))
         {
            partNumber += ", ";
         }
         
         index++;
      }
      
      return (partNumber);
   }
   
   public void addReport(OasisReport report)
   {
      oasisReports.add(report);
   }
   
   public String toHtml()
   {
      String html = "";
      
      html += "<td>\n";
      
      html += "<div class=\"card\">\n";
      
      html += "<div class=\"machine-number-container\">Machine#: " + getMachineNumber() + "</div>\n";
      
      html += "<div class=\"machine-number-container\">Part#: " + getPartNumber() + "</div>\n";

      html += "<table class=\"report-summary-table\">\n";
      
      // Table heading.
      html += "<tr class=\"report-summary-table-header\">\n";
      html += "<th class=\"part-number-column\">Part#</th>\n";
      html += "<th class=\"time-column\">Time</th>\n";
      html += "<th class=\"quantity-column\">Quantity</th>\n";
      html += "<th class=\"failures-column\">Failures</th>\n";
      html += "<th class=\"efficiency-column\">Efficiency</th>\n";
      html += "</tr>\n";
      
      //
      // Oasis reports.
      //
      
      Collections.sort(oasisReports);
      
      SimpleDateFormat formatter = new SimpleDateFormat("MM/dd hh:mm a");
      
      for (OasisReport report : oasisReports)
      {
         html += "<tr>\n";
         
         // Part Number
         html += "<td>" + report.getPartNumber() + "</td>\n";
         
         // Time
         Date date = report.getDate();
         String timeString = formatter.format(date);
         html += "<td>" + timeString + "</td>\n";
         
         // Part Count
         html += "<td>" + report.getPartCount() + "</td>\n";
         
         // Quantity
         html += "<td>" + report.getFailureCount() + "</td>\n";
         
         // Efficiency
         html += "<td>" + report.getEfficiency() + "%</td>\n";

         html += "</tr>\n";
      }
      
      //
      // Missing reports.
      //
      
      int expectedReports = isLargePart() ? LARGE_PART_EXPECTED_REPORTS : SMALL_PART_EXPECTED_REPORTS;
      
      int missingReports = (oasisReports.size() < expectedReports) ? (expectedReports - oasisReports.size()) : 0;
      
      if (missingReports > 0)
      {
         String checks = (missingReports > 1) ? "checks" : "check";
         html += "<tr class=\"missing-report\"><td colspan=\"5\" align=\"center\">Missing " + missingReports + " " + checks + "</td></tr>";
      }
      
      html += "</table>\n";
      
      html += "</div>\n";  // end  <div class="card">
      
      html += "</td>\n";
      
      //
      // Manager notes
      //
      
      html += "<td>\n"
            + "<div class=\"manager-notes-container\">\n"
            + "<div><label>Parts Count:</label><input class=\"parts-count-input\" type=\"text\"/></div>\n"
            + "<br/>\n"
            + "<label>Comments:</label>\n"
            + "<br/>\n"
            + "<textarea rows=\"10\" cols=\"50\" class=\"comments-input\"></textarea>\n"
            + "</div>\n"
            + "</td>\n";
      
      return (html);
   }
   
   private boolean isLargePart()
   {
      boolean isLargePart = false;
      
      final String PART_A = "A";
      final String PART_B = "B";
      
      for (OasisReport report : oasisReports)
      {
         String partNumber = report.getPartNumber();
         
         if ((partNumber.toUpperCase().endsWith(PART_A)) || 
             (partNumber.toUpperCase().endsWith(PART_B)))
         {
            isLargePart = true;
            break;
         }
      }
      
      return (isLargePart);
   }
   
   private String machineNumber;
   
   private ArrayList<OasisReport> oasisReports = new ArrayList<OasisReport>();
}
