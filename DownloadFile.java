//------------------------------------------------------------------------------
// Download a file
// Philip R Brenan at gmail dot com, Appa Apps Ltd, 2017
//------------------------------------------------------------------------------
package com.appaapps.downloadFile;

import java.io.*;
import java.net.*;
import java.util.*;

abstract public class DownloadFile extends Thread                               // A thread used to download a Url to a file
 {public final int oneMegaByte = 1000000;                                       // Nffer size
  public final String downloadUrl;                                              // Url to read
  public final String outputFile;                                               // File to write
  public Long      contentLength = null;                                        // Length of content
  public long      progress      = 0;                                           // Content read so far
  public Integer   httpResponse  = null;                                        // Http response
  public Exception exception     = null;                                        // Exception encountered
  public Boolean   failed        = null;                                        // Download failed with either the httpResponse or exception indicating the error
  public Boolean   finished      = null;                                        // Download finished successfully
  public boolean   stop          = false;                                       // Stop downloading if this becomes true

  public DownloadFile(String DownloadUrl, String OutputFile)                    // Url to read, File to write
   {downloadUrl = DownloadUrl;
    outputFile  = OutputFile;
   }

  public void run()                                                             // Download and save content, start with Thread.start()
   {try
     {final URL url = new URL(downloadUrl);                                     // Read URL

      if (true)                                                                 // Get content length
       {final HttpURLConnection connection =
         (HttpURLConnection)url.openConnection();
        connection.connect();
        httpResponse = connection.getResponseCode();                            // HTTP response
        if (httpResponse != HttpURLConnection.HTTP_OK)
         {connection.disconnect();                                              // Disconnect on error
          failed = true;
          failed(this);
          return;
         }
        contentLength = (long)connection.getContentLength();                    // Record length
       }

      starting(this);                                                           // Ready to start downloading
      final BufferedInputStream i =                                             // Read content and save into a file
        new BufferedInputStream(url.openStream(), oneMegaByte);
      final byte[]              b = new byte[oneMegaByte];                      // Allocate buffer
      final FileOutputStream    o = new FileOutputStream(outputFile);           // File to write to

      for(int n = i.read(b); n != -1 && !stop; n = i.read(b))                   // Read data from url and save in a file
       {o.write(b, 0, n);
        progress += n;
        progress(this);
       }
      i.close();
      o.close();
      finished = true;                                                          // Finished
      finished(this);
     }
    catch(Exception x)                                                          // Exception
     {exception = x;
      failed = true; failed(this);
     }
   }
// Override these methods to observe progress
  public void starting(DownloadFile d) {}                                       // Override to receive number of bytes to be downloaded
  public void progress(DownloadFile d) {}                                       // Override to receive updates on how many bytes have been downloaded
  public void finished(DownloadFile d) {}                                       // Override called after the download has completed
  public void failed  (DownloadFile d) {}                                       // Override called on HTTP failure

  public void stopDownload() {stop = true;}                                     // Stop the download

  public static void main(String[] args)                                        // Download a url to a file
   {final DownloadFile d = new DownloadFile
     ("http://www.agecon.okstate.edu/quicken/files/2009/AssetAcct.zip",         // Sample url
      "zzz.zip")                                                                // File
     {public void starting(DownloadFile d) {say("Start ",       d.contentLength);}
      public void progress(DownloadFile d) {say("Progress ",    d.progress);}
      public void finished(DownloadFile d) {say("Finished");}
      public void failed  (DownloadFile d)
       {say("Http failed ", d.httpResponse);
        if (d.exception != null)
         {say("Exception ", d.exception);
          d.exception.printStackTrace();
         }
       }
     };

    d.start();                                                                  // Start a download
   }

  static void say(Object...O)                                                   // Say something
   {final StringBuilder b = new StringBuilder();
    for(Object o: O) b.append(o.toString());
    System.err.print(b.toString()+"\n");
   }
 }
