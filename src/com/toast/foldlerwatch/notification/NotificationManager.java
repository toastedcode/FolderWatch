package com.toast.foldlerwatch.notification;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.toast.foldlerwatch.InspectionMonitor;
import com.toast.foldlerwatch.common.LogLevel;
import com.toast.foldlerwatch.oasisreport.OasisReport;

public class NotificationManager
{
   public void queueNotification(Path path)
   {
      final int DELAY = 5000;  // milliseconds
   
      notifications.add(new Notification(path));
      
      if (notifyTimer == null)
      {
         notifyTimer = new Timer();
         
         notifyTimer.scheduleAtFixedRate(new TimerTask()
         {
            @Override
            public void run()
            {
               processNotifications();
            }
            
         }, DELAY, DELAY);
      }
   }
   
   private void processNotifications()
   {
      ArrayList<Notification> removedNotifications = new ArrayList<>();
      
      for (Notification notification : notifications)
      {
         String extension = notification.path.toString().substring(notification.path.toString().lastIndexOf('.'));
         
         if (!extension.equals(".rpt"))
         {
            InspectionMonitor.log(LogLevel.WARNING, String.format("Ignoring file non-Oasis report file [%s]", notification.path.getFileName()));
            
            // Remove notification.
            removedNotifications.add(notification);
         }
         else
         {
            // Parse the Oasis report file.
            boolean success = false;
            OasisReport report = new OasisReport();
            try
            {
               success = report.parse(notification.path.toFile());
            }
            catch (Exception e)
            {
               
            }
            
            if (success == false)
            {
               InspectionMonitor.log(LogLevel.WARNING, String.format("Failed to parse the file [%s].\n", notification.path.toString()));
               
               removedNotifications.add(notification);
            }
            else
            {
               /*
               if (!notification.processedEmailNotification)
               {
                  System.out.format("Email notification.\n");
                  
                  Email.emailNotification(notification.path, report, new EmailListener(){
                     @Override
                     public void onSend()
                     {
                        notification.processedEmailNotification = true;
                     }
   
                     @Override
                     public void onSendFailure()
                     {
                        notification.processedEmailNotification = true;
                        InspectionMonitor.log(LogLevel.WARNING, "Failed to send email.");
                     }
                  });

                  notification.processedEmailNotification = true;
               }
               */
               notification.processedEmailNotification = true;
               
               if (!notification.processedServerNotification)
               {
                  
                  Server.serverUpload(notification.path);
                  
                  notification.processedServerNotification = true;
               }
               
               if (notification.isComplete())
               {
                  // Remove notification.
                  removedNotifications.add(notification);
               }
            }
         }
      }
      
      // Remove complete notifications.
      for (Notification notification : removedNotifications)
      {
         notifications.remove(notification);
      }
   }
   
   private ArrayList<Notification> notifications = new ArrayList<>();
   
   private Timer notifyTimer = null;
}
