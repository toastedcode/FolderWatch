package com.toast.foldlerwatch.notification;

import java.nio.file.Path;

public class Notification
{
   public Notification(Path path)
   {
      this.path = path;
      processedEmailNotification = false;
      processedServerNotification = false;
   }
   
   public boolean isComplete()
   {
      return (processedEmailNotification && processedServerNotification);
   }
   
   public Path path;
   
   public boolean processedEmailNotification;
   
   public boolean processedServerNotification;
}
