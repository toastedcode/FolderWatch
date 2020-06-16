package com.toast.foldlerwatch.config;

public class EmployeeConfig
{
   public String firstName;
   public String lastName;
   public int employeeNumber;
   
   public String getFullName()
   {
      return (firstName + " " + lastName);
   }
}
