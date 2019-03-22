package com.toast.foldlerwatch;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.String;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;

import com.toast.foldlerwatch.archive.Archive;
import com.toast.foldlerwatch.oasisreport.OasisReport;
import com.toast.foldlerwatch.summaryreport.SummaryReport;
import com.toast.foldlerwatch.summaryreport.SummaryReport.Shift;
import com.toast.foldlerwatch.parser.ParseException;

public class FolderWatch
{
   public static void main(final String args[])
   {
      try
      {
         loadProperties();
         
         if (args.length > 0)
         {
            switch (args[0])
            {
               case "-summaryreport":
               {
                  SummaryReport.Shift shift = SummaryReport.Shift.ALL;
                  
                  // Get shift, if specified.
                  if ((args.length >= 3) &&
                      (args[1].equals("-shift")))
                  {
                     try
                     {
                        shift = SummaryReport.Shift.valueOf(args[2]);
                     }
                     catch (IllegalArgumentException e)
                     {
                        shift = SummaryReport.Shift.ALL;
                     }
                  }
                  
                  summaryReport(shift);
                  break;
               }
               
               case "-archive":
               {
                  SummaryReport.Shift shift = SummaryReport.Shift.ALL;
                  
                  // Get shift, if specified.
                  if ((args.length >= 3) &&
                      (args[1].equals("-shift")))
                  {
                     try
                     {
                        shift = SummaryReport.Shift.valueOf(args[2]);
                     }
                     catch (IllegalArgumentException e)
                     {
                        shift = SummaryReport.Shift.ALL;
                     }
                  }
                  
                  archive(shift);
                  break;
               }
               
               case "-folderwatch":
               default:
               {
                  folderWatch();
                  break;
               }
               
            }
         }
         else
         {
            folderWatch();
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
   
   public static void summaryReport(SummaryReport.Shift shift)
   {
      // Get the appropriate watched folders from the properties file.
      String[] folders = getFolders(shift);
      
      for (String folder : folders)
      {
         // Create a Path object from the path string.
         Path watchedPath = FileSystems.getDefault().getPath(folder);
         
         final SummaryReport summaryReport = new SummaryReport();
         
         try (DirectoryStream<Path> stream = Files.newDirectoryStream(watchedPath))
         {
            // Loop through all files in the directory.
            for (Path file: stream)
            {
               // Look for Oasis report files.
               if (file.getFileName().toString().endsWith(".rpt"))
               {
                  OasisReport report = new OasisReport();
                  
                  // Parse
                  report.parse(file.toFile());
                  
                  // Add to summary.
                  summaryReport.addReport(report);
               }
            }
         }
         catch (IOException | ParseException e)
         {
            e.printStackTrace();
         }
         
         if (summaryReport.size() > 0)
         {
            emailNotification(watchedPath, summaryReport);
         }
      }
   }
   
   public static void archive(SummaryReport.Shift shift)
   {
      // Get the appropriate watched folders from the properties file.
      String[] folders = getFolders(shift);
      
      // Get the archive folder from the properties file.
      String archiveFolder = properties.getProperty("archive");
      
      for (String folder : folders)
      {
         // Create a Path object from the folder string.
         Path folderPath = FileSystems.getDefault().getPath(folder);
         
         // Create a Path object from the archive folder string.
         String[] subFolders = folder.split("\\\\");
         String watchFolderName = subFolders[subFolders.length - 1];
         String archiveSubFolder = archiveFolder + "\\" + watchFolderName;
         Path archivePath = FileSystems.getDefault().getPath(archiveSubFolder);
         
         try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath))
         {
            // Loop through all files in the directory.
            for (Path file: stream)
            {
               // Look for Oasis report files.
               if ((file.getFileName().toString().endsWith(".rpt")) ||
                   (file.getFileName().toString().endsWith(".xls")))
               {
                  Archive.archive(file, archivePath);
               }
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
   }
   
   public static void folderWatch()
   {
      try
      {
         watchService = FileSystems.getDefault().newWatchService();
         
         // Get the appropriate watched folders from the properties file.
         String[] folders = getFolders(Shift.ALL);
         
         for (String folder : folders)
         {
            System.out.format("Monitoring folder: %s\n",  folder);
            
            // Create a Path object from the path string.
            Path watchedPath = FileSystems.getDefault().getPath(folder);
          
            // Register the watch service for this path.
            watchedPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
         }
        
         watchLoop();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
   
   static private void watchLoop()
   {
      while (true)
      {

         // Wait for key to be signaled.
         WatchKey key;
         try 
         {
             key = watchService.take();
         }
         catch (InterruptedException x)
         {
             return;
         }
         
         for (WatchEvent<?> event: key.pollEvents())
         {
             WatchEvent.Kind<?> kind = event.kind();

             // This key is registered only for ENTRY_CREATE events, but an OVERFLOW event can
             // occur regardless if events are lost or discarded.
             if (kind == StandardWatchEventKinds.OVERFLOW)
             {
                 continue;
             }
             
             // Extract the watched folder.
             Path watchedPath = (Path)key.watchable();

             // The filename is the context of the event.
             @SuppressWarnings("unchecked")
             WatchEvent<Path> ev = (WatchEvent<Path>)event;
             String filename = ev.context().toString();
             
             Path fullPath = Paths.get(watchedPath.toString() + "\\" + filename);
             
             if (isWatchTime() && debounce(filename, System.currentTimeMillis()))
             {
                onNewFileDetected(fullPath);
             }
         }

         // Reset the key -- this step is critical if you want to receive further watch events.  
         // If the key is no longer valid, the directory is inaccessible so exit the loop.
         boolean valid = key.reset();
         if (!valid)
         {
             break;
         }
      }      
   }
   
   static private void onNewFileDetected(final Path path)
   {
      final int DELAY = 250;  // milliseconds
      
      final String filename = path.getFileName().toString();
         
      // Execute on a delay.
      // Note: We were seeing an IO Exception when we attempted to process this file immediately after it was added to the folder.
      Timer delayTimer = new Timer();
      delayTimer.schedule(new TimerTask()
      {
         @Override
         public void run()
         {
            if (filename.contains(".rpt"))
            {
               System.out.format("Detected file addition to %s: %s\n", path.toString(), filename);
               
               onNewRptReport(path);
            }
            else if (filename.contains(".xls"))
            {
               System.out.format("Detected file addition to %s: %s\n", path.toString(), filename);
               
               onNewXlsReport(path);
            }
            else
            {
               System.out.format("Ignoring non-Oasis report file [%s].\n", filename);
            }
         }
      }, DELAY);
   }
      
   static private void onNewRptReport(Path path)
   {
      try
      {
         // Parse the Oasis report file.
         OasisReport report = new OasisReport();
         report.parse(path.toFile());
         
         System.out.format("Notifying %s\n", properties.getProperty("mail.to"));
         emailNotification(path, report);
         
         System.out.format("Uploading report summary to server.\n");
         String serverUrl = properties.getProperty("server.url");
         serverUpload(serverUrl, report);
      }
      catch (IOException | ParseException e)
      {
         System.out.format("Exception: %s\n", e.toString());
      }
   }
   
   static private void onNewXlsReport(Path srcPath)
   {
      Path targetPath = Paths.get(srcPath.toString().replace(".xls", ".rpt"));
      
      // Convert to a .rpt file.
      XlsToRpt.convert(srcPath.toString(), targetPath.toString());
      
      if (properties.containsKey("openOnXlsConversion") &&
          Boolean.valueOf(properties.getProperty("openOnXlsConversion")))
      {
         try
         {
            // Open up the new file in the Oasis report program for editing custom fields.
            Desktop.getDesktop().open(targetPath.toFile());
         }
         catch (IOException e)
         {
            System.out.format("Exception: %s\n", e.toString());
         }
      }
   }
   
   static private void emailNotification(Path path, SummaryReport report)
   {
      String[] receipients = properties.getProperty("mail.to").split(",");
      String host = properties.getProperty("mail.server");
      final String user = properties.getProperty("mail.user");
      final String password = properties.getProperty("mail.password");
      String port = properties.getProperty("mail.port");
      //String subject = properties.getProperty("mail.subject");
      //String body = properties.getProperty("mail.body");
      
      String subject = createEmailSubject(report);
      String body = createEmailBody(path, report);

      Properties mailProperties = new Properties();  
      mailProperties.put("mail.smtp.auth", "true");
      mailProperties.put("mail.smtp.starttls.enable", "true");
      mailProperties.put("mail.smtp.host", host);
      mailProperties.put("mail.smtp.port", port);
      mailProperties.put("mail.smtp.socketFactory.port", port);
      mailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

      Session session = Session.getInstance(
         mailProperties,
         new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication(user, password);
            }
         });
          
      // Compose the message  
      try
      {  
          MimeMessage message = new MimeMessage(session);
          
          for (String receipient : receipients)
          {
             message.addRecipient(Message.RecipientType.TO, new InternetAddress(receipient));
          }
          
          message.setFrom(new InternetAddress(user));  
 
          message.setSubject(subject);  
          message.setText(body);
          
          // Create a multipart message.
          Multipart multipart = new MimeMultipart();
          
          // Part 1: Message body.
          BodyPart messageBodyPart = new MimeBodyPart();
          messageBodyPart.setContent(body , "text/html");  // Create HTML content.
          multipart.addBodyPart(messageBodyPart);

          // Part 2:  attachment
          SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yy");
          String dateString = formatter.format(new Date());
          String summaryFilename = "OasisInspectionSummary_" + path.getFileName().toString() + "_" + dateString + ".html";                
          messageBodyPart = new MimeBodyPart();
          DataSource source = new ByteArrayDataSource(body.getBytes(), "text/html");
          messageBodyPart.setDataHandler(new DataHandler(source));
          messageBodyPart.setFileName(summaryFilename);
          multipart.addBodyPart(messageBodyPart);

          // Send the complete message parts
          message.setContent(multipart);
         
         // Send the message.  
         Transport.send(message);  
          
         System.out.println("Message was sent successfully.");  
           
       }
       catch (MessagingException e)
       {
          e.printStackTrace();
       }      
   }
   
   static private void emailNotification(Path path, OasisReport report)
   {
      String[] receipients = properties.getProperty("mail.to").split(",");
      String host = properties.getProperty("mail.server");
      final String user = properties.getProperty("mail.user");
      final String password = properties.getProperty("mail.password");
      String port = properties.getProperty("mail.port");
      //String subject = properties.getProperty("mail.subject");
      //String body = properties.getProperty("mail.body");
      
      String subject = createEmailSubject(report);
      String body = createEmailBody(path, report);

      Properties mailProperties = new Properties();  
      mailProperties.put("mail.smtp.auth", "true");
      mailProperties.put("mail.smtp.starttls.enable", "true");
      mailProperties.put("mail.smtp.host", host);
      mailProperties.put("mail.smtp.port", port);
      mailProperties.put("mail.smtp.socketFactory.port", port);
      mailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

      Session session = Session.getInstance(
         mailProperties,
         new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication(user, password);
            }
         });
          
      // Compose the message  
      try
      {  
          MimeMessage message = new MimeMessage(session);
          
          for (String receipient : receipients)
          {
             message.addRecipient(Message.RecipientType.TO, new InternetAddress(receipient));
          }
          
          message.setFrom(new InternetAddress(user));  
 
          message.setSubject(subject);  
          message.setText(body);
          
          // Create a multipart message.
          Multipart multipart = new MimeMultipart();
          
          // Part 1: Message body.
          BodyPart messageBodyPart = new MimeBodyPart();
          messageBodyPart.setContent(body , "text/html");  // Create HTML content.
          multipart.addBodyPart(messageBodyPart);

          // Part 2:  attachment
          messageBodyPart = new MimeBodyPart();
          DataSource source = new FileDataSource(path.toFile());
          messageBodyPart.setDataHandler(new DataHandler(source));
          messageBodyPart.setFileName(path.getFileName().toString());
          multipart.addBodyPart(messageBodyPart);

          // Send the complete message parts
          message.setContent(multipart);
         
         // Send the message.  
         Transport.send(message);  
          
         System.out.println("Message was sent successfully.");  
           
       }
       catch (MessagingException e)
       {
          e.printStackTrace();
       }
   }
   
   static private void serverUpload(String urlString, OasisReport report)
   {
      StringBuilder sb = new StringBuilder();

      // Format date for PHP and GET request.
      Date date = report.getDate();
      String dateString = "";
      if (date != null)
      {
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
         dateString = formatter.format(report.getDate());
         dateString = dateString.replaceAll(" ", "%20");
      }
      
      // Url
      sb.append(urlString);
      sb.append("?");
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
            
            System.out.format("Server response: %s\n", response.toString());
         }
         else
         {
            System.out.format("Bad server request: %s\n", sb.toString());
         }
      }
      catch (SocketTimeoutException | ConnectException e)
      {
         System.out.format("Failed to contact server.\n");
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   static private void loadProperties() throws IOException
   {
      properties = new Properties();
      FileInputStream in = new FileInputStream("./default.properties");
      properties.load(in);
      in.close();
   }
   
   static private String[] getFolders(SummaryReport.Shift shift)
   {
      String[] folders = {};
      String[] firstShiftFolders = {};
      String[] secondShiftFolders = {};
      
      if (properties.containsKey("folders.firstShift"))
      {
         firstShiftFolders = properties.getProperty("folders.firstShift").split(",");
      }
      
      if (properties.containsKey("folders.secondShift"))
      {
         secondShiftFolders = properties.getProperty("folders.secondShift").split(",");
      }
      
      switch (shift)
      {
         case FIRST:
         {
            folders = firstShiftFolders;
            break;
         }
         
         case SECOND:
         {
            folders = secondShiftFolders;
            break;
         }
         
         case ALL:
         default:
         {
            folders = Arrays.copyOf(firstShiftFolders, firstShiftFolders.length + secondShiftFolders.length);
            System.arraycopy(secondShiftFolders, 0, folders, firstShiftFolders.length, secondShiftFolders.length);
            break;
         }
      }
      
      return (folders);
   }
   
   static boolean debounce(String filename, long eventTime)
   {
      final long DEBOUNCE_TIME = 100;  // milliseconds
      
      boolean shouldProcess = ((!filename.equals(lastFilename)) ||
                               ((eventTime - lastEventTime) > DEBOUNCE_TIME));

      lastEventTime = eventTime;
      lastFilename = filename;
      
      return (shouldProcess);
   }
   
   static boolean isWatchTime()
   {
      boolean isTime = true;
      
      // TODO: Does not work!!!
      
      if (properties.containsKey("startTime") &&
          properties.containsKey("endTime"))
      {
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeZone(TimeZone.getDefault());
         
         // Get the current time.
         calendar.setTime(new Date());
         Date now = calendar.getTime();

         // Get today (00:00 AM)
         calendar.set(Calendar.MILLISECOND, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         final Date today = calendar.getTime(); // 00:00 AM of today
         
         System.out.printf("Now: %s\n", now.toString());  
         System.out.printf("Today: %s\n", today.toString());
         System.out.printf("Start: %s\n", properties.getProperty("startTime").toString());
         System.out.printf("End: %s\n", properties.getProperty("endTime").toString());

         try
         {
            DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
            formatter.setTimeZone(TimeZone.getDefault());
            long currentTime = now.getTime();
            long startTime = today.getTime() + formatter.parse(properties.getProperty("startTime")).getTime();
            long endTime = today.getTime() + formatter.parse( properties.getProperty("endTime")).getTime();
            
            isTime = ((currentTime >= startTime) &&
                      (currentTime <= endTime));
         }
         catch (java.text.ParseException e)
         {
            isTime = true;
         }
      }

      return (isTime);
   }
   
   static String createEmailSubject(OasisReport report)
   {
      String subject = 
            String.format("Inspector %s, machine %s, %d parts, %.2f%% efficiency, %d failures",
                          report.getEmployeeNumber(),
                          report.getMachineNumber(),
                          report.getPartCount(),
                          report.getEfficiency(),
                          report.getFailureCount());
      
      return (subject);
   }
   
   static String createEmailBody(Path path, OasisReport report)
   {
      final String EMAIL_PROLOGUE_DIV = "<div id=\"email-prologue-container\">";
      
      String body = report.toHtml();
      
      String prologue = String.format("A new Oasis inspection report has been submitted in: <br/>%s", path.toString());
      
      StringBuilder stringBuilder = new StringBuilder(body);
      
      int pos = stringBuilder.indexOf(EMAIL_PROLOGUE_DIV) + EMAIL_PROLOGUE_DIV.length() + 1;  // +1 for \n
      
      stringBuilder.insert(pos, prologue);
      
      body = stringBuilder.toString();
      
      return (body);
   }
   
   static String createEmailSubject(SummaryReport report)
   {
      String subject = 
            String.format("New Oasis shift summary report");
      
      return (subject);
   }
   
   static String createEmailBody(Path path, SummaryReport report)
   {
      final String EMAIL_PROLOGUE_DIV = "<div id=\"email-prologue-container\">";
      
      String body = report.toHtml();
      
      String prologue = String.format("A new Oasis shift summary report has been created from folder: \"%s\"", path.toString());
      
      StringBuilder stringBuilder = new StringBuilder(body);
      
      int pos = stringBuilder.indexOf(EMAIL_PROLOGUE_DIV) + EMAIL_PROLOGUE_DIV.length() + 1;  // +1 for \n
      
      stringBuilder.insert(pos, prologue);
      
      body = stringBuilder.toString();
      
      return (body);
   }
   
   static private Properties properties;
   
   static WatchService watchService;
   
   static String lastFilename;
   
   static long lastEventTime;
   
}
