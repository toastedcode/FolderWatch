package com.toast.foldlerwatch.gui;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import com.toast.foldlerwatch.common.LogLevel;

public class LogPanel extends JTextPane
{
   private static final long serialVersionUID = 1L;

   public void log(LogLevel logLevel, String text)
   {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yy hh:mm:ss a");
      
      String logText = String.format("%s %s\n", simpleDateFormat.format(new Date()), text);
      
      StyledDocument doc = this.getStyledDocument();
      
      try
      {
         doc.insertString(doc.getLength(),  logText, null);
      } 
      catch (BadLocationException e)
      {
         e.printStackTrace();
      }    
   }
}
