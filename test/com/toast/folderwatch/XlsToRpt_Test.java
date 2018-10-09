package com.toast.folderwatch;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.toast.foldlerwatch.XlsToRpt;
import com.toast.foldlerwatch.oasisreport.OasisReport;
import com.toast.foldlerwatch.parser.ParseException;

public class XlsToRpt_Test
{

   @Test
   public void testXlsToRpt() throws IOException, ParseException
   {
      XlsToRpt.convert("InspectionReport.xls",  "InspectionReport.rpt");
      
      OasisReport report = new OasisReport();
      report.parse(new File("InspectionReport.rpt"));
   }
}
