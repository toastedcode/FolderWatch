package com.toast.foldlerwatch.notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;

import com.toast.foldlerwatch.config.Configuration;
import com.toast.foldlerwatch.oasisreport.OasisReport;

public class Server2
{
   static public void serverUpload(OasisReport report)
   {
      String urlString = Configuration.serverConfig.url;
      
      StringBuilder sb = new StringBuilder();

      // Format date for PHP and GET request.
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
      String date = formatter.format(report.getDate());
      date = date.replaceAll(" ", "%20");
      
      // Url
      sb.append(urlString);
      sb.append("?");
      // dateTime
      sb.append("dateTime=");
      sb.append(date);
      sb.append("&");
      // Employee number
      sb.append("employeeNumber=");
      sb.append(report.getEmployeeNumber());
      sb.append("&");
      // WC number
      sb.append("wcNumber=");
      sb.append(report.getMachineNumber());
      sb.append("&");
      // Part number
      sb.append("partNumber=");
      sb.append(report.getPartNumber());
      sb.append("&");
      // Part count
      sb.append("partCount=");
      sb.append(report.getPartCount());
      sb.append("&");
      // Failures
      sb.append("failures=");
      sb.append(report.getFailureCount());
      sb.append("&");
      // Efficiency
      sb.append("efficiency=");
      sb.append(report.getEfficiency());
      sb.append("&");
      
      InputStream inputStream;
      
      try
      {         
         URL url = new URL(sb.toString());
         
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(5000);
         
         connection.connect();
         
         if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
         {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null)
            {
               response.append(line);
               response.append('\r');
            }
            
            System.out.format("Server response: %s", response.toString());
         }
         else
         {
            System.out.format("Bad server request: %s.", sb.toString());
         }
      }
      catch (SocketTimeoutException | ConnectException e)
      {
         System.out.format("Failed to contact server.");
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
