package com.toast.folderwatch;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

import com.toast.foldlerwatch.OasisReport;

public class OasisReport_Test
{
   @Test
   public void testParse() throws IOException
   {
      OasisReport report = new OasisReport();
      
      // Open a report file.
      File file = new File("testreport.txt");
      
      System.out.format("File: %s\n", file.getAbsolutePath());
      
      assertTrue(report.parse(file));
      
      assertTrue(report.getEmployeeNumber().equals("370"));
      assertTrue(report.getMachineNumber().equals("618"));
      assertTrue(report.getSampleSize() == 6);
      assertTrue(report.getPartCount() == 2725);
      assertTrue(report.getEfficiency() == 77.5);
      
      System.out.format("Report date: %s\n",  report.getDate().toString());
   }
   
   @Test
   public void testToHtml() throws IOException
   {
      OasisReport report = new OasisReport();
      
      // Open a report file.
      File file = new File("testreport.txt");
    
      // Parse
      assertTrue(report.parse(file));
      
      // Convert to HTML.
      String html = report.toHtml();
      
      //
      // Write to a file.
      //

      FileWriter fileWriter = new FileWriter("report.html");
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      
      bufferedWriter.write(html);
      
      bufferedWriter.close();
      
      //System.out.format("%s\n", report.toHtml());
   }
}
