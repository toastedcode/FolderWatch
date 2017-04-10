package com.toast.foldlerwatch.summaryreport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.toast.foldlerwatch.oasisreport.MeasurementType;
import com.toast.foldlerwatch.oasisreport.OasisReport;

public class MachineSummary
{
   public final int EXPECTED_REPORTS = 4;
   
   public MachineSummary(String machineNumber)
   {
      this.machineNumber = machineNumber;
   }
   
   String getMachineNumber()
   {
      return (machineNumber);
   }
   
   void addReport(OasisReport report)
   {
      oasisReports.add(report);
   }
   
   public String toHtml()
   {
      String html = "";
      
      html += "<td>\n";
      
      html += "<div class=\"card\">\n";
      
      html += "<div class=\"machine-number-container\">Machine#: " + getMachineNumber() + "</div>\n";

      html += "<table class=\"report-summary-table\">\n";
      
      // Table heading.
      html += "<tr class=\"report-summary-table-header\">\n";
      html += "<th>Time</th>\n";
      html += "<th>Quantity</th>\n";
      html += "<th>Efficiency</th>\n";
      html += "</tr>\n";
      
      //
      // Oasis reports.
      //
      
      Collections.sort(oasisReports);
      
      SimpleDateFormat formatter = new SimpleDateFormat("MM/dd hh:mm a");
      
      for (OasisReport report : oasisReports)
      {
         html += "<tr>\n";
         
         // Time
         Date date = report.getDate();
         String timeString = formatter.format(date);
         html += "<td>" + timeString + "</td>\n";
         
         // Quantity
         html += "<td>" + report.getPartCount() + "</td>\n";
         
         // Efficiency
         html += "<td>" + report.getEfficiency() + "%</td>\n";

         html += "</tr>\n";
      }
      
      //
      // Missing reports.
      //
      
      int missingReports = (oasisReports.size() < EXPECTED_REPORTS) ? (EXPECTED_REPORTS - oasisReports.size()) : 0;
      
      for (int i = 0; i < missingReports; i++)
      {
         html += "<tr class=\"missing-report\"><td>Missing Check</td><td></td><td></td></tr>";
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
   
   private String machineNumber;
   
   private ArrayList<OasisReport> oasisReports = new ArrayList<OasisReport>();
}
