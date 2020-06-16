package com.toast.foldlerwatch.filemanager;

import java.nio.file.Path;

public interface FileManagerListener
{
   public void onNewFile(Path path);
   
   public void onUpdatedFile(Path path);
   
   public void onDeletedFile(Path path);
}
