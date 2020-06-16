package com.toast.foldlerwatch.notification;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import com.toast.foldlerwatch.config.Configuration;
import com.toast.foldlerwatch.config.EmailConfig;
import com.toast.foldlerwatch.oasisreport.OasisReport;
import com.toast.foldlerwatch.summaryreport.SummaryReport;

public class Email
{
   static public void emailNotification(Path path, SummaryReport report, EmailListener listener)
   {
      String subject = createEmailSubject(report);
      String body = createEmailBody(path, report);

      Properties mailProperties = new Properties();  
      mailProperties.put("mail.smtp.auth", "true");
      mailProperties.put("mail.smtp.starttls.enable", "true");
      mailProperties.put("mail.smtp.host", Configuration.emailServerConfig.host);
      mailProperties.put("mail.smtp.port", Configuration.emailServerConfig.port);
      mailProperties.put("mail.smtp.socketFactory.port", Configuration.emailServerConfig.host);
      mailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

      Session session = Session.getInstance(
         mailProperties,
         new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication(Configuration.emailServerConfig.user, Configuration.emailServerConfig.password);
            }
         });
          
      // Compose the message  
      try
      {  
          MimeMessage message = new MimeMessage(session);
          
          for (EmailConfig emailConfig : Configuration.emailAddresses)
          {
             if (emailConfig.isEnabled)
             {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailConfig.address));
             }
          }
          
          message.setFrom(new InternetAddress(Configuration.emailServerConfig.user));  
 
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
         
         listener.onSend();
           
       }
       catch (MessagingException e)
       {
          listener.onSendFailure();
       }      
   }
   
   static public void emailNotification(Path path, OasisReport report, EmailListener listener)
   {
      String subject = createEmailSubject(report);
      String body = createEmailBody(path, report);

      Properties mailProperties = new Properties();  
      mailProperties.put("mail.smtp.auth", "true");
      mailProperties.put("mail.smtp.starttls.enable", "true");
      mailProperties.put("mail.smtp.host", Configuration.emailServerConfig.host);
      mailProperties.put("mail.smtp.port", Configuration.emailServerConfig.port);
      mailProperties.put("mail.smtp.socketFactory.port", Configuration.emailServerConfig.port);
      mailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

      Session session = Session.getInstance(
         mailProperties,
         new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication(Configuration.emailServerConfig.user, Configuration.emailServerConfig.user);
            }
         });
          
      // Compose the message  
      try
      {  
          MimeMessage message = new MimeMessage(session);
          
          for (EmailConfig emailConfig : Configuration.emailAddresses)
          {
             if (emailConfig.isEnabled)
             {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailConfig.address));
             }
          }
          
          message.setFrom(new InternetAddress(Configuration.emailServerConfig.user));  
 
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
         
         listener.onSend();
           
       }
       catch (MessagingException e)
       {
          e.printStackTrace();
          
          listener.onSendFailure();
       }
   }
   
   private static String createEmailSubject(OasisReport report)
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
   
   private static String createEmailBody(Path path, OasisReport report)
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
   
   private static String createEmailSubject(SummaryReport report)
   {
      String subject = 
            String.format("New Oasis shift summary report");
      
      return (subject);
   }
   
   private static String createEmailBody(Path path, SummaryReport report)
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
}
