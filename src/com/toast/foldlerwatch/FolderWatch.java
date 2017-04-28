package com.toast.foldlerwatch;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.String;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
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
                  summaryReport();
                  break;
               }
               
               case "-archive":
               {
                  archive();
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
   
   public static void summaryReport()
   {
      // Get all the watched folders from the properties file.
      String[] folders = properties.getProperty("folders").split(",");
      
      for (String folder : folders)
      {
         // Create a Path object from the path string.
         watchedPath = FileSystems.getDefault().getPath(folder);
         
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
         catch (IOException e)
         {
            e.printStackTrace();
         }
         
         if (summaryReport.size() > 0)
         {
            emailNotification(watchedPath, summaryReport);
         }
      }
   }
   
   public static void archive()
   {
      // Get all the watched folders from the properties file.
      String[] folders = properties.getProperty("folders").split(",");
      
      // Get the archive folder from the properties file.
      String archiveFolder = properties.getProperty("archive");
      
      for (String folder : folders)
      {
         // Create a Path object from the folder string.
         Path folderPath = FileSystems.getDefault().getPath(folder);
         
         // Create a Path object from the archive folder string.
         String[] subFolders = folder.split("/");
         String watchFolderName = subFolders[subFolders.length - 1];
         String archiveSubFolder = archiveFolder + watchFolderName + "/";
         Path archivePath = FileSystems.getDefault().getPath(archiveSubFolder);
         
         try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath))
         {
            // Loop through all files in the directory.
            for (Path file: stream)
            {
               // Look for Oasis report files.
               if (file.getFileName().toString().endsWith(".rpt"))
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
         
         // Get all the watched folders from the properties file.
         String[] folders = properties.getProperty("folders").split(",");
         
         for (String folder : folders)
         {
            // Create a Path object from the path string.
            watchedPath = FileSystems.getDefault().getPath(folder);
          
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
             WatchEvent<Path> ev = (WatchEvent<Path>)event;
             String filename = ev.context().toString();
             
             Path fullPath = Paths.get(watchedPath.toString() + "\\" + filename);
             
             if (isWatchTime())
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
   
   static private void onNewFileDetected(Path path)
   {
      String filename = path.getFileName().toString();
      
      if (!filename.contains(".rpt"))
      {
         System.out.format("Ignoring non-Oasis report file [%s].", filename);
      }
      else
      {
         System.out.format("Detected file addition to %s: %s%n", watchedPath.toString(), filename);
         
         try
         {
            // Parse the Oasis report file.
            OasisReport report = new OasisReport();
            boolean success = report.parse(path.toFile());
            
            if (success == false)
            {
               System.out.format("Failed to parse the file [%s].\n", filename);
            }
            else
            {
               System.out.format("Notifying %s%n", properties.getProperty("mail.to"));
               emailNotification(path, report);
            }
         }
         catch (IOException e)
         {
            System.out.format("Exception: %s%n", e.toString());
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

   static private void loadProperties() throws IOException
   {
      properties = new Properties();
      FileInputStream in = new FileInputStream("./default.properties");
      properties.load(in);
      in.close();
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
   
   static Path watchedPath; 
   
}
