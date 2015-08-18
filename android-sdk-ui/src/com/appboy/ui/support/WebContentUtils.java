package com.appboy.ui.support;

import com.appboy.Constants;
import com.appboy.models.IInAppMessageHtml;
import com.appboy.support.AppboyLogger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WebContentUtils {
  private static final String TAG = String.format("%s.%s", Constants.APPBOY_LOG_TAG_PREFIX, WebContentUtils.class.getName());
  private static final String FILE_URI_SCHEME_PREFIX = "file://";
  private static final int ZIP_UNPACK_BYTE_BUFFER_LENGTH = 8192;

  // Default visibility for testing
  static final String HTML_INAPP_MESSAGES_FOLDER_NAME = "appboy-html-inapp-messages";

  /**
   * Takes a remote url pointing to zip file assets and then downloads them into a local directory.
   * <p/>
   * The local content folder will be created one level under localCacheDirectory and the assets from
   * remoteZipUrl will be unzipped into that location. Finally, this method will return the local URL pointing to the assets
   * using the "file://" protocol.
   *
   * We clear the cache directory before any downloads in case orphaned IAMs exist on the system.
   *
   * In case of error, any files created will be deleted. This includes any files downloaded or unzipped.
   *
   * @param localCacheDirectory the root directory path where the assets will be downloaded
   * @param remoteZipUrl        the URL for the assets zip
   * @return the local web content directory on the file system. Returns null if the download fails.
   */
  public static String getLocalHtmlUrlFromRemoteUrl(File localCacheDirectory, String remoteZipUrl) {
    if (localCacheDirectory == null) {
      AppboyLogger.w(TAG, "Internal cache directory is null. No local URL will be created.");
      return null;
    }
    if (StringUtils.isNullOrBlank(remoteZipUrl)) {
      AppboyLogger.w(TAG, "Remote zip url is null or empty. No local URL will be created.");
      return null;
    }

    // Make sure there are no orphaned IAMs in our cache directory.
    clearEntireHtmlInAppMessageCache(localCacheDirectory);

    String appboyHtmlInAppMessagesFolder = localCacheDirectory.getAbsolutePath() + "/" + HTML_INAPP_MESSAGES_FOLDER_NAME;
    // We need to uniquely identify the inAppMessage in some way in the html folder
    String inappMessageIdentifier = String.valueOf(System.currentTimeMillis());
    String unpackDirectoryPath = appboyHtmlInAppMessagesFolder + "/" + inappMessageIdentifier;

    AppboyLogger.d(TAG, "Starting download of url: " + remoteZipUrl);
    // Download the zip file
    File zipFile = downloadFileToPath(unpackDirectoryPath, remoteZipUrl, inappMessageIdentifier);
    if (zipFile == null) {
      AppboyLogger.d(TAG, "Could not download zip file to local storage.");
      // Clean up after ourselves in case of error
      deleteFileOrDirectory(new File(unpackDirectoryPath));
      return null;
    }
    AppboyLogger.d(TAG, "Html content zip downloaded.");

    // Unzip into a directory after creating it
    boolean unpackSuccessful = unpackZipIntoDirectory(unpackDirectoryPath, zipFile);
    if (!unpackSuccessful) {
      AppboyLogger.w(TAG, "Error during the zip unpack.");
      // Clean up after ourselves in case of error
      deleteFileOrDirectory(new File(unpackDirectoryPath));
      return null;
    }
    AppboyLogger.d(TAG, "Html content zip unpacked.");

    // Return a proper file protocol URL. The trailing slash ensures that the URL properly points to
    // the unpacked file location
    return FILE_URI_SCHEME_PREFIX + unpackDirectoryPath + "/";
  }

  /**
   * @param downloadDirectoryAbsolutePath the absolute file path to the download directory. Must begin with "/".
   * @param zipFileUrl                    the url of the desired zip file
   * @param outputFilename                filename of the end result file with no file extension
   * @return the downloaded zip file. If then file could not be downloaded, then returns null
   */
  // Default visibility for testing
  static File downloadFileToPath(String downloadDirectoryAbsolutePath, String zipFileUrl, String outputFilename) {
    if (StringUtils.isNullOrBlank(downloadDirectoryAbsolutePath)) {
      AppboyLogger.i(TAG, "Download directory null or blank. Zip file not downloaded.");
      return null;
    }
    if (StringUtils.isNullOrBlank(zipFileUrl)) {
      AppboyLogger.i(TAG, "Zip file url null or blank. Zip file not downloaded.");
      return null;
    }
    if (StringUtils.isNullOrBlank(outputFilename)) {
      AppboyLogger.i(TAG, "Output filename null or blank. Zip file not downloaded.");
      return null;
    }
    // Via http://stackoverflow.com/questions/1714761/download-a-file-programatically-on-android
    File downloadDir = new File(downloadDirectoryAbsolutePath);
    downloadDir.mkdirs();
    File outputFile = new File(downloadDirectoryAbsolutePath, outputFilename + ".zip");

    HttpURLConnection urlConnection = null;
    DataInputStream dataInputStream = null;
    BufferedOutputStream bufferedOutputStream = null;
    try {
      URL url = new URL(zipFileUrl);
      urlConnection = (HttpURLConnection) url.openConnection();
      // Check the response from the server
      int responseCode = urlConnection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        AppboyLogger.d(TAG, String.format("HTTP response code was %s. Zip file with url %s could not be downloaded.", responseCode, zipFileUrl));
        return null;
      }
      int contentLength = urlConnection.getContentLength();
      if (contentLength == -1) {
        AppboyLogger.d(TAG, String.format("Content length to the zip file could not be set. Zip file with url %s could not be downloaded.", zipFileUrl));
        return null;
      }
      byte[] byteBuffer = new byte[contentLength];

      // Get the input stream.
      dataInputStream = new DataInputStream(url.openStream());
      dataInputStream.readFully(byteBuffer);
      dataInputStream.close();
      urlConnection.disconnect();

      // Pipe to the file output stream
      bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
      bufferedOutputStream.write(byteBuffer);
      bufferedOutputStream.close();
    } catch (MalformedURLException e) {
      AppboyLogger.e(TAG, "MalformedURLException during download of zip file from url.", e);
      return null;
    } catch (IOException e) {
      AppboyLogger.e(TAG, "IOException during download of zip file from url.", e);
      return null;
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Exception during download of zip file from url.", e);
      return null;
    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
      try {
        if (dataInputStream != null) {
          dataInputStream.close();
        }
        if (bufferedOutputStream != null) {
          bufferedOutputStream.close();
        }
      } catch (IOException e) {
        AppboyLogger.e(TAG, "IOException during closing of zip file download streams.", e);
      }
    }

    return outputFile;
  }

  /**
   * Takes a zip file and unzips it into a destination directory. Creates the destination directory if
   * not already present.
   *
   * @param unpackDirectory the absolute path to the destination directory
   * @param zipFile         the file to be unpacked
   * @return true if the unzipping procedure was successful
   */
  // Default visibility for testing
  static boolean unpackZipIntoDirectory(String unpackDirectory, File zipFile) {
    if (StringUtils.isNullOrBlank(unpackDirectory)) {
      AppboyLogger.i(TAG, "Unpack directory null or blank. Zip file not unpacked.");
      return false;
    }
    if (zipFile == null) {
      AppboyLogger.i(TAG, "Zip file is null. Zip file not unpacked.");
      return false;
    }
    // Via: http://stackoverflow.com/questions/3382996/how-to-unzip-files-programmatically-in-android
    File unpackDir = new File(unpackDirectory);
    unpackDir.mkdirs();

    ZipInputStream zipInputStream = null;
    BufferedOutputStream bufferedOutputStream = null;
    try {
      // Get the zip file as buffered input
      FileInputStream fileInputStream = new FileInputStream(zipFile);
      BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
      zipInputStream = new ZipInputStream(bufferedInputStream);

      // Setup the variables for the buffers
      String entryFilename;
      ZipEntry zipEntry;
      byte[] buffer = new byte[ZIP_UNPACK_BYTE_BUFFER_LENGTH];
      int byteCount;
      String absoluteEntryPath;

      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        entryFilename = zipEntry.getName();
        if (entryFilename.toLowerCase().startsWith("__macosx")) {
          // Mac creates these files as indexes, skip them
          continue;
        }
        absoluteEntryPath = unpackDirectory + "/" + entryFilename;

        // If the entry is a directory, then create it and continue the loop. Even though the zip file
        // is expected to be a flat folder, we don't want to explode on future releases.
        if (zipEntry.isDirectory()) {
          File zipEntryFile = new File(absoluteEntryPath);
          zipEntryFile.mkdirs();
          continue;
        }

        // Since we're dealing with a zip entry, flush the bytes to the file stream
        bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(absoluteEntryPath));
        while ((byteCount = zipInputStream.read(buffer)) != -1) {
          bufferedOutputStream.write(buffer, 0, byteCount);
        }

        // Close our streams
        bufferedOutputStream.close();
        zipInputStream.closeEntry();
      }

      zipInputStream.close();
      return true;
    } catch (FileNotFoundException e) {
      AppboyLogger.e(TAG, "FileNotFoundException during unpack of zip file.", e);
      return false;
    } catch (IOException e) {
      AppboyLogger.e(TAG, "IOException during unpack of zip file.", e);
      return false;
    } catch (Exception e) {
      AppboyLogger.e(TAG, "Exception during unpack of zip file.", e);
      return false;
    } finally {
      try {
        if (zipInputStream != null) {
          zipInputStream.close();
        }
        if (bufferedOutputStream != null) {
          bufferedOutputStream.close();
        }
      } catch (IOException e) {
        AppboyLogger.e(TAG, "IOException during closing of zip file unpacking streams.", e);
      }
    }
  }

  /**
   * Clears the folder containing all the HTML In-App Messages in the local storage. This includes
   * any downloaded zip files and unpacked zip files. Adapted from http://stackoverflow.com/questions/18200811/android-clear-cache-programmatically
   *
   * @param localCacheDirectory the local cache directory, NOT the IAM cache.  The IAM cache path will be constructed
   * from the local cache directory.
   */
  public static void clearEntireHtmlInAppMessageCache(File localCacheDirectory) {
    AppboyLogger.d(TAG, "Deleting Html In App Messages folder");
    File appboyHtmlCacheDirectory = new File(localCacheDirectory, HTML_INAPP_MESSAGES_FOLDER_NAME);
    deleteFileOrDirectory(appboyHtmlCacheDirectory);
  }

  /**
   * @param inAppMessage the IInAppMessageHtml to have its assets cleared from the local cache
   */
  public static void clearInAppMessageLocalAssets(IInAppMessageHtml inAppMessage) {
    if (inAppMessage == null) {
      AppboyLogger.d(TAG, "Could not clear InAppMessage Local Assets for a null IInAppMessageHtml. Doing nothing.");
      return;
    }
    String localDirectoryPath = inAppMessage.getLocalAssetsDirectoryUrl();
    if (StringUtils.isNullOrBlank(localDirectoryPath)) {
      // This could occur if the assets download was unsuccessful
      AppboyLogger.d(TAG, "IInAppMessageHtml has null local assets file url. Doing nothing.");
      return;
    }

    // Get the actual file directory for the assets
    int startIndex = FILE_URI_SCHEME_PREFIX.length();
    int endIndex = localDirectoryPath.length() - 1;
    String assetsDirectoryAbsolutePath = localDirectoryPath.substring(startIndex, endIndex);

    AppboyLogger.d(TAG, "Deleting local html assets with path: " + assetsDirectoryAbsolutePath);
    deleteFileOrDirectory(new File(assetsDirectoryAbsolutePath));
  }

  /**
   * Recursively deletes all the files under a directory. If the input file is not a directory, then
   * this only deletes the file. This is recursive since directories must be empty before deletion.
   *
   * @param fileOrDirectory a file or directory to be deleted.
   */
  // Default visibility for testing
  static void deleteFileOrDirectory(File fileOrDirectory) {
    if (fileOrDirectory == null || !fileOrDirectory.exists()) {
      // If the file is somehow malformed, we can't delete it
      return;
    }

    // If the file is a directory, then recursively delete all of its children
    if (fileOrDirectory.isDirectory()) {
      for (String childFileName : fileOrDirectory.list()) {
        deleteFileOrDirectory(new File(fileOrDirectory, childFileName));
      }
    }
    // Now that any potential children have been deleted, delete the file
    fileOrDirectory.delete();
  }
}
