package com.toast.foldlerwatch;

// Oasis report line type examples:
//
// START|C:\gpc\Oasis\Data\M8206 Rev 10.txt
// DATA|thd lgh|0.6600|0.6620|0.6675|0.6980|0.7000|PASS
// END|Mar 21 2017|11:05:19 PM
// UserField1|Insp. Type:
// UserData1|

public enum ReportLineType
{
   UNKNOWN("", 0),
   PART_INSPECTION_START("START", 2),
   PART_INSPECTION_DATA("DATA", 8),
   PART_INSPECTION_END("END", 3),
   USER_FIELD_LABEL("UserField", 2),
   USER_FIELD_VALUE("UserData", 2);  // Allows for second token to be empty.
   
   ReportLineType(String token, int tokenCount)
   {
      this.token = token;
      this.tokenCount = tokenCount;
   }
   
   public String getToken()
   {
      return (token);
   }
   
   public int getTokenCount()
   {
      return (tokenCount);
   }
   
   static public ReportLineType valueOfToken(String token)
   {
      ReportLineType value = UNKNOWN;
      
      for (ReportLineType lineType : ReportLineType.values())
      {
         if (token.contains(lineType.getToken()))
         {
            value = lineType;
         }
      }
      
      return (value);
   }
         
   
   private String token;
   
   private int tokenCount;
}
