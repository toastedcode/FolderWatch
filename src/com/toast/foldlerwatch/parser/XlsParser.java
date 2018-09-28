package com.toast.foldlerwatch.parser;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.toast.foldlerwatch.oasisreport.MeasurementResult;
import com.toast.foldlerwatch.oasisreport.MeasurementType;
import com.toast.foldlerwatch.oasisreport.OasisReport;
import com.toast.foldlerwatch.oasisreport.PartInspection;
import com.toast.foldlerwatch.oasisreport.PartMeasurement;
import com.toast.foldlerwatch.oasisreport.UserField;
import com.toast.foldlerwatch.oasisreport.UserFieldType;

import jxl.Workbook;
import jxl.biff.CellReferenceHelper;
import jxl.read.biff.BiffException;
import jxl.Cell;
import jxl.Sheet;

public class XlsParser implements Parser
{
   @Override
   public void parse(File file, OasisReport report) throws IOException, ParseException
   {
      this.report = report;
      
      try
      {
         workbook = Workbook.getWorkbook(file);
         
         sheetNames = workbook.getSheetNames();
         
         parseUserFields();
         
         parseInspections();
         
         System.out.format("Inspection report:\n%s\n", this.report.toHtml());
      }
      catch (BiffException e)
      {
         throw new ParseException();
      }
   }
   
   private void parseUserFields()
   {
      for (UserFieldType userFieldType : UserFieldType.values())
      {
         String cellAddress = ReportField.getReportField(userFieldType).address;
         
         if (!cellAddress.isEmpty())
         {
            cellAddress = sheetNames[0] + "!" + cellAddress;
            String content = getContent(cellAddress);
         
            if (!content.isEmpty())
            {
               UserField userField = new UserField();
               userField.setLabel(userFieldType.getLabel());
               userField.setValue(content);
               
               report.addUserField(userFieldType.ordinal(), userField);
            }
         }
      }
   }
   
   private void parseInspections() throws ParseException
   {
      Sheet sheet = workbook.getSheet(sheetNames[0]);
      
      for (int row = 1; row <= sheet.getRows(); row++)
      {
         String lineAddress = sheetNames[0] + "!" + ReportField.SUMMARY.address + Integer.toString(row);
         
         if (getContent(lineAddress).equals("SUMMARY DATA"))
         {
            break;
         }
         else
         {
            lineAddress = sheetNames[0] + "!" + ReportField.DIMENSION.address + Integer.toString(row);
            
            if (getContent(lineAddress).equals("Dimension"))
            {
               lineAddress = nextRow(lineAddress);
               
               PartInspection inspection = new PartInspection();
               
               parsePartInspection(lineAddress, inspection);
               
               report.addInspection(inspection);
            }
         }
      }
   }
   
   private void parsePartInspection(String lineAddress, PartInspection inspection) throws ParseException
   {
      // --------------------------------------------------------------------
      // |Dimension|Low Limit|Low Warn|Measured|High Warn|High Limit|Status |
      // --------------------------------------------------------------------
      // |<label>  |<value>  |<value> |<value> |<value>  |<value>   |<value>|
      // --------------------------------------------------------------------
      // |<label>  |<value>  |<value> |<value> |<value>  |<value>   |<value>|
      // --------------------------------------------------------------------
      // |<label>  |<value>  |<value> |<value> |<value>  |<value>   |<value>|
      // --------------------------------------------------------------------
      // |Date     |Time     |File    |        |         |          |       |
      // --------------------------------------------------------------------
      // |         |         |        |        |         |          |       |
      // --------------------------------------------------------------------
      
      boolean isLastMeasurement = getContent(nextRow(lineAddress)).isEmpty();
      
      while (!isLastMeasurement)
      {
         PartMeasurement measurement = new PartMeasurement();
         
         parsePartMeasurement(lineAddress, measurement);
         
         inspection.addMeasurement(measurement);
         
         lineAddress = nextRow(lineAddress);
         
         isLastMeasurement = getContent(nextRow(lineAddress)).isEmpty();
      }
      
      parseInspectionStats(lineAddress, inspection);
   }
   
   private void parsePartMeasurement(String lineAddress, PartMeasurement measurement)
   {
      // Measurement name
      String cellAddress = getSheet(lineAddress) + "!" + ReportField.DIMENSION.address + Integer.toString(getRow(lineAddress));
      measurement.setName(getContent(cellAddress));

      // Measurement values.
      for (MeasurementType measurementType : MeasurementType.values())
      {
         cellAddress = getSheet(lineAddress) + "!" + ReportField.getReportField(measurementType).address + Integer.toString(getRow(lineAddress)); 
         String content = getContent(cellAddress);
         
         try
         {
            measurement.addValue(measurementType, Double.valueOf(content));
         }
         catch (NumberFormatException e)
         {
            measurement.addValue(measurementType, 0.0);
         }
      }
      
      // Measurement status
      cellAddress = getSheet(lineAddress) + "!" + ReportField.STATUS.address + Integer.toString(getRow(lineAddress));
      String content = getContent(cellAddress);
      if (content.isEmpty())
      {
         measurement.setResult(MeasurementResult.PASS);
      }
      else
      {
         measurement.setResult(MeasurementResult.valueOfToken(content));
      }
   }
   
   private void parseInspectionStats(String lineAddress, PartInspection inspection) throws ParseException
   {
      // Inspection date
      String cellAddress = getSheet(lineAddress) + "!" + ReportField.INSPECTION_DATE.address + Integer.toString(getRow(lineAddress));
      String dateString = getContent(cellAddress);
      cellAddress = getSheet(lineAddress) + "!" + ReportField.INSPECTION_TIME.address + Integer.toString(getRow(lineAddress));
      String timeString = getContent(cellAddress);
      
      try
      {
         // To match: Mar 21 2017|11:05:43 PM
         DateFormat format = new SimpleDateFormat("MMM dd yyyy hh:mm:ss a", Locale.ENGLISH);
         inspection.setDate(format.parse(dateString + " " + timeString));
      }
      catch (java.text.ParseException e)
      {
         throw new ParseException();
      }
      
      // Data file
      cellAddress = getSheet(lineAddress) + "!" + ReportField.DATA_FILE.address + Integer.toString(getRow(lineAddress));
      inspection.setDataFile(getContent(cellAddress));
   }
   
   private String getContent(String cellAddress)
   {
      String content = "";
      
      Cell cell = workbook.getCell(cellAddress);
      
      if ((cell != null) &&
          (!cell.getContents().isEmpty()))
      {
         content = cell.getContents();
      }
      
      return (content);
   }
   
   private String getSheet(String address)
   {
      return (CellReferenceHelper.getSheet(address));
   }
   
   private String getColumn(String address)
   {
      return (CellReferenceHelper.getColumnReference(CellReferenceHelper.getColumn(address)));
   }
   
   private int getRow(String address)
   {
      return (CellReferenceHelper.getRow(address) + 1);
   }
   
   private String nextRow(String address)
   {
      return (getSheet(address) + "!" + getColumn(address) + Integer.toString(getRow(address) + 1));
   }
   
   private enum ReportField
   {
      // User Field Types
      INSPECTION_TYPE("B8"),
      INSPECTOR("B9"),
      COMMENTS("B10"),
      PART_COUNT("E8"),
      SAMPLE_SIZE("E9"),
      MACHINE_NUMBER(""),
      DATE("G8"),
      PART_NUMBER("G9"),
      EFFICIENCY(""),
      // Dimension column
      DIMENSION("A"),
      // Measurement Types
      LOW_LIMIT("B"),
      LOW_WARN("C"),
      MEASURED("D"),
      HIGH_WARN("E"),
      HIGH_LIMIT("F"),
      // Inspection stats
      INSPECTION_DATE("A"),
      INSPECTION_TIME("B"),
      DATA_FILE("C"),
      // Status column
      STATUS("G"),
      // Summary column
      SUMMARY("B");
      
      private ReportField(String address)
      {
         this.address = address;
      }
      
      public static ReportField getReportField(UserFieldType userFieldType)
      {
         return (ReportField.values()[userFieldType.ordinal()]);
      }
      
      public static ReportField getReportField(MeasurementType measurementType)
      {
         return (ReportField.values()[measurementType.ordinal() + LOW_LIMIT.ordinal()]);
      }
      
      public String address;
   }
   
   private OasisReport report;
   
   private Workbook workbook;
   
   private String[] sheetNames; 
}
