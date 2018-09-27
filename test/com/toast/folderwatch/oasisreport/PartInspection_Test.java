package com.toast.folderwatch.oasisreport;

public class PartInspection_Test
{
   // TODO:  Move into a Parser test.
   /*
   @Test
   public void testParse()
   {
      PartInspection partInspection = new PartInspection();
      
      String[] lines = 
      {
         "START|C:\\gpc\\Oasis\\Data\\M8206 Rev 10.txt",
         "DATA|thd lgh|0.6600|0.6620|0.6667|0.6980|0.7000|PASS",
         "DATA|.75|0.7400|0.7410|0.7485|0.7590|0.7600|PASS",
         "DATA|.641|0.6310|0.6320|0.6419|0.6500|0.6510|PASS",
         "DATA|und cut|0.6310|0.6320|0.6426|0.6500|0.6510|PASS",
         "DATA|major|0.7385|0.7389|0.7454|0.7461|0.7465|PASS",
         "DATA|pitch|0.7030|0.7032|0.7055|0.7063|0.7065|PASS",
         "DATA|nose|0.4280|0.4290|0.4414|0.4470|0.4480|PASS",
         "DATA|45 DEG| 44.50| 44.55| 44.64| 45.45| 45.50|PASS",
         "DATA|.25 LGH|0.2400|0.2410|0.2476|0.2590|0.2600|PASS",
         "DATA|OAL|1.5180|1.5185|1.5236|1.5275|1.5280|PASS",
         "END|Mar 21 2017|11:05:43 PM"
      };
      
      for (String line : lines)
      {
         assertTrue(partInspection.parse(line));
      }

      assertTrue(partInspection.getDataFile().equals("C:\\gpc\\Oasis\\Data\\M8206 Rev 10.txt"));
      assertTrue(partInspection.getMeasurments().size() == 10);
      
      System.out.format("Date: %s\n", partInspection.getDate().toString());
   }
   */
}
