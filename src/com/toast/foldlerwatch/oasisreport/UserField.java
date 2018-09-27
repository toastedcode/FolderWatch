package com.toast.foldlerwatch.oasisreport;

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
   
   boolean isValid()
   {
      return ((label != null) && (value != null));
   }
   
   String getLabel()
   {
      return (label);
   }
   
   public void setLabel(String label)
   {
      this.label = label;
   }
   
   String getValue()
   {
      return (value);
   }
   
   public void setValue(String value)
   {
      this.value = value;
   }

   private String label = null;
   
   private String value = null;
}
