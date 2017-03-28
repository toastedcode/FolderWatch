package com.toast.foldlerwatch;

public enum MeasurementResult
{
   UNKNOWN("UNKNOWN"),
   PASS("PASS"),
   WARN_LOW("WARN LOW"),
   WARN_HIGH("WARN HIGH"),
   FAIL_LOW("FAIL LOW"),
   FAIL_HIGH("FAIL HIGH"),
   FAIL_NULL("FAIL NULL");

   MeasurementResult(String token)
   {
      this.token = token;
   }
   
   public String getToken()
   {
      return (token);
   }
   
   public boolean isPassed()
   {
      return (this == PASS);
   }
   
   public boolean isFailed()
   {
      return ((this == FAIL_LOW) || (this == FAIL_HIGH) || (this == FAIL_NULL));
            
   }
   
   public boolean isWarning()
   {
      return ((this == WARN_LOW) || (this == WARN_HIGH));     
   }
   
   static public MeasurementResult valueOfToken(String token)
   {
      MeasurementResult value = UNKNOWN;
      
      for (MeasurementResult measurementResult : MeasurementResult.values())
      {
         if (token.contains(measurementResult.getToken()))
         {
            value = measurementResult;
         }
      }
      
      return (value);
   }
   
   private String token;     

}
