package com.toast.foldlerwatch;

public enum UserFieldType
{
   INSPECTION_TYPE("Inspection Type"),
   EMPLOYEE_NUMBER("Employee #"),
   COMMENTS("Comments"),
   PART_COUNT("Part Count"),
   SAMPLE_SIZE("Sample Size"),
   MACHINE_NUMBER("Machine #"),
   DATE("Date"),
   PART_NUMBER("Part #"),
   EFFICIENCY("Efficiency");
   
   UserFieldType(String label)
   {
      this.label = label;
   }
   
   public String getLabel()
   {
      return (label);
   }
   
   private String label;   
}
