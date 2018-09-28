package com.toast.folderwatch.oasisreport;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Test;

import com.toast.foldlerwatch.oasisreport.OasisReport;
import com.toast.foldlerwatch.parser.ParseException;

public class OasisReport_Test
{
   @Test
   public void testParse() throws IOException, ParseException
   {
      OasisReport report = new OasisReport();
      
      // Open a report file.
      //File file = new File("M8206 Rev 10.rpt");
      //File file = new File("InspectionReport_Cracked.xls");
      //File file = new File("InspectionReport_Copied.xls");
      File file = new File("InspectionReport.xls");
      //File file = new File("InspectionReport2.xls");
      
      System.out.format("File: %s\n", file.getAbsolutePath());
      
      assertTrue(report.parse(file));
      
      assertTrue(report.getEmployeeNumber().equals("370"));
      assertTrue(report.getMachineNumber().equals("618"));
      assertTrue(report.getSampleSize() == 6);
      assertTrue(report.getPartCount() == 2725);
      assertTrue(report.getEfficiency() == 77.0);
      
      System.out.format("Report date: %s\n",  report.getDate().toString());
   }
   
   @Test
   public void testToHtml() throws IOException, ParseException
   {
      OasisReport report = new OasisReport();
      
      // Open a report file.
      //File file = new File("M8206 Rev 10.rpt");
      File file = new File("InspectionReport.xls");
    
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
   
   @Test
   public void testToHtml_large() throws IOException, ParseException
   {
      Path p = Paths.get("./testcases/");
      
      FileVisitor<Path> fv = new SimpleFileVisitor<Path>()
      {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
           String filename = file.getFileName().toString();
           
           OasisReport report = new OasisReport();
           
           // Parse
           try
           {
              assertTrue(report.parse(file.toFile()));
           }
           catch (ParseException e)
           {
              assertTrue(false);  // TODO: Proper way?
           }
           
           // Convert to HTML.
           String html = report.toHtml();
           
           //
           // Write to a file.
           //

           FileWriter fileWriter = new FileWriter("./reports/" + filename.replace(".rpt",  ".html"));
           BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
           
           bufferedWriter.write(html);
           
           bufferedWriter.close();
           
           return (FileVisitResult.CONTINUE);
        }
      };

      try
      {
         Files.walkFileTree(p, fv);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
