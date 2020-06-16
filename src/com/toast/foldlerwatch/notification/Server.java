package com.toast.foldlerwatch.notification;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

import com.toast.foldlerwatch.InspectionMonitor;
import com.toast.foldlerwatch.common.LogLevel;
import com.toast.foldlerwatch.config.Configuration;

public class Server
{
   static public void serverUpload(Path path)
   {
      String charset = "UTF-8";
      String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
      String CRLF = "\r\n"; // Line separator required by multipart/form-data.
      
      String urlString = Configuration.serverConfig.url;

      try
      {
         URLConnection connection = new URL(urlString).openConnection();
         connection.setDoOutput(true);
         connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

          OutputStream output = connection.getOutputStream();
          PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

          /*
          // Send normal param.
          writer.append("--" + boundary).append(CRLF);
          writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
          writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
          writer.append(CRLF).append(param).append(CRLF).flush();
          */

          // Send text file.
          writer.append("--" + boundary).append(CRLF);
          writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + path.getFileName() + "\"").append(CRLF);
          writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
          writer.append(CRLF).flush();
          Files.copy(path, output);
          output.flush(); // Important before continuing with writer!
          writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

          // End of multipart/form-data.
          writer.append("--" + boundary + "--").append(CRLF).flush();

          // Request is lazily fired whenever you need to obtain information about response.
          int responseCode = ((HttpURLConnection) connection).getResponseCode();
          System.out.println(responseCode); // Should be 200
      }
      catch (Exception e)
      {
         InspectionMonitor.log(LogLevel.WARNING, "File upload failed.");
      }
   }
}
