package com.toast.foldlerwatch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Employee
{
   public static void loadEmployees()
   {
      final String EMPLOYEE_DATA_FILE = "./employees.csv";
      
      final int EMPLOYEE_NUMBER_INDEX = 0;
      final int FIRST_NAME_INDEX = 1;
      final int LAST_NAME_INDEX = 2;
      
      final boolean IGNORE_FIRST_ROW = true;

      try
      {
         String data[][] = CsvReader.read(EMPLOYEE_DATA_FILE, IGNORE_FIRST_ROW);
        
         for (int i = 0; i < data.length; i++)
         {
            String employeeNumber = data[i][EMPLOYEE_NUMBER_INDEX];
            String firstName = data[i][FIRST_NAME_INDEX];
            String lastName = data[i][LAST_NAME_INDEX];
            
            employees.put(employeeNumber, new Employee(employeeNumber, firstName, lastName));
         }
      }
      catch (FileNotFoundException e)
      {
         fileError = true;
      }
      catch (IOException e)
      {
         fileError = true;
      }
      finally
      {
         loadedEmployeeData = true;
         fileError = false;
      }
   }
   
   public static Employee getEmployee(String employeeNumber)
   {
      Employee employee = null;
      
      if (!loadedEmployeeData && !fileError)
      {
         loadEmployees();
      }
      
      if (loadedEmployeeData)
      {
         employee = employees.get(employeeNumber);
      }
      
      return (employee);
   }
   
   public Employee(String employeeNumber, String firstName, String lastName)
   {
      this.employeeNumber = employeeNumber;
      this.firstName = firstName;
      this.lastName = lastName;
   }
   
   public String getEmployeeNumber()
   {
      return (employeeNumber);
   }
   
   public String getFirstName()
   {
      return (firstName);
   }
   
   public String getFullName()
   {
      return (firstName + " " + lastName);
   }
   
   private static Map<String, Employee> employees = new HashMap<String, Employee>();
   
   private static boolean loadedEmployeeData = false;
   
   private static boolean fileError = false;
   
   String employeeNumber;
   
   String firstName;
   
   String lastName;
}
