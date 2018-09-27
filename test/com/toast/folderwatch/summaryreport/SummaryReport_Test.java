package com.toast.folderwatch.summaryreport;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
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
import com.toast.foldlerwatch.summaryreport.SummaryReport;
import com.toast.foldlerwatch.parser.ParseException;

public class SummaryReport_Test
{
   @Test
   public void testToHtml_large() throws IOException, ParseException
   {
      Path p = Paths.get("./testcases/");
      
      final SummaryReport summaryReport = new SummaryReport();
      
      FileVisitor<Path> fv = new SimpleFileVisitor<Path>()
      {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
           OasisReport report = new OasisReport();
           
           // Parse
           try
           {
              assertTrue(report.parse(file.toFile()));
           }
           catch (ParseException e)
           {
              assertTrue(false);
           }
           
           // Add to summary.
           summaryReport.addReport(report);
           
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
      
      String html = summaryReport.toHtml();
      
      
      //
      // Write to a file.
      //

      FileWriter fileWriter = new FileWriter("./reports/summary.html");
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      
      bufferedWriter.write(html);
      
      bufferedWriter.close();
   }
}