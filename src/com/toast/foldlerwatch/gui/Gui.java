package com.toast.foldlerwatch.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import com.toast.foldlerwatch.InspectionMonitor;
import com.toast.foldlerwatch.common.LogLevel;

public class Gui extends JFrame
{
   private static final long serialVersionUID = 1L;
   
   private enum Tab
   {
      LOG("Event Log"),
      FOLDER("Folder Config"),
      USER("User Config"),
      EMAIL("Email Config"),
      SERVER("Server Config");

      public String getTitle()
      {
         return (title);
      }
      
      private String title;
      
      private Tab(String title)
      {
         this.title = title;
      }
   };

   public Gui(InspectionMonitor inspectionMonitor)
   {
      this.inspectionMonitor = inspectionMonitor;
      
      loadImages();
      
      setIconImage(images.get("icon"));
      setTitle("Oasis Inspection Monitor");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(400, 400);
      
      tabbedPane = new JTabbedPane();

      tabbedPane.addTab(Tab.LOG.getTitle(), new LogPanel());
      tabbedPane.addTab(Tab.FOLDER.getTitle(), new FolderPanel());
      //tabbedPane.addTab("Log", new UserPanel());
      //tabbedPane.addTab("Log", new EmailPanel());
      //tabbedPane.addTab("Log", new ServerPanel());
      
      getContentPane().add(tabbedPane, BorderLayout.CENTER);
      
      setVisible(true);
      
      addToSystemTray();
   }
   
   public void log(
      LogLevel logLevel, 
      String text)
   {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yy hh:mm:ss a");
      
      String logText = String.format("%s %s\n", simpleDateFormat.format(new Date()), text);
      
      StyledDocument doc = ((LogPanel)getTab(Tab.LOG)).getStyledDocument();
      
      try
      {
         doc.insertString(doc.getLength(),  logText, null);
      } 
      catch (BadLocationException e)
      {
         e.printStackTrace();
      }    
   }
   
   private void addToSystemTray()
   {
      if (SystemTray.isSupported())
      {
         final PopupMenu popup = new PopupMenu();
     
         final SystemTray tray = SystemTray.getSystemTray();

         trayIcon = new TrayIcon(images.get("icon"));         
         
         trayIcon.setToolTip("Oasis Inspection Monitor");
    
         // Create a pop-up menu components
         MenuItem openMenuItem = new MenuItem("Open");
         MenuItem exitMenuItem = new MenuItem("Exit");
         CheckboxMenuItem enableMenuItem = new CheckboxMenuItem("Enabled");
         
         openMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               setVisible(true);
               setExtendedState(JFrame.NORMAL);
            }
         });

         exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               tray.remove(trayIcon);
               System.exit(0);
            }
         });
         
         enableMenuItem.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
               if (e.getStateChange() == ItemEvent.SELECTED)
               {
                  // Enable
               }
               else
               {
                  // Enable
               }
            }
         });
    
         //Add components to pop-up menu
         popup.add(openMenuItem);
         popup.add(exitMenuItem);
         popup.add(enableMenuItem);
    
         trayIcon.setPopupMenu(popup);
         
         // Setup GUI to minimize to system tray.
         addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e)
            {
               switch (e.getNewState())
               {
                  case ICONIFIED:
                  case 7:
                  {
                     setVisible(false);
                     break;
                  }
                  
                  case MAXIMIZED_BOTH:
                  case NORMAL:
                  default:
                  {
                     setVisible(true);
                     break;
                  }
               }
            }
         });
    
         try
         {
            tray.add(trayIcon);
         }
         catch (AWTException e)
         {
            System.out.println("TrayIcon could not be added.");
         }
      }
   }
   
   private void loadImages()
   {
      images.put("icon", createImage("server-computer-icon-small.png", "tray icon"));
   }
   
   private static Image createImage(
         String path, 
         String description)
   {
      Image image = null;
      
      URL imageURL = Gui.class.getResource(path);
        
      if (imageURL == null)
      {
         System.err.println("Resource not found: " + path);
      }
      else
      {
         image = new ImageIcon(imageURL, description).getImage();
      }
      
      return (image);
   }
   
   private Component getTab(Tab tab)
   {
      return (tabbedPane.getComponentAt(tabbedPane.indexOfTab(tab.getTitle())));
   }
   
   private InspectionMonitor inspectionMonitor;
   
   JTabbedPane tabbedPane;
   
   private TrayIcon trayIcon;
   
   JLabel ipAddressLabel;
   
   JLabel statusLabel;
   
   JLabel stateChangeTimeLabel;
   
   private Map<String, Image> images = new HashMap<>();;
}
