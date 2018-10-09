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
   
   public boolean isValid()
   {
      return ((label != null) && (value != null));
   }
   
   public String getLabel()
   {
      return (label);
   }
   
   public void setLabel(String label)
   {
      this.label = label;
   }
   
   public String getValue()
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
