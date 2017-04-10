package com.toast.foldlerwatch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CsvReader
{
   static String[][] read(String path, boolean ignoreFirstRow) throws FileNotFoundException, IOException
   {
      final String SEPERATOR = ",";
      
      String[][] csv;
      
      BufferedReader reader = new BufferedReader(new FileReader(path));
      
      String line = "";
      List<String[]> rows = new LinkedList<String[]>();
      
      int index = 0;
      while ((line = reader.readLine()) != null)
      {
         if (ignoreFirstRow && (index == 0))
         {
            index++;
            continue;
         }
         
         rows.add(line.split(SEPERATOR));
         index++;
      }
      
      reader.close();
      
      // http://stackoverflow.com/questions/13433407/java-read-csv-file-and-save-into-arrays
      csv = rows.toArray(new String[rows.size()][]);
      
      return (csv);
   }
}
