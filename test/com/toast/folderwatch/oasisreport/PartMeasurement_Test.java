package com.toast.folderwatch.oasisreport;

public class PartMeasurement_Test
{

   // TODO:  Move into a Parser test.
   /*
   @Test
   public void testParse()
   {
      PartMeasurement measurement = new PartMeasurement();
      
      Map<MeasurementType, Double> solution = new HashMap<>();
      solution.put(MeasurementType.LOW_LIMIT, 0.66);
      solution.put(MeasurementType.LOW_WARN, 0.662);
      solution.put(MeasurementType.MEASURED, 0.6626);
      solution.put(MeasurementType.HIGH_WARN, 0.6980);
      solution.put(MeasurementType.HIGH_LIMIT, 0.7);
      
      boolean success = measurement.parse("DATA|thd lgh|0.6600|0.6620|0.6626|0.6980|0.7000|PASS");
      
      assertTrue(success);
      assertTrue(measurement.getName().equals("thd lgh"));
      
      for (MeasurementType type : MeasurementType.values())
      {
         assertTrue(measurement.getValue(type).equals(solution.get(type)));
      }
      
      assertTrue(measurement.getResult() == MeasurementResult.PASS);
   }
   */
}
