package com.toast.foldlerwatch;

import java.io.File;
import java.io.IOException;

import com.toast.foldlerwatch.oasisreport.OasisReport;
import com.toast.foldlerwatch.parser.ParseException;
import com.toast.foldlerwatch.parser.RptParser;
import com.toast.foldlerwatch.parser.XlsParser;

public class XlsToRpt
{

   public static void main(String[] args)
   {
      if (args.length == 2)
      {
         String sourcePath = args[0];
         
         String destinationPath = args[1];
         
         convert(sourcePath, destinationPath);
      }
      else
      {
         System.out.println("Usage: xlsreport [source file] [destination file]");
      }
   }
   
   public static void convert(String sourcePath, String destinationPath)
   {
      File sourceFile = new File(sourcePath);
      
      File destinationFile = new File(destinationPath);
      
      System.out.format("Converting: %s -> %s\n", sourceFile.getPath(), destinationFile.getPath());

      if (!sourcePath.substring(sourcePath.lastIndexOf(".")).equals(".xls"))
      {
         System.out.println("Error: Source file must be an Oasis Excel report (*.xls).");
      }
      else if (!destinationPath.substring(destinationPath.lastIndexOf(".")).equals(".rpt"))
      {
         System.out.println("Error: Destination file must be an Oasis report (*.rpt).");
      }
      else
      {
         XlsParser xlsParser = new XlsParser();
         
         OasisReport report = new OasisReport();
         
         try
         {
            xlsParser.parse(sourceFile, report);
         }
         catch (ParseException e)
         {
            System.out.format("Error: Could not parse Oasis Excel file %s\n", sourcePath);
         }
         catch (IOException e)
         {
            System.out.format("Error: Could not open Oasis Excel file %s\n", sourcePath);
         }
         
         RptParser rptParser = new RptParser();

         try
         {
            rptParser.serialize(report, destinationFile);
         }
         catch (IOException e)
         {
            System.out.format("Error: Could not save to Oasis report file %s\n", sourcePath);
         }
         
         System.out.format("Success!: %s -> %s\n", sourcePath, destinationPath);
      }
   }
}
