package com.toast.foldlerwatch.archive;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Archive
{
   public static void archive(Path file, Path archivePath) throws IOException
   {
      // Get the date of the report file.
      Date reportDate = new Date(file.toFile().lastModified());
      
      // Convert to a string that we'll use in the archive folder.
      SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
      String dateString = formatter.format(reportDate);
      
      // Create the archive folder for this date range.
      String archiveSubFolder = archivePath.toString() + "\\" + dateString + "\\";
      Path archiveSubPath = FileSystems.getDefault().getPath(archiveSubFolder + "\\");
      
      // If it doesn't exist, create it on the file system.
      if (Files.notExists(archiveSubPath))
      {
         boolean wasCreated = new File(archiveSubFolder).mkdirs();
         if (!wasCreated)
         {
             System.out.format("Failed to create archive folder [%s].\n", archiveSubFolder);
         }
      }
      
      // Create the path for the archived file.
      Path archiveFile = FileSystems.getDefault().getPath(archiveSubFolder + "\\" + file.getFileName().toString());
      
      // Move the file.
      Files.move(file, archiveFile);
   }
}
