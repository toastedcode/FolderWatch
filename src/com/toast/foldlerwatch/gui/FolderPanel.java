package com.toast.foldlerwatch.gui;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.toast.foldlerwatch.config.Configuration;
import com.toast.foldlerwatch.config.FolderConfig;

public class FolderPanel extends JPanel
{
   private static final long serialVersionUID = 1L;

   public FolderPanel()
   {
      String[] columnNames = {"Folder", "Eabled", ""};
      
      initialize(Configuration.folders);
      
      folderTable = new JTable(data, columnNames);
      
      setLayout(new BorderLayout());
      add(folderTable.getTableHeader(), BorderLayout.PAGE_START);
      add(folderTable, BorderLayout.CENTER);
      
      initialize(Configuration.folders);
   }
   
   private void initialize(Collection<FolderConfig> folders)
   {
      data = new Object[folders.size()][3];
      
      int row = 0;
      for (FolderConfig folder : folders)
      {
         data[row][0] = folder.path;
         
         JCheckBox isEnabledCheckBox = new JCheckBox();
         /*
         toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
               Configuration.folders.f
            }
         });
         */
         
         data[row][1] = isEnabledCheckBox;
         
         JButton deleteButton = new JButton("Delete");
         
         data[row][2] = deleteButton;
         
         row++;
      }
   }
   
   private JTable folderTable;
   
   private Object[][] data;
}
