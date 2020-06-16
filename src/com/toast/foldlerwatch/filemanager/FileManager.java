package com.toast.foldlerwatch.filemanager;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;

public class FileManager
{
   public FileManager()
   {
      try
      {
         watchService = FileSystems.getDefault().newWatchService();
      }
      catch (Exception e)
      {
         System.out.format("Failed to initialize File Manager");
      }
   }
   
   public void start() throws IOException
   {
      if ((watchService != null) && !isRunning)
      {
         isRunning = true;

         // Watch folders on a separate thread.
         Runnable runnable = 
            new Runnable()
            {
               public void run()
               {
                  watchLoop();
               }
            };
         
         watchThread = new Thread(runnable);
         watchThread.start();
      }
   }
   
   public void stop()
   {
      isRunning = false;
   }
   
   public void addListener(FileManagerListener listener)
   {
      listeners.add(listener);
   }
   
   public void monitor(Path path) throws IOException
   {
      WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
      
      watchKeys.put(path.toString(), watchKey);
   }
   
   public void unmonitor(Path path)
   {
      WatchKey watchKey = watchKeys.get(path.toString());
      
      if (watchKey != null)
      {
         watchKey.cancel();
      }
   }
   
   private void watchLoop()
   {
      while (isRunning)
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
             
             // Notify listeners.
             for (FileManagerListener listener : listeners)
             {
                listener.onNewFile(fullPath);
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
   
   private Thread watchThread = null;
   
   private ArrayList<FileManagerListener> listeners = new ArrayList<>();
   
   private WatchService watchService;
   
   private HashMap<String, WatchKey> watchKeys = new HashMap<>();
   
   private boolean isRunning = false; 
}
