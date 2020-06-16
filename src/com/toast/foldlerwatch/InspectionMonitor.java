package com.toast.foldlerwatch;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import javax.swing.SwingUtilities;

import com.toast.foldlerwatch.common.LogLevel;
import com.toast.foldlerwatch.config.Configuration;
import com.toast.foldlerwatch.config.FolderConfig;
import com.toast.foldlerwatch.filemanager.FileManager;
import com.toast.foldlerwatch.filemanager.FileManagerListener;
import com.toast.foldlerwatch.gui.Gui;
import com.toast.foldlerwatch.notification.NotificationManager;

public class InspectionMonitor implements Runnable, FileManagerListener
{
   public static void main(final String args[])
   {
      try
      {
         // Spawn the GUI thread.
         // Note: invokeLater() causes this to be executed on the event dispatch thread.
         SwingUtilities.invokeLater(
               instance = new InspectionMonitor());
      }
      catch(Exception e)
      {
         System.out.print("Failed to start application.\n");            
      }
   }

   @Override
   public void run()
   {
      try
      {
         // Load config.
         Configuration.load();
         
         // Create GUI.
         gui = new Gui(this);
         
         // Create a FileManager
         fileManager = new FileManager();
         fileManager.addListener(this);
         
         // Add monitored files.
         for (FolderConfig folder : Configuration.folders)
         {
            if (folder.isEnabled)
            {
               try
               {
                  // Create a Path object from the path string.
                  Path path = FileSystems.getDefault().getPath(folder.path);
                  
                  // Begin folder monitoring.
                  fileManager.monitor(path);
                  
                  gui.log(LogLevel.DEBUG, String.format("Monitoring \"%s\"",  folder.path));
               }
               catch (Exception e)
               {
                  gui.log(LogLevel.WARNING, String.format("Could not being monitoring of \"%s\"",  folder.path));
               }
            }
         }
         
         fileManager.start();
         
         // Create a NotificationManager
         notificationManager = new NotificationManager();
      }
      catch (Exception e)
      {
         
      }
   }
   
   public static void log(LogLevel logLevel, String text)
   {
      SwingUtilities.invokeLater(new Runnable(){
         @Override
         public void run()
         {
            instance.gui.log(logLevel, text);
         }
      });
   }

   @Override
   public void onNewFile(Path path)
   {
      System.out.format("InspectionMonitor.onNewFile: %s added\n", path.toString());
      log(LogLevel.DEBUG, path.getFileName() + " added");
      
      notificationManager.queueNotification(path);
   }

   @Override
   public void onUpdatedFile(Path path)
   {
      gui.log(LogLevel.DEBUG, path.getFileName() + " updated");
   }

   @Override
   public void onDeletedFile(Path path)
   {
      gui.log(LogLevel.DEBUG, path.getFileName() + " deleted");
   }

   private static InspectionMonitor instance;
   
   private Gui gui;
   
   private FileManager fileManager;
   
   private NotificationManager notificationManager;
}
