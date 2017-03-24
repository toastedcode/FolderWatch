package com.toast.foldlerwatch;

public class UserField
{
   public UserField()
   {
   }
   
   public UserField(String label, String value)
   {
      this.label = label;
      this.value = value;
   }
   
   public boolean parse(String line)
   {
      boolean success = true;
      
      String[] tokens = line.split("\\|");
      
      if (tokens.length > 0)
      {
         ReportLineType lineType = ReportLineType.valueOfToken(tokens[0]);
         
         switch (lineType)
         {
            case USER_FIELD_LABEL:
            {
               if (tokens.length == 1)
               {
                  // Allow for empty values.
                  value = "";
               }
               else
               {
                  label = tokens[1];
               }
               break;
            }
            
            case USER_FIELD_VALUE:
            {
               if (tokens.length == 1)
               {
                  // Allow for empty values.
                  value = "";
               }
               else
               {
                  value = tokens[1];
               }
               break;
            }
            
            default:
            {
               // Parse error!
               success = false;
               break;
            }
         }
      }
      
      return (success);
    }
   
   boolean isValid()
   {
      return ((label != null) && (value != null));
   }
   
   String getLabel()
   {
      return (label);
   }
   
   String getValue()
   {
      return (value);
   }

   private String label = null;
   
   private String value = null;
}
