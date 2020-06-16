package com.toast.foldlerwatch.config;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Configuration
{
   public static void load() throws IOException, ParseException
   {
      JSONParser parser = new JSONParser();
      Reader reader = new FileReader("properties.json");

      JSONObject jsonObj = (JSONObject)parser.parse(reader);

      // Email server config
      JSONObject emailServerObj = (JSONObject)jsonObj.get("emailServerConfig");
      emailServerConfig = new EmailServerConfig();
      emailServerConfig.host = (String)emailServerObj.get("host");
      emailServerConfig.user = (String)emailServerObj.get("user");
      emailServerConfig.password = (String)emailServerObj.get("password");
      emailServerConfig.port = ((Long)emailServerObj.get("port")).intValue();
      
      // Server config
      JSONObject serverObj = (JSONObject)jsonObj.get("serverConfig");
      serverConfig = new ServerConfig();
      serverConfig.url = (String)serverObj.get("url");
      
      // Folders
      JSONArray folderObjs = (JSONArray)jsonObj.get("folders");
      for (Object folderObj : folderObjs)
      {
         FolderConfig folderConfig = new FolderConfig();
         folderConfig.path = (String)((JSONObject)folderObj).get("path");
         folderConfig.isEnabled = (Boolean)((JSONObject)folderObj).get("isEnabled");
         folders.add(folderConfig);
      }
      
      // Employees
      JSONArray employeeObjs = (JSONArray)jsonObj.get("employees");
      for (Object employeeObj : employeeObjs)
      {
         EmployeeConfig employeeConfig = new EmployeeConfig();
         employeeConfig.firstName = (String)((JSONObject)employeeObj).get("firstName");
         employeeConfig.lastName = (String)((JSONObject)employeeObj).get("lastName");
         employeeConfig.employeeNumber = ((Long)((JSONObject)employeeObj).get("employeeNumber")).intValue();
         employees.put(employeeConfig.employeeNumber,  employeeConfig);
      }
      
      // Email addresses
      JSONArray emailObjs = (JSONArray)jsonObj.get("emailAddresses");
      for (Object emailObj : emailObjs)
      {
         EmailConfig emailConfig = new EmailConfig();
         emailConfig.address = (String)((JSONObject)emailObj).get("address");
         emailConfig.isEnabled = (Boolean)((JSONObject)emailObj).get("isEnabled");
         emailAddresses.add(emailConfig);
      }
      
      // Archive folder
      archiveFolder = (String)jsonObj.get("archiveFolder");

      reader.close();
   }
   
   public static void save() throws IOException
   {
      JSONObject jsonObj = new JSONObject();
      
      // Email server config
      JSONObject emailServerObj = new JSONObject();
      emailServerObj.put("host",  emailServerConfig.host);
      emailServerObj.put("user",  emailServerConfig.user);
      emailServerObj.put("password",  emailServerConfig.password);
      emailServerObj.put("port",  emailServerConfig.port);
      jsonObj.put("emailServerConfig",  emailServerObj);

      // Server config
      JSONObject serverObj = new JSONObject();
      serverObj.put("url", serverConfig.url);
      jsonObj.put("serverConfig",  serverObj);
      
      // Folders
      JSONArray folderObjs = new JSONArray();
      for (FolderConfig folderConfig : folders)
      {
         JSONObject folderConfigObj = new JSONObject();
         folderConfigObj.put("path",  folderConfig.path);
         folderConfigObj.put("isEnabled",  folderConfig.isEnabled);
         
         folderObjs.add(folderConfigObj);
      }
      jsonObj.put("folders",  folderObjs);
      
      // Employees
      JSONArray employeeObjs = new JSONArray();
      for (EmployeeConfig employeeConfig : employees.values())
      {
         JSONObject employeeObj = new JSONObject();
         employeeObj.put("firstName",  employeeConfig.firstName);
         employeeObj.put("lastName",  employeeConfig.lastName);
         employeeObj.put("employeeNumber",  employeeConfig.employeeNumber);
         
         employeeObjs.add(employeeObj);
      }
      jsonObj.put("employees",  employeeObjs);
      
      // Email addresses
      JSONArray emailObjs = new JSONArray();
      for (EmailConfig emailConfig : emailAddresses)
      {
         JSONObject emailObj = new JSONObject();
         emailObj.put("path",  emailConfig.address);
         emailObj.put("isEnabled",  emailConfig.isEnabled);
         
         emailObjs.add(emailObj);
      }
      jsonObj.put("emailAddresses",  emailObjs);

      FileWriter writer = new FileWriter("saved.json");
      String test = jsonObj.toJSONString();
      writer.write(jsonObj.toJSONString());
      writer.close();
   }
   
   public static ArrayList<FolderConfig> folders = new ArrayList<>();
   
   public static String archiveFolder;
   
   public static HashMap<Integer, EmployeeConfig> employees = new HashMap<>();
   
   public static ArrayList<EmailConfig> emailAddresses = new ArrayList<>();
   
   public static EmailServerConfig emailServerConfig;
   
   public static ServerConfig serverConfig;
}
